package org.synyx.urlaubsverwaltung.branding;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;

class BrandingDataProviderTest {

    private final BrandingDataProvider sut = new BrandingDataProvider(new BrandingProperties("Abwesenheiten"));

    @Test
    void ensureApplicationName() {

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("viewName");

        sut.postHandle(new MockHttpServletRequest(), null, null, modelAndView);

        assertThat(modelAndView.getModelMap()).containsEntry("applicationName", "Abwesenheiten");
    }

    @ParameterizedTest
    @ValueSource(strings = {"redirect:/web/overview", "forward:/web/overview"})
    void ensureNoApplicationNameForRedirectAndForward(String viewName) {

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(viewName);

        sut.postHandle(new MockHttpServletRequest(), null, null, modelAndView);

        assertThat(modelAndView.getModelMap()).doesNotContainKey("applicationName");
    }
}
