package org.synyx.urlaubsverwaltung.web.sicknote;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class UnknownSickNoteExceptionTest {

    @Test
    public void ensureCorrectExceptionMessage() {

        UnknownSickNoteException exception = new UnknownSickNoteException(42);

        Assert.assertEquals("Wrong exception message", "No sick note found for ID = 42", exception.getMessage());
    }
}
