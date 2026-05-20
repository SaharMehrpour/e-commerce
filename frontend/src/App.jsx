import { useState } from "react";
import "./App.css";
import CreateOrderPage from "./pages/CreateOrderPage";
import HomePage from "./pages/HomePage";
import OrderDetailsPage from "./pages/OrderDetailsPage";
import OrdersPage from "./pages/OrdersPage";

const pages = {
  home: {
    label: "Home",
    component: <HomePage />,
  },
  create: {
    label: "Give Order",
    component: <CreateOrderPage />,
  },
  orders: {
    label: "All Orders",
    component: <OrdersPage />,
  },
  details: {
    label: "Order Info",
    component: <OrderDetailsPage />,
  },
};

function App() {
  const [activePage, setActivePage] = useState("home");

  return (
    <main className="app-shell">
      <header className="app-header app-header-modern">
        <div className="brand">
          <div className="brand-title">E-Commerce</div>
          <div className="brand-subtitle">Order Management System</div>
        </div>

        <nav className="page-tabs nav-pills" aria-label="Order pages">
          {Object.entries(pages).map(([pageId, page]) => (
            <button
              key={pageId}
              type="button"
              onClick={() => setActivePage(pageId)}
              className={`nav-pill ${
                activePage === pageId ? "active" : ""
              }`}
            >
              {page.label}
            </button>
          ))}
        </nav>
      </header>

      <section className="page-container">
        {pages[activePage].component}
      </section>
    </main>
  );
}

export default App;
