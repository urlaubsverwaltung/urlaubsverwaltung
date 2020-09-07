package org.synyx.urlaubsverwaltung.account;

import org.synyx.urlaubsverwaltung.util.CalcUtil;

import java.math.BigDecimal;


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
     * Vacaction days for this year that have already been used NEXT year
     */
    private final BigDecimal vacationDaysUsedNextYear;

    private VacationDaysLeft(BigDecimal vacationDays, BigDecimal remainingVacationDays,
                             BigDecimal remainingVacationDaysNotExpiring, BigDecimal vacationDaysUsedNextYear) {

        this.vacationDays = vacationDays;
        this.remainingVacationDays = remainingVacationDays;
        this.remainingVacationDaysNotExpiring = remainingVacationDaysNotExpiring;
        this.vacationDaysUsedNextYear = vacationDaysUsedNextYear;
    }

    public static Builder builder() {

        return new Builder();
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

    /**
     * Builds information object about left vacation days.
     */
    public static class Builder {

        private BigDecimal annualVacationDays;
        private BigDecimal remainingVacationDays;
        private BigDecimal remainingVacationDaysNotExpiring;
        private BigDecimal usedDaysBeforeApril;
        private BigDecimal usedDaysAfterApril;
        private BigDecimal vacationDaysUsedNextYear = BigDecimal.ZERO;

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


        public Builder forUsedDaysBeforeApril(BigDecimal usedDaysBeforeApril) {

            this.usedDaysBeforeApril = usedDaysBeforeApril;

            return this;
        }


        public Builder forUsedDaysAfterApril(BigDecimal usedDaysAfterApril) {

            this.usedDaysAfterApril = usedDaysAfterApril;

            return this;
        }

        public Builder withVacationDaysUsedNextYear(BigDecimal vacationDaysUsedNextYear) {

            this.vacationDaysUsedNextYear = vacationDaysUsedNextYear;

            return this;
        }


        public VacationDaysLeft get() {

            BigDecimal leftVacationDays = annualVacationDays.subtract(vacationDaysUsedNextYear);
            BigDecimal leftRemainingVacationDays = remainingVacationDays;
            BigDecimal leftRemainingVacationDaysNotExpiring = remainingVacationDaysNotExpiring;

            leftRemainingVacationDays = leftRemainingVacationDays.subtract(usedDaysBeforeApril);

            if (CalcUtil.isPositive(leftRemainingVacationDays)) {
                // remaining vacation days are enough for the days before April

                if (leftRemainingVacationDays.compareTo(leftRemainingVacationDaysNotExpiring) < 1) {
                    // left remaining vacation days are equal or less than not expiring remaining vacation days,
                    // so set the left not expiring remaining vacation days to the value of the remaining vacation days

                    leftRemainingVacationDaysNotExpiring = leftRemainingVacationDays;
                }

                leftRemainingVacationDaysNotExpiring = leftRemainingVacationDaysNotExpiring.subtract(
                    usedDaysAfterApril);

                if (CalcUtil.isNegative(leftRemainingVacationDaysNotExpiring)) {
                    // not expiring remaining vacation days are not enough for the days after April

                    // subtract the difference from the annual vacation days
                    leftVacationDays = leftVacationDays.subtract(leftRemainingVacationDaysNotExpiring.abs());

                    // set not expiring remaining vacation days to 0, because they are not enough
                    leftRemainingVacationDaysNotExpiring = BigDecimal.ZERO;

                    // subtract all the not expiring remaining vacation days from remaining vacation days,
                    // because they have been used completely
                    leftRemainingVacationDays = leftRemainingVacationDays.subtract(remainingVacationDaysNotExpiring);

                    if (CalcUtil.isNegative(leftRemainingVacationDays)) {
                        // if subtracting the difference leads to a negative number of left remaining vacation days,
                        // set them to 0
                        leftRemainingVacationDays = BigDecimal.ZERO;
                    }
                } else {
                    // not expiring remaining vacation days are enough for the days after April

                    // subtract all the days after April from remaining vacation days,
                    // because they have been used by the not expiring remaining vacation days
                    leftRemainingVacationDays = leftRemainingVacationDays.subtract(usedDaysAfterApril);
                }
            } else {
                // remaining vacation days are not enough for the days before April

                // subtract the difference from the annual vacation days
                leftVacationDays = leftVacationDays.subtract(leftRemainingVacationDays.abs());
                leftVacationDays = leftVacationDays.subtract(usedDaysAfterApril);

                // set remaining vacation days and not expiring remaining vacation days to 0
                leftRemainingVacationDays = BigDecimal.ZERO;
                leftRemainingVacationDaysNotExpiring = BigDecimal.ZERO;
            }

            return new VacationDaysLeft(leftVacationDays, leftRemainingVacationDays,
                leftRemainingVacationDaysNotExpiring, vacationDaysUsedNextYear);
        }
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
