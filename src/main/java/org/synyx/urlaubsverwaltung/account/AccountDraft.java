package org.synyx.urlaubsverwaltung.account;

import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

/**
 * A not yet existent {@link Account} containing information taken from the previous year.
 * Can be used to manually create a new {@link Account} for instance.
 */
public final class AccountDraft {

    private final Person person;
    private final Year year;
    private final boolean doRemainingVacationDaysExpireGlobally;
    private final Boolean doRemainingVacationDaysExpireLocally;
    private final LocalDate expiryDateGlobally;
    private final LocalDate expiryDateLocally;
    private final BigDecimal annualVacationDays;
    private final BigDecimal remainingVacationDaysNotExpiring;

    AccountDraft(Person person,
                 Year year,
                 boolean doRemainingVacationDaysExpireGlobally,
                 Boolean doRemainingVacationDaysExpireLocally,
                 LocalDate expiryDateGlobally,
                 LocalDate expiryDateLocally,
                 BigDecimal annualVacationDays,
                 BigDecimal remainingVacationDaysNotExpiring
    ) {
        this.person = person;
        this.year = year;
        this.doRemainingVacationDaysExpireGlobally = doRemainingVacationDaysExpireGlobally;
        this.doRemainingVacationDaysExpireLocally = doRemainingVacationDaysExpireLocally;
        this.expiryDateGlobally = expiryDateGlobally;
        this.expiryDateLocally = expiryDateLocally;
        this.annualVacationDays = annualVacationDays;
        this.remainingVacationDaysNotExpiring = remainingVacationDaysNotExpiring;
    }

    public Person getPerson() {
        return person;
    }

    public Year getYear() {
        return year;
    }

    public LocalDate getValidFrom() {
        return year.atDay(1);
    }

    public LocalDate getValidTo() {
        return year.atDay(1).with(lastDayOfYear());
    }

    public boolean doRemainingVacationDaysExpireGlobally() {
        return doRemainingVacationDaysExpireGlobally;
    }

    public Boolean doRemainingVacationDaysExpireLocally() {
        return doRemainingVacationDaysExpireLocally;
    }

    public LocalDate getExpiryDateGlobally() {
        return expiryDateGlobally;
    }

    public LocalDate getExpiryDateLocally() {
        return expiryDateLocally;
    }

    public BigDecimal getAnnualVacationDays() {
        return annualVacationDays;
    }

    public BigDecimal getRemainingVacationDaysNotExpiring() {
        return remainingVacationDaysNotExpiring;
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private Person person;
        private Year year;
        private boolean doRemainingVacationDaysExpireGlobally;
        private Boolean doRemainingVacationDaysExpireLocally;
        private LocalDate expiryDateGlobally;
        private LocalDate expiryDateLocally;
        private BigDecimal annualVacationDays;
        private BigDecimal remainingVacationDaysNotExpiring;

        public Builder person(Person person) {
            this.person = person;
            return this;
        }

        public Builder year(Year year) {
            this.year = year;
            return this;
        }

        public Builder doRemainingVacationDaysExpireGlobally(boolean doRemainingVacationDaysExpireGlobally) {
            this.doRemainingVacationDaysExpireGlobally = doRemainingVacationDaysExpireGlobally;
            return this;
        }

        public Builder doRemainingVacationDaysExpireLocally(Boolean doRemainingVacationDaysExpireLocally) {
            this.doRemainingVacationDaysExpireLocally = doRemainingVacationDaysExpireLocally;
            return this;
        }

        public Builder expiryDateGlobally(LocalDate expiryDateGlobally) {
            this.expiryDateGlobally = expiryDateGlobally;
            return this;
        }

        public Builder expiryDateLocally(LocalDate expiryDateLocally) {
            this.expiryDateLocally = expiryDateLocally;
            return this;
        }

        public Builder annualVacationDays(BigDecimal annualVacationDays) {
            this.annualVacationDays = annualVacationDays;
            return this;
        }

        public Builder setRemainingVacationDaysNotExpiring(BigDecimal remainingVacationDaysNotExpiring) {
            this.remainingVacationDaysNotExpiring = remainingVacationDaysNotExpiring;
            return this;
        }

        public AccountDraft build() {
            return new AccountDraft(
                person,
                year,
                doRemainingVacationDaysExpireGlobally,
                doRemainingVacationDaysExpireLocally,
                expiryDateGlobally,
                expiryDateLocally,
                annualVacationDays,
                remainingVacationDaysNotExpiring
            );
        }
    }
}
