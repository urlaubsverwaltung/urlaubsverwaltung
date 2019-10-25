package org.synyx.urlaubsverwaltung.availability.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.availability.api.TimedAbsence.Type.VACATION;


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

        final Optional<TimedAbsence> vacationAbsence = checkForVacation(date, person);

        if (vacationAbsence.isPresent()) {
            List<TimedAbsence> knownAbsencesList = knownAbsences.getAbsencesList();
            knownAbsencesList.add(vacationAbsence.get());

            return new TimedAbsenceSpans(knownAbsencesList);
        }

        return knownAbsences;
    }

    @Override
    boolean isLastPriorityProvider() {

        return true;
    }

    private Optional<TimedAbsence> checkForVacation(LocalDate date, Person person) {

        final List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(date, date, person)
            .stream()
            .filter(application -> application.hasStatus(WAITING) ||
                application.hasStatus(TEMPORARY_ALLOWED) || application.hasStatus(ALLOWED))
            .collect(toList());

        if (applications.isEmpty()) {
            return Optional.empty();
        }

        Application application = applications.get(0);

        return Optional.of(new TimedAbsence(application.getDayLength(), VACATION));
    }
}
