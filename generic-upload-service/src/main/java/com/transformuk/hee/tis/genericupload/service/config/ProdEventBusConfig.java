package com.transformuk.hee.tis.genericupload.service.config;

import com.transformuk.hee.tis.genericupload.service.storage.FileRecordStorage;
import com.transformuk.hee.tis.genericupload.service.storage.FileRecordStorageInFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "stage", "prod", "uidev"})
public class ProdEventBusConfig {

  private static final Logger LOG = LoggerFactory.getLogger(ProdEventBusConfig.class);

  @Bean
  public FileRecordStorage fileRecordStorage() {
    LOG.info("Using File dead letter writer");
    return new FileRecordStorageInFile();
  }
}