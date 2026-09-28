import { createContext, useEffect, useState } from "react";
import keycloak from "./keycloak";

export const AuthContext = createContext(null);

let keycloakInitialized = false;

export function AuthProvider({ children }) {
    const [authenticated, setAuthenticated] = useState(false);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (keycloakInitialized) {
            return;
        }

        keycloakInitialized = true;

        keycloak
            .init({
                onLoad: "check-sso",
                pkceMethod: "S256",
            })
            .then((authenticated) => {
                setAuthenticated(authenticated);
                setLoading(false);
            })
            .catch((error) => {
                console.error("Keycloak initialization failed", error);
                setLoading(false);
            });
    }, []);

    if (loading) {
        return <div>Loading...</div>;
    }

    return (
        <AuthContext.Provider
            value={{
                keycloak,
                authenticated,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}