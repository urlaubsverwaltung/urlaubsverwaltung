/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.view;

import org.synyx.urlaubsverwaltung.domain.Person;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina
 */
public class PersonForm {

    private String lastName;

    private String firstName;

    private String email;

    private String year;

    private BigDecimal vacationDaysEnt;

    private BigDecimal remainingVacationDaysEnt;

    private BigDecimal vacationDaysAcc;

    private BigDecimal remainingVacationDaysAcc;

    private boolean remainingVacationDaysExpireAcc;

    public PersonForm() {
    }


    public PersonForm(Person person, String year, BigDecimal daysEnt, BigDecimal remainingEnt, BigDecimal daysAcc,
        BigDecimal remainingAcc, boolean expiring) {

        this.lastName = person.getLastName();
        this.firstName = person.getFirstName();
        this.email = person.getEmail();
        this.year = year;
        this.remainingVacationDaysExpireAcc = expiring;

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


    public BigDecimal getRemainingVacationDaysAcc() {

        return remainingVacationDaysAcc;
    }


    public void setRemainingVacationDaysAcc(BigDecimal remainingVacationDaysAcc) {

        this.remainingVacationDaysAcc = remainingVacationDaysAcc;
    }


    public BigDecimal getRemainingVacationDaysEnt() {

        return remainingVacationDaysEnt;
    }


    public void setRemainingVacationDaysEnt(BigDecimal remainingVacationDaysEnt) {

        this.remainingVacationDaysEnt = remainingVacationDaysEnt;
    }


    public boolean isRemainingVacationDaysExpireAcc() {

        return remainingVacationDaysExpireAcc;
    }


    public void setRemainingVacationDaysExpireAcc(boolean remainingVacationDaysExpireAcc) {

        this.remainingVacationDaysExpireAcc = remainingVacationDaysExpireAcc;
    }


    public BigDecimal getVacationDaysAcc() {

        return vacationDaysAcc;
    }


    public void setVacationDaysAcc(BigDecimal vacationDaysAcc) {

        this.vacationDaysAcc = vacationDaysAcc;
    }


    public BigDecimal getVacationDaysEnt() {

        return vacationDaysEnt;
    }


    public void setVacationDaysEnt(BigDecimal vacationDaysEnt) {

        this.vacationDaysEnt = vacationDaysEnt;
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
