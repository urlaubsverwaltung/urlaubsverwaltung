package org.synyx.urlaubsverwaltung.web;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ErrorControllerTest {

    private ErrorController sut;

    @BeforeEach
    void setUp() {
        sut = new ErrorController(mock(ErrorAttributes.class));
    }

    @Test
    void errorHtml403() {
        final HttpServletRequest request = new MockHttpServletRequest();
        final HttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(403);

        final ModelAndView modelAndView = sut.errorHtml(request, response);
        assertThat(modelAndView.getViewName()).isEqualTo("thymeleaf/error/403");
        assertThat(modelAndView.getModel()).containsEntry("status", 403);
    }

    @Test
    void errorHtml404() {
        final HttpServletRequest request = new MockHttpServletRequest();
        final HttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(404);

        final ModelAndView modelAndView = sut.errorHtml(request, response);
        assertThat(modelAndView.getViewName()).isEqualTo("thymeleaf/error/404");
        assertThat(modelAndView.getModel()).containsEntry("status", 404);
    }

    @Test
    void errorHtml400() {
        final HttpServletRequest request = new MockHttpServletRequest();
        final HttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(400);

        final ModelAndView modelAndView = sut.errorHtml(request, response);
        assertThat(modelAndView.getViewName()).isEqualTo("thymeleaf/error");
        assertThat(modelAndView.getModel()).containsEntry("status", 400);
    }

    @Test
    void errorHtml500() {
        final HttpServletRequest request = new MockHttpServletRequest();
        final HttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);

        final ModelAndView modelAndView = sut.errorHtml(request, response);
        assertThat(modelAndView.getViewName()).isEqualTo("thymeleaf/error");
        assertThat(modelAndView.getModel()).containsEntry("status", 500);
    }
}
