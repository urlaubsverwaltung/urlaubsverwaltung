package org.synyx.urlaubsverwaltung.sicknote.web;

import org.junit.Assert;
import org.junit.jupiter.api.Test;


class UnknownSickNoteExceptionTest {

    @Test
    void ensureCorrectExceptionMessage() {

        UnknownSickNoteException exception = new UnknownSickNoteException(42);

        Assert.assertEquals("Wrong exception message", "No sick note found for ID = 42", exception.getMessage());
    }
}
