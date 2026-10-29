import { getApiUrl } from "./config";

const BASE_URL = `${getApiUrl()}/orders`;

export async function createOrder(order, keycloak) {
    await keycloak.updateToken(30);

    const response = await fetch(BASE_URL, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${keycloak.token}`
        },
        body: JSON.stringify(order)
    });

    await processError(response);

    return response.json();
}

export async function getOrders(keycloak) {
    if (!keycloak?.authenticated || !keycloak?.token) {
        throw new Error("You must be logged in to view orders.");
    }

    await keycloak.updateToken(30);

    const response = await fetch(BASE_URL, {
        headers: {
            "Authorization": `Bearer ${keycloak.token}`
        }
    });

    await processError(response);
    return response.json();
}

export async function getOrderById(id, keycloak) {

    const response = await fetch(`${BASE_URL}/${id}`, {
        headers: {
            "Authorization": `Bearer ${keycloak.token}`
        }
    });
    await processError(response);
    return response.json();
}

export async function updateOrder(id, updatedOrder, keycloak) {

    const response = await fetch(`${BASE_URL}/${id}`, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${keycloak.token}`
        },
        body: JSON.stringify(updatedOrder)
    });

    await processError(response);
    return response.json();
}

export async function cancelOrderById(id, keycloak) {

    const response = await fetch(`${BASE_URL}/${id}/cancel`, {
        method: "PATCH",
        headers: {
            "Authorization": `Bearer ${keycloak.token}`
        }
    });
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
