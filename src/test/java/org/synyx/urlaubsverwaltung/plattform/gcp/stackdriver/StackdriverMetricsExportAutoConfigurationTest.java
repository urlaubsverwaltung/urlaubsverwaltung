package org.synyx.urlaubsverwaltung.plattform.gcp.stackdriver;


import io.micrometer.core.instrument.Clock;
import io.micrometer.stackdriver.StackdriverConfig;
import io.micrometer.stackdriver.StackdriverMeterRegistry;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

public class StackdriverMetricsExportAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(StackdriverMetricsExportAutoConfiguration.class));

    @Test
    public void backsOffWithoutAClock() {
        this.contextRunner.run((context) -> assertThat(context).doesNotHaveBean(StackdriverMeterRegistry.class));
    }

    @Test
    public void failsWithoutAnProjectId() {
        this.contextRunner.withUserConfiguration(BaseConfiguration.class)
                .run((context) -> assertThat(context).hasFailed());
    }

    @Test
    public void autoConfiguresConfigAndMeterRegistry() {
        this.contextRunner.withUserConfiguration(BaseConfiguration.class)
                .withPropertyValues("management.metrics.export.stackdriver.project-id=qwert")
                .run((context) -> assertThat(context).hasSingleBean(StackdriverMeterRegistry.class)
                        .hasSingleBean(StackdriverConfig.class));
    }

    @Test
    public void autoConfigurationCanBeDisabled() {
        this.contextRunner.withUserConfiguration(BaseConfiguration.class)
                .withPropertyValues("management.metrics.export.stackdriver.enabled=false")
                .run((context) -> assertThat(context).doesNotHaveBean(StackdriverMeterRegistry.class)
                        .doesNotHaveBean(StackdriverConfig.class));
    }

    @Test
    public void allowsCustomConfigToBeUsed() {
        this.contextRunner.withUserConfiguration(CustomConfigConfiguration.class).run((context) -> assertThat(context)
                .hasSingleBean(StackdriverMeterRegistry.class).hasSingleBean(StackdriverConfig.class).hasBean("customConfig"));
    }

    @Test
    public void allowsCustomRegistryToBeUsed() {
        this.contextRunner.withUserConfiguration(CustomRegistryConfiguration.class)
                .withPropertyValues("management.metrics.export.stackdriver.project-id=qwert")
                .run((context) -> assertThat(context).hasSingleBean(StackdriverMeterRegistry.class)
                        .hasBean("customRegistry").hasSingleBean(StackdriverConfig.class));
    }

    @Test
    public void stopsMeterRegistryWhenContextIsClosed() {
        this.contextRunner.withUserConfiguration(BaseConfiguration.class)
                .withPropertyValues("management.metrics.export.stackdriver.project-id=qwert").run((context) -> {
            StackdriverMeterRegistry registry = context.getBean(StackdriverMeterRegistry.class);
            assertThat(registry.isClosed()).isFalse();
            context.close();
            assertThat(registry.isClosed()).isTrue();
        });
    }

    @Configuration
    static class BaseConfiguration {
        @Bean
        Clock clock() {
            return Clock.SYSTEM;
        }
    }

    @Configuration
    @Import(BaseConfiguration.class)
    static class CustomConfigConfiguration {
        @Bean
        StackdriverConfig customConfig() {
            return (key) -> {
                if ("stackdriver.projectId".equals(key)) {
                    return "qwert";
                }
                return null;
            };
        }
    }

    @Configuration
    @Import(BaseConfiguration.class)
    static class CustomRegistryConfiguration {
        @Bean
        StackdriverMeterRegistry customRegistry(StackdriverConfig config, Clock clock) {
            return new StackdriverMeterRegistry(config, clock);
        }
    }
}
