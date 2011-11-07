/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;


/**
 * @author  aljona
 */
@Entity
public class Urlaubskonto extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 1L;

    private Person person;

    private Integer vacationDays;

    private Integer restVacationDays;

    private Integer year;

    public Integer getRestVacationDays() {

        return restVacationDays;
    }


    public void setRestVacationDays(Integer restVacationDays) {

        this.restVacationDays = restVacationDays;
    }


    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public Integer getVacationDays() {

        return vacationDays;
    }


    public void setVacationDays(Integer vacationDays) {

        this.vacationDays = vacationDays;
    }


    public Integer getYear() {

        return year;
    }


    public void setYear(Integer year) {

        this.year = year;
    }
}
