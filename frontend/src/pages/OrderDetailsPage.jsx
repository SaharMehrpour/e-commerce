import { useState } from "react";
import { cancelOrderById, getOrderById } from "../api/orders";

function OrderDetailsPage() {
  const [orderId, setOrderId] = useState("");
  const [order, setOrder] = useState(null);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setOrder(null);
    setIsLoading(true);

    try {
      const data = await getOrderById(orderId.trim());
      setOrder(data);
    } catch {
      setError("No order found for that ID.");
    } finally {
      setIsLoading(false);
    }
  }

  async function handleCancel() {
    setError("");
    setIsLoading(true);
    
    try { 
      const data = await cancelOrderById(orderId.trim());
      setOrder(data);
    } catch {
      setError("Failed to cancel the order. Please try again.");
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <section className="page-panel">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <div>
          <h2 className="mb-1">Order Lookup</h2>

          <p className="section-subtitle mb-0">
            Search for an order by ID
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="mb-3">
        <div className="input-group">
          <input
            type="text"
            className="form-control"
            value={orderId}
            onChange={(event) => setOrderId(event.target.value)}
            placeholder="Enter order ID"
            required
          />

          <button
            type="submit"
            className="btn btn-primary"
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <span
                  className="spinner-border spinner-border-sm me-2"
                  role="status"
                />
                Searching...
              </>
            ) : (
              "Get Order"
            )}
          </button>
        </div>
      </form>

      {error && (
        <div className="alert alert-danger py-2">
          {error}
        </div>
      )}

      {order && (
        <div className="card border-0 shadow-sm">
          <div className="card-body p-3">
            <div className="d-flex justify-content-between align-items-center mb-3">
              <div>
                <h5 className="mb-0">
                  Order Information
                </h5>
              </div>

              <span
                className={`status-badge ${
                  order.status === "CREATED"
                    ? "status-created"
                    : "status-cancelled"
                }`}
              >
                {order.status}
              </span>
            </div>

            {/* Order ID Row */}
            <div className="border rounded p-2 mb-3 bg-light">
              <div className="d-flex justify-content-between align-items-center gap-3">
                <div className="flex-grow-1 overflow-hidden">
                  <div className="order-field-label mb-1">
                    Order ID
                  </div>

                  <div
                    className="text-truncate"
                    title={order.id}
                    style={{
                      fontSize: "0.9rem",
                      fontFamily: "monospace",
                    }}
                  >
                    {order.id}
                  </div>
                </div>
              </div>
            </div>

            {/* Compact Fields */}
            <div className="row g-2">
              <div className="col-md-4">
                <div className="order-field compact-field">
                  <span className="order-field-label">
                    User
                  </span>

                  <span>{order.userId}</span>
                </div>
              </div>

              <div className="col-md-4">
                <div className="order-field compact-field">
                  <span className="order-field-label">
                    Product
                  </span>

                  <span>{order.productId}</span>
                </div>
              </div>

              <div className="col-md-4">
                <div className="order-field compact-field">
                  <span className="order-field-label">
                    Quantity
                  </span>

                  <span>{order.quantity}</span>
                </div>
              </div>
            </div>

            {order.status === "CREATED" && (
              <div className="mt-3 d-flex justify-content-end">
                <button
                  type="button"
                  className="btn btn-danger btn-sm"
                  disabled={isLoading}
                  onClick={handleCancel}
                >
                  {isLoading ? (
                    <>
                      <span
                        className="spinner-border spinner-border-sm me-2"
                        role="status"
                      />
                      Sending Request...
                    </>
                  ) : (
                    "Cancel Order"
                  )}
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </section>
  );
}

export default OrderDetailsPage;
