package org.synyx.urlaubsverwaltung.restapi.availability;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;

import java.util.List;
import java.util.Optional;


/**
 * @author  Timo Eifler - eifler@synyx.de
 */
@Service
class SickDayAbsenceProvider extends AbstractTimedAbsenceProvider {

    private SickNoteService sickNoteService;

    @Autowired
    SickDayAbsenceProvider(VacationAbsenceProvider nextPriorityProvider, SickNoteService sickNoteService) {

        super(nextPriorityProvider);

        this.sickNoteService = sickNoteService;
    }

    @Override
    TimedAbsenceSpans addAbsence(TimedAbsenceSpans knownAbsences, Person person, DateMidnight date) {

        Optional<TimedAbsence> sickDayAbsence = checkForSickDay(date, person);

        if (sickDayAbsence.isPresent()) {
            List<TimedAbsence> knownAbsencesList = knownAbsences.getAbsencesList();
            knownAbsencesList.add(sickDayAbsence.get());

            return new TimedAbsenceSpans(knownAbsencesList);
        }

        return knownAbsences;
    }


    @Override
    boolean isLastPriorityProvider() {

        return false;
    }


    private Optional<TimedAbsence> checkForSickDay(DateMidnight date, Person person) {

        List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, date, date);

        if (!sickNotes.isEmpty()) {
            SickNote sickNote = sickNotes.get(0);

            if (sickNote != null && sickNote.isActive()) {
                return Optional.of(new TimedAbsence(sickNote.getDayLength(), TimedAbsence.Type.SICK_NOTE));
            }
        }

        return Optional.empty();
    }
}
