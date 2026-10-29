# Testing, Metrics, and Development Notes

[← Back to the main README](../README.md)

## Testing

### Backend

The backend uses JUnit 5 and Mockito for unit and integration testing across controllers, services, Kafka producers and consumers, and caching behavior.

Run backend tests from the repository root:

```bash
./mvnw clean test
```

### Frontend

The frontend uses Vitest and React Testing Library.

Run frontend tests from the frontend directory:

```bash
npm run test
```

## Metrics and observability

The project uses Spring Boot Actuator and Micrometer to expose application and business metrics to Prometheus.

Examples include:

- Order creation metrics
- Order cancellation metrics
- Inventory update metrics

### Endpoints

| Purpose | URL |
|---|---|
| Prometheus scrape endpoint | `http://localhost:8080/actuator/prometheus` |
| Spring Boot metrics endpoint | `http://localhost:8080/actuator/metrics` |
| Prometheus UI | `http://localhost:9090` |

The URLs above describe the local Docker Compose setup. In Kubernetes, access depends on the deployed service and Ingress configuration.

## Key design decisions

### Event-driven architecture

Services publish and consume domain events through Kafka to reduce direct coupling and support asynchronous workflows.

### Idempotent event processing

Consumers persist processed event IDs in PostgreSQL to help prevent duplicate business operations when messages are retried or redelivered.

### API Gateway

Provides a unified entry point for client requests while hiding internal service topology.

### Keycloak authentication and RBAC

Centralizes user authentication and role management. OIDC and JWT support authentication, while Spring Security enforces role-based access rules on protected endpoints. See [Authentication and Authorization](SECURITY.md).

### Polyglot persistence

MongoDB and PostgreSQL are used according to each service's data needs.

### Redis caching

Redis can improve performance and reduce repeated database reads for frequently accessed data.

### Kubernetes deployment

Minikube and Ingress provide a local environment for practicing container orchestration and service routing.

### Service ownership

Each microservice owns its domain logic, persistence, APIs, and event publishing responsibilities.

### Containerized infrastructure

Docker Compose supports local development; Kubernetes and Minikube demonstrate orchestration.

## Future improvements

- Payment Service
- User Profile Service
- Notification Service
- Transactional Outbox Pattern
- Distributed tracing with OpenTelemetry
- Grafana dashboards
- CI/CD pipeline using GitHub Actions
- Service discovery
- Circuit breakers and additional resilience patterns
