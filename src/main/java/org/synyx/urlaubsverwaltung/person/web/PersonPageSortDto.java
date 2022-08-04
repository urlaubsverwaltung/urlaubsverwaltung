package org.synyx.urlaubsverwaltung.person.web;

class PersonPageSortDto {

    private boolean firstNameDescending;
    private boolean firstNameAscending;
    private boolean lastNameDescending;
    private boolean lastNameAscending;

    /**
     * Construct PersonSortDto with explicit static builder methods.
     */
    private PersonPageSortDto() {
        firstNameDescending = false;
        firstNameAscending = false;
        lastNameDescending = false;
        lastNameAscending = false;
    }

    public boolean isFirstNameDescending() {
        return firstNameDescending;
    }

    public boolean isFirstNameAscending() {
        return firstNameAscending;
    }

    public boolean isLastNameDescending() {
        return lastNameDescending;
    }

    public boolean isLastNameAscending() {
        return lastNameAscending;
    }

    static PersonPageSortDto firstName(boolean ascending) {
        final PersonPageSortDto personPageSortDto = new PersonPageSortDto();
        personPageSortDto.firstNameAscending = ascending;
        personPageSortDto.firstNameDescending = !ascending;
        return personPageSortDto;
    }

    static PersonPageSortDto lastName(boolean ascending) {
        final PersonPageSortDto personPageSortDto = new PersonPageSortDto();
        personPageSortDto.lastNameAscending = ascending;
        personPageSortDto.lastNameDescending = !ascending;
        return personPageSortDto;
    }
}
