package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.calendar.CompanyCalendarAccessible;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarBackupDTOTest {

    @Test
    void happyPathDTOToCompanyCalendarAccessible() {
        CalendarBackupDTO calendarBackupDTO = new CalendarBackupDTO(null, null, null, true);
        CompanyCalendarAccessible entity = calendarBackupDTO.toCompanyCalendarAccessibleEntity();

        assertThat(entity).isNotNull();
        assertThat(entity.isAccessible()).isEqualTo(calendarBackupDTO.companyCalendarAccessible());
    }

}
