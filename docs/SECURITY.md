# Authentication and Authorization

[← Back to the main README](../README.md)

The application uses **Keycloak, OpenID Connect (OIDC), JWT, and Spring Security** to authenticate users and enforce role-based access control (RBAC).

## Authentication flow

1. The user logs in through the React frontend using the Keycloak JavaScript adapter.
2. Keycloak authenticates the user and issues a JWT access token.
3. The frontend includes the token in API requests using `Authorization: Bearer <token>`.
4. Spring Security validates the JWT and extracts the user's roles.
5. Protected endpoints allow or deny requests based on authentication and authorization rules.

## Role-based access control

The configured realm is `ecommerce`, the frontend client is `ecommerce-frontend`, and the application uses these realm roles:

| Role | Permissions |
|---|---|
| `CUSTOMER` | Access customer-facing functionality, including permitted order operations |
| `ADMIN` | Perform administrative operations, including inventory management |

Spring Security maps Keycloak realm roles to Spring Security authorities. Endpoint-level authorization rules, including `@PreAuthorize`, enforce permissions on protected operations.

For example, inventory stock updates require the `ADMIN` role:

```java
@PreAuthorize("hasRole('ADMIN')")
```

An authenticated user who lacks the required role receives `403 Forbidden`. Unauthenticated requests to protected endpoints are rejected. This separates **authentication (who the user is)** from **authorization (what the user can do)**.

## First-time Keycloak setup

Before running the application for the first time, configure Keycloak, generate the required certificates if your local setup uses them, and configure the realm and client.

### 1. Generate the required certificate

Generate the certificate and private key required by your Keycloak configuration. Store them where the Docker Compose or Kubernetes configuration expects them.

For local development with a self-signed certificate, ensure the certificate includes the appropriate hostname (for example, `keycloak.ecommerce.local`) and that the application environment trusts it when HTTPS is used.

**Security note:** Never commit private keys, passwords, tokens, or other sensitive credentials to version control.

### 2. Start Keycloak

Start the infrastructure and Keycloak services using the project's Docker Compose configuration or Kubernetes manifests.

For Kubernetes:

```bash
./start.sh k8s
```

Wait until the Keycloak container or pod is ready before continuing.

### 3. Configure the realm and client

Open the Keycloak administration console at the configured Keycloak URL and sign in with administrator credentials. Create or import the following configuration:

- **Realm:** `ecommerce`
- **Client:** `ecommerce-frontend`
- **Protocol:** OpenID Connect (OIDC)
- **Realm roles:** `ADMIN`, `CUSTOMER`
- **Users:** Create the required users and assign appropriate realm roles.

Configure the client settings to match the React frontend's Keycloak configuration, including client type, redirect URIs, and web origins.

### 4. Configure application authentication

Verify that the frontend and backend use the same Keycloak realm and the correct OIDC issuer URL.

Check that:

- The React frontend uses the Keycloak JavaScript adapter.
- The backend validates JWT access tokens issued by Keycloak.
- Spring Security maps Keycloak realm roles to Spring Security authorities.
- Protected endpoints enforce the required roles.

### 5. Verify the setup

1. Open the frontend and log in with a configured user.
2. Confirm that the user receives an access token.
3. Verify that a `CUSTOMER` can access permitted customer operations.
4. Verify that an `ADMIN` can perform inventory-management operations.
5. Confirm that unauthorized requests receive `401 Unauthorized` or `403 Forbidden`, as appropriate.

Once setup is complete, subsequent application starts should reuse the existing Keycloak configuration and certificates unless they have been removed or changed.
