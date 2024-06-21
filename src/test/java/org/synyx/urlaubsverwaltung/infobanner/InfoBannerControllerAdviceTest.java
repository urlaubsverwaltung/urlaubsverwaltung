package org.synyx.urlaubsverwaltung.infobanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class InfoBannerControllerAdviceTest {

    private InfoBannerControllerAdvice sut;

    @BeforeEach
    void setUp() {

        final InfoBannerConfigProperties.Text infoText = new InfoBannerConfigProperties.Text("info text");
        final InfoBannerConfigProperties properties = new InfoBannerConfigProperties(true, infoText);

        sut = new InfoBannerControllerAdvice(properties);
    }

    @Test
    void ensureModelAttributeIsNotSetWhenModelAndViewIsNull() {
        assertThatNoException()
            .isThrownBy(() -> sut.postHandle(null, null, null, null));
    }

    @Test
    void ensureModelAttributeIsNotSetWhenModelAndViewHasNoView() {
        final ModelAndView modelAndView = new ModelAndView();
        sut.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModel()).doesNotContainEntry("infoBannerText", "info text");
    }

    @ParameterizedTest
    @ValueSource(strings = {"forward::view-name", "redirect::view-name"})
    void ensureModelAttributeIsNotSetWhenViewNameStartsWith(String prefix) {

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(prefix);

        sut.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModel()).doesNotContainEntry("infoBannerText", "info text");
    }

    @Test
    void ensureModelAttribute() {
        final ModelAndView modelAndView = new ModelAndView("any-viewname");
        sut.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModel()).containsEntry("infoBannerText", "info text");
    }
}
