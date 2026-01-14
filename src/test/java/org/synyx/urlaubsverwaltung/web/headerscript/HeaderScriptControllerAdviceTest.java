package org.synyx.urlaubsverwaltung.web.headerscript;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class HeaderScriptControllerAdviceTest {

    private HeaderScriptControllerAdvice sut;

    @BeforeEach
    void setUp() {
        final String scriptContent = "<script defer src=\"https://example.com/my-script.js\"></script>";
        final HeaderScriptConfigProperties properties = new HeaderScriptConfigProperties(true, scriptContent);
        sut = new HeaderScriptControllerAdvice(properties);
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
        assertThat(modelAndView.getModel()).doesNotContainKey("headerScriptContent");
    }

    @ParameterizedTest
    @ValueSource(strings = {"forward:view-name", "redirect:view-name"})
    void ensureModelAttributeIsNotSetWhenViewNameStartsWith(String viewName) {
        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(viewName);

        sut.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModel()).doesNotContainKey("headerScriptContent");
    }

    @Test
    void ensureModelAttribute() {
        final ModelAndView modelAndView = new ModelAndView("any-viewname");
        sut.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModel()).containsEntry("headerScriptContent", "<script defer src=\"https://example.com/my-script.js\"></script>");
    }

    @Test
    void ensureEmptyContentIsAdded() {
        final HeaderScriptConfigProperties properties = new HeaderScriptConfigProperties(true, "");
        final HeaderScriptControllerAdvice adviceWithEmptyContent = new HeaderScriptControllerAdvice(properties);

        final ModelAndView modelAndView = new ModelAndView("any-viewname");
        adviceWithEmptyContent.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModel()).containsEntry("headerScriptContent", "");
    }
}
