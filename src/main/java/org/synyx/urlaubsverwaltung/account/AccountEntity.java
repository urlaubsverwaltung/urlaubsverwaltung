package org.synyx.urlaubsverwaltung.account;

import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * This class describes how many vacation days and remaining vacation days a person has in which period (validFrom, validTo).
 */
@Entity(name = "account")
public class AccountEntity {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private Person person;

    private LocalDate validFrom;

    private LocalDate validTo;

    private boolean doRemainingVacationDaysExpire;

    private LocalDate expiryDate;

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

    protected AccountEntity() {
        /* OK */
    }

    public AccountEntity(Person person, LocalDate validFrom, LocalDate validTo, boolean doRemainingVacationDaysExpire,
                         LocalDate expiryDate, BigDecimal annualVacationDays, BigDecimal remainingVacationDays,
                         BigDecimal remainingVacationDaysNotExpiring, String comment) {

        this.person = person;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.doRemainingVacationDaysExpire = doRemainingVacationDaysExpire;
        this.expiryDate = expiryDate;
        this.annualVacationDays = annualVacationDays;
        this.remainingVacationDays = remainingVacationDays;
        this.remainingVacationDaysNotExpiring = remainingVacationDaysNotExpiring;
        this.comment = comment;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public boolean isDoRemainingVacationDaysExpire() {
        return doRemainingVacationDaysExpire;
    }

    public void setDoRemainingVacationDaysExpire(boolean doRemainingVacationDaysExpire) {
        this.doRemainingVacationDaysExpire = doRemainingVacationDaysExpire;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
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
            "person=" + person +
            ", validFrom=" + validFrom +
            ", validTo=" + validTo +
            ", doRemainingVacationDaysExpire=" + doRemainingVacationDaysExpire +
            ", expiryDate=" + expiryDate +
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
        final AccountEntity that = (AccountEntity) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
