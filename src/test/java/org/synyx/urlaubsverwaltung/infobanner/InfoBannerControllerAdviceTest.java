package org.synyx.urlaubsverwaltung.infobanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class InfoBannerControllerAdviceTest {

    private InfoBannerControllerAdvice sut;

    @BeforeEach
    void setUp() {

        final InfoBannerConfigProperties.Text text = new InfoBannerConfigProperties.Text();
        text.setDe("info text");

        final InfoBannerConfigProperties properties = new InfoBannerConfigProperties();
        properties.setText(text);

        sut = new InfoBannerControllerAdvice(properties);
    }

    @Test
    void ensureModelAttributeIsNotSetWhenModelAndViewIsNull() {
        assertThatNoException()
            .isThrownBy(() -> sut.postHandle(null, null, null, null));
    }

    @Test
    void ensureModelAttributeIsNotSetWhenModelAndViewHasNoView() throws Exception {
        final ModelAndView modelAndView = new ModelAndView();
        sut.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModel()).doesNotContainEntry("infoBannerText", "info text");
    }

    @Test
    void ensureModelAttribute() throws Exception {
        final ModelAndView modelAndView = new ModelAndView("any-viewname");
        sut.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModel()).containsEntry("infoBannerText", "info text");
    }
}
