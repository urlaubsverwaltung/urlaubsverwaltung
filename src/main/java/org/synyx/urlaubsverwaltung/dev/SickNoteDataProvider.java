package org.synyx.urlaubsverwaltung.dev;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteType;

import java.time.LocalDate;
import java.util.Optional;


/**
 * Provides sick note test data.
 */
@Component
@ConditionalOnProperty("testdata.create")
class SickNoteDataProvider {

    private final SickNoteInteractionService sickNoteInteractionService;
    private final DurationChecker durationChecker;

    @Autowired
    SickNoteDataProvider(SickNoteInteractionService sickNoteInteractionService, DurationChecker durationChecker) {

        this.sickNoteInteractionService = sickNoteInteractionService;
        this.durationChecker = durationChecker;
    }

    SickNote createSickNote(Person person, Person office, DayLength dayLength, LocalDate startDate,
                            LocalDate endDate, SickNoteType type, boolean withAUB) {

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

            sickNote = sickNoteInteractionService.create(sickNote, office, Optional.empty());
        }

        return sickNote;
    }
}
