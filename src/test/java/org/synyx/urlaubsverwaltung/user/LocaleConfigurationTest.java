package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocaleConfigurationTest {

    @Mock
    private InterceptorRegistry interceptorRegistry;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void beansHaveBeenCreated() {
        this.contextRunner
            .withBean(UserSettingsRepository.class, () -> mock(UserSettingsRepository.class))
            .withBean(UserSettingsService.class)
            .withBean(LocaleModelInterceptor.class)
            .withUserConfiguration(LocaleConfiguration.class)
            .run(context -> {
                assertThat(context).getBean("localeResolver").isInstanceOf(UserSettingsAwareSessionLocaleResolver.class);
            });
    }

    @Test
    void ensuresToAddInterceptorsToRegistry() {

        final LocaleModelInterceptor interceptor = new LocaleModelInterceptor();
        final LocaleConfiguration configuration = new LocaleConfiguration(interceptor);

        configuration.addInterceptors(interceptorRegistry);

        verify(interceptorRegistry).addInterceptor(interceptor);
    }
}
