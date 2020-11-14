package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteTypeService;

import java.time.LocalDate;
import java.util.List;

import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.ACTIVE;

/**
 * Provides sick note demo data.
 */
class SickNoteDataProvider {

    private final SickNoteInteractionService sickNoteInteractionService;
    private final DurationChecker durationChecker;
    private final SickNoteTypeService sickNoteTypeService;

    SickNoteDataProvider(SickNoteInteractionService sickNoteInteractionService, DurationChecker durationChecker, SickNoteTypeService sickNoteTypeService) {

        this.sickNoteInteractionService = sickNoteInteractionService;
        this.durationChecker = durationChecker;
        this.sickNoteTypeService = sickNoteTypeService;
    }

    void createSickNote(Person person, Person office, DayLength dayLength, LocalDate startDate, LocalDate endDate, SickNoteCategory sickNoteCategory, boolean withAUB) {
        if (durationChecker.durationIsGreaterThanZero(startDate, endDate, person)) {

            final SickNoteType type = getSickNoteType(sickNoteCategory);

            final SickNote sickNote = new SickNote();
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

    private SickNoteType getSickNoteType(SickNoteCategory sickNoteCategory) {

        SickNoteType type = null;
        final List<SickNoteType> sickNoteTypes = sickNoteTypeService.getSickNoteTypes();
        for (SickNoteType sickNoteType : sickNoteTypes) {
            if (sickNoteType.isOfCategory(sickNoteCategory)) {
                type = sickNoteType;
            }
        }
        return type;
    }
}
