package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.synyx.urlaubsverwaltung.person.PersonService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PersonDeleteViewControllerConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(PersonService.class, () -> mock(PersonService.class))
        .withUserConfiguration(PersonDeleteViewController.class);

    @Test
    void ensurePersonDeleteViewControllerExistsWhenPropertyIsMissing() {
        contextRunner
            .run(context -> assertThat(context).hasSingleBean(PersonDeleteViewController.class));
    }

    @Test
    void ensurePersonDeleteViewControllerExistsWhenPropertyIsEnabled() {
        contextRunner
            .withPropertyValues("uv.person.ui-deletion-enabled=true")
            .run(context -> assertThat(context).hasSingleBean(PersonDeleteViewController.class));
    }

    @Test
    void ensurePersonDeleteViewControllerDoesNotExistWhenPropertyIsDisabled() {
        contextRunner
            .withPropertyValues("uv.person.ui-deletion-enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(PersonDeleteViewController.class));
    }

    @Test
    void ensurePersonDeleteViewControllerDoesNotExistWhenPropertyIsDisabledUppercase() {
        contextRunner
            .withPropertyValues("uv.person.ui-deletion-enabled=FALSE")
            .run(context -> assertThat(context).doesNotHaveBean(PersonDeleteViewController.class));
    }
}
