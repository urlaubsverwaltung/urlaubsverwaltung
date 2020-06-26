package org.synyx.urlaubsverwaltung.availability.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;


@Service
class VacationAbsenceProvider extends AbstractTimedAbsenceProvider {

    private final ApplicationService applicationService;

    @Autowired
    VacationAbsenceProvider(ApplicationService applicationService) {

        super(null);

        this.applicationService = applicationService;
    }

    @Override
    TimedAbsenceSpans addAbsence(TimedAbsenceSpans knownAbsences, Person person, LocalDate date) {

        final List<Optional<TimedAbsence>> optionalTimedAbsences = checkForVacation(date, person);
        if (optionalTimedAbsences.isEmpty()) {
            return knownAbsences;
        }

        List<TimedAbsence> knownAbsencesList = knownAbsences.getAbsencesList();
        for (Optional<TimedAbsence> optionalTimedAbsence : optionalTimedAbsences) {
            optionalTimedAbsence.ifPresent(knownAbsencesList::add);
        }
        return new TimedAbsenceSpans(knownAbsencesList);
    }

    @Override
    boolean isLastPriorityProvider() {

        return true;
    }

    private List<Optional<TimedAbsence>> checkForVacation(LocalDate date, Person person) {

        final

        List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(date, date,
            person)
            .stream()
            .filter(application -> application.hasStatus(WAITING) ||
                application.hasStatus(TEMPORARY_ALLOWED) || application.hasStatus(ALLOWED))
            .collect(toList());

        if (applications.isEmpty()) {
            return List.of();
        }

        final List<Optional<TimedAbsence>> vacationTimeAbsence = new ArrayList<>();
        for (Application application : applications) {
            vacationTimeAbsence.add(Optional.of(new TimedAbsence(application.getDayLength())));
        }

        return vacationTimeAbsence;
    }
}
