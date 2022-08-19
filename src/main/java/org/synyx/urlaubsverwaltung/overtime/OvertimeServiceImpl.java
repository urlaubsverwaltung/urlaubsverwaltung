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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.math.RoundingMode.HALF_EVEN;
import static java.time.Duration.ZERO;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.CREATED;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.EDITED;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.util.DecimalConverter.toFormattedDecimal;

/**
 * @since 2.11.0
 */
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
        return overtimeRepository.findByPersonAndStartDateBetweenOrderByStartDateDesc(person, Year.of(year).atDay(1), DateUtil.getLastDayOfYear(year));
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

        overtimeMailService.sendOvertimeNotification(savedOvertime, savedOvertimeComment);
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
            .map(Overtime::getDuration)
            .reduce(ZERO, Duration::plus);
    }

    @Override
    public Duration getTotalOvertimeForPersonBeforeYear(Person person, int year) {
        final LocalDate firstDayOfYear = Year.of(year).atDay(1);
        final Duration totalOvertimeReductionBeforeYear = applicationService.getTotalOvertimeReductionOfPersonBefore(person, firstDayOfYear);
        final Duration totalOvertimeBeforeYear = overtimeRepository.findByPersonAndStartDateIsBefore(person, firstDayOfYear).stream()
            .map(Overtime::getDuration)
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
    public Map<Person, LeftOvertime> getLeftOvertimeTotalAndDateRangeForPersons(List<Person> persons, List<Application> applications, LocalDate start, LocalDate end) {

        final Map<Person, List<Application>> overtimeApplicationsByPerson = applications.stream()
            .filter(application -> application.getVacationType().getCategory().equals(OVERTIME))
            .collect(groupingBy(Application::getPerson));

        final Map<Person, OvertimeReduction> reductionByPerson = getTotalOvertimeReduction(overtimeApplicationsByPerson, start, end);

        final List<OvertimeDurationSum> overallOvertimeDurations = overtimeRepository.calculateTotalHoursForPersons(persons);
        final List<OvertimeDurationSum> dateRangeOvertimeDurations = overtimeRepository.calculateTotalHoursForPersons(persons, start, end);

        final Map<Person, Double> overallHoursByPerson = overallOvertimeDurations.stream().collect(toMap(OvertimeDurationSum::getPerson, OvertimeDurationSum::getDurationDouble));
        final Map<Person, Double> dateRangeHoursByPerson = dateRangeOvertimeDurations.stream().collect(toMap(OvertimeDurationSum::getPerson, OvertimeDurationSum::getDurationDouble));

        final DateRange dateRange = new DateRange(start, end);

        return persons.stream()
            .map(person -> {
                final OvertimeReduction personOvertimeReduction = reductionByPerson.getOrDefault(person, OvertimeReduction.identity());

                // overall
                final Double overallDouble = overallHoursByPerson.getOrDefault(person, 0d);
                final Duration overallDuration = hoursToDuration(BigDecimal.valueOf(overallDouble));
                final Duration finalOverallDuration = overallDuration.minus(personOvertimeReduction.getReductionOverall());

                // date range
                final Double dateRangeDouble = dateRangeHoursByPerson.getOrDefault(person, 0d);
                final Duration dateRangeDuration = hoursToDuration(BigDecimal.valueOf(dateRangeDouble));
                // TODO avoid database call `getTotalOvertimeForPersonBeforeYear`
                final Duration overallOvertimeBeforeYear = getTotalOvertimeForPersonBeforeYear(person, start.getYear());
                final Duration finalDateRangeDuration = overallOvertimeBeforeYear.plus(dateRangeDuration).minus(personOvertimeReduction.getReductionDateRange());

                return Map.entry(person, new LeftOvertime(finalOverallDuration, finalDateRangeDuration));
            })
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<Person, OvertimeReduction> getTotalOvertimeReduction(Map<Person, List<Application>> overtimeApplicationsByPerson, LocalDate start, LocalDate end) {
        final DateRange dateRange = new DateRange(start, end);
        return overtimeApplicationsByPerson.entrySet()
            .stream()
            .map(entry -> {
                final Person person = entry.getKey();
                final List<Application> overtimeApplications = entry.getValue();

                final BigDecimal dateRangeOvertimeReductionSeconds = overtimeApplications.stream().map(application -> {
                    final DateRange applicationDateRage = new DateRange(application.getStartDate(), application.getEndDate());
                    final Duration durationOfOverlap = dateRange.overlap(applicationDateRage).map(DateRange::duration).orElse(ZERO);
                    return toFormattedDecimal(application.getHours())
                        .divide(toFormattedDecimal(applicationDateRage.duration()), HALF_EVEN)
                        .multiply(toFormattedDecimal(durationOfOverlap)).setScale(0, HALF_EVEN);
                }).reduce(BigDecimal.ZERO, BigDecimal::add);

                final Duration totalOvertimeReductionDuration = overtimeApplications.stream().map(Application::getHours).reduce(ZERO, Duration::plus);
                final Duration dateRangeOvertimeReductionDuration = Duration.ofSeconds(dateRangeOvertimeReductionSeconds.longValue());

                return Map.entry(person, new OvertimeReduction(totalOvertimeReductionDuration, dateRangeOvertimeReductionDuration));
            })
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Duration hoursToDuration(BigDecimal hours) {
        final long minutes = hours.multiply(BigDecimal.valueOf(60)).setScale(0, HALF_EVEN).longValue();
        return Duration.ofMinutes(minutes);
    }

    @Override
    public Duration getLeftOvertimeForPerson(Person person, LocalDate start, LocalDate end) {

        final DateRange dateRangeOfPeriod = new DateRange(start, end);

        final Duration overtimeForPeriod = overtimeRepository.findByPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(person, start, end).stream()
            .map(overtime -> {
                final DateRange overtimeDateRange = new DateRange(overtime.getStartDate(), overtime.getEndDate());
                final Duration durationOfOverlap = dateRangeOfPeriod.overlap(overtimeDateRange).map(DateRange::duration).orElse(ZERO);
                return toFormattedDecimal(overtime.getDuration())
                    .divide(toFormattedDecimal(overtimeDateRange.duration()), HALF_EVEN)
                    .multiply(toFormattedDecimal(durationOfOverlap)).setScale(0, HALF_EVEN);
            })
            .map(DecimalConverter::toDuration)
            .reduce(ZERO, Duration::plus);

        final Duration overtimeReductionForPeriod = applicationService.getTotalOvertimeReductionOfPerson(person, start, end);

        final Duration totalOvertimeBeforeYear = getTotalOvertimeForPersonBeforeYear(person, start.getYear());
        return totalOvertimeBeforeYear.plus(overtimeForPeriod).minus(overtimeReductionForPeriod);
    }

    /**
     * Is signedInUser person allowed to write (edit or update) the overtime record of personOfOvertime.
     * <pre>
     *  |                        | others | own   |  others | own  |
     *  |------------------------|--------|-------|---------|------|
     *  | PrivilegedOnly         | true   |       |  false  |      |
     *  | OFFICE                 | true   | true  |  true   | true |
     *  | BOSS                   | false  | true  |  false  | true |
     *  | SECOND_STAGE_AUTHORITY | false  | true  |  false  | true |
     *  | DEPARTMENT_HEAD        | false  | true  |  false  | true |
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
            || signedInUser.equals(personOfOvertime) && (!overtimeSettings.isOvertimeWritePrivilegedOnly() || signedInUser.isPrivileged());
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
