import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  getOrderById,
  cancelOrderById,
  updateOrder,
} from "../../api/orders";
import OrderDetailsPage from "./OrderDetailsPage";
import { firstOrder, cancelledOrder } from "../../test/testOrders";

vi.mock("../../api/orders", () => ({
  getOrderById: vi.fn(),
  cancelOrderById: vi.fn(),
  updateOrder: vi.fn(),
}));

describe("OrderDetailsPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("looks up an order by id", async () => {
    const user = userEvent.setup();
    getOrderById.mockResolvedValue(firstOrder);

    render(<OrderDetailsPage />);

    await user.type(
      screen.getByPlaceholderText(/enter order id/i),
      " order-1 "
    );
    await user.click(screen.getByRole("button", { name: /get order/i }));

    expect(getOrderById).toHaveBeenCalledWith("order-1");
    expect(await screen.findByText(/order details/i)).toBeInTheDocument();
    expect(screen.getByText("user-1")).toBeInTheDocument();
    expect(screen.getByText("product-1")).toBeInTheDocument();
  });

  it("shows an error when request fails", async () => {
    const user = userEvent.setup();

    getOrderById.mockRejectedValue(new Error("Error happened"));

    render(<OrderDetailsPage />);

    await user.type(
      screen.getByPlaceholderText(/enter order id/i),
      "missing"
    );
    await user.click(screen.getByRole("button", { name: /get order/i }));

    expect(
      await screen.findByText(/Error happened/i)
    ).toBeInTheDocument();
  });

  it("shows a cancel button when a created order is found", async () => {
    const user = userEvent.setup();
    getOrderById.mockResolvedValue(firstOrder);

    render(<OrderDetailsPage />);

    await user.type(
      screen.getByPlaceholderText(/enter order id/i),
      " order-1 "
    );
    await user.click(screen.getByRole("button", { name: /get order/i }));

    expect(
      await screen.findByRole("button", { name: /cancel order/i })
    ).toBeInTheDocument();
  });

  it("does not show a cancel button when a cancelled order is found", async () => {
    const user = userEvent.setup();
    getOrderById.mockResolvedValue(cancelledOrder);

    render(<OrderDetailsPage />);

    await user.type(
      screen.getByPlaceholderText(/enter order id/i),
      " order-1 "
    );
    await user.click(screen.getByRole("button", { name: /get order/i }));

    expect(
      screen.queryByRole("button", { name: /cancel order/i })
    ).not.toBeInTheDocument();
  });

  it("shows loading state while searching for an order", async () => {
    const user = userEvent.setup();

    let resolvePromise;
    getOrderById.mockReturnValue(
      new Promise((resolve) => {
        resolvePromise = resolve;
      })
    );

    render(<OrderDetailsPage />);

    await user.type(
      screen.getByPlaceholderText(/enter order id/i),
      "order-1"
    );
    await user.click(screen.getByRole("button", { name: /get order/i }));

    expect(screen.getByText(/loading order/i)).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /searching/i })
    ).toBeDisabled();

    resolvePromise(firstOrder);

    await waitFor(() => {
      expect(
        screen.queryByText(/loading order/i)
      ).not.toBeInTheDocument();
    });

    expect(await screen.findByText(/order details/i)).toBeInTheDocument();
  });

  it("cancels an order successfully", async () => {
    const user = userEvent.setup();

    getOrderById.mockResolvedValue(firstOrder);

    cancelOrderById.mockResolvedValue({
      ...firstOrder,
      status: "CANCELLED",
    });

    render(<OrderDetailsPage />);

    await user.type(
      screen.getByPlaceholderText(/enter order id/i),
      "order-1"
    );

    await user.click(screen.getByRole("button", { name: /get order/i }));

    const cancelButton = await screen.findByRole(
      "button",
      { name: /cancel order/i }
    );

    await user.click(cancelButton);

    expect(cancelOrderById).toHaveBeenCalledWith("order-1");
    expect(await screen.findByText(/cancelled/i)).toBeInTheDocument();

    expect(
      screen.queryByRole("button", { name: /cancel order/i })
    ).not.toBeInTheDocument();
  });

  it("shows an error when cancelling the order fails", async () => {
    const user = userEvent.setup();

    getOrderById.mockResolvedValue(firstOrder);

    cancelOrderById.mockRejectedValue(new Error("cancel failed"));

    render(<OrderDetailsPage />);

    await user.type(
      screen.getByPlaceholderText(/enter order id/i),
      "order-1"
    );
    await user.click(screen.getByRole("button", { name: /get order/i }));

    await user.click(
      await screen.findByRole("button", { name: /cancel order/i })
    );

    expect(await screen.findByText(/cancel failed/i)).toBeInTheDocument();
  });

  it("clears the previous error after a successful search", async () => {
    const user = userEvent.setup();

    getOrderById
      .mockRejectedValueOnce(new Error("Not Found"))
      .mockResolvedValueOnce(firstOrder);

    render(<OrderDetailsPage />);

    const input = screen.getByPlaceholderText(/enter order id/i);

    await user.type(input, "missing");
    await user.click(screen.getByRole("button", { name: /get order/i }));

    expect(
      await screen.findByText(/Not Found/i)
    ).toBeInTheDocument();

    await user.clear(input);
    await user.type(input, "order-1");
    await user.click(screen.getByRole("button", { name: /get order/i }));

    expect(
      screen.queryByText(/Not Found/i)
    ).not.toBeInTheDocument();

    expect(await screen.findByText(/order details/i)).toBeInTheDocument();
  });

  it("renders created status styling correctly", async () => {
    const user = userEvent.setup();

    getOrderById.mockResolvedValue(firstOrder);

    render(<OrderDetailsPage />);

    await user.type(
      screen.getByPlaceholderText(/enter order id/i),
      "order-1"
    );
    await user.click(screen.getByRole("button", { name: /get order/i }));

    const badge = await screen.findByText(/created/i);

    expect(badge).toHaveClass("status-created");
    expect(badge).not.toHaveClass("status-cancelled");
  });

  it("updates an order successfully", async () => {
    const user = userEvent.setup();

    getOrderById.mockResolvedValue(firstOrder);
    updateOrder.mockResolvedValue({
      ...firstOrder,
      productId: "updated-product",
      quantity: 5,
    });

    render(<OrderDetailsPage />);

    await user.type(
      screen.getByPlaceholderText(/enter order id/i),
      "order-1"
    );
    await user.click(screen.getByRole("button", { name: /get order/i }));

    await user.click(
      await screen.findByRole("button", { name: /update order/i })
    );

    const productInput = screen.getByDisplayValue(firstOrder.productId);
    const quantityInput = screen.getByDisplayValue(
      String(firstOrder.quantity)
    );

    await user.clear(productInput);
    await user.type(productInput, "updated-product");

    await user.clear(quantityInput); // minimum is 1
    await user.type(quantityInput, "5");

    await user.click(screen.getByRole("button", { name: /save changes/i }));

    expect(updateOrder).toHaveBeenCalledWith("order-1", {
      productId: "updated-product",
      quantity: 15,
    });

    expect(
      await screen.findByText("updated-product")
    ).toBeInTheDocument();
  });

  it("disables save button when no changes are made", async () => {
    const user = userEvent.setup();

    getOrderById.mockResolvedValue(firstOrder);

    render(<OrderDetailsPage />);

    await user.type(
      screen.getByPlaceholderText(/enter order id/i),
      "order-1"
    );
    await user.click(screen.getByRole("button", { name: /get order/i }));

    await user.click(
      await screen.findByRole("button", { name: /update order/i })
    );

    const saveButton = screen.getByRole("button", {
      name: /save changes/i,
    });

    expect(saveButton).toBeDisabled();
  });

  it("covers handleUpdate catch block (update failure sets error)", async () => {
    const user = userEvent.setup();

    getOrderById.mockResolvedValue(firstOrder);
    updateOrder.mockRejectedValue(new Error("update failed"));

    render(<OrderDetailsPage />);

    await user.type(
      screen.getByPlaceholderText(/enter order id/i),
      "order-1"
    );
    await user.click(screen.getByRole("button", { name: /get order/i }));

    await user.click(
      await screen.findByRole("button", { name: /update order/i })
    );

    const productInput = screen.getByDisplayValue(firstOrder.productId);
    await user.clear(productInput);
    await user.type(productInput, "new-product");

    await user.click(
      screen.getByRole("button", { name: /save changes/i })
    );

    expect(await screen.findByText(/update failed/i)).toBeInTheDocument();
  });

  it("covers handleChange delete branch when value equals original order field", async () => {
    const user = userEvent.setup();

    getOrderById.mockResolvedValue(firstOrder);
    updateOrder.mockResolvedValue(firstOrder);

    render(<OrderDetailsPage />);

    await user.type(
      screen.getByPlaceholderText(/enter order id/i),
      "order-1"
    );
    await user.click(screen.getByRole("button", { name: /get order/i }));

    await user.click(
      await screen.findByRole("button", { name: /update order/i })
    );

    const productInput = screen.getByDisplayValue(firstOrder.productId);

    await user.clear(productInput);
    await user.type(productInput, "temp-value");

    await user.clear(productInput);
    await user.type(productInput, firstOrder.productId);

    const saveBtn = screen.getByRole("button", {
      name: /save changes/i,
    });

    expect(saveBtn).toBeDisabled();
  });

  it("covers discard button resetting editing state", async () => {
    const user = userEvent.setup();

    getOrderById.mockResolvedValue(firstOrder);

    render(<OrderDetailsPage />);

    await user.type(
      screen.getByPlaceholderText(/enter order id/i),
      "order-1"
    );
    await user.click(screen.getByRole("button", { name: /get order/i }));

    await user.click(
      await screen.findByRole("button", { name: /update order/i })
    );

    expect(
      screen.getByDisplayValue(firstOrder.productId)
    ).toBeInTheDocument();

    await user.click(
      screen.getByRole("button", { name: /discard/i })
    );

    expect(
      screen.queryByDisplayValue(firstOrder.productId)
    ).not.toBeInTheDocument();

    expect(
      screen.getByRole("button", { name: /update order/i })
    ).toBeInTheDocument();
  });

});