package com.ecommerce.infrastructure.redis;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.beans.factory.ObjectProvider;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public CacheManager cacheManager(ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider) {
        RedisConnectionFactory redisConnectionFactory = redisConnectionFactoryProvider.getIfAvailable();

        if (redisConnectionFactory == null) {
            return new ConcurrentMapCacheManager("orders");
        }

        return RedisCacheManager.builder(redisConnectionFactory).build();
    }
}
