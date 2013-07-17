
package org.synyx.urlaubsverwaltung.person.web;

import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.person.Person;


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

    private boolean remainingVacationDaysExpire;

    public PersonForm() {
    }


    public PersonForm(Person person, String year, Account account, String annualVacationDays,
        String remainingVacationDays, boolean remainingVacationDaysExpire) {

        this.loginName = person.getLoginName();
        this.lastName = person.getLastName();
        this.firstName = person.getFirstName();
        this.email = person.getEmail();
        this.year = year;

        if (account != null) {
            this.dayFrom = String.valueOf(account.getValidFrom().getDayOfMonth());
            this.monthFrom = String.valueOf(account.getValidFrom().getMonthOfYear());
            this.dayTo = String.valueOf(account.getValidTo().getDayOfMonth());
            this.monthTo = String.valueOf(account.getValidTo().getMonthOfYear());
            this.annualVacationDays = annualVacationDays;
            this.remainingVacationDays = remainingVacationDays;
            this.remainingVacationDaysExpire = remainingVacationDaysExpire;
        } else {
            // default values for validFrom and validTo: 1.1. - 31.12.
            this.dayFrom = String.valueOf(1);
            this.monthFrom = String.valueOf(1);
            this.dayTo = String.valueOf(31);
            this.monthTo = String.valueOf(12);
        }
    }

    public void setDefaultValuesForValidity() {

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


    public boolean isRemainingVacationDaysExpire() {

        return remainingVacationDaysExpire;
    }


    public void setRemainingVacationDaysExpire(boolean remainingVacationDaysExpire) {

        this.remainingVacationDaysExpire = remainingVacationDaysExpire;
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


    public Person fillPersonObject(Person person) {

        person.setLoginName(this.loginName);
        person.setLastName(this.lastName);
        person.setFirstName(this.firstName);
        person.setEmail(this.email);
        person.setActive(this.active);

        return person;
    }
}
