package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LocaleConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void beansHaveBeenCreated() {
        this.contextRunner
            .withBean(UserSettingsRepository.class, () -> mock(UserSettingsRepository.class))
            .withBean(UserSettingsService.class)
            .withUserConfiguration(LocaleConfiguration.class)
            .run(context -> {
                assertThat(context).hasBean("localeResolver");
                assertThat(context.getBean(LocaleInterceptorConfigurer.class).getHandlerInterceptors())
                    .hasSize(1)
                    .hasOnlyElementsOfTypes(LocaleModelInterceptor.class);
            });
    }

    @Test
    void localeModelInterceptorConfiguredWithLanguage() {
        final LocaleConfiguration localeConfiguration = new LocaleConfiguration();
        final LocaleModelInterceptor localeModelInterceptor = localeConfiguration.localeModelInterceptor();
        assertThat(localeModelInterceptor.getParamName()).isEqualTo("language");
    }
}
