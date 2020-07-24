package org.synyx.urlaubsverwaltung.workingtime.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkingTimePropertiesTest {

    @Test
    void defaultValues() {

        final WorkingTimeProperties workingTimeProperties = new WorkingTimeProperties();
        assertThat(workingTimeProperties.getDefaultWorkingDays()).contains(1, 2, 3, 4, 5);
    }
}
