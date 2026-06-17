#!/bin/bash
set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[1;31m'
NC='\033[0m'

MODE="dev"
NO_BUILD=false
NO_HOSTS=false

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/backend"

echo "=================================="

# =========================
# ARG PARSING
# =========================

for arg in "$@"; do
    case "$arg" in
        k8s|dev)
            MODE="$arg"
            ;;
        no-build|--no-build)
            NO_BUILD=true
            ;;
        no-hosts|--no-hosts)
            NO_HOSTS=true
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
        echo "🧠 Ensuring /etc/hosts entry..."

        if ! grep -q "ecommerce.local" /etc/hosts; then
            echo -e "${YELLOW}Adding hosts entry...${NC}"
            echo "$HOST_ENTRY" | sudo tee -a /etc/hosts >/dev/null
        fi
    fi

    echo ""
    echo "🚇 Starting minikube tunnel..."

    if ! pgrep -f "minikube tunnel" >/dev/null; then
        nohup minikube tunnel >/tmp/minikube-tunnel.log 2>&1 &
        echo $! > /tmp/minikube-tunnel.pid
    else
        echo "Tunnel already running."
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
