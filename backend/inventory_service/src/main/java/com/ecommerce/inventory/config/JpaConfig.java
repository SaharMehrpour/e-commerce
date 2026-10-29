package com.ecommerce.inventory.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.ecommerce.inventory")
@EntityScan(basePackages = "com.ecommerce.inventory")
public class JpaConfig {
}