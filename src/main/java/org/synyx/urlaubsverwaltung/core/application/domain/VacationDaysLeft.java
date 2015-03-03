package org.synyx.urlaubsverwaltung.core.application.domain;

import java.math.BigDecimal;


/**
 * Contains information about the left vacation days of a person respective about the left remaining vacation days.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class VacationDaysLeft {

    private BigDecimal annualVacationDays;
    private BigDecimal remainingVacationDays;
    private BigDecimal remainingVacationDaysNotExpiring;
    private BigDecimal usedDaysBeforeApril;
    private BigDecimal usedDaysAfterApril;

    public VacationDaysLeft() {

        // OK
    }


    private VacationDaysLeft(Builder builder) {

        this.annualVacationDays = builder.annualVacationDays;
        this.remainingVacationDays = builder.remainingVacationDays;
        this.remainingVacationDaysNotExpiring = builder.remainingVacationDaysNotExpiring;
        this.usedDaysBeforeApril = builder.usedDaysBeforeApril;
        this.usedDaysAfterApril = builder.usedDaysAfterApril;
    }

    public static Builder builder() {

        return new Builder();
    }


    public BigDecimal getVacationDays() {

        // TODO: Calculate left vacation days

        return null;
    }


    public BigDecimal getRemainingVacationDays() {

        // TODO: Calculate

        return null;
    }


    public BigDecimal getRemainingVacationDaysNotExpiring() {

        // TODO: Calculate

        return null;
    }


    public BigDecimal getTotalVacationDays() {

        // TODO: Calculate, depends on current date (before or after April)

        return null;
    }


    boolean isBeforeApril() {

        return false;
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

        public Builder() {

            // OK
        }

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

            return new VacationDaysLeft(this);
        }
    }
}
