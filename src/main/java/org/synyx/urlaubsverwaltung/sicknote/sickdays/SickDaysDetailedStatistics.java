package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.util.List;

class SickDaysDetailedStatistics {

    private final String personalNumber;
    private final Person person;
    private final List<SickNote> sickNotes;
    private final List<String> departments;

    SickDaysDetailedStatistics(String personalNumber, Person person, List<SickNote> sickNotes, List<String> departments) {
        this.personalNumber = personalNumber;
        this.person = person;
        this.sickNotes = sickNotes;
        this.departments = departments;
    }

    public String getPersonalNumber() {
        return personalNumber;
    }

    public Person getPerson() {
        return person;
    }

    public List<SickNote> getSickNotes() {
        return sickNotes;
    }

    public List<String> getDepartments() {
        return departments;
    }
}
