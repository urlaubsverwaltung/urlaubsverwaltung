package org.synyx.urlaubsverwaltung.dev;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;

import java.util.Optional;


/**
 * Provides sick note test data.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component
class ApplicationForLeaveDataProvider {

    private final ApplicationInteractionService applicationInteractionService;
    private final DurationChecker durationChecker;

    @Autowired
    ApplicationForLeaveDataProvider(ApplicationInteractionService applicationInteractionService,
        DurationChecker durationChecker) {

        this.applicationInteractionService = applicationInteractionService;
        this.durationChecker = durationChecker;
    }

    Application createWaitingApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = null;

        if (durationChecker.startAndEndDatesAreInCurrentYear(startDate, endDate)
                && durationChecker.durationIsGreaterThanZero(startDate, endDate, person)) {
            application = new Application();
            application.setPerson(person);
            application.setStartDate(startDate);
            application.setEndDate(endDate);
            application.setVacationType(vacationType);
            application.setDayLength(dayLength);
            application.setReason(
                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt"
                + "ut labore et dolore magna aliquyam erat, sed diam voluptua."
                + "At vero eos et accusam et justo duo dolores");

            if (vacationType.getCategory().equals(VacationCategory.OVERTIME)) {
                switch (dayLength) {
                    case FULL:
                        application.setHours(new BigDecimal("8"));
                        break;

                    default:
                        application.setHours(new BigDecimal("4"));
                        break;
                }
            }

            applicationInteractionService.apply(application, person, Optional.of("Ich hätte gerne Urlaub"));
        }

        return application;
    }


    Application createPremilinaryAllowedApplication(Person person, Person headOf, VacationType vacationType,
        DayLength dayLength, DateMidnight startDate, DateMidnight endDate) {

        Application application = createWaitingApplication(person, vacationType, dayLength, startDate, endDate);
        application.setTwoStageApproval(true);
        application = applicationInteractionService.allow(application, headOf, Optional.of("Erst mal OK"));

        return application;
    }


    Application createAllowedApplication(Person person, Person boss, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = createWaitingApplication(person, vacationType, dayLength, startDate, endDate);

        if (application != null) {
            applicationInteractionService.allow(application, boss, Optional.of("Ist in Ordnung"));
        }

        return application;
    }


    Application createRejectedApplication(Person person, Person boss, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = createWaitingApplication(person, vacationType, dayLength, startDate, endDate);

        if (application != null) {
            applicationInteractionService.reject(application, boss,
                Optional.of("Aus organisatorischen Gründen leider nicht möglich"));
        }

        return application;
    }


    Application createCancelledApplication(Person person, Person office, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = createAllowedApplication(person, office, vacationType, dayLength, startDate, endDate);

        if (application != null) {
            applicationInteractionService.cancel(application, office,
                Optional.of("Urlaub wurde nicht genommen, daher storniert"));
        }

        return application;
    }
}
