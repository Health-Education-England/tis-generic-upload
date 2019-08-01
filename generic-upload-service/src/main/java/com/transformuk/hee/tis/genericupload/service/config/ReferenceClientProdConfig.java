package com.transformuk.hee.tis.genericupload.service.config;

import com.transformuk.hee.tis.reference.client.config.ReferenceClientConfig;
import org.keycloak.admin.client.Keycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile({"dev", "stage", "prod", "uidev"})
public class ReferenceClientProdConfig extends ReferenceClientConfig {

  @Bean
  public RestTemplate tcsRestTemplate(Keycloak keycloak) {
    return super.prodReferenceRestTemplate(keycloak);
  }
}
