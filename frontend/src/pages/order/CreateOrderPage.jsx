import { useState } from "react";
import { createOrder } from "../../api/orders";

const initialOrder = {
  userId: "",
  productId: "",
  quantity: 1,
};

function CreateOrderPage() {
  const [order, setOrder] = useState(initialOrder);
  const [createdOrder, setCreatedOrder] = useState(null);
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function handleChange(event) {
    const { name, value } = event.target;

    setOrder((currentOrder) => ({
      ...currentOrder,
      [name]: name === "quantity" ? Number(value) : value,
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setCreatedOrder(null);
    setIsSubmitting(true);

    try {
      const data = await createOrder(order);
      setCreatedOrder(data);
      setOrder(initialOrder);
    } catch (error) {
      setError(error.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="page-panel">
      <div className="mb-4">
        <h2 className="mb-1">Create Order</h2>

        <p className="section-subtitle mb-0">
          Submit a new customer order
        </p>
      </div>

      {isSubmitting && (
        <div className="loading-overlay">
          <div className="spinner-box">
            <div className="spinner-border text-primary" role="status" />
              <div className="loading-text">Submitting an order...</div>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="row g-3">
          <div className="col-md-6">
            <label className="form-label">User ID</label>

            <input
              type="text"
              name="userId"
              className="form-control"
              value={order.userId}
              onChange={handleChange}
              placeholder="Enter customer ID"
              required
            />
          </div>

          <div className="col-md-6">
            <label className="form-label">Product ID</label>

            <input
              type="text"
              name="productId"
              className="form-control"
              value={order.productId}
              onChange={handleChange}
              placeholder="Enter product ID"
              required
            />
          </div>

          <div className="col-md-3">
            <label htmlFor="quantity-input" className="form-label">Quantity</label>

            <input
              id="quantity-input"
              type="number"
              min="1"
              name="quantity"
              className="form-control"
              value={order.quantity}
              onChange={handleChange}
              required
            />
          </div>
        </div>

        <div className="mt-4 d-flex justify-content-end">
          <button
            type="submit"
            className="btn btn-primary"
            disabled={isSubmitting}
          >
            {isSubmitting ? "Creating..." : "Create Order"}
          </button>
        </div>
      </form>

      {error && (
        <div className="alert alert-danger mt-3 py-2">
          {error}
        </div>
      )}

      {createdOrder && (
        <div className="alert alert-success border-0 shadow-sm mt-4">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h5 className="mb-0">Order Created</h5>

            <span className="status-badge status-created">
              {createdOrder.status}
            </span>
          </div>

          {/* ID Row */}
          <div className="border rounded p-2 mb-3 bg-light">
            <div className="order-field-label mb-1">
              Order ID
            </div>

            <div
              className="text-truncate"
              style={{
                fontSize: "0.9rem",
                fontFamily: "monospace",
              }}
              title={createdOrder.id}
            >
              {createdOrder.id}
            </div>
          </div>

          {/* Compact details */}
          <div className="row g-2">
            <div className="col-md-4">
              <div className="order-field compact-field">
                <span className="order-field-label">
                  User
                </span>
                <span>{createdOrder.userId}</span>
              </div>
            </div>

            <div className="col-md-4">
              <div className="order-field compact-field">
                <span className="order-field-label">
                  Product
                </span>
                <span>{createdOrder.productId}</span>
              </div>
            </div>

            <div className="col-md-4">
              <div className="order-field compact-field">
                <span className="order-field-label">
                  Quantity
                </span>
                <span>{createdOrder.quantity}</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

export default CreateOrderPage;
