package com.transformuk.hee.tis.genericupload.service.config;

import com.transformuk.hee.tis.tcs.client.config.TcsClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("local")
public class TcsClientLocalConfig extends TcsClientConfig {
  @Bean
  public RestTemplate tcsRestTemplate() {
    return super.defaultTcsRestTemplate();
  }
}
