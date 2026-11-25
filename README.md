# Medical Management System

Spring Boot microservice project based on the Medical Management System project documentation. This repository uses **Java 21 and PostgreSQL 16**; the supplied PDF describes Java 17 and MySQL, so the repository configuration is the source of truth for running this code.

| Module | Port | Database | Current state |
| --- | ---: | --- | --- |
| `config-server` | 8888 | — | Centralized service configuration |
| `eureka-server` | 8761 | — | Service discovery |
| `api-gateway` | 8080 | — | Routes requests |
| `auth-service` | 8081 | `medical_db.auth` | Startup scaffold |
| `patient-service` | 8082 | `medical_db.patient` | Patient CRUD |
| `doctor-service` | 8083 | `medical_db.doctor` | Doctor CRUD, specialization search, availability and fees |
| `appointment-service` | 8084 | `medical_db.appointment` | Booking, lookup, cancel, complete |
| `medical-record-service` | 8085 | `medical_db.record` | Create and view records |
| `billing-service` | 8086 | `medical_db.billing` | Invoice generation, ownership checks and simulated payments |

## Build and run

The React frontend lives in the sibling `MediX-frontend` project. Start this backend with `docker compose up -d --build`, then start the frontend from `../MediX-frontend` with its own `docker compose up -d --build`. The frontend is available at `http://localhost:3000` and connects to this backend gateway on port 8080.

Requirements: Java 21, Maven 3.9, PostgreSQL 16, and Kafka; or Docker and Docker Compose for the full stack.

```sh
mvn test
docker compose up --build
```

The gateway is available at `http://localhost:8080`. PostgreSQL creates one `medical_db` database; its initialization script creates six service schemas and the local `medical` account. Each database service creates its tables from `schema.sql` in its own schema on startup and then validates the JPA mappings. For local runs outside Docker, set `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`, and `EUREKA_URL` as needed. Start PostgreSQL, the Config Server, and Eureka before the domain services, then start the gateway.

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
| Patient | `POST /patients` | Create profile |
| Patient | `GET /patients`, `GET /patients/{id}` | List or fetch profiles |
| Patient | `PUT /patients/{id}`, `DELETE /patients/{id}` | Update or delete profile |
| Doctor | `POST /doctors`, `GET /doctors`, `GET /doctors/{id}` | Create, list or fetch doctors |
| Doctor | `PUT /doctors/{id}`, `DELETE /doctors/{id}` | ADMIN updates or deletes a doctor |
| Doctor | `GET /doctors/search?specialization=cardio` | Case-insensitive partial specialization search |
| Billing | `POST /invoices` | ADMIN or STAFF creates invoice for a completed appointment |
| Billing | `GET /invoices/{id}` | ADMIN, STAFF or owning PATIENT reads an invoice |
| Billing | `POST /invoices/{id}/pay` | ADMIN or STAFF records simulated payment |
| Appointment | `POST /appointments` | Book a doctor and time slot |
| Appointment | `GET /appointments/{id}`, `GET /appointments/patient/{id}` | Fetch appointment or patient history |
| Appointment | `PUT /appointments/{id}/cancel`, `PUT /appointments/{id}/complete` | Change a booked appointment's status |
| Medical record | `POST /records` | Create diagnosis and prescription |
| Medical record | `GET /records/{id}`, `GET /records/patient/{id}` | Fetch record or patient history |

Patient requests contain `userId`, `name`, `dob`, `gender`, `phone`, `address`, and `bloodGroup`. Booking requests contain `patientId`, `doctorId`, `date`, `startTime`, `endTime`, and optional `reason`. Record requests contain `patientId`, `doctorId`, `appointmentId`, required `diagnosis`, and optional `prescription` and `notes`. Dates and times use ISO formats such as `2026-12-01` and `10:00`.

Booking checks the patient and doctor through Kafka request and reply messages, checks the doctor's `availableDays`, and rejects overlapping `BOOKED` appointments with HTTP 409. A PostgreSQL advisory transaction lock serializes bookings for the same doctor and date. Cancel and complete require a `BOOKED` appointment. A record uses Kafka to check the patient and completed appointment, and only one record is allowed per appointment. Kafka lookups time out after five seconds and return HTTP 503 when the broker or responding service is unavailable.

Kafka topics are `patient.lookup.requests`, `doctor.lookup.requests`, `appointment.lookup.requests`, `appointment.lookup.replies`, and `record.lookup.replies`. Request messages contain `correlationId`, `replyTopic`, and `id`; replies echo `correlationId` and include `found` plus the fields needed by the caller. The request and reply topics are created by the services on startup.

The record service expects gateway supplied `X-User-Role` and `X-User-Id` headers. Only a `DOCTOR` role can create records; a `PATIENT` role can read records linked to its patient profile. The gateway and auth service do **not yet** validate JWTs or establish trusted user headers, so these checks are not a complete security boundary. Keep this stack in a development environment until those dependencies are finished.

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

The tests cover patient profile operations, booking conflicts and status rules, and medical record reference and access checks. A full Kafka, database, and gateway integration run is still needed.

## Doctor and billing implementation

The supplied `Medical_Management_System_Project_Plan (1).pdf` defines the endpoint and role requirements. This implementation retains the repository's Java 21, PostgreSQL 16 and Kafka request/reply conventions in place of the PDF's Java 17, MySQL and Feign examples. The referenced original project brief was not supplied separately.

Doctor writes require `X-User-Role: ADMIN`. A doctor request includes `userId`, `name`, `specialization`, optional `phone`, `consultationFee` and `availableDays`. Fees are nonnegative with up to ten integer digits and two decimal places. Days are comma-separated full or abbreviated weekday names, for example `MONDAY,WED,FRIDAY`; the service normalizes these to `MON,WED,FRI` for booking compatibility and removes duplicates. Zero permits free consultations. Doctor profile deletion does not cascade into other service databases; existing invoices retain their fee and IDs.

Invoice creation accepts only `{"appointmentId":1}`. Billing looks up the completed appointment, then obtains its doctor's consultation fee through Kafka. It stores the fee as an immutable invoice amount with status `UNPAID`. One invoice is allowed per appointment, enforced by a database unique constraint even for concurrent requests. `POST /invoices/{id}/pay` needs no body and changes the status to `PAID`, recording `paidAt`; repeated payment returns 409. This records a simulated payment and does not charge a payment provider.

Ownership uses Billing-to-Patient Kafka lookup: the invoice stores the appointment's patient profile ID; Billing resolves that profile's `userId` and compares it with `X-User-Id`. Patient profile IDs and authenticated user IDs are not interchangeable. PATIENT cannot create invoices or record payments. ADMIN and STAFF can read all invoices. Missing or unrecognized roles are denied. Missing references return 400, missing resources 404, duplicate invoices or invalid state transitions 409, and lookup outages 503. Errors contain `timestamp`, `status`, `error`, `message`, and `path`.

Billing uses `billing.lookup.replies` plus the existing patient, doctor and appointment request topics. Doctor replies now include `id`, `userId`, `availableDays` and numeric `consultationFee`. Existing appointment lookup consumers remain compatible. Billing initializes `invoices` via `schema.sql` and validates JPA mappings. Compose supplies the Kafka bootstrap address and waits for broker health.

These services follow the existing header-based access convention. Auth and the gateway still need JWT validation and trusted header handling before these checks provide a security boundary. Use synthetic data in this development stack. Existing Dockerfiles, `/actuator/health`, `/v3/api-docs` and `/swagger-ui/index.html` remain available on each service's internal port; only gateway business routes are currently aggregated.

Run `mvn test` at the repository root. New service tests cover doctor normalization, invalid days, ADMIN write restrictions, search, missing doctors, invoice fee snapshots, incomplete appointments, duplicate invoices, patient ownership, write restrictions, payment and missing invoices. Full PostgreSQL/Kafka/Compose validation remains a separate integration check.

For a manual gateway smoke test after creating a patient and doctor, booking and completing the appointment:

```sh
curl -X POST http://localhost:8080/invoices -H 'Content-Type: application/json' -H 'X-User-Role: STAFF' -d '{"appointmentId":1}'
curl http://localhost:8080/invoices/1 -H 'X-User-Role: PATIENT' -H 'X-User-Id: 10'
curl -X POST http://localhost:8080/invoices/1/pay -H 'X-User-Role: STAFF'
```

Replace IDs with returned IDs and the linked patient's actual `userId`. Confirm an incomplete appointment and repeat payment return 409, another patient receives 403, and a PATIENT doctor write receives 403. The header examples exercise development checks and do not demonstrate JWT authentication.

See [COMMIT_MESSAGES.md](COMMIT_MESSAGES.md) for 15 proposed conventional commit messages. No commits or authorship dates are created automatically.
