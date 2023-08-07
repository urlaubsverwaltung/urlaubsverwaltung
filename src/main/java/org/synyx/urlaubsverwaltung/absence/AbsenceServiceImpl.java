package org.synyx.urlaubsverwaltung.absence;

import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.REVOKED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.CONVERTED_TO_VACATION;


@Service
public class AbsenceServiceImpl implements AbsenceService {

    private static final Logger LOG = getLogger(lookup().lookupClass());


    private static final List<ApplicationStatus> APPLICATION_STATUSES = List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
    private static final List<SickNoteStatus> SICK_NOTE_STATUSES = List.of(ACTIVE);

    private final ApplicationService applicationService;
    private final SickNoteService sickNoteService;
    private final SettingsService settingsService;
    private final WorkingTimeCalendarService workingTimeCalendarService;

    @Autowired
    public AbsenceServiceImpl(ApplicationService applicationService, SickNoteService sickNoteService,
                              SettingsService settingsService, WorkingTimeCalendarService workingTimeCalendarService) {

        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
        this.settingsService = settingsService;
        this.workingTimeCalendarService = workingTimeCalendarService;
    }

    @Override
    public List<AbsencePeriod> getOpenAbsences(Person person, LocalDate start, LocalDate end) {
        return getOpenAbsences(List.of(person), start, end);
    }

    @Override
    public List<AbsencePeriod> getOpenAbsences(List<Person> persons, LocalDate start, LocalDate end) {

        final DateRange askedDateRange = new DateRange(start, end);

        final Map<Person, WorkingTimeCalendar> workingTimeCalendarByPerson = workingTimeCalendarService.getWorkingTimesByPersons(persons, askedDateRange);

        final List<Application> openApplications = applicationService.getForStatesAndPerson(APPLICATION_STATUSES, persons, start, end);
        final List<AbsencePeriod> applicationAbsences = generateAbsencePeriodFromApplication(openApplications, askedDateRange, workingTimeCalendarByPerson::get);

        final List<SickNote> openSickNotes = sickNoteService.getForStatesAndPerson(SICK_NOTE_STATUSES, persons, start, end);
        final List<AbsencePeriod> sickNoteAbsences = generateAbsencePeriodFromSickNotes(openSickNotes, askedDateRange, workingTimeCalendarByPerson::get);

        return Stream.concat(applicationAbsences.stream(), sickNoteAbsences.stream()).collect(toList());
    }

    @Override
    public List<AbsencePeriod> getClosedAbsences(Person person, LocalDate start, LocalDate end) {
        return getClosedAbsences(List.of(person), start, end);
    }

    @Override
    public List<AbsencePeriod> getClosedAbsences(List<Person> persons, LocalDate start, LocalDate end) {

        final DateRange askedDateRange = new DateRange(start, end);

        final Map<Person, WorkingTimeCalendar> workingTimeCalendarByPerson = workingTimeCalendarService.getWorkingTimesByPersons(persons, askedDateRange);

        List<ApplicationStatus> closedAppStatus = List.of(REJECTED, CANCELLED, REVOKED);
        final List<Application> closedApplications = applicationService.getForStatesAndPerson(closedAppStatus, persons, start, end);
        final List<AbsencePeriod> applicationAbsences = generateAbsencePeriodFromApplication(closedApplications, askedDateRange, workingTimeCalendarByPerson::get);

        List<SickNoteStatus> closedSickNoteStatus = List.of(CONVERTED_TO_VACATION, SickNoteStatus.CANCELLED);
        final List<SickNote> closedSickNotes = sickNoteService.getForStatesAndPerson(closedSickNoteStatus, persons, start, end);
        final List<AbsencePeriod> sickNoteAbsences = generateAbsencePeriodFromSickNotes(closedSickNotes, askedDateRange, workingTimeCalendarByPerson::get);

        return Stream.concat(applicationAbsences.stream(), sickNoteAbsences.stream()).collect(toList());
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

        final List<SickNote> openSickNotes = sickNoteService.getForStatesSince(SICK_NOTE_STATUSES, since);
        final List<Absence> sickNoteAbsences = generateAbsencesFromSickNotes(openSickNotes);

        return ListUtils.union(applicationAbsences, sickNoteAbsences);
    }

    private List<Absence> generateAbsencesFromApplication(List<Application> applications) {
        final AbsenceTimeConfiguration config = getAbsenceTimeConfiguration();
        return applications.stream()
            .map(application -> new Absence(application.getPerson(), application.getPeriod(), config))
            .collect(toList());
    }

    private List<AbsencePeriod> generateAbsencePeriodFromApplication(List<Application> applications,
                                                                     DateRange askedDateRange,
                                                                     Function<Person, WorkingTimeCalendar> workingTimeCalendarSupplier) {
        return applications.stream()
            .map(application -> toAbsencePeriod(application, askedDateRange, workingTimeCalendarSupplier))
            .collect(toList());
    }

    private List<Absence> generateAbsencesFromSickNotes(List<SickNote> sickNotes) {
        final AbsenceTimeConfiguration config = getAbsenceTimeConfiguration();
        return sickNotes.stream()
            .map(sickNote -> new Absence(sickNote.getPerson(), sickNote.getPeriod(), config))
            .collect(toList());
    }

    private List<AbsencePeriod> generateAbsencePeriodFromSickNotes(List<SickNote> sickNotes,
                                                                   DateRange askedDateRange,
                                                                   Function<Person, WorkingTimeCalendar> workingTimeCalendarSupplier) {
        return sickNotes.stream()
            .map(sickNote -> toAbsencePeriod(sickNote, askedDateRange, workingTimeCalendarSupplier))
            .collect(toList());
    }

    private AbsencePeriod toAbsencePeriod(Application application, DateRange askedDateRange, Function<Person, WorkingTimeCalendar> workingTimeCalendarSupplier) {
        return new AbsencePeriod(days(application, askedDateRange, workingTimeCalendarSupplier));
    }

    private AbsencePeriod.AbsenceStatus toAbsenceStatus(ApplicationStatus applicationStatus) {
        return switch (applicationStatus) {
            case ALLOWED -> AbsencePeriod.AbsenceStatus.ALLOWED;
            case WAITING -> AbsencePeriod.AbsenceStatus.WAITING;
            case TEMPORARY_ALLOWED -> AbsencePeriod.AbsenceStatus.TEMPORARY_ALLOWED;
            case ALLOWED_CANCELLATION_REQUESTED -> AbsencePeriod.AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED;
            case REVOKED -> AbsencePeriod.AbsenceStatus.REVOKED;
            case REJECTED -> AbsencePeriod.AbsenceStatus.REJECTED;
            case CANCELLED -> AbsencePeriod.AbsenceStatus.CANCELLED;
            default -> throw new IllegalStateException("application status not expected here.");
        };
    }

    private List<AbsencePeriod.Record> days(Application application, DateRange askedDateRange, Function<Person, WorkingTimeCalendar> workingTimeCalendarSupplier) {

        final LocalDate start = maxDate(application.getStartDate(), askedDateRange.getStartDate());
        final LocalDate end = minDate(application.getEndDate(), askedDateRange.getEndDate());

        final Person person = application.getPerson();
        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarSupplier.apply(person);

        return new DateRange(start, end).stream()
            .map(date -> Map.entry(date, workingTimeCalendar.workingTimeDayLength(date).orElse(DayLength.ZERO)))
            .filter(entry -> !entry.getValue().equals(DayLength.ZERO))
            .map(entry -> toVacationAbsencePeriodRecord(entry.getKey(), entry.getValue(), application))
            .collect(toList());
    }

    private AbsencePeriod.Record toVacationAbsencePeriodRecord(LocalDate date, DayLength workingDayLength, Application application) {

        final Person person = application.getPerson();
        final Long applicationId = application.getId();
        final AbsencePeriod.AbsenceStatus status = toAbsenceStatus(application.getStatus());
        final Long vacationTypeId = application.getVacationType().getId();
        final boolean visibleToEveryone = application.getVacationType().isVisibleToEveryone();
        final DayLength applicationDayLength = application.getDayLength();

        final AbsencePeriod.RecordMorningVacation morning;
        final AbsencePeriod.RecordNoonVacation noon;

        if (workingDayLength.isMorning()) {
            noon = null;
            if (applicationDayLength.isFull() || applicationDayLength.isMorning()) {
                morning = new AbsencePeriod.RecordMorningVacation(person, applicationId, status, vacationTypeId, visibleToEveryone);
            } else {
                LOG.info("calculate absence seems fishy. workingDayLength={} application.dayLength={} application.id={}", workingDayLength, applicationDayLength, applicationId);
                morning = null;
            }
        } else if (workingDayLength.isNoon()) {
            morning = null;
            if (applicationDayLength.isFull() || applicationDayLength.isNoon()) {
                noon = new AbsencePeriod.RecordNoonVacation(person, applicationId, status, vacationTypeId, visibleToEveryone);
            } else {
                LOG.info("calculate absence seems fishy. workingDayLength={} application.dayLength={} application.id={} ", workingDayLength, applicationDayLength, applicationId);
                noon = null;
            }
        } else if (applicationDayLength.isMorning()) {
            morning = new AbsencePeriod.RecordMorningVacation(person, applicationId, status, vacationTypeId, visibleToEveryone);
            noon = null;
        } else if (applicationDayLength.isNoon()) {
            morning = null;
            noon = new AbsencePeriod.RecordNoonVacation(person, applicationId, status, vacationTypeId, visibleToEveryone);
        } else {
            morning = new AbsencePeriod.RecordMorningVacation(person, applicationId, status, vacationTypeId, visibleToEveryone);
            noon = new AbsencePeriod.RecordNoonVacation(person, applicationId, status, vacationTypeId, visibleToEveryone);
        }

        return new AbsencePeriod.Record(date, person, morning, noon);
    }

    private AbsencePeriod toAbsencePeriod(SickNote sickNote, DateRange askedDateRange, Function<Person, WorkingTimeCalendar> workingTimeCalendarSupplier) {
        return new AbsencePeriod(days(sickNote, askedDateRange, workingTimeCalendarSupplier));
    }

    private AbsencePeriod.AbsenceStatus toAbsenceStatus(SickNoteStatus sickNoteStatus) {
        return switch (sickNoteStatus) {
            case ACTIVE -> AbsencePeriod.AbsenceStatus.ACTIVE;
            case CANCELLED -> AbsencePeriod.AbsenceStatus.CANCELLED;
            case CONVERTED_TO_VACATION -> AbsencePeriod.AbsenceStatus.CONVERTED_TO_VACATION;
        };
    }

    private List<AbsencePeriod.Record> days(SickNote sickNote, DateRange askedDateRange, Function<Person, WorkingTimeCalendar> workingTimeCalendarSupplier) {

        final LocalDate start = maxDate(sickNote.getStartDate(), askedDateRange.getStartDate());
        final LocalDate end = minDate(sickNote.getEndDate(), askedDateRange.getEndDate());

        final Person person = sickNote.getPerson();
        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarSupplier.apply(person);

        return new DateRange(start, end).stream()
            .map(date -> Map.entry(date, workingTimeCalendar.workingTimeDayLength(date).orElse(DayLength.ZERO)))
            // sickNotes are
            .map(entry -> toSickAbsencePeriodRecord(entry.getKey(), entry.getValue(), sickNote))
            .collect(toList());
    }

    private AbsencePeriod.Record toSickAbsencePeriodRecord(LocalDate date, DayLength workingTimeDayLength, SickNote sickNote) {

        final Long sickNoteId = sickNote.getId();
        final Person person = sickNote.getPerson();
        final AbsencePeriod.AbsenceStatus status = toAbsenceStatus(sickNote.getStatus());

        final AbsencePeriod.RecordMorningSick morning;
        final AbsencePeriod.RecordNoonSick noon;

        if (workingTimeDayLength.isHalfDay()) {
            if (workingTimeDayLength.isMorning()) {
                morning = null;
                noon = new AbsencePeriod.RecordNoonSick(person, sickNoteId, status);
            } else {
                morning = new AbsencePeriod.RecordMorningSick(person, sickNoteId, status);
                noon = null;
            }
            return new AbsencePeriod.Record(date, person, morning, noon);
        }

        if (DayLength.MORNING.equals(sickNote.getDayLength())) {
            morning = new AbsencePeriod.RecordMorningSick(person, sickNoteId, status);
            noon = null;
        } else if (DayLength.NOON.equals(sickNote.getDayLength())) {
            morning = null;
            noon = new AbsencePeriod.RecordNoonSick(person, sickNoteId, status);
        } else {
            morning = new AbsencePeriod.RecordMorningSick(person, sickNoteId, status);
            noon = new AbsencePeriod.RecordNoonSick(person, sickNoteId, status);
        }

        return new AbsencePeriod.Record(date, person, morning, noon);
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
}
