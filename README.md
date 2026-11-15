# Medical Management System

Initial microservice structure based on the project documentation. This repository contains eight Spring Boot applications in one Maven reactor.

| Module | Port | Database | Purpose |
| --- | ---: | --- | --- |
| `eureka-server` | 8761 | — | Service discovery |
| `api-gateway` | 8080 | — | Routes client requests |
| `auth-service` | 8081 | `auth_db` | Authentication domain |
| `patient-service` | 8082 | `patient_db` | Patient domain |
| `doctor-service` | 8083 | `doctor_db` | Doctor domain |
| `appointment-service` | 8084 | `appointment_db` | Appointment domain |
| `medical-record-service` | 8085 | `record_db` | Medical record domain |
| `billing-service` | 8086 | `billing_db` | Billing domain |

## Requirements

- Java 21 and Maven 3.9 for local builds
- Docker and Docker Compose to run the complete stack

## Build and run

```sh
mvn clean package
docker compose up --build
```

The gateway is available at `http://localhost:8080`. Check `http://localhost:8080/actuator/health` after startup. The MySQL initialization script creates six isolated databases and a local development account (`medical` / `medical`). The MySQL root password in Compose is also for local development. Change these credentials before any shared deployment.

Only the gateway port is published by Compose. Eureka and the domain services communicate on the internal Docker network. For local runs outside Docker, set `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`, and `EUREKA_URL` as needed, then start Eureka first, followed by the services and gateway.

## Current scope

This initializes the applications, dependencies, discovery, gateway route definitions, database configuration, health endpoints, and container builds. The REST controllers, entities, business logic, JWT validation, authorization, and Feign clients described in the project documentation are the next implementation steps. The gateway currently routes requests but does not secure them, so keep this scaffold in a development environment.
