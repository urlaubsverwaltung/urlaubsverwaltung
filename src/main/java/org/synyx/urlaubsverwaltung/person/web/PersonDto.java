package org.synyx.urlaubsverwaltung.person.web;

public class PersonDto {

    private final long id;

    private final String gravatarUrl;
    private final String firstName;
    private final String lastName;
    private final String niceName;

    private final Double entitlementYear;
    private final Double entitlementActual;
    private final Double entitlementRemaining;
    private final Double vacationDaysLeft;
    private final Double vacationDaysLeftRemaining;

    private PersonDto(long id, String gravatarUrl, String firstName, String lastName, String niceName,
                      Double entitlementYear, Double entitlementActual, Double entitlementRemaining,
                      Double vacationDaysLeft, Double vacationDaysLeftRemaining) {

        this.id = id;
        this.gravatarUrl = gravatarUrl;
        this.firstName = firstName;
        this.lastName = lastName;
        this.niceName = niceName;
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
                entitlementYear,
                entitlementActual,
                entitlementRemaining,
                vacationDaysLeft,
                vacationDaysLeftRemaining
            );
        }
    }
}
