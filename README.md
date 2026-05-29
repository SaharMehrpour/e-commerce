# 🛒 E-Commerce Event-Driven System

A full-stack e-commerce system built with **Spring Boot**, **React**, and an **event-driven architecture using Kafka**, with caching via **Redis** and persistence in **MongoDB** and **PostgreSQL**.

This project is designed for learning and demonstrating modern backend architecture concepts such as microservices design, messaging systems, caching strategies, and full-stack integration.



## 🚀 Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring Web MVC
- Spring Data MongoDB
- Spring Data PostgreSQL
- Spring Kafka
- Redis (Spring Cache abstraction)

### Frontend
- React (Vite)
- JavaScript
- Fetch API

### Infrastructure
- Docker & Docker Compose
- Apache Kafka
- ZooKeeper
- MongoDB
- PostgreSQL
- Redis


## 🧱 Architecture Overview
```
React Frontend
↓ HTTP
Spring Boot Backend
↓
MongoDB (source of truth)
PostgreSQL (source of truth)
Redis (cache layer)
↓
Kafka (event-driven messaging)
↓
Future microservices (Payment, Inventory, Shipping)
```


## 📦 Features (So Far)

### Order Service
- Create order
- Cancel order
- Fetch all orders
- Fetch single order
- Update single order

### Inventory Service
- Track product stock
- Reserve stock
- Deduct stock (on confirmed order)
- Restore stock (on cancellation)
- Check availability
- Update stock
- View the inventory

### Event-Driven Messaging
- `OrderCreatedEvent`
- `OrderCancelledEvent`
- `OrderUpdatedEvent`
- Kafka producer integration

### Caching
- Redis caching for orders
- Cache update on write operations

### Frontend
- Create orders from UI
- View orders
- Simple React interface


## 🧪 Testing

### Backend
- JUnit 5
- Custom fake repository
- Fake Kafka producer
- Pure unit tests (no Spring context required)

```bash
./mvnw clean test
```

### Frontend
```bash
npm run test
```

## 🐳 Running the Project

Instead of running services manually, you can use the provided scripts.

### 🚀 Start the application
```
./start.sh
```

This script will:

- Start all infrastructure services using Docker Compose (`docker-compose up -d`):
    - Kafka
    - ZooKeeper
    - MongoDB
    - PostgreSQL
    - Redis
- Build and run the Spring Boot backend:
    - `./mvnw clean install`
    - `./mvnw spring-boot:run`
- Install and start the React frontend:
    - `npm install`
    - `npm run dev`

After running, the system will be available at:

- Backend → http://localhost:8080
- Frontend → http://localhost:5173

### 🛑 Stop the application
```
./stop.sh
```

This script will:

- Stop all Docker containers (Kafka, MongoDB, Redis, ZooKeeper, PostgreSQL)
- Stop any running backend process
- Stop the frontend development server (if running in background)

## 📡 API Endpoints
Orders:
- Create Order `POST /orders`
- Get All Orders `GET /orders`
- Get Order by ID `GET /orders/{id}`
- Cancel Order `PATCH /orders/{id}/cancel`

Inventory:
- Stock product `PATCH /inventory/add`
- Reserve stock `PATCH /inventory/reserve`
- Deduct stock `PATCH /inventory/deduct`
- Restore stock `PATCH /inventory/release`
- Check availability `GET /inventory/{product_id}`
- Update stock  `PATCH /inventory/{product_id}`
- Get inventory `GET /inventory`


## 🧠 Key Design Decisions

***Event-Driven Architecture:***
Orders emit events instead of directly triggering downstream logic.

***Redis Cache:***
Used to improve read performance for frequently accessed orders.

***MongoDB:***
Acts as the source of truth for order data.

***PostgreSQL:***
A Relational database acts as the source of truth for inventory data.

***Separation of Concerns:***
- Controller → HTTP layer
- Service → business logic
- Kafka Producer → event publishing
- Repository → persistence

## 🔥 Future Improvements
- Payment microservice (Kafka consumer)
- Inventory service
- Dockerize frontend
- Kubernetes deployment (Minikube)
- API Gateway
- Authentication (JWT / OAuth2)
- Distributed tracing

## Acknowledgements

Some development tasks were assisted by AI coding tools (including OpenAI Codex).