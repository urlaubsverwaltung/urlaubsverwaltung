package org.synyx.urlaubsverwaltung.person.web;

public class PersonDto {

    private final long id;

    private final String gravatarUrl;
    private final String firstName;
    private final String lastName;
    private final String niceName;
    private final String initials;

    private final String personnelNumber;
    private final String additionalInformation;

    private final Double entitlementYear;
    private final Double entitlementActual;
    private final Double entitlementRemaining;
    private final Double vacationDaysLeft;
    private final Double vacationDaysLeftRemaining;

    private PersonDto(long id, String gravatarUrl, String firstName, String lastName, String niceName, String initials,
                      String personnelNumber, String additionalInformation, Double entitlementYear, Double entitlementActual, Double entitlementRemaining,
                      Double vacationDaysLeft, Double vacationDaysLeftRemaining) {

        this.id = id;
        this.gravatarUrl = gravatarUrl;
        this.firstName = firstName;
        this.lastName = lastName;
        this.niceName = niceName;
        this.initials = initials;
        this.personnelNumber = personnelNumber;
        this.additionalInformation = additionalInformation;
        this.entitlementYear = entitlementYear;
        this.entitlementActual = entitlementActual;
        this.entitlementRemaining = entitlementRemaining;
        this.vacationDaysLeft = vacationDaysLeft;
        this.vacationDaysLeftRemaining = vacationDaysLeftRemaining;
    }

    public Long getId() {
        return id;
    }

    public String getGravatarUrl() {
        return gravatarUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNiceName() {
        return niceName;
    }

    public String getInitials() {
        return initials;
    }

    public String getPersonnelNumber() {
        return personnelNumber;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public Double getEntitlementYear() {
        return entitlementYear;
    }

    public Double getEntitlementActual() {
        return entitlementActual;
    }

    public Double getEntitlementRemaining() {
        return entitlementRemaining;
    }

    public Double getVacationDaysLeft() {
        return vacationDaysLeft;
    }

    public Double getVacationDaysLeftRemaining() {
        return vacationDaysLeftRemaining;
    }

    static PersonDto.Builder builder() {
        return new Builder();
    }

    static class Builder {
        private long id;
        private String gravatarUrl;
        private String firstName;
        private String lastName;
        private String niceName;
        private String initials;
        private String personnelNumber;
        private String additionalInformation;
        private Double entitlementYear;
        private Double entitlementActual;
        private Double entitlementRemaining;
        private Double vacationDaysLeft;
        private Double vacationDaysLeftRemaining;

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder gravatarUrl(String gravatarUrl) {
            this.gravatarUrl = gravatarUrl;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder niceName(String niceName) {
            this.niceName = niceName;
            return this;
        }

        public Builder initials(String initials) {
            this.initials = initials;
            return this;
        }

        public Builder personnelNumber(String personnelNumber) {
            this.personnelNumber = personnelNumber;
            return this;
        }

        public Builder additionalInformation(String additionalInformation) {
            this.additionalInformation = additionalInformation;
            return this;
        }

        public Builder entitlementYear(Double entitlementYear) {
            this.entitlementYear = entitlementYear;
            return this;
        }

        public Builder entitlementActual(Double entitlementActual) {
            this.entitlementActual = entitlementActual;
            return this;
        }

        public Builder entitlementRemaining(Double entitlementRemaining) {
            this.entitlementRemaining = entitlementRemaining;
            return this;
        }

        public Builder vacationDaysLeft(Double vacationDaysLeft) {
            this.vacationDaysLeft = vacationDaysLeft;
            return this;
        }

        public Builder vacationDaysLeftRemaining(Double vacationDaysLeftRemaining) {
            this.vacationDaysLeftRemaining = vacationDaysLeftRemaining;
            return this;
        }

        public PersonDto build() {
            return new PersonDto(
                id,
                gravatarUrl,
                firstName,
                lastName,
                niceName,
                initials,
                personnelNumber,
                additionalInformation,
                entitlementYear,
                entitlementActual,
                entitlementRemaining,
                vacationDaysLeft,
                vacationDaysLeftRemaining
            );
        }
    }
}
