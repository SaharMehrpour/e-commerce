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
      <h2>Order By ID</h2>

      <form className="lookup-form" onSubmit={handleSubmit}>
        <label>
          Order ID
          <input
            value={orderId}
            onChange={(event) => setOrderId(event.target.value)}
            placeholder="Enter order id"
            required
          />
        </label>

        <button type="submit" disabled={isLoading}>
          {isLoading ? "Searching..." : "Get Order"}
        </button>
      </form>

      {error && <p className="message error">{error}</p>}

      {order && (
        <div className="result-box">
          <h3>Order Information</h3>
          <p>ID: {order.id}</p>
          <p>User: {order.userId}</p>
          <p>Product: {order.productId}</p>
          <p>Quantity: {order.quantity}</p>
          <p>Status: {order.status}</p>
        </div>
      )}
      
      {order && order.status === "CREATED" && (
        <div className="result-box">
          <button type="submit" disabled={isLoading} onClick={handleCancel}>
            {isLoading ? "Sending Request..." : "Cancel Order"}
          </button>
        </div>
      )}

    </section>
  );
}

export default OrderDetailsPage;
