package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ErrorControllerTest {

    private ErrorController sut;

    @BeforeEach
    void setUp() {
        sut = new ErrorController(mock(ErrorAttributes.class));
    }

    private static Stream<Arguments> errorCodes() {
        return Stream.of(
            Arguments.of(400, "thymeleaf/error"),
            Arguments.of(403, "thymeleaf/error/403"),
            Arguments.of(404, "thymeleaf/error/404"),
            Arguments.of(500, "thymeleaf/error")
        );
    }

    @ParameterizedTest
    @MethodSource("errorCodes")
    void errorHtml(int errorCode, String viewName) {
        final HttpServletRequest request = new MockHttpServletRequest();
        final HttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(errorCode);

        final ModelAndView modelAndView = sut.errorHtml(request, response);
        assertThat(modelAndView.getViewName()).isEqualTo(viewName);
        assertThat(modelAndView.getModel()).containsEntry("status", errorCode);
    }
}
