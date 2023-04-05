package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.Month.APRIL;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.stream.Collectors.toList;

/**
 * Provides person demo data.
 */
class PersonDataProvider {

    private final PersonService personService;
    private final PersonBasedataService personBasedataService;
    private final WorkingTimeWriteService workingTimeWriteService;
    private final AccountInteractionService accountInteractionService;
    private final Clock clock;

    PersonDataProvider(PersonService personService, PersonBasedataService personBasedataService, WorkingTimeWriteService workingTimeWriteService,
                       AccountInteractionService accountInteractionService, Clock clock) {
        this.personService = personService;
        this.personBasedataService = personBasedataService;
        this.workingTimeWriteService = workingTimeWriteService;
        this.accountInteractionService = accountInteractionService;
        this.clock = clock;
    }

    boolean isPersonAlreadyCreated(String email) {
        return personService.getPersonByMailAddress(email).isPresent();
    }

    Person createTestPerson(String username, int personnelNumber, String firstName, String lastName, String email, List<Role> permissions, List<MailNotification> notifications) {

        final Optional<Person> personByUsername = personService.getPersonByUsername(username);
        if (personByUsername.isPresent()) {
            return personByUsername.get();
        }

        final Person savedPerson = personService.create(username, lastName, firstName, email, notifications, permissions);
        personBasedataService.update(new PersonBasedata(new PersonId(savedPerson.getId()), String.valueOf(personnelNumber), ""));

        final int currentYear = Year.now(clock).getValue();
        final LocalDate firstDayOfYear = Year.of(currentYear).atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final List<Integer> workingDays = Stream.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY).map(DayOfWeek::getValue).collect(toList());
        workingTimeWriteService.touch(workingDays, firstDayOfYear.minusYears(1), savedPerson);

        accountInteractionService.updateOrCreateHolidaysAccount(
            savedPerson,
            firstDayOfYear,
            lastDayOfYear,
            null,
            LocalDate.of(currentYear, APRIL, 1),
            BigDecimal.valueOf(30),
            BigDecimal.valueOf(30),
            BigDecimal.valueOf(5),
            ZERO,
            null);

        return savedPerson;
    }
}
