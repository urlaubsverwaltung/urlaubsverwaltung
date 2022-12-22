package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class UnknownOvertimeExceptionTest {

    @Test
    void ensureCorrectExceptionMessage() {
        UnknownOvertimeException exception = new UnknownOvertimeException(42L);
        assertThat(exception.getMessage()).isEqualTo("No overtime found for ID = 42");
    }
}
