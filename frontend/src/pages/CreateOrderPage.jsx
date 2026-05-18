import { useState } from "react";
import { createOrder } from "../api/orders";

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
    } catch {
      setError("Could not create the order. Check that the backend is running.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="page-panel">
      <h2>Give An Order</h2>

      <form className="order-form" onSubmit={handleSubmit}>
        <label>
          User ID
          <input
            name="userId"
            value={order.userId}
            onChange={handleChange}
            placeholder="Enter customer ID"
            required
          />
        </label>

        <label>
          Product ID
          <input
            name="productId"
            value={order.productId}
            onChange={handleChange}
            placeholder="Enter product ID"
            required
          />
        </label>

        <label>
          Quantity
          <input
            min="1"
            name="quantity"
            type="number"
            value={order.quantity}
            onChange={handleChange}
            required
          />
        </label>

        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Creating..." : "Create Order"}
        </button>
      </form>

      {error && <p className="message error">{error}</p>}

      {createdOrder && (
        <div className="result-box">
          <h3>Created Order</h3>
          <p>ID: {createdOrder.id}</p>
          <p>User: {createdOrder.userId}</p>
          <p>Product: {createdOrder.productId}</p>
          <p>Quantity: {createdOrder.quantity}</p>
          <p>Status: {createdOrder.status}</p>
        </div>
      )}
    </section>
  );
}

export default CreateOrderPage;
