import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { getOrderById } from "../api/orders";
import OrderDetailsPage from "./OrderDetailsPage";
import { firstOrder, cancelledOrder } from "../test/testOrders";

vi.mock("../api/orders", () => ({
  getOrderById: vi.fn(),
}));

describe("OrderDetailsPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("looks up an order by id", async () => {
    const user = userEvent.setup();
    getOrderById.mockResolvedValue(firstOrder);

    render(<OrderDetailsPage />);

    await user.type(screen.getByPlaceholderText("Enter order id"), " order-1 ");
    await user.click(screen.getByRole("button", { name: "Get Order" }));

    expect(getOrderById).toHaveBeenCalledWith("order-1");
    expect(await screen.findByText("Order Information")).toBeInTheDocument();
    expect(screen.getByText("Product: product-1")).toBeInTheDocument();
  });

  it("shows an error when no order is found", async () => {
    const user = userEvent.setup();
    getOrderById.mockRejectedValue(new Error("not found"));

    render(<OrderDetailsPage />);

    await user.type(screen.getByPlaceholderText("Enter order id"), "missing");
    await user.click(screen.getByRole("button", { name: "Get Order" }));

    expect(await screen.findByText("No order found for that ID.")).toBeInTheDocument();
  });

  it("shows a cancel button when a created order is found", async () => {
    const user = userEvent.setup();
    getOrderById.mockResolvedValue(firstOrder);

    render(<OrderDetailsPage />);

    await user.type(screen.getByPlaceholderText("Enter order id"), " order-1 ");
    await user.click(screen.getByRole("button", { name: "Get Order" }));

    expect(await screen.findByText("Cancel Order")).toBeInTheDocument();

  });

  it("don't show a cancel button when a cancelled order is found", async () => {
    const user = userEvent.setup();
    getOrderById.mockResolvedValue(cancelledOrder);

    render(<OrderDetailsPage />);

    await user.type(screen.getByPlaceholderText("Enter order id"), " order-1 ");
    await user.click(screen.getByRole("button", { name: "Get Order" }));

    expect(screen.queryByText("Cancel Order")).not.toBeInTheDocument();
  });
});
