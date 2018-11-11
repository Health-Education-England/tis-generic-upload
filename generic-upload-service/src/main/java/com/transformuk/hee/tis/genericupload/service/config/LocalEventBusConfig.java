package com.transformuk.hee.tis.genericupload.service.config;

import com.transformuk.hee.tis.genericupload.service.storage.FileRecordStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class LocalEventBusConfig {

  private static final Logger LOG = LoggerFactory.getLogger(LocalEventBusConfig.class);

  @Bean
  public FileRecordStorage fileRecordStorage() {
    LOG.info("Using File dead letter writer");
    return null; //new AzureFileRecordStorage();
  }
}
