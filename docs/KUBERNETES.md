# Running on Kubernetes with Minikube

[← Back to the main README](../README.md)

The project includes a Kubernetes deployment using Minikube, Kubernetes manifests, the NGINX Ingress Controller, local hostname entries, and Keycloak for authentication and RBAC.

## Requirements

Install:

- Docker
- `kubectl`
- Minikube

Verify the installations:

```bash
docker --version
kubectl version --client
minikube version
```

## Start the Kubernetes environment

From the repository root:

```bash
./start.sh k8s
```

The script is intended to build images, start Minikube, enable Ingress, deploy the manifests, configure host entries, and start the Minikube tunnel as supported by the local setup.

Skip Docker image builds:

```bash
./start.sh k8s --no-build
```

Skip `/etc/hosts` modification if the required entries already exist:

```bash
./start.sh k8s --no-hosts
```

## Access the application

The configured hostnames are:

- Frontend/application: `http://ecommerce.local`
- Keycloak: `http://keycloak.ecommerce.local`

Ensure both hostnames resolve to the Minikube Ingress endpoint. Depending on your operating system and Minikube setup, the tunnel may need to remain running in a separate terminal.

For first-time realm/client configuration, see [Authentication and Authorization](SECURITY.md#first-time-keycloak-setup).

## Stop the Kubernetes environment

Delete application resources and stop Minikube:

```bash
./stop.sh k8s
```

Preserve `/etc/hosts` entries:

```bash
./stop.sh k8s --no-hosts-removed
```

## Useful Kubernetes commands

View pods:

```bash
kubectl get pods -n ecommerce
```

View services:

```bash
kubectl get svc -n ecommerce
```

View Ingress resources:

```bash
kubectl get ingress -n ecommerce
```

View recent events:

```bash
kubectl get events -n ecommerce --sort-by=.lastTimestamp
```

Follow application logs:

```bash
kubectl logs -f deployment/order-service -n ecommerce
kubectl logs -f deployment/inventory-service -n ecommerce
kubectl logs -f deployment/gateway-service -n ecommerce
```

## Troubleshooting checklist

- Check Minikube: `minikube status`
- Check workload status: `kubectl get pods -n ecommerce`
- Inspect service endpoints: `kubectl get endpoints -n ecommerce`
- Inspect Ingress and controller pods: `kubectl get ingress -n ecommerce` and `kubectl get pods -n ingress-nginx`
- Review pod logs and recent Kubernetes events.
- Confirm both local hostnames resolve to the correct Ingress endpoint.
- If a request hangs, inspect browser network details and gateway/service logs to identify which hop is not responding.
