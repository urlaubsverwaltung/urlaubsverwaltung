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
public class HolidaysAccount extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = getSerialVersionUID();

    // One person may have multiple accounts (depending on years)
    @ManyToOne
    private Person person;

    // Year of account
    private Integer year;

    // Residual number of leave days that person has
    private Double vacationDays;

    // Residual number of remaining days of vacation that person has
    private Double remainingVacationDays;

    // Number of days that person has used this year
    private Double usedVacationDays;

    // Number of days of special leave used this year
    private Double sonderUrlaub;

    // Number of days of unpaid leave used this year
    private Double unbezahlterUrlaub;

    public static long getSerialVersionUID() {

        return serialVersionUID;
    }


    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public Double getRemainingVacationDayss() {

        return remainingVacationDays;
    }


    public void setRemainingVacationDays(Double remainingVacationDays) {

        this.remainingVacationDays = remainingVacationDays;
    }


    public Double getSonderUrlaub() {

        return sonderUrlaub;
    }


    public void setSonderUrlaub(Double sonderUrlaub) {

        this.sonderUrlaub = sonderUrlaub;
    }


    public Double getUnbezahlterUrlaub() {

        return unbezahlterUrlaub;
    }


    public void setUnbezahlterUrlaub(Double unbezahlterUrlaub) {

        this.unbezahlterUrlaub = unbezahlterUrlaub;
    }


    public Double getUsedVacationDays() {

        return usedVacationDays;
    }


    public void setUsedVacationDays(Double usedVacationDays) {

        this.usedVacationDays = usedVacationDays;
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
