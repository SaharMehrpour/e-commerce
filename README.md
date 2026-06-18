# 🛒 E-Commerce Event-Driven Microservices System

A full-stack e-commerce system built with **Spring Boot**, **React**, and an **event-driven architecture powered by Kafka**. The application is split into independent microservices behind an **API Gateway**, uses **Redis** for caching, **MongoDB** for order storage, **PostgreSQL** for inventory management and idempotent event processing, **Prometheus** for observability, and supports both **Docker Compose** and **Kubernetes (Minikube)** deployments.

This project demonstrates modern backend engineering concepts including microservices architecture, event-driven communication, API gateways, caching strategies, containerization, orchestration with Kubernetes, and full-stack integration.


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
- Kubernetes
- Minikube
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
 API Gateway
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

Acts as the single entry point for all client requests and routes traffic to internal microservices.

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

This demonstrates a polyglot persistence approach where each service owns its data store.

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

Build and start all services:

```bash
./start.sh
```

Skip image rebuild:

```bash
./start.sh --no-build
```

#### Available URLs

| Service | URL |
|----------|----------|
| Frontend | http://localhost:5173 |
| API Gateway | http://localhost:8080 |
| Prometheus | http://localhost:9090 |

### Stop

```bash
./stop.sh
```

This stops and removes all Docker Compose containers and networks.


## ☸️ Running on Kubernetes (Minikube)

The project includes a complete Kubernetes deployment using:

- Minikube
- NGINX Ingress Controller
- Kubernetes manifests
- Local DNS via `/etc/hosts`

### Requirements

Install:

- Docker
- kubectl
- Minikube

Verify:

```bash
docker --version
kubectl version --client
minikube version
```

### Start Kubernetes Environment

Build images, start Minikube, enable Ingress, deploy all manifests, configure hosts file, and start the Minikube tunnel:

```bash
./start.sh k8s
```

#### Skip Docker Image Build

Useful when images are already built:

```bash
./start.sh k8s --no-build
```

#### Skip /etc/hosts Modification (Assuming Prior Modification)

```bash
./start.sh k8s --no-hosts
```

### Access the Application

Once deployment completes:

```text
http://ecommerce.local
```

Traffic flow:

```text
Browser
   │
   ▼
Ingress
   │
   ▼
API Gateway
   │
   ├── Order Service
   └── Inventory Service
```

### Stop Kubernetes Environment

Delete application resources and stop Minikube:

```bash
./stop.sh k8s
```

#### Preserve /etc/hosts Entry

```bash
./stop.sh k8s --no-hosts-removed
```

### Useful Kubernetes Commands

View pods:

```bash
kubectl get pods -n ecommerce
```

View services:

```bash
kubectl get svc -n ecommerce
```

View ingress:

```bash
kubectl get ingress -n ecommerce
```

View events:

```bash
kubectl get events -n ecommerce --sort-by=.lastTimestamp
```

View logs:

```bash
kubectl logs -f deployment/order-service -n ecommerce
kubectl logs -f deployment/inventory-service -n ecommerce
kubectl logs -f deployment/gateway-service -n ecommerce
```


## 📊 Metrics & Observability

The project uses Spring Boot Actuator and Micrometer to expose application and business metrics to Prometheus.

Examples include:

- Order creation metrics
- Order cancellation metrics
- Inventory update metrics

### Prometheus Endpoint

```text
http://localhost:8080/actuator/prometheus
```

### Metrics Endpoint

```text
http://localhost:8080/actuator/metrics
```

### Prometheus UI

Docker deployment:

```text
http://localhost:9090
```



## 📁 Project Structure

```text
backend/
├── gateway_service/
├── order_service/
├── inventory_service/
├── shared_lib/
├── deploy/
│   └── prometheus/
├── k8s/
├── docker-compose.yml
└── docker-compose.k8s.yml

frontend/

start.sh
stop.sh
```



## 🧠 Key Design Decisions

### Event-Driven Architecture

Services publish and consume domain events through Kafka to reduce coupling and support asynchronous workflows.

### Idempotent Event Processing

Consumers persist processed event IDs in PostgreSQL to prevent duplicate business operations.

### API Gateway

Provides a unified entry point while hiding internal service topology.

### Polyglot Persistence

MongoDB and PostgreSQL are used based on the requirements of each domain.

### Redis Caching

Improves performance and reduces database load.

### Kubernetes Deployment

Supports local production-like orchestration through Minikube and Ingress.

### Service Ownership

Each microservice owns:

- Domain logic
- Persistence
- APIs
- Event publishing

### Containerized Infrastructure

The entire platform can run via:

- Docker Compose (development)
- Kubernetes / Minikube (orchestration)


## 🔥 Future Improvements

- Payment Service
- User Service
- Notification Service
- Transactional Outbox Pattern
- Distributed Tracing (OpenTelemetry)
- Grafana Dashboards
- CI/CD Pipeline (GitHub Actions → Kubernetes)
- JWT Authentication
- OAuth2 / OpenID Connect
- Service Discovery
- Circuit Breakers & Resilience Patterns


## Acknowledgements

Some development tasks were assisted by AI coding tools, including GitHub Copilot and OpenAI models.
