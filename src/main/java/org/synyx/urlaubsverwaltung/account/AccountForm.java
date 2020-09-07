package org.synyx.urlaubsverwaltung.account;

import org.synyx.urlaubsverwaltung.util.DateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;

public class AccountForm {

    private int holidaysAccountYear;
    private LocalDate holidaysAccountValidFrom;
    private LocalDate holidaysAccountValidTo;
    private BigDecimal annualVacationDays;
    private BigDecimal actualVacationDays;
    private BigDecimal remainingVacationDays;
    private BigDecimal remainingVacationDaysNotExpiring;
    private String comment;

    AccountForm() {
        this(ZonedDateTime.now(UTC).getYear());
    }

    AccountForm(int year) {
        this.holidaysAccountYear = year;
        this.holidaysAccountValidFrom = DateUtil.getFirstDayOfYear(year);
        this.holidaysAccountValidTo = DateUtil.getLastDayOfYear(year);
    }

    AccountForm(int year, Optional<Account> holidaysAccountOptional) {

        if (holidaysAccountOptional.isPresent()) {
            Account holidaysAccount = holidaysAccountOptional.get();

            this.holidaysAccountYear = holidaysAccount.getValidFrom().getYear();
            this.holidaysAccountValidFrom = holidaysAccount.getValidFrom();
            this.holidaysAccountValidTo = holidaysAccount.getValidTo();
            this.annualVacationDays = holidaysAccount.getAnnualVacationDays();
            this.actualVacationDays = holidaysAccount.getVacationDays();
            this.remainingVacationDays = holidaysAccount.getRemainingVacationDays();
            this.remainingVacationDaysNotExpiring = holidaysAccount.getRemainingVacationDaysNotExpiring();
            this.comment = holidaysAccount.getComment();
        } else {
            this.holidaysAccountYear = year;
            this.holidaysAccountValidFrom = DateUtil.getFirstDayOfYear(year);
            this.holidaysAccountValidTo = DateUtil.getLastDayOfYear(year);
        }
    }

    public int getHolidaysAccountYear() {
        return holidaysAccountYear;
    }

    public void setHolidaysAccountYear(int holidaysAccountYear) {
        this.holidaysAccountYear = holidaysAccountYear;
    }

    public LocalDate getHolidaysAccountValidFrom() {
        return holidaysAccountValidFrom;
    }

    public void setHolidaysAccountValidFrom(LocalDate holidaysAccountValidFrom) {
        this.holidaysAccountValidFrom = holidaysAccountValidFrom;
    }

    public LocalDate getHolidaysAccountValidTo() {
        return holidaysAccountValidTo;
    }

    public void setHolidaysAccountValidTo(LocalDate holidaysAccountValidTo) {
        this.holidaysAccountValidTo = holidaysAccountValidTo;
    }

    public BigDecimal getAnnualVacationDays() {
        return annualVacationDays;
    }

    public void setAnnualVacationDays(BigDecimal annualVacationDays) {
        this.annualVacationDays = annualVacationDays;
    }

    public BigDecimal getActualVacationDays() {
        return actualVacationDays;
    }

    public void setActualVacationDays(BigDecimal actualVacationDays) {
        this.actualVacationDays = actualVacationDays;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
