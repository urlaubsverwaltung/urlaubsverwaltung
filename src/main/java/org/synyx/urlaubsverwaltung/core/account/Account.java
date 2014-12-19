package org.synyx.urlaubsverwaltung.core.account;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;


/**
 * This class describes how many vacation days and remaining vacation days a person has in which period (validFrom,
 * validTo).
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Entity
public class Account extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 890434378423784389L;

    @ManyToOne
    private Person person;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date validFrom;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date validTo;

    // theoretical number of vacation days a person has, i.e. it's the annual entitlement, but it is possible that
    // person e.g. will quit soon the company so he has not the full holidays entitlement; the actual number of vacation
    // days for a year describes the field vacationDays
    private BigDecimal annualVacationDays;
    private BigDecimal vacationDays;
    private BigDecimal remainingVacationDays;

    // if true: remaining vacation days expire on 1st Apr.
    // if false: remaining vacation days don't expire and may be used even after Apr. (until Dec.)
    private boolean remainingVacationDaysExpire;

    public Account() {

        /* OK */
    }


    public Account(Person person, Date validFrom, Date validTo, BigDecimal annualVacationDays,
        BigDecimal remainingVacationDays, boolean remainingVacationDaysExpire) {

        this.person = person;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.annualVacationDays = annualVacationDays;
        this.remainingVacationDays = remainingVacationDays;
        this.remainingVacationDaysExpire = remainingVacationDaysExpire;
    }

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


    public DateMidnight getValidFrom() {

        if (this.validFrom == null) {
            return null;
        }

        return new DateTime(this.validFrom).toDateMidnight();
    }


    public void setValidFrom(DateMidnight validFrom) {

        if (validFrom == null) {
            this.validFrom = null;
        } else {
            this.validFrom = validFrom.toDate();
        }
    }


    public DateMidnight getValidTo() {

        if (this.validTo == null) {
            return null;
        }

        return new DateTime(this.validTo).toDateMidnight();
    }


    public void setValidTo(DateMidnight validTo) {

        if (validTo == null) {
            this.validTo = null;
        } else {
            this.validTo = validTo.toDate();
        }
    }


    public int getYear() {

        return new DateTime(this.validFrom).toDateMidnight().getYear();
    }
}
