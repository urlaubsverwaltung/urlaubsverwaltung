package org.synyx.urlaubsverwaltung.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import static org.assertj.core.api.Assertions.assertThat;

class LocaleConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void beansHaveBeenCreated() {
        this.contextRunner.withUserConfiguration(LocaleConfiguration.class)
            .run(context -> {
                assertThat(context).hasBean("localeResolver");
                assertThat(context.getBean(LocaleInterceptorConfigurer.class).getHandlerInterceptors())
                    .hasSize(2)
                    .hasOnlyElementsOfTypes(LocaleChangeInterceptor.class, LocaleModelInterceptor.class);
            });
    }

    @Test
    void localeChangeInterceptorConfiguredWithLanguage() {
        final LocaleConfiguration localeConfiguration = new LocaleConfiguration();
        final LocaleChangeInterceptor localeChangeInterceptor = localeConfiguration.localeChangeInterceptor();
        assertThat(localeChangeInterceptor.getParamName()).isEqualTo("language");
    }

    @Test
    void localeModelInterceptorConfiguredWithLanguage() {
        final LocaleConfiguration localeConfiguration = new LocaleConfiguration();
        final LocaleModelInterceptor localeModelInterceptor = localeConfiguration.localeModelInterceptor();
        assertThat(localeModelInterceptor.getParamName()).isEqualTo("language");
    }
}
