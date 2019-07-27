package org.synyx.urlaubsverwaltung.web;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ErrorControllerTest {

    private ErrorController sut;

    @Before
    public void setUp() {
        sut = new ErrorController(mock(ErrorAttributes.class));
    }

    @Test
    public void errorHtml() {
        final HttpServletRequest request = new MockHttpServletRequest();
        final HttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(403);

        final ModelAndView modelAndView = sut.errorHtml(request, response);
        assertThat(modelAndView.getViewName()).isEqualTo("errors");
        assertThat(modelAndView.getModel().get("statusCode")).isEqualTo(403);
    }
}
