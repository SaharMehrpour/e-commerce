FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY . .

RUN mvn -pl order_service -am clean package -DskipTests

FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=builder /app/order_service/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]