# Medical Management System

Spring Boot microservice project based on the Medical Management System project documentation. This repository uses **Java 21 and PostgreSQL 16**; the supplied PDF describes Java 17 and MySQL, so the repository configuration is the source of truth for running this code.

| Module | Port | Database | Current state |
| --- | ---: | --- | --- |
| `config-server` | 8888 | — | Centralized service configuration |
| `eureka-server` | 8761 | — | Service registry and health endpoint |
| `api-gateway` | 8080 | — | JWT verification, role rules, routing, CORS |
| `auth-service` | 8081 | `auth_db` | Registration, login, refresh, and user management |
| `patient-service` | 8082 | `patient_db` | Patient CRUD |
| `doctor-service` | 8083 | `doctor_db` | Create and fetch doctor profiles for booking |
| `appointment-service` | 8084 | `appointment_db` | Booking, lookup, cancel, complete |
| `medical-record-service` | 8085 | `record_db` | Create and view records |
| `billing-service` | 8086 | `billing_db` | Startup scaffold |

## Build and run

Requirements: Java 21, Maven 3.9, PostgreSQL 16, and Kafka; or Docker and Docker Compose for the full stack.

Set a signing secret of at least 32 UTF-8 bytes before starting Auth and the gateway. Both services must use the same `JWT_SECRET`. To create the first admin user, also set `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` (at least eight characters). For PowerShell:

```powershell
$env:JWT_SECRET = "replace-with-a-random-secret-of-at-least-32-bytes"
$env:BOOTSTRAP_ADMIN_EMAIL = "admin@example.com"
$env:BOOTSTRAP_ADMIN_PASSWORD = "replace-with-a-strong-password"
docker compose up --build
```

```sh
mvn test
docker compose up --build
```

The gateway is available at `http://localhost:8080`, and the Eureka dashboard at `http://localhost:8761`. The PostgreSQL initialization script creates the six databases and the local `medical` account. Auth, patient, doctor, appointment, and medical record services create their own tables from `schema.sql` on startup and then validate the JPA mappings. For local runs outside Docker, set `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`, and `EUREKA_URL` as needed. Start PostgreSQL, the Config Server, and Eureka before the domain services, then start the gateway.

Kafka is available at `localhost:29092` from the host and `kafka:9092` inside Compose. Set `KAFKA_BOOTSTRAP_SERVERS` for local service runs (default `localhost:9092`). Start Kafka before the patient, doctor, appointment, and medical record services.

## Centralized configuration

The Config Server uses Spring Cloud Config's native backend. Its configuration files are in `config-server/src/main/resources/config-repo/`: `application.yml` holds shared PostgreSQL credential placeholders and JPA settings, and each database service has its own file with its database URL. The six database services import this configuration at startup and fail to start if the Config Server is unavailable. `CONFIG_SERVER_URL` defaults to `http://localhost:8888`; Compose sets it to `http://config-server:8888` and waits for the health check. Changes to these packaged files require rebuilding and restarting the Config Server and restarting its clients.

For a local run, start the server first:

```sh
mvn -pl config-server spring-boot:run
```

Then inspect a service's configuration at `http://localhost:8888/patient-service/default`. Set `DB_HOST`, `DB_PORT`, `DB_USERNAME`, and `DB_PASSWORD` in each service's environment to override the local development defaults. The Config Server is only reachable inside the Compose network; its HTTP response includes database configuration, so keep it on that trusted network.

## Implemented endpoints

| Service | Method and path | Action |
| --- | --- | --- |
| Auth | `POST /auth/register`, `POST /auth/login` | Register a patient or obtain tokens |
| Auth | `POST /auth/refresh`, `GET /auth/me` | Refresh tokens or read current user |
| Auth | `POST /auth/users` | Admin creates a user with a chosen role |
| Patient | `POST /patients` | Create profile |
| Patient | `GET /patients`, `GET /patients/{id}` | List or fetch profiles |
| Patient | `PUT /patients/{id}`, `DELETE /patients/{id}` | Update or delete profile |
| Doctor | `POST /doctors`, `GET /doctors/{id}` | Create or fetch a doctor for booking |
| Appointment | `POST /appointments` | Book a doctor and time slot |
| Appointment | `GET /appointments/{id}`, `GET /appointments/patient/{id}` | Fetch appointment or patient history |
| Appointment | `PUT /appointments/{id}/cancel`, `PUT /appointments/{id}/complete` | Change a booked appointment's status |
| Medical record | `POST /records` | Create diagnosis and prescription |
| Medical record | `GET /records/{id}`, `GET /records/patient/{id}` | Fetch record or patient history |

Patient requests contain `userId`, `name`, `dob`, `gender`, `phone`, `address`, and `bloodGroup`. Booking requests contain `patientId`, `doctorId`, `date`, `startTime`, `endTime`, and optional `reason`. Record requests contain `patientId`, `doctorId`, `appointmentId`, required `diagnosis`, and optional `prescription` and `notes`. Dates and times use ISO formats such as `2026-12-01` and `10:00`.

Booking checks the patient and doctor through Kafka request and reply messages, checks the doctor's `availableDays`, and rejects overlapping `BOOKED` appointments with HTTP 409. A PostgreSQL advisory transaction lock serializes bookings for the same doctor and date. Cancel and complete require a `BOOKED` appointment. A record uses Kafka to check the patient and completed appointment, and only one record is allowed per appointment. Kafka lookups time out after five seconds and return HTTP 503 when the broker or responding service is unavailable.

Kafka topics are `patient.lookup.requests`, `doctor.lookup.requests`, `appointment.lookup.requests`, `appointment.lookup.replies`, and `record.lookup.replies`. Request messages contain `correlationId`, `replyTopic`, and `id`; replies echo `correlationId` and include `found` plus the fields needed by the caller. The request and reply topics are created by the services on startup.

## Authentication and access

Public registration creates a `PATIENT` user. Admins can create `ADMIN`, `DOCTOR`, `PATIENT`, or `STAFF` users through `POST /auth/users`. Passwords are stored as BCrypt hashes. Login returns a one-hour access token and a seven-day refresh token; a refresh token can request a new pair at `POST /auth/refresh`. Refresh tokens are signed but are not stored or revoked, so issuing a new pair does not invalidate an earlier refresh token.

The gateway verifies JWT signature, issuer, expiry, and token type, then applies role rules to routes. It removes incoming `X-User-Id` and `X-User-Role` headers and replaces them with verified token claims. Patient and appointment services check that patients access their own data; the record service permits creation by doctors and limits patient reads to their own records. Gateway, Eureka, and Kafka ports are published by Compose; downstream services trust gateway headers on the private Docker network. Directly exposing a downstream service would bypass that trust boundary.

## Service package layout

Each implemented service keeps its Spring Boot application class in the root package and groups its other Java classes by responsibility:

```text
src/main/java/com/medical/<service>/
├── controller/   REST endpoints
├── service/      business logic
├── repository/   Spring Data JPA repositories
├── entity/       JPA entities and enums
├── dto/          request and response objects
├── exception/    API exception handler
└── client/       Kafka lookup clients (appointment and medical record services)
```

Kafka listeners and topic configuration are under `config/`.

## Tests

```sh
mvn clean test
```

The tests cover patient profile operations and ownership, appointment booking and access rules, medical record reference and access checks, Auth token rules, gateway authorization and identity headers, and Eureka health. A full Kafka, database, and gateway integration run is still needed.
