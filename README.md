# Predicto

Community esports prediction/tipping platform. League of Legends match predictions with points, rank ladder, and seasonal leaderboards.

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- Git

## Quick Start

### 1. Start local infrastructure

```bash
docker compose up -d
```

This starts:
- **PostgreSQL 16** on port 5432
- **Redis 7** on port 6379
- **Adminer** on port 8081 (DB admin UI)

### 2. Run the application

```bash
mvn spring-boot:run
```

The app connects to `localhost:5432` and `localhost:6379` by default. Flyway runs all pending migrations on startup.

### 3. Verify

- **Health endpoint**: `GET http://localhost:8080/api/health` returns `{"status":"UP","timestamp":"..."}`
- **Actuator health**: `GET http://localhost:8080/actuator/health`
- **Adminer**: http://localhost:8081 (server=`postgres`, user=`predicto`, password=`predicto`, database=`predicto`)

## Configuration

All settings are in `src/main/resources/application.yml`. Override via environment variables:

| Variable | Default | Description |
|---|---|---|
| `DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/predicto` | JDBC URL |
| `DATASOURCE_USERNAME` | `predicto` | DB user |
| `DATASOURCE_PASSWORD` | `predicto` | DB password |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `CITO_API_KEY` | *(empty)* | CitoAPI key (for real API mode) |

## CitoAPI Mock Mode

This phase uses **mocked CitoAPI data** by default (`cito.api.mock-enabled=true` in `application.yml`).
The mock reads static fixture files from `src/main/resources/cito-fixtures/` and generates
match schedules with relative timestamps so the full sync pipeline is exercisable immediately.

To switch to the real CitoAPI:
1. Set `cito.api.mock-enabled=false` in `application.yml`
2. Set `CITO_API_KEY` environment variable
3. Verify field mappings against live API responses (the `HttpCitoApiClient` was built from
   published documentation, not tested against a live endpoint)

No code changes are needed — both implementations satisfy the same `CitoApiClient` interface,
and the sync services depend only on that interface.

## Manual Sync Trigger

A temporary, unsecured endpoint is available for manual testing:

```bash
# Populate leagues, teams, and players from mock fixtures
curl -X POST "http://localhost:8080/api/admin/sync/trigger?job=catalog"

# Populate matches from mock schedule
curl -X POST "http://localhost:8080/api/admin/sync/trigger?job=schedule"

# Resolve results for past matches
curl -X POST "http://localhost:8080/api/admin/sync/trigger?job=results"

# Lock matches starting within 15 minutes
curl -X POST "http://localhost:8080/api/admin/sync/trigger?job=lock"
```

This endpoint is **unsecured** and will be removed or locked behind admin auth in a later phase.

## Scheduled Jobs

| Job | Interval | Description |
|---|---|---|
| Catalog sync | 12 hours | Syncs leagues, teams, players |
| Schedule sync | 30 minutes | Syncs match schedule |
| Result sync | 5 minutes | Resolves finished match results |
| Lock job | 1 minute | Locks matches 15 min before start |

All jobs write a row to the `sync_runs` table per execution with status and item count.

## Project Structure

```
com.predicto
├── admin/       – Sync trigger controller (temporary)
├── auth/        – User entity & repository
├── betting/     – Bets, match odds, score odds
├── catalog/
│   ├── cito/    – CitoApiClient interface, DTOs, mock and HTTP implementations
│   ├── sync/    – Scheduled sync services, SyncRun entity
│   └── ...      – Leagues, teams, players, matches entities & repositories
├── common/      – Base entity, audit log, enums
├── health/      – Health check endpoint
├── pickban/     – Pick/ban predictions
├── season/      – Seasons, rank tiers, leaderboard, rewards
└── wallet/      – Wallets
```

## Build

```bash
mvn clean install
```
