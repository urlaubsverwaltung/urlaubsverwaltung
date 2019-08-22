package org.synyx.urlaubsverwaltung.person.web;

import org.junit.Test;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

import static org.assertj.core.api.Assertions.assertThat;


public class UnknownPersonExceptionTest {

    @Test
    public void ensureCorrectExceptionMessage() {
        UnknownPersonException exception = new UnknownPersonException(42);
        assertThat(exception.getMessage()).isEqualTo("No person found for ID = 42");
    }

    @Test
    public void ensureCorrectAlternateExceptionMessage() {
        UnknownPersonException exception = new UnknownPersonException("username");
        assertThat(exception.getMessage()).isEqualTo("No person found for identifier = username");
    }
}
