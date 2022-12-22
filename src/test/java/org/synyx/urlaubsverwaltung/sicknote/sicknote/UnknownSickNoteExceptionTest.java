package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class UnknownSickNoteExceptionTest {

    @Test
    void ensureCorrectExceptionMessage() {

        UnknownSickNoteException exception = new UnknownSickNoteException(42L);

        assertThat(exception.getMessage()).isEqualTo("No sick note found for ID = 42");
    }
}
