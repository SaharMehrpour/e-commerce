import { render, screen, waitFor } from "@testing-library/react";
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

  it("shows loading spinner while fetching orders and stops after loading", async () => {
    const user = userEvent.setup();

    let resolvePromise;
    getOrders.mockReturnValue(
      new Promise((resolve) => {
        resolvePromise = resolve;
      }),
    );

    render(<OrdersPage />);

    await user.click(
      screen.getByRole("button", { name: /load orders|Refresh/i }),
    );

    expect(screen.getByRole("status", { name: /loading orders/i })).toBeInTheDocument();
    resolvePromise([]);
    await waitFor(() => {
      expect(screen.queryByRole("status", { name: /loading orders/i })).not.toBeInTheDocument();
    });
  });

  it("shows an error message when loading orders fails", async () => {
    const user = userEvent.setup();

    getOrders.mockRejectedValueOnce(new Error("API failed"));

    render(<OrdersPage />);

    await user.click(screen.getByRole("button", { name: /load orders|Refresh/i }));

    expect(
      await screen.findByText(/could not load orders\. check that the backend is running\./i),
    ).toBeInTheDocument();

    expect(screen.queryByText(/loading orders/i)).not.toBeInTheDocument();
  });

  it("disables the button while loading orders", async () => {
    const user = userEvent.setup();

    let resolvePromise;

    getOrders.mockReturnValue(
      new Promise((resolve) => {
        resolvePromise = resolve;
      }),
    );

    render(<OrdersPage />);

    const button = screen.getByRole("button", { name: /load orders/i });

    await user.click(button);

    expect(button).toBeDisabled();

    resolvePromise([]);

    await waitFor(() => {
      expect(button).not.toBeDisabled();
    });
  });

  it("copies the order id to clipboard", async () => {
    const user = userEvent.setup();

    getOrders.mockResolvedValueOnce([firstOrder]);

    const writeTextMock = vi.fn();
    Object.defineProperty(navigator, "clipboard", {
      value: {
        writeText: writeTextMock,
      },
      writable: true,
    });

    render(<OrdersPage />);

    await user.click(
      screen.getByRole("button", { name: /load orders/i }),
    );

    expect(await screen.findByText(/order-1/i)).toBeInTheDocument();

    const copyButton = screen.getByRole("button", { name: "" });

    await user.click(copyButton);

    expect(writeTextMock).toHaveBeenCalledWith("order-1");
  });

  it("shows the empty state when API returns no orders", async () => {
    const user = userEvent.setup();

    getOrders.mockResolvedValueOnce([]);

    render(<OrdersPage />);

    await user.click(
      screen.getByRole("button", { name: /load orders/i }),
    );

    expect(
      await screen.findByRole("status", { name: /no orders yet/i }),
    ).toBeInTheDocument();
  });

  it("renders cancelled status styling correctly", async () => {
    const user = userEvent.setup();

    const cancelledOrder = {
      ...firstOrder,
      status: "CANCELLED",
    };

    getOrders.mockResolvedValueOnce([cancelledOrder]);

    render(<OrdersPage />);

    await user.click(screen.getByRole("button", { name: /load orders/i }));

    const badge = await screen.findByText(/cancelled/i);

    expect(badge).toHaveClass("status-cancelled");
    expect(badge).not.toHaveClass("status-created");
  });

});