package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class PersonCalendarRepositoryIT extends TestContainersBase {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonCalendarRepository sut;

    @Test
    void ensureUniqueConstraintForPersonCalendarWithDifferentPersons() {

        final Person savedPerson = personService.save(new Person("sam", "smith", "sam", "smith@example.org"));
        final PersonCalendar firstPersonCalendar = new PersonCalendar(savedPerson);
        firstPersonCalendar.setCalendarPeriod(Period.ofDays(1));
        sut.save(firstPersonCalendar);

        final Person savedPersonTwo = personService.save(new Person("martin", "scissor", "martin", "martin@example.org"));
        final PersonCalendar secondPersonCalendar = new PersonCalendar(savedPersonTwo);
        secondPersonCalendar.setCalendarPeriod(Period.ofDays(1));
        sut.save(secondPersonCalendar);

        assertThat(sut.findAll()).hasSize(2);
    }

    @Test
    void ensureUniqueConstraintForPersonCalendar() {

        final Person savedPerson = personService.save(new Person("sam", "smith", "sam", "smith@example.org"));

        final PersonCalendar firstPersonCalendar = new PersonCalendar(savedPerson);
        firstPersonCalendar.setCalendarPeriod(Period.ofDays(1));
        sut.save(firstPersonCalendar);

        final PersonCalendar secondPersonCalendar = new PersonCalendar(savedPerson);
        secondPersonCalendar.setCalendarPeriod(Period.ofDays(1));

        final DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> sut.save(secondPersonCalendar));
        assertThat(exception.getMessage()).contains("constraint [unique_calendar_per_type]");
    }
}
