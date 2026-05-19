import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { getOrders } from "../api/orders";
import OrdersPage from "./OrdersPage";
import { resetOrdersPageCache } from "./ordersPageCache";
import { firstOrder, secondOrder } from "../test/testOrders";

vi.mock("../api/orders", () => ({
  getOrders: vi.fn(),
}));

describe("OrdersPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    resetOrdersPageCache();
  });

  it("does not call the API until Load Orders is clicked", () => {
    render(<OrdersPage />);

    expect(getOrders).not.toHaveBeenCalled();
    expect(screen.getByText(/click load orders/i)).toBeInTheDocument();
  });

  it("loads orders when clicked and keeps the latest loaded orders after remount", async () => {
    const user = userEvent.setup();
    getOrders.mockResolvedValueOnce([firstOrder]);

    const { unmount } = render(<OrdersPage />);

    await user.click(screen.getByRole("button", { name: /load orders/i }));

    expect(await screen.findByText(/order-1/i)).toBeInTheDocument();

    expect(screen.getByRole("button", { name: /refresh/i })).toBeInTheDocument();

    unmount();
    render(<OrdersPage />);

    expect(screen.getByText(/order-1/i)).toBeInTheDocument();
    expect(getOrders).toHaveBeenCalledTimes(1);
  });

  it("updates cached orders when Refresh is clicked", async () => {
    const user = userEvent.setup();
    getOrders.mockResolvedValueOnce([firstOrder]).mockResolvedValueOnce([secondOrder]);

    render(<OrdersPage />);

    await user.click(screen.getByRole("button", { name: /load orders/i }));

    expect(await screen.findByText(/order-1/i)).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /refresh/i }));

    expect(await screen.findByText(/order-2/i)).toBeInTheDocument();

    expect(screen.queryByText(/order-1/i)).not.toBeInTheDocument();
  });
});