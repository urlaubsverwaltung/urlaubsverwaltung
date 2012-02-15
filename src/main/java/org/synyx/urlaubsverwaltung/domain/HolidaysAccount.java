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
 * @author  Aljona Murygina This class contains current values of vacation days that a person has.
 */
@Entity
public class HolidaysAccount extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 341278947843289L;

    // One person may have multiple accounts (depending on years)
    @ManyToOne
    private Person person;

    // Year of account
    private int year;

    // Current number of leave days that person has
    private BigDecimal vacationDays;

    // remainingVacationDays + remainingVacationDaysNotExpiring = entitlement's remainingVacationDays

    // Current number of remaining days of vacation that person has
    // will not be used anymore until 31st March of a year
    private BigDecimal remainingVacationDays;

    // if true: remaining vacation days expire on 1st Apr.
    // if false: remaining vacation days don't expire and may be used even after Apr. (until Dec.)
    private boolean remainingVacationDaysExpire;

    // Number of days of special leave used this year
    private BigDecimal specialLeave;

    // Number of days of unpaid leave used this year
    private BigDecimal unpaidLeave;

    // Number of taken days off because of overtime
    private BigDecimal overtime;

    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public BigDecimal getRemainingVacationDays() {

        return remainingVacationDays;
    }


    public void setRemainingVacationDays(BigDecimal remainingVacationDays) {

        this.remainingVacationDays = remainingVacationDays;
    }


    public boolean isRemainingVacationDaysExpire() {

        return remainingVacationDaysExpire;
    }


    public void setRemainingVacationDaysExpire(boolean remainingVacationDaysExpire) {

        this.remainingVacationDaysExpire = remainingVacationDaysExpire;
    }


    public BigDecimal getVacationDays() {

        return vacationDays;
    }


    public void setVacationDays(BigDecimal vacationDays) {

        this.vacationDays = vacationDays;
    }


    public int getYear() {

        return year;
    }


    public void setYear(int year) {

        this.year = year;
    }


    public BigDecimal getSpecialLeave() {

        return specialLeave;
    }


    public void setSpecialLeave(BigDecimal specialLeave) {

        this.specialLeave = specialLeave;
    }


    public BigDecimal getUnpaidLeave() {

        return unpaidLeave;
    }


    public void setUnpaidLeave(BigDecimal unpaidLeave) {

        this.unpaidLeave = unpaidLeave;
    }


    public BigDecimal getOvertime() {

        return overtime;
    }


    public void setOvertime(BigDecimal overtime) {

        this.overtime = overtime;
    }
}
