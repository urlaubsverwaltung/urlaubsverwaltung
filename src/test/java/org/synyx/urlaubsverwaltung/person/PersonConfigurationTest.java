package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class PersonConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(PersonConfiguration.class);

    @Test
    void ensureUiDeletionIsEnabledByDefault() {
        contextRunner
            .run(context -> assertThat(context.getBean(PersonProperties.class).isUiDeletionEnabled()).isTrue());
    }

    @Test
    void ensureUiDeletionCanBeDisabled() {
        contextRunner
            .withPropertyValues("uv.person.ui-deletion-enabled=false")
            .run(context -> assertThat(context.getBean(PersonProperties.class).isUiDeletionEnabled()).isFalse());
    }

    @Test
    void ensureUiDeletionCanBeDisabledUppercase() {
        contextRunner
            .withPropertyValues("uv.person.ui-deletion-enabled=FALSE")
            .run(context -> assertThat(context.getBean(PersonProperties.class).isUiDeletionEnabled()).isFalse());
    }
}
