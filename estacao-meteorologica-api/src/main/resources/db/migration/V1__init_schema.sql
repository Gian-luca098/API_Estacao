-- ============================================================================
-- Estação Meteorológica — schema inicial do Supabase
-- ----------------------------------------------------------------------------
-- Migration do Flyway espelhando, sem alterações de estrutura, o arquivo
-- supabase/migrations/0001_init.sql do projeto front-end (Front_Estacao-main).
-- É aplicada automaticamente pelo Flyway na subida da aplicação (ver
-- application.yml -> spring.flyway). Se você já rodou esse SQL manualmente no
-- SQL Editor do Supabase antes de usar este backend, não tem problema: o
-- "baseline-on-migrate: true" faz o Flyway apenas registrar o schema
-- existente como já migrado, sem tentar recriá-lo.
--
-- Tabelas:
--   1. stations   -> metadados de cada estação física
--   2. readings   -> leituras de sensores em série temporal (fonte da verdade)
--   3. forecasts  -> previsão do tempo para as próximas horas
--   4. sensors    -> status de saúde de cada sensor físico da estação
--
-- Valores derivados (ponto de orvalho, tendência de pressão, agregação
-- horária do gráfico) são propositalmente NÃO armazenados aqui: no front-end
-- original eles são calculados no cliente (src/lib/meteorology.ts e
-- src/lib/aggregate.ts) a partir das leituras brutas. Esta API mantém o
-- mesmo princípio e expõe apenas os dados normalizados.
-- ============================================================================

create extension if not exists "pgcrypto";

-- ---------------------------------------------------------------------------
-- 1. stations
-- ---------------------------------------------------------------------------
create table if not exists public.stations (
  id uuid primary key default gen_random_uuid(),
  code text not null unique,
  name text not null,
  latitude double precision not null,
  longitude double precision not null,
  altitude_m numeric not null,
  firmware_version text not null,
  reading_interval_seconds integer not null default 60,
  created_at timestamptz not null default now()
);

comment on table public.stations is 'Metadados de cada estação meteorológica física.';
comment on column public.stations.code is 'Identificador curto exibido no painel, ex.: "EMT-04".';

-- ---------------------------------------------------------------------------
-- 2. readings — série temporal das leituras dos sensores
-- ---------------------------------------------------------------------------
create table if not exists public.readings (
  id bigint generated always as identity primary key,
  station_id uuid not null references public.stations (id) on delete cascade,
  recorded_at timestamptz not null default now(),
  temperature_c numeric not null,
  humidity_pct numeric not null check (humidity_pct between 0 and 100),
  pressure_hpa numeric not null,
  rain_last_hour_mm numeric not null default 0,
  rain_24h_mm numeric not null default 0,
  wind_speed_kmh numeric not null default 0,
  wind_gust_24h_kmh numeric,
  wind_avg_1h_kmh numeric,
  wind_direction_deg numeric not null check (wind_direction_deg >= 0 and wind_direction_deg < 360)
);

comment on table public.readings is 'Leituras brutas dos sensores, uma linha por ciclo de leitura (padrão: 60s).';
comment on column public.readings.rain_last_hour_mm is
  'Chuva acumulada na última hora a partir do momento desta leitura (janela móvel, não um delta desde a leitura anterior).';

create index if not exists readings_station_recorded_idx
  on public.readings (station_id, recorded_at desc);

-- ---------------------------------------------------------------------------
-- 3. forecasts — previsão para as próximas horas
-- ---------------------------------------------------------------------------
create table if not exists public.forecasts (
  id bigint generated always as identity primary key,
  station_id uuid not null references public.stations (id) on delete cascade,
  forecast_for timestamptz not null,
  label text not null,
  temperature_c numeric not null,
  rain_chance_pct numeric not null check (rain_chance_pct between 0 and 100),
  icon text not null check (icon in ('sun', 'cloud', 'cloud-rain')),
  created_at timestamptz not null default now()
);

comment on table public.forecasts is 'Pontos de previsão futuros exibidos no painel "Previsão".';

create index if not exists forecasts_station_time_idx
  on public.forecasts (station_id, forecast_for asc);

-- ---------------------------------------------------------------------------
-- 4. sensors — status de saúde de cada sensor físico
-- ---------------------------------------------------------------------------
create table if not exists public.sensors (
  id bigint generated always as identity primary key,
  station_id uuid not null references public.stations (id) on delete cascade,
  name text not null,
  status text not null default 'ok' check (status in ('ok', 'warning', 'offline')),
  last_checked_at timestamptz not null default now()
);

comment on table public.sensors is
  'Status atual de cada sensor físico (temperatura, umidade, pluviômetro, anemômetro, barômetro...).';

-- ---------------------------------------------------------------------------
-- Row Level Security — mantido como no projeto original: leitura pública via
-- chave anônima do Supabase para o painel React. Esta API Spring Boot conecta
-- diretamente como usuário "postgres" (via JDBC), que é o dono das tabelas e
-- não é afetado pelas policies de RLS abaixo — elas continuam protegendo o
-- acesso via chave anon/PostgREST do front-end.
-- ---------------------------------------------------------------------------
alter table public.stations enable row level security;
alter table public.readings enable row level security;
alter table public.forecasts enable row level security;
alter table public.sensors enable row level security;

drop policy if exists "Leitura pública de stations" on public.stations;
create policy "Leitura pública de stations" on public.stations
  for select using (true);

drop policy if exists "Leitura pública de readings" on public.readings;
create policy "Leitura pública de readings" on public.readings
  for select using (true);

drop policy if exists "Leitura pública de forecasts" on public.forecasts;
create policy "Leitura pública de forecasts" on public.forecasts
  for select using (true);

drop policy if exists "Leitura pública de sensors" on public.sensors;
create policy "Leitura pública de sensors" on public.sensors
  for select using (true);

-- ---------------------------------------------------------------------------
-- Realtime — habilita eventos em tempo real na tabela de leituras, usada
-- pelos hooks useLatestReading/useReadingsHistory do front-end original.
-- Publicação padrão do Supabase; se o projeto já a tiver (comum), o "if not
-- exists" evita erro de "relation is already member of publication".
-- ---------------------------------------------------------------------------
do $$
begin
  if not exists (
    select 1 from pg_publication_tables
    where pubname = 'supabase_realtime'
      and schemaname = 'public'
      and tablename = 'readings'
  ) then
    alter publication supabase_realtime add table public.readings;
  end if;
end $$;
