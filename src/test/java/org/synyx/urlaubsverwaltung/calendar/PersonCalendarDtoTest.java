package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.calendar.CalendarPeriodViewType.HALF_YEAR;

class PersonCalendarDtoTest {

    @Test
    void defaultValues() {
        final PersonCalendarDto personCalendarDto = new PersonCalendarDto();
        assertThat(personCalendarDto.getCalendarPeriod()).isEqualByComparingTo(HALF_YEAR);
    }
}
