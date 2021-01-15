package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.calendar.CalendarPeriodViewType.HALF_YEAR;

class DepartmentCalendarDtoTest {

    @Test
    void defaultValues() {
        final DepartmentCalendarDto departmentCalendarDto = new DepartmentCalendarDto();
        assertThat(departmentCalendarDto.getCalendarPeriod()).isEqualByComparingTo(HALF_YEAR);
    }
}
