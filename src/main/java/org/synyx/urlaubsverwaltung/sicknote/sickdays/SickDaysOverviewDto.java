package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import java.math.BigDecimal;

public class SickDaysOverviewDto {

    private final long personId;
    private final String personAvatarUrl;
    private final String personnelNumber;
    private final String personFirstName;
    private final String personLastName;
    private final String personNiceName;
    private final BigDecimal amountSickDays;
    private final BigDecimal amountSickDaysWithAUB;
    private final BigDecimal amountChildSickDays;
    private final BigDecimal amountChildSickDaysWithAUB;

    private SickDaysOverviewDto(long personId, String personAvatarUrl, String personnelNumber, String personFirstName,
                                String personLastName, String personNiceName, BigDecimal amountSickDays,
                                BigDecimal amountSickDaysWithAUB, BigDecimal amountChildSickDays, BigDecimal amountChildSickDaysWithAUB) {

        this.personId = personId;
        this.personAvatarUrl = personAvatarUrl;
        this.personnelNumber = personnelNumber;
        this.personFirstName = personFirstName;
        this.personLastName = personLastName;
        this.personNiceName = personNiceName;
        this.amountSickDays = amountSickDays;
        this.amountSickDaysWithAUB = amountSickDaysWithAUB;
        this.amountChildSickDays = amountChildSickDays;
        this.amountChildSickDaysWithAUB = amountChildSickDaysWithAUB;
    }

    public long getPersonId() {
        return personId;
    }

    public String getPersonAvatarUrl() {
        return personAvatarUrl;
    }

    public String getPersonnelNumber() {
        return personnelNumber;
    }

    public String getPersonFirstName() {
        return personFirstName;
    }

    public String getPersonLastName() {
        return personLastName;
    }

    public String getPersonNiceName() {
        return personNiceName;
    }

    public BigDecimal getAmountSickDays() {
        return amountSickDays;
    }

    public BigDecimal getAmountSickDaysWithAUB() {
        return amountSickDaysWithAUB;
    }

    public BigDecimal getAmountChildSickDays() {
        return amountChildSickDays;
    }

    public BigDecimal getAmountChildSickDaysWithAUB() {
        return amountChildSickDaysWithAUB;
    }

    public static SickDaysOverviewDto.Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "SickDaysOverviewDto{" +
            "personId=" + personId +
            ", personAvatarUrl='" + personAvatarUrl + '\'' +
            ", personnelNumber='" + personnelNumber + '\'' +
            ", personFirstName='" + personFirstName + '\'' +
            ", personLastName='" + personLastName + '\'' +
            ", personNiceName='" + personNiceName + '\'' +
            ", amountSickDays=" + amountSickDays +
            ", amountSickDaysWithAUB=" + amountSickDaysWithAUB +
            ", amountChildSickDays=" + amountChildSickDays +
            ", amountChildSickDaysWithAUB=" + amountChildSickDaysWithAUB +
            '}';
    }

    static class Builder {
        private long personId;
        private String personAvatarUrl;
        private String personnelNumber;
        private String personFirstName;
        private String personLastName;
        private String personNiceName;
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
                personNiceName, amountSickDays, amountSickDaysWithAU, amountChildSickDays,
                amountChildSickDaysWithAU);
        }
    }
}
