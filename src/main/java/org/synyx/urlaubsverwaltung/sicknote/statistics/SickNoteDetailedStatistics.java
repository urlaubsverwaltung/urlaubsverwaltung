package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.util.List;


public class SickNoteDetailedStatistics {

    private final String personalNumber;
    private final String firstName;
    private final String lastName;
    private final List<SickNote> sickNotes;

    public SickNoteDetailedStatistics(String personalNumber, String firstName, String lastName, List<SickNote> sickNotes) {
        this.personalNumber = personalNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sickNotes = sickNotes;
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
}
