function HomePage() {
  return (
    <section className="page-panel">
      <div className="hero-section mb-4">
        <h2 className="hero-title">Order Management Demo</h2>

        <p className="hero-text">
          A small full-stack e-commerce demo application built with React,
          Spring Boot, MongoDB, Kafka, and Docker.
        </p>
      </div>

      <div className="row g-3">
        <div className="col-md-6 col-lg-4">
          <div className="card info-card">
            <div className="card-body">
              <h5 className="card-title">
                <i className="bi bi-window me-2"></i>
                Frontend
              </h5>

              <p className="card-text">
                React with Vite powers the browser UI and API integration.
              </p>
            </div>
          </div>
        </div>

        <div className="col-md-6 col-lg-4">
          <div className="card info-card">
            <div className="card-body">
              <h5 className="card-title">
                <i className="bi bi-server me-2"></i>
                Backend
              </h5>

              <p className="card-text">
                Spring Boot provides REST APIs for order operations.
              </p>
            </div>
          </div>
        </div>

        <div className="col-md-6 col-lg-4">
          <div className="card info-card">
            <div className="card-body">
              <h5 className="card-title">
                <i className="bi bi-database me-2"></i>
                Database
              </h5>

              <p className="card-text">
                MongoDB stores order data across sessions.
              </p>
            </div>
          </div>
        </div>

        <div className="col-md-6 col-lg-4">
          <div className="card info-card">
            <div className="card-body">
              <h5 className="card-title">
                <i className="bi bi-broadcast me-2"></i>
                Messaging
              </h5>

              <p className="card-text">
                Kafka receives order creation events asynchronously.
              </p>
            </div>
          </div>
        </div>

        <div className="col-md-6 col-lg-4">
          <div className="card info-card">
            <div className="card-body">
              <h5 className="card-title">
                <i className="bi bi-box-seam me-2"></i>
                Local Setup
              </h5>

              <p className="card-text">
                Docker Compose manages MongoDB, Kafka, and Zookeeper.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

export default HomePage;
