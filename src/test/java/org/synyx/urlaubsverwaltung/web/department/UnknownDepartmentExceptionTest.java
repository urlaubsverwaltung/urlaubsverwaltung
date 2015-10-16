package org.synyx.urlaubsverwaltung.web.department;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class UnknownDepartmentExceptionTest {

    @Test
    public void ensureCorrectExceptionMessage() {

        UnknownDepartmentException exception = new UnknownDepartmentException(42);

        Assert.assertEquals("Wrong exception message", "No department found for ID = 42", exception.getMessage());
    }
}
