import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { getInventoryItemById, updateInventoryItem } from "../../api/inventory";
import InventoryDetailsPage from "./InventoryDetailsPage";
import { firstInventoryItem } from "../../test/testInventory";

vi.mock("../../api/inventory", () => ({
  getInventoryItemById: vi.fn(),
  updateInventoryItem: vi.fn(),
}));

describe("InventoryDetailsPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

    it("displays inventory item details when a valid ID is entered", async () => {
        const user = userEvent.setup();
        getInventoryItemById.mockResolvedValueOnce(firstInventoryItem);

        render(<InventoryDetailsPage />);

        await user.type(
            screen.getByPlaceholderText(/enter product id/i),
            " product-1 "
        );
        await user.click(screen.getByRole("button", { name: /get inventory item/i }));   

        expect(getInventoryItemById).toHaveBeenCalledWith("product-1");
        expect(await screen.findByText(/inventory item id/i)).toBeInTheDocument();
        expect(screen.getByText("product-1")).toBeInTheDocument();
        expect(screen.getByText(/available quantity/i)).toBeInTheDocument();
        expect(screen.getByText(/reserved quantity/i)).toBeInTheDocument();
    });

    it("displays an error message when an invalid ID is entered", async () => {
        const user = userEvent.setup();
        getInventoryItemById.mockRejectedValue(
            Object.assign(new Error("Not Found"), { status: 404 })
        );

        render(<InventoryDetailsPage />);

        await user.type(
            screen.getByPlaceholderText(/enter product id/i),
            "missing"
        );
        await user.click(screen.getByRole("button", { name: /get inventory item/i }));

        expect(
            await screen.findByText(/no inventory item found for that id/i)
        ).toBeInTheDocument();
    });

    it("displays a generic error message when the backend is unreachable", async () => {
        const user = userEvent.setup();
        getInventoryItemById.mockRejectedValue(
            Object.assign(new Error("Not Connected"), { status: 500 })
        );

        render(<InventoryDetailsPage />);

        await user.type(
            screen.getByPlaceholderText(/enter product id/i),
            "item123"
        );
        await user.click(screen.getByRole("button", { name: /get inventory item/i }));

        await waitFor(() => {
            expect(getInventoryItemById).toHaveBeenCalledWith("item123");
        });

        expect(screen.getByText(/Backend is not reachable. Please try again later./i)).toBeInTheDocument();
    });

    it("enables editing mode and updates quantity fields", async () => {
        const user = userEvent.setup();

        getInventoryItemById.mockResolvedValueOnce(firstInventoryItem);

        render(<InventoryDetailsPage />);

        await user.type(
            screen.getByPlaceholderText(/enter product id/i),
            "product-1"
        );

        await user.click(screen.getByRole("button", { name: /get inventory item/i }));

        expect(
            await screen.findByRole("button", { name: /update inventory item/i })
        ).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: /update inventory item/i }));

        const availableQuantityInput = screen.getByDisplayValue(
            firstInventoryItem.availableQuantity.toString()
        );

        await user.clear(availableQuantityInput); // minimum is 1
        await user.type(availableQuantityInput, "5");

        expect(screen.getByDisplayValue("15")).toBeInTheDocument();

        expect(screen.getByRole("button", { name: /save changes/i })).toBeEnabled();
    });

    it("disables save changes button when no fields are modified", async () => {
        const user = userEvent.setup();

        getInventoryItemById.mockResolvedValueOnce(firstInventoryItem);

        render(<InventoryDetailsPage />);

        await user.type(
            screen.getByPlaceholderText(/enter product id/i),
            "product-1"
        );

        await user.click(screen.getByRole("button", { name: /get inventory item/i }));
        await user.click(screen.getByRole("button", { name: /update inventory item/i }));

        expect(screen.getByRole("button", { name: /save changes/i })).toBeDisabled();
    });

    it("discards changes and exits editing mode", async () => {
        const user = userEvent.setup();

        getInventoryItemById.mockResolvedValueOnce(firstInventoryItem);

        render(<InventoryDetailsPage />);

        await user.type(
            screen.getByPlaceholderText(/enter product id/i),
            "product-1"
        );

        await user.click(screen.getByRole("button", { name: /get inventory item/i }));
        await user.click(screen.getByRole("button", { name: /update inventory item/i }));

        const availableQuantityInput = screen.getByDisplayValue(
            firstInventoryItem.availableQuantity.toString()
        );

        await user.clear(availableQuantityInput);
        await user.type(availableQuantityInput, "99");
        await user.click(screen.getByRole("button", { name: /discard/i }));

        expect(screen.queryByRole("button", { name: /save changes/i })).not.toBeInTheDocument();
        expect(screen.getByRole("button", { name: /update inventory item/i })).toBeInTheDocument();
        expect(screen.getByText(firstInventoryItem.availableQuantity.toString())).toBeInTheDocument();
    });

    it("shows loading state while searching for inventory item", async () => {
        const user = userEvent.setup();

        getInventoryItemById.mockImplementation(
            () =>
                new Promise((resolve) =>
                    setTimeout(() => resolve(firstInventoryItem), 100)
                )
        );

        render(<InventoryDetailsPage />);

        await user.type(
            screen.getByPlaceholderText(/enter product id/i),
            "product-1"
        );

        await user.click(screen.getByRole("button", { name: /get inventory item/i }));

        expect(screen.getByText(/loading inventoryitem/i)).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /searching/i })).toBeDisabled();
        expect(await screen.findByText(/inventory item id/i)).toBeInTheDocument();
    });

    it("clears previous error after a successful search", async () => {
        const user = userEvent.setup();

        getInventoryItemById
            .mockRejectedValueOnce(
                Object.assign(new Error("Not Found"), { status: 404 })
            )
            .mockResolvedValueOnce(firstInventoryItem);

        render(<InventoryDetailsPage />);

        const input = screen.getByPlaceholderText(/enter product id/i);

        await user.type(input, "missing");
        await user.click(screen.getByRole("button", { name: /get inventory item/i }));

        expect(
            await screen.findByText(/no inventory item found for that id/i)
        ).toBeInTheDocument();

        await user.clear(input);
        await user.type(input, "product-1");
        await user.click(screen.getByRole("button", { name: /get inventory item/i }));

        expect(await screen.findByText(/inventory item id/i)).toBeInTheDocument();
        expect(
            screen.queryByText(/no inventory item found for that id/i)
        ).not.toBeInTheDocument();
    });

    it("updates inventory item successfully", async () => {
        const user = userEvent.setup();

        const updatedInventoryItem = {
            ...firstInventoryItem,
            availableQuantity: 15,
        };

        getInventoryItemById.mockResolvedValueOnce(firstInventoryItem);
        updateInventoryItem.mockResolvedValueOnce(updatedInventoryItem);

        render(<InventoryDetailsPage />);

        await user.type(
            screen.getByPlaceholderText(/enter product id/i),
            "product-1"
        );

        await user.click(screen.getByRole("button", { name: /get inventory item/i }));
        await user.click(screen.getByRole("button", { name: /update inventory item/i }));

        const availableQuantityInput = screen.getByDisplayValue(
            firstInventoryItem.availableQuantity.toString()
        );

        fireEvent.change(availableQuantityInput, { target: { value: "15" }});

        await user.click(screen.getByRole("button", { name: /save changes/i }));
        await waitFor(() => {
            expect(updateInventoryItem).toHaveBeenCalledWith("product-1", { availableQuantity: 15 });
        });

        expect(screen.getByRole("button", { name: /update inventory item/i })).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: /save changes/i })).not.toBeInTheDocument();
        expect(screen.getByText("15")).toBeInTheDocument();
    });

    it("logs error and shows message when update fails", async () => {
        const user = userEvent.setup();

        const errorSpy = vi.spyOn(console, "error").mockImplementation(() => {});

        const updateError = new Error("Update failed");

        getInventoryItemById.mockResolvedValueOnce(firstInventoryItem);
        updateInventoryItem.mockRejectedValueOnce(updateError);

        render(<InventoryDetailsPage />);

        await user.type(
            screen.getByPlaceholderText(/enter product id/i),
            "product-1"
        );

        await user.click(screen.getByRole("button", { name: /get inventory item/i }));

        await user.click(screen.getByRole("button", { name: /update inventory item/i }));

        const availableQuantityInput = screen.getByDisplayValue(
            firstInventoryItem.availableQuantity.toString()
        );

        fireEvent.change(availableQuantityInput, { target: { value: "15" } });

        await user.click(screen.getByRole("button", { name: /save changes/i }));

        await waitFor(() => {
            expect(errorSpy).toHaveBeenCalledWith(
                "Failed to update inventory item",
                { err: updateError }
            );
        });

        expect(await screen.findByText(/failed to update the inventory item/i)).toBeInTheDocument();
        errorSpy.mockRestore();
    });

    it("removes field from validated changes when value matches original value again", async () => {
        const user = userEvent.setup();

        getInventoryItemById.mockResolvedValueOnce(firstInventoryItem);

        render(<InventoryDetailsPage />);

        await user.type(screen.getByPlaceholderText(/enter product id/i), "product-1");
        await user.click(screen.getByRole("button", { name: /get inventory item/i }));
        await user.click(screen.getByRole("button", { name: /update inventory item/i }));

        const availableQuantityInput = screen.getByDisplayValue(
            firstInventoryItem.availableQuantity.toString()
        );

        fireEvent.change(availableQuantityInput, { target: { value: "15" } });

        expect(screen.getByRole("button", { name: /save changes/i })).toBeEnabled();

        fireEvent.change(availableQuantityInput, {
            target: { value: firstInventoryItem.availableQuantity.toString()},
        });

        expect(screen.getByRole("button", { name: /save changes/i })).toBeDisabled();
    });

});