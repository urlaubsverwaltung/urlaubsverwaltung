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

    private BigDecimal vacationDays;

    private BigDecimal remainingVacationDays;

    public PersonForm() {
    }


    public PersonForm(Person person, String year, BigDecimal days, BigDecimal remaining) {

        this.lastName = person.getLastName();
        this.firstName = person.getFirstName();
        this.email = person.getEmail();
        this.year = year;

        if (days != null) {
            this.vacationDays = days;
        }

        if (remaining != null) {
            this.remainingVacationDays = remaining;
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


    public BigDecimal getVacationDays() {

        return vacationDays;
    }


    public void setVacationDays(BigDecimal vacationDays) {

        this.vacationDays = vacationDays;
    }


    public BigDecimal getRemainingVacationDays() {

        return remainingVacationDays;
    }


    public void setRemainingVacationDays(BigDecimal remainingVacationDays) {

        this.remainingVacationDays = remainingVacationDays;
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
