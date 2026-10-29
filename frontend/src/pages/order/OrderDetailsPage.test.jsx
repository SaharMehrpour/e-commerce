import { act, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { getOrders } from "../../api/orders";
import { AuthContext } from "../../auth/AuthContext";
import OrdersPage from "./OrdersPage";
import {
  getOrdersPageCache,
  resetOrdersPageCache,
} from "./ordersPageCache";

vi.mock("../../api/orders", () => ({
  getOrders: vi.fn(),
}));

const mockKeycloak = {
  token: "mock-token",
  authenticated: true,
  tokenParsed: { sub: "test-user-1" },
  updateToken: vi.fn().mockResolvedValue(false),
};

const firstOrder = {
  id: "order-1",
  userId: "test-user-1",
  productId: "product-1",
  quantity: 2,
  status: "CREATED",
};

const secondOrder = {
  id: "order-2",
  userId: "test-user-1",
  productId: "product-2",
  quantity: 3,
  status: "CREATED",
};

const cancelledOrder = {
  id: "order-3",
  userId: "test-user-1",
  productId: "product-3",
  quantity: 1,
  status: "CANCELLED",
};

function renderOrdersPage() {
  return render(
    <AuthContext.Provider
      value={{
        keycloak: mockKeycloak,
        authenticated: true,
      }}
    >
      <OrdersPage />
    </AuthContext.Provider>
  );
}

describe("OrdersPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    resetOrdersPageCache();
    mockKeycloak.updateToken.mockResolvedValue(false);
  });

  it("does not call the API until Load Orders is clicked", () => {
    renderOrdersPage();

    expect(getOrders).not.toHaveBeenCalled();

    expect(
      screen.getByText("Click Load Orders to fetch orders from the API.")
    ).toBeInTheDocument();
  });

  it("loads orders when clicked and keeps the latest loaded orders after remount", async () => {
    const user = userEvent.setup();

    getOrders.mockResolvedValueOnce([firstOrder]);

    const { unmount } = renderOrdersPage();

    await user.click(
      screen.getByRole("button", { name: /load orders/i })
    );

    expect(await screen.findByText(firstOrder.id)).toBeInTheDocument();

    expect(getOrders).toHaveBeenCalledTimes(1);
    expect(getOrders).toHaveBeenCalledWith(mockKeycloak);
    expect(mockKeycloak.updateToken).toHaveBeenCalledWith(30);

    await waitFor(() => {
      expect(getOrdersPageCache("test-user-1")).toEqual({
        orders: [firstOrder],
        hasLoaded: true,
      });
    });

    unmount();

    renderOrdersPage();

    expect(screen.getByText(firstOrder.id)).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /refresh/i })
    ).toBeInTheDocument();

    // The cached orders are restored without another API request.
    expect(getOrders).toHaveBeenCalledTimes(1);
  });

  it("updates cached orders when Refresh is clicked", async () => {
    const user = userEvent.setup();

    getOrders
      .mockResolvedValueOnce([firstOrder])
      .mockResolvedValueOnce([secondOrder]);

    const { unmount } = renderOrdersPage();

    await user.click(
      screen.getByRole("button", { name: /load orders/i })
    );

    expect(await screen.findByText(firstOrder.id)).toBeInTheDocument();

    await waitFor(() => {
      expect(getOrdersPageCache("test-user-1").orders).toEqual([
        firstOrder,
      ]);
    });

    await user.click(
      screen.getByRole("button", { name: /refresh/i })
    );

    expect(await screen.findByText(secondOrder.id)).toBeInTheDocument();

    await waitFor(() => {
      expect(getOrdersPageCache("test-user-1")).toEqual({
        orders: [secondOrder],
        hasLoaded: true,
      });
      expect(screen.queryByText(firstOrder.id)).not.toBeInTheDocument();
    });

    unmount();

    renderOrdersPage();

    expect(screen.getByText(secondOrder.id)).toBeInTheDocument();
    expect(screen.queryByText(firstOrder.id)).not.toBeInTheDocument();
    expect(getOrders).toHaveBeenCalledTimes(2);
  });

  it("shows loading spinner while fetching orders and stops after loading", async () => {
    const user = userEvent.setup();

    let resolveOrders;

    getOrders.mockReturnValue(
      new Promise((resolve) => {
        resolveOrders = resolve;
      })
    );

    renderOrdersPage();

    await user.click(
      screen.getByRole("button", { name: /load orders/i })
    );

    // This is the specifically labelled loading status in the component.
    expect(
      screen.getByRole("status", { name: "Loading Orders" })
    ).toBeInTheDocument();

    expect(screen.getAllByText("Loading orders...")).toHaveLength(2);

    await act(async () => {
      resolveOrders([firstOrder]);
    });

    expect(await screen.findByText(firstOrder.id)).toBeInTheDocument();

    await waitFor(() => {
      expect(
        screen.queryByRole("status", { name: "Loading Orders" })
      ).not.toBeInTheDocument();
    });
  });

  it("shows an error message when loading orders fails", async () => {
    const user = userEvent.setup();

    getOrders.mockRejectedValueOnce(new Error("API failed"));

    renderOrdersPage();

    await user.click(
      screen.getByRole("button", { name: /load orders/i })
    );

    expect(await screen.findByText("API failed")).toBeInTheDocument();
    expect(getOrders).toHaveBeenCalledWith(mockKeycloak);
  });

  it("disables the button while loading orders", async () => {
    const user = userEvent.setup();

    let resolveOrders;

    getOrders.mockReturnValue(
      new Promise((resolve) => {
        resolveOrders = resolve;
      })
    );

    renderOrdersPage();

    const loadButton = screen.getByRole("button", {
      name: /load orders/i,
    });

    await user.click(loadButton);

    // The same button remains present but changes its accessible name.
    const loadingButton = screen.getByRole("button", {
      name: /loading/i,
    });

    expect(loadingButton).toBeDisabled();

    await act(async () => {
      resolveOrders([firstOrder]);
    });

    expect(await screen.findByText(firstOrder.id)).toBeInTheDocument();

    await waitFor(() => {
      const refreshButton = screen.getByRole("button", {
        name: /refresh/i,
      });

      expect(refreshButton).toBeEnabled();
    });
  });

  it("copies the order id to clipboard", async () => {
    const user = userEvent.setup();
    const writeText = vi.fn().mockResolvedValue(undefined);

    Object.defineProperty(navigator, "clipboard", {
      configurable: true,
      value: { writeText },
    });

    getOrders.mockResolvedValueOnce([firstOrder]);

    renderOrdersPage();

    await user.click(
      screen.getByRole("button", { name: /load orders/i })
    );

    const orderId = await screen.findByText(firstOrder.id);
    const orderRow = orderId.closest("tr");

    expect(orderRow).not.toBeNull();

    const copyButton = orderRow.querySelector("button");

    expect(copyButton).not.toBeNull();

    await user.click(copyButton);

    expect(writeText).toHaveBeenCalledWith(firstOrder.id);
  });

  it("shows the empty state when API returns no orders", async () => {
    const user = userEvent.setup();

    getOrders.mockResolvedValueOnce([]);

    renderOrdersPage();

    await user.click(
      screen.getByRole("button", { name: /load orders/i })
    );

    expect(
      await screen.findByRole("status", { name: "No orders yet" })
    ).toHaveTextContent("No orders yet.");

    expect(getOrders).toHaveBeenCalledWith(mockKeycloak);
  });

  it("renders cancelled status styling correctly", async () => {
    const user = userEvent.setup();

    getOrders.mockResolvedValueOnce([cancelledOrder]);

    renderOrdersPage();

    await user.click(
      screen.getByRole("button", { name: /load orders/i })
    );

    expect(
      await screen.findByText(cancelledOrder.id)
    ).toBeInTheDocument();

    const status = screen.getByText("CANCELLED");

    expect(status).toHaveClass("status-cancelled");
  });

});