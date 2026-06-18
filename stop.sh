#!/bin/bash
set -euo pipefail

RED='\033[1;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

MODE=${1:-dev}
NO_HOSTS_REMOVED=false

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/backend"

echo "=================================="

for arg in "$@"; do
    case "$arg" in
        k8s|dev)
            MODE="$arg"
            ;;
        no-hosts|no-hosts-removed|--no-hosts|--no-hosts-removed)
            NO_HOSTS_REMOVED=true
            ;;
        *)
            echo -e "${RED}Unknown argument: $arg${NC}"
            exit 1
            ;;
    esac
done

echo "Mode             : $MODE"
echo "No Hosts Removed : $NO_HOSTS_REMOVED"
echo "=================================="

if [ "$MODE" = "k8s" ]; then

    echo "🛑 Stopping Kubernetes (Minikube)..."
    echo "=================================="

    for cmd in kubectl minikube; do
        command -v "$cmd" >/dev/null 2>&1 || {
            echo -e "${RED}Missing '$cmd'${NC}"
            exit 1
        }
    done

    echo ""
    echo "🚇 Stopping minikube tunnel..."

    if [ -f /tmp/minikube-tunnel.pid ]; then
        kill "$(cat /tmp/minikube-tunnel.pid)" 2>/dev/null || true
        rm -f /tmp/minikube-tunnel.pid
        echo "Tunnel stopped."
    else
        pkill -f "minikube tunnel" 2>/dev/null || true
        echo "Tunnel cleanup done."
    fi

    echo ""
    echo "🧹 Cleaning /etc/hosts..."

    if [ "$NO_HOSTS_REMOVED" = true ]; then
        echo "⏭️ Skipping /etc/hosts cleanup (--no-hosts-removed)"
    else
        sudo sed -i.bak '/ecommerce.local/d' /etc/hosts 2>/dev/null || true
    fi

    echo ""
    echo "🗑️ Deleting namespace..."

    kubectl delete namespace ecommerce --ignore-not-found=true

    while kubectl get namespace ecommerce >/dev/null 2>&1; do
        echo "Waiting for namespace deletion..."
        sleep 2
    done

    echo -e "${YELLOW}Namespace removed.${NC}"

    echo ""
    echo "🛑 Stopping Minikube..."

    minikube stop

    echo "=================================="
    echo "Kubernetes stopped cleanly."
    echo "=================================="

    echo ""
    echo "ℹ️ Note: kubectl context is left unchanged."
    echo "   Run 'minikube start' to restore access."

else

    echo "🐳 Stopping DEV Docker Compose..."
    echo "=================================="

    command -v docker >/dev/null 2>&1 || {
        echo -e "${RED}docker missing${NC}"
        exit 1
    }

    docker compose -f docker-compose.yml down

    echo ""
    echo "=================================="
    echo "Docker stopped."
    echo "=================================="

fi