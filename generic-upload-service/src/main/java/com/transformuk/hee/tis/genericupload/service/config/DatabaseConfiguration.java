package com.transformuk.hee.tis.genericupload.service.config;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories("com.transformuk.hee.tis.genericupload.service.repository")
@EnableTransactionManagement
public class DatabaseConfiguration {

  private final Environment env;

  public DatabaseConfiguration(Environment env) {
    this.env = env;
  }

  @Bean
  public Hibernate5Module hibernate5Module() {
    return new Hibernate5Module();
  }
}
