package com.transformuk.hee.tis.genericupload.service.config;

import com.transformuk.hee.tis.reference.client.config.ReferenceClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("local")
public class ReferenceClientLocalConfig extends ReferenceClientConfig {
  @Bean
  public RestTemplate tcsRestTemplate() {
    return super.defaultReferenceRestTemplate();
  }
}
