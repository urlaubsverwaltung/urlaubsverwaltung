package org.synyx.urlaubsverwaltung.account.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;

/**
 * This class describes how many vacation days and remaining vacation days a person has in which period (validFrom, validTo).
 */
@Entity
public class Account extends AbstractPersistable<Integer> {

    @ManyToOne
    private Person person;

    private Instant validFrom;

    private Instant validTo;

    // theoretical number of vacation days a person has, i.e. it's the annual entitlement, but it is possible that
    // person e.g. will quit soon the company so he has not the full holidays entitlement; the actual number of vacation
    // days for a year describes the field vacationDays
    private BigDecimal annualVacationDays;
    private BigDecimal vacationDays;

    // remaining vacation days from the last year, if it's after 1st April, only the not expiring remaining vacation
    // days may be used

    private BigDecimal remainingVacationDays;
    private BigDecimal remainingVacationDaysNotExpiring;

    private String comment;

    public Account() {

        /* OK */
    }

    public Account(Person person, Instant validFrom, Instant validTo, BigDecimal annualVacationDays,
                   BigDecimal remainingVacationDays, BigDecimal remainingVacationDaysNotExpiring, String comment) {

        this.person = person;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.annualVacationDays = annualVacationDays;
        this.remainingVacationDays = remainingVacationDays;
        this.remainingVacationDaysNotExpiring = remainingVacationDaysNotExpiring;
        this.comment = comment;
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

    public BigDecimal getRemainingVacationDaysNotExpiring() {

        return remainingVacationDaysNotExpiring;
    }

    public void setRemainingVacationDaysNotExpiring(BigDecimal remainingVacationDaysNotExpiring) {

        this.remainingVacationDaysNotExpiring = remainingVacationDaysNotExpiring;
    }

    public BigDecimal getVacationDays() {

        return vacationDays;
    }

    public void setVacationDays(BigDecimal vacationDays) {

        this.vacationDays = vacationDays;
    }

    public Instant getValidFrom() {

        if (this.validFrom == null) {
            return null;
        }

        return this.validFrom;
    }

    public void setValidFrom(Instant validFrom) {

        this.validFrom = validFrom;
    }

    public Instant getValidTo() {

        if (this.validTo == null) {
            return null;
        }

        return this.validTo;
    }

    public void setValidTo(Instant validTo) {

        this.validTo = validTo;
    }

    public int getYear() {

        return Year.from(validFrom).getValue();
    }

    @Override
    public String toString() {
        return "Account{" +
            "person=" + person +
            ", validFrom=" + validFrom +
            ", validTo=" + validTo +
            ", annualVacationDays=" + annualVacationDays +
            ", vacationDays=" + vacationDays +
            ", remainingVacationDays=" + remainingVacationDays +
            ", remainingVacationDaysNotExpiring=" + remainingVacationDaysNotExpiring +
            '}';
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
