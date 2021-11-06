package org.synyx.urlaubsverwaltung.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;

class LocaleModelInterceptorTest {

    @Test
    void modelAndView() {
        LocaleContextHolder.setLocale(GERMAN);

        final LocaleModelInterceptor localeModelInterceptor = new LocaleModelInterceptor();
        final ModelAndView modelAndView = new ModelAndView();

        localeModelInterceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), modelAndView);
        assertThat(modelAndView.getModelMap().getAttribute("locale")).isEqualTo("de");
    }

    @Test
    void modelAndViewWithParameterSet() {
        LocaleContextHolder.setLocale(GERMAN);

        final LocaleModelInterceptor localeModelInterceptor = new LocaleModelInterceptor();
        localeModelInterceptor.setParamName("language");
        final ModelAndView modelAndView = new ModelAndView();

        localeModelInterceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), modelAndView);
        assertThat(modelAndView.getModelMap().getAttribute("language")).isEqualTo("de");
    }
}
