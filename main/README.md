- To start mongodb and kafka: `docker compose up -d`
- To install the app: `./mvnw clean install`
- To start the app: `./mvnw clean spring-boot:run`


### Reset the Database and Cache

1. Stop and remove all three containers
```bash
docker rm -f postgres-inventory ecommerce-mongo ecommerce-redis
```

2. Delete all three data volumes
```bash
docker volume rm main_inventory_pgdata main_mongo-data main_redis-data
```