package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class PersonConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(PersonConfiguration.class);

    @Test
    void ensureUiDeactivationIsEnabledByDefault() {
        contextRunner
            .run(context -> assertThat(context.getBean(PersonProperties.class).isUiDeactivationEnabled()).isTrue());
    }

    @Test
    void ensureUiDeactivationCanBeDisabled() {
        contextRunner
            .withPropertyValues("uv.person.ui-deactivation-enabled=false")
            .run(context -> assertThat(context.getBean(PersonProperties.class).isUiDeactivationEnabled()).isFalse());
    }
}
