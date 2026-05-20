import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import App from "./App";

vi.mock("./api/orders", () => ({
  createOrder: vi.fn(),
  getOrderById: vi.fn(),
  getOrders: vi.fn(),
}));

describe("App", () => {
  it("starts on home page and navigates between pages", async () => {
    const user = userEvent.setup();

    render(<App />);

    expect(screen.getByText(/order management demo/i)).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /give order/i }));

    expect(screen.getByRole("heading", { name: /create order/i })).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /all orders/i }));

    expect(screen.getByRole("heading", { name: /all orders/i })).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /order info/i }));

    expect(screen.getByRole("heading", { name: /order lookup/i })).toBeInTheDocument();
  });
});