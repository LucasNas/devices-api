# Backend Device API Challenge – Multi-App Implementation

This repository contains **two independent backend services** implementing a Device Management API:

- **Java MVC Application** (`devices-api-java-mvc`) — Spring Boot MVC, JPA, Flyway
- **Kotlin Reactive Application** (`devices-api-kotlin-reactive`) — Spring WebFlux, R2DBC, Liquibase

Both services include:

- CRUD operations
- Business rules & domain constraints
- PostgreSQL persistence
- Kafka event publishing and consuming
- Swagger/OpenAPI documentation
- Extensive unit tests
- Docker + Docker Compose support

---

# 🚀 Recommended Way to Run the Entire System

The **primary method** to run both applications together (including PostgreSQL, Kafka, ZooKeeper) is:

## ✅ 1. Build both services first (required)

### Java MVC app
```
cd devices-api-java-mvc
./mvnw clean package
cd ..
```

### Kotlin Reactive app
```
cd devices-api-kotlin-reactive
./gradlew build
cd ..
```

## ✅ 2. Start the entire stack
From the **root folder**:

```
docker compose up --build
```

This brings up:

- PostgreSQL
- ZooKeeper
- Kafka broker
- Java MVC service (port **8080**)
- Kotlin Reactive service (port **8081**)

### Swagger URLs

| Service | Swagger UI | OpenAPI JSON |
|---------|------------|--------------|
| Java MVC | http://localhost:8080/swagger-ui.html | http://localhost:8080/v3/api-docs |
| Kotlin Reactive | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |

---

# 📘 Project Overview

A device management backend with two independent services (MVC & Reactive).  
Each service supports:

### ✔ Create devices
### ✔ Read devices individually or filtered
### ✔ Full update (with rules)
### ✔ Delete device (with rules)
### ✔ Synchronization over Kafka across both apps

### Key Domain Rules

| Rule | Description |
|------|-------------|
| Immutable fields | `externalId` and `creationTime` never change |
| Forbidden updates | Device in `IN_USE` **cannot change name or brand** |
| Forbidden deletion | Device in `IN_USE` **cannot be deleted** |

---

# 🏗️ Project Structure

```
.
├── devices-api-java-mvc/
│   ├── src/main/java
│   ├── src/test/java
│   ├── Dockerfile
│   └── README.md
│
├── devices-api-kotlin-reactive/
│   ├── src/main/kotlin
│   ├── src/test/kotlin
│   ├── Dockerfile
│   └── README.md
│
├── collections/
│   ├── postman/
│   └── bruno/
│
├── docker-compose.yml
└── README.md
```

---

# 🔧 Technologies Used

| Category | Java MVC App | Kotlin Reactive App |
|----------|--------------|----------------------|
| Language | Java 21 | Kotlin 1.9 |
| Framework | Spring Boot MVC | Spring Boot WebFlux |
| Database | PostgreSQL + JPA | PostgreSQL + R2DBC |
| Migrations | Flyway | Liquibase |
| Messaging | Kafka Producer/Consumer | Kafka Producer/Consumer |
| Testing | JUnit 5, Mockito | JUnit 5, MockK, StepVerifier |
| Docs | Springdoc OpenAPI | Springdoc OpenAPI |

---

# 🗄 Database Migrations

- **Flyway (Java MVC)**  
  `devices-api-java-mvc/src/main/resources/db/migration`

- **Liquibase (Kotlin Reactive)**  
  `devices-api-kotlin-reactive/src/main/resources/db/changelog`

---

# ▶️ Running Each App Individually (Optional)

## Java MVC

### Build
```
cd devices-api-java-mvc
./mvnw clean package
```

### Run
```
./mvnw spring-boot:run
```

Runs on `http://localhost:8080`

---

## Kotlin Reactive

### Build
```
cd devices-api-kotlin-reactive
./gradlew build
```

### Run
```
./gradlew bootRun
```

Runs on `http://localhost:8081`

---

# 📡 Kafka Event Flow

Topic:
```
devices.events
```

Both apps:

- **Publish** on device created
- **Consume** events from the other app
- **Mirror** the device locally (upsert by externalId)

Each app includes rules to avoid infinite loops (origin field).

---

# 🧪 Testing Summary

### Java MVC
- Controller layer tests (MockMvc)
- Service tests with Mockito
- Kafka listener tests
- Kafka publisher tests
- Exception handler tests
- Domain and mapping tests

### Kotlin Reactive
- Controller tests (WebTestClient)
- Service tests (MockK + StepVerifier)
- Repository adapter tests
- Kafka listener tests
- Kafka publisher tests
- Domain + mapper tests

---

# 📂 API Collections (Postman & Bruno)

Inside:

```
/collections
```

These collections include:
- All CRUD endpoints
- Filters

---

