package org.synyx.urlaubsverwaltung.account;

import org.springframework.lang.Nullable;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * This class describes how many vacation days and remaining vacation days a person has in which period (validFrom, validTo).
 */
public class Account {

    private Long id;
    private Person person;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Boolean doRemainingVacationDaysExpireLocally;
    private boolean doRemainingVacationDaysExpireGlobally;
    private LocalDate expiryDateLocally;
    private LocalDate expiryDateGlobally;
    private LocalDate expiryNotificationSentDate;

    // theoretical number of vacation days a person has, i.e. it's the annual entitlement, but it is possible that
    // person e.g. will quit soon the company so he has not the full holidays entitlement; the actual number of vacation
    // days for a year describes the field vacationDays
    private BigDecimal annualVacationDays;
    private BigDecimal actualVacationDays;

    // remaining vacation days from the last year, if it's expiry day, only the not expiring remaining vacation days may be used
    private BigDecimal remainingVacationDays;
    private BigDecimal remainingVacationDaysNotExpiring;

    private String comment;

    public Account() {
        /* OK */
    }

    public Account(Person person, LocalDate validFrom, LocalDate validTo, Boolean doRemainingVacationDaysExpireLocally,
                   @Nullable LocalDate expiryDateLocally, BigDecimal annualVacationDays, BigDecimal remainingVacationDays,
                   BigDecimal remainingVacationDaysNotExpiring, String comment) {

        this.person = person;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.doRemainingVacationDaysExpireLocally = doRemainingVacationDaysExpireLocally;
        this.expiryDateLocally = expiryDateLocally;
        this.annualVacationDays = annualVacationDays;
        this.remainingVacationDays = remainingVacationDays;
        this.remainingVacationDaysNotExpiring = remainingVacationDaysNotExpiring;
        this.comment = comment;
    }

    /**
     * Returns if the remaining vacation days do expire globally or locally (persons holiday account specific configuration)
     * <table border="1">
     *   <tr>
     *     <th>Globally</th>
     *     <th>Locally</th>
     *     <th>Result</th>
     *   </tr>
     *   <tr>
     *     <td><strong>true</strong></td><td>null</td><td><strong>true</strong></td>
     *   </tr>
     *   <tr>
     *     <td><strong>false</strong></td><td>null</td><td><strong>false</strong></td>
     *   </tr>
     *   <tr>
     *     <td>true</td><td><strong>true</strong></td><td><strong>true</strong></td>
     *   </tr>
     *   <tr>
     *     <td>true</td><td><strong>false</strong></td><td><strong>false</strong></td>
     *   </tr>
     *   <tr>
     *     <td>false</td><td><strong>true</strong></td><td><strong>true</strong></td>
     *   </tr>
     *   <tr>
     *     <td>false</td><td><strong>false</strong></td><td><strong>false</strong></td>
     *   </tr>
     * </table>
     *
     * @return true the remaining vacation days of a user does expire, otherwise false
     */
    public boolean doRemainingVacationDaysExpire() {
        return doRemainingVacationDaysExpireLocally == null ? doRemainingVacationDaysExpireGlobally : doRemainingVacationDaysExpireLocally;
    }

    /**
     * Returns the expiry date for the remaining vacation days locally if set, otherwise the global expiry date.
     *
     * @return the user specific calculated expiry date
     */
    public LocalDate getExpiryDate() {
        return expiryDateLocally != null ? expiryDateLocally : expiryDateGlobally;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getActualVacationDays() {
        return actualVacationDays;
    }

    public void setActualVacationDays(BigDecimal vacationDays) {
        this.actualVacationDays = vacationDays;
    }

    public LocalDate getValidFrom() {
        return this.validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return this.validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    public Boolean isDoRemainingVacationDaysExpireLocally() {
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

    public LocalDate getExpiryNotificationSentDate() {
        return expiryNotificationSentDate;
    }

    public void setExpiryNotificationSentDate(LocalDate expiryDateNotificationSent) {
        this.expiryNotificationSentDate = expiryDateNotificationSent;
    }

    public int getYear() {
        return this.validFrom.getYear();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Account{" +
            "id=" + id +
            ", person=" + person +
            ", validFrom=" + validFrom +
            ", validTo=" + validTo +
            ", doRemainingVacationDaysExpireLocally=" + doRemainingVacationDaysExpireLocally +
            ", doRemainingVacationDaysExpireGlobally=" + doRemainingVacationDaysExpireGlobally +
            ", expiryDateLocally=" + expiryDateLocally +
            ", expiryDateGlobally=" + expiryDateGlobally +
            ", expiryNotificationSentDate=" + expiryNotificationSentDate +
            ", annualVacationDays=" + annualVacationDays +
            ", actualVacationDays=" + actualVacationDays +
            ", remainingVacationDays=" + remainingVacationDays +
            ", remainingVacationDaysNotExpiring=" + remainingVacationDaysNotExpiring +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Account that = (Account) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
