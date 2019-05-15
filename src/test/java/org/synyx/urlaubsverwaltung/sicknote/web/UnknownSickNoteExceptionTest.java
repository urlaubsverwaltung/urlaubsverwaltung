package org.synyx.urlaubsverwaltung.sicknote.web;

import org.junit.Assert;
import org.junit.Test;


public class UnknownSickNoteExceptionTest {

    @Test
    public void ensureCorrectExceptionMessage() {

        UnknownSickNoteException exception = new UnknownSickNoteException(42);

        Assert.assertEquals("Wrong exception message", "No sick note found for ID = 42", exception.getMessage());
    }
}
