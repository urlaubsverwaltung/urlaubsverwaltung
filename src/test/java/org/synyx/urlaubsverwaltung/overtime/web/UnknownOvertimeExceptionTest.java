package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.Assert;
import org.junit.Test;


public class UnknownOvertimeExceptionTest {

    @Test
    public void ensureCorrectExceptionMessage() {

        UnknownOvertimeException exception = new UnknownOvertimeException(42);

        Assert.assertEquals("Wrong exception message", "No overtime found for ID = 42", exception.getMessage());
    }
}
