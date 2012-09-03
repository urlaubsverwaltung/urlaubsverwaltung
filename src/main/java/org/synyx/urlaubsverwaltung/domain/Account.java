package org.synyx.urlaubsverwaltung.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * This class describes how many vacation days and remaining vacation days a person has in which period (validFrom, validTo).
 * 
 * @author Aljona Murygina - murygina@synyx.de
 */
public class Account extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 890434378423784389L;
    @ManyToOne
    private Person person;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date validFrom;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date validTo;
    private int year;
    // theoretical number of vacation days a person has, i.e. it's the annual entitlement, but it is possible that
    // person e.g. will quit soon the company so he has not the full holidays entitlement; the actual number of vacation
    // days for a year describes the field vacationDays
    private BigDecimal annualVacationDays;
    private BigDecimal vacationDays;
    private BigDecimal remainingVacationDays;
    // if true: remaining vacation days expire on 1st Apr.
    // if false: remaining vacation days don't expire and may be used even after Apr. (until Dec.)
    private boolean remainingVacationDaysExpire;

    public Account(Person person, Date validFrom, Date validTo, BigDecimal annualVacationDays, BigDecimal remainingVacationDays, boolean remainingVacationDaysExpire) {
        this.person = person;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.annualVacationDays = annualVacationDays;
        this.remainingVacationDays = remainingVacationDays;
        this.remainingVacationDaysExpire = remainingVacationDaysExpire;
    }

    /**
     * Method to calculate the actual vacation days: (months * annual vacation days) / months per year
     * e.g.: (5 months * 28 days)/12 = 11.6666 = 12
     * 
     * Please notice following rounding rules:
     * 11.1 --> 11.0
     * 11.3 --> 11.5
     * 11.6 --> 12.0
     */
    public void calculateActualVacationDays() {

        int months = Months.monthsBetween(new DateTime(this.validFrom).toDateMidnight(), new DateTime(this.validTo).toDateMidnight()).getMonths() + 1;

        double unroundedVacationDays = (months * this.annualVacationDays.doubleValue()) / 12;
        BigDecimal bd = new BigDecimal(unroundedVacationDays).setScale(2, RoundingMode.HALF_UP);

        String bdString = bd.toString();
        bdString = bdString.split("\\.")[1];
        Integer referenceValue = Integer.parseInt(bdString);
        BigDecimal days;

        // please notice: d.intValue() is an Integer, e.g. 11
        int bdIntValue = bd.intValue();

        if (referenceValue > 0 && referenceValue < 30) {

            days = new BigDecimal(bdIntValue);

        } else if (referenceValue >= 30 && referenceValue < 50) {

            days = new BigDecimal(bdIntValue + 0.5);

        } else if (referenceValue >= 50) {

            days = new BigDecimal(bdIntValue + 1);

        } else {
            // default fallback because I'm a scaredy cat
            days = new BigDecimal(unroundedVacationDays).setScale(2);
        }

        this.vacationDays = days;

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
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
