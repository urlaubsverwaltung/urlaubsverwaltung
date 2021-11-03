package org.synyx.urlaubsverwaltung.config;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocaleInterceptorConfigurerTest {

    @Mock
    private InterceptorRegistry interceptorRegistry;

    @Test
    void ensuresToAddInterceptorsToRegistry() {

        final LocaleModelInterceptor localeModelInterceptor = new LocaleModelInterceptor();
        final LocaleInterceptorConfigurer localeInterceptorConfigurer = new LocaleInterceptorConfigurer(List.of(localeModelInterceptor));
        localeInterceptorConfigurer.addInterceptors(interceptorRegistry);

        verify(interceptorRegistry).addInterceptor(localeModelInterceptor);
    }

    @Test
    void ensuresToGetInterceptors() {

        final LocaleModelInterceptor localeModelInterceptor = new LocaleModelInterceptor();
        final LocaleInterceptorConfigurer localeInterceptorConfigurer = new LocaleInterceptorConfigurer(List.of(localeModelInterceptor));

        final List<HandlerInterceptor> handlerInterceptors = localeInterceptorConfigurer.getHandlerInterceptors();
        assertThat(handlerInterceptors).containsExactly(localeModelInterceptor);
    }
}
