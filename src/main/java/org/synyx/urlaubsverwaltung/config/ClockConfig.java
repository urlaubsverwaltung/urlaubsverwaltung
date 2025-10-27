package org.synyx.urlaubsverwaltung.config;

import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public JpaProperties jpaProperties(JpaProperties properties, Clock clock) {
        properties.getProperties().put("hibernate.jdbc.time_zone", clock.getZone().getId());
        return properties;
    }
}
