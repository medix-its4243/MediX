# Medical Management System

Spring Boot microservice project based on the Medical Management System project documentation. This repository uses **Java 21 and PostgreSQL 16**; the supplied PDF describes Java 17 and MySQL, so the repository configuration is the source of truth for running this code.

| Module | Port | Database | Current state |
| --- | ---: | --- | --- |
| `eureka-server` | 8761 | — | Service discovery |
| `api-gateway` | 8080 | — | Routes requests |
| `auth-service` | 8081 | `auth_db` | Startup scaffold |
| `patient-service` | 8082 | `patient_db` | Patient CRUD |
| `doctor-service` | 8083 | `doctor_db` | Startup scaffold |
| `appointment-service` | 8084 | `appointment_db` | Booking, lookup, cancel, complete |
| `medical-record-service` | 8085 | `record_db` | Create and view records |
| `billing-service` | 8086 | `billing_db` | Startup scaffold |

## Build and run

Requirements: Java 21, Maven 3.9, and PostgreSQL 16; or Docker and Docker Compose for the full stack.

```sh
mvn test
docker compose up --build
```

Only the gateway is published by Compose, at `http://localhost:8080`. The PostgreSQL initialization script creates the six databases and the local `medical` account. The three implemented services create their own tables from `schema.sql` on startup and then validate the JPA mappings. For local runs outside Docker, set `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`, and `EUREKA_URL` as needed. Start PostgreSQL and Eureka before the domain services, then start the gateway.

## Implemented endpoints

| Service | Method and path | Action |
| --- | --- | --- |
| Patient | `POST /patients` | Create profile |
| Patient | `GET /patients`, `GET /patients/{id}` | List or fetch profiles |
| Patient | `PUT /patients/{id}`, `DELETE /patients/{id}` | Update or delete profile |
| Appointment | `POST /appointments` | Book a doctor and time slot |
| Appointment | `GET /appointments/{id}`, `GET /appointments/patient/{id}` | Fetch appointment or patient history |
| Appointment | `PUT /appointments/{id}/cancel`, `PUT /appointments/{id}/complete` | Change a booked appointment's status |
| Medical record | `POST /records` | Create diagnosis and prescription |
| Medical record | `GET /records/{id}`, `GET /records/patient/{id}` | Fetch record or patient history |

Patient requests contain `userId`, `name`, `dob`, `gender`, `phone`, `address`, and `bloodGroup`. Booking requests contain `patientId`, `doctorId`, `date`, `startTime`, `endTime`, and optional `reason`. Record requests contain `patientId`, `doctorId`, `appointmentId`, required `diagnosis`, and optional `prescription` and `notes`. Dates and times use ISO formats such as `2026-12-01` and `10:00`.

Booking checks the patient and doctor through Eureka and Feign, checks the doctor's `availableDays`, and rejects overlapping `BOOKED` appointments with HTTP 409. A PostgreSQL advisory transaction lock serializes bookings for the same doctor and date. Cancel and complete require a `BOOKED` appointment. A record requires a completed appointment with matching patient and doctor IDs, and only one record is allowed per appointment.

The record service expects gateway supplied `X-User-Role` and `X-User-Id` headers. Only a `DOCTOR` role can create records; a `PATIENT` role can read records linked to its patient profile. The gateway and auth service do **not yet** validate JWTs or establish trusted user headers, so these checks are not a complete security boundary. The doctor service also has no `/doctors/{id}` API yet, so end-to-end booking needs that service to be implemented. Keep this stack in a development environment until those dependencies are finished.

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
└── client/       Feign clients (appointment and medical record services)
```

Configuration classes can be added under `config/` when needed.

## Tests

```sh
mvn -pl patient-service,appointment-service,medical-record-service -am test
```

The tests cover patient profile operations, booking conflicts and status rules, and medical record reference and access checks. A full database and gateway integration run is still needed once the doctor and auth services are implemented.
