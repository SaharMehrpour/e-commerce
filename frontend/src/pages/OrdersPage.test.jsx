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
    expect(screen.getByText("Click Load Orders to fetch orders from the API.")).toBeInTheDocument();
  });

  it("loads orders when clicked and keeps the latest loaded orders after remount", async () => {
    const user = userEvent.setup();
    getOrders.mockResolvedValueOnce([firstOrder]);

    const { unmount } = render(<OrdersPage />);

    await user.click(screen.getByRole("button", { name: "Load Orders" }));

    expect(await screen.findByText("ID: order-1")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Refresh" })).toBeInTheDocument();

    unmount();
    render(<OrdersPage />);

    expect(screen.getByText("ID: order-1")).toBeInTheDocument();
    expect(getOrders).toHaveBeenCalledTimes(1);
  });

  it("updates cached orders when Refresh is clicked", async () => {
    const user = userEvent.setup();
    getOrders.mockResolvedValueOnce([firstOrder]).mockResolvedValueOnce([secondOrder]);

    render(<OrdersPage />);

    await user.click(screen.getByRole("button", { name: "Load Orders" }));
    expect(await screen.findByText("ID: order-1")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Refresh" }));

    expect(await screen.findByText("ID: order-2")).toBeInTheDocument();
    expect(screen.queryByText("ID: order-1")).not.toBeInTheDocument();
  });
});
