<<<<<<< HEAD
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

The gateway is available at `http://localhost:8080`. Check `http://localhost:8080/actuator/health` after startup. The PostgreSQL initialization script creates six isolated databases and a local development account (`medical` / `medical`). The PostgreSQL superuser password in Compose is also for local development. Change these credentials before any shared deployment.

Only the gateway port is published by Compose. Eureka and the domain services communicate on the internal Docker network. For local runs outside Docker, set `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`, and `EUREKA_URL` as needed, then start Eureka first, followed by the services and gateway.

## Current scope

This initializes the applications, dependencies, discovery, gateway route definitions, database configuration, health endpoints, and container builds. The REST controllers, entities, business logic, JWT validation, authorization, and Feign clients described in the project documentation are the next implementation steps. The gateway currently routes requests but does not secure them, so keep this scaffold in a development environment.
=======
<div align="center">

# 🏥 MediCare Connect

### A secure, cloud-ready medical management platform built with Spring Boot microservices

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2023.0-6DB33F?logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT-000000?logo=jsonwebtokens&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue)

*Book appointments • Manage doctors and patients • Keep medical records • Generate invoices*

</div>

---

## 📖 Table of Contents

- [About the Project](#-about-the-project)
- [Key Features](#-key-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Microservices Overview](#-microservices-overview)
- [Security Model](#-security-model)
- [Getting Started](#-getting-started)
- [Running with Docker](#-running-with-docker)
- [API Reference](#-api-reference)
- [Try It Out](#-try-it-out)
- [Project Structure](#-project-structure)
- [Testing](#-testing)
- [Business Scenarios](#-business-scenarios)
- [Roadmap](#-roadmap)
- [Contributing](#-contributing)
- [Author](#-author)

---

## 🎯 About the Project

**MediCare Connect** is a microservices-based platform that digitizes the daily work of a clinic. Patients book appointments online, doctors record diagnoses and prescriptions, administrators manage the doctor directory, and staff generate invoices and track payments, all through one secure entry point.

Many clinics still depend on paper files, phone bookings and spreadsheets. That leads to double bookings, lost records, weak data protection and billing mistakes. MediCare Connect solves these problems by splitting the system into small, independent services that can be built, deployed and scaled separately.

> Built as part of the **ITS4243: Microservices and Cloud Computing** module.

---

## ✨ Key Features

| | Feature | Description |
|---|---|---|
| 🔐 | **JWT authentication** | Register, log in and refresh tokens; passwords hashed with BCrypt |
| 🛡️ | **Role-based access** | Four roles: `ADMIN`, `DOCTOR`, `PATIENT`, `STAFF` |
| 🚪 | **API Gateway** | One public entry point that validates tokens and routes requests |
| 🔎 | **Service discovery** | Services find each other by name through Eureka |
| 📅 | **Smart booking** | Prevents double booking of the same doctor and time slot |
| 🩺 | **Medical records** | Diagnoses and prescriptions linked to patient, doctor and appointment |
| 💳 | **Automatic billing** | Invoices use the doctor's consultation fee; payments are tracked |
| 🗄️ | **Database per service** | Each service owns its data, with no shared tables |
| 📚 | **Swagger docs** | Interactive API documentation for every service |
| 🐳 | **Docker ready** | One command starts the whole system |

---

## 🏗️ Architecture

```
                      ┌─────────────────────────┐
                      │ Client (Web/Postman/App)│
                      └────────────┬────────────┘
                                   │  HTTP + JWT
                      ┌────────────▼────────────┐
                      │       API GATEWAY       │  :8080
                      │ JWT filter • Roles • CORS│
                      └────────────┬────────────┘
     ┌───────────┬─────────────┬───┴────────┬──────────────┬─────────────┐
     │           │             │            │              │             │
┌────▼─────┐┌────▼─────┐┌──────▼───┐┌───────▼────┐┌────────▼───┐┌────────▼──┐
│   Auth   ││ Patient  ││  Doctor  ││Appointment ││  Medical   ││  Billing  │
│  :8081   ││  :8082   ││  :8083   ││   :8084    ││  Record    ││   :8086   │
│          ││          ││          ││            ││   :8085    ││           │
└────┬─────┘└────┬─────┘└────┬─────┘└─────┬──────┘└─────┬──────┘└─────┬─────┘
     │           │           │            │             │             │
  auth_db    patient_db  doctor_db  appointment_db  record_db    billing_db

   Appointment ──Feign──► Patient, Doctor
   Medical Record ──Feign──► Patient, Appointment
   Billing ──Feign──► Appointment, Doctor

        All services register with ──► EUREKA SERVER :8761
```

### How a request flows

1. The client logs in through `POST /auth/login` and receives a **JWT**.
2. Each later request carries `Authorization: Bearer <token>`.
3. The **API Gateway** validates the token, rejects invalid ones with `401`, and checks the role for the route.
4. The gateway forwards the request with `X-User-Id` and `X-User-Role` headers.
5. The target service reads those headers, runs the business logic and replies.

### Design principles

- **Single entry point:** only port `8080` is public.
- **Database per service:** loose coupling and independent scaling.
- **Stateless security:** no server-side sessions.
- **DTO-only APIs:** entities are never exposed.
- **Uniform errors:** every service returns the same error format.

---

## 🧰 Tech Stack

| Area | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x, Spring Web, Spring Data JPA, Bean Validation |
| Cloud | Spring Cloud Gateway, Netflix Eureka, OpenFeign, LoadBalancer |
| Security | Spring Security, JJWT, BCrypt |
| Database | PostgreSQL 16 |
| Docs | springdoc-openapi (Swagger UI) |
| Testing | JUnit 5, Mockito |
| DevOps | Docker, Docker Compose, Maven multi-module build |

---

## 🧩 Microservices Overview

| Service | Port | Database | Responsibility |
|---|---|---|---|
| 🧭 **Eureka Server** | 8761 | none | Service registry and dashboard |
| 🚪 **API Gateway** | 8080 | none | Routing, JWT validation, role checks, CORS |
| 🔐 **Auth Service** | 8081 | `auth_db` | Registration, login, token issue and refresh |
| 🧑‍🤝‍🧑 **Patient Service** | 8082 | `patient_db` | Patient profiles |
| 👨‍⚕️ **Doctor Service** | 8083 | `doctor_db` | Doctor profiles, specialization, fees |
| 📅 **Appointment Service** | 8084 | `appointment_db` | Book, cancel and complete appointments |
| 📋 **Medical Record Service** | 8085 | `record_db` | Diagnoses, prescriptions, history |
| 💳 **Billing Service** | 8086 | `billing_db` | Invoices and payments |

---

## 🔒 Security Model

| Route | Allowed roles |
|---|---|
| `/auth/register`, `/auth/login`, `/auth/refresh` | Public |
| `POST / PUT / DELETE /doctors` | `ADMIN` |
| `GET /doctors`, `/doctors/search` | Any logged-in user |
| `/patients` | `ADMIN`, `STAFF`, `PATIENT` (own data) |
| `POST /appointments` | `PATIENT`, `STAFF` |
| `POST /records` | `DOCTOR` |
| `GET /records/patient/{id}` | `DOCTOR`, `ADMIN`, `PATIENT` (own) |
| `/invoices` | `ADMIN`, `STAFF`, `PATIENT` (own, read only) |

**Security notes**

- Passwords are stored with BCrypt.
- `JWT_SECRET` comes from an environment variable and is never committed.
- Downstream services are not reachable from outside the Docker network.
- Change the default admin account before any real deployment.

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 16 (or Docker)
- Git

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/medicare-connect.git
cd medicare-connect
```

### 2. Set environment variables

```bash
export JWT_SECRET=change-this-to-a-long-random-secret-of-at-least-32-chars
export DB_USERNAME=medical
export DB_PASSWORD=medical
```

### 3. Build all modules

```bash
mvn clean install -DskipTests
```

### 4. Start the services in this order

```bash
mvn -pl eureka-server spring-boot:run          # 1. registry
mvn -pl auth-service spring-boot:run           # 2. business services
mvn -pl patient-service spring-boot:run
mvn -pl doctor-service spring-boot:run
mvn -pl appointment-service spring-boot:run
mvn -pl medical-record-service spring-boot:run
mvn -pl billing-service spring-boot:run
mvn -pl api-gateway spring-boot:run            # 3. gateway last
```

Open the Eureka dashboard at **http://localhost:8761** and confirm every service shows as `UP`.

---

## 🐳 Running with Docker

```bash
docker compose up --build
```

This starts PostgreSQL (with all six databases), Eureka, every service and the gateway. Only port `8080` is published.

```bash
docker compose down        # stop everything
docker compose down -v     # stop and remove the database volume
```

---

## 📡 API Reference

All requests go through the gateway at `http://localhost:8080`.

### Auth

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Create a user |
| POST | `/auth/login` | Get access and refresh tokens |
| POST | `/auth/refresh` | Renew the access token |
| GET | `/auth/me` | Current user details |

### Patients

| Method | Endpoint | Description |
|---|---|---|
| POST | `/patients` | Create a patient |
| GET | `/patients` | List patients |
| GET | `/patients/{id}` | Get one patient |
| PUT | `/patients/{id}` | Update a patient |
| DELETE | `/patients/{id}` | Delete a patient |

### Doctors

| Method | Endpoint | Description |
|---|---|---|
| POST | `/doctors` | Add a doctor (ADMIN) |
| GET | `/doctors` | List doctors |
| GET | `/doctors/search?specialization=` | Search by specialization |
| PUT / DELETE | `/doctors/{id}` | Update or remove (ADMIN) |

### Appointments

| Method | Endpoint | Description |
|---|---|---|
| POST | `/appointments` | Book an appointment |
| GET | `/appointments/{id}` | Get one appointment |
| GET | `/appointments/patient/{id}` | Appointments of a patient |
| PUT | `/appointments/{id}/cancel` | Cancel |
| PUT | `/appointments/{id}/complete` | Mark as completed |

### Medical Records

| Method | Endpoint | Description |
|---|---|---|
| POST | `/records` | Add diagnosis and prescription (DOCTOR) |
| GET | `/records/{id}` | Get one record |
| GET | `/records/patient/{id}` | Records of a patient |

### Billing

| Method | Endpoint | Description |
|---|---|---|
| POST | `/invoices` | Generate an invoice |
| GET | `/invoices/{id}` | Get one invoice |
| POST | `/invoices/{id}/pay` | Record a payment |

### Standard error format

```json
{
  "timestamp": "2026-11-15T10:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "Patient not found with id 5",
  "path": "/patients/5"
}
```

📚 **Swagger UI:** `http://localhost:<service-port>/swagger-ui.html`

---

## 🧪 Try It Out

**Register a patient**

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"Pass@1234","role":"PATIENT"}'
```

**Log in**

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"Pass@1234"}'
```

**Search doctors (use the token you received)**

```bash
curl "http://localhost:8080/doctors/search?specialization=Cardiology" \
  -H "Authorization: Bearer <your-token>"
```

**Book an appointment**

```bash
curl -X POST http://localhost:8080/appointments \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{"patientId":1,"doctorId":1,"date":"2026-12-01","startTime":"10:00","endTime":"10:30","reason":"Chest pain"}'
```

---

## 📂 Project Structure

```
medicare-connect/
├── pom.xml                    # parent POM (multi-module)
├── docker-compose.yml
├── eureka-server/
├── api-gateway/
├── auth-service/
├── patient-service/
├── doctor-service/
├── appointment-service/
├── medical-record-service/
├── billing-service/
└── README.md

Inside each service:
src/main/java/com/medical/<service>/
├── controller/    # REST endpoints
├── service/       # business logic
├── repository/    # Spring Data JPA
├── entity/        # JPA entities
├── dto/           # request and response objects
├── exception/     # custom exceptions and global handler
├── config/        # configuration
└── client/        # Feign clients (where needed)
```

---

## ✅ Testing

```bash
mvn test                       # all modules
mvn -pl auth-service test      # one module
```

Unit tests use **JUnit 5** and **Mockito** for the service layers of the Auth, Patient and Appointment services.

---

## 💼 Business Scenarios

1. **Registration and secure login:** users get a verified, role-limited session.
2. **Patient books an appointment:** the system checks both parties and blocks double booking.
3. **Doctor records diagnosis and prescription:** a secure, permanent medical history.
4. **Administrator manages doctor profiles:** an accurate directory of doctors and fees.
5. **Invoice generation and payment:** automatic billing with a traceable payment record.

---

## 🗺️ Roadmap

- [x] Service discovery and API Gateway
- [x] JWT authentication with roles
- [x] Core clinical and billing services
- [ ] Resilience4j circuit breakers
- [ ] Centralized configuration with Spring Cloud Config
- [ ] Email and SMS notifications with Kafka or RabbitMQ
- [ ] Distributed tracing with Zipkin
- [ ] React frontend
- [ ] Lab results and pharmacy services

---

## 🤝 Contributing

Contributions are welcome.

1. Fork the repository.
2. Create a branch: `git checkout -b feature/amazing-feature`
3. Commit with a clear message: `git commit -m "feat(scope): add amazing feature"`
4. Push the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request.

**Commit format:** `feat`, `fix`, `docs`, `test`, `chore`.

---

## 👤 Author

**Your Name**
GitHub: [@your-username](https://github.com/your-username)
Module: ITS4243 Microservices and Cloud Computing

---

<div align="center">

⭐ If you find this project useful, please give it a star! ⭐

</div>
>>>>>>> df4f29e94c11d900dfdf161dbabf60520a40ea0b
