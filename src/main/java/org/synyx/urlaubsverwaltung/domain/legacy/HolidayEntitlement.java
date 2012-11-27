/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.domain.legacy;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.synyx.urlaubsverwaltung.domain.Person;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 *
 *          <p>This class describes the holidays entitlement that a person has for a whole year.</p>
 */
@Entity(name = "LegacyEntitlement")
public class HolidayEntitlement extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 89043562784389L;

    // a person may have multiple entitlements (depending on years)
    @ManyToOne
    private Person person;

    // theoretical number of vacation days a person has, i.e. it's the annual entitlement, but it is possible that
    // person e.g. will quit soon the company so he has not the full holidays entitlement; the actual number of vacation
    // days for a year describes the field vacationDays
    private BigDecimal annualVacationDays;

    // actual number of vacation days a person may use this year
    private BigDecimal vacationDays;

    // number of remaining vacation days a person may use this year (until 31st March of a year)
    // is set on 1st January of a year
    private BigDecimal remainingVacationDays;

    private int year;

    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public BigDecimal getAnnualVacationDays() {

        return annualVacationDays;
    }


    public void setAnnualVacationDays(BigDecimal annualVacationDays) {

        this.annualVacationDays = annualVacationDays;
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
}
