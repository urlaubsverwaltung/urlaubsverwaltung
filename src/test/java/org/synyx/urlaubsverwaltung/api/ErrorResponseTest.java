package org.synyx.urlaubsverwaltung.api;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;


public class ErrorResponseTest {

    @Test
    public void ensureCorrectErrorResponse() {

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, new NumberFormatException("foo"));

        assertThat(errorResponse.getTimestamp() > 0).isTrue();
        assertThat(errorResponse.getStatus()).isEqualTo(400);
        assertThat(errorResponse.getError()).isEqualTo("Bad Request");
        assertThat(errorResponse.getException()).isEqualTo("java.lang.NumberFormatException");
        assertThat(errorResponse.getMessage()).isEqualTo("foo");
    }
}
