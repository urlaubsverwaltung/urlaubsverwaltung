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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

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

    @Test
    void ensureDeleteByPersonWithoutOneOfRolesDeletesCalendarsOfUsersAndInactivePersons() {

        final Person user = personService.create("user", "user", "user", "user@example.org", List.of(), List.of(USER));
        final Person inactive = personService.create("inactive", "inactive", "inactive", "inactive@example.org", List.of(), List.of());
        final Person boss = personService.create("boss", "boss", "boss", "boss@example.org", List.of(), List.of(USER, BOSS));
        final Person office = personService.create("office", "office", "office", "office@example.org", List.of(), List.of(USER, OFFICE));
        for (Person person : List.of(user, inactive, boss, office)) {
            final CompanyCalendar companyCalendar = new CompanyCalendar(person);
            companyCalendar.setCalendarPeriod(Period.ofDays(1));
            sut.save(companyCalendar);
        }

        sut.deleteByPersonWithoutOneOfRoles(List.of(BOSS, OFFICE));

        assertThat(sut.findAll()).extracting(CompanyCalendar::getPerson).containsExactlyInAnyOrder(boss, office);
    }
}
