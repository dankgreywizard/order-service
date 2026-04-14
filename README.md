
# Order Service

A Spring Boot 4.0.5 microservice for managing orders. Built with Java 25, Maven, PostgreSQL, and modern observability features.

## Tech Stack

- **Java**: 25
- **Spring Boot**: 4.0.5
- **Database**: PostgreSQL 18 (H2 for development)
- **Migrations**: Liquibase
- **Build Tool**: Maven
- **Container**: Docker with multi-stage builds

## Features

- RESTful API with OpenAPI/Swagger documentation
- Layered architecture (Controller → Service → Repository)
- Structured logging with Logstash encoder
- Metrics export to Prometheus
- Retry mechanisms with Spring Retry
- Security with Spring Security
- Database migrations with Liquibase
- DTO mapping with MapStruct
- Testcontainers for integration testing

## Project Structure

```
order-service/
├── src/
│   ├── main/
│   │   ├── java/com/skmcore/orderservice/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Request/Response DTOs
│   │   │   ├── event/           # Event classes
│   │   │   ├── exception/       # Custom exceptions
│   │   │   ├── mapper/          # MapStruct mappers
│   │   │   ├── model/           # JPA entities
│   │   │   ├── repository/      # Spring Data repositories
│   │   │   └── service/         # Business logic
│   │   └── resources/
│   │       ├── application.yml  # Application configuration
│   │       ├── logback-spring.xml
│   │       └── db/changelog/    # Liquibase migrations
│   └── test/
├── Dockerfile
├── .dockerignore
├── pom.xml
└── README.md
```

## Quick Start

### Manual Testing UI
The service includes a simple dashboard for manual testing, accessible at:
- **Landing Page**: [http://localhost:8080/](http://localhost:8080/)
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (dev/default profile only)

### Prerequisites

- Java 25
- Maven 3.9+
- Docker (optional, for containerized deployment)

### Build

Compile and package the application:

```bash
mvn clean package
```

### Run Locally (Dev Mode)

Run with the `dev` profile (uses H2 in-memory database):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Run with Docker

Build the Docker image:

```bash
docker build -t order-service .
```

Run the container:

```bash
docker run -p 8080:8080 order-service
```

## API Documentation

Once the application is running, you can access:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI Docs: `http://localhost:8080/v3/api-docs`

## Observability

- Health Check: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Prometheus: `http://localhost:8080/actuator/prometheus`

```bash
# Clean and package
mvn clean package

# Run tests
mvn test

# Skip tests (for faster builds)
mvn clean package -DskipTests
```

### Run Locally (Development Profile)

```bash
# Uses H2 in-memory database
mvn spring-boot:run

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run the JAR directly
java -jar target/order-service-*.jar
```

### Run with PostgreSQL (Staging Profile)

```bash
# Set environment variables
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=orderdb
export DB_USER=postgres
export DB_PASSWORD=password

# Run with staging profile
mvn spring-boot:run -Dspring-boot.run.profiles=staging

# Or run JAR
java -jar -Dspring.profiles.active=staging target/order-service-*.jar
```

## Configuration Profiles

| Profile | Database | Connection Pool | Logging Level |
|---------|----------|-----------------|---------------|
| `dev` / `default` | H2 in-memory | N/A | DEBUG |
| `staging` | PostgreSQL | 10 | INFO |
| `prod` | PostgreSQL | 30 | WARN |

### Profile-Specific Settings

#### Development (dev/default)
- H2 in-memory database
- H2 console enabled at `/h2-console`
- SQL logging enabled
- Debug logging

#### Staging
- PostgreSQL via environment variables
- Connection pool: 10 connections
- JSON structured logging (Logstash)
- Liquibase migrations enabled

#### Production
- PostgreSQL via environment variables
- Connection pool: 30 connections
- Minimal logging (WARN level)
- JSON structured logging (Logstash)
- Liquibase migrations enabled

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `orderdb` |
| `DB_USER` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `password` |
| `JAVA_OPTS` | JVM options | `-XX:+UseContainerSupport` |

## API Documentation

Once running, access the API documentation at:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

## Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Health check |
| `/actuator/info` | Application info |
| `/actuator/metrics` | Application metrics |
| `/actuator/prometheus` | Prometheus metrics |
| `/actuator/loggers` | Logger configuration |

## Docker

### Build Image

```bash
# Build Docker image
docker build -t order-service:latest .

# Or use BuildKit for faster builds
DOCKER_BUILDKIT=1 docker build -t order-service:latest .
```

### Run Container

```bash
# Run with default profile (H2)
docker run -p 8080:8080 order-service:latest

# Run with staging profile and PostgreSQL
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=staging \
  -e DB_HOST=postgres \
  -e DB_PORT=5432 \
  -e DB_NAME=orderdb \
  -e DB_USER=postgres \
  -e DB_PASSWORD=password \
  order-service:latest
```

### Run with Docker Compose

The easiest way to run the full stack (application + PostgreSQL) is using Docker Compose:

```bash
# Build and start services
docker-compose up --build -d

# Check logs
docker-compose logs -f order-service

# Stop services
docker-compose down
```

After starting with Docker Compose, the application will be available at [http://localhost:8080/](http://localhost:8080/).
---
The `prod` profile is used by default in the `docker-compose.yml`.

## Local LLM Integration (Ollama)

This project includes custom skills to bridge local models (like **Llama 3.1**) with external tools.

### Prerequisites

1. Install [Ollama](https://ollama.com/)
2. Pull the Llama 3.1 model:
   ```bash
   ollama pull llama3.1
   ```

### Usage with Claude Code

Launch Claude Code with the Llama 3.1 model:

```bash
claude --model ollama/llama3.1
```

### Custom Skills

- **Search**: `python3 local-skills.py search "query"` (uses DuckDuckGo)
- **Summarize**: `python3 local-skills.py summarize "text"` (uses local Llama 3.1)

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests with Testcontainers
mvn verify

# Run with specific test profile
mvn test -Dspring.profiles.active=test
```

## Architecture

### Layered Architecture

```
Controller Layer
      ↓
  Service Layer (Business Logic)
      ↓
 Repository Layer (Data Access)
      ↓
  Database (PostgreSQL/H2)
```

### Coding Conventions

- **UUID**: Used for all entity primary keys
- **BigDecimal**: Used for monetary values (never double/float)
- **Constructor Injection**: Used everywhere (never field injection)
- **Records**: Used for DTOs where possible
- **ResponseEntity**: Returned from controllers (not raw objects)
- **AssertJ**: Used for assertions in tests

## Contributing

1. Create a feature branch
2. Make changes following the coding conventions
3. Add unit tests for all public service methods
4. Ensure all tests pass: `mvn test`
5. Submit a pull request

## License

Copyright (c) 2025 SKMCore. All rights reserved.
