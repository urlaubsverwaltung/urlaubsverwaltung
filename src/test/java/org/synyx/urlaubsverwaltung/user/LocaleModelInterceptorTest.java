package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;

class LocaleModelInterceptorTest {

    @Test
    void ensureLanguageModelAttribute() throws Exception {
        LocaleContextHolder.setLocale(GERMAN);

        final LocaleModelInterceptor interceptor = new LocaleModelInterceptor();

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("view-name");
        interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), modelAndView);

        assertThat(modelAndView.getModelMap().getAttribute("language")).isEqualTo("de");
    }

    @Test
    void ensureNoExceptionThrownWhenModelAndViewIsNull() throws Exception {
        final LocaleModelInterceptor interceptor = new LocaleModelInterceptor();
        interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), null);
    }

    @Test
    void ensureLanguageModelAttributeIsNotSetWhenThereIsNoViewName() throws Exception {
        LocaleContextHolder.setLocale(GERMAN);

        final LocaleModelInterceptor interceptor = new LocaleModelInterceptor();

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(null);
        interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), modelAndView);

        assertThat(modelAndView.getModelMap()).isEmpty();
    }

    @Test
    void ensureLanguageModelAttributeIsNotSetForForward() throws Exception {
        LocaleContextHolder.setLocale(GERMAN);

        final LocaleModelInterceptor interceptor = new LocaleModelInterceptor();

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("forward::view-name");
        interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), modelAndView);

        assertThat(modelAndView.getModelMap()).isEmpty();
    }

    @Test
    void ensureLanguageModelAttributeIsNotSetForRedirect() throws Exception {
        LocaleContextHolder.setLocale(GERMAN);

        final LocaleModelInterceptor interceptor = new LocaleModelInterceptor();

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect::view-name");
        interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), modelAndView);

        assertThat(modelAndView.getModelMap()).isEmpty();
    }
}
