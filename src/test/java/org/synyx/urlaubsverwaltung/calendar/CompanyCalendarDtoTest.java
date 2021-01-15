package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.calendar.CalendarPeriodViewType.HALF_YEAR;

class CompanyCalendarDtoTest {

    @Test
    void defaultValues() {
        final CompanyCalendarDto companyCalendarDto = new CompanyCalendarDto();
        assertThat(companyCalendarDto.getCalendarPeriod()).isEqualByComparingTo(HALF_YEAR);
    }
}
