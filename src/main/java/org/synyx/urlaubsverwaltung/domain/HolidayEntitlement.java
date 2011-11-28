/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Entity
public class HolidayEntitlement extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = getSerialVersionUID();

    // One person may have multiple entitlements (depending on years)
    @ManyToOne
    private Person person;

    private Double vacationDays;

    private Integer year;

    public static long getSerialVersionUID() {

        return serialVersionUID;
    }


    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public Double getVacationDays() {

        return vacationDays;
    }


    public void setVacationDays(Double vacationDays) {

        this.vacationDays = vacationDays;
    }


    public Integer getYear() {

        return year;
    }


    public void setYear(Integer year) {

        this.year = year;
    }
}
