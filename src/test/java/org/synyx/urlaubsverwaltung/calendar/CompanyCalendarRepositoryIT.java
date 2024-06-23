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
class CompanyCalendarRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private PersonService personService;

    @Autowired
    private CompanyCalendarRepository sut;

    @Test
    void ensureUniqueConstraintForCompanyCalendarWithDifferentPersons() {

        final Person savedPerson = personService.create("sam", "sam", "smith", "smith@example.org");
        final CompanyCalendar firstCompanyCalendar = new CompanyCalendar(savedPerson);
        firstCompanyCalendar.setCalendarPeriod(Period.ofDays(1));
        sut.save(firstCompanyCalendar);

        final Person savedPersonTwo = personService.create("martin", "martin", "scissor", "martin@example.org");
        final CompanyCalendar secondCompanyCalendar = new CompanyCalendar(savedPersonTwo);
        secondCompanyCalendar.setCalendarPeriod(Period.ofDays(1));
        sut.saveAndFlush(secondCompanyCalendar);

        assertThat(sut.findAll()).hasSize(2);
    }

    @Test
    void ensureUniqueConstraintForCompanyCalendar() {

        final Person savedPerson = personService.create("sam", "sam", "smith", "smith@example.org");

        final CompanyCalendar firstCompanyCalendar = new CompanyCalendar(savedPerson);
        firstCompanyCalendar.setCalendarPeriod(Period.ofDays(1));
        sut.save(firstCompanyCalendar);

        final CompanyCalendar secondCompanyCalendar = new CompanyCalendar(savedPerson);
        secondCompanyCalendar.setCalendarPeriod(Period.ofDays(1));

        final DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> sut.saveAndFlush(secondCompanyCalendar));
        assertThat(exception.getMessage()).contains("constraint [company_calendar_person_id_key]");
    }
}
