# 🛒 E-Commerce Event-Driven Microservices System

A full-stack e-commerce system built with **Spring Boot**, **React**, and an **event-driven architecture powered by Kafka**. The application is split into independent microservices behind an **API Gateway**, uses **Keycloak for authentication and role-based access control (RBAC)**, **Redis** for caching, **MongoDB** for order storage, **PostgreSQL** for inventory management and idempotent event processing, and **Prometheus** for observability. The system supports both **Docker Compose** and **Kubernetes (Minikube)** deployments.

This project demonstrates modern backend engineering concepts, including microservices architecture, event-driven communication, API gateways, authentication and authorization, caching strategies, containerization, Kubernetes orchestration, and full-stack integration.

## 🚀 Tech Stack

### Backend
- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data MongoDB
- Spring Data JPA
- Spring Security
- OAuth2 Resource Server and JWT validation
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
- Keycloak JavaScript adapter

### Security
- Keycloak 26.4
- OpenID Connect (OIDC)
- JWT-based authentication
- Role-Based Access Control (RBAC)
- Spring Security method-level authorization

### Infrastructure
- Docker
- Docker Compose
- Kubernetes
- Minikube
- NGINX Ingress Controller
- Apache Kafka
- ZooKeeper
- MongoDB
- PostgreSQL
- Redis
- Prometheus

## 🧱 Architecture Overview

```text
                         Keycloak
                    (OIDC / JWT / RBAC)
                           │
                           ▼
                    React Frontend
                           │
                     Login / JWT
                           │
                           ▼
                 ecommerce.local (Ingress)
                           │
                           ▼
                      API Gateway
                           │
                 Authenticated Requests
                           │
                 ┌─────────┴──────────┐
                 ▼                    ▼
            Order Service       Inventory Service
              (MongoDB)           (PostgreSQL)
                 │                    │
                 └─────── Kafka ──────┘
                            │
                            ▼
                Idempotency Store
                   (PostgreSQL)

              Redis Cache
              Prometheus Metrics
```

Keycloak handles user authentication and issues JWT access tokens. The frontend includes the access token in API requests, and Spring Security validates the token and extracts user roles to enforce authorization rules.

## 🧩 Services

### Order Service

Responsible for order creation, updates, cancellations, persistence in MongoDB, publishing order events via Kafka, and validating inventory availability through the Inventory Service.

Spring Security protects the service's APIs, allowing authenticated requests and applying authorization rules based on the user's roles.

### Inventory Service

Responsible for stock management, reservation and restoration of inventory, persistence in PostgreSQL, and publishing inventory events. It also maintains idempotency records for Kafka event processing.

Role-based authorization restricts inventory management operations to users with the `ADMIN` role.

### API Gateway

Acts as the single entry point for client requests and routes traffic to internal microservices. It provides a centralized routing layer for the frontend's API requests.

### Keycloak

Manages user identities, login, and role assignments. It provides OpenID Connect (OIDC) authentication and issues JWT access tokens containing user claims and realm roles.

The configured realm is `ecommerce`, with the frontend client `ecommerce-frontend` and the roles `ADMIN` and `CUSTOMER`.

### Shared Library

Contains shared event models, DTOs, and common utilities used across services.

## 🔐 Authentication and Authorization

The application uses **Keycloak, OpenID Connect (OIDC), JWT, and Spring Security** to authenticate users and enforce role-based access control.

### Authentication Flow

1. The user logs in through the React frontend using the Keycloak JavaScript adapter.
2. Keycloak authenticates the user and issues a JWT access token.
3. The frontend includes the access token in API requests using the `Authorization: Bearer <token>` header.
4. Spring Security validates the JWT and extracts the user's roles from the token.
5. Protected endpoints allow or deny requests based on authentication and authorization rules.

### Role-Based Access Control (RBAC)

The application uses two realm roles:

| Role | Permissions |
|---|---|
| `CUSTOMER` | Access customer-facing functionality, including permitted order operations |
| `ADMIN` | Perform administrative operations, including inventory management |

Spring Security maps Keycloak realm roles to Spring Security authorities. Endpoint-level authorization rules, including `@PreAuthorize`, enforce permissions on protected operations.

For example, inventory stock updates require the `ADMIN` role:

```java
@PreAuthorize("hasRole('ADMIN')")
```

A user who is authenticated but lacks the required role receives a `403 Forbidden` response. Unauthenticated requests to protected endpoints are rejected.

This separates **authentication (who the user is)** from **authorization (what the user can do)**.

## 📡 Event-Driven Communication

Services communicate asynchronously through Kafka by publishing and consuming domain events. Consumers persist processed event identifiers in PostgreSQL to provide idempotent processing and prevent duplicate side effects caused by retries or message re-delivery.

## ⚡ Caching Strategy

Redis is used as a distributed cache to improve performance and reduce database load. Frequently accessed data, such as orders and inventory items, is cached, and cache entries are invalidated or refreshed on write operations.

## 💾 Data Storage

### MongoDB

Stores order data and order lifecycle state.

### PostgreSQL

Stores inventory data and tracks processed Kafka events for idempotency guarantees.

This demonstrates a polyglot persistence approach in which each service owns its data store.

## 🧪 Testing

The backend uses JUnit 5 and Mockito for unit and integration testing across controllers, services, Kafka producers and consumers, and caching behavior.

```bash
./mvnw clean test
```

The frontend uses Vitest and React Testing Library.

```bash
npm run test
```

## 🐳 Running Locally (Docker Compose)

### Requirements

- Docker
- Docker Compose

### Start

Build and start all services:

```bash
./start.sh
```

Skip image rebuilding:

```bash
./start.sh --no-build
```

### Available URLs

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| API Gateway | http://localhost:8080 |
| Prometheus | http://localhost:9090 |

The Keycloak URL depends on the configured Docker Compose port mapping.

### Stop

```bash
./stop.sh
```

This stops and removes all Docker Compose containers and networks.

## ☸️ Running on Kubernetes (Minikube)

The project includes a Kubernetes deployment using:

- Minikube
- NGINX Ingress Controller
- Kubernetes manifests
- Local DNS via `/etc/hosts`
- Keycloak for authentication and RBAC

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

Build images, start Minikube, enable Ingress, deploy the manifests, configure host entries, and start the Minikube tunnel:

```bash
./start.sh k8s
```

### Skip Docker Image Build

Useful when images are already built:

```bash
./start.sh k8s --no-build
```

### Skip `/etc/hosts` Modification

Use this if the required host entries are already configured:

```bash
./start.sh k8s --no-hosts
```

### Access the Application

Once deployment completes:

```text
http://ecommerce.local
```

Keycloak is configured with the `ecommerce` realm and the `ecommerce-frontend` client. Its ingress hostname is:

```text
http://keycloak.ecommerce.local
```

Ensure both hostnames resolve to the Minikube ingress endpoint.

### Application Traffic Flow

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
          │              │
          └────── Kafka ─┘
```

### Stop Kubernetes Environment

Delete application resources and stop Minikube:

```bash
./stop.sh k8s
```

Preserve `/etc/hosts` entries:

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

View application logs:

```bash
kubectl logs -f deployment/order-service -n ecommerce
kubectl logs -f deployment/inventory-service -n ecommerce
kubectl logs -f deployment/gateway-service -n ecommerce
```

### 🔐 First-Time Keycloak Setup

Before running the application for the first time, configure Keycloak, generate the required certificates, and set up the realm and client.

#### 1. Generate the Required Certificate

Generate the certificate and private key required by your Keycloak configuration. Store them in the location expected by your Docker Compose or Kubernetes configuration.

If using a self-signed certificate for local development, ensure the certificate includes the appropriate hostname (for example, `keycloak.ecommerce.local`) and that the application environment trusts it when HTTPS is used.

**Note:** Keep private keys and other sensitive credentials out of version control.

#### 2. Start Keycloak

Start the infrastructure and Keycloak services using the project's Docker Compose configuration or Kubernetes manifests.

For Kubernetes, start the environment using:

```bash
./start.sh k8s
```

Wait until the Keycloak container or pod is ready before proceeding.

#### 3. Configure the Realm and Client

Open the Keycloak administration console using the configured Keycloak URL and log in with the administrator credentials.

Create or import the following configuration:

- **Realm:** `ecommerce`
- **Client:** `ecommerce-frontend`
- **Authentication protocol:** OpenID Connect (OIDC)
- **Roles:** `ADMIN`, `CUSTOMER`
- **Users:** Create the required users and assign the appropriate realm roles.

Configure the client settings to match the React frontend's Keycloak configuration, including the client type, redirect URIs, and web origins.

#### 4. Configure Application Authentication

Verify that the frontend and backend use the same Keycloak realm and the correct OIDC issuer URL.

Ensure that:

- The React frontend uses the Keycloak JavaScript adapter.
- The backend validates JWT access tokens issued by Keycloak.
- Spring Security maps Keycloak realm roles to Spring Security authorities.
- Protected endpoints enforce the required roles.

#### 5. Verify the Setup

1. Open the frontend and log in with a configured user.
2. Confirm that the user receives an access token.
3. Verify that a `CUSTOMER` can access permitted customer operations.
4. Verify that an `ADMIN` can perform inventory management operations.
5. Confirm that unauthorized requests receive `401 Unauthorized` or `403 Forbidden`, as appropriate.

Once setup is complete, subsequent application starts should reuse the existing Keycloak configuration and certificates unless they have been removed or changed.

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

Provides a unified entry point for client requests while hiding internal service topology.

### Keycloak Authentication and RBAC

Centralizes user authentication and role management with Keycloak. OIDC and JWT enable secure authentication, while Spring Security enforces role-based access rules on protected endpoints.

### Polyglot Persistence

MongoDB and PostgreSQL are used according to each domain's requirements.

### Redis Caching

Improves performance and reduces database load.

### Kubernetes Deployment

Supports local, production-like orchestration through Minikube and Ingress.

### Service Ownership

Each microservice owns its:

- Domain logic
- Persistence
- APIs
- Event publishing

### Containerized Infrastructure

The platform can run through:

- Docker Compose for local development
- Kubernetes and Minikube for orchestration

## 🔥 Future Improvements

- Payment Service
- User Profile Service
- Notification Service
- Transactional Outbox Pattern
- Distributed Tracing (OpenTelemetry)
- Grafana Dashboards
- CI/CD Pipeline (GitHub Actions → Kubernetes)
- Service Discovery
- Circuit Breakers and Resilience Patterns

## Acknowledgements

Some development tasks were assisted by AI coding tools, including GitHub Copilot and OpenAI models.
