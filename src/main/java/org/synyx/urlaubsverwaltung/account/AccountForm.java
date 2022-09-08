package org.synyx.urlaubsverwaltung.account;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;

import static java.time.Month.APRIL;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YYYY;

public class AccountForm {

    private int holidaysAccountYear;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY})
    private LocalDate holidaysAccountValidFrom;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY})
    private LocalDate holidaysAccountValidTo;
    private Boolean doRemainingVacationDaysExpireLocally;
    private boolean doRemainingVacationDaysExpireGlobally;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY})
    private LocalDate expiryDate;
    private BigDecimal annualVacationDays;
    private BigDecimal actualVacationDays;
    private BigDecimal remainingVacationDays;
    private BigDecimal remainingVacationDaysNotExpiring;
    private String comment;

    private AccountForm() {
    }

    AccountForm(int year) {
        this.holidaysAccountYear = year;
        this.holidaysAccountValidFrom = Year.of(year).atDay(1);
        this.holidaysAccountValidTo = holidaysAccountValidFrom.with(lastDayOfYear());
        this.doRemainingVacationDaysExpireLocally = true;
        this.expiryDate = LocalDate.of(year, APRIL, 1);
    }

    AccountForm(Account holidaysAccount) {
        this.holidaysAccountYear = holidaysAccount.getValidFrom().getYear();
        this.holidaysAccountValidFrom = holidaysAccount.getValidFrom();
        this.holidaysAccountValidTo = holidaysAccount.getValidTo();
        this.doRemainingVacationDaysExpireLocally = holidaysAccount.isDoRemainingVacationDaysExpireLocally();
        this.doRemainingVacationDaysExpireGlobally = holidaysAccount.isDoRemainingVacationDaysExpireGlobally();
        this.expiryDate = holidaysAccount.getExpiryDate();
        this.annualVacationDays = holidaysAccount.getAnnualVacationDays();
        this.actualVacationDays = holidaysAccount.getActualVacationDays();
        this.remainingVacationDays = holidaysAccount.getRemainingVacationDays();
        this.remainingVacationDaysNotExpiring = holidaysAccount.getRemainingVacationDaysNotExpiring();
        this.comment = holidaysAccount.getComment();
    }

    public int getHolidaysAccountYear() {
        return holidaysAccountYear;
    }

    public void setHolidaysAccountYear(int holidaysAccountYear) {
        this.holidaysAccountYear = holidaysAccountYear;
    }

    public String getHolidaysAccountValidFromIsoValue() {
        if (holidaysAccountValidFrom == null) {
            return "";
        }

        return holidaysAccountValidFrom.format(ISO_DATE);
    }

    public LocalDate getHolidaysAccountValidFrom() {
        return holidaysAccountValidFrom;
    }

    public void setHolidaysAccountValidFrom(LocalDate holidaysAccountValidFrom) {
        this.holidaysAccountValidFrom = holidaysAccountValidFrom;
    }

    public String getHolidaysAccountValidToIsoValue() {
        if (holidaysAccountValidTo == null) {
            return "";
        }

        return holidaysAccountValidTo.format(ISO_DATE);
    }

    public LocalDate getHolidaysAccountValidTo() {
        return holidaysAccountValidTo;
    }

    public void setHolidaysAccountValidTo(LocalDate holidaysAccountValidTo) {
        this.holidaysAccountValidTo = holidaysAccountValidTo;
    }

    public Boolean getDoRemainingVacationDaysExpireLocally() {
        return doRemainingVacationDaysExpireLocally;
    }

    public void setDoRemainingVacationDaysExpireLocally(Boolean doRemainingVacationDaysExpireLocally) {
        this.doRemainingVacationDaysExpireLocally = doRemainingVacationDaysExpireLocally;
    }

    public boolean isDoRemainingVacationDaysExpireGlobally() {
        return doRemainingVacationDaysExpireGlobally;
    }

    public void setDoRemainingVacationDaysExpireGlobally(boolean doRemainingVacationDaysExpireGlobally) {
        this.doRemainingVacationDaysExpireGlobally = doRemainingVacationDaysExpireGlobally;
    }

    public String getExpiryDateToIsoValue() {
        if (expiryDate == null) {
            return "";
        }

        return expiryDate.format(ISO_DATE);
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
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
