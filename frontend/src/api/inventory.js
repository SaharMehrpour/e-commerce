import { getApiUrl } from "./config";

const BASE_URL = `${getApiUrl()}/inventory`;

export async function getInventory(keycloak) {
    if (!keycloak?.authenticated || !keycloak?.token) {
        throw new Error("You must be logged in to view inventory.");
    }

    const response = await fetch(BASE_URL, {
        headers: {
            "Authorization": `Bearer ${keycloak.token}`
        }
    });

    if (!response.ok) {
        const errorData = await response.json();
        const error = new Error(errorData.message || "Request failed");
        error.status = response.status;
        throw error;
    }

    return response.json();
}

export async function updateStock(keycloak, productId, quantity) {
  await keycloak.updateToken(30);

  const response = await fetch("/api/inventory/update", {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${keycloak.token}`,
    },
    body: JSON.stringify({
      productId,
      quantity,
    }),
  });

  if (!response.ok) {
    throw new Error(
      `Failed to update inventory (${response.status})`
    );
  }

  return response.json();
}

