#!/bin/bash
set -euo pipefail

RED='\033[1;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

usage() {
    cat <<EOF
Usage: ./stop.sh [mode] [options]

Modes:
  dev                 Start with Docker Compose (default)
  k8s                 Start on Kubernetes (Minikube)

Options:
  no-hosts, no-hosts-removed, --no-hosts, --no-hosts-removed    Skip cleaning /etc/hosts (k8s only)
  -h, --help, help        Show this help and exit

Examples:
  ./stop.sh                      # dev mode, with build
  ./stop.sh k8s                  # k8s mode, with build
  ./stop.sh k8s --no-hosts       # k8s mode, don't touch /etc/hosts
EOF
}

MODE=${1:-dev}
NO_HOSTS_REMOVED=false

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/backend"

echo "=================================="

for arg in "$@"; do
    case "$arg" in
        -h|--help|help)
            usage
            exit 0
            ;;
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

    PID_FILE="/tmp/minikube-tunnel.pid"

    if [ -f "$PID_FILE" ]; then
        tunnel_pid="$(cat "$PID_FILE")"

        if [[ "$tunnel_pid" =~ ^[0-9]+$ ]] &&
        kill -0 "$tunnel_pid" 2>/dev/null; then

            process_command="$(ps -p "$tunnel_pid" -o command= 2>/dev/null || true)"

            case "$process_command" in
                *minikube*tunnel*)
                    kill "$tunnel_pid" 2>/dev/null || true
                    echo "Sent stop signal to tunnel process $tunnel_pid."
                    ;;
                *)
                    echo "PID $tunnel_pid is not a recognized Minikube tunnel process; leaving it untouched."
                    ;;
            esac
        else
            echo "Tunnel is stopped."
        fi

        rm -f "$PID_FILE"
    else
        echo "No tunnel PID file found; no process was terminated."
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