package org.synyx.urlaubsverwaltung.overtime;

import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.Duration.ZERO;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Objects.requireNonNullElse;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.CREATED;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.EDITED;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@Transactional
@Service
class OvertimeServiceImpl implements OvertimeService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final OvertimeRepository overtimeRepository;
    private final OvertimeCommentRepository overtimeCommentRepository;
    private final ApplicationService applicationService;
    private final PersonService personService;
    private final WorkingTimeCalendarService workingTimeCalendarService;
    private final OvertimeMailService overtimeMailService;
    private final SettingsService settingsService;
    private final Clock clock;

    OvertimeServiceImpl(
        OvertimeRepository overtimeRepository,
        OvertimeCommentRepository overtimeCommentRepository,
        ApplicationService applicationService,
        PersonService personService,
        WorkingTimeCalendarService workingTimeCalendarService,
        OvertimeMailService overtimeMailService,
        SettingsService settingsService,
        Clock clock
    ) {
        this.overtimeRepository = overtimeRepository;
        this.overtimeCommentRepository = overtimeCommentRepository;
        this.applicationService = applicationService;
        this.personService = personService;
        this.workingTimeCalendarService = workingTimeCalendarService;
        this.overtimeMailService = overtimeMailService;
        this.settingsService = settingsService;
        this.clock = clock;
    }

    @Override
    public List<Overtime> getOvertimeRecordsForPersonAndYear(Person person, int year) {
        final LocalDate firstDayOfYear = Year.of(year).atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());
        return overtimeRepository.findByPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(person, firstDayOfYear, lastDayOfYear)
            .stream()
            .map(OvertimeServiceImpl::entityToOvertime)
            .toList();
    }

    @Override
    @Transactional
    public Overtime createOvertime(PersonId overtimePersonId, DateRange dateRange, Duration duration, PersonId authorId, @Nullable String comment) {

        final Map<PersonId, Person> personById = personService.getAllPersonsByIds(List.of(overtimePersonId, authorId))
            .stream()
            .collect(toMap(Person::getIdAsPersonId, identity()));

        if (personById.isEmpty()) {
            throw new IllegalStateException("Cannot create overtime with non-existent person or author.");
        }

        final OvertimeEntity entity = new OvertimeEntity();
        entity.setPerson(personById.get(overtimePersonId));
        entity.setStartDate(dateRange.startDate());
        entity.setEndDate(dateRange.endDate());
        entity.setDuration(duration);
        entity.setExternal(false);
        entity.onUpdate();

        final OvertimeEntity saved = overtimeRepository.save(entity);

        final Person authorPerson = personById.get(authorId);

        final OvertimeCommentEntity commentEntity = new OvertimeCommentEntity(clock);
        commentEntity.setOvertime(saved);
        commentEntity.setPerson(authorPerson);
        commentEntity.setText(requireNonNullElse(comment, "").strip());
        commentEntity.setAction(CREATED);

        final OvertimeCommentEntity savedCommentEntity = overtimeCommentRepository.save(commentEntity);

        sendOvertimeModifiedNotification(saved, savedCommentEntity, authorPerson);

        LOG.info("Created new overtime. overtime id={}, person id={}, author id={}",
            saved.getId(), saved.getPerson().getId(), commentEntity.getPerson().getId());

        return entityToOvertime(saved);
    }

    @Override
    @Transactional
    public Overtime updateOvertime(OvertimeId overtimeId, DateRange dateRange, Duration duration, PersonId editorId, @Nullable String comment) throws UnknownOvertimeException {

        final OvertimeEntity existingEntity = overtimeRepository.findById(overtimeId.value())
            .orElseThrow(() -> new UnknownOvertimeException(overtimeId.value()));

        final Map<PersonId, Person> personById = personService.getAllPersonsByIds(List.of(existingEntity.getPerson().getIdAsPersonId(), editorId))
            .stream()
            .collect(toMap(Person::getIdAsPersonId, identity()));

        if (personById.isEmpty()) {
            throw new IllegalStateException("Cannot update overtime with non-existent person and editor.");
        }

        existingEntity.setStartDate(dateRange.startDate());
        existingEntity.setEndDate(dateRange.endDate());
        existingEntity.setDuration(duration);

        final OvertimeEntity updated = overtimeRepository.save(existingEntity);

        final Person editorPerson = personById.get(editorId);

        final OvertimeCommentEntity commentEntity = new OvertimeCommentEntity(clock);
        commentEntity.setOvertime(updated);
        commentEntity.setPerson(editorPerson);
        commentEntity.setAction(EDITED);
        commentEntity.setText(requireNonNullElse(comment, "").strip());

        final OvertimeCommentEntity savedCommentEntity = overtimeCommentRepository.save(commentEntity);

        sendOvertimeModifiedNotification(updated, savedCommentEntity, editorPerson);

        LOG.info("Updated overtime. overtime id={}, person id={}, author id={}",
            updated.getId(), updated.getPerson().getId(), commentEntity.getPerson().getId());

        return entityToOvertime(updated);
    }

    @Override
    public OvertimeComment saveComment(OvertimeId overtimeId, OvertimeCommentAction action, String comment, Person author) {

        final OvertimeEntity overtimeEntity = overtimeRepository.findById(overtimeId.value())
            .orElseThrow(() -> new IllegalStateException("expected overtime to exist."));

        final OvertimeCommentEntity overtimeComment = new OvertimeCommentEntity(author, overtimeEntity, action, clock);
        overtimeComment.setText(comment);

        final OvertimeCommentEntity saved = overtimeCommentRepository.save(overtimeComment);

        return entityToOvertimeComment(saved);
    }

    @Override
    public Optional<Overtime> getOvertimeById(OvertimeId id) {
        return overtimeRepository.findById(id.value()).map(OvertimeServiceImpl::entityToOvertime);
    }

    @Override
    public List<OvertimeComment> getCommentsForOvertime(OvertimeId overtimeId) {
        return overtimeCommentRepository.findByOvertimeIdOrderByIdDesc(overtimeId.value()).stream()
            .map(OvertimeServiceImpl::entityToOvertimeComment)
            .toList();
    }

    @Override
    public Duration getTotalOvertimeForPersonAndYear(Person person, int year) {
        return getOvertimeRecordsForPersonAndYear(person, year).stream()
            .map(Overtime::getDurationByYear)
            .map(map -> map.getOrDefault(year, ZERO))
            .reduce(ZERO, Duration::plus);
    }

    @Override
    public Duration getTotalOvertimeForPersonBeforeYear(Person person, int year) {
        final LocalDate firstDayOfYear = Year.of(year).atDay(1);
        final LocalDate lastDayOfBeforeYear = firstDayOfYear.minusYears(1).with(lastDayOfYear());
        final Duration totalOvertimeReductionBeforeYear = applicationService.getTotalOvertimeReductionOfPersonUntil(person, lastDayOfBeforeYear);
        final Duration totalOvertimeBeforeYear = overtimeRepository.findByPersonAndStartDateIsBefore(person, firstDayOfYear)
            .stream()
            .map(OvertimeServiceImpl::entityToOvertime)
            .map(overtime -> overtime.getTotalDurationBefore(year))
            .reduce(ZERO, Duration::plus);

        return totalOvertimeBeforeYear.minus(totalOvertimeReductionBeforeYear);
    }

    @Override
    public Duration getLeftOvertimeForPerson(Person person) {
        final Duration totalOvertime = getTotalOvertimeForPerson(person);
        final Duration overtimeReduction = applicationService.getTotalOvertimeReductionOfPerson(person);

        return totalOvertime.minus(overtimeReduction);
    }

    @Override
    public Duration getLeftOvertimeForPerson(Person person, List<Long> excludingApplicationIDs) {

        final List<Application> applications = applicationService.findApplicationsByIds(excludingApplicationIDs);
        final Duration leftOvertimeForPerson = getLeftOvertimeForPerson(person);

        return applications.stream()
            .filter(application -> application.getVacationType().isOfCategory(OVERTIME))
            .map(Application::getHours)
            .reduce(leftOvertimeForPerson, Duration::plus);
    }

    @Override
    public Map<Person, LeftOvertime> getLeftOvertimeTotalAndDateRangeForPersons(List<Person> persons, List<Application> applications, LocalDate start, LocalDate end) {

        final Year year = Year.from(start);
        final DateRange dateRange = new DateRange(start, end);

        final List<Application> overtimeApplications = applications.stream()
            .filter(application -> application.getVacationType().getCategory().equals(OVERTIME))
            .filter(application -> activeStatuses().contains(application.getStatus()))
            .toList();

        final Map<Person, Duration> overtimeSumBeforeYearByPerson = getOvertimeSumBeforeYear(persons, year);
        final Map<Person, Duration> yearOvertimeSumByPerson = getTotalOvertimeUntil(persons, DateRange.ofYear(year));
        final Map<Person, Duration> dateRangeOvertimeSumByPerson = getTotalOvertimeUntil(persons, dateRange);
        final Map<Person, OvertimeReduction> overtimeReductionByPerson = getOvertimeReduction(overtimeApplications, year, dateRange);

        return persons.stream().collect(toMap(identity(), person -> {

            final OvertimeReduction overtimeReduction = overtimeReductionByPerson.getOrDefault(person, OvertimeReduction.identity());
            final Duration overallOvertimeBeforeYear = overtimeSumBeforeYearByPerson.getOrDefault(person, ZERO);

            // overall
            final Duration yearDuration = yearOvertimeSumByPerson.getOrDefault(person, ZERO);
            final Duration finalOverallDuration = overallOvertimeBeforeYear.plus(yearDuration).minus(overtimeReduction.reductionYear());

            // date range
            final Duration dateRangeDuration = dateRangeOvertimeSumByPerson.getOrDefault(person, ZERO);
            final Duration finalDateRangeDuration = overallOvertimeBeforeYear.plus(dateRangeDuration).minus(overtimeReduction.reductionDateRange());

            return new LeftOvertime(finalOverallDuration, finalDateRangeDuration);
        }));
    }

    @Override
    public List<Overtime> getAllOvertimesByPersonId(PersonId personId) {
        return overtimeRepository.findAllByPersonId(personId.value()).stream()
            .map(OvertimeServiceImpl::entityToOvertime)
            .toList();
    }

    @Override
    public Optional<Overtime> getExternalOvertimeByDate(LocalDate date, PersonId personId) {
        return overtimeRepository.findByPersonIdAndStartDateAndEndDateAndExternalIsTrue(personId.value(), date, date)
            .map(OvertimeServiceImpl::entityToOvertime);
    }

    private static Overtime entityToOvertime(OvertimeEntity entity) {
        return new Overtime(
            new OvertimeId(entity.getId()),
            entity.getPerson().getIdAsPersonId(),
            new DateRange(entity.getStartDate(), entity.getEndDate()),
            entity.getDuration(),
            entity.isExternal() ? OvertimeType.EXTERNAL : OvertimeType.UV_INTERNAL,
            entity.getLastModificationDate().atStartOfDay().toInstant(ZoneOffset.UTC)
        );
    }

    private static OvertimeComment entityToOvertimeComment(OvertimeCommentEntity entity) {

        final PersonId personId = entity.getPerson() == null ? null : entity.getPerson().getIdAsPersonId();

        return new OvertimeComment(
            new OvertimeCommentId(entity.getId()),
            entity.getOvertime().getId(),
            entity.getAction(),
            Optional.ofNullable(personId),
            entity.getDate(),
            entity.getText()
        );
    }

    private Map<Person, Duration> getOvertimeSumBeforeYear(Collection<Person> persons, Year year) {

        final Year previousYear = year.minusYears(1);
        final LocalDate lastDayOfLastYear = previousYear.atDay(previousYear.length());

        final Map<Person, Duration> totalReductionBeforeYearByPerson = applicationService.getTotalOvertimeReductionOfPersonUntil(persons, lastDayOfLastYear);
        final Map<Person, Duration> overtimesStartingWithoutRequestedYearByPerson = getTotalOvertimeUntil(persons, lastDayOfLastYear);

        return persons.stream().collect(toMap(identity(), person -> {
            final Duration totalOvertimeReductionBeforeYear = totalReductionBeforeYearByPerson.getOrDefault(person, ZERO);
            final Duration totalOvertimeBeforeYear = overtimesStartingWithoutRequestedYearByPerson.getOrDefault(person, ZERO);
            return totalOvertimeBeforeYear.minus(totalOvertimeReductionBeforeYear);
        }));
    }

    private Map<Person, Duration> getTotalOvertimeUntil(Collection<Person> persons, LocalDate until) {

        final Map<PersonId, Person> personById = persons.stream()
            .distinct()
            .collect(toMap(Person::getIdAsPersonId, identity()));

        return overtimeRepository.findByPersonIsInAndStartDateIsLessThanEqual(persons, until).stream()
            .map(OvertimeServiceImpl::entityToOvertime)
            .collect(toMap(
                overtime -> personById.get(overtime.personId()),
                overtime -> overtime.durationForDateRange(new DateRange(overtime.startDate(), until)),
                Duration::plus
            ));
    }

    private Map<Person, Duration> getTotalOvertimeUntil(List<Person> persons, DateRange dateRange) {

        final Map<PersonId, Person> personById = persons.stream()
            .distinct()
            .collect(toMap(Person::getIdAsPersonId, identity()));

        final LocalDate start = dateRange.startDate();
        final LocalDate end = dateRange.endDate();

        return overtimeRepository.findByPersonIsInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(persons, start, end).stream()
            .map(OvertimeServiceImpl::entityToOvertime)
            .collect(toMap(
                overtime -> personById.get(overtime.personId()),
                overtime -> overtime.durationForDateRange(dateRange),
                Duration::plus
            ));
    }

    private Map<Person, OvertimeReduction> getOvertimeReduction(List<Application> overtimeApplications, Year year, DateRange dateRange) {

        final DateRange yearRange = DateRange.ofYear(year);
        final Map<PersonId, WorkingTimeCalendar> workingTimeCalendarByPersonId = getWorkingTimeCalendars(overtimeApplications);

        return overtimeApplications.stream()
            .collect(toMap(Application::getPerson, application -> {
                final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarByPersonId.get(application.getPerson().getIdAsPersonId());
                final Duration yearReduction = application.getOvertimeReductionShareFor(yearRange, (personId, range) -> workingTimeCalendar);
                final Duration dateRangeReduction = application.getOvertimeReductionShareFor(dateRange, (personId, range) -> workingTimeCalendar);
                return new OvertimeReduction(yearReduction, dateRangeReduction);
            }, OvertimeReduction::plus));
    }

    /**
     * Is signedInUser person allowed to create the overtime record of personOfOvertime.
     * If overtime is active and overtime sync is inactive, the user is allowed to create the overtime record:
     * <pre>
     *  |                        |overtime active| sync active| others | own   |  others | own  |
     *  |------------------------|---------------|------------|--------|-------|---------|------|
     *  | PrivilegedOnly         | true          | false      | true   |       |  false  |      |
     *  | OFFICE                 | true          | false      | true   | true  |  true   | true |
     *  | BOSS                   | true          | false      | true   | true  |  false  | true |
     *  | SECOND_STAGE_AUTHORITY | true          | false      | true   | true  |  false  | true |
     *  | DEPARTMENT_HEAD        | true          | false      | true   | true  |  false  | true |
     *  | USER                   | true          | false      | false  | false |  false  | true |
     *
     *  if overtime is inactive, the user is not allowed to create the overtime records
     *  if overtime sync is active, the user is not allowed to create the overtime records
     * </pre>
     *
     * @param signedInUser     person which creates overtime record
     * @param personOfOvertime person which the overtime record belongs to
     * @return @code{true} if allowed, otherwise @code{false}
     */
    @Override
    public boolean isUserIsAllowedToCreateOvertime(Person signedInUser, Person personOfOvertime) {
        final OvertimeSettings overtimeSettings = getOvertimeSettings();
        return overtimeSettings.isOvertimeActive()
            && !overtimeSettings.isOvertimeSyncActive()
            &&
            (
                signedInUser.hasRole(OFFICE)
                    || (signedInUser.equals(personOfOvertime) && !overtimeSettings.isOvertimeWritePrivilegedOnly())
                    || (signedInUser.isPrivileged() && overtimeSettings.isOvertimeWritePrivilegedOnly())
            );
    }


    /**
     * Is signedInUser person allowed to update the overtime record of personOfOvertime.
     * Update is only allowed if it is not an external overtime record.
     * <pre>
     *  |                        | others | own   |  others | own  |
     *  |------------------------|--------|-------|---------|------|
     *  | PrivilegedOnly         | true   |       |  false  |      |
     *  | OFFICE                 | true   | true  |  true   | true |
     *  | BOSS                   | true   | true  |  false  | true |
     *  | SECOND_STAGE_AUTHORITY | true   | true  |  false  | true |
     *  | DEPARTMENT_HEAD        | true   | true  |  false  | true |
     *  | USER                   | false  | false |  false  | true |
     * </pre>
     *
     * @param signedInUser     person which updates an overtime record
     * @param personOfOvertime person which the overtime record belongs to
     * @return @code{true} if allowed, otherwise @code{false}
     */
    @Override
    public boolean isUserIsAllowedToUpdateOvertime(Person signedInUser, Person personOfOvertime, Overtime overtime) {
        final OvertimeSettings overtimeSettings = getOvertimeSettings();
        return overtimeSettings.isOvertimeActive()
            && !overtime.type().equals(OvertimeType.EXTERNAL)
            &&
            (
                signedInUser.hasRole(OFFICE)
                    || (signedInUser.equals(personOfOvertime) && !overtimeSettings.isOvertimeWritePrivilegedOnly())
                    || (signedInUser.isPrivileged() && overtimeSettings.isOvertimeWritePrivilegedOnly())
            );
    }

    /**
     * Is signedInUser person allowed to add a comment the overtime record of personOfOvertime.
     * <pre>
     *  |                        | others | own   |  others | own  |
     *  |------------------------|--------|-------|---------|------|
     *  | PrivilegedOnly         | true   |       |  false  |      |
     *  | OFFICE                 | true   | true  |  true   | true |
     *  | BOSS                   | true   | true  |  false  | true |
     *  | SECOND_STAGE_AUTHORITY | true   | true  |  false  | true |
     *  | DEPARTMENT_HEAD        | true   | true  |  false  | true |
     *  | USER                   | false  | false |  false  | true |
     * </pre>
     *
     * @param signedInUser     person which adds an overtime comment
     * @param personOfOvertime person which the overtime record belongs to
     * @return @code{true} if allowed, otherwise @code{false}
     */
    @Override
    public boolean isUserIsAllowedToAddOvertimeComment(Person signedInUser, Person personOfOvertime) {
        final OvertimeSettings overtimeSettings = getOvertimeSettings();
        return overtimeSettings.isOvertimeActive()
            &&
            (
                signedInUser.hasRole(OFFICE)
                    || (signedInUser.equals(personOfOvertime) && !overtimeSettings.isOvertimeWritePrivilegedOnly())
                    || (signedInUser.isPrivileged() && overtimeSettings.isOvertimeWritePrivilegedOnly())
            );
    }

    /**
     * Deletes all {@link OvertimeEntity} in the database of person with id.
     *
     * @param event deletion event with the id of the person which is deleted
     */
    @EventListener
    void deleteAll(PersonDeletedEvent event) {
        final Person personToBeDeleted = event.person();
        overtimeCommentRepository.deleteByOvertimePerson(personToBeDeleted);
        deleteCommentAuthor(personToBeDeleted);
        overtimeRepository.deleteByPerson(personToBeDeleted);
    }

    void deleteCommentAuthor(Person author) {
        final List<OvertimeCommentEntity> overtimeComments = overtimeCommentRepository.findByPerson(author);
        overtimeComments.forEach(overtimeComment -> overtimeComment.setPerson(null));
        overtimeCommentRepository.saveAll(overtimeComments);
    }

    private Duration getTotalOvertimeForPerson(Person person) {
        return overtimeRepository.calculateTotalHoursForPerson(person)
            .map(totalHours -> Math.round(totalHours * 60))
            .map(totalMinutes -> Duration.of(totalMinutes, MINUTES))
            .orElse(ZERO);
    }

    private record OvertimeReduction(Duration reductionYear, Duration reductionDateRange) {
        static OvertimeReduction identity() {
            return new OvertimeReduction(ZERO, ZERO);
        }

        OvertimeReduction plus(OvertimeReduction overtimeReduction) {
            return new OvertimeReduction(
                reductionYear.plus(overtimeReduction.reductionYear),
                reductionDateRange.plus(overtimeReduction.reductionDateRange)
            );
        }
    }

    private OvertimeSettings getOvertimeSettings() {
        return settingsService.getSettings().getOvertimeSettings();
    }

    private void sendOvertimeModifiedNotification(OvertimeEntity overtimeEntity, OvertimeCommentEntity commentEntity, Person modifierPerson) {

        if (modifierPerson.equals(overtimeEntity.getPerson())) {
            overtimeMailService.sendOvertimeNotificationToApplicantFromApplicant(overtimeEntity, commentEntity);
        } else {
            overtimeMailService.sendOvertimeNotificationToApplicantFromManagement(overtimeEntity, commentEntity, modifierPerson);
        }

        overtimeMailService.sendOvertimeNotificationToManagement(overtimeEntity, commentEntity);
    }

    private Map<PersonId, WorkingTimeCalendar> getWorkingTimeCalendars(Collection<Application> applications) {

        if (applications.isEmpty()) {
            return Map.of();
        }

        final Set<Person> persons = new HashSet<>();

        LocalDate from = LocalDate.MAX;
        LocalDate to = LocalDate.MIN;

        for (Application application : applications) {
            from = application.getStartDate().isBefore(from) ? application.getStartDate() : from;
            to = application.getEndDate().isAfter(to) ? application.getEndDate() : to;
            persons.add(application.getPerson());
        }

        return workingTimeCalendarService.getWorkingTimesByPersons(persons, new DateRange(from, to))
            .entrySet().stream().collect(toMap(
                entry -> entry.getKey().getIdAsPersonId(),
                Map.Entry::getValue
            ));
    }
}
