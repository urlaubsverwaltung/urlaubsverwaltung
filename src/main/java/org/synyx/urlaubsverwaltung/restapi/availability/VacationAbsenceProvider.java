package org.synyx.urlaubsverwaltung.restapi.availability;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author  Timo Eifler - eifler@synyx.de
 */
@Service
class VacationAbsenceProvider extends AbstractTimedAbsenceProvider {

    private ApplicationService applicationService;

    @Autowired
    VacationAbsenceProvider(ApplicationService applicationService) {

        super(null);

        this.applicationService = applicationService;
    }

    @Override
    TimedAbsenceSpans addAbsence(TimedAbsenceSpans knownAbsences, Person person, DateMidnight date) {

        Optional<TimedAbsence> vacationAbsence = checkForVacation(date, person);

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


    private Optional<TimedAbsence> checkForVacation(DateMidnight date, Person person) {

        List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(date, date,
                    person)
                .stream()
                .filter(application ->
                            application.hasStatus(ApplicationStatus.WAITING)
                            || application.hasStatus(ApplicationStatus.TEMPORARY_ALLOWED)
                            || application.hasStatus(ApplicationStatus.ALLOWED))
                .collect(Collectors.toList());

        if (applications.isEmpty()) {
            return Optional.empty();
        }

        Application application = applications.get(0);

        return Optional.of(new TimedAbsence(application.getDayLength(), TimedAbsence.Type.VACATION));
    }
}
