package com.transformuk.hee.tis.genericupload.service.config;

import com.transformuk.hee.tis.security.config.KeycloakClientConfig;
import org.keycloak.admin.client.Keycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class KeycloakConfig extends KeycloakClientConfig {

  @Bean
  public Keycloak keycloak() {
    return super.createKeycloak();
  }
}
