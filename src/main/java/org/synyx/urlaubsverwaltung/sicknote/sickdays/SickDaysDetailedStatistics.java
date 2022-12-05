package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.util.List;

class SickDaysDetailedStatistics {

    private final String personalNumber;
    private final String firstName;
    private final String lastName;
    private final List<SickNote> sickNotes;
    private final List<String> departments;

    SickDaysDetailedStatistics(String personalNumber, String firstName, String lastName, List<SickNote> sickNotes, List<String> departments) {
        this.personalNumber = personalNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sickNotes = sickNotes;
        this.departments = departments;
    }

    public String getPersonalNumber() {
        return personalNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<SickNote> getSickNotes() {
        return sickNotes;
    }

    public List<String> getDepartments() {
        return departments;
    }
}
