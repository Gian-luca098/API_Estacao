-- ============================================================================
-- Dados de exemplo (OPCIONAL) — cópia de supabase/seed.sql do front-end.
-- ----------------------------------------------------------------------------
-- Este arquivo NÃO é executado automaticamente (não fica em db/migration,
-- então o Flyway o ignora). É só uma cópia de conveniência para quem quiser
-- popular a estação EMT-04 com dados de teste rapidamente.
--
-- Para rodar: cole o conteúdo no SQL Editor do Supabase, ou via psql:
--   psql "$DATABASE_URL" -f src/main/resources/db/seed-demo-data.sql
--
-- É reexecutável: os dados anteriores da estação de exemplo são removidos
-- antes de inserir novamente, então rodá-lo várias vezes não duplica linhas.
-- ============================================================================

do $$
declare
  v_station_id uuid := '11111111-1111-1111-1111-111111111111';
begin
  delete from public.sensors where station_id = v_station_id;
  delete from public.forecasts where station_id = v_station_id;
  delete from public.readings where station_id = v_station_id;

  insert into public.stations (id, code, name, latitude, longitude, altitude_m, firmware_version, reading_interval_seconds)
  values (v_station_id, 'EMT-04', 'Estação Tubarão/SC', -28.4666, -49.0064, 9, 'v2.3.1', 60)
  on conflict (code) do update set
    name = excluded.name,
    latitude = excluded.latitude,
    longitude = excluded.longitude,
    altitude_m = excluded.altitude_m,
    firmware_version = excluded.firmware_version,
    reading_interval_seconds = excluded.reading_interval_seconds;

  insert into public.readings (
    station_id, recorded_at, temperature_c, humidity_pct, pressure_hpa,
    rain_last_hour_mm, rain_24h_mm, wind_speed_kmh, wind_gust_24h_kmh,
    wind_avg_1h_kmh, wind_direction_deg
  )
  select
    v_station_id,
    now() - (horas_atras || ' hours')::interval,
    temperatura,
    62,
    1013.2,
    chuva,
    4.6,
    14,
    31,
    11,
    45 -- NE
  from (values
    (23, 19.5, 0.0), (22, 19.1, 0.0), (21, 19.3, 0.0), (20, 19.8, 0.0),
    (19, 20.4, 0.0), (18, 21.6, 0.0), (17, 23.2, 0.0), (16, 24.9, 0.0),
    (15, 26.1, 0.4), (14, 27.3, 1.2), (13, 28.0, 0.8), (12, 28.6, 0.0),
    (11, 29.1, 0.0), (10, 29.5, 0.0), (9,  29.8, 0.0), (8,  29.4, 0.6),
    (7,  28.7, 1.8), (6,  27.6, 0.0), (5,  26.2, 0.0), (4,  24.8, 0.0),
    (3,  23.1, 0.0), (2,  21.9, 0.0), (1,  20.8, 0.0), (0,  20.0, 0.0)
  ) as dados(horas_atras, temperatura, chuva);

  insert into public.forecasts (station_id, forecast_for, label, temperature_c, rain_chance_pct, icon)
  values
    (v_station_id, now(),                         'Agora', 28, 5,  'sun'),
    (v_station_id, now() + interval '3 hours',    '15h',   29, 10, 'sun'),
    (v_station_id, now() + interval '6 hours',    '18h',   26, 20, 'cloud'),
    (v_station_id, now() + interval '9 hours',    '21h',   23, 40, 'cloud-rain'),
    (v_station_id, now() + interval '12 hours',   '00h',   21, 60, 'cloud-rain'),
    (v_station_id, now() + interval '15 hours',   '03h',   19, 15, 'cloud');

  insert into public.sensors (station_id, name, status)
  values
    (v_station_id, 'Sensor de temperatura', 'ok'),
    (v_station_id, 'Sensor de umidade',     'ok'),
    (v_station_id, 'Pluviômetro',           'ok'),
    (v_station_id, 'Anemômetro',            'ok'),
    (v_station_id, 'Barômetro',             'ok');
end $$;
