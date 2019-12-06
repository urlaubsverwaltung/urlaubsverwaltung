package org.synyx.urlaubsverwaltung.availability.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.availability.api.TimedAbsence.Type.SICK_NOTE;


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

        final List<Optional<TimedAbsence>> optionalSickDayAbsences = checkForSickDay(date, person);
        if (optionalSickDayAbsences.isEmpty()) {
            return knownAbsences;
        }

        List<TimedAbsence> knownAbsencesList = knownAbsences.getAbsencesList();
        for (Optional<TimedAbsence> optionalSickDayAbsence : optionalSickDayAbsences) {
            optionalSickDayAbsence.ifPresent(knownAbsencesList::add);
        }

        return new TimedAbsenceSpans(knownAbsencesList);
    }

    @Override
    boolean isLastPriorityProvider() {

        return false;
    }

    private List<Optional<TimedAbsence>> checkForSickDay(LocalDate date, Person person) {

        final List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, date, date);
        if (sickNotes.isEmpty()) {
            return List.of();
        }

        final List<Optional<TimedAbsence>> sickNoteTimeAbsence = new ArrayList<>();
        for (SickNote sickNote : sickNotes) {
            if (sickNote != null && sickNote.isActive()) {
                sickNoteTimeAbsence.add(Optional.of(new TimedAbsence(sickNote.getDayLength(), SICK_NOTE)));
            }
        }

        return sickNoteTimeAbsence;
    }
}
