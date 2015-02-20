
package org.synyx.urlaubsverwaltung.web.person;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.core.account.Account;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.calendar.Day;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.mail.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.util.NumberUtil;
import org.synyx.urlaubsverwaltung.security.Role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;


/**
 * View class representing a person and its account.
 *
 * @author  Aljona Murygina
 */
public class PersonForm {

    private String loginName;

    private String lastName;

    private String firstName;

    private String email;

    private boolean active = true;

    private String dayFrom;

    private String monthFrom;

    private String dayTo;

    private String monthTo;

    private String year;

    private String annualVacationDays;

    private String remainingVacationDays;

    private String remainingVacationDaysNotExpiring;

    private DateMidnight validFrom;

    private List<Integer> workingDays = new ArrayList<>();

    private List<Role> permissions = new ArrayList<>();

    private List<MailNotification> notifications = new ArrayList<>();

    private Locale locale;

    public PersonForm() {

        setDefaultValuesForValidity();
    }


    public PersonForm(Person person, String year, Account account, WorkingTime workingTime, Collection<Role> roles,
        Collection<MailNotification> notifications, Locale locale) {

        this.loginName = person.getLoginName();
        this.lastName = person.getLastName();
        this.firstName = person.getFirstName();
        this.email = person.getEmail();
        this.year = year;
        this.locale = locale;

        if (account != null) {
            this.dayFrom = String.valueOf(account.getValidFrom().getDayOfMonth());
            this.monthFrom = String.valueOf(account.getValidFrom().getMonthOfYear());
            this.dayTo = String.valueOf(account.getValidTo().getDayOfMonth());
            this.monthTo = String.valueOf(account.getValidTo().getMonthOfYear());
            this.annualVacationDays = NumberUtil.formatNumber(account.getAnnualVacationDays(), locale);
            this.remainingVacationDays = NumberUtil.formatNumber(account.getRemainingVacationDays(), locale);
            this.remainingVacationDaysNotExpiring = NumberUtil.formatNumber(
                    account.getRemainingVacationDaysNotExpiring(), locale);
        } else {
            setDefaultValuesForValidity();
        }

        this.workingDays = new ArrayList<>();

        if (workingTime != null) {
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

    private void setDefaultValuesForValidity() {

        // default values for validFrom and validTo: 1.1. - 31.12.
        this.dayFrom = String.valueOf(1);
        this.monthFrom = String.valueOf(1);
        this.dayTo = String.valueOf(31);
        this.monthTo = String.valueOf(12);
    }


    public boolean isActive() {

        return active;
    }


    public void setActive(boolean active) {

        this.active = active;
    }


    public String getAnnualVacationDays() {

        return annualVacationDays;
    }


    public void setAnnualVacationDays(String annualVacationDays) {

        this.annualVacationDays = annualVacationDays;
    }


    public String getDayFrom() {

        return dayFrom;
    }


    public void setDayFrom(String dayFrom) {

        this.dayFrom = dayFrom;
    }


    public String getDayTo() {

        return dayTo;
    }


    public void setDayTo(String dayTo) {

        this.dayTo = dayTo;
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


    public String getMonthFrom() {

        return monthFrom;
    }


    public void setMonthFrom(String monthFrom) {

        this.monthFrom = monthFrom;
    }


    public String getMonthTo() {

        return monthTo;
    }


    public void setMonthTo(String monthTo) {

        this.monthTo = monthTo;
    }


    public String getRemainingVacationDays() {

        return remainingVacationDays;
    }


    public void setRemainingVacationDays(String remainingVacationDays) {

        this.remainingVacationDays = remainingVacationDays;
    }


    public String getRemainingVacationDaysNotExpiring() {

        return remainingVacationDaysNotExpiring;
    }


    public void setRemainingVacationDaysNotExpiring(String remainingVacationDaysNotExpiring) {

        this.remainingVacationDaysNotExpiring = remainingVacationDaysNotExpiring;
    }


    public String getYear() {

        return year;
    }


    public void setYear(String year) {

        this.year = year;
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


    public Locale getLocale() {

        return locale;
    }


    public void setLocale(Locale locale) {

        this.locale = locale;
    }


    public void fillPersonAttributes(Person person) {

        person.setLoginName(loginName);
        person.setLastName(lastName);
        person.setFirstName(firstName);
        person.setEmail(email);

        person.setNotifications(notifications);

        if (personShouldBeSetToInactive(permissions)) {
            person.setActive(false);

            List<Role> onlyInactive = new ArrayList<>();
            onlyInactive.add(Role.INACTIVE);
            person.setPermissions(onlyInactive);
        } else {
            person.setActive(true);
            person.setPermissions(permissions);
        }
    }


    private boolean personShouldBeSetToInactive(Collection<Role> permissions) {

        Optional<Role> shouldBeInactiveOptional = Iterables.tryFind(permissions, new Predicate<Role>() {

                    @Override
                    public boolean apply(Role role) {

                        return role.equals(Role.INACTIVE);
                    }
                });

        return shouldBeInactiveOptional.isPresent();
    }
}
