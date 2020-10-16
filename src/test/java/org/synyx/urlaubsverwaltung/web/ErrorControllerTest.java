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
    void errorHtml() {
        final HttpServletRequest request = new MockHttpServletRequest();
        final HttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(403);

        final ModelAndView modelAndView = sut.errorHtml(request, response);
        assertThat(modelAndView.getViewName()).isEqualTo("errors");
        assertThat(modelAndView.getModel()).containsEntry("statusCode", 403);
    }
}
