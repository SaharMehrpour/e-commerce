# 🛒 E-Commerce Event-Driven Microservices System

A full-stack e-commerce system built with **Spring Boot**, **React**, and an **event-driven architecture powered by Kafka**. The application is split into independent microservices behind an **API Gateway**, uses **Redis** for caching, **MongoDB** for order storage, **PostgreSQL** for inventory management and idempotent event processing, and **Prometheus** for observability.

This project demonstrates modern backend engineering concepts including microservices architecture, event-driven communication, API gateways, caching strategies, containerization, and full-stack integration.



## 🚀 Tech Stack

### Backend
- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data MongoDB
- Spring Data JPA
- PostgreSQL
- Spring Kafka
- Spring Cache (Redis)
- Spring Cloud Gateway
- Spring Boot Actuator
- Micrometer

### Frontend
- React (Vite)
- JavaScript
- Fetch API

### Infrastructure
- Docker
- Docker Compose
- Apache Kafka
- ZooKeeper
- MongoDB
- PostgreSQL
- Redis
- Prometheus



## 🧱 Architecture Overview

```text
React Frontend
        │
        ▼
 ecommerce.local (Ingress)
        │
        ▼
 API Gateway (:8080)
        │
 ┌──────┴─────────────┐
 ▼                    ▼
Order Service     Inventory Service
(MongoDB)         (PostgreSQL)
        │              │
        └──── Kafka ───┘
               │
               ▼
     Idempotency Store (PostgreSQL)

Redis Cache
Prometheus Metrics
```

## 🧩 Services

### Order Service

Responsible for order creation, updates, cancellations, persistence in MongoDB, publishing order events via Kafka, and validating inventory availability through the Inventory Service.

### Inventory Service

Responsible for stock management, reservation and restoration of inventory, persistence in PostgreSQL, and publishing inventory events. It also maintains idempotency records for Kafka event processing.

### API Gateway

Acts as the single entry point for all client requests and routes traffic to internal microservices based on path rules.

### Shared Library

Contains shared event models, DTOs, and common utilities used across all services.

## 📡 Event-Driven Communication

Services communicate asynchronously through Kafka by publishing and consuming domain events. Consumers persist processed event identifiers in PostgreSQL to guarantee idempotent processing and prevent duplicate side effects caused by retries or re-delivery.

## ⚡ Caching Strategy

Redis is used as a distributed cache to improve performance and reduce database load. Frequently accessed data such as orders and inventory items are cached, and cache entries are invalidated or refreshed on write operations.

## 💾 Data Storage

### MongoDB

Stores order data and order lifecycle state.

### PostgreSQL

Stores inventory data and also tracks processed Kafka events for idempotency guarantees.

This setup demonstrates polyglot persistence with domain-driven data ownership.

## 🧪 Testing

The backend uses JUnit 5 and Mockito for unit and integration testing across controllers, services, Kafka producers/consumers, and caching behavior.

```bash
./mvnw clean test
```

The frontend uses Vitest and React Testing Library.

```bash
npm run test
```


## 🐳 Running Locally (Docker Compose)

Requirements
- Docker
- Docker Compose

### Start
```bash
./start.sh
```

| Service	| URL |
| - | - |
| Frontend	| http://localhost:5173 |
| API Gateway	| http://localhost:8080 |
| Prometheus	| http://localhost:9090 |

### Stop
```bash
./stop.sh
```

This stops and removes all containers and networks created by Docker Compose.

## ☸️ Running on Kubernetes (Minikube)

The project also supports a full Kubernetes deployment locally using Minikube.

Start Minikube
```bash
minikube start --driver=docker
minikube addons enable ingress
```

Access Application

Add to `/etc/hosts`:
```
127.0.0.1 ecommerce.local
```

Then run:
```bash
minikube tunnel
```

Now access:
```
http://ecommerce.local
```

This routes through:
Ingress → API Gateway → Microservices


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
├── k8s/
└── docker-compose.yml

frontend/

start.sh
stop.sh
```



## 🧠 Key Design Decisions

### Event-Driven Architecture

Services publish domain events via Kafka to decouple workflows and enable asynchronous processing.

### Idempotent Event Processing

Kafka consumers store processed event IDs in PostgreSQL to guarantee exactly-once business effects.

### API Gateway

Provides a unified entry point and abstracts internal microservice topology.

### Polyglot Persistence

MongoDB and PostgreSQL are used based on domain needs and ownership boundaries.

### Redis Caching

Reduces database load and improves response times for frequently accessed data.

### Kubernetes Deployment

The system is fully deployable on Minikube using Ingress-based routing to simulate production-like environments.

### Service Separation

Each microservice owns its domain logic, persistence, API, and event publishing responsibilities.

### Containerized Infrastructure

All services run via Docker Compose for local development and Kubernetes manifests for orchestration testing.


## 🔥 Future Improvements

* Payment Service
* User Service
* Notification Service
* Transactional Outbox Pattern
* Distributed Tracing (OpenTelemetry)
* Grafana Dashboards
* CI/CD Pipeline (GitHub Actions → Kubernetes)
* JWT Authentication
* OAuth2 / OpenID Connect
* Service Discovery
* Circuit Breakers & Resilience Patterns



## Acknowledgements

Some development tasks were assisted by AI coding tools, including GitHub Copilot and OpenAI models.
