# Architecture and Service Responsibilities

[← Back to the main README](../README.md)

## System overview

The application is a full-stack e-commerce system composed of a React frontend, an API Gateway, and independently deployed backend services. Keycloak handles user authentication, Kafka supports asynchronous event-driven communication, and each service uses storage appropriate to its domain.

```text
Browser
   │
   ├── Login ───────────────► Keycloak
   │                            │
   │◄──── JWT Access Token ─────┘
   │
   └── Authenticated API Requests
                │
                ▼
              Ingress
                │
                ▼
            API Gateway
                │
          ┌─────┴────────┐
          ▼              ▼
     Order Service  Inventory Service
       (MongoDB)      (PostgreSQL)
          │              │
          └────── Kafka ─┘

      Redis Cache · Prometheus Metrics
```

## Services

### Order Service

Responsible for order creation, updates, cancellations, persistence in MongoDB, publishing order events through Kafka, and validating inventory availability through the Inventory Service.

Spring Security protects the service APIs and applies authorization rules based on user roles.

### Inventory Service

Responsible for stock management, reservation and restoration of inventory, persistence in PostgreSQL, publishing inventory events, and maintaining idempotency records for Kafka event processing.

Inventory-management operations are restricted to users with the `ADMIN` role.

### API Gateway

Acts as the single entry point for client requests and routes traffic to internal microservices. This provides a centralized routing layer for frontend API requests.

### Keycloak

Manages user identities, login, and role assignments. It provides OIDC authentication and issues JWT access tokens containing user claims and realm roles. See [Authentication and Authorization](SECURITY.md).

### Shared Library

Contains shared event models, DTOs, and common utilities used across services.

## Event-driven communication

Services publish and consume domain events through Kafka. Consumers persist processed event identifiers in PostgreSQL to support idempotent processing and reduce duplicate side effects caused by retries or message redelivery.

## Data storage

- **MongoDB:** Order data and order lifecycle state.
- **PostgreSQL:** Inventory data and records of processed Kafka events.
- **Redis:** Distributed cache for frequently accessed data.

This is a polyglot persistence approach: each service uses storage that fits its domain and maintains ownership of its data.

## Observability

Spring Boot Actuator and Micrometer expose application and business metrics for Prometheus. See [Testing and Development](DEVELOPMENT.md#metrics-and-observability).

## Project structure

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
