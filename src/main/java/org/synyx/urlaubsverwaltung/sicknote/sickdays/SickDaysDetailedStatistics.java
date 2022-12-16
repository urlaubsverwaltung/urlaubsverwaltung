package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory;

import java.time.LocalDate;
import java.util.List;

import static org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays.SickDayType.TOTAL;
import static org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays.SickDayType.WITH_AUB;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;

public class SickDaysDetailedStatistics {

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

    public SickDays getSickDays(LocalDate from, LocalDate to) {
        return calculateSickDaysForCategory(from, to, SICK_NOTE);
    }

    public SickDays getChildSickDays(LocalDate from, LocalDate to) {
        return calculateSickDaysForCategory(from, to, SICK_NOTE_CHILD);
    }

    private SickDays calculateSickDaysForCategory(LocalDate from, LocalDate to, SickNoteCategory category) {
        return sickNotes.stream()
            .filter(sickNote -> sickNote.getSickNoteType().isOfCategory(category))
            .map(sickNote -> sickDaysForSickNote(from, to, sickNote))
            .reduce(new SickDays(), this::sumSickDays);
    }

    private SickDays sickDaysForSickNote(LocalDate from, LocalDate to, SickNote sickNote) {
        final SickDays sickDays = new SickDays();
        sickDays.addDays(TOTAL, sickNote.getWorkDays(from, to));
        sickDays.addDays(WITH_AUB, sickNote.getWorkDaysWithAub(from, to));
        return sickDays;
    }

    private SickDays sumSickDays(SickDays sum, SickDays next) {
        sum.addDays(TOTAL, next.getDays().get(TOTAL.name()));
        sum.addDays(WITH_AUB, next.getDays().get(WITH_AUB.name()));
        return sum;
    }
}
