import { useState, useContext, useRef, useEffect } from "react";
import "./App.css";
import CreateOrderPage from "./pages/order/CreateOrderPage";
import HomePage from "./pages/HomePage";
import OrderDetailsPage from "./pages/order/OrderDetailsPage";
import OrdersPage from "./pages/order/OrdersPage";
import InventoryPage from "./pages/inventory/InventoryPage";
import { AuthContext } from "./auth/AuthContext";

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
  const [userMenuOpen, setUserMenuOpen] = useState(false);

  const { keycloak, authenticated } = useContext(AuthContext);

  const userMenuRef = useRef(null);

  const isAdmin = keycloak?.realmAccess?.roles?.includes("ADMIN");

  const effectivePage =
    activePage === "inventory" && (!authenticated || !isAdmin)
      ? "home"
      : activePage;

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        userMenuRef.current &&
        !userMenuRef.current.contains(event.target)
      ) {
        setUserMenuOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const getUsername = () => {
    return (
      keycloak?.tokenParsed?.preferred_username ||
      keycloak?.tokenParsed?.name ||
      keycloak?.tokenParsed?.email ||
      "User"
    );
  };

  return (
    <main className="app-shell">
      <header className="app-header app-header-modern">
        {/* =========================
            TOP HEADER
        ========================= */}
        <div className="header-top">
          <div className="brand">
            <div className="brand-title">E-Commerce</div>
            <div className="brand-subtitle">
              Order Management System
            </div>
          </div>

          <div className="header-actions">
            {authenticated ? (
              <div className="user-menu" ref={userMenuRef}>
                <button
                  type="button"
                  className="user-menu-trigger"
                  onClick={() => setUserMenuOpen((open) => !open)}
                  aria-expanded={userMenuOpen}
                  aria-haspopup="menu"
                >
                  <span>Hello, {getUsername()}</span>

                  <span className="user-menu-chevron">
                    {userMenuOpen ? "▲" : "▼"}
                  </span>
                </button>

                {userMenuOpen && (
                  <div className="user-dropdown" role="menu">
                    <div className="user-dropdown-header">
                      <div className="user-dropdown-name">
                        {getUsername()}
                      </div>

                      {keycloak?.tokenParsed?.email && (
                        <div className="user-dropdown-email">
                          {keycloak.tokenParsed.email}
                        </div>
                      )}
                    </div>

                    <div className="user-dropdown-divider" />

                    <button
                      type="button"
                      className="user-dropdown-item"
                      onClick={() => {
                        setUserMenuOpen(false);
                        keycloak.logout();
                      }}
                    >
                      Logout
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <button
                type="button"
                className="login-button"
                onClick={() => keycloak.login()}
              >
                Sign In
              </button>
            )}
          </div>
        </div>

        {/* =========================
            NAVIGATION
        ========================= */}
        <nav
          className="page-tabs nav-pills"
          aria-label="Order pages"
        >
          {Object.entries(pages)
            .filter(([pageId]) => {
              if (pageId === "home") return true;
              if (!authenticated) return false;
              if (pageId === "inventory") return isAdmin;
              return true;
            })
            .map(([pageId, page]) => (
              <button
                key={pageId}
                type="button"
                onClick={() => setActivePage(pageId)}
                className={`nav-pill ${effectivePage === pageId ? "active" : ""
                  }`}
              >
                {page.label}
              </button>
            ))}
        </nav>
      </header>

      {/* =========================
          PAGE CONTENT
      ========================= */}
      <section className="page-container">
        {pages[effectivePage].component}
      </section>
    </main>
  );
}

export default App;
