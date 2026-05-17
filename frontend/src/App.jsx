import { useEffect, useState } from "react";
import { createOrder, getOrders } from "./api/orders";

function App() {

  const [orders, setOrders] = useState([]);

  async function loadOrders() {
    const data = await getOrders();
    setOrders(data);
  }

  useEffect(() => {
    loadOrders();
  }, []);

  async function handleCreateOrder() {

    const newOrder = {
      userId: "u1",
      productId: "p1",
      quantity: 2
    };

    await createOrder(newOrder);

    loadOrders();
  }

  return (
    <div style={{ padding: "20px" }}>

      <h1>E-Commerce Orders</h1>

      <button onClick={handleCreateOrder}>
        Create Order
      </button>

      <hr />

      {orders.map((order, index) => (
        <div key={index}>
          <p>User: {order.userId}</p>
          <p>Product: {order.productId}</p>
          <p>Quantity: {order.quantity}</p>
          <p>Status: {order.status}</p>

          <hr />
        </div>
      ))}

    </div>
  );
}

export default App;