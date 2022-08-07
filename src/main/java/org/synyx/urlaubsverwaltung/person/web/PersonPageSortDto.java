package org.synyx.urlaubsverwaltung.person.web;

class PersonPageSortDto {

    private boolean firstNameDesc = false;
    private boolean firstNameAsc = false;
    private boolean lastNameDesc = false;
    private boolean lastNameAsc = false;
    private boolean entitlementYearAsc = false;
    private boolean entitlementYearDesc = false;
    private boolean entitlementActualAsc = false;
    private boolean entitlementActualDesc = false;
    private boolean vacationDaysLeftAsc = false;
    private boolean vacationDaysLeftDesc = false;
    private boolean entitlementRemainingAsc = false;
    private boolean entitlementRemainingDesc = false;
    private boolean vacationDaysLeftRemainingAsc = false;
    private boolean vacationDaysLeftRemainingDesc = false;

    /**
     * Construct PersonSortDto with explicit static builder methods.
     */
    private PersonPageSortDto() {
    }

    public boolean isFirstNameDesc() {
        return firstNameDesc;
    }

    public boolean isFirstNameAsc() {
        return firstNameAsc;
    }

    public boolean isLastNameDesc() {
        return lastNameDesc;
    }

    public boolean isLastNameAsc() {
        return lastNameAsc;
    }

    public boolean isEntitlementYearAsc() {
        return entitlementYearAsc;
    }

    public boolean isEntitlementYearDesc() {
        return entitlementYearDesc;
    }

    public boolean isEntitlementActualAsc() {
        return entitlementActualAsc;
    }

    public boolean isEntitlementActualDesc() {
        return entitlementActualDesc;
    }

    public boolean isVacationDaysLeftAsc() {
        return vacationDaysLeftAsc;
    }

    public boolean isVacationDaysLeftDesc() {
        return vacationDaysLeftDesc;
    }

    public boolean isEntitlementRemainingAsc() {
        return entitlementRemainingAsc;
    }

    public boolean isEntitlementRemainingDesc() {
        return entitlementRemainingDesc;
    }

    public boolean isVacationDaysLeftRemainingAsc() {
        return vacationDaysLeftRemainingAsc;
    }

    public boolean isVacationDaysLeftRemainingDesc() {
        return vacationDaysLeftRemainingDesc;
    }

    static PersonPageSortDto firstName(boolean ascending) {
        final PersonPageSortDto personPageSortDto = new PersonPageSortDto();
        personPageSortDto.firstNameAsc = ascending;
        personPageSortDto.firstNameDesc = !ascending;
        return personPageSortDto;
    }

    static PersonPageSortDto lastName(boolean ascending) {
        final PersonPageSortDto personPageSortDto = new PersonPageSortDto();
        personPageSortDto.lastNameAsc = ascending;
        personPageSortDto.lastNameDesc = !ascending;
        return personPageSortDto;
    }

    static PersonPageSortDto entitlementYear(boolean ascending) {
        final PersonPageSortDto personPageSortDto = new PersonPageSortDto();
        personPageSortDto.entitlementYearAsc = ascending;
        personPageSortDto.entitlementYearDesc = !ascending;
        return personPageSortDto;
    }

    static PersonPageSortDto entitlementActual(boolean ascending) {
        final PersonPageSortDto personPageSortDto = new PersonPageSortDto();
        personPageSortDto.entitlementActualAsc = ascending;
        personPageSortDto.entitlementActualDesc = !ascending;
        return personPageSortDto;
    }

    static PersonPageSortDto vacationDaysLeft(boolean ascending) {
        final PersonPageSortDto personPageSortDto = new PersonPageSortDto();
        personPageSortDto.vacationDaysLeftAsc = ascending;
        personPageSortDto.vacationDaysLeftDesc = !ascending;
        return personPageSortDto;
    }

    static PersonPageSortDto entitlementRemaining(boolean ascending) {
        final PersonPageSortDto personPageSortDto = new PersonPageSortDto();
        personPageSortDto.entitlementRemainingAsc = ascending;
        personPageSortDto.entitlementRemainingDesc = !ascending;
        return personPageSortDto;
    }

    static PersonPageSortDto vacationDaysLeftRemaining(boolean ascending) {
        final PersonPageSortDto personPageSortDto = new PersonPageSortDto();
        personPageSortDto.vacationDaysLeftRemainingAsc = ascending;
        personPageSortDto.vacationDaysLeftRemainingDesc = !ascending;
        return personPageSortDto;
    }
}
