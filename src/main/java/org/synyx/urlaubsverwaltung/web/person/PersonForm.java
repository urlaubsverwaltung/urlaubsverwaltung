
package org.synyx.urlaubsverwaltung.web.person;


import lombok.Data;
import lombok.NonNull;
import org.joda.time.DateMidnight;
import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.calendar.Day;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.mail.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.security.Role;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.security.Role.INACTIVE;


/**
 * View class representing a person with a holidays account.
 *
 * @author  Aljona Murygina
 */
@Data
public class PersonForm {

    private Integer id;

    private String loginName;

    private String lastName;

    private String firstName;

    private String email;

    private int holidaysAccountYear;

    private DateMidnight holidaysAccountValidFrom;

    private DateMidnight holidaysAccountValidTo;

    private BigDecimal annualVacationDays;

    private BigDecimal remainingVacationDays;

    private BigDecimal remainingVacationDaysNotExpiring;

    private DateMidnight validFrom;

    private List<Integer> workingDays = new ArrayList<>();

    private List<Role> permissions = new ArrayList<>();

    private List<MailNotification> notifications = new ArrayList<>();

    public PersonForm() {

        this(DateMidnight.now().getYear());
    }


    public PersonForm(int year) {

        this.holidaysAccountYear = year;
        this.holidaysAccountValidFrom = DateUtil.getFirstDayOfYear(year);
        this.holidaysAccountValidTo = DateUtil.getLastDayOfYear(year);
    }


    public PersonForm(@NonNull Person person, int year, Optional<Account> holidaysAccountOptional,
        Optional<WorkingTime> workingTimeOptional, Collection<Role> roles, Collection<MailNotification> notifications) {

        this.id = person.getId();
        this.loginName = person.getLoginName();
        this.lastName = person.getLastName();
        this.firstName = person.getFirstName();
        this.email = person.getEmail();

        if (holidaysAccountOptional.isPresent()) {
            Account holidaysAccount = holidaysAccountOptional.get();

            this.holidaysAccountYear = holidaysAccount.getValidFrom().getYear();
            this.holidaysAccountValidFrom = holidaysAccount.getValidFrom();
            this.holidaysAccountValidTo = holidaysAccount.getValidTo();
            this.annualVacationDays = holidaysAccount.getAnnualVacationDays();
            this.remainingVacationDays = holidaysAccount.getRemainingVacationDays();
            this.remainingVacationDaysNotExpiring = holidaysAccount.getRemainingVacationDaysNotExpiring();
        } else {
            this.holidaysAccountYear = year;
            this.holidaysAccountValidFrom = DateUtil.getFirstDayOfYear(year);
            this.holidaysAccountValidTo = DateUtil.getLastDayOfYear(year);
        }

        if (workingTimeOptional.isPresent()) {
            WorkingTime workingTime = workingTimeOptional.get();

            for (Day day : Day.values()) {
                Integer dayOfWeek = day.getDayOfWeek();

                DayLength dayLength = workingTime.getDayLengthForWeekDay(dayOfWeek);

                if (dayLength != DayLength.ZERO) {
                    workingDays.add(dayOfWeek);
                }
            }

            this.validFrom = workingTime.getValidFrom();
        }

        this.permissions = new ArrayList<>(roles);
        this.notifications = new ArrayList<>(notifications);
    }

    public Person generatePerson() {

        Person person = new Person();
        fillPersonAttributes(person);

        return person;
    }


    public void fillPersonAttributes(Person person) {

        person.setLoginName(loginName);
        person.setLastName(lastName);
        person.setFirstName(firstName);
        person.setEmail(email);

        person.setNotifications(notifications);

        if (personShouldBeSetToInactive(permissions)) {
            List<Role> onlyInactive = new ArrayList<>();
            onlyInactive.add(INACTIVE);
            person.setPermissions(onlyInactive);
        } else {
            person.setPermissions(permissions);
        }
    }


    private boolean personShouldBeSetToInactive(Collection<Role> permissions) {

        return permissions.stream().
                filter(permission -> permission.equals(INACTIVE)).
                findFirst().
                isPresent();
    }
}
