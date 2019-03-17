package org.synyx.urlaubsverwaltung.web.person;

import org.junit.Assert;
import org.junit.Test;


public class UnknownPersonExceptionTest {

    @Test
    public void ensureCorrectExceptionMessage() {

        UnknownPersonException exception = new UnknownPersonException(42);

        Assert.assertEquals("Wrong exception message", "No person found for ID = 42", exception.getMessage());
    }


    @Test
    public void ensureCorrectAlternateExceptionMessage() {

        UnknownPersonException exception = new UnknownPersonException("username");

        Assert.assertEquals("Wrong exception message", "No person found for identifier = username",
            exception.getMessage());
    }
}
