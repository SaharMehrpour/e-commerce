import { useState } from "react";
import { getOrders } from "../api/orders";
import { getOrdersPageCache, setOrdersPageCache } from "./ordersPageCache";

function OrdersPage() {
  const initialCache = getOrdersPageCache();
  const [orders, setOrders] = useState(initialCache.orders);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [hasLoaded, setHasLoaded] = useState(initialCache.hasLoaded);

  async function loadOrders() {
    setError("");
    setIsLoading(true);
    setHasLoaded(true);

    try {
      const data = await getOrders();
      setOrdersPageCache(data);
      setOrders(data);
    } catch {
      setError("Could not load orders. Check that the backend is running.");
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <section className="page-panel">
      <div className="d-flex justify-content-between align-items-start mb-3">
        <div>
          <h2 className="mb-1">All Orders</h2>

          <p className="text-muted mb-0" style={{ fontSize: "0.9rem" }}>
            View and manage all customer orders
          </p>
        </div>

        {isLoading && (
          <div className="loading-overlay">
            <div className="spinner-box">
              <div className="spinner-border text-primary" />
              <div className="loading-text" role="status" aria-label="Loading Orders">
                Loading orders...
              </div>
            </div>
          </div>
        )}

        <button
          type="button"
          className="btn btn-primary btn-sm"
          onClick={loadOrders}
          disabled={isLoading}
        >
          {isLoading ? (
            <>
              <span
                className="spinner-border spinner-border-sm me-2"
                role="status"
              />
              Loading...
            </>
          ) : hasLoaded ? (
            <>
              <i className="bi bi-arrow-clockwise me-1"></i>
              Refresh
            </>
          ) : (
            <>
              <i className="bi bi-cloud-download me-1"></i>
              Load Orders
            </>
          )}
        </button>
      </div>

      {!hasLoaded && (
        <p className="message">Click Load Orders to fetch orders from the API.</p>
      )}

      {isLoading && <p className="message">Loading orders...</p>}
      {error && <p className="message error">{error}</p>}

      {hasLoaded && !isLoading && !error && orders.length === 0 && (
        <p className="message" role="status" aria-label="No orders yet">
          No orders yet.
        </p>
      )}

      {orders.length > 0 && (
        <table className="table table-hover align-middle orders-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>User</th>
              <th>Product</th>
              <th>Quantity</th>
              <th>Status</th>
            </tr>
          </thead>

          <tbody>
            {orders.map((order) => (
              <tr key={order.id}>
                <td className="id-cell">
                  <span>{order.id}</span>

                  <button
                    className="copy-btn"
                    onClick={() => navigator.clipboard.writeText(order.id)}
                  >
                    <i className="bi bi-copy"></i>
                  </button>
                </td>
                <td>{order.userId}</td>
                <td>{order.productId}</td>
                <td>{order.quantity}</td>

                <td>
                  <span
                    className={`status-badge ${
                      order.status === "CREATED"
                        ? "status-created"
                        : "status-cancelled"
                    }`}
                  >
                    {order.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}

export default OrdersPage;
