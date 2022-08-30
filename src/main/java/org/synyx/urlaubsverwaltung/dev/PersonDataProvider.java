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
import java.util.ArrayList;
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
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.MailNotification.OVERTIME_NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

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

    boolean isPersonAlreadyCreated(String username) {

        final Optional<Person> personByUsername = personService.getPersonByUsername(username);
        return personByUsername.isPresent();
    }

    Person createTestPerson(DemoUser demoUser, int personnelNumber, String firstName, String lastName, String email) {

        final String username = demoUser.getUsername();
        final String passwordHash = demoUser.getPasswordHash();
        final Role[] roles = demoUser.getRoles();

        return createTestPerson(username, personnelNumber, passwordHash, firstName, lastName, email, roles);
    }

    Person createTestPerson(String username, int personnelNumber, String passwordHash, String firstName, String lastName, String email, Role... roles) {

        final Optional<Person> personByUsername = personService.getPersonByUsername(username);
        if (personByUsername.isPresent()) {
            return personByUsername.get();
        }

        final List<Role> permissions = asList(roles);
        final List<MailNotification> notifications = getNotificationsForRoles(permissions);

        final Person person = new Person(username, lastName, firstName, email);
        person.setPermissions(permissions);
        person.setNotifications(notifications);
        person.setPassword(passwordHash);
        final Person savedPerson = personService.create(person);
        personBasedataService.update(new PersonBasedata(new PersonId(person.getId()), String.valueOf(personnelNumber), ""));

        final int currentYear = Year.now(clock).getValue();
        final LocalDate firstDayOfYear = Year.of(currentYear).atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final List<Integer> workingDays = Stream.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY).map(DayOfWeek::getValue).collect(toList());
        workingTimeWriteService.touch(workingDays, firstDayOfYear.minusYears(1), savedPerson);

        accountInteractionService.updateOrCreateHolidaysAccount(
            savedPerson,
            firstDayOfYear,
            lastDayOfYear,
            true,
            LocalDate.of(currentYear, APRIL, 1),
            BigDecimal.valueOf(30),
            BigDecimal.valueOf(30),
            BigDecimal.valueOf(5),
            ZERO,
            null);

        return savedPerson;
    }

    private List<MailNotification> getNotificationsForRoles(List<Role> roles) {

        final List<MailNotification> notifications = new ArrayList<>();
        notifications.add(NOTIFICATION_USER);

        if (roles.contains(DEPARTMENT_HEAD)) {
            notifications.add(NOTIFICATION_DEPARTMENT_HEAD);
        }
        if (roles.contains(SECOND_STAGE_AUTHORITY)) {
            notifications.add(NOTIFICATION_SECOND_STAGE_AUTHORITY);
        }
        if (roles.contains(BOSS)) {
            notifications.add(NOTIFICATION_BOSS_ALL);
        }
        if (roles.contains(OFFICE)) {
            notifications.add(NOTIFICATION_OFFICE);
            notifications.add(OVERTIME_NOTIFICATION_OFFICE);
        }

        return notifications;
    }
}
