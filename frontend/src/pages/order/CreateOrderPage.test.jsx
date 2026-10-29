import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { createOrder } from "../../api/orders";
import { AuthContext } from "../../auth/AuthContext";
import CreateOrderPage from "./CreateOrderPage";
import { firstOrder } from "../../test/testOrders";

vi.mock("../../api/orders", () => ({
  createOrder: vi.fn(),
}));

const mockKeycloak = {
  token: "mock-token",
  authenticated: true,
};

function renderCreateOrderPage() {
  return render(
    <AuthContext.Provider
      value={{
        keycloak: mockKeycloak,
        authenticated: true,
      }}
    >
      <CreateOrderPage />
    </AuthContext.Provider>,
  );
}

describe("CreateOrderPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("creates an order from form values and shows the created order", async () => {
    const user = userEvent.setup();
    createOrder.mockResolvedValue(firstOrder);

    renderCreateOrderPage();

    await user.type(
      screen.getByPlaceholderText("Enter product ID"),
      "product-1",
    );

    await user.clear(screen.getByLabelText("Quantity"));
    await user.type(screen.getByLabelText("Quantity"), "2");

    await user.click(
      screen.getByRole("button", { name: "Create Order" }),
    );

    expect(createOrder).toHaveBeenCalledWith(
      {
        productId: "product-1",
        quantity: 2,
      },
      mockKeycloak,
    );

    expect(await screen.findByText("Order Created")).toBeInTheDocument();
    expect(screen.getByText(/Order ID/i)).toBeInTheDocument();
    expect(screen.getByText(firstOrder.status)).toBeInTheDocument();
  });

  it("shows an error when creating an order fails", async () => {
    const user = userEvent.setup();
    createOrder.mockRejectedValue(new Error("network error"));

    renderCreateOrderPage();

    await user.type(
      screen.getByPlaceholderText("Enter product ID"),
      "product-1",
    );

    await user.click(
      screen.getByRole("button", { name: /Create Order/i }),
    );

    expect(await screen.findByText(/network error/i)).toBeInTheDocument();
  });
});
