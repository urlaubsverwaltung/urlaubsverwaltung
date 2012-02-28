/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.view;

import org.synyx.urlaubsverwaltung.domain.Person;


/**
 * @author  Aljona Murygina
 */
public class PersonForm {

    private String lastName;

    private String firstName;

    private String email;

    private String year;

    private String annualVacationDaysEnt;

    private String vacationDaysEnt;

    private String remainingVacationDaysEnt;

    private String vacationDaysAcc;

    private String remainingVacationDaysAcc;

    private boolean remainingVacationDaysExpireAcc;

    public PersonForm() {
    }


    public PersonForm(Person person, String year, String annualDaysEnt, String daysEnt, String remainingEnt,
        String daysAcc, String remainingAcc, boolean expiring) {

        this.lastName = person.getLastName();
        this.firstName = person.getFirstName();
        this.email = person.getEmail();
        this.year = year;
        this.remainingVacationDaysExpireAcc = expiring;

        if (annualDaysEnt != null) {
            this.annualVacationDaysEnt = annualDaysEnt;
        }

        if (daysEnt != null) {
            this.vacationDaysEnt = daysEnt;
        }

        if (remainingEnt != null) {
            this.remainingVacationDaysEnt = remainingEnt;
        }

        if (daysAcc != null) {
            this.vacationDaysAcc = daysAcc;
        }

        if (remainingAcc != null) {
            this.remainingVacationDaysAcc = remainingAcc;
        }
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


    public String getRemainingVacationDaysAcc() {

        return remainingVacationDaysAcc;
    }


    public void setRemainingVacationDaysAcc(String remainingVacationDaysAcc) {

        this.remainingVacationDaysAcc = remainingVacationDaysAcc;
    }


    public String getRemainingVacationDaysEnt() {

        return remainingVacationDaysEnt;
    }


    public void setRemainingVacationDaysEnt(String remainingVacationDaysEnt) {

        this.remainingVacationDaysEnt = remainingVacationDaysEnt;
    }


    public boolean isRemainingVacationDaysExpireAcc() {

        return remainingVacationDaysExpireAcc;
    }


    public void setRemainingVacationDaysExpireAcc(boolean remainingVacationDaysExpireAcc) {

        this.remainingVacationDaysExpireAcc = remainingVacationDaysExpireAcc;
    }


    public String getVacationDaysAcc() {

        return vacationDaysAcc;
    }


    public void setVacationDaysAcc(String vacationDaysAcc) {

        this.vacationDaysAcc = vacationDaysAcc;
    }


    public String getVacationDaysEnt() {

        return vacationDaysEnt;
    }


    public void setVacationDaysEnt(String vacationDaysEnt) {

        this.vacationDaysEnt = vacationDaysEnt;
    }


    public String getAnnualVacationDaysEnt() {

        return annualVacationDaysEnt;
    }


    public void setAnnualVacationDaysEnt(String annualVacationDaysEnt) {

        this.annualVacationDaysEnt = annualVacationDaysEnt;
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
