package org.synyx.urlaubsverwaltung.overtime;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.util.DecimalConverter;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.math.RoundingMode.HALF_EVEN;
import static java.time.Duration.ZERO;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.CREATED;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.EDITED;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.util.DecimalConverter.toFormattedDecimal;

@Transactional
@Service
class OvertimeServiceImpl implements OvertimeService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final OvertimeRepository overtimeRepository;
    private final OvertimeCommentRepository overtimeCommentRepository;
    private final ApplicationService applicationService;
    private final OvertimeMailService overtimeMailService;
    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    public OvertimeServiceImpl(OvertimeRepository overtimeRepository, OvertimeCommentRepository overtimeCommentRepository,
                               ApplicationService applicationService, OvertimeMailService overtimeMailService,
                               SettingsService settingsService, Clock clock) {
        this.overtimeRepository = overtimeRepository;
        this.overtimeCommentRepository = overtimeCommentRepository;
        this.applicationService = applicationService;
        this.overtimeMailService = overtimeMailService;
        this.settingsService = settingsService;
        this.clock = clock;
    }

    @Override
    public List<Overtime> getOvertimeRecordsForPersonAndYear(Person person, int year) {
        final LocalDate firstDayOfYear = Year.of(year).atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());
        return overtimeRepository.findByPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(person, firstDayOfYear, lastDayOfYear);
    }

    @Override
    public Overtime record(Overtime overtime, Optional<String> comment, Person author) {

        final boolean isNewOvertime = overtime.getId() == null;

        // save overtime record
        overtime.onUpdate();
        final Overtime savedOvertime = overtimeRepository.save(overtime);

        // save comment
        final OvertimeCommentAction action = isNewOvertime ? CREATED : EDITED;
        final OvertimeComment overtimeComment = new OvertimeComment(author, savedOvertime, action, clock);
        comment.ifPresent(overtimeComment::setText);
        final OvertimeComment savedOvertimeComment = overtimeCommentRepository.save(overtimeComment);

        if (author.equals(overtime.getPerson())) {
            overtimeMailService.sendOvertimeNotificationToApplicantFromApplicant(savedOvertime, savedOvertimeComment);
        } else {
            overtimeMailService.sendOvertimeNotificationToApplicantFromManagement(savedOvertime, savedOvertimeComment, author);
        }

        overtimeMailService.sendOvertimeNotificationToManagement(savedOvertime, savedOvertimeComment);
        LOG.info("{} overtime record: {}", isNewOvertime ? "Created" : "Updated", savedOvertime);

        return savedOvertime;
    }

    @Override
    public Optional<Overtime> getOvertimeById(Integer id) {
        return overtimeRepository.findById(id);
    }

    @Override
    public List<OvertimeComment> getCommentsForOvertime(Overtime overtime) {
        return overtimeCommentRepository.findByOvertime(overtime);
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
        final Duration totalOvertimeReductionBeforeYear = applicationService.getTotalOvertimeReductionOfPersonBefore(person, firstDayOfYear);
        final Duration totalOvertimeBeforeYear = overtimeRepository.findByPersonAndStartDateIsBefore(person, firstDayOfYear).stream()
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

    // TODO remove if not needed
    @Override
    public Duration getLeftOvertimeForPerson(Person person, LocalDate start, LocalDate end) {

        final DateRange dateRangeOfPeriod = new DateRange(start, end);

        final Duration overtimeForPeriod = overtimeRepository.findByPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(person, start, end).stream()
                .map(overtime -> {
                    final DateRange overtimeDateRange = new DateRange(overtime.getStartDate(), overtime.getEndDate());
                    final Duration durationOfOverlap = dateRangeOfPeriod.overlap(overtimeDateRange).map(DateRange::duration).orElse(ZERO);
                    return toFormattedDecimal(overtime.getDuration())
                            .divide(toFormattedDecimal(overtimeDateRange.duration()), HALF_EVEN)
                            .multiply(toFormattedDecimal(durationOfOverlap))
                            .setScale(0, HALF_EVEN);
                })
                .map(DecimalConverter::toDuration)
                .reduce(ZERO, Duration::plus);

        final Duration overtimeReductionForPeriod = applicationService.getTotalOvertimeReductionOfPerson(person, start, end);

        final Duration totalOvertimeBeforeYear = getTotalOvertimeForPersonBeforeYear(person, start.getYear());
        return totalOvertimeBeforeYear.plus(overtimeForPeriod).minus(overtimeReductionForPeriod);
    }

    @Override
    public Map<Person, LeftOvertime> getLeftOvertimeTotalAndDateRangeForPersons(List<Person> persons, List<Application> applications, LocalDate start, LocalDate end) {

        final Map<Person, List<Application>> overtimeApplicationsByPerson = applications.stream()
            .filter(application -> application.getVacationType().getCategory().equals(OVERTIME))
            .filter(application -> List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED).contains(application.getStatus()))
            .collect(groupingBy(Application::getPerson));

        final Map<Person, Duration> overtimeSumBeforeYearByPerson = getOvertimeSumBeforeYear(persons, start.getYear());
        final Map<Person, Duration> yearOvertimeSumByPerson = getTotalOvertimeUntil(persons, start.with(firstDayOfYear()), start.with(lastDayOfYear()));
        final Map<Person, Duration> dateRangeOvertimeSumByPerson = getTotalOvertimeUntil(persons, start, end);
        final Map<Person, OvertimeReduction> dateRangeOvertimeReductionByPerson = getOvertimeReduction(overtimeApplicationsByPerson, start, end);

        return persons.stream()
            .map(person -> {
                final OvertimeReduction personOvertimeReduction = dateRangeOvertimeReductionByPerson.getOrDefault(person, OvertimeReduction.identity());
                final Duration overallOvertimeBeforeYear = overtimeSumBeforeYearByPerson.getOrDefault(person, ZERO);

                // overall
                final Duration yearDuration = yearOvertimeSumByPerson.getOrDefault(person, ZERO);
                final Duration finalOverallDuration = overallOvertimeBeforeYear.plus(yearDuration).minus(personOvertimeReduction.getReductionOverall());

                // date range
                final Duration dateRangeDuration = dateRangeOvertimeSumByPerson.getOrDefault(person, ZERO);
                final Duration finalDateRangeDuration = overallOvertimeBeforeYear.plus(dateRangeDuration).minus(personOvertimeReduction.getReductionDateRange());

                return Map.entry(person, new LeftOvertime(finalOverallDuration, finalDateRangeDuration));
            })
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<Person, Duration> getOvertimeSumBeforeYear(Collection<Person> persons, int year) {

        final LocalDate lastDayOfLastYear = Year.of(year - 1).atDay(1).with(lastDayOfYear());
        final Map<Person, Duration> totalReductionBeforeYearByPerson = applicationService.getTotalOvertimeReductionOfPersonUntil(persons, lastDayOfLastYear);
        final Map<Person, Duration> overtimesStartingWithoutRequestedYearByPerson = getTotalOvertimeUntil(persons, lastDayOfLastYear);
        return persons.stream()
            .map(person -> {
                final Duration totalOvertimeReductionBeforeYear = totalReductionBeforeYearByPerson.getOrDefault(person, ZERO);
                final Duration totalOvertimeBeforeYear = overtimesStartingWithoutRequestedYearByPerson.getOrDefault(person, ZERO);
                final Duration duration = totalOvertimeBeforeYear.minus(totalOvertimeReductionBeforeYear);
                return Map.entry(person, duration);
            })
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<Person, Duration> getTotalOvertimeUntil(Collection<Person> persons, LocalDate until) {
        return overtimeRepository.findByPersonIsInAndStartDateIsLessThanEqual(persons, until).stream()
            .map(overtime -> {
                final DateRange requestedDateRange = new DateRange(overtime.getStartDate(), until);
                final Duration overtimeDurationForDateRange = overtime.getDurationForDateRange(requestedDateRange);
                return Map.entry(overtime.getPerson(), overtimeDurationForDateRange);
            })
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, Duration::plus));
    }

    private Map<Person, Duration> getTotalOvertimeUntil(List<Person> persons, LocalDate start, LocalDate end) {
        final DateRange requestedDateRange = new DateRange(start, end);
        return overtimeRepository.findByPersonIsInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(persons, start, end).stream()
            .map(overtime -> {
                final Duration overtimeDurationForDateRange = overtime.getDurationForDateRange(requestedDateRange);
                return Map.entry(overtime.getPerson(), overtimeDurationForDateRange);
            })
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, Duration::plus));
    }

    private Map<Person, OvertimeReduction> getOvertimeReduction(Map<Person, List<Application>> overtimeApplicationsByPerson, LocalDate start, LocalDate end) {
        final DateRange dateRange = new DateRange(start, end);
        return overtimeApplicationsByPerson.entrySet()
            .stream()
            .map(entry -> {
                final List<Application> overtimeApplications = entry.getValue();
                final Duration totalOvertimeReductionDuration = overtimeApplications.stream()
                    .map(Application::getHours)
                    .reduce(ZERO, Duration::plus);

                final BigDecimal dateRangeOvertimeReductionSeconds = overtimeApplications.stream()
                    .filter(application -> !application.getStartDate().isBefore(start) && !application.getStartDate().isAfter(end))
                    .map(application -> {
                        final DateRange applicationDateRage = new DateRange(application.getStartDate(), application.getEndDate());
                        final Duration durationOfOverlap = dateRange.overlap(applicationDateRage).map(DateRange::duration).orElse(ZERO);
                        return toFormattedDecimal(application.getHours())
                            .divide(toFormattedDecimal(applicationDateRage.duration()), HALF_EVEN)
                            .multiply(toFormattedDecimal(durationOfOverlap)).setScale(0, HALF_EVEN);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                final Duration dateRangeOvertimeReductionDuration = Duration.ofSeconds(dateRangeOvertimeReductionSeconds.longValue());

                final Person person = entry.getKey();
                return Map.entry(person, new OvertimeReduction(totalOvertimeReductionDuration, dateRangeOvertimeReductionDuration));
            })
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Is signedInUser person allowed to write (edit or update) the overtime record of personOfOvertime.
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
     * @param signedInUser     person which writes overtime record
     * @param personOfOvertime person which the overtime record belongs to
     * @return @code{true} if allowed, otherwise @code{false}
     */
    @Override
    public boolean isUserIsAllowedToWriteOvertime(Person signedInUser, Person personOfOvertime) {
        final OvertimeSettings overtimeSettings = settingsService.getSettings().getOvertimeSettings();
        return signedInUser.hasRole(OFFICE)
            || (signedInUser.equals(personOfOvertime) && !overtimeSettings.isOvertimeWritePrivilegedOnly())
            || (signedInUser.isPrivileged() && overtimeSettings.isOvertimeWritePrivilegedOnly());
    }

    /**
     * Deletes all {@link Overtime} in the database of person with id.
     *
     * @param event deletion event with the id of the person which is deleted
     */
    @EventListener
    void deleteAll(PersonDeletedEvent event) {
        final Person personToBeDeleted = event.getPerson();
        overtimeCommentRepository.deleteByOvertimePerson(personToBeDeleted);
        deleteCommentAuthor(personToBeDeleted);
        overtimeRepository.deleteByPerson(personToBeDeleted);
    }

    void deleteCommentAuthor(Person author) {
        final List<OvertimeComment> overtimeComments = overtimeCommentRepository.findByPerson(author);
        overtimeComments.forEach(overtimeComment -> overtimeComment.setPerson(null));
        overtimeCommentRepository.saveAll(overtimeComments);
    }

    private Duration getTotalOvertimeForPerson(Person person) {
        return overtimeRepository.calculateTotalHoursForPerson(person)
            .map(totalHours -> Math.round(totalHours * 60))
            .map(totalMinutes -> Duration.of(totalMinutes, MINUTES))
            .orElse(ZERO);
    }

    private static class OvertimeReduction {

        private final Duration reductionOverall;
        private final Duration reductionDateRange;

        OvertimeReduction(Duration reductionOverall, Duration reductionDateRange) {
            this.reductionOverall = reductionOverall;
            this.reductionDateRange = reductionDateRange;
        }

        Duration getReductionOverall() {
            return reductionOverall;
        }

        Duration getReductionDateRange() {
            return reductionDateRange;
        }

        static OvertimeReduction identity() {
            return new OvertimeReduction(ZERO, ZERO);
        }
    }
}
