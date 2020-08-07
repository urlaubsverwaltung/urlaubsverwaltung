package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;


/**
 * Provides sick note test data.
 */
class ApplicationForLeaveDataProvider {

    private final ApplicationInteractionService applicationInteractionService;
    private final DurationChecker durationChecker;
    private final VacationTypeService vacationTypeService;

    ApplicationForLeaveDataProvider(ApplicationInteractionService applicationInteractionService,
                                    DurationChecker durationChecker, VacationTypeService vacationTypeService) {

        this.applicationInteractionService = applicationInteractionService;
        this.durationChecker = durationChecker;
        this.vacationTypeService = vacationTypeService;
    }

    Application createWaitingApplication(Person person, VacationCategory vacationCategory, DayLength dayLength, Instant startDate, Instant endDate) {

        Application application = null;

        if (durationChecker.startAndEndDatesAreInCurrentYear(startDate, endDate)
            && durationChecker.durationIsGreaterThanZero(startDate, endDate, person)) {

            final VacationType vacationType = getVacationType(vacationCategory);

            application = new Application();
            application.setPerson(person);
            application.setApplicationDate(startDate.minus(5L, ChronoUnit.DAYS));
            application.setStartDate(startDate);
            application.setEndDate(endDate);
            application.setVacationType(vacationType);
            application.setDayLength(dayLength);
            application.setReason(
                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt"
                    + "ut labore et dolore magna aliquyam erat, sed diam voluptua."
                    + "At vero eos et accusam et justo duo dolores");

            if (vacationCategory.equals(OVERTIME)) {
                if (dayLength == FULL) {
                    application.setHours(new BigDecimal("8"));
                } else {
                    application.setHours(new BigDecimal("4"));
                }
            }

            applicationInteractionService.apply(application, person, Optional.of("Ich hätte gerne Urlaub"));
        }

        return application;
    }


    Application createAllowedApplication(Person person, Person boss, VacationCategory vacationCategory, DayLength dayLength, Instant startDate, Instant endDate) {

        final Application application = createWaitingApplication(person, vacationCategory, dayLength, startDate, endDate);

        if (application != null) {
            applicationInteractionService.allow(application, boss, Optional.of("Ist in Ordnung"));
        }

        return application;
    }


    void createRejectedApplication(Person person, Person boss, VacationCategory vacationCategory, DayLength dayLength, Instant startDate, Instant endDate) {

        final Application application = createWaitingApplication(person, vacationCategory, dayLength, startDate, endDate);

        if (application != null) {
            applicationInteractionService.reject(application, boss, Optional.of("Aus organisatorischen Gründen leider nicht möglich"));
        }
    }


    void createCancelledApplication(Person person, Person office, VacationCategory vacationCategory, DayLength dayLength, Instant startDate, Instant endDate) {

        final Application application = createAllowedApplication(person, office, vacationCategory, dayLength, startDate, endDate);

        if (application != null) {
            applicationInteractionService.cancel(application, office, Optional.of("Urlaub wurde nicht genommen, daher storniert"));
        }
    }

    private VacationType getVacationType(VacationCategory vacationCategory) {

        VacationType vacationType = null;
        final List<VacationType> vacationTypes = vacationTypeService.getVacationTypes();
        for (VacationType savedVacationType : vacationTypes) {
            if (savedVacationType.isOfCategory(vacationCategory)) {
                vacationType = savedVacationType;
            }
        }
        return vacationType;
    }
}
