package org.synyx.urlaubsverwaltung.department.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class UnknownDepartmentExceptionTest {

    @Test
    void ensureCorrectExceptionMessage() {

        UnknownDepartmentException exception = new UnknownDepartmentException(42L);

        assertThat(exception.getMessage()).isEqualTo("No department found for ID = 42");
    }
}
