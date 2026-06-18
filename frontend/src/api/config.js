const DEFAULT_API_URL = "/api";

export function getApiUrl() {
    const configuredApiUrl = import.meta.env.VITE_API_URL || DEFAULT_API_URL;

    return configuredApiUrl.replace(/\/$/, "");
}
