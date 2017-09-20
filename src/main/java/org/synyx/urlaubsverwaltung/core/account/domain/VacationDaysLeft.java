package org.synyx.urlaubsverwaltung.core.account.domain;

import lombok.Value;
import org.synyx.urlaubsverwaltung.core.util.CalcUtil;

import java.math.BigDecimal;


/**
 * Contains information about the left vacation days of a person respective about the left remaining vacation days.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Value
public class VacationDaysLeft {

    BigDecimal vacationDays;
    BigDecimal remainingVacationDays;
    BigDecimal remainingVacationDaysNotExpiring;

    public static Builder builder() {

        return new Builder();
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


        public VacationDaysLeft get() {

            BigDecimal leftVacationDays = annualVacationDays;
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
                    leftRemainingVacationDaysNotExpiring);
        }
    }
}
