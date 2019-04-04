package org.synyx.urlaubsverwaltung.availability.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
class SickDayAbsenceProvider extends AbstractTimedAbsenceProvider {

    private final SickNoteService sickNoteService;

    @Autowired
    SickDayAbsenceProvider(VacationAbsenceProvider nextPriorityProvider, SickNoteService sickNoteService) {

        super(nextPriorityProvider);

        this.sickNoteService = sickNoteService;
    }

    @Override
    TimedAbsenceSpans addAbsence(TimedAbsenceSpans knownAbsences, Person person, LocalDate date) {

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


    private Optional<TimedAbsence> checkForSickDay(LocalDate date, Person person) {

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
