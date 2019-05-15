package org.synyx.urlaubsverwaltung.department.web;

import org.junit.Assert;
import org.junit.Test;


public class UnknownDepartmentExceptionTest {

    @Test
    public void ensureCorrectExceptionMessage() {

        UnknownDepartmentException exception = new UnknownDepartmentException(42);

        Assert.assertEquals("Wrong exception message", "No department found for ID = 42", exception.getMessage());
    }
}
