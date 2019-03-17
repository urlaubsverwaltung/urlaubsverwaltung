package org.synyx.urlaubsverwaltung.web;

import org.junit.Assert;
import org.junit.Test;


public class AbstractNoResultFoundExceptionTest {

    @Test
    public void ensureCorrectExceptionMessage() {

        TestException exception = new TestException(42, "person");

        Assert.assertEquals("Wrong exception message", "No person found for ID = 42", exception.getMessage());
    }


    @Test
    public void ensureCorrectAlternateExceptionMessage() {

        TestException exception = new TestException("username", "person");

        Assert.assertEquals("Wrong exception message", "No person found for identifier = username",
            exception.getMessage());
    }

    private class TestException extends AbstractNoResultFoundException {

        public TestException(Integer id, String type) {

            super(id, type);
        }


        public TestException(String id, String type) {

            super(id, type);
        }
    }
}
