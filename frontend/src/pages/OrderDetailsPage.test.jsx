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

    await user.type(screen.getByPlaceholderText(/Enter order ID/i), " order-1 ");
    await user.click(screen.getByRole("button", { name: /Get Order/i }));

    expect(getOrderById).toHaveBeenCalledWith("order-1");
    expect(await screen.findByText(/Order Information/i)).toBeInTheDocument();
    expect(screen.getByText("user-1")).toBeInTheDocument();
    expect(screen.getByText("product-1")).toBeInTheDocument();
  });

  it("shows an error when no order is found", async () => {
    const user = userEvent.setup();
    getOrderById.mockRejectedValue(new Error("not found"));

    render(<OrderDetailsPage />);

    await user.type(screen.getByPlaceholderText(/Enter order ID/i), "missing");
    await user.click(screen.getByRole("button", { name: /Get Order/i }));

    expect(await screen.findByText(/No order found for that ID/i)).toBeInTheDocument();
  });

  it("shows a cancel button when a created order is found", async () => {
    const user = userEvent.setup();
    getOrderById.mockResolvedValue(firstOrder);

    render(<OrderDetailsPage />);

    await user.type(screen.getByPlaceholderText(/Enter order ID/i), " order-1 ");
    await user.click(screen.getByRole("button", { name: /Get Order/i }));

    expect(await screen.findByRole("button", { name: /Cancel Order/i })).toBeInTheDocument();
  });

  it("does not show a cancel button when a cancelled order is found", async () => {
    const user = userEvent.setup();
    getOrderById.mockResolvedValue(cancelledOrder);

    render(<OrderDetailsPage />);

    await user.type(screen.getByPlaceholderText(/Enter order ID/i), " order-1 ");
    await user.click(screen.getByRole("button", { name: /Get Order/i }));

    expect(screen.queryByRole("button", { name: /Cancel Order/i })).not.toBeInTheDocument();
  });
});