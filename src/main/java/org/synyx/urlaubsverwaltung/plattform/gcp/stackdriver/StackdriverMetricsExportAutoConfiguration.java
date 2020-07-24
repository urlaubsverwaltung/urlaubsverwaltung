package org.synyx.urlaubsverwaltung.plattform.gcp.stackdriver;

import io.micrometer.core.instrument.Clock;
import io.micrometer.stackdriver.StackdriverConfig;
import io.micrometer.stackdriver.StackdriverMeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfigureBefore({CompositeMeterRegistryAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class})
@AutoConfigureAfter(MetricsAutoConfiguration.class)
@ConditionalOnBean(Clock.class)
@ConditionalOnClass(StackdriverMeterRegistry.class)
@ConditionalOnProperty(prefix = "management.metrics.export.stackdriver", name = "enabled", havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(StackdriverProperties.class)
public class StackdriverMetricsExportAutoConfiguration {

    // TODO Replace after https://github.com/spring-projects/spring-boot/pull/19528 is merged and we're running the spring-boot version 2.3

    private final StackdriverProperties properties;

    public StackdriverMetricsExportAutoConfiguration(StackdriverProperties stackdriverProperties) {
        this.properties = stackdriverProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public StackdriverConfig stackdriverConfig() {
        return new StackdriverPropertiesConfigAdapter(this.properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public StackdriverMeterRegistry stackdriverMeterRegistry(StackdriverConfig stackdriverConfig, Clock clock) {
        return StackdriverMeterRegistry.builder(stackdriverConfig)
            .clock(clock)
            .build();
    }
}
