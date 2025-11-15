# Backend Device API Challenge – Multi-App Implementation

This repository contains **two independent backend services** implementing a Device Management API:

- **Java MVC Application** (`devices-api-java-mvc`) — Spring Boot MVC, JPA, Flyway
- **Kotlin Reactive Application** (`devices-api-kotlin-reactive`) — Spring WebFlux, R2DBC, Liquibase

Both services include:

- CRUD operations for Device entities
- Domain rule enforcement
- PostgreSQL persistence
- Kafka event publishing/consuming
- Swagger/OpenAPI documentation
- Extensive unit tests
- Docker & Docker Compose support

---

# 🚀 Recommended Way to Run the Entire System

The **primary and recommended method** to run the whole stack (both apps + PostgreSQL + Kafka + ZooKeeper) is via **Docker Compose**.

Because the Dockerfiles expect pre-built JARs (`target/*.jar` and `build/libs/*.jar`), you must first **build both applications locally**, then run Compose.

### 1️⃣ Build the Java MVC app

```bash
cd devices-api-java-mvc
./mvnw clean package
cd ..
```

### 2️⃣ Build the Kotlin reactive app

```bash
cd devices-api-kotlin-reactive
./gradlew build
cd ..
```

### 3️⃣ Start the full stack with Docker Compose (from repo root)

```bash
docker compose up --build
```

This command:

- Uses the already-built JARs
- Builds lightweight runtime images for both apps
- Starts Kafka + ZooKeeper
- Starts PostgreSQL
- Starts both services on different ports

| Service           | Port  | Swagger URL                                |
|-------------------|-------|--------------------------------------------|
| Java MVC App      | 8080  | http://localhost:8080/swagger-ui.html      |
| Kotlin Reactive   | 8081  | http://localhost:8081/swagger-ui.html      |

---

# 📘 Project Overview

A device management system supporting:

- Creation of devices
- Full updates
- Deletion (unless `IN_USE`)
- Filtering by brand/state
- Kafka-based event propagation
- Reactive processing (Kotlin app)

### Domain rules enforced:

- `creationTime` cannot be changed
- Devices in `IN_USE` **cannot**:
    - change name
    - change brand
    - be deleted

---

# 🏗️ Project Structure

```
.
├── devices-api-java-mvc/              
│   ├── src/main/java
│   ├── src/test/java
│   └── README.md
│
├── devices-api-kotlin-reactive/       
│   ├── src/main/kotlin
│   ├── src/test/kotlin
│   └── README.md
│
├── docker-compose.yml
└── README.md
```

---

# 🔧 Technologies Used

| Category        | Java MVC App                   | Kotlin Reactive App             |
|----------------|--------------------------------|---------------------------------|
| Language       | Java 21                        | Kotlin 1.9                      |
| Framework      | Spring Boot MVC                | Spring Boot WebFlux             |
| Database       | PostgreSQL + JPA/Hibernate     | PostgreSQL + R2DBC              |
| Migrations     | Flyway                         | Liquibase                       |
| Messaging      | Kafka Producer + Listener      | Kafka Producer + Listener       |
| Documentation  | Springdoc OpenAPI              | Springdoc OpenAPI               |
| Testing        | JUnit 5 + Mockito              | JUnit 5 + MockK + StepVerifier  |

---

# 🗄️ Database Migrations

- **Java MVC App:** Flyway migrations  
  Location:  
  `devices-api-java-mvc/src/main/resources/db/migration`

- **Kotlin Reactive App:** Liquibase changelogs  
  Location:  
  `devices-api-kotlin-reactive/src/main/resources/db/changelog`

---

# ▶️ Running Services Individually (Optional, Without Docker)

You can also run each service locally without Docker.

## 📦 Java MVC App

### Build

```bash
cd devices-api-java-mvc
./mvnw clean package
```

### Run

```bash
./mvnw spring-boot:run
```

App runs on: `http://localhost:8080`

---

## ⚡ Kotlin Reactive App

### Build

```bash
cd devices-api-kotlin-reactive
./gradlew build
```

### Run

```bash
./gradlew bootRun
```

App runs on: `http://localhost:8081`

---

# 📡 Kafka Event Flow

Topic used:
```text
devices.events
```

- **Java MVC app:** publishes events on device creation
- **Kotlin Reactive app:** publishes events and also consumes them, capable of upserting devices based on incoming events

---

# 🧪 Testing Summary

### Java MVC:

- Service tests
- Exception handler tests
- Kafka publisher tests
- Domain/model tests

### Kotlin Reactive:

- Controller tests with WebTestClient
- Reactive service tests (Mono/Flux)
- Repository adapter tests
- Kafka listener & publisher tests
- Domain + mapper tests
- Event mapping tests

---

# 🔗 Swagger Documentation

| Service             | Swagger UI URL                              | OpenAPI JSON URL                       |
|---------------------|----------------------------------------------|-----------------------------------------|
| Java MVC App        | http://localhost:8080/swagger-ui.html        | http://localhost:8080/v3/api-docs       |
| Kotlin Reactive App | http://localhost:8081/swagger-ui.html        | http://localhost:8081/v3/api-docs       |


---
