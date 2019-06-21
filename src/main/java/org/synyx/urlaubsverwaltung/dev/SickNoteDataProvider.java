package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteType;

import java.time.LocalDate;

import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.ACTIVE;


/**
 * Provides sick note test data.
 */
class SickNoteDataProvider {

    private final SickNoteInteractionService sickNoteInteractionService;
    private final DurationChecker durationChecker;

    SickNoteDataProvider(SickNoteInteractionService sickNoteInteractionService, DurationChecker durationChecker) {

        this.sickNoteInteractionService = sickNoteInteractionService;
        this.durationChecker = durationChecker;
    }

    void createSickNote(Person person, Person office, DayLength dayLength, LocalDate startDate,
                            LocalDate endDate, SickNoteType type, boolean withAUB) {

        final SickNote sickNote;

        if (durationChecker.durationIsGreaterThanZero(startDate, endDate, person)) {
            sickNote = new SickNote();
            sickNote.setPerson(person);
            sickNote.setStartDate(startDate);
            sickNote.setEndDate(endDate);
            sickNote.setStatus(ACTIVE);
            sickNote.setSickNoteType(type);
            sickNote.setDayLength(dayLength);

            if (withAUB) {
                sickNote.setAubStartDate(startDate);
                sickNote.setAubEndDate(endDate);
            }

            sickNoteInteractionService.create(sickNote, office);
        }
    }
}
