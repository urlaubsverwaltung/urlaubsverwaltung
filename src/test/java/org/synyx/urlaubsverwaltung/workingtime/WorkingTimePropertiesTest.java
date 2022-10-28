package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkingTimePropertiesTest {

    @Test
    void defaultValues() {

        final WorkingTimeProperties workingTimeProperties = new WorkingTimeProperties();
        assertThat(workingTimeProperties.getDefaultWorkingDays()).contains(1, 2, 3, 4, 5);
        assertThat(workingTimeProperties.isDefaultWorkingDaysDeactivated()).isFalse();
    }

    @Test
    void isDefaultWorkingDaysDeactivated() {
        final WorkingTimeProperties workingTimeProperties = new WorkingTimeProperties();
        workingTimeProperties.setDefaultWorkingDays(List.of(-1));

        assertThat(workingTimeProperties.isDefaultWorkingDaysDeactivated()).isTrue();
    }
}
