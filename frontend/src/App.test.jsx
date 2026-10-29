import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { AuthContext } from "./auth/AuthContext";
import App from "./App";

vi.mock("./api/orders", () => ({
  createOrder: vi.fn(),
  getOrderById: vi.fn(),
  getOrders: vi.fn(),
}));

const mockKeycloak = {
  token: "mock-token",
  tokenParsed: {
    preferred_username: "testuser",
    email: "test@example.com",
  },
  realmAccess: { roles: [] },
  login: vi.fn(),
  logout: vi.fn(),
  updateToken: vi.fn().mockResolvedValue(true),
};

function renderApp({ authenticated = false, roles = [] } = {}) {
  const keycloak = {
    ...mockKeycloak,
    realmAccess: { roles },
  };

  return render(
    <AuthContext.Provider value={{ keycloak, authenticated }}>
      <App />
    </AuthContext.Provider>
  );
}

describe("App", () => {
  it("shows only Home when the user is logged out", () => {
    renderApp();

    expect(
      screen.getByRole("button", { name: "Home" })
    ).toBeInTheDocument();

    expect(
      screen.queryByRole("button", { name: /give order/i })
    ).not.toBeInTheDocument();

    expect(
      screen.queryByRole("button", { name: /all orders/i })
    ).not.toBeInTheDocument();

    expect(
      screen.queryByRole("button", { name: /order info/i })
    ).not.toBeInTheDocument();

    expect(
      screen.queryByRole("button", { name: "Inventory" })
    ).not.toBeInTheDocument();
  });

  it("hides Inventory from authenticated CUSTOMER users", () => {
    renderApp({
      authenticated: true,
      roles: ["CUSTOMER"],
    });

    expect(
      screen.getByRole("button", { name: /give order/i })
    ).toBeInTheDocument();

    expect(
      screen.getByRole("button", { name: /all orders/i })
    ).toBeInTheDocument();

    expect(
      screen.getByRole("button", { name: /order info/i })
    ).toBeInTheDocument();

    expect(
      screen.queryByRole("button", { name: "Inventory" })
    ).not.toBeInTheDocument();
  });

  it("shows Inventory to authenticated ADMIN users", () => {
    renderApp({
      authenticated: true,
      roles: ["ADMIN"],
    });

    expect(
      screen.getByRole("button", { name: "Inventory" })
    ).toBeInTheDocument();
  });

  it("navigates between pages for an authenticated user", async () => {
    const user = userEvent.setup();

    renderApp({
      authenticated: true,
      roles: ["CUSTOMER"],
    });

    await user.click(
      screen.getByRole("button", { name: /give order/i })
    );

    expect(
      screen.getByRole("heading", { name: /create order/i })
    ).toBeInTheDocument();

    await user.click(
      screen.getByRole("button", { name: /all orders/i })
    );

    expect(
      screen.getByRole("heading", { name: /all orders/i })
    ).toBeInTheDocument();

    await user.click(
      screen.getByRole("button", { name: /order info/i })
    );

    expect(
      screen.getByRole("heading", { name: /order lookup/i })
    ).toBeInTheDocument();
  });
});
