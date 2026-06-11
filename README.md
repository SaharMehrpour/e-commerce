# 🛒 E-Commerce Event-Driven Microservices System

A full-stack e-commerce system built with **Spring Boot**, **React**, and an **event-driven architecture powered by Kafka**. The application is split into independent microservices behind an **API Gateway**, uses **Redis** for caching, **MongoDB** for order storage, **PostgreSQL** for inventory management and idempotent event processing, and **Prometheus** for observability.

This project is designed to demonstrate modern backend engineering concepts including microservices architecture, event-driven communication, API gateways, caching strategies, containerization, and full-stack integration.



## 🚀 Tech Stack

### Backend

* Java 21
* Spring Boot
* Spring Web MVC
* Spring Data MongoDB
* Spring Data JPA
* PostgreSQL
* Spring Kafka
* Spring Cache (Redis)
* Spring Cloud Gateway
* Spring Boot Actuator
* Micrometer

### Frontend

* React (Vite)
* JavaScript
* Fetch API

### Infrastructure

* Docker
* Docker Compose
* Apache Kafka
* ZooKeeper
* MongoDB
* PostgreSQL
* Redis
* Prometheus



## 🧱 Architecture Overview

```text
React Frontend
       │
       ▼
 API Gateway (:8080)
       │
 ┌─────┴─────────────┐
 ▼                   ▼
Order Service    Inventory Service
(MongoDB)        (PostgreSQL)
      │                │
      └──── Kafka ─────┘
             │
             ▼
      Idempotency Store
        (PostgreSQL)

      Redis Cache
      Prometheus
```

### Services

#### Order Service

Responsible for order creation, updates, cancellation, persistence in MongoDB, publishing order-related Kafka events, and inventory availability checks through the Inventory Service.

#### Inventory Service

Responsible for inventory management, stock reservation and restoration, persistence in PostgreSQL, and publishing inventory-related Kafka events.

#### API Gateway

Provides a single entry point for frontend clients and routes requests to the appropriate backend service.

#### Shared Library

Contains shared event models, DTOs, idempotency utilities, and common components used across services.



## 📡 Event-Driven Communication

Services communicate asynchronously through Kafka by publishing and consuming domain events. Order-related actions generate order events, while inventory operations generate inventory events. Consumers use an idempotency mechanism backed by PostgreSQL to ensure events are processed only once, protecting the system from duplicate deliveries and retries.



## ⚡ Caching Strategy

Redis is used as a distributed cache to reduce database load and improve read performance. Frequently accessed order and inventory data are cached, while cache entries are automatically refreshed or invalidated when underlying data changes.



## 💾 Data Storage

### MongoDB

Stores order data and order lifecycle information.

### PostgreSQL

Stores inventory data and serves as the persistence layer for processed Kafka event tracking used by the idempotency mechanism.

This architecture demonstrates polyglot persistence by allowing each service to use the storage technology best suited to its domain.



## 🧪 Testing

The backend is tested using JUnit 5 and Mockito with a combination of unit and integration tests covering controllers, services, repositories, Kafka interactions, and caching behavior.

```bash
./mvnw clean test
```

The frontend uses Vitest and React Testing Library to verify component behavior and user interactions.

```bash
npm run test
```



## 🐳 Running the Project

This application is fully containerized.

### Requirements

* Docker
* Docker Compose

### 🚀 Start

```bash
./start.sh
```

After startup:

| Service     | URL                   |
| -- | -- |
| Frontend    | http://localhost:5173 |
| API Gateway | http://localhost:8080 |
| Prometheus  | http://localhost:9090 |

### 🛑 Stop

```bash
./stop.sh
```

This stops and removes all containers and networks created by Docker Compose.



## 📊 Metrics & Observability

The project uses Spring Boot Actuator and Micrometer to expose application and business metrics to Prometheus. Custom metrics track domain operations such as order creation, order cancellation, and inventory updates, enabling monitoring and future dashboard integration.

Prometheus endpoint:

```text
http://localhost:8080/actuator/prometheus
```

Metrics endpoint:

```text
http://localhost:8080/actuator/metrics
```

Prometheus UI:

```text
http://localhost:9090
```



## 📁 Project Structure

```text
backend/
├── gateway-service/
├── order_service/
├── inventory_service/
├── shared_lib/
├── deploy/
│   └── prometheus/
└── docker-compose.yml

frontend/

start.sh
stop.sh
```



## 🧠 Key Design Decisions

### Event-Driven Architecture

Services publish domain events through Kafka to decouple business workflows and support asynchronous communication.

### Idempotent Event Processing

Kafka consumers persist processed event identifiers in PostgreSQL to ensure events are handled exactly once from the application's perspective.

### API Gateway

Provides a single entry point for frontend clients and hides internal service topology.

### Polyglot Persistence

MongoDB and PostgreSQL are used to demonstrate service-specific storage choices and domain ownership.

### Redis Caching

Improves read performance and reduces database load by caching frequently requested data.

### Service Separation

Each service owns its domain, persistence layer, API, and event publishing responsibilities.

### Containerized Infrastructure

All services and supporting infrastructure run through Docker Compose for reproducible local development.



## 🔥 Future Improvements

* Payment Service
* User Service
* Notification Service
* Transactional Outbox Pattern
* Distributed Tracing (OpenTelemetry)
* Grafana Dashboards
* Kubernetes Deployment
* CI/CD Deployment Pipeline
* JWT Authentication
* OAuth2 / OpenID Connect
* Service Discovery
* Circuit Breakers and Retries



## Acknowledgements

Some development tasks were assisted by AI coding tools, including GitHub Copilot and OpenAI models.
