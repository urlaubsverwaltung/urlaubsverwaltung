package org.synyx.urlaubsverwaltung.dev;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.synyx.urlaubsverwaltung.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static org.synyx.urlaubsverwaltung.period.WeekDay.FRIDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.MONDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.THURSDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.TUESDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.WEDNESDAY;
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
 * Provides person test data.
 */
class PersonDataProvider {

    private final PersonService personService;
    private final WorkingTimeService workingTimeService;
    private final AccountInteractionService accountInteractionService;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    PersonDataProvider(PersonService personService, WorkingTimeService workingTimeService,
                       AccountInteractionService accountInteractionService, PasswordEncoder passwordEncoder, Clock clock) {

        this.personService = personService;
        this.workingTimeService = workingTimeService;
        this.accountInteractionService = accountInteractionService;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    boolean isPersonAlreadyCreated(String username) {

        final Optional<Person> personByUsername = personService.getPersonByUsername(username);
        return personByUsername.isPresent();
    }

    Person createTestPerson(TestUser testUser, String firstName, String lastName, String email) {

        final String username = testUser.getUsername();
        final String password = testUser.getPassword();
        final Role[] roles = testUser.getRoles();

        return createTestPerson(username, password, firstName, lastName, email, roles);
    }

    Person createTestPerson(String username, String password, String firstName, String lastName, String email, Role... roles) {


        final Optional<Person> personByUsername = personService.getPersonByUsername(username);
        if (personByUsername.isPresent()) {
            return personByUsername.get();
        }

        final List<Role> permissions = asList(roles);
        final List<MailNotification> notifications = getNotificationsForRoles(permissions);

        final Person person = personService.create(username, lastName, firstName, email, notifications, permissions);
        person.setPassword(passwordEncoder.encode(password));

        final Person savedPerson = personService.save(person);

        final int currentYear = Year.now(clock).getValue();
        workingTimeService.touch(
            asList(MONDAY.getDayOfWeek(), TUESDAY.getDayOfWeek(), WEDNESDAY.getDayOfWeek(), THURSDAY.getDayOfWeek(), FRIDAY.getDayOfWeek()),
            Optional.empty(), LocalDate.of(currentYear - 1, 1, 1), savedPerson);

        final LocalDate firstDayOfYear = DateUtil.getFirstDayOfYear(currentYear);
        final LocalDate lastDayOfYear = DateUtil.getLastDayOfYear(currentYear);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear,
            lastDayOfYear, new BigDecimal("30"), new BigDecimal("30"), new BigDecimal("5"),
            ZERO, null);

        return savedPerson;
    }

    private List<MailNotification> getNotificationsForRoles(List<Role> roles) {

        List<MailNotification> notifications = new ArrayList<>();

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
