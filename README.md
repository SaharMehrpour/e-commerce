# 🛒 E-Commerce Event-Driven System

A full-stack e-commerce system built with **Spring Boot**, **React**, and an **event-driven architecture using Kafka**, with caching via **Redis** and persistence in **MongoDB**.

This project is designed for learning and demonstrating modern backend architecture concepts such as microservices design, messaging systems, caching strategies, and full-stack integration.



## 🚀 Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring Web MVC
- Spring Data MongoDB
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
- Redis


## 🧱 Architecture Overview
```
React Frontend
↓ HTTP
Spring Boot Backend
↓
MongoDB (source of truth)
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

The backend uses:
- JUnit 5
- Custom fake repository
- Fake Kafka producer
- Pure unit tests (no Spring context required)

### Run tests
```bash
./mvnw test
```

## 🐳 Running the Project

Instead of running services manually, you can use the provided scripts.

### 🚀 Start the application
```
./start.sh
```

This script will:

- Start all infrastructure services using Docker Compose:
    - Kafka
    - ZooKeeper
    - MongoDB
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

- Stop all Docker containers (Kafka, MongoDB, Redis, ZooKeeper)
- Stop any running backend process
- Stop the frontend development server (if running in background)

## 📡 API Endpoints
- Create Order `POST /orders`
- Get All Orders `GET /orders`
- Get Order by ID `GET /orders/{id}`
- Cancel Order `PATCH /orders/{id}/cancel`


## 🧠 Key Design Decisions

### Event-Driven Architecture

Orders emit events instead of directly triggering downstream logic.

### Redis Cache

Used to improve read performance for frequently accessed orders.

### MongoDB

Acts as the source of truth for order data.

### Separation of Concerns
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

## 👨‍💻 Author

Built as a learning project for mastering:

- Microservices
- Kafka event-driven systems
- Full-stack architecture
- Spring Boot backend engineering

## Acknowledgements

Some development tasks were assisted by AI coding tools (including OpenAI Codex).