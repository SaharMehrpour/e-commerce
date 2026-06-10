package com.ecommerce.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.ecommerce.inventory",
        "com.ecommerce.shared"
})
// @EntityScan({
//     "com.ecommerce.inventory",
//     "com.ecommerce.shared"
// })
// @EnableJpaRepositories({
//     "com.ecommerce.inventory",
// })
@EnableJpaRepositories(basePackages = "com.ecommerce.inventory")
@EntityScan(basePackages = "com.ecommerce.inventory")
@EnableKafka
@EnableCaching
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}