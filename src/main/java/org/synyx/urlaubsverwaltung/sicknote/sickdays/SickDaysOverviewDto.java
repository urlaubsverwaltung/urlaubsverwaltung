package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import java.math.BigDecimal;

public record SickDaysOverviewDto(
    long personId,
    String personAvatarUrl,
    String personnelNumber,
    String personFirstName,
    String personLastName,
    String personNiceName,
    String personInitials,
    BigDecimal amountSickDays,
    BigDecimal amountSickDaysWithAUB,
    BigDecimal amountChildSickDays,
    BigDecimal amountChildSickDaysWithAUB
) {

    public static SickDaysOverviewDto.Builder builder() {
        return new Builder();
    }

    static class Builder {
        private long personId;
        private String personAvatarUrl;
        private String personnelNumber;
        private String personFirstName;
        private String personLastName;
        private String personNiceName;
        private String personInitials;
        private BigDecimal amountSickDays;
        private BigDecimal amountSickDaysWithAU;
        private BigDecimal amountChildSickDays;
        private BigDecimal amountChildSickDaysWithAU;

        public Builder personId(long personId) {
            this.personId = personId;
            return this;
        }

        public Builder personAvatarUrl(String personAvatarUrl) {
            this.personAvatarUrl = personAvatarUrl;
            return this;
        }

        public Builder personnelNumber(String personnelNumber) {
            this.personnelNumber = personnelNumber;
            return this;
        }

        public Builder personFirstName(String personFirstName) {
            this.personFirstName = personFirstName;
            return this;
        }

        public Builder personLastName(String personLastName) {
            this.personLastName = personLastName;
            return this;
        }

        public Builder personNiceName(String personNiceName) {
            this.personNiceName = personNiceName;
            return this;
        }

        public Builder personInitials(String personInitials) {
            this.personInitials = personInitials;
            return this;
        }

        public Builder amountSickDays(BigDecimal amountSickDays) {
            this.amountSickDays = amountSickDays;
            return this;
        }

        public Builder amountSickDaysWithAUB(BigDecimal amountSickDaysWithAU) {
            this.amountSickDaysWithAU = amountSickDaysWithAU;
            return this;
        }

        public Builder amountChildSickDays(BigDecimal amountChildSickDays) {
            this.amountChildSickDays = amountChildSickDays;
            return this;
        }

        public Builder amountChildSickNoteDaysWithAUB(BigDecimal amountChildSickDaysWithAU) {
            this.amountChildSickDaysWithAU = amountChildSickDaysWithAU;
            return this;
        }

        public SickDaysOverviewDto build() {
            return new SickDaysOverviewDto(personId, personAvatarUrl, personnelNumber, personFirstName, personLastName,
                personNiceName, personInitials, amountSickDays, amountSickDaysWithAU, amountChildSickDays,
                amountChildSickDaysWithAU);
        }
    }
}
