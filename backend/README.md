## Development Setup

### Start Infrastructure Services

```bash
docker compose up -d mongodb postgres-inventory redis zookeeper kafka
```

### Build Backend Services

From the repository root:

```bash
mvn clean install
```

### Run Services Locally

API Gateway:

```bash
cd backend/gateway_service
mvn spring-boot:run
```

Order Service:

```bash
cd backend/order_service
mvn spring-boot:run
```

Inventory Service:

```bash
cd backend/inventory_service
mvn spring-boot:run
```

---

## Reset Databases and Cache

Stop and remove the containers:

```bash
docker compose down
```

Remove persisted data volumes:

```bash
docker volume rm \
  backend_inventory_pgdata \
  backend_mongo-data \
  backend_redis-data
```

---

## Run the Full System with Docker

Build and start all services:

```bash
docker compose up --build
```

Run in detached mode:

```bash
docker compose up --build -d
```

After startup:

| Service     | URL                   |
| ----------- | --------------------- |
| Frontend    | http://localhost:5173 |
| API Gateway | http://localhost:8080 |
| Prometheus  | http://localhost:9090 |

Stop all services:

```bash
docker compose down
```

Or from the repository root:

```bash
./start.sh
```

```bash
./stop.sh
```
