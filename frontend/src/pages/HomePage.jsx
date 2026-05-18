function HomePage() {
  return (
    <section className="page-panel home-page">
      <div className="home-intro">
        <h2>Order Management Demo</h2>
        <p>
          This app demonstrates a small e-commerce order workflow. You can
          create an order, fetch the full order list, and look up one order by
          its ID.
        </p>
      </div>

      <div className="info-grid">
        <article className="info-card">
          <h3>Frontend</h3>
          <p>React with Vite provides the browser UI and calls the order API.</p>
        </article>

        <article className="info-card">
          <h3>Backend</h3>
          <p>Spring Boot exposes REST endpoints for creating and reading orders.</p>
        </article>

        <article className="info-card">
          <h3>Data</h3>
          <p>MongoDB stores order records so they can be reused across sessions.</p>
        </article>

        <article className="info-card">
          <h3>Messaging</h3>
          <p>Kafka receives an event when a new order is created.</p>
        </article>

        <article className="info-card">
          <h3>Local Setup</h3>
          <p>Docker Compose starts MongoDB, Kafka, and Zookeeper for development.</p>
        </article>
      </div>
    </section>
  );
}

export default HomePage;
