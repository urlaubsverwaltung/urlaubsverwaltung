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

    public PersonForm() {
    }


    public PersonForm(Person person, String year, BigDecimal days) {

        this.lastName = person.getLastName();
        this.firstName = person.getFirstName();
        this.email = person.getEmail();
        this.year = year;

        if (days != null) {
            this.vacationDays = days;
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
