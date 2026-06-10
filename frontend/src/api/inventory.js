const BASE_URL = import.meta.env.VITE_INVENTORY_API_URL + "/inventory";

export async function getInventory() {

    const response = await fetch(BASE_URL);

    if (!response.ok) {
        const errorData = await response.json();
        const error = new Error(errorData.message || "Request failed");
        error.status = response.status;
        throw error;
    }

    return response.json();
}