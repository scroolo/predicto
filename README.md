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

## Project Structure

```
com.predicto
├── auth/        – User entity & repository
├── betting/     – Bets, match odds, score odds
├── catalog/     – Leagues, teams, players, matches
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
