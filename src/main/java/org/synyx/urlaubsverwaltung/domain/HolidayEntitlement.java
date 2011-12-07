/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Entity
public class HolidayEntitlement extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 89043562784389L;

    // One person may have multiple entitlements (depending on years)
    @ManyToOne
    private Person person;

    // Determined number of vacation days a person may use this year
    private BigDecimal vacationDays;

    // Determined number of remaining vacation days a person may use this year (until 31st March of a year)
    // is set on 1st January of a year
    private BigDecimal remainingVacationDays;

    private int year;

    private boolean active;

    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
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


    public int getYear() {

        return year;
    }


    public void setYear(int year) {

        this.year = year;
    }


    public boolean isActive() {

        return active;
    }


    public void setActive(boolean active) {

        this.active = active;
    }
}
