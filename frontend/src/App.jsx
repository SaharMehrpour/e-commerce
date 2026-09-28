import { useState, useContext } from "react";
import "./App.css";
import CreateOrderPage from "./pages/order/CreateOrderPage";
import HomePage from "./pages/HomePage";
import OrderDetailsPage from "./pages/order/OrderDetailsPage";
import OrdersPage from "./pages/order/OrdersPage";
import InventoryPage from "./pages/inventory/InventoryPage";
import { AuthContext } from "./auth/AuthProvider";

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
  inventory: {
    label: "Inventory",
    component: <InventoryPage />,
  },
};

function App() {
  const [activePage, setActivePage] = useState("home");
  const { keycloak, authenticated } = useContext(AuthContext);

  return (
    <main className="app-shell">
      <header className="app-header app-header-modern">
        <div className="brand">
          <div className="brand-title">E-Commerce</div>
          <div className="brand-subtitle">Order Management System</div>
        </div>

        <div>
          {authenticated ? (
              <>
                  <span>Logged in</span>
                  <button onClick={() => keycloak.logout()}>
                      Logout
                  </button>
              </>
          ) : (
              <button onClick={() => keycloak.login()}>
                  Login
              </button>
          )}
          <button
            onClick={async () => {
                await keycloak.updateToken(30);
                const response = await fetch("http://localhost:8082/orders", {
                    headers: {
                        Authorization: `Bearer ${keycloak.token}`,
                    },
                });

                console.log("Order API status:", response.status);
                console.log("Order API response:", await response.text());
            }}
        >
            Test Order API
        </button>
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
