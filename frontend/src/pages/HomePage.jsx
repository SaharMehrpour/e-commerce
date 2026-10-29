function HomePage() {
  return (
    <section className="page-panel">
      <div className="hero-section mb-4">
        <h2 className="hero-title">Order Management Demo</h2>

        <p className="hero-text">
          A full-stack e-commerce application built with React, Spring Boot,
          Kafka, Redis, MongoDB, PostgreSQL, and Keycloak, demonstrating
          microservices, event-driven communication, and secure access control.
        </p>
      </div>

      <div className="row g-3">
        <div className="col-md-6 col-lg-4">
          <div className="card info-card h-100">
            <div className="card-body">
              <h5 className="card-title">
                <i className="bi bi-window me-2"></i>
                Frontend
              </h5>

              <p className="card-text">
                React with Vite powers the browser UI, API integration,
                and authenticated user workflows.
              </p>
            </div>
          </div>
        </div>

        <div className="col-md-6 col-lg-4">
          <div className="card info-card h-100">
            <div className="card-body">
              <h5 className="card-title">
                <i className="bi bi-server me-2"></i>
                Backend
              </h5>

              <p className="card-text">
                Spring Boot microservices expose REST APIs for order
                management and inventory operations.
              </p>
            </div>
          </div>
        </div>

        <div className="col-md-6 col-lg-4">
          <div className="card info-card h-100">
            <div className="card-body">
              <h5 className="card-title">
                <i className="bi bi-database me-2"></i>
                Data Storage
              </h5>

              <p className="card-text">
                MongoDB stores orders, PostgreSQL manages inventory and
                event idempotency, and Redis provides caching.
              </p>
            </div>
          </div>
        </div>

        <div className="col-md-6 col-lg-4">
          <div className="card info-card h-100">
            <div className="card-body">
              <h5 className="card-title">
                <i className="bi bi-broadcast me-2"></i>
                Event-Driven Messaging
              </h5>

              <p className="card-text">
                Kafka supports asynchronous communication between
                microservices through domain events.
              </p>
            </div>
          </div>
        </div>

        <div className="col-md-6 col-lg-4">
          <div className="card info-card h-100">
            <div className="card-body">
              <h5 className="card-title">
                <i className="bi bi-shield-lock me-2"></i>
                Security & Access Control
              </h5>

              <p className="card-text">
                Keycloak handles authentication using OpenID Connect (OIDC)
                and JWT access tokens. Spring Security validates tokens
                and enforces role-based access control (RBAC).
              </p>

              <ul className="mb-0">
                <li>
                  <strong>ADMIN:</strong> Authorized to manage inventory.
                </li>
                <li>
                  <strong>CUSTOMER:</strong> Accesses permitted customer operations.
                </li>
                <li>
                  <strong>Authorization:</strong> Protected endpoints enforce
                  role-based permissions.
                </li>
              </ul>
            </div>
          </div>
        </div>

        <div className="col-md-6 col-lg-4">
          <div className="card info-card h-100">
            <div className="card-body">
              <h5 className="card-title">
                <i className="bi bi-diagram-3 me-2"></i>
                API Gateway
              </h5>

              <p className="card-text">
                Spring Cloud Gateway provides a unified entry point
                and routes client requests to internal microservices.
              </p>
            </div>
          </div>
        </div>

        <div className="col-md-6 col-lg-4">
          <div className="card info-card h-100">
            <div className="card-body">
              <h5 className="card-title">
                <i className="bi bi-box-seam me-2"></i>
                Containerization
              </h5>

              <p className="card-text">
                Docker and Docker Compose simplify local development
                by running application services and supporting infrastructure.
              </p>
            </div>
          </div>
        </div>

        <div className="col-md-6 col-lg-4">
          <div className="card info-card h-100">
            <div className="card-body">
              <h5 className="card-title">
                <i className="bi bi-cloud me-2"></i>
                Kubernetes
              </h5>

              <p className="card-text">
                Minikube and Kubernetes manifests deploy the application
                locally, with NGINX Ingress routing traffic through
                configured hostnames.
              </p>
            </div>
          </div>
        </div>

        <div className="col-md-6 col-lg-4">
          <div className="card info-card h-100">
            <div className="card-body">
              <h5 className="card-title">
                <i className="bi bi-graph-up me-2"></i>
                Monitoring
              </h5>

              <p className="card-text">
                Spring Boot Actuator and Micrometer expose application
                metrics, which Prometheus collects for observability.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

export default HomePage;
