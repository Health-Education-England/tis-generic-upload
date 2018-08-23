package com.transformuk.hee.tis.genericupload.service.config;

import com.transformuk.hee.tis.assessment.client.config.AssessmentClientConfig;
import com.transformuk.hee.tis.reference.client.config.ReferenceClientConfig;
import org.keycloak.admin.client.Keycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("prod")
public class AssessmentClientProdConfig extends AssessmentClientConfig {
  @Bean
  public RestTemplate assessmentRestTemplate(Keycloak keycloak) {
    return super.prodAssessmentRestTemplate(keycloak);
  }
}
