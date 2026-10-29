# Running Locally with Docker Compose

[← Back to the main README](../README.md)

## Requirements

- Docker
- Docker Compose

## Start the application

From the repository root, build and start the services:

```bash
./start.sh
```

Skip rebuilding images if they are already available:

```bash
./start.sh --no-build
```

## Available URLs

| Service | URL |
|---|---|
| Frontend | `http://localhost:5173` |
| API Gateway | `http://localhost:8080` |
| Prometheus | `http://localhost:9090` |

The Keycloak URL depends on the configured Docker Compose port mapping.

## First-time authentication setup

For the Keycloak realm, client, role, certificate, and verification steps, see [Authentication and Authorization](SECURITY.md#first-time-keycloak-setup).

## Stop the application

```bash
./stop.sh
```

This stops and removes the Docker Compose containers and networks managed by the project's stop script.

## Troubleshooting checklist

- Check container status with `docker compose ps`.
- Review service logs with `docker compose logs -f <service-name>`.
- Confirm that required ports are not already in use.
- Confirm that the frontend's Keycloak URL and backend issuer URL match the active Keycloak configuration.
- If authentication fails, verify the realm, client, redirect URIs, web origins, user roles, and token issuer.
