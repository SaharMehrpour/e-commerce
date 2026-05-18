const BASE_URL = "http://localhost:8080/orders";

export async function createOrder(order) {

    const response = await fetch(BASE_URL, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(order)
    });

    return response.json();
}

export async function getOrders() {

    const response = await fetch(BASE_URL);

    return response.json();
}

export async function getOrderById(id) {

    const response = await fetch(`${BASE_URL}/${id}`);

    if (!response.ok) {
        throw new Error("Order not found");
    }

    return response.json();
}
