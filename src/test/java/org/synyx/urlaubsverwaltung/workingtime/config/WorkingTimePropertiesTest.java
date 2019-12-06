package org.synyx.urlaubsverwaltung.workingtime.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkingTimePropertiesTest {

    @Test
    public void defaultValues() {

        final WorkingTimeProperties workingTimeProperties = new WorkingTimeProperties();
        assertThat(workingTimeProperties.getDefaultWorkingDays()).contains(1, 2, 3, 4, 5);
    }
}
