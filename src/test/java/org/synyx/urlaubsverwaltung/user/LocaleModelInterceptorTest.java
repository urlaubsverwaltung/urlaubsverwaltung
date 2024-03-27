package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LocaleModelInterceptorTest {

    @Test
    void ensureLanguageModelAttribute() {
        LocaleContextHolder.setLocale(GERMAN);

        final LocaleModelInterceptor interceptor = new LocaleModelInterceptor();

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("view-name");
        interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), modelAndView);

        assertThat(modelAndView.getModelMap().getAttribute("locale")).isEqualTo(GERMAN);
        assertThat(modelAndView.getModelMap().getAttribute("language")).isEqualTo("de");
    }

    @Test
    void ensureNoExceptionThrownWhenModelAndViewIsNull() {
        final LocaleModelInterceptor interceptor = new LocaleModelInterceptor();
        assertDoesNotThrow(() ->
            interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), null)
        );
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"forward::view-name", "redirect::view-name"})
    void ensureLanguageModelAttributeIsNotSet(String viewName) {
        LocaleContextHolder.setLocale(GERMAN);

        final LocaleModelInterceptor interceptor = new LocaleModelInterceptor();

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(viewName);
        interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), modelAndView);

        assertThat(modelAndView.getModelMap()).isEmpty();
    }
}
