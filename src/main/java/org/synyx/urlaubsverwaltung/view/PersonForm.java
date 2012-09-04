
package org.synyx.urlaubsverwaltung.view;

import org.joda.time.DateMidnight;
import org.synyx.urlaubsverwaltung.domain.Account;
import org.synyx.urlaubsverwaltung.domain.Person;


/**
 * @author  Aljona Murygina
 */
public class PersonForm {

    private String lastName;

    private String firstName;

    private String email;
    
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

    public PersonForm(Person person, Account account, String annualVacationDays, String remainingVacationDays, boolean remainingVacationDaysExpire) {
        
        this.lastName = person.getLastName();
        this.firstName = person.getFirstName();
        this.email = person.getEmail();
        
        if(account != null) {
        this.dayFrom = String.valueOf(account.getValidFrom().getDayOfMonth());
        this.monthFrom = String.valueOf(account.getValidFrom().getMonthOfYear());
        this.dayTo = String.valueOf(account.getValidTo().getDayOfMonth());
        this.monthTo = String.valueOf(account.getValidTo().getMonthOfYear());
        this.year = String.valueOf(account.getYear());
        this.annualVacationDays = annualVacationDays;
        this.remainingVacationDays = remainingVacationDays;
        this.remainingVacationDaysExpire = remainingVacationDaysExpire;
        } else {
            this.year = String.valueOf(DateMidnight.now().getYear());
        }
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


    public Person fillPersonObject(Person person) {

        person.setLastName(this.lastName);
        person.setFirstName(this.firstName);
        person.setEmail(this.email);

        return person;
    }
}
