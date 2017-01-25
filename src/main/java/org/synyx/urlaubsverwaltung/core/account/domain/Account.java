package org.synyx.urlaubsverwaltung.core.account.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.util.DateFormat;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

/**
 * This class describes how many vacation days and remaining vacation days a person has in which period (validFrom, validTo).
 *
 * @author Aljona Murygina - murygina@synyx.de
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

    // remaining vacation days from the last year, if it's after 1st April, only the not expiring remaining vacation
    // days may be used

    private BigDecimal remainingVacationDays;
    private BigDecimal remainingVacationDaysNotExpiring;

    private String comment;

    public Account() {

        /* OK */
    }

    public Account(Person person, Date validFrom, Date validTo, BigDecimal annualVacationDays,
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

    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            // NOSONAR - Formatting issues
            .append("person", getPerson().getLoginName())
            .append("validFrom", getValidFrom().toString(DateFormat.PATTERN))
            .append("validTo", getValidTo().toString(DateFormat.PATTERN))
            .append("annualVacationDays", getAnnualVacationDays()).append("vacationDays", getVacationDays())
            .append("remainingVacationDays", getRemainingVacationDays())
            .append("remainingVacationDaysNotExpiring", getRemainingVacationDaysNotExpiring())
            .append("comment", getComment()).toString();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
