package org.synyx.urlaubsverwaltung.overtime;

import jakarta.transaction.Transactional;
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
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;

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
import static java.math.RoundingMode.HALF_UP;
import static java.time.Duration.ZERO;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.CREATED;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.EDITED;
import static org.synyx.urlaubsverwaltung.overtime.web.OvertimeListMapper.durationToBigDecimalInHours;
import static org.synyx.urlaubsverwaltung.overtime.web.OvertimeListMapper.hoursBigDecimalToDuration;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

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
    public OvertimeServiceImpl(
        OvertimeRepository overtimeRepository,
        OvertimeCommentRepository overtimeCommentRepository,
        ApplicationService applicationService,
        OvertimeMailService overtimeMailService,
        SettingsService settingsService,
        Clock clock
    ) {
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
    public Overtime save(Overtime overtime, Optional<String> comment, Person author) {

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
    public OvertimeComment saveComment(Overtime overtime, OvertimeCommentAction action, String comment, Person author) {

        final OvertimeComment overtimeComment = new OvertimeComment(author, overtime, action, clock);
        overtimeComment.setText(comment);

        return overtimeCommentRepository.save(overtimeComment);
    }

    @Override
    public Optional<Overtime> getOvertimeById(Long id) {
        return overtimeRepository.findById(id);
    }

    @Override
    public List<OvertimeComment> getCommentsForOvertime(Overtime overtime) {
        return overtimeCommentRepository.findByOvertimeOrderByIdDesc(overtime);
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
    public Map<Person, LeftOvertime> getLeftOvertimeTotalAndDateRangeForPersons(
        List<Person> persons,
        Map<Person, WorkingTimeCalendar> workingTimeCalendarByPerson,
        List<Application> applications,
        LocalDate start,
        LocalDate end
    ) {

        // this method is only use once, currently
        // and start/end are both in the same year, always
        final Year year = Year.from(start); // TODO really? year must be part of the signature, does it?

        final Map<Person, List<Application>> overtimeApplicationsByPerson = applications.stream()
            .filter(application -> application.getVacationType().getCategory().equals(OVERTIME))
            .filter(application -> activeStatuses().contains(application.getStatus()))
            .collect(groupingBy(Application::getPerson));

        // überstunden duration bis jahresgrenze vor `start`
        // IST: 12h
        // SOLL: 14,22h -- 32h(angesammelt) minus 17,78h(anteilig abgebaut) = 14,22h
        final Map<Person, Duration> overtimeSumBeforeYearByPerson = getOvertimeSumBeforeYear(persons, year);

        // überstunden
        // IST: leer, es wird für komplett 2025 angefragt
        // SOLL: 0h (2025 keine überstunden, 2024 waren 32h)
        final Map<Person, Duration> yearOvertimeSumByPerson =
            getTotalOvertimeUntil(persons, year.atDay(1), year.atDay(year.length()));

        // überstunden
        // IST: leer, es wird für 1.1.25 bis 31.12.25 angefragt
        // SOLL: 0h (2025 keine überstunden, 2024 waren 32h)
        final Map<Person, Duration> dateRangeOvertimeSumByPerson =
            getTotalOvertimeUntil(persons, start, end);

        // überstunden-abbau mit anträgen
        // IST: 32h / 0h
        // SOLL: 14,22h -- von 1.1.25 bis 21.12.25 sind es anteilig 14,22h überstundenabbau
        //
        // -----------------------------------------------
        // start/end: 1.1.25 bis 31.1.25
        // SOLL: 14,22h
        //
        // start/end: 1.2.25 bis 28.2.25
        // SOLL: 0h -- application ist im januar
        final Map<Person, OvertimeReduction> dateRangeOvertimeReductionByPerson =
            getOvertimeReduction(overtimeApplicationsByPerson, workingTimeCalendarByPerson, start, end);

        return persons.stream()
            .map(person -> {

                // abgebaut mit überstundenabbau anträgen
                final OvertimeReduction ueberstundenAbbauDiesesJahr =
                    dateRangeOvertimeReductionByPerson.getOrDefault(person, OvertimeReduction.identity());
                // IST: 32h
                // SOLL: 14,22h

                // TODO fixme
                final Duration angesammelteUeberstundenVorjahr = overtimeSumBeforeYearByPerson.getOrDefault(person, ZERO);
                // IST: 32h
                // SOLL: 14,22h -- 32h(angesammelt) minus 17,78h(anteilig abgebaut) = 14,22h : 14,22h

                // overall
                final Duration angesammelteUeberstundenDiesesJahr = yearOvertimeSumByPerson.getOrDefault(person, ZERO);
                // IST: 0h
                // SOLL: 0h keine überstunden gesammelt in 2025

                final Duration angesammelteUeberstundenGesamt =
                    angesammelteUeberstundenVorjahr.plus(angesammelteUeberstundenDiesesJahr).minus(ueberstundenAbbauDiesesJahr.reductionYear());
                // SOLL: 0 = 14,22 + 0 - 14,22

                // date range
                final Duration angesammelteUeberstundenDateRange = dateRangeOvertimeSumByPerson.getOrDefault(person, ZERO);
                // IST: 0h
                // SOLL: 0h -- keine überstunden angesammelt in 2025

                final Duration angesammelteUeberstundenGesamtDateRange =
                    angesammelteUeberstundenVorjahr.plus(angesammelteUeberstundenDateRange).minus(ueberstundenAbbauDiesesJahr.reductionDateRange());
                // SOLL: 0 = 14,22 + 0 - 14,22

                return Map.entry(person, new LeftOvertime(angesammelteUeberstundenGesamt, angesammelteUeberstundenGesamtDateRange));
            })
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public List<Overtime> getAllOvertimesByPersonId(Long personId) {
        return overtimeRepository.findAllByPersonId(personId);
    }

    @Override
    public Optional<Overtime> getExternalOvertimeByDate(LocalDate date, Long personId) {
        return overtimeRepository.findByPersonIdAndStartDateAndEndDateAndExternalIsTrue(personId, date, date);
    }

    private Map<Person, Duration> getOvertimeSumBeforeYear(Collection<Person> persons, Year year) {

        // year = 2025

        final Year previousYear = year.minusYears(1);
        final LocalDate lastDayOfPreviousYear = previousYear.atDay(previousYear.length());
        // 31.12.2024

        // abgebaute überstunden mit anträgen
        // von an-beginn der Zeit bis zum letzten tag des angefragten jahres
        // IST:
        // SOLL: 17,78h (17h 48min)
        final Map<Person, Duration> totalReductionPreviousYearByPerson =
            applicationService.getTotalOvertimeReductionOfPersonUntil(persons, lastDayOfPreviousYear);

        // überstunden einträge
        // von an-beginn der Zeit bis zum letzten tag des angefragten jahres
        // IST:
        // SOLL: 32h
        final Map<Person, Duration> overtimesStartingWithoutRequestedYearByPerson = getTotalOvertimeUntil(persons, lastDayOfPreviousYear);

        return persons.stream()
            .map(person -> {
                final Duration totalOvertimeReductionPreviousYear = totalReductionPreviousYearByPerson.getOrDefault(person, ZERO);
                // SOLL: 17,78h

                final Duration totalOvertimeBeforeYear = overtimesStartingWithoutRequestedYearByPerson.getOrDefault(person, ZERO);
                // SOLL: 32h

                final Duration duration = totalOvertimeBeforeYear.minus(totalOvertimeReductionPreviousYear);
                // SOLL: 32h - 17,78h = 14,22 h

                // IST: 12h
                // SOLL: 17,78h (17h 48min)
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

    private Map<Person, OvertimeReduction> getOvertimeReduction(
        Map<Person, List<Application>> overtimeApplicationsByPerson,
        Map<Person, WorkingTimeCalendar> workingTimeCalendarByPerson,
        LocalDate start,
        LocalDate end
    ) {

        // currently, start and end are always in the same year.
        final Year year = Year.from(start);
        final DateRange yearDateRange = new DateRange(year.atDay(1), year.atDay(year.length()));

        final DateRange dateRange = new DateRange(start, end);

        return overtimeApplicationsByPerson.entrySet()
            .stream()
            .map(entry -> {
                final Person person = entry.getKey();
                final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarByPerson.get(person);
                final List<Application> overtimeApplications = entry.getValue();

                final Duration reductionYear = overtimeApplications.stream()
                    .map(application -> {
                        // TODO same calculation in OvertimeListMapper#byAbsences

                        final BigDecimal arbeitstageDesAntrags = workingTimeCalendar.workingTime(application);
                        // SOLL: 4.5

                        final BigDecimal ueberstundenDesAntrags = durationToBigDecimalInHours(application.getHours());
                        // SOLL: 32.0

                        final BigDecimal ueberstundenAbbauTagesAnteil = ueberstundenDesAntrags.divide(arbeitstageDesAntrags, HALF_UP);
                        // SOLL: 7.11

                        // 0, 0.5, 1, 1.5, 2, 2.5, ... Anzahl Tage die gearbeitet wird. Wochenenden und Feiertage sind hier z. B. raus.
                        final LocalDate from = application.getStartDate().isBefore(yearDateRange.startDate()) ? yearDateRange.startDate() : application.getStartDate();
                        final LocalDate to = application.getEndDate().isAfter(yearDateRange.endDate()) ? yearDateRange.endDate() : application.getEndDate();
                        final BigDecimal arbeitstageImBerechnetenJahr = workingTimeCalendar.workingTime(from, to);
                        // SOLL: 2.5

                        final BigDecimal ueberstundenAbbauImBerechnetenJahr = ueberstundenAbbauTagesAnteil.multiply(arbeitstageImBerechnetenJahr);
                        // SOLL: 17.78

                        return hoursBigDecimalToDuration(ueberstundenAbbauImBerechnetenJahr);
                    })
                    .reduce(Duration.ZERO, Duration::plus);
                // SOLL: 14,22h -- weil nur ein kalenderjahr beachtet wird (2025)

                final Duration reductionDateRange = overtimeApplications.stream()
                    .map(application -> {
                        if (application.getStartDate().isAfter(end) || application.getEndDate().isBefore(start)) {
                            return ZERO;
                        }

                        final BigDecimal arbeitstageDesAntrags = workingTimeCalendar.workingTime(application);
                        final BigDecimal ueberstundenDesAntrags = durationToBigDecimalInHours(application.getHours());
                        final BigDecimal ueberstundenAbbauTagesAnteil = ueberstundenDesAntrags.divide(arbeitstageDesAntrags, HALF_UP);

                        final LocalDate from = application.getStartDate().isBefore(start) ? start : application.getStartDate();
                        final LocalDate to = application.getEndDate().isAfter(end) ? end : application.getEndDate();
                        final BigDecimal arbeitstageImBerechnetenJahr = workingTimeCalendar.workingTime(from, to);

                        final BigDecimal ueberstundenAbbauImBerechnetenJahr = ueberstundenAbbauTagesAnteil.multiply(arbeitstageImBerechnetenJahr);

                        return hoursBigDecimalToDuration(ueberstundenAbbauImBerechnetenJahr);
                    })
                    .reduce(Duration.ZERO, Duration::plus);

                // IST:
                // SOLL: 14,22h -- von 1.1.25 bis 21.12.25 sind es anteilig 14,22h überstundenabbau
                return Map.entry(person, new OvertimeReduction(reductionYear, reductionDateRange));
            })
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
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
            && !overtime.isExternal()
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
     * Deletes all {@link Overtime} in the database of person with id.
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

    private record OvertimeReduction(Duration reductionYear, Duration reductionDateRange) {
        static OvertimeReduction identity() {
            return new OvertimeReduction(ZERO, ZERO);
        }
    }

    private OvertimeSettings getOvertimeSettings() {
        return settingsService.getSettings().getOvertimeSettings();
    }
}
