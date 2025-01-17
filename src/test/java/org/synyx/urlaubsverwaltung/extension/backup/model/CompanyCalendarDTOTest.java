package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.calendar.CompanyCalendar;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;

class CompanyCalendarDTOTest {

    @Test
    void happyPath() {
        CompanyCalendarDTO dto = new CompanyCalendarDTO(1L, "externalId", Period.ofMonths(6), "secret");
        Person owner = new Person();
        CompanyCalendar entity = dto.toCompanyCalendarEntity(owner);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getPerson()).isEqualTo(owner);
        assertThat(entity.getCalendarPeriod()).isEqualTo(dto.calendarPeriod());
        assertThat(entity.getSecret()).isEqualTo(dto.secret());
    }

}
