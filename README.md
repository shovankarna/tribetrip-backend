# TripTribe Backend

This repository contains the backend microservices and infrastructure for the TripTribe application.

## 🛠 Tech Stack
- **Java 17+** (Spring Boot 3)
- **PostgreSQL** (Database)
- **Keycloak** (Identity Management)
- **Docker & Docker Compose** (Containerization)

## 🚀 Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 17 SDK (for local development)

### Environment Setup
1. Copy the example environment file (if any) or create `.env` in `infra/` folder.
2. The `.env` file should contain the following secrets (do NOT commit this file):

```properties
POSTGRES_USER=
POSTGRES_PASSWORD=
POSTGRES_DB=
DB_PORT=5432
PGADMIN_EMAIL=
PGADMIN_PASSWORD=
PGADMIN_PORT=
KEYCLOAK_ADMIN=
KEYCLOAK_ADMIN_PASSWORD=
KC_DB_URL=jdbc:postgresql://postgres:5432/<db>
KC_DB_USER=
KC_DB_PASSWORD=
USER_SERVICE_PORT=
USER_DB_URL=jdbc:postgresql://postgres:5432/<db>
USER_DB_USERNAME=
USER_DB_PASSWORD=
```

### Running the Application
To start all services (Database, Auth, User Service):

```bash
cd infra
docker-compose up -d --build
```

### Services
- **User Service**: `http://localhost:8081/api`
- **Keycloak Console**: `http://localhost:8080`
- **pgAdmin**: `http://localhost:5050`
