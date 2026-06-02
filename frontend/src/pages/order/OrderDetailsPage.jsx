import { useState } from "react";
import {
  cancelOrderById,
  getOrderById,
  updateOrder,
} from "../../api/orders";

function OrderDetailsPage() {
  const [orderId, setOrderId] = useState("");
  const [order, setOrder] = useState(null);
  const [orderFields, setOrderFields] = useState(null);
  const [validatedOrder, setValidatedOrder] = useState(null);

  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isEditing, setIsEditing] = useState(false);

  async function handleFindOrder(event) {
    event.preventDefault();
    setError("");
    setOrder(null);
    setOrderFields(null);
    setValidatedOrder(null);
    setIsEditing(false);

    try {
      setIsLoading(true);
      const data = await getOrderById(orderId.trim());
      setOrder(data);
    } catch (error) {
      setError(error.message);
    } finally {
      setIsLoading(false);
    }
  }

  async function handleUpdate() {
    try {
      setIsLoading(true);

      const data = await updateOrder(orderId.trim(), validatedOrder);

      setOrder(data);
      setOrderFields(null);
      setIsEditing(false);
    } catch (error) {
      setError(error.message);
    } finally {
      setValidatedOrder(null);
      setIsLoading(false);
    }
  }

  async function handleCancel() {
    try {
      setIsLoading(true);

      const data = await cancelOrderById(orderId.trim());

      setOrder(data);
      setValidatedOrder(null);
      setOrderFields(null);
      setIsEditing(false);
    } catch (error) {
      setError(error.message);
    } finally {
      setIsLoading(false);
    }
  }

  function handleChange(field, value) {
    setOrderFields((prev) => ({
      ...(prev || order),
      [field]: value,
    }));

    setValidatedOrder((prev) => {
      const nextState = { ...prev };
      if (order && order[field] === value) {
        delete nextState[field];
      } else {
        nextState[field] = value;
      }
      return nextState;
    });
  }

  const displayOrder = orderFields || order;
  const isSaveDisabled = !validatedOrder || Object.keys(validatedOrder).length === 0 || isLoading;

  
  function renderSearch() {
    return (
      <form onSubmit={handleFindOrder} className="mb-4">
        <div className="input-group">
          <input
            type="text"
            className="form-control"
            value={orderId}
            onChange={(e) => setOrderId(e.target.value)}
            placeholder="Enter order ID"
            required
          />

          <button
            type="submit"
            className="btn btn-primary"
            disabled={isLoading}
          >
            {isLoading ? "Searching..." : "Get Order"}
          </button>
        </div>
      </form>
    );
  }

  function renderStatusBadge() {
    return (
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h5 className="mb-0">
          Order Details
        </h5>

        <span
          className={`status-badge ${order.status === "CREATED"
            ? "status-created"
            : "status-cancelled"
            }`}
        >
          {order.status}
        </span>
      </div>
    );
  }

  function renderIDField() {
    return (
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
          title={order.id}
        >
          {order.id}
        </div>
      </div>
    );
  }

  function renderUserField() {
    return (
      <div className="col-md-4">
        <div className="order-field compact-field">
          <span className="order-field-label">
            User
          </span>
          <span>{order.userId}</span>
        </div>
      </div>
    );
  }

  function renderProductField() {
    return (
      <div className="col-md-4">
        <div className="order-field compact-field">
          <span className="order-field-label">
            Product
          </span>

          {isEditing ? (
            <input
              type="text"
              className="form-control form-control-sm"
              value={displayOrder?.productId || ""}
              onChange={(e) => handleChange("productId", e.target.value)}
            />
          ) : (
            <span>{order.productId}</span>
          )}
        </div>
      </div>
    );
  }

  function renderQuantityField() {
    return (
      <div className="col-md-4">
        <div className="order-field compact-field">
          <span className="order-field-label">
            Quantity
          </span>

          {isEditing ? (
            <input
              type="number"
              min="1"
              className="form-control form-control-sm"
              value={displayOrder?.quantity || 1}
              onChange={(e) => handleChange("quantity", Number(e.target.value))}
            />
          ) : (
            <span>{order.quantity}</span>
          )}
        </div>
      </div>
    );
  }

  return (
    <section className="page-panel">
      <div className="mb-4">
        <h2 className="mb-1">Order Lookup</h2>

        <p className="section-subtitle mb-0">
          Search for an order by ID
        </p>
      </div>

      {isLoading && (
        <div className="loading-overlay">
          <div className="spinner-box">
            <div className="spinner-border text-primary" role="status" />
            <div className="loading-text">
              Loading order...
            </div>
          </div>
        </div>
      )}

      {/* Search */}
      {renderSearch()}

      {/* Error */}
      {error && (
        <div className="alert alert-danger py-2">
          {error}
        </div>
      )}

      {/* Order Card (same style as CreateOrderPage result) */}
      {order && (
        <div className="alert alert-success border-0 shadow-sm mt-4">
          {renderStatusBadge()}

          {/* ID */}
          {renderIDField()}

          {/* Compact fields */}
          <div className="row g-2">
            {/* User */}
            {renderUserField()}

            {/* Product */}
            {renderProductField()}

            {/* Quantity */}
            {renderQuantityField()}
          </div>

          {/* Actions */}
          {order.status === "CREATED" && (
            <div className="mt-4 d-flex justify-content-end gap-2">
              {!isEditing ? (
                <>
                  <button
                    className="btn btn-outline-danger btn-sm"
                    disabled={isLoading}
                    onClick={handleCancel}
                    type="button"
                  >
                    Cancel Order
                  </button>

                  <button
                    className="btn btn-primary btn-sm"
                    type="button"
                    onClick={() => setIsEditing(true)}
                  >
                    Update Order
                  </button>
                </>
              ) : (
                <>
                  <button
                    className="btn btn-light border btn-sm"
                    type="button"
                    onClick={() => {
                      setIsEditing(false);
                      setOrderFields(null);
                    }}
                  >
                    Discard
                  </button>

                  <button
                    className="btn btn-success btn-sm"
                    type="button"
                    onClick={handleUpdate}
                    disabled={isSaveDisabled}
                  >
                    Save Changes
                  </button>
                </>
              )}
            </div>
          )}
        </div>
      )}
    </section>
  );
}

export default OrderDetailsPage;
