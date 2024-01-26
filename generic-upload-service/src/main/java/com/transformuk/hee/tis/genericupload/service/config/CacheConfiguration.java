package com.transformuk.hee.tis.genericupload.service.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableCaching(proxyTargetClass = true)
@Configuration
public class CacheConfiguration {

  @Bean
  public Caffeine<Object, Object> caffeineConfig() {
    return Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS); //TODO externalise
  }

  @Bean
  public CacheManager cacheManager(Caffeine<Object, Object> caffieneConfig) {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
    cacheManager.setCaffeine(caffieneConfig);
    return cacheManager;
  }
}
