package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class AbstractNoResultFoundExceptionTest {

    @Test
    void ensureCorrectExceptionMessage() {

        TestException exception = new TestException(42L, "person");

        assertThat(exception.getMessage()).isEqualTo("No person found for ID = 42");
    }


    @Test
    void ensureCorrectAlternateExceptionMessage() {

        TestException exception = new TestException("username", "person");

        assertThat(exception.getMessage())
            .isEqualTo("No person found for identifier = username");
    }

    private class TestException extends AbstractNoResultFoundException {

        public TestException(Long id, String type) {
            super(id, type);
        }

        public TestException(String id, String type) {
            super(id, type);
        }
    }
}
