package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.calendar.DepartmentCalendar;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentCalendarDTOTest {

    @Test
    void toDepartmentCalendarEntityCreatesEntityWithValidData() {
        DepartmentCalendarDTO dto = new DepartmentCalendarDTO(1L, "externalId", 2L, Period.ofMonths(6), "secret");
        Person owner = new Person();
        DepartmentCalendar entity = dto.toDepartmentCalendarEntity(2L, owner);

        assertThat(entity).isNotNull();
        assertThat(entity.getDepartmentId()).isEqualTo(dto.departmentId());
        assertThat(entity.getPerson()).isEqualTo(owner);
        assertThat(entity.getCalendarPeriod()).isEqualTo(dto.calendarPeriod());
        assertThat(entity.getSecret()).isEqualTo(dto.secret());
    }

}
