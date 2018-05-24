package com.transformuk.hee.tis.genericupload.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.transformuk.hee.tis.audit.repository.TisAuditRepository;
import com.transformuk.hee.tis.client.impl.ServiceKey;
import com.transformuk.hee.tis.filestorage.config.TisFileStorageConfig;
import com.transformuk.hee.tis.genericupload.service.config.ApplicationProperties;
import com.transformuk.hee.tis.genericupload.service.config.DefaultProfileUtil;
import io.github.jhipster.config.JHipsterConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.autoconfigure.MetricFilterAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ComponentScans({
        @ComponentScan("com.transformuk.hee.tis.genericupload.service"),
        @ComponentScan("com.transformuk.hee.tis.client.impl"),
        @ComponentScan("com.transformuk.hee.tis.tcs.client"),
        @ComponentScan("com.transformuk.hee.tis.reference")
})
@EnableAutoConfiguration(exclude = {MetricFilterAutoConfiguration.class, MetricRepositoryAutoConfiguration.class})
@EnableConfigurationProperties({ApplicationProperties.class})
@PropertySource({
        "classpath:/config/application.properties",
        "classpath:/config/profileclientapplication.properties",
        "classpath:/config/tcsclientapplication.properties",
        "classpath:/config/referenceclientapplication.properties"
})
@Import(TisFileStorageConfig.class)
@EnableScheduling
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static final int EXPIRE_DATA_IN_SECONDS = 60;

    private final Environment env;

    public Application(Environment env) {
        this.env = env;
    }

    /**
     * Main method, used to run the application.
     *
     * @param args the command line arguments
     * @throws UnknownHostException if the local host name could not be resolved into an address
     */
    public static void main(String[] args) throws UnknownHostException {
        SpringApplication app = new SpringApplication(Application.class);
        DefaultProfileUtil.addDefaultProfile(app);
        Environment env = app.run(args).getEnvironment();
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        log.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\t{}://localhost:{}\n\t" +
                        "External: \t{}://{}:{}\n\t" +
                        "Profile(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol,
                env.getProperty("server.port"),
                protocol,
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getActiveProfiles());
    }

    /**
     * Initializes generic-upload.
     * <p>
     * Spring profiles can be configured with a program arguments --spring.profiles.active=your-active-profile
     * <p>
     * You can find more information on how profiles work with JHipster on <a href="http://www.jhipster.tech/profiles/">http://www.jhipster.tech/profiles/</a>.
     */
    @PostConstruct
    public void initApplication() {
        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION)) {
            log.error("You have misconfigured your application! It should not run " +
                    "with both the 'dev' and 'prod' profiles at the same time.");
        }
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_CLOUD)) {
            log.error("You have misconfigured your application! It should not " +
                    "run with both the 'dev' and 'cloud' profiles at the same time.");
        }
        //TODO check if job can connect to Azure here - run a healthcheck on a Scheduled job and do the check before a user attempts to upload a file
    }

    @Bean
    public AuditEventRepository auditEventRepository() {
        return new TisAuditRepository();
    }

    @Bean
    public Cache<Class, Map<String, ServiceKey>> bulkServiceData() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(EXPIRE_DATA_IN_SECONDS, TimeUnit.SECONDS)
                .removalListener(notification -> log.info("***** Removal of {} contents from cache *****", notification.getKey()))
                .build();
    }

}
