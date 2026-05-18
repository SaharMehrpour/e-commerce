import { useState } from "react";
import { getOrders } from "../api/orders";

let cachedOrders = [];
let cachedHasLoaded = false;

function OrdersPage() {
  const [orders, setOrders] = useState(cachedOrders);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [hasLoaded, setHasLoaded] = useState(cachedHasLoaded);

  async function loadOrders() {
    setError("");
    setIsLoading(true);
    setHasLoaded(true);

    try {
      const data = await getOrders();
      cachedOrders = data;
      cachedHasLoaded = true;
      setOrders(data);
    } catch {
      setError("Could not load orders. Check that the backend is running.");
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <section className="page-panel">
      <div className="page-header">
        <h2>All Orders</h2>
        <button type="button" onClick={loadOrders} disabled={isLoading}>
          {hasLoaded ? "Refresh" : "Load Orders"}
        </button>
      </div>

      {!hasLoaded && (
        <p className="message">Click Load Orders to fetch orders from the API.</p>
      )}

      {isLoading && <p className="message">Loading orders...</p>}
      {error && <p className="message error">{error}</p>}

      {hasLoaded && !isLoading && !error && orders.length === 0 && (
        <p className="message">No orders yet.</p>
      )}

      <div className="orders-list">
        {orders.map((order) => (
          <article className="order-card" key={order.id}>
            <p>ID: {order.id}</p>
            <p>User: {order.userId}</p>
            <p>Product: {order.productId}</p>
            <p>Quantity: {order.quantity}</p>
            <p>Status: {order.status}</p>
          </article>
        ))}
      </div>
    </section>
  );
}

export default OrdersPage;
