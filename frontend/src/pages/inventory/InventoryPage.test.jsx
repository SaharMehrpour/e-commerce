import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { AuthContext } from "../../auth/AuthProvider";
import { getInventory, updateStock } from "../../api/inventory";
import InventoryPage from "./InventoryPage";
import { resetInventoryPageCache } from "./inventoryPageCache";
import {
  firstInventoryItem,
  secondInventoryItem,
} from "../../test/testInventory";

vi.mock("../../api/inventory", () => ({
  getInventory: vi.fn(),
  updateStock: vi.fn(),
}));

const mockKeycloak = {
  token: "mock-token",
  tokenParsed: {
    preferred_username: "admin1",
    email: "admin@example.com",
  },
  realmAccess: {
    roles: ["ADMIN"],
  },
  updateToken: vi.fn().mockResolvedValue(true),
  logout: vi.fn(),
  login: vi.fn(),
};

function renderInventoryPage() {
  return render(
    <AuthContext.Provider
      value={{
        keycloak: mockKeycloak,
        authenticated: true,
      }}
    >
      <InventoryPage />
    </AuthContext.Provider>
  );
}

describe("InventoryPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    resetInventoryPageCache();
    mockKeycloak.updateToken.mockResolvedValue(true);
  });

  it("does not call the API until Load Inventory is clicked", () => {
    renderInventoryPage();

    expect(getInventory).not.toHaveBeenCalled();
    expect(
      screen.getByRole("button", { name: /load inventory/i })
    ).toBeInTheDocument();
  });

  it("loads inventory when clicked and keeps the latest loaded inventory after remount", async () => {
    const user = userEvent.setup();
    getInventory.mockResolvedValueOnce([firstInventoryItem]);

    const { unmount } = renderInventoryPage();

    await user.click(
      screen.getByRole("button", { name: /load inventory/i })
    );

    expect(await screen.findByText("product-1")).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /refresh/i })
    ).toBeInTheDocument();

    unmount();
    renderInventoryPage();

    expect(screen.getByText("product-1")).toBeInTheDocument();
    expect(getInventory).toHaveBeenCalledTimes(1);
  });

  it("updates cached inventory when Refresh is clicked", async () => {
    const user = userEvent.setup();

    getInventory
      .mockResolvedValueOnce([firstInventoryItem])
      .mockResolvedValueOnce([secondInventoryItem]);

    renderInventoryPage();

    await user.click(
      screen.getByRole("button", { name: /load inventory/i })
    );

    expect(await screen.findByText("product-1")).toBeInTheDocument();

    await user.click(
      screen.getByRole("button", { name: /refresh/i })
    );

    expect(await screen.findByText("product-2")).toBeInTheDocument();
    expect(screen.queryByText("product-1")).not.toBeInTheDocument();
  });

  it("displays an error message if loading inventory fails", async () => {
    const user = userEvent.setup();
    getInventory.mockRejectedValueOnce(new Error("API error"));

    renderInventoryPage();

    await user.click(
      screen.getByRole("button", { name: /load inventory/i })
    );

    expect(await screen.findByText(/API error/i)).toBeInTheDocument();
  });

  it("displays a message when there are no inventory items", async () => {
    const user = userEvent.setup();
    getInventory.mockResolvedValueOnce([]);

    renderInventoryPage();

    await user.click(
      screen.getByRole("button", { name: /load inventory/i })
    );

    expect(
      await screen.findByText(/no inventory items yet/i)
    ).toBeInTheDocument();
  });

  it("copies the product ID to the clipboard", async () => {
    const user = userEvent.setup();
    getInventory.mockResolvedValueOnce([firstInventoryItem]);

    const writeTextMock = vi.fn();

    Object.defineProperty(navigator, "clipboard", {
      configurable: true,
      value: { writeText: writeTextMock },
    });

    renderInventoryPage();

    await user.click(
      screen.getByRole("button", { name: /load inventory/i })
    );

    await user.click(
      await screen.findByRole("button", { name: /copy product id/i })
    );

    expect(writeTextMock).toHaveBeenCalledWith("product-1");
  });

  it("adds a new inventory item and refreshes the inventory", async () => {
    const user = userEvent.setup();

    const updatedItem = {
      ...firstInventoryItem,
      productId: "product-3",
      availableQuantity: 5,
    };

    getInventory
      .mockResolvedValueOnce([firstInventoryItem])
      .mockResolvedValueOnce([firstInventoryItem, updatedItem]);

    updateStock.mockResolvedValueOnce(updatedItem);

    renderInventoryPage();

    await user.click(
      screen.getByRole("button", { name: /load inventory/i })
    );

    await user.type(
      await screen.findByLabelText("Product ID"),
      "product-3"
    );

    await user.type(
      screen.getByLabelText("Initial Quantity"),
      "5"
    );

    await user.click(
      screen.getByRole("button", { name: /add item/i })
    );

    await waitFor(() => {
      expect(updateStock).toHaveBeenCalledTimes(1);
      expect(updateStock.mock.calls[0].slice(1)).toEqual([
        "product-3",
        5,
      ]);
    });

    expect(
      await screen.findByText(/inventory item .* successfully/i)
    ).toBeInTheDocument();

    await waitFor(() => {
      expect(getInventory).toHaveBeenCalledTimes(2);
    });

    expect(screen.getByText("product-1")).toBeInTheDocument();
    expect(screen.getByText("product-3")).toBeInTheDocument();
  });

  it("updates an existing item's available quantity and refreshes inventory", async () => {
    const user = userEvent.setup();

    const updatedItem = {
      ...firstInventoryItem,
      availableQuantity: 8,
    };

    getInventory
      .mockResolvedValueOnce([firstInventoryItem])
      .mockResolvedValueOnce([updatedItem]);

    updateStock.mockResolvedValueOnce(updatedItem);

    renderInventoryPage();

    await user.click(
      screen.getByRole("button", { name: /load inventory/i })
    );

    await user.click(
      await screen.findByRole("button", { name: /edit quantity/i })
    );

    const quantityInput = screen.getByRole("spinbutton", {
      name: /new available quantity/i,
    });

    await user.clear(quantityInput);
    await user.type(quantityInput, "8");

    await user.click(screen.getByRole("button", { name: /save/i }));

    await waitFor(() => {
      expect(updateStock).toHaveBeenCalledTimes(1);
      expect(updateStock.mock.calls[0].slice(1)).toEqual([
        "product-1",
        8,
      ]);
    });

    expect(
      await screen.findByText("Quantity updated successfully.")
    ).toBeInTheDocument();

    await waitFor(() => {
      expect(getInventory).toHaveBeenCalledTimes(2);
    });
  });

  it("allows canceling an edit without calling the update API", async () => {
    const user = userEvent.setup();
    getInventory.mockResolvedValueOnce([firstInventoryItem]);

    renderInventoryPage();

    await user.click(
      screen.getByRole("button", { name: /load inventory/i })
    );

    await user.click(
      await screen.findByRole("button", { name: /edit quantity/i })
    );

    expect(
      screen.getByRole("spinbutton", {
        name: /new available quantity/i,
      })
    ).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /cancel/i }));

    expect(
      screen.queryByRole("spinbutton", {
        name: /new available quantity/i,
      })
    ).not.toBeInTheDocument();

    expect(updateStock).not.toHaveBeenCalled();
  });

  it("rejects an invalid quantity when adding an item", async () => {
    const user = userEvent.setup();
    getInventory.mockResolvedValueOnce([firstInventoryItem]);

    renderInventoryPage();

    await user.click(
      screen.getByRole("button", { name: /load inventory/i })
    );

    await user.type(
      await screen.findByLabelText("Product ID"),
      "product-3"
    );

    await user.type(
      screen.getByLabelText("Initial Quantity"),
      "0"
    );

    // Bypass browser validation to test the component's own validation.
    screen
      .getByLabelText("Initial Quantity")
      .closest("form")
      .setAttribute("novalidate", "");

    await user.click(
      screen.getByRole("button", { name: /add item/i })
    );

    expect(
      await screen.findByText(/whole-number quantity/i)
    ).toBeInTheDocument();

    expect(updateStock).not.toHaveBeenCalled();
  });

  it("displays an error when updating an item's quantity fails", async () => {
    const user = userEvent.setup();

    getInventory.mockResolvedValueOnce([firstInventoryItem]);
    updateStock.mockRejectedValueOnce(new Error("Update failed"));

    renderInventoryPage();

    await user.click(
      screen.getByRole("button", { name: /load inventory/i })
    );

    await user.click(
      await screen.findByRole("button", { name: /edit quantity/i })
    );

    const quantityInput = screen.getByRole("spinbutton", {
      name: /new available quantity/i,
    });

    await user.clear(quantityInput);
    await user.type(quantityInput, "8");

    await user.click(screen.getByRole("button", { name: /save/i }));

    expect(await screen.findByText("Update failed")).toBeInTheDocument();
  });
});
