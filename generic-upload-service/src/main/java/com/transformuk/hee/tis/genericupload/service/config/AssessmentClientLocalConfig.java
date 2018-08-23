package com.transformuk.hee.tis.genericupload.service.config;

import com.transformuk.hee.tis.assessment.client.config.AssessmentClientConfig;
import com.transformuk.hee.tis.tcs.client.config.TcsClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("dev")
public class AssessmentClientLocalConfig extends AssessmentClientConfig {
  @Bean
  public RestTemplate assessmentRestTemplate() {
    return super.defaultAssessmentRestTemplate();
  }
}
