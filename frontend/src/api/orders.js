const BASE_URL = import.meta.env.VITE_API_URL + "/orders";

export async function createOrder(order) {

    const response = await fetch(BASE_URL, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(order)
    });

    await processError(response);

    return response.json();
}

export async function getOrders() {

    const response = await fetch(BASE_URL);
    await processError(response);
    return response.json();
}

export async function getOrderById(id) {

    const response = await fetch(`${BASE_URL}/${id}`);
    await processError(response);
    return response.json();
}

export async function updateOrder(id, updatedOrder) {

    const response = await fetch(`${BASE_URL}/${id}`, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(updatedOrder)
    });

    await processError(response);
    return response.json();
}

export async function cancelOrderById(id) {

    const response = await fetch(`${BASE_URL}/${id}/cancel`, { method: "PATCH" });
    await processError(response);
    return response.json();
}

async function processError(response) {
    if (!response.ok) {
        const errorData = await response.json();
        const error = new Error(errorData.message || "Request failed");
        error.status = response.status;

        throw error;
    }
}