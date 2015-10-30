
package org.synyx.urlaubsverwaltung.web.person;

import org.joda.time.DateMidnight;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.Day;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.core.person.Role.INACTIVE;


/**
 * View class representing a person with a holidays account.
 *
 * @author  Aljona Murygina
 */
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


    public PersonForm(Person person, int year, Optional<Account> holidaysAccountOptional,
        Optional<WorkingTime> workingTimeOptional, Collection<Role> roles, Collection<MailNotification> notifications) {

        Assert.notNull(person, "Person must not be null");

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

    public Integer getId() {

        return id;
    }


    public void setId(Integer id) {

        this.id = id;
    }


    public int getHolidaysAccountYear() {

        return holidaysAccountYear;
    }


    public void setHolidaysAccountYear(int holidaysAccountYear) {

        this.holidaysAccountYear = holidaysAccountYear;
    }


    public BigDecimal getAnnualVacationDays() {

        return annualVacationDays;
    }


    public void setAnnualVacationDays(BigDecimal annualVacationDays) {

        this.annualVacationDays = annualVacationDays;
    }


    public DateMidnight getHolidaysAccountValidFrom() {

        return holidaysAccountValidFrom;
    }


    public void setHolidaysAccountValidFrom(DateMidnight holidaysAccountValidFrom) {

        this.holidaysAccountValidFrom = holidaysAccountValidFrom;
    }


    public DateMidnight getHolidaysAccountValidTo() {

        return holidaysAccountValidTo;
    }


    public void setHolidaysAccountValidTo(DateMidnight holidaysAccountValidTo) {

        this.holidaysAccountValidTo = holidaysAccountValidTo;
    }


    public String getEmail() {

        return email;
    }


    public void setEmail(String email) {

        this.email = email;
    }


    public String getFirstName() {

        return firstName;
    }


    public void setFirstName(String firstName) {

        this.firstName = firstName;
    }


    public String getLastName() {

        return lastName;
    }


    public void setLastName(String lastName) {

        this.lastName = lastName;
    }


    public BigDecimal getRemainingVacationDays() {

        return remainingVacationDays;
    }


    public void setRemainingVacationDays(BigDecimal remainingVacationDays) {

        this.remainingVacationDays = remainingVacationDays;
    }


    public BigDecimal getRemainingVacationDaysNotExpiring() {

        return remainingVacationDaysNotExpiring;
    }


    public void setRemainingVacationDaysNotExpiring(BigDecimal remainingVacationDaysNotExpiring) {

        this.remainingVacationDaysNotExpiring = remainingVacationDaysNotExpiring;
    }


    public String getLoginName() {

        return loginName;
    }


    public void setLoginName(String loginName) {

        this.loginName = loginName;
    }


    public List<Integer> getWorkingDays() {

        return workingDays;
    }


    public void setWorkingDays(List<Integer> workingDays) {

        this.workingDays = workingDays;
    }


    public DateMidnight getValidFrom() {

        return validFrom;
    }


    public void setValidFrom(DateMidnight validFrom) {

        this.validFrom = validFrom;
    }


    public List<Role> getPermissions() {

        return permissions;
    }


    public void setPermissions(List<Role> permissions) {

        this.permissions = permissions;
    }


    public List<MailNotification> getNotifications() {

        return notifications;
    }


    public void setNotifications(List<MailNotification> notifications) {

        this.notifications = notifications;
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

        return permissions.stream().filter(permission -> permission.equals(INACTIVE)).findFirst().isPresent();
    }
}
