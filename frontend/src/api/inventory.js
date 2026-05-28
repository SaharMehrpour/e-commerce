const BASE_URL = "http://localhost:8080/inventory";

export async function getInventoryItemById(productId) {

    const response = await fetch(`${BASE_URL}/${productId}`);

    if (!response.ok) {
        const error = new Error("Request failed");
        error.status = response.status;
        throw error;
    }

    return response.json();
}

export async function updateInventoryItem(productId, inventoryItemData) {

    const response = await fetch(`${BASE_URL}/${productId}`, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(inventoryItemData),
    });

    if (!response.ok) {
        throw new Error("Failed to update inventory item");
    }

    return response.json();
}

export async function getInventory() {

    const response = await fetch(BASE_URL);

    if (!response.ok) {
        const error = new Error("Request failed");
        error.status = response.status;
        throw error;
    }

    return response.json();
}