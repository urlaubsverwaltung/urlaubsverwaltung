package org.synyx.urlaubsverwaltung.account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import static java.math.BigDecimal.ZERO;
import static org.synyx.urlaubsverwaltung.util.CalcUtil.isNegative;
import static org.synyx.urlaubsverwaltung.util.CalcUtil.isPositive;


/**
 * Contains information about the left vacation days of a person respective about the left remaining vacation days.
 */
public final class VacationDaysLeft {

    /**
     * Vacation days for this year that have not been used yet
     */
    private final BigDecimal vacationDays;

    /**
     * Additional vacation days left over from last year
     */
    private final BigDecimal remainingVacationDays;

    /**
     * Non-expiring vacation days left over from last year (included in `remainingVacationDays`)
     */
    private final BigDecimal remainingVacationDaysNotExpiring;

    /**
     * Vacation days for this year that have already been used NEXT year
     */
    private final BigDecimal vacationDaysUsedNextYear;

    private VacationDaysLeft(BigDecimal vacationDays, BigDecimal remainingVacationDays,
                             BigDecimal remainingVacationDaysNotExpiring, BigDecimal vacationDaysUsedNextYear) {
        this.vacationDays = vacationDays;
        this.remainingVacationDays = remainingVacationDays;
        this.remainingVacationDaysNotExpiring = remainingVacationDaysNotExpiring;
        this.vacationDaysUsedNextYear = vacationDaysUsedNextYear;
    }

    public BigDecimal getLeftVacationDays(LocalDate today, boolean doRemainingVacationDaysExpire, LocalDate expiryDate) {
        return vacationDays.add(getRemainingVacationDaysLeft(today, doRemainingVacationDaysExpire, expiryDate));
    }

    public BigDecimal getRemainingVacationDaysLeft(LocalDate today, boolean doRemainingVacationDaysExpire, LocalDate expiryDate) {
        if (!doRemainingVacationDaysExpire || today.isBefore(expiryDate)) {
            return remainingVacationDays;
        } else {
            // it's after expiry day - only the left not expiring remaining vacation days must be used
            return remainingVacationDaysNotExpiring;
        }
    }

    public BigDecimal getExpiredRemainingVacationDays(LocalDate today, LocalDate expiryDate) {
        if (today.isBefore(expiryDate)) {
            return ZERO;
        }
        return remainingVacationDays.subtract(remainingVacationDaysNotExpiring);
    }

    public BigDecimal getVacationDays() {
        return vacationDays;
    }

    public BigDecimal getRemainingVacationDays() {
        return remainingVacationDays;
    }

    public BigDecimal getRemainingVacationDaysNotExpiring() {
        return remainingVacationDaysNotExpiring;
    }

    public BigDecimal getVacationDaysUsedNextYear() {
        return vacationDaysUsedNextYear;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds information object about left vacation days.
     */
    public static class Builder {

        private BigDecimal annualVacationDays = ZERO;
        private BigDecimal remainingVacationDays = ZERO;
        private BigDecimal remainingVacationDaysNotExpiring = ZERO;
        private BigDecimal usedVacationDaysBeforeExpiry = ZERO;
        private BigDecimal usedVacationDaysAfterExpiry = ZERO;
        private BigDecimal vacationDaysUsedNextYear = ZERO;

        public Builder withAnnualVacation(BigDecimal annualVacation) {
            this.annualVacationDays = annualVacation;
            return this;
        }

        public Builder withRemainingVacation(BigDecimal remainingVacation) {
            this.remainingVacationDays = remainingVacation;
            return this;
        }

        public Builder notExpiring(BigDecimal remainingVacationNotExpiring) {
            this.remainingVacationDaysNotExpiring = remainingVacationNotExpiring;
            return this;
        }

        public Builder forUsedVacationDaysBeforeExpiry(BigDecimal usedVacationDaysBeforeExpiry) {
            this.usedVacationDaysBeforeExpiry = usedVacationDaysBeforeExpiry;
            return this;
        }

        public Builder forUsedVacationDaysAfterExpiry(BigDecimal usedVacationDaysAfterExpiry) {
            this.usedVacationDaysAfterExpiry = usedVacationDaysAfterExpiry;
            return this;
        }

        public Builder withVacationDaysUsedNextYear(BigDecimal vacationDaysUsedNextYear) {
            this.vacationDaysUsedNextYear = vacationDaysUsedNextYear;
            return this;
        }

        public VacationDaysLeft build() {

            BigDecimal leftVacationDays = annualVacationDays.subtract(vacationDaysUsedNextYear);
            BigDecimal leftRemainingVacationDays = remainingVacationDays;
            BigDecimal leftRemainingVacationDaysNotExpiring = remainingVacationDaysNotExpiring;

            leftRemainingVacationDays = leftRemainingVacationDays.subtract(usedVacationDaysBeforeExpiry);

            if (isPositive(leftRemainingVacationDays)) {
                // remaining vacation days are enough for the days before expiry

                if (leftRemainingVacationDays.compareTo(leftRemainingVacationDaysNotExpiring) < 1) {
                    // left remaining vacation days are equal or less than not expiring remaining vacation days,
                    // so set the left not expiring remaining vacation days to the value of the remaining vacation days

                    leftRemainingVacationDaysNotExpiring = leftRemainingVacationDays;
                }

                leftRemainingVacationDaysNotExpiring = leftRemainingVacationDaysNotExpiring.subtract(
                    usedVacationDaysAfterExpiry);

                if (isNegative(leftRemainingVacationDaysNotExpiring)) {
                    // not expiring remaining vacation days are not enough for the days after expiry

                    // subtract the difference from the annual vacation days
                    leftVacationDays = leftVacationDays.subtract(leftRemainingVacationDaysNotExpiring.abs());

                    // set not expiring remaining vacation days to 0, because they are not enough
                    leftRemainingVacationDaysNotExpiring = ZERO;

                    // subtract all the not expiring remaining vacation days from remaining vacation days,
                    // because they have been used completely
                    leftRemainingVacationDays = leftRemainingVacationDays.subtract(remainingVacationDaysNotExpiring);

                    if (isNegative(leftRemainingVacationDays)) {
                        // if subtracting the difference leads to a negative number of left remaining vacation days,
                        // set them to 0
                        leftRemainingVacationDays = ZERO;
                    }
                } else {
                    // not expiring remaining vacation days are enough for the days after expiry

                    // subtract all the days after expiry from remaining vacation days,
                    // because they have been used by the not expiring remaining vacation days
                    leftRemainingVacationDays = leftRemainingVacationDays.subtract(usedVacationDaysAfterExpiry);
                }
            } else {
                // remaining vacation days are not enough for the days before expiry

                // subtract the difference from the annual vacation days
                leftVacationDays = leftVacationDays.subtract(leftRemainingVacationDays.abs());
                leftVacationDays = leftVacationDays.subtract(usedVacationDaysAfterExpiry);

                // set remaining vacation days and not expiring remaining vacation days to 0
                leftRemainingVacationDays = ZERO;
                leftRemainingVacationDaysNotExpiring = ZERO;
            }

            return new VacationDaysLeft(leftVacationDays, leftRemainingVacationDays,
                leftRemainingVacationDaysNotExpiring, vacationDaysUsedNextYear);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VacationDaysLeft that = (VacationDaysLeft) o;
        return Objects.equals(vacationDays, that.vacationDays)
            && Objects.equals(remainingVacationDays, that.remainingVacationDays)
            && Objects.equals(remainingVacationDaysNotExpiring, that.remainingVacationDaysNotExpiring)
            && Objects.equals(vacationDaysUsedNextYear, that.vacationDaysUsedNextYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vacationDays, remainingVacationDays, remainingVacationDaysNotExpiring, vacationDaysUsedNextYear);
    }

    @Override
    public String toString() {
        return "VacationDaysLeft{" +
            "vacationDays=" + vacationDays +
            ", remainingVacationDays=" + remainingVacationDays +
            ", remainingVacationDaysNotExpiring=" + remainingVacationDaysNotExpiring +
            ", vacationDaysUsedNextYear=" + vacationDaysUsedNextYear +
            '}';
    }
}
