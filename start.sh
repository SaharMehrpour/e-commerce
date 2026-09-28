#!/bin/bash
set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[1;31m'
NC='\033[0m'

usage() {
    cat <<EOF
Usage: ./start.sh [mode] [options]

Modes:
  dev                 Start with Docker Compose (default)
  k8s                 Start on Kubernetes (Minikube)

Options:
  no-build, --no-build    Skip building Docker images
  no-hosts, --no-hosts    Skip editing /etc/hosts (k8s only)
  tunnel, --tunnel        Start minikube tunnel (k8s only)
  -h, --help, help        Show this help and exit

Examples:
  ./start.sh                      # dev mode, with build
  ./start.sh k8s                  # k8s mode, with build
  ./start.sh k8s --no-build       # k8s mode, reuse existing images
  ./start.sh k8s --no-hosts       # k8s mode, don't touch /etc/hosts
EOF
}

MODE="dev"
NO_BUILD=false
NO_HOSTS=false
TUNNEL=false

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/backend"

echo "=================================="

# =========================
# ARG PARSING
# =========================

for arg in "$@"; do
    case "$arg" in
        -h|--help|help)
            usage
            exit 0
            ;;
        k8s|dev)
            MODE="$arg"
            ;;
        no-build|--no-build)
            NO_BUILD=true
            ;;
        no-hosts|--no-hosts)
            NO_HOSTS=true
            ;;
        tunnel|--tunnel)
            TUNNEL=true
            ;;
        *)
            echo -e "${RED}Unknown argument: $arg${NC}"
            exit 1
            ;;
    esac
done

echo "Mode     : $MODE"
echo "No Build : $NO_BUILD"
echo "No Hosts : $NO_HOSTS"
echo "=================================="

cleanup() {
    eval "$(minikube docker-env -u)" >/dev/null 2>&1 || true
}
trap cleanup EXIT

show_k8s_status() {
    echo ""
    echo -e "${YELLOW}Kubernetes status:${NC}"
    kubectl get pods -n ecommerce -o wide || true

    echo ""
    echo -e "${YELLOW}Recent events:${NC}"
    kubectl get events -n ecommerce --sort-by=.lastTimestamp | tail -n 30 || true
}

wait_for_deployments() {
    local timeout="$1"
    shift

    for deployment in "$@"; do
        echo "Waiting for deployment/$deployment..."

        if ! kubectl rollout status "deployment/$deployment" -n ecommerce --timeout="$timeout"; then
            echo -e "${RED}Timed out waiting for deployment/$deployment.${NC}"
            show_k8s_status
            return 1
        fi
    done
}

# =========================
# K8S MODE
# =========================

if [ "$MODE" = "k8s" ]; then

    echo "🚀 Starting KUBERNETES (Minikube)..."
    echo "=================================="

    for cmd in minikube kubectl; do
        command -v "$cmd" >/dev/null 2>&1 || {
            echo -e "${RED}Error: '$cmd' not installed.${NC}"
            exit 1
        }
    done

    if ! minikube status >/dev/null 2>&1; then
        minikube start \
            --driver=docker \
            --ports=80:80 \
            --ports=443:443 \
            --memory=4096 \
            --cpus=2 \
            --kubernetes-version=stable
    fi

    echo -e "${GREEN}✅ Minikube is ready.${NC}"

    echo ""
    echo "🔌 Enabling ingress..."
    minikube addons enable ingress >/dev/null 2>&1 || true

    echo ""
    echo "⏳ Waiting for ingress..."
    kubectl wait -n ingress-nginx \
        --for=condition=available deployment \
        --all \
        --timeout=240s

    echo ""
    echo "📦 Ensuring namespace..."
    kubectl create namespace ecommerce --dry-run=client -o yaml | kubectl apply -f -

    if [ "$NO_BUILD" = false ]; then

        echo ""
        echo "🔨 Building Docker images inside Minikube..."

        eval "$(minikube docker-env)"

        docker compose -f docker-compose.k8s.yml build \
            --no-cache \
            order-service \
            inventory-service \
            gateway-service \
            frontend

        eval "$(minikube docker-env -u)"

        echo -e "${GREEN}✅ Images built successfully.${NC}"
    else
        echo ""
        echo "⏭️ Skipping Docker build"
    fi

    echo "⏳ Waiting for ingress webhook to stabilize..."
    for i in {1..30}; do
        kubectl get endpoints ingress-nginx-controller-admission -n ingress-nginx >/dev/null 2>&1 && break
        echo "Waiting for webhook endpoints... ($i/30)"
        sleep 2
    done

    echo ""
    echo "🔒 Ensuring TLS secret..."
    CERT_DIR="$SCRIPT_DIR/certs"   # use $SCRIPT_DIR/backend/certs if that's where your certs live

    if [ ! -f "$CERT_DIR/tls.crt" ] || [ ! -f "$CERT_DIR/tls.key" ]; then
        if command -v mkcert >/dev/null 2>&1; then
            mkcert -install
            mkdir -p "$CERT_DIR"
            mkcert -cert-file "$CERT_DIR/tls.crt" -key-file "$CERT_DIR/tls.key" \
                ecommerce.local keycloak.ecommerce.local
        else
            echo -e "${YELLOW}No certs found and mkcert is not installed; ingress will use its default self-signed cert.${NC}"
        fi
    fi

    if [ -f "$CERT_DIR/tls.crt" ] && [ -f "$CERT_DIR/tls.key" ]; then
        kubectl create secret tls ecommerce-local-tls \
            --cert="$CERT_DIR/tls.crt" --key="$CERT_DIR/tls.key" \
            -n ecommerce --dry-run=client -o yaml | kubectl apply -f -
    fi

    echo ""
    echo "🔐 Creating Keycloak realm ConfigMap..."
    kubectl create configmap keycloak-realm-import \
        --from-file=ecommerce-realm.json=k8s/keycloak/import/ecommerce-realm.json \
        -n ecommerce --dry-run=client -o yaml | kubectl apply -f -

    echo ""
    echo "📦 Applying Kubernetes manifests..."
    kubectl apply -f k8s/ --recursive

    echo ""
    echo "♻️ Restarting deployments..."
    kubectl rollout restart deployment \
        frontend \
        order-service \
        inventory-service \
        gateway-service \
        kafka \
        mongodb \
        postgres-inventory \
        redis \
        zookeeper \
        -n ecommerce

    echo ""
    echo "⏳ Waiting for core services..."
    wait_for_deployments 180s \
        mongodb \
        redis \
        zookeeper \
        kafka \
        postgres-inventory

    echo ""
    echo "⏳ Waiting for app services..."
    wait_for_deployments 300s \
        order-service \
        inventory-service \
        gateway-service \
        frontend

    echo ""
    sleep 10


    if [ "$NO_HOSTS" = true ]; then
        echo ""
        echo "⏭️ Skipping /etc/hosts entry (--no-hosts)"
    else
        HOST_ENTRY="127.0.0.1 ecommerce.local"

        echo ""
        echo "🧠 Ensuring /etc/hosts entries..."

        for host in ecommerce.local keycloak.ecommerce.local; do
            if ! grep -qE "(^|[[:space:]])${host}([[:space:]]|$)" /etc/hosts; then
                echo -e "${YELLOW}Adding hosts entry for ${host}...${NC}"
                echo "127.0.0.1 ${host}" | sudo tee -a /etc/hosts >/dev/null
            fi
        done
    fi

    if [ "$TUNNEL" = true ]; then
        echo ""
        echo "🚇 Starting minikube tunnel..."
        if ! pgrep -f "minikube tunnel" >/dev/null; then
            nohup minikube tunnel >/tmp/minikube-tunnel.log 2>&1 &
            echo $! > /tmp/minikube-tunnel.pid
        else
            echo "Tunnel already running."
        fi
    fi

    echo ""
    echo "=================================="
    echo "🌐 http://ecommerce.local"
    echo "=================================="

# =========================
# DEV MODE
# =========================

else

    echo "🐳 Starting DEV Docker Compose..."
    echo "=================================="

    command -v docker >/dev/null 2>&1 || {
        echo -e "${RED}Error: docker not installed.${NC}"
        exit 1
    }

    if [ "$NO_BUILD" = false ]; then
        echo "🔨 Building images via docker compose..."
        docker compose -f docker-compose.yml build
    fi

    docker compose -f docker-compose.yml up -d

    echo ""
    echo "Frontend  : http://localhost:5173"
    echo "Backend   : http://localhost:8080"
    echo "Prometheus: http://localhost:9090"
    echo "=================================="
fi
