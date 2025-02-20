package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeServiceImpl.convert;

/**
 * Implementation of interface {@link ApplicationService}.
 */
@Service
class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final MessageSource messageSource;

    @Autowired
    ApplicationServiceImpl(ApplicationRepository applicationRepository, MessageSource messageSource) {
        this.applicationRepository = applicationRepository;
        this.messageSource = messageSource;
    }

    @Override
    public Optional<Application> getApplicationById(Long id) {
        return applicationRepository.findById(id).map(this::toApplication);
    }

    @Override
    public List<Application> findApplicationsByIds(Iterable<Long> applicationIds) {
        return toApplication(applicationRepository.findAllById(applicationIds));
    }

    @Override
    public Application save(Application application) {
        final ApplicationEntity savedEntity = applicationRepository.save(toApplicationEntity(application));
        return toApplication(savedEntity);
    }

    @Override
    public List<Application> getApplicationsForACertainPeriodAndPerson(LocalDate startDate, LocalDate endDate, Person person) {
        return toApplication(applicationRepository.getApplicationsForACertainTimeAndPerson(startDate, endDate, person));
    }

    @Override
    public List<Application> getApplicationsForACertainPeriodAndStatus(LocalDate startDate, LocalDate endDate, List<Person> persons, List<ApplicationStatus> statuses) {
        return toApplication(applicationRepository.findByPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndStatusIn(persons, startDate, endDate, statuses));
    }

    @Override
    public List<Application> getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate startDate, LocalDate endDate, Person person, List<ApplicationStatus> statuses, VacationCategory vacationCategory) {
        return toApplication(applicationRepository.findByStatusInAndPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndVacationTypeCategory(statuses, person, startDate, endDate, vacationCategory));
    }

    @Override
    public List<Application> getApplicationsForACertainPeriodAndState(LocalDate startDate, LocalDate endDate, ApplicationStatus status) {
        return toApplication(applicationRepository.getApplicationsForACertainTimeAndState(startDate, endDate, status));
    }

    @Override
    public List<Application> getApplicationsWhereApplicantShouldBeNotifiedAboutUpcomingApplication(LocalDate from, LocalDate to, List<ApplicationStatus> statuses) {
        return toApplication(applicationRepository.findByStatusInAndStartDateBetweenAndUpcomingApplicationsReminderSendIsNull(statuses, from, to));
    }

    @Override
    public List<Application> getApplicationsWhereHolidayReplacementShouldBeNotified(LocalDate from, LocalDate to, List<ApplicationStatus> statuses) {
        return toApplication(applicationRepository.findByStatusInAndStartDateBetweenAndHolidayReplacementsIsNotEmptyAndUpcomingHolidayReplacementNotificationSendIsNull(statuses, from, to));
    }

    @Override
    public List<Application> getForStates(List<ApplicationStatus> statuses) {
        return toApplication(applicationRepository.findByStatusIn(statuses));
    }

    @Override
    public List<Application> getForStatesSince(List<ApplicationStatus> statuses, LocalDate since) {
        return toApplication(applicationRepository.findByStatusInAndEndDateGreaterThanEqual(statuses, since));
    }

    @Override
    public List<Application> getForStatesAndPerson(List<ApplicationStatus> statuses, List<Person> persons) {
        return toApplication(applicationRepository.findByStatusInAndPersonIn(statuses, persons));
    }

    @Override
    public List<Application> getForStatesAndPersonSince(List<ApplicationStatus> statuses, List<Person> persons, LocalDate since) {
        return toApplication(applicationRepository.findByStatusInAndPersonInAndEndDateIsGreaterThanEqual(statuses, persons, since));
    }

    @Override
    public List<Application> getForStatesAndPerson(List<ApplicationStatus> statuses, List<Person> persons, LocalDate start, LocalDate end) {
        return toApplication(applicationRepository.findByStatusInAndPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(statuses, persons, start, end));
    }

    @Override
    public List<Application> getForStates(List<ApplicationStatus> statuses, LocalDate start, LocalDate end) {
        return toApplication(applicationRepository.findByStatusInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(statuses, start, end));
    }

    @Override
    public Duration getTotalOvertimeReductionOfPerson(Person person) {
        final BigDecimal overtimeReduction = Optional.ofNullable(applicationRepository.calculateTotalOvertimeReductionOfPerson(person)).orElse(BigDecimal.ZERO);
        return Duration.ofMinutes(overtimeReduction.multiply(BigDecimal.valueOf(60)).longValue());
    }

    @Override
    public Duration getTotalOvertimeReductionOfPersonUntil(Person person, LocalDate until) {
        return getTotalOvertimeReductionOfPersonUntil(List.of(person), until).getOrDefault(person, Duration.ZERO);
    }

    @Override
    public Map<Person, Duration> getTotalOvertimeReductionOfPersonUntil(Collection<Person> persons, LocalDate until) {

        final Map<Person, Duration> overtimeReductionByPerson = applicationRepository.findByPersonInAndVacationTypeCategoryAndStatusInAndStartDateIsLessThanEqual(persons, OVERTIME, activeStatuses(), until).stream()
            .map(applicationEntity -> {
                final Application application = toApplication(applicationEntity);
                final DateRange dateRangeOfPeriod = new DateRange(application.getStartDate(), until);
                final Duration overtimeReduction = application.getOvertimeReductionShareFor(dateRangeOfPeriod);
                return Map.entry(application.getPerson(), overtimeReduction);
            })
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, Duration::plus));

        return persons.stream()
            .map(person -> Map.entry(person, overtimeReductionByPerson.getOrDefault(person, Duration.ZERO)))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public List<Application> getForHolidayReplacement(Person holidayReplacement, LocalDate date) {
        return toApplication(applicationRepository.findByHolidayReplacements_PersonAndEndDateIsGreaterThanEqualAndStatusIn(holidayReplacement, date, activeStatuses()));
    }

    @Override
    public List<Application> deleteApplicationsByPerson(Person person) {
        return toApplication(applicationRepository.deleteByPerson(person));
    }

    @Override
    public void deleteInteractionWithApplications(Person person) {
        final List<ApplicationEntity> applicationsWithoutBoss = applicationRepository.findByBoss(person);
        applicationsWithoutBoss.forEach(application -> application.setBoss(null));
        applicationRepository.saveAll(applicationsWithoutBoss);

        final List<ApplicationEntity> applicationsWithoutCanceller = applicationRepository.findByCanceller(person);
        applicationsWithoutCanceller.forEach(application -> application.setCanceller(null));
        applicationRepository.saveAll(applicationsWithoutCanceller);

        final List<ApplicationEntity> applicationsWithoutApplier = applicationRepository.findByApplier(person);
        applicationsWithoutApplier.forEach(application -> application.setApplier(null));
        applicationRepository.saveAll(applicationsWithoutApplier);
    }

    /**
     * Deletes all application replacements of applications.
     *
     * @param event the person which is deleted
     */
    @EventListener
    void deleteHolidayReplacements(PersonDeletedEvent event) {
        final List<ApplicationEntity> applicationsWithReplacedApplicationReplacements = applicationRepository.findAllByHolidayReplacements_Person(event.person()).stream()
            .map(deleteHolidayReplacement(event.person()))
            .toList();
        applicationRepository.saveAll(applicationsWithReplacedApplicationReplacements);
    }

    private Function<ApplicationEntity, ApplicationEntity> deleteHolidayReplacement(Person deletedPerson) {
        return applicationEntity -> {
            applicationEntity.setHolidayReplacements(
                applicationEntity.getHolidayReplacements().stream()
                    .filter(holidayReplacementEntity -> !holidayReplacementEntity.getPerson().equals(deletedPerson))
                    .collect(toList())
            );
            return applicationEntity;
        };
    }

    private List<Application> toApplication(Iterable<ApplicationEntity> entityList) {
        return StreamSupport.stream(entityList.spliterator(), false).map(this::toApplication).toList();
    }

    private Application toApplication(ApplicationEntity applicationEntity) {

        final VacationType<? extends VacationType<?>> vacationType =
            convert(applicationEntity.getVacationType(), messageSource);

        final Application application = new Application();
        application.setId(applicationEntity.getId());
        application.setAddress(applicationEntity.getAddress());
        application.setApplicationDate(applicationEntity.getApplicationDate());
        application.setCancelDate(applicationEntity.getCancelDate());
        application.setEditedDate(applicationEntity.getEditedDate());
        application.setApplier(applicationEntity.getApplier());
        application.setBoss(applicationEntity.getBoss());
        application.setCanceller(applicationEntity.getCanceller());
        application.setTwoStageApproval(applicationEntity.isTwoStageApproval());
        application.setEndDate(applicationEntity.getEndDate());
        application.setStartTime(applicationEntity.getStartTime());
        application.setEndTime(applicationEntity.getEndTime());
        application.setDayLength(applicationEntity.getDayLength());
        application.setPerson(applicationEntity.getPerson());
        application.setReason(applicationEntity.getReason());
        application.setStartDate(applicationEntity.getStartDate());
        application.setStatus(applicationEntity.getStatus());
        application.setVacationType(vacationType);
        application.setRemindDate(applicationEntity.getRemindDate());
        application.setTeamInformed(applicationEntity.isTeamInformed());
        application.setHours(applicationEntity.getHours());
        application.setUpcomingHolidayReplacementNotificationSend(applicationEntity.getUpcomingHolidayReplacementNotificationSend());
        application.setUpcomingApplicationsReminderSend(applicationEntity.getUpcomingApplicationsReminderSend());
        application.setHolidayReplacements(applicationEntity.getHolidayReplacements());
        return application;
    }

    private static ApplicationEntity toApplicationEntity(Application application) {
        final ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(application.getId());
        applicationEntity.setAddress(application.getAddress());
        applicationEntity.setApplicationDate(application.getApplicationDate());
        applicationEntity.setCancelDate(application.getCancelDate());
        applicationEntity.setEditedDate(application.getEditedDate());
        applicationEntity.setApplier(application.getApplier());
        applicationEntity.setBoss(application.getBoss());
        applicationEntity.setCanceller(application.getCanceller());
        applicationEntity.setTwoStageApproval(application.isTwoStageApproval());
        applicationEntity.setEndDate(application.getEndDate());
        applicationEntity.setStartTime(application.getStartTime());
        applicationEntity.setEndTime(application.getEndTime());
        applicationEntity.setDayLength(application.getDayLength());
        applicationEntity.setPerson(application.getPerson());
        applicationEntity.setReason(application.getReason());
        applicationEntity.setStartDate(application.getStartDate());
        applicationEntity.setStatus(application.getStatus());
        applicationEntity.setVacationType(convert(application.getVacationType()));
        applicationEntity.setRemindDate(application.getRemindDate());
        applicationEntity.setTeamInformed(application.isTeamInformed());
        applicationEntity.setHours(application.getHours());
        applicationEntity.setUpcomingHolidayReplacementNotificationSend(application.getUpcomingHolidayReplacementNotificationSend());
        applicationEntity.setUpcomingApplicationsReminderSend(application.getUpcomingApplicationsReminderSend());
        applicationEntity.setHolidayReplacements(application.getHolidayReplacements());
        return applicationEntity;
    }
}
