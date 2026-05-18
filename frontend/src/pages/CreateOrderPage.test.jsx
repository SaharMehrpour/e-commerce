import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { createOrder } from "../api/orders";
import CreateOrderPage from "./CreateOrderPage";
import { firstOrder } from "../test/testOrders";

vi.mock("../api/orders", () => ({
  createOrder: vi.fn(),
}));

describe("CreateOrderPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("creates an order from form values and shows the created order", async () => {
    const user = userEvent.setup();
    createOrder.mockResolvedValue(firstOrder);

    render(<CreateOrderPage />);

    await user.type(screen.getByPlaceholderText("Enter customer ID"), "user-1");
    await user.type(screen.getByPlaceholderText("Enter product ID"), "product-1");
    await user.clear(screen.getByLabelText("Quantity"));
    await user.type(screen.getByLabelText("Quantity"), "2");
    await user.click(screen.getByRole("button", { name: "Create Order" }));

    expect(createOrder).toHaveBeenCalledWith({
      userId: "user-1",
      productId: "product-1",
      quantity: 2,
    });
    expect(await screen.findByText("Created Order")).toBeInTheDocument();
    expect(screen.getByText("ID: order-1")).toBeInTheDocument();
    expect(screen.getByText("Status: CREATED")).toBeInTheDocument();
  });

  it("shows an error when creating an order fails", async () => {
    const user = userEvent.setup();
    createOrder.mockRejectedValue(new Error("network"));

    render(<CreateOrderPage />);

    await user.type(screen.getByPlaceholderText("Enter customer ID"), "user-1");
    await user.type(screen.getByPlaceholderText("Enter product ID"), "product-1");
    await user.click(screen.getByRole("button", { name: "Create Order" }));

    expect(
      await screen.findByText("Could not create the order. Check that the backend is running."),
    ).toBeInTheDocument();
  });
});
