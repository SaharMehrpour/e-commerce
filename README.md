# 🛒 E-Commerce Event-Driven Microservices System

A full-stack e-commerce demo built with **Java 21, Spring Boot, React, Kafka, and Docker**. The application is organized into independent microservices behind an API Gateway, uses **Keycloak** for authentication and role-based access control (RBAC), **Redis** for caching, **MongoDB** for orders, **PostgreSQL** for inventory and idempotent event processing, and **Prometheus** for observability.

The project demonstrates modern backend and platform engineering concepts, including microservices, asynchronous messaging, API gateways, JWT-based security, caching, containerization, Kubernetes orchestration, and full-stack integration.

## Architecture at a glance

```text
                         Keycloak
                    (OIDC / JWT / RBAC)
                           │
                           ▼
                    React Frontend
                           │
                 ecommerce.local / Ingress
                           │
                           ▼
                      API Gateway
                           │
                 ┌─────────┴──────────┐
                 ▼                    ▼
            Order Service       Inventory Service
              (MongoDB)           (PostgreSQL)
                 │                    │
                 └─────── Kafka ──────┘
                            │
                    Idempotency Store
                      (PostgreSQL)

              Redis Cache · Prometheus Metrics
```

## Tech stack

- **Backend:** Java 21, Spring Boot, Spring Web MVC, Spring Data MongoDB/JPA, Spring Security, Spring Kafka, Spring Cache, Spring Cloud Gateway, Actuator, Micrometer
- **Frontend:** React, Vite, JavaScript, Fetch API, Keycloak JavaScript adapter
- **Security:** Keycloak 26.4, OpenID Connect (OIDC), JWT, Spring Security method-level authorization
- **Data and messaging:** MongoDB, PostgreSQL, Redis, Apache Kafka, ZooKeeper
- **Infrastructure:** Docker, Docker Compose, Kubernetes, Minikube, NGINX Ingress Controller, Prometheus

## Services

| Component | Responsibility |
|---|---|
| **Order Service** | Creates, updates, cancels, and stores orders; publishes order events; checks inventory availability |
| **Inventory Service** | Manages stock, reserves/restores inventory, publishes inventory events, and tracks processed event IDs |
| **API Gateway** | Provides a single entry point and routes frontend API requests to internal services |
| **Keycloak** | Manages login, identities, OIDC tokens, and realm roles |
| **Shared Library** | Holds shared event models, DTOs, and utilities |

## Run the project

### Docker Compose

Requirements: Docker and Docker Compose.

```bash
./start.sh
```

Skip rebuilding images:

```bash
./start.sh --no-build
```

Frontend: `http://localhost:5173`  
API Gateway: `http://localhost:8080`  
Prometheus: `http://localhost:9090`

The Keycloak URL depends on the configured Compose port mapping.

Stop the environment:

```bash
./stop.sh
```

### Kubernetes with Minikube

Requirements: Docker, `kubectl`, and Minikube.

```bash
./start.sh k8s
```

Useful options:

```bash
./start.sh k8s --no-build
./start.sh k8s --no-hosts
```

The application is configured to use `http://ecommerce.local` and Keycloak at `http://keycloak.ecommerce.local`. Ensure both hostnames resolve to the Minikube Ingress endpoint.

Stop the Kubernetes environment:

```bash
./stop.sh k8s
```

See the deployment guides for prerequisites, setup details, commands, and troubleshooting:

- [Run locally with Docker Compose](docs/DOCKER-COMPOSE.md)
- [Deploy with Kubernetes and Minikube](docs/KUBERNETES.md)

## Documentation

- [Architecture and service responsibilities](docs/ARCHITECTURE.md)
- [Authentication, authorization, and first-time Keycloak setup](docs/SECURITY.md)
- [Local Docker Compose setup](docs/DOCKER-COMPOSE.md)
- [Kubernetes and Minikube deployment](docs/KUBERNETES.md)
- [Testing, metrics, project structure, and design decisions](docs/DEVELOPMENT.md)

## Key design choices

- **Event-driven communication:** Kafka supports asynchronous communication between services.
- **Idempotent event processing:** PostgreSQL records processed event identifiers to help prevent duplicate side effects when messages are redelivered.
- **Polyglot persistence:** MongoDB stores orders; PostgreSQL stores inventory and event-processing records.
- **Caching:** Redis reduces repeated data access and database load.
- **Centralized entry point:** The API Gateway hides internal service topology from the frontend.
- **Containerized deployment:** Docker Compose supports local development, while Kubernetes/Minikube demonstrates orchestration and Ingress.

## Future improvements

- Payment, user-profile, and notification services
- Transactional Outbox Pattern
- Distributed tracing with OpenTelemetry
- Grafana dashboards
- GitHub Actions CI/CD pipeline
- Service discovery
- Circuit breakers and additional resilience patterns

## Acknowledgements

Some development tasks were assisted by AI coding tools, including GitHub Copilot and OpenAI models.
