package org.synyx.urlaubsverwaltung.absence;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.AbsencePeriod.RecordMorningNoWorkday;
import org.synyx.urlaubsverwaltung.absence.AbsencePeriod.RecordNoonNoWorkday;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHoliday;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

@Service
public class AbsenceServiceImpl implements AbsenceService {

    private static final List<ApplicationStatus> APPLICATION_STATUSES = List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
    private static final List<SickNoteStatus> SICK_NOTE_STATUSES = List.of(ACTIVE);

    private final ApplicationService applicationService;
    private final SickNoteService sickNoteService;
    private final SettingsService settingsService;
    private final WorkingTimeService workingTimeService;
    private final PublicHolidaysService publicHolidaysService;

    @Autowired
    public AbsenceServiceImpl(ApplicationService applicationService, SickNoteService sickNoteService,
                              SettingsService settingsService, WorkingTimeService workingTimeService,
                              PublicHolidaysService publicHolidaysService) {

        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
        this.settingsService = settingsService;
        this.workingTimeService = workingTimeService;
        this.publicHolidaysService = publicHolidaysService;
    }

    @Override
    public List<AbsencePeriod> getOpenAbsences(Person person, LocalDate start, LocalDate end) {
        return getOpenAbsences(List.of(person), start, end);
    }

    @Override
    public List<AbsencePeriod> getOpenAbsences(List<Person> persons, LocalDate start, LocalDate end) {
        final DateRange askedDateRange = new DateRange(start, end);
        final List<WorkingTime> workingTimeList = workingTimeService.getByPersons(persons);
        final FederalState systemDefaultFederalState = workingTimeService.getSystemDefaultFederalState();

        final List<Application> openApplications = applicationService.getForStatesAndPerson(APPLICATION_STATUSES, persons, start, end);
        final List<AbsencePeriod> applicationAbsences = generateAbsencePeriodFromApplication(openApplications, askedDateRange, workingTimeList, systemDefaultFederalState);

        final List<SickNote> openSickNotes = sickNoteService.getForStatesAndPerson(SICK_NOTE_STATUSES, persons, start, end);
        final List<AbsencePeriod> sickNoteAbsences = generateAbsencePeriodFromSickNotes(openSickNotes, askedDateRange, workingTimeList, systemDefaultFederalState);

        return Stream.concat(applicationAbsences.stream(), sickNoteAbsences.stream()).collect(toList());
    }

    @Override
    public Map<Person, Map<LocalDate, List<AbsencePeriod>>> getOpenAbsencesForPersons(List<Person> persons, LocalDate start, LocalDate end) {

        final DateRange askedDateRange = new DateRange(start, end);
        final FederalState systemDefaultFederalState = workingTimeService.getSystemDefaultFederalState();

        final List<Application> openApplications = applicationService.getForStatesAndPerson(APPLICATION_STATUSES, persons, start, end);
        final Map<Person, List<Application>> applicationsByPerson = openApplications.stream().collect(groupingBy(Application::getPerson));

        final List<SickNote> openSickNotes = sickNoteService.getForStatesAndPerson(SICK_NOTE_STATUSES, persons, start, end);
        final Map<Person, List<SickNote>> sickNotesByPerson = openSickNotes.stream().collect(groupingBy(SickNote::getPerson));

        final Map<Person, Map<LocalDate, List<AbsencePeriod>>> openAbsencesByPerson = new HashMap<>();

        final Map<Person, LocalDate> personNextDateCursor = persons.stream().collect(toMap(Function.identity(), unused -> askedDateRange.getStartDate()));

        final Map<Person, Map<LocalDate, List<AbsenceTuple>>> absenceTuplesByPersonAndStartDate = new HashMap<>();
        final Map<Person, Map<LocalDate, WorkingTime>> workingTimesByPerson = new HashMap<>();

        for (LocalDate askedDateCursor : askedDateRange) {
            for (Person person : persons) {
                // note the side effects of collections!
                // stuff will be computed if absent here, as well as further below.
                final Map<LocalDate, List<AbsencePeriod>> absencePeriodsByDate = openAbsencesByPerson.computeIfAbsent(person, p -> new HashMap<>());
                final Map<LocalDate, List<AbsenceTuple>> personAbsenceTuples = absenceTuplesByPersonAndStartDate.computeIfAbsent(person, p -> absenceTuplesForPerson(askedDateRange, p, applicationsByPerson, sickNotesByPerson));
                final List<AbsenceTuple> absenceTuples = personAbsenceTuples.getOrDefault(askedDateCursor, List.of());
                final Map<LocalDate, WorkingTime> workingTimeByDate = workingTimesByPerson.computeIfAbsent(person, p -> workingTimeForPerson(p, askedDateRange));

                if (personNextDateCursor.get(person).equals(askedDateCursor)) {
                    if (absenceTuples.isEmpty()) {
                        // neither applicationForLeave nor sickNote
                        if (isWorkday(askedDateCursor, List.of(workingTimeByDate.get(askedDateCursor)))) {
                            // person works -> no absences and therefore empty list
                            absencePeriodsByDate.put(askedDateCursor, List.of());
                        } else {
                            // person does not work
                            final RecordMorningNoWorkday morning = new RecordMorningNoWorkday();
                            final RecordNoonNoWorkday noon = new RecordNoonNoWorkday();
                            final AbsencePeriod.Record record = new AbsencePeriod.Record(askedDateCursor, person, morning, noon);
                            absencePeriodsByDate.put(askedDateCursor, List.of(new AbsencePeriod(List.of(record))));
                        }
                        // next date to check absences for
                        personNextDateCursor.put(person, askedDateCursor.plusDays(1));
                    }
                    else {
                        // current date has at least on absence
                        //
                        for (AbsenceTuple absenceTuple : absenceTuples) {
                            // absence date range could overlap the requested date range
                            final LocalDate startDate = absenceTuple.getStartDate().isBefore(askedDateRange.getStartDate()) ? askedDateRange.getStartDate() : absenceTuple.startDate;
                            final LocalDate endDate = absenceTuple.getEndDate().isAfter(askedDateRange.getEndDate()) ? askedDateRange.getEndDate() : absenceTuple.endDate;

                            final List<AbsencePeriod.Record> records = new ArrayList<>();
                            for (LocalDate absenceDateCursor : new DateRange(startDate, endDate)) {

                                final WorkingTime workingTime = workingTimeByDate.get(absenceDateCursor);
                                final List<WorkingTime> personsWorkingTimeDateList = List.of(workingTime);

                                if (!isWorkday(absenceDateCursor, personsWorkingTimeDateList)) {
                                    // no-workday trumps other absence
                                    records.add(new AbsencePeriod.Record(absenceDateCursor, person, new RecordMorningNoWorkday(), new RecordNoonNoWorkday()));
                                } else if (absenceTuple.getApplication() != null) {
                                    // applicationForLeave
                                    final DateDayLengthTuple dateDayLengthTuple = new DateDayLengthTuple(absenceDateCursor, publicHolidayAbsence(absenceDateCursor, personsWorkingTimeDateList, systemDefaultFederalState));
                                    if (!dateDayLengthTuple.publicHolidayDayLength.equals(DayLength.FULL)) {
                                        // no public holiday -> add absence entry for applicationForLeave
                                        records.add(toVacationAbsencePeriodRecord(dateDayLengthTuple, absenceTuple.getApplication(), personsWorkingTimeDateList));
                                    }
                                } else if (absenceTuple.getSickNote() != null) {
                                    // sickNote
                                    final DateDayLengthTuple dateDayLengthTuple = new DateDayLengthTuple(absenceDateCursor, publicHolidayAbsence(absenceDateCursor, personsWorkingTimeDateList, systemDefaultFederalState));
                                    if (!dateDayLengthTuple.publicHolidayDayLength.equals(DayLength.FULL)) {
                                        // no public holiday -> add absence entry for sickNote
                                        records.add(toSickAbsencePeriodRecord(dateDayLengthTuple, absenceTuple.getSickNote(), personsWorkingTimeDateList));
                                    }
                                }
                            }

                            // there could be applicationForLeave on morning and sickNote on noon.
                            final List<AbsencePeriod> absencePeriodsForStartDate = absencePeriodsByDate.computeIfAbsent(startDate, localDate -> new ArrayList<>());
                            absencePeriodsForStartDate.add(new AbsencePeriod(records));

                            // next date to check absences for
                            personNextDateCursor.put(person, endDate.plusDays(1));
                        }
                    }
                } else {
                    // no absence starts at current date -> empty list
                    absencePeriodsByDate.put(askedDateCursor, List.of());
                }
            }
        }

        return openAbsencesByPerson;
    }

    private Map<LocalDate, List<AbsenceTuple>> absenceTuplesForPerson(DateRange dateRange, Person person, Map<Person, List<Application>> applicationsByPerson, Map<Person, List<SickNote>> sickNotesByPerson) {
        final List<AbsenceTuple> absenceTuples = new ArrayList<>();
        absenceTuples.addAll(applicationsByPerson.getOrDefault(person, List.of()).stream().map(application -> new AbsenceTuple(application, dateRange)).collect(toList()));
        absenceTuples.addAll(sickNotesByPerson.getOrDefault(person, List.of()).stream().map(sickNote -> new AbsenceTuple(sickNote, dateRange)).collect(toList()));
        absenceTuples.sort(comparing(AbsenceTuple::getStartDate).thenComparing(AbsenceTuple::getDayLength));
        return absenceTuples.stream().collect(groupingBy(AbsenceTuple::getStartDate));
    }

    private Map<LocalDate, WorkingTime> workingTimeForPerson(Person person, DateRange dateRange) {
        return toLocalDateWorkingTime(workingTimeService.getWorkingTimesByPersonAndDateRange(person, dateRange));
    }

    private static class AbsenceTuple {
        private final Application application;
        private final SickNote sickNote;

        private final LocalDate startDate;
        private final LocalDate endDate;
        private final DayLength dayLength;

        AbsenceTuple(Application application, DateRange dateRange) {
            this.application = application;
            this.startDate = application.getStartDate().isAfter(dateRange.getStartDate()) ? application.getStartDate() : dateRange.getStartDate();
            this.endDate = application.getEndDate().isBefore(dateRange.getEndDate()) ? application.getEndDate() : dateRange.getEndDate();
            this.dayLength = application.getDayLength();
            this.sickNote = null;
        }

        AbsenceTuple(SickNote sickNote, DateRange dateRange) {
            this.application = null;
            this.sickNote = sickNote;
            this.startDate = sickNote.getStartDate().isAfter(dateRange.getStartDate()) ? sickNote.getStartDate() : dateRange.getStartDate();
            this.endDate = sickNote.getEndDate().isBefore(dateRange.getEndDate()) ? sickNote.getEndDate() : dateRange.getEndDate();
            this.dayLength = sickNote.getDayLength();
        }

        Application getApplication() {
            return application;
        }

        SickNote getSickNote() {
            return sickNote;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public DayLength getDayLength() {
            return dayLength;
        }
    }

    private Map<LocalDate, WorkingTime> toLocalDateWorkingTime(Map<DateRange, WorkingTime> workingTimes) {
        final Map<LocalDate, WorkingTime> localDateWorkingTimeMap = new HashMap<>();
        workingTimes.forEach((key, value) -> key.iterator().forEachRemaining(localDate -> localDateWorkingTimeMap.put(localDate, value)));
        return localDateWorkingTimeMap;
    }

    @Override
    public List<Absence> getOpenAbsencesSince(List<Person> persons, LocalDate since) {
        final List<Application> openApplications = applicationService.getForStatesAndPersonSince(APPLICATION_STATUSES, persons, since);
        final List<Absence> applicationAbsences = generateAbsencesFromApplication(openApplications);

        final List<SickNote> openSickNotes = sickNoteService.getForStatesAndPersonSince(SICK_NOTE_STATUSES, persons, since);
        final List<Absence> sickNoteAbsences = generateAbsencesFromSickNotes(openSickNotes);

        return ListUtils.union(applicationAbsences, sickNoteAbsences);
    }

    @Override
    public List<Absence> getOpenAbsencesSince(LocalDate since) {
        final List<Application> openApplications = applicationService.getForStatesSince(APPLICATION_STATUSES, since);
        final List<Absence> applicationAbsences = generateAbsencesFromApplication(openApplications);

        final List<SickNote> openSickNotes = sickNoteService.getForStates(SICK_NOTE_STATUSES);
        final List<Absence> sickNoteAbsences = generateAbsencesFromSickNotes(openSickNotes);

        return ListUtils.union(applicationAbsences, sickNoteAbsences);
    }

    private List<Absence> generateAbsencesFromApplication(List<Application> applications) {
        final AbsenceTimeConfiguration config = getAbsenceTimeConfiguration();
        return applications.stream()
            .map(application -> new Absence(application.getPerson(), application.getPeriod(), config))
            .collect(toList());
    }

    private List<AbsencePeriod> generateAbsencePeriodFromApplication(List<Application> applications, DateRange askedDateRange, List<WorkingTime> workingTimeList, FederalState systemDefaultFederalState) {
        return applications.stream()
            .map(application -> toAbsencePeriod(application, askedDateRange, workingTimeList, systemDefaultFederalState))
            .collect(toList());
    }

    private List<Absence> generateAbsencesFromSickNotes(List<SickNote> sickNotes) {
        final AbsenceTimeConfiguration config = getAbsenceTimeConfiguration();
        return sickNotes.stream()
            .map(sickNote -> new Absence(sickNote.getPerson(), sickNote.getPeriod(), config))
            .collect(toList());
    }

    private List<AbsencePeriod> generateAbsencePeriodFromSickNotes(List<SickNote> sickNotes, DateRange askedDateRange, List<WorkingTime> workingTimeList, FederalState systemDefaultFederalState) {
        return sickNotes.stream()
            .map(sickNote -> toAbsencePeriod(sickNote, askedDateRange, workingTimeList, systemDefaultFederalState))
            .collect(toList());
    }

    private AbsencePeriod toAbsencePeriod(Application application, DateRange askedDateRange, List<WorkingTime> workingTimeList, FederalState systemDefaultFederalState) {
        return new AbsencePeriod(days(application, askedDateRange, workingTimeList, systemDefaultFederalState));
    }

    private AbsencePeriod.Record.AbsenceStatus toAbsenceStatus(ApplicationStatus applicationStatus) {
        switch (applicationStatus) {
            case ALLOWED:
                return AbsencePeriod.Record.AbsenceStatus.ALLOWED;
            case WAITING:
                return AbsencePeriod.Record.AbsenceStatus.WAITING;
            case TEMPORARY_ALLOWED:
                return AbsencePeriod.Record.AbsenceStatus.TEMPORARY_ALLOWED;
            case ALLOWED_CANCELLATION_REQUESTED:
                return AbsencePeriod.Record.AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED;
            default:
                throw new IllegalStateException("application status not expected here.");
        }
    }

    private List<AbsencePeriod.Record> days(Application application, DateRange askedDateRange, List<WorkingTime> workingTimeList, FederalState systemDefaultFederalState) {

        final LocalDate start = maxDate(application.getStartDate(), askedDateRange.getStartDate());
        final LocalDate end = minDate(application.getEndDate(), askedDateRange.getEndDate());

        return new DateRange(start, end).stream()
            .map(date -> new DateDayLengthTuple(date, publicHolidayAbsence(date, workingTimeList, systemDefaultFederalState)))
            // ignore full public holiday since it is no "absence".
            // it could still be an official workday with an application for leave.
            .filter(tuple -> !tuple.publicHolidayDayLength.equals(DayLength.FULL))
            .map(tuple -> toVacationAbsencePeriodRecord(tuple, application, List.of()))
            .collect(toList());
    }

    private boolean isWorkday(LocalDate date, List<WorkingTime> workingTimeList) {
        return workingTimeList
            .stream()
            .filter(w -> w.getValidFrom().isBefore(date) || w.getValidFrom().isEqual(date))
            .findFirst()
            .map(w -> w.isWorkingDay(date.getDayOfWeek()))
            .orElse(false);
    }

    private AbsencePeriod.Record toVacationAbsencePeriodRecord(DateDayLengthTuple tuple, Application application, List<WorkingTime> workingTimeList) {

        final Person person = application.getPerson();
        final AbsencePeriod.Record.AbsenceStatus status = toAbsenceStatus(application.getStatus());

        if (!workingTimeList.isEmpty() && !isWorkday(tuple.date, workingTimeList)) {
            final AbsencePeriod.RecordMorning morning = new RecordMorningNoWorkday();
            final AbsencePeriod.RecordNoon noon = new RecordNoonNoWorkday();
            return new AbsencePeriod.Record(tuple.date, person, morning, noon);
        }

        if (tuple.publicHolidayDayLength.isHalfDay()) {
            final AbsencePeriod.RecordMorning morning;
            final AbsencePeriod.RecordNoon noon;
            if (tuple.publicHolidayDayLength.equals(DayLength.MORNING)) {
                morning = null;
                noon = new AbsencePeriod.RecordNoonVacation(application.getId(), status);
            } else if (tuple.publicHolidayDayLength.equals(DayLength.NOON)) {
                morning = new AbsencePeriod.RecordMorningVacation(application.getId(), status);
                noon = null;
            } else {
                morning = new AbsencePeriod.RecordMorningVacation(application.getId(), status);
                noon = new AbsencePeriod.RecordNoonVacation(application.getId(), status);
            }
            return new AbsencePeriod.Record(tuple.date, person, morning, noon);
        }

        final AbsencePeriod.RecordMorningVacation morning;
        final AbsencePeriod.RecordNoonVacation noon;

        if (DayLength.MORNING.equals(application.getDayLength())) {
            morning = new AbsencePeriod.RecordMorningVacation(application.getId(), status);
            noon = null;
        } else if (DayLength.NOON.equals(application.getDayLength())) {
            morning = null;
            noon = new AbsencePeriod.RecordNoonVacation(application.getId(), status);
        } else {
            morning = new AbsencePeriod.RecordMorningVacation(application.getId(), status);
            noon = new AbsencePeriod.RecordNoonVacation(application.getId(), status);
        }

        return new AbsencePeriod.Record(tuple.date, person, morning, noon);
    }

    private AbsencePeriod toAbsencePeriod(SickNote sickNote, DateRange
        askedDateRange, List<WorkingTime> workingTimeList, FederalState systemDefaultFederalState) {
        return new AbsencePeriod(days(sickNote, askedDateRange, workingTimeList, systemDefaultFederalState));
    }

    private List<AbsencePeriod.Record> days(SickNote sickNote, DateRange
        askedDateRange, List<WorkingTime> workingTimeList, FederalState systemDefaultFederalState) {

        final LocalDate start = maxDate(sickNote.getStartDate(), askedDateRange.getStartDate());
        final LocalDate end = minDate(sickNote.getEndDate(), askedDateRange.getEndDate());

        return new DateRange(start, end).stream()
            .map(date -> new DateDayLengthTuple(date, publicHolidayAbsence(date, workingTimeList, systemDefaultFederalState)))
            // ignore full public holiday since it is no "absence".
            // it could still be an official workday with a sick note.
            .filter(tuple -> !tuple.publicHolidayDayLength.equals(DayLength.FULL))
            .map(tuple -> toSickAbsencePeriodRecord(tuple, sickNote, List.of()))
            .collect(toList());
    }

    private AbsencePeriod.Record toSickAbsencePeriodRecord(DateDayLengthTuple tuple, SickNote sickNote, List<WorkingTime> workingTimeList) {

        final Person person = sickNote.getPerson();

        if (!workingTimeList.isEmpty() && !isWorkday(tuple.date, workingTimeList)) {
            final AbsencePeriod.RecordMorning morning = new RecordMorningNoWorkday();
            final AbsencePeriod.RecordNoon noon = new RecordNoonNoWorkday();
            return new AbsencePeriod.Record(tuple.date, person, morning, noon);
        }

        if (tuple.publicHolidayDayLength.isHalfDay()) {
            final AbsencePeriod.RecordMorning morning;
            final AbsencePeriod.RecordNoon noon;
            if (tuple.publicHolidayDayLength.equals(DayLength.MORNING)) {
                morning = null;
                noon = new AbsencePeriod.RecordNoonSick(sickNote.getId());
            } else if (tuple.publicHolidayDayLength.equals(DayLength.NOON)) {
                morning = new AbsencePeriod.RecordMorningSick(sickNote.getId());
                noon = null;
            } else {
                morning = new AbsencePeriod.RecordMorningSick(sickNote.getId());
                noon = new AbsencePeriod.RecordNoonSick(sickNote.getId());
            }
            return new AbsencePeriod.Record(tuple.date, person, morning, noon);
        }

        final AbsencePeriod.RecordMorningSick morning;
        final AbsencePeriod.RecordNoonSick noon;

        if (DayLength.MORNING.equals(sickNote.getDayLength())) {
            morning = new AbsencePeriod.RecordMorningSick(sickNote.getId());
            noon = null;
        } else if (DayLength.NOON.equals(sickNote.getDayLength())) {
            morning = null;
            noon = new AbsencePeriod.RecordNoonSick(sickNote.getId());
        } else {
            morning = new AbsencePeriod.RecordMorningSick(sickNote.getId());
            noon = new AbsencePeriod.RecordNoonSick(sickNote.getId());
        }

        return new AbsencePeriod.Record(tuple.date, person, morning, noon);
    }

    private DayLength publicHolidayAbsence(LocalDate date, List<WorkingTime> workingTimeList, FederalState
        federalStateDefault) {

        final FederalState federalState = workingTime(date, workingTimeList)
            .or(() -> workingTimeList.isEmpty()
                ? Optional.empty()
                : Optional.of(workingTimeList.get(0))
            )
            .map(WorkingTime::getFederalState)
            .orElse(federalStateDefault);

        final Optional<PublicHoliday> maybePublicHoliday = publicHolidaysService.getPublicHoliday(date, federalState);
        return maybePublicHoliday.isPresent() ? maybePublicHoliday.get().getDayLength() : DayLength.ZERO;
    }

    private static Optional<WorkingTime> workingTime(LocalDate validFrom, List<WorkingTime> workingTimeList) {
        final Predicate<LocalDate> isEqual = validFrom::isEqual;
        final Predicate<LocalDate> isAfter = validFrom::isAfter;
        return workingTimeList.stream()
            .filter(workingTime -> isEqual.or(isAfter).test(workingTime.getValidFrom()))
            .findFirst();
    }

    private AbsenceTimeConfiguration getAbsenceTimeConfiguration() {
        final TimeSettings timeSettings = settingsService.getSettings().getTimeSettings();
        return new AbsenceTimeConfiguration(timeSettings);
    }

    private static LocalDate maxDate(LocalDate date, LocalDate date2) {
        return date.isAfter(date2) ? date : date2;
    }

    private static LocalDate minDate(LocalDate date, LocalDate date2) {
        return date.isBefore(date2) ? date : date2;
    }

    private static class DateDayLengthTuple {
        final LocalDate date;
        final DayLength publicHolidayDayLength;

        private DateDayLengthTuple(LocalDate date, DayLength publicHolidayDayLength) {
            this.date = date;
            this.publicHolidayDayLength = publicHolidayDayLength;
        }
    }
}
