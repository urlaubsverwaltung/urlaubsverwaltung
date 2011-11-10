/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;


/**
 * @author  aljona
 */
@Entity
public class Urlaubsanspruch extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 1L;

    // Eine Person kann mehrere Ansprueche besitzen (je nach Jahr)
    @ManyToOne
    private Person person;

    private Integer vacationDays;

    private Integer year;

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
