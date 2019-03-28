package org.synyx.urlaubsverwaltung.dev;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.period.WeekDay;
import org.synyx.urlaubsverwaltung.core.person.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Provides person test data.
 */
@Component
@ConditionalOnProperty("testdata.create")
class PersonDataProvider {

    private final PersonService personService;
    private final WorkingTimeService workingTimeService;
    private final AccountInteractionService accountInteractionService;

    @Autowired
    PersonDataProvider(PersonService personService, WorkingTimeService workingTimeService,
                       AccountInteractionService accountInteractionService) {

        this.personService = personService;
        this.workingTimeService = workingTimeService;
        this.accountInteractionService = accountInteractionService;
    }

    Person createTestPerson(String login, String password, String firstName, String lastName, String email, Role... roles) {

        List<Role> permissions = Arrays.asList(roles);
        List<MailNotification> notifications = getNotificationsForRoles(permissions);

        Person person = personService.create(login, lastName, firstName, email, notifications, permissions);
        person.setPassword(new StandardPasswordEncoder().encode(password));
        personService.save(person);

        int currentYear = DateMidnight.now().getYear();
        workingTimeService.touch(
                Arrays.asList(WeekDay.MONDAY.getDayOfWeek(), WeekDay.TUESDAY.getDayOfWeek(),
                        WeekDay.WEDNESDAY.getDayOfWeek(), WeekDay.THURSDAY.getDayOfWeek(), WeekDay.FRIDAY.getDayOfWeek()),
                Optional.empty(), new DateMidnight(currentYear - 1, 1, 1), person);

        accountInteractionService.updateOrCreateHolidaysAccount(person, DateUtil.getFirstDayOfYear(currentYear),
                DateUtil.getLastDayOfYear(currentYear), new BigDecimal("30"), new BigDecimal("30"), new BigDecimal("5"),
                BigDecimal.ZERO, null);

        return person;
    }

    List<MailNotification> getNotificationsForRoles(List<Role> roles) {

        List<MailNotification> notifications = new ArrayList<>();

        notifications.add(MailNotification.NOTIFICATION_USER);

        if (roles.contains(Role.DEPARTMENT_HEAD)) {
            notifications.add(MailNotification.NOTIFICATION_DEPARTMENT_HEAD);
        }

        if (roles.contains(Role.SECOND_STAGE_AUTHORITY)) {
            notifications.add(MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY);
        }

        if (roles.contains(Role.BOSS)) {
            notifications.add(MailNotification.NOTIFICATION_BOSS);
        }

        if (roles.contains(Role.OFFICE)) {
            notifications.add(MailNotification.NOTIFICATION_OFFICE);
        }

        return notifications;
    }
}
