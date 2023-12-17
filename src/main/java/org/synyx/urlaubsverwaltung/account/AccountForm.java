package org.synyx.urlaubsverwaltung.account;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;

import static java.time.Month.APRIL;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.ISO_DATE;

public class AccountForm {

    private int holidaysAccountYear;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate holidaysAccountValidFrom;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate holidaysAccountValidTo;
    private boolean overrideVacationDaysExpire;
    private Boolean doRemainingVacationDaysExpireLocally;
    private boolean doRemainingVacationDaysExpireGlobally;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate expiryDateLocally;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate expiryDateGlobally;
    private BigDecimal annualVacationDays;
    private BigDecimal actualVacationDays;
    private BigDecimal remainingVacationDays;
    private BigDecimal remainingVacationDaysNotExpiring;
    private String comment;

    private AccountForm() {
    }

    /* TODO remove - only used in tests */
    AccountForm(int year) {
        this.holidaysAccountYear = year;
        this.holidaysAccountValidFrom = Year.of(year).atDay(1);
        this.holidaysAccountValidTo = holidaysAccountValidFrom.with(lastDayOfYear());
        this.overrideVacationDaysExpire = false;
        this.doRemainingVacationDaysExpireLocally = true;
        this.expiryDateLocally = LocalDate.of(year, APRIL, 1);
    }

    AccountForm(Account holidaysAccount) {
        this.holidaysAccountYear = holidaysAccount.getValidFrom().getYear();
        this.holidaysAccountValidFrom = holidaysAccount.getValidFrom();
        this.holidaysAccountValidTo = holidaysAccount.getValidTo();
        this.overrideVacationDaysExpire = holidaysAccount.isDoRemainingVacationDaysExpireLocally() != null;
        this.doRemainingVacationDaysExpireLocally = holidaysAccount.isDoRemainingVacationDaysExpireLocally();
        this.doRemainingVacationDaysExpireGlobally = holidaysAccount.isDoRemainingVacationDaysExpireGlobally();
        this.expiryDateLocally = holidaysAccount.getExpiryDateLocally();
        this.expiryDateGlobally = holidaysAccount.getExpiryDateGlobally();
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

        return holidaysAccountValidFrom.format(DateTimeFormatter.ISO_DATE);
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

        return holidaysAccountValidTo.format(DateTimeFormatter.ISO_DATE);
    }

    public LocalDate getHolidaysAccountValidTo() {
        return holidaysAccountValidTo;
    }

    public void setHolidaysAccountValidTo(LocalDate holidaysAccountValidTo) {
        this.holidaysAccountValidTo = holidaysAccountValidTo;
    }

    public boolean isOverrideVacationDaysExpire() {
        return overrideVacationDaysExpire;
    }

    public void setOverrideVacationDaysExpire(boolean overrideVacationDaysExpire) {
        this.overrideVacationDaysExpire = overrideVacationDaysExpire;
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

    public boolean doRemainingVacationDaysExpire() {
        return doRemainingVacationDaysExpireLocally == null ? doRemainingVacationDaysExpireGlobally : doRemainingVacationDaysExpireLocally;
    }

    public String getExpiryDateToIsoValue() {
        if (expiryDateLocally == null) {
            return "";
        }

        return expiryDateLocally.format(DateTimeFormatter.ISO_DATE);
    }

    public LocalDate getExpiryDateLocally() {
        return expiryDateLocally;
    }

    public void setExpiryDateLocally(LocalDate expiryDateLocally) {
        this.expiryDateLocally = expiryDateLocally;
    }

    public LocalDate getExpiryDateGlobally() {
        return expiryDateGlobally;
    }

    public void setExpiryDateGlobally(LocalDate expiryDateGlobally) {
        this.expiryDateGlobally = expiryDateGlobally;
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
