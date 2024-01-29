package com.transformuk.hee.tis.genericupload.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Properties are configured in the application.yml file.
 * </p>
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

}
