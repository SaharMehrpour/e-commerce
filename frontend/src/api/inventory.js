const BASE_URL = "http://localhost:8080/inventory";

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