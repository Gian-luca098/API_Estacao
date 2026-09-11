# Estação Meteorológica — API (Spring Boot)

API REST em Java 21 + Spring Boot, construída a partir da análise do front-end React/TypeScript
em `Front_Estacao-main`, persistindo no mesmo banco PostgreSQL gerenciado pelo Supabase que o
front-end já usa.

## 1. O que foi extraído do front-end

| Front-end (`Front_Estacao-main`) | Uso nesta API |
|---|---|
| `.env` → `VITE_SUPABASE_URL=https://eyuggwjixeuraiqjbnze.supabase.co` | Ref do projeto (`eyuggwjixeuraiqjbnze`) usado para montar o host JDBC padrão em `application.yml` |
| `.env` → `VITE_SUPABASE_ANON_KEY` | **Não usado.** Autentica só a API REST/Realtime (PostgREST) do Supabase; a conexão JDBC direta usa a senha do banco (ver seção 3) |
| `supabase/migrations/0001_init.sql` | Copiado para `src/main/resources/db/migration/V1__init_schema.sql` (Flyway) |
| `supabase/seed.sql` | Copiado para `src/main/resources/db/seed-demo-data.sql` (referência opcional, não roda automático) |
| `src/types/weather.ts` | Base dos DTOs de resposta (mesmos nomes de campo em camelCase) |
| `src/hooks/useStation.ts` | `GET /api/stations/code/{code}` |
| `src/hooks/useLatestReading.ts` | `GET /api/stations/{stationId}/readings/latest` |
| `src/hooks/useReadingsHistory.ts` | `GET /api/stations/{stationId}/readings/history?hours=24` |
| `src/hooks/useForecast.ts` | `GET /api/stations/{stationId}/forecasts?limit=6` |
| `src/hooks/useSensors.ts` | `GET /api/stations/{stationId}/sensors` |

`src/lib/meteorology.ts`, `aggregate.ts` e `format.ts` calculam ponto de orvalho, tendência de
pressão e agregação horária **no cliente**, a partir das leituras brutas — de propósito, nada
disso é persistido no banco. Esta API mantém o mesmo princípio: serve dados normalizados, e
esses cálculos continuam sendo responsabilidade do front-end.

## 2. Arquitetura e decisões de projeto

```
src/main/java/com/estacao/meteorologica/
├── EstacaoMeteorologicaApplication.java
├── config/
│   └── CorsConfig.java              # libera localhost:5173/4173/3000 para /api/**
├── model/
│   ├── Station.java  Reading.java  Forecast.java  Sensor.java
│   ├── enums/          # ForecastIcon, SensorStatus (com valor de fio explícito)
│   └── converter/      # AttributeConverter<Enum, String> para as duas acima
├── repository/         # StationRepository, ReadingRepository, ForecastRepository, SensorRepository
├── dto/                # *Request (records + Bean Validation) e *Response (records)
├── service/            # regras de negócio, defaults e conversão Entity <-> DTO
├── controller/         # endpoints REST
└── exception/          # ResourceNotFoundException, DuplicateResourceException,
                         # ApiError, GlobalExceptionHandler (@RestControllerAdvice)
```

Pontos que exigiram atenção na análise do schema (`0001_init.sql`) e foram tratados
deliberadamente:

- **Enums com valor de fio diferente do nome da constante.** A coluna `forecasts.icon` tem
  `check (icon in ('sun', 'cloud', 'cloud-rain'))` — "cloud-rain" tem hífen e não é um
  identificador Java válido; `sensors.status` espera minúsculas (`'ok'`, `'warning'`,
  `'offline'`). Um `@Enumerated(EnumType.STRING)` ingênuo gravaria `"CLOUD_RAIN"` / `"OK"` e
  violaria a constraint. Por isso `ForecastIcon`/`SensorStatus` carregam um valor de fio
  explícito, usado tanto pelo Jackson (`@JsonValue`/`@JsonCreator`, JSON) quanto por um
  `AttributeConverter` dedicado (coluna do banco).
- **`BigDecimal` para colunas `numeric`, `Double` para `double precision`.** `latitude` e
  `longitude` são `double precision` no schema; todo o resto de sensores (temperatura, umidade,
  pressão, chuva, vento) é `numeric`, mapeado para `BigDecimal` para não introduzir erro de
  ponto flutuante em dados de sensor.
- **Sem coleções `@OneToMany` em `Station`.** `readings` é uma série temporal que pode crescer
  bastante; manter listas bidirecionais convidaria a N+1 e problemas de serialização. Cada
  entidade filha referencia a estação via `@ManyToOne` unidirecional; a exclusão em cascata
  continua garantida pelo `on delete cascade` do schema (apagar uma estação apaga suas
  readings/forecasts/sensors).
- **Schema gerenciado por Flyway, não pelo Hibernate.** `ddl-auto: validate` — o Hibernate só
  confere se as entidades batem com o schema real na subida (falha rápido, com mensagem clara,
  se algo divergir); quem cria/altera tabelas é sempre o SQL versionado em
  `db/migration`. `baseline-on-migrate: true` cobre o caso comum de você já ter rodado o SQL
  manualmente no editor do Supabase antes de usar esta API.
- **Endpoints "espelho" + CRUD completo.** Além do CRUD genérico (`GET/POST/PUT/DELETE`) por
  recurso, existem rotas dedicadas (`/latest`, `/history`, `/code/{code}`) que replicam
  exatamente a query de cada hook React, já com os mesmos parâmetros e defaults (`hours=24`,
  `limit` opcional para forecasts).
- **Validação espelhando os `check constraints`.** Ex.: `humidityPct` em `[0, 100]` e
  `windDirectionDeg` em `[0, 360)` são validados via Bean Validation antes de chegar ao banco,
  devolvendo 400 com mensagem legível em vez de uma violação de constraint (que resultaria em
  409/500 vindo direto do driver JDBC).
- **Versão do Spring Boot: 3.5.16, não 4.1.x.** Em setembro de 2026, a linha 4.x (Framework 7)
  já é a atualmente suportada pela Spring — mas ela chegou com mudanças de ruptura relevantes
  (Jackson 2 → 3, novos padrões do Spring Security, remoção de APIs). Como não há acesso ao
  Maven Central neste ambiente para compilar e validar o projeto de fato, optei pela linha
  3.5.x (Jakarta EE 10 / Hibernate 6), a última da geração 3.x e uma combinação que sei, com
  alta confiança, que compila e roda corretamente. O build via Docker (que roda na sua máquina,
  com acesso normal à internet) valida a compilação de verdade. Migrar para o Boot 4.x depois é
  possível — avise se quiser ajuda com isso.

## 3. Pré-requisitos

- Um projeto Supabase já com o schema aplicado (ver seção 4).
- A **senha do banco Postgres** desse projeto: painel do Supabase → *Project Settings* →
  *Database* → *Database password* (ou redefina ali se não a tiver salva). **Não é** a
  `VITE_SUPABASE_ANON_KEY` do `.env` do front-end — são credenciais diferentes, para propósitos
  diferentes.
- Para rodar local sem Docker: JDK 21 e Maven 3.9+.
- Para rodar via Docker: Docker e Docker Compose. Não precisa de JDK/Maven instalado — o
  Dockerfile faz o build dentro de um container.

## 4. Conectando ao Supabase

Por padrão, `application.yml`/`docker-compose.yml` já apontam para o projeto identificado no
`.env` do front-end (`db.eyuggwjixeuraiqjbnze.supabase.co:5432`), a **conexão direta**. Ela usa
IPv6; se sua rede/Docker Desktop só tiver saída IPv4, a conexão trava. Nesse caso, troque
`DB_HOST`/`DB_PORT`/`DB_USER` pelos valores do **Session pooler** (IPv4) do painel do Supabase
(*Project Settings → Database → Connect*), algo como:

```
DB_HOST=aws-0-<regiao>.pooler.supabase.com
DB_PORT=5432
DB_USER=postgres.eyuggwjixeuraiqjbnze
```

Na primeira subida, o Flyway aplica automaticamente `V1__init_schema.sql` (o mesmo schema de
`supabase/migrations/0001_init.sql`). Se as tabelas já existirem (você seguiu o README original
do front-end e rodou o SQL manualmente), o Flyway detecta isso e só passa a controlar as
próximas migrations, sem tentar recriar nada.

Dados de exemplo são opcionais: rode `src/main/resources/db/seed-demo-data.sql` no SQL Editor
do Supabase se quiser a estação `EMT-04` já populada com leituras/previsões/sensores fake.

## 5. Rodando localmente (sem Docker)

```bash
export DB_PASSWORD=coloque_a_senha_do_banco_aqui
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Todas as outras variáveis (`DB_HOST`, `DB_PORT`,
`CORS_ALLOWED_ORIGINS` etc.) têm defaults em `application.yml` e só precisam ser exportadas se
você quiser sobrescrevê-los.

## 6. Rodando com Docker

```bash
cp .env.example .env
# edite o .env e preencha DB_PASSWORD (e troque DB_HOST/PORT/USER se precisar do pooler)

docker compose up --build
```

A API fica disponível em `http://localhost:8080`. `docker compose down` para o container.

## 7. Endpoints disponíveis

Todas as respostas são JSON; erros seguem o formato da seção 8.

### Stations
| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/stations` | Lista todas as estações |
| GET | `/api/stations/{id}` | Busca por id (UUID) |
| GET | `/api/stations/code/{code}` | Busca por código (ex.: `EMT-04`) — espelha `useStation` |
| POST | `/api/stations` | Cria uma estação → `201` + `Location` |
| PUT | `/api/stations/{id}` | Atualiza (substituição completa) |
| DELETE | `/api/stations/{id}` | Remove (cascade em readings/forecasts/sensors) |

### Readings
| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/stations/{stationId}/readings/latest` | Última leitura — espelha `useLatestReading` |
| GET | `/api/stations/{stationId}/readings/history?hours=24` | Janela de tempo — espelha `useReadingsHistory` |
| GET | `/api/stations/{stationId}/readings` | Todas as leituras da estação (mais recentes primeiro) |
| POST | `/api/stations/{stationId}/readings` | Cria uma leitura para essa estação → `201` |
| GET | `/api/readings?page=0&size=50` | Listagem paginada entre todas as estações |
| GET / PUT / DELETE | `/api/readings/{id}` | CRUD direto por id da leitura |

### Forecasts
| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/stations/{stationId}/forecasts` | Todas as previsões da estação |
| GET | `/api/stations/{stationId}/forecasts?limit=6` | Espelha `useForecast(stationId, 6)` |
| POST | `/api/stations/{stationId}/forecasts` | Cria uma previsão → `201` |
| GET / PUT / DELETE | `/api/forecasts/{id}` | CRUD direto por id |

### Sensors
| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/stations/{stationId}/sensors` | Sensores da estação — espelha `useSensors` |
| POST | `/api/stations/{stationId}/sensors` | Cria um sensor → `201` |
| GET / PUT / DELETE | `/api/sensors/{id}` | CRUD direto por id |

### Infraestrutura
| Método | Rota | Descrição |
|---|---|---|
| GET | `/actuator/health` | Usado pelo `HEALTHCHECK` do Docker |

## 8. Formato de erro

Todo erro (400/404/409/500) devolvido pelo `GlobalExceptionHandler` segue este formato:

```json
{
  "timestamp": "2026-09-09T14:32:10.123-03:00",
  "status": 404,
  "error": "Not Found",
  "message": "Nenhuma estação encontrada com o código 'EMT-99'.",
  "path": "/api/stations/code/EMT-99",
  "details": null
}
```

Em erros de validação (`400`), `details` traz uma lista de mensagens por campo, ex.:
`["humidityPct: A umidade deve ser menor ou igual a 100."]`.

## 9. Testes

`mvn test` roda os testes unitários incluídos (conversores de enum e validação de DTOs) — eles
não dependem de banco de dados, então funcionam mesmo sem acesso ao Supabase. Não há testes de
integração com banco real neste entrega; se quiser, o próximo passo natural é
`spring-boot-testcontainers` + Testcontainers PostgreSQL.

## 10. Próximos passos sugeridos

- **Autenticação nas rotas de escrita.** Hoje POST/PUT/DELETE estão abertos, replicando o fato
  de que o front-end original só lê. Antes de expor isso publicamente, vale adicionar
  Spring Security (API key para o firmware da estação, ou OAuth2/JWT para um painel admin).
- **OpenAPI/Swagger** (`springdoc-openapi`) para documentação interativa dos endpoints.
- **Testcontainers** para testes de integração reais contra um Postgres efêmero.
- Migrar para **Spring Boot 4.x** quando quiser acompanhar a linha atualmente suportada pela
  Spring — a mudança mais visível seria a migração do Jackson 2 para 3.
