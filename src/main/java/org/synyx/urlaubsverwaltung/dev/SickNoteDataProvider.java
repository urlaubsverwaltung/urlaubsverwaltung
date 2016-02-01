package org.synyx.urlaubsverwaltung.dev;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteType;


/**
 * Provides sick note test data.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component
class SickNoteDataProvider {

    private final SickNoteInteractionService sickNoteInteractionService;
    private final DurationChecker durationChecker;

    @Autowired
    SickNoteDataProvider(SickNoteInteractionService sickNoteInteractionService, DurationChecker durationChecker) {

        this.sickNoteInteractionService = sickNoteInteractionService;
        this.durationChecker = durationChecker;
    }

    SickNote createSickNote(Person person, Person office, DayLength dayLength, DateMidnight startDate,
        DateMidnight endDate, SickNoteType type, boolean withAUB) {

        SickNote sickNote = null;

        if (durationChecker.durationIsGreaterThanZero(startDate, endDate, person)) {
            sickNote = new SickNote();
            sickNote.setPerson(person);
            sickNote.setStartDate(startDate);
            sickNote.setEndDate(endDate);
            sickNote.setStatus(SickNoteStatus.ACTIVE);
            sickNote.setSickNoteType(type);
            sickNote.setDayLength(dayLength);

            if (withAUB) {
                sickNote.setAubStartDate(startDate);
                sickNote.setAubEndDate(endDate);
            }

            sickNote = sickNoteInteractionService.create(sickNote, office);
        }

        return sickNote;
    }
}
