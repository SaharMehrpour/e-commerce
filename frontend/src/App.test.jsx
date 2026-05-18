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
  it("starts on the home page and navigates to the order pages", async () => {
    const user = userEvent.setup();

    render(<App />);

    expect(screen.getByText("Order Management Demo")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Give Order" }));
    expect(screen.getByRole("heading", { name: "Give An Order" })).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "All Orders" }));
    expect(screen.getByRole("heading", { name: "All Orders" })).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Order Info" }));
    expect(screen.getByRole("heading", { name: "Order By ID" })).toBeInTheDocument();
  });
});
