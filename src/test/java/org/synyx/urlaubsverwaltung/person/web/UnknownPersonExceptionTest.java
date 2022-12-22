package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

import static org.assertj.core.api.Assertions.assertThat;


class UnknownPersonExceptionTest {

    @Test
    void ensureCorrectExceptionMessage() {
        UnknownPersonException exception = new UnknownPersonException(42L);
        assertThat(exception.getMessage()).isEqualTo("No person found for ID = 42");
    }

    @Test
    void ensureCorrectAlternateExceptionMessage() {
        UnknownPersonException exception = new UnknownPersonException("username");
        assertThat(exception.getMessage()).isEqualTo("No person found for identifier = username");
    }
}
