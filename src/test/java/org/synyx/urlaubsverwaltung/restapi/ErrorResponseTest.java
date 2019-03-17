package org.synyx.urlaubsverwaltung.restapi;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;


public class ErrorResponseTest {

    @Test
    public void ensureCorrectErrorResponse() {

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, new NumberFormatException("foo"));

        Assert.assertTrue("Wrong timestamp", errorResponse.getTimestamp() > 0);
        Assert.assertEquals("Wrong status", 400, errorResponse.getStatus());
        Assert.assertEquals("Wrong error", "Bad Request", errorResponse.getError());
        Assert.assertEquals("Wrong error", "java.lang.NumberFormatException", errorResponse.getException());
        Assert.assertEquals("Wrong error", "foo", errorResponse.getMessage());
    }
}
