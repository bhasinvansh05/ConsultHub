# EECS-3311 Project

## Docker setup

This repo includes Docker support for the backend and a PostgreSQL database.

### Prerequisites

- Docker Desktop (or Docker Engine + Compose)

### Run with Docker Compose

From the repository root:

1. Create your local environment file:

```bash
cp .env.example .env
# PowerShell alternative:
Copy-Item .env.example .env
```

2. Start containers:

```bash
docker compose up --build
```

Services:

- Backend: `http://localhost:8080`
- PostgreSQL: `localhost:5432`
  - database: `consulting_db`
  - user: `admin`
  - password: `changeme`

### Use Neon PostgreSQL

To use Neon instead of the local Docker Postgres container, set these values in `.env`:

```dotenv
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-dawn-bonus-aif7xij3-pooler.c-4.us-east-1.aws.neon.tech/consulting_db?sslmode=require&channel_binding=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=your_real_password
DB_SSLMODE=require
DB_CHANNEL_BINDING=require

# For running tests against Neon (instead of H2)
SPRING_TEST_DATABASE_REPLACE=NONE
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

Note: Neon provides a `postgresql://...` URL. For Spring Boot JDBC, use the `jdbc:postgresql://...` form shown above.

### Stop services

```bash
docker compose down
```

To remove database volume too:

```bash
docker compose down -v
```