package com.transformuk.hee.tis.genericupload.service.config;

import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableCaching(proxyTargetClass = true)
@Configuration
public class CacheConfiguration {

  @Bean
  public CacheManager cacheManager() {
    GuavaCacheManager guavaCacheManager = new GuavaCacheManager();
    guavaCacheManager.setCacheBuilder(
        CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS)); //TODO externalise
    return guavaCacheManager;
  }
}
