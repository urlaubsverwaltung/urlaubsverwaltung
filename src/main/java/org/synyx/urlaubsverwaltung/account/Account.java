package org.synyx.urlaubsverwaltung.account;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Column;
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
@Entity
public class Account {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GenericGenerator(
        name = "account_id_seq",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "account_id_seq"),
            @Parameter(name = "initial_value", value = "1"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "account_id_seq")
    private Integer id;

    @ManyToOne
    private Person person;

    private LocalDate validFrom;

    private LocalDate validTo;

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

    protected Account() {
        /* OK */
    }

    public Account(Person person, LocalDate validFrom, LocalDate validTo, BigDecimal annualVacationDays,
                   BigDecimal remainingVacationDays, BigDecimal remainingVacationDaysNotExpiring, String comment) {

        this.person = person;
        this.validFrom = validFrom;
        this.validTo = validTo;
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

    public BigDecimal getVacationDays() {
        return vacationDays;
    }

    public void setVacationDays(BigDecimal vacationDays) {
        this.vacationDays = vacationDays;
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
            ", annualVacationDays=" + annualVacationDays +
            ", vacationDays=" + vacationDays +
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
