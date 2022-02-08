package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeServiceImpl.convert;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

/**
 * Provides application for leave demo data.
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

    Application createWaitingApplication(Person person, VacationCategory vacationCategory, DayLength dayLength,
                                         LocalDate startDate, LocalDate endDate) {

        Application application = null;

        if (durationChecker.startAndEndDatesAreInCurrentYear(startDate, endDate)
            && durationChecker.durationIsGreaterThanZero(startDate, endDate, person)) {

            application = new Application();
            application.setPerson(person);
            application.setApplicationDate(startDate.minusDays(5L));
            application.setStartDate(startDate);
            application.setEndDate(endDate);
            application.setVacationType(convert(getVacationType(vacationCategory)));
            application.setDayLength(dayLength);
            application.setReason("Lorem ipsum dolor sit amet, consetetur sadipscing elitr");

            if (vacationCategory.equals(OVERTIME)) {
                if (dayLength == FULL) {
                    application.setHours(Duration.ofHours(8));
                } else {
                    application.setHours(Duration.ofHours(4));
                }
            }

            applicationInteractionService.apply(application, person, Optional.of("Ich hätte gerne Urlaub"));
        }

        return application;
    }

    Application createAllowedApplication(Person person, Person boss, VacationCategory vacationCategory, DayLength dayLength, LocalDate startDate, LocalDate endDate) {

        final Application application = createWaitingApplication(person, vacationCategory, dayLength, startDate, endDate);
        if (application != null) {
            applicationInteractionService.allow(application, boss, Optional.of("Ist in Ordnung"));
        }

        return application;
    }

    void createRejectedApplication(Person person, Person boss, VacationCategory vacationCategory, DayLength dayLength, LocalDate startDate, LocalDate endDate) {

        final Application application = createWaitingApplication(person, vacationCategory, dayLength, startDate, endDate);
        if (application != null) {
            applicationInteractionService.reject(application, boss, Optional.of("Aus organisatorischen Gründen leider nicht möglich"));
        }
    }

    void createCancelledApplication(Person person, Person boss, Person office, VacationCategory vacationCategory, DayLength dayLength, LocalDate startDate, LocalDate endDate) {

        final Application application = createAllowedApplication(person, boss, vacationCategory, dayLength, startDate, endDate);
        if (application != null) {
            applicationInteractionService.cancel(application, office, Optional.of("Urlaub wurde nicht genommen, daher storniert"));
        }
    }

    private VacationType getVacationType(VacationCategory vacationCategory) {

        VacationType vacationType = null;
        final List<VacationType> vacationTypes = vacationTypeService.getAllVacationTypes();
        for (VacationType savedVacationType : vacationTypes) {
            if (savedVacationType.isOfCategory(vacationCategory)) {
                vacationType = savedVacationType;
            }
        }
        return vacationType;
    }
}
