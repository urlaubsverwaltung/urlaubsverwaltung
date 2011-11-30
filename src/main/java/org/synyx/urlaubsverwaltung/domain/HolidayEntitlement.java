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

    private BigDecimal vacationDays;

    private Integer year;

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


    public Integer getYear() {

        return year;
    }


    public void setYear(Integer year) {

        this.year = year;
    }
}
