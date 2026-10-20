package org.synyx.urlaubsverwaltung.config;

import org.slf4j.Logger;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Instant;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.ZoneOffset.UTC;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@EnableConfigurationProperties(ClockProperties.class)
public class ClockConfig {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    @Bean
    public Clock clock(ClockProperties clockProperties) {

        final Instant fixedInstant = clockProperties.fixedInstant();
        if (fixedInstant == null) {
            return Clock.systemUTC();
        }

        LOG.warn("Application time is frozen at {} - do not use this in production!", fixedInstant);
        return Clock.fixed(fixedInstant, UTC);
    }

    @Bean
    public JpaProperties jpaProperties(JpaProperties properties, Clock clock) {
        properties.getProperties().put("hibernate.jdbc.time_zone", clock.getZone().getId());
        return properties;
    }
}
