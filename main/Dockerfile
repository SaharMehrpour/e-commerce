FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# RUNTIME STAGE
FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=builder /app/target/main-0.0.1-SNAPSHOT.jar app.jar

ENV MONGO_URI=mongodb://mongodb:27017/ecommerce
ENV SPRING_MONGODB_URI=mongodb://mongodb:27017/ecommerce
ENV REDIS_HOST=redis
ENV REDIS_PORT=6379
ENV POSTGRES_URL=jdbc:postgresql://postgres-inventory:5432/inventory_db
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=password
ENV KAFKA_SERVERS=kafka:29092
ENV INVENTORY_SERVICE_URL=http://backend:8080/inventory

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
