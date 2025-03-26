package org.synyx.urlaubsverwaltung.dev;

import org.slf4j.Logger;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.application.NotPrivilegedToApproveException;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

/**
 * Provides application for leave demo data.
 */
class ApplicationForLeaveDataProvider {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final ApplicationInteractionService applicationInteractionService;
    private final ApplicationService applicationService;
    private final DurationChecker durationChecker;
    private final VacationTypeService vacationTypeService;

    ApplicationForLeaveDataProvider(
        ApplicationInteractionService applicationInteractionService, ApplicationService applicationService,
        DurationChecker durationChecker, VacationTypeService vacationTypeService
    ) {
        this.applicationInteractionService = applicationInteractionService;
        this.applicationService = applicationService;
        this.durationChecker = durationChecker;
        this.vacationTypeService = vacationTypeService;
    }

    Optional<Application> createWaitingApplication(Person person, VacationCategory vacationCategory, DayLength dayLength, LocalDate startDate, LocalDate endDate) {

        if (!durationChecker.startAndEndDatesAreInCurrentYear(startDate, endDate) || !durationChecker.doesPersonWork(person, startDate, endDate)) {
            return Optional.empty();
        }

        final Application application = new Application();
        application.setPerson(person);
        application.setApplicationDate(startDate.minusDays(5L));
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setVacationType(getVacationType(vacationCategory));
        application.setDayLength(dayLength);
        application.setReason("Lorem ipsum dolor sit amet, consetetur sadipscing elitr");

        if (vacationCategory.equals(OVERTIME)) {
            if (dayLength == FULL) {
                application.setHours(Duration.ofHours(8));
            } else {
                application.setHours(Duration.ofHours(4));
            }
        }

        final Application savedApplication = applicationInteractionService.apply(application, person, Optional.of("Ich hätte gerne Urlaub"));
        return Optional.of(savedApplication);
    }

    Optional<Application> createAllowedApplication(Person person, Person boss, VacationCategory vacationCategory, DayLength dayLength, LocalDate startDate, LocalDate endDate) {

        final Optional<Application> maybeApplication = createWaitingApplication(person, vacationCategory, dayLength, startDate, endDate);
        if (maybeApplication.isPresent()) {
            try {
                final Application application = applicationInteractionService.allow(maybeApplication.get(), boss, Optional.of("Ist in Ordnung"));
                return Optional.of(application);
            } catch (NotPrivilegedToApproveException e) {
                LOG.info("Application cannot be allowed by user {}", boss);
            }
        }

        return maybeApplication;
    }

    void createRejectedApplication(Person person, Person boss, VacationCategory vacationCategory, DayLength dayLength, LocalDate startDate, LocalDate endDate) {
        final Optional<Application> maybeApplication = createWaitingApplication(person, vacationCategory, dayLength, startDate, endDate);
        maybeApplication.ifPresent(application -> applicationInteractionService.reject(application, boss, Optional.of("Aus organisatorischen Gründen leider nicht möglich")));
    }

    void createCancelledApplication(Person person, Person boss, Person office, VacationCategory vacationCategory, DayLength dayLength, LocalDate startDate, LocalDate endDate) {
        final Optional<Application> maybeApplication = createAllowedApplication(person, boss, vacationCategory, dayLength, startDate, endDate);
        maybeApplication.ifPresent(application -> applicationInteractionService.cancel(application, office, Optional.of("Urlaub wurde nicht genommen, daher storniert")));
    }

    boolean personHasNoApplications(Person person) {
        return applicationService.getForStatesAndPerson(List.of(ApplicationStatus.values()), List.of(person)).isEmpty();
    }

    private VacationType<?> getVacationType(VacationCategory vacationCategory) {

        VacationType<?> vacationType = null;
        final List<VacationType<?>> vacationTypes = vacationTypeService.getAllVacationTypes();
        for (VacationType<?> savedVacationType : vacationTypes) {
            if (savedVacationType.isOfCategory(vacationCategory)) {
                vacationType = savedVacationType;
            }
        }
        return vacationType;
    }
}
