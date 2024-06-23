package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class PersonCalendarRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonCalendarRepository sut;

    @Test
    void ensureUniqueConstraintForPersonCalendarWithDifferentPersons() {

        final Person savedPerson = personService.create("sam", "sam", "smith", "smith@example.org");
        final PersonCalendar firstPersonCalendar = new PersonCalendar(savedPerson);
        firstPersonCalendar.setCalendarPeriod(Period.ofDays(1));
        sut.save(firstPersonCalendar);

        final Person savedPersonTwo = personService.create("martin", "martin", "scissor", "martin@example.org");
        final PersonCalendar secondPersonCalendar = new PersonCalendar(savedPersonTwo);
        secondPersonCalendar.setCalendarPeriod(Period.ofDays(1));
        sut.save(secondPersonCalendar);

        assertThat(sut.findAll()).hasSize(2);
    }

    @Test
    void ensureUniqueConstraintForPersonCalendar() {

        final Person savedPerson = personService.create("sam", "sam", "smith", "smith@example.org");

        final PersonCalendar firstPersonCalendar = new PersonCalendar(savedPerson);
        firstPersonCalendar.setCalendarPeriod(Period.ofDays(1));
        sut.save(firstPersonCalendar);

        final PersonCalendar secondPersonCalendar = new PersonCalendar(savedPerson);
        secondPersonCalendar.setCalendarPeriod(Period.ofDays(1));

        final DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> sut.saveAndFlush(secondPersonCalendar));
        assertThat(exception.getMessage()).contains("constraint [person_calendar_person_id_key]");
    }
}
