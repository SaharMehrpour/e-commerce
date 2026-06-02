import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { getInventory } from "../../api/inventory";
import InventoryPage from "./InventoryPage";
import { getInventoryPageCache, setInventoryPageCache, resetInventoryPageCache } from "./inventoryPageCache";
import { firstInventoryItem, secondInventoryItem } from "../../test/testInventory";

vi.mock("../../api/inventory", () => ({
  getInventory: vi.fn(),
}));

describe("InventoryPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    resetInventoryPageCache();
  });

    it("does not call the API until Load Inventory is clicked", () => {
    render(<InventoryPage />);
    
    expect(getInventory).not.toHaveBeenCalled();
    expect(screen.getByText(/click load inventory/i)).toBeInTheDocument();
  });
  
  it("loads inventory when clicked and keeps the latest loaded inventory after remount", async () => {
    const user = userEvent.setup();
    getInventory.mockResolvedValueOnce([firstInventoryItem]);
    const { unmount } = render(<InventoryPage />);
    
    await user.click(screen.getByRole("button", { name: /load inventory/i }));  
    expect(await screen.findByText(/product-1/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /refresh/i })).toBeInTheDocument();
    
    unmount();
    render(<InventoryPage />);
    
    expect(screen.getByText(/product-1/i)).toBeInTheDocument();
    expect(getInventory).toHaveBeenCalledTimes(1);
  });
  
  it("updates cached inventory when Refresh is clicked", async () => {
    const user = userEvent.setup();
    getInventory.mockResolvedValueOnce([firstInventoryItem]).mockResolvedValueOnce([secondInventoryItem]);
    
    render(<InventoryPage />);
    
    await user.click(screen.getByRole("button", { name: /load inventory/i }));
    expect(await screen.findByText(/product-1/i)).toBeInTheDocument();
    
    await user.click(screen.getByRole("button", { name: /refresh/i }));
    expect(await screen.findByText(/product-2/i)).toBeInTheDocument();
    
    expect(screen.queryByText(/product-1/i)).not.toBeInTheDocument();
  });

  it("displays an error message if the API call fails", async () => {
    const user = userEvent.setup();
    getInventory.mockRejectedValueOnce(new Error("API error"));
    
    render(<InventoryPage />);
    
    await user.click(screen.getByRole("button", { name: /load inventory/i }));
    
    expect(await screen.findByText(/API error/i)).toBeInTheDocument();
  });

  it("displays a message when there are no inventory items", async () => {
    const user = userEvent.setup();
    getInventory.mockResolvedValueOnce([]);
    
    render(<InventoryPage />);
    
    await user.click(screen.getByRole("button", { name: /load inventory/i }));
    
    expect(await screen.findByText(/no inventory items yet/i)).toBeInTheDocument();
  });

  it("copies the product id to clipboard", async () => {
    const user = userEvent.setup();
    getInventory.mockResolvedValueOnce([firstInventoryItem]);

    const writeTextMock = vi.fn();
    Object.defineProperty(navigator, "clipboard", {
      value: {
        writeText: writeTextMock,
      },
      writable: true,
    });
    
    render(<InventoryPage />);
    
    await user.click(screen.getByRole("button", { name: /load inventory/i }));
    
    const copyButton = await screen.findByRole("button", { name: "" });
    await user.click(copyButton);
    
    expect(writeTextMock).toHaveBeenCalledWith("product-1");
  });

});