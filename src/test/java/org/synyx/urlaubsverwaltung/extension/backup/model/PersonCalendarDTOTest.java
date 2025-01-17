package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.calendar.PersonCalendar;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;

class PersonCalendarDTOTest {

    @Test
    void personCalendarDTOHandlesValidData() {
        PersonCalendarDTO dto = new PersonCalendarDTO(1L, "externalId", Period.ofMonths(6), "secret");
        Person person = new Person();

        final PersonCalendar personCalendar = dto.toPersonCalendarEntity(person);

        assertThat(personCalendar.getPerson()).isEqualTo(person);
        assertThat(personCalendar.getCalendarPeriod()).isEqualTo(dto.calendarPeriod());
        assertThat(personCalendar.getSecret()).isEqualTo(dto.secret());
    }

}
