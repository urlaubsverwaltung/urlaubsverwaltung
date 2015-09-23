package org.synyx.urlaubsverwaltung.core.account.domain;

import lombok.Data;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.synyx.urlaubsverwaltung.core.person.Person;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import java.math.BigDecimal;
import java.util.Date;


/**
 * This class describes how many vacation days and remaining vacation days a person has in which period (validFrom,
 * validTo).
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
@Entity
@Data
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

    public Account() {

        /* OK */
    }


    public Account(Person person, Date validFrom, Date validTo, BigDecimal annualVacationDays,
                   BigDecimal remainingVacationDays, BigDecimal remainingVacationDaysNotExpiring) {

        this.person = person;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.annualVacationDays = annualVacationDays;
        this.remainingVacationDays = remainingVacationDays;
        this.remainingVacationDaysNotExpiring = remainingVacationDaysNotExpiring;
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
