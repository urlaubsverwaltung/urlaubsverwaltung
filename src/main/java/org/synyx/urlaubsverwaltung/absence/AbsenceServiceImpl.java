package org.synyx.urlaubsverwaltung.absence;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.ACTIVE;


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
        final List<WorkingTime> workingTimeList = workingTimeService.getByPersonsAndDateInterval(persons, start, end);
        final FederalState systemDefaultFederalState = workingTimeService.getSystemDefaultFederalState();

        final List<Application> openApplications = applicationService.getForStatesAndPerson(APPLICATION_STATUSES, persons, start, end);
        final List<AbsencePeriod> applicationAbsences = generateAbsencePeriodFromApplication(openApplications, askedDateRange, workingTimeList, systemDefaultFederalState);

        final List<SickNote> openSickNotes = sickNoteService.getForStatesAndPerson(SICK_NOTE_STATUSES, persons, start, end);
        final List<AbsencePeriod> sickNoteAbsences = generateAbsencePeriodFromSickNotes(openSickNotes, askedDateRange, workingTimeList, systemDefaultFederalState);

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

    private List<AbsencePeriod> generateAbsencePeriodFromApplication(List<Application> applications,
                                                                     DateRange askedDateRange,
                                                                     List<WorkingTime> workingTimeList,
                                                                     FederalState systemDefaultFederalState) {
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

    private List<AbsencePeriod> generateAbsencePeriodFromSickNotes(List<SickNote> sickNotes,
                                                                   DateRange askedDateRange,
                                                                   List<WorkingTime> workingTimeList,
                                                                   FederalState systemDefaultFederalState) {
        return sickNotes.stream()
            .map(sickNote ->  toAbsencePeriod(sickNote, askedDateRange, workingTimeList, systemDefaultFederalState))
            .collect(toList());
    }

    private AbsencePeriod toAbsencePeriod(Application application, DateRange askedDateRange, List<WorkingTime> workingTimeList,
                                          FederalState systemDefaultFederalState) {
        return new AbsencePeriod(days(application, askedDateRange, workingTimeList, systemDefaultFederalState));
    }

    private AbsencePeriod.AbsenceStatus toAbsenceStatus(ApplicationStatus applicationStatus) {
        switch (applicationStatus) {
            case ALLOWED:
                return AbsencePeriod.AbsenceStatus.ALLOWED;
            case WAITING:
                return AbsencePeriod.AbsenceStatus.WAITING;
            case TEMPORARY_ALLOWED:
                return AbsencePeriod.AbsenceStatus.TEMPORARY_ALLOWED;
            case ALLOWED_CANCELLATION_REQUESTED:
                return AbsencePeriod.AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED;
            default:
                throw new IllegalStateException("application status not expected here.");
        }
    }

    private List<AbsencePeriod.Record> days(Application application, DateRange askedDateRange, List<WorkingTime> workingTimeList,
                                            FederalState systemDefaultFederalState) {

        final LocalDate start = maxDate(application.getStartDate(), askedDateRange.getStartDate());
        final LocalDate end = minDate(application.getEndDate(), askedDateRange.getEndDate());

        return new DateRange(start, end).stream()
            .map(date -> new DateDayLengthTuple(date, publicHolidayAbsence(date, workingTimeList, systemDefaultFederalState)))
            // ignore full public holiday since it is no "absence".
            // it could still be an official workday with an application for leave.
            .filter(tuple -> !tuple.publicHolidayDayLength.equals(DayLength.FULL))
            .map(tuple -> toVacationAbsencePeriodRecord(tuple, application))
            .collect(toList());
    }

    private AbsencePeriod.Record toVacationAbsencePeriodRecord(DateDayLengthTuple tuple, Application application) {

        final Person person = application.getPerson();
        final AbsencePeriod.AbsenceStatus status = toAbsenceStatus(application.getStatus());

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
        }
        else if (DayLength.NOON.equals(application.getDayLength())) {
            morning = null;
            noon = new AbsencePeriod.RecordNoonVacation(application.getId(), status);
        }
        else {
            morning = new AbsencePeriod.RecordMorningVacation(application.getId(), status);
            noon = new AbsencePeriod.RecordNoonVacation(application.getId(), status);
        }

        return new AbsencePeriod.Record(tuple.date, person, morning, noon);
    }

    private AbsencePeriod toAbsencePeriod(SickNote sickNote, DateRange askedDateRange, List<WorkingTime> workingTimeList,
                                          FederalState systemDefaultFederalState) {
        return new AbsencePeriod(days(sickNote, askedDateRange, workingTimeList, systemDefaultFederalState));
    }

    private List<AbsencePeriod.Record> days(SickNote sickNote, DateRange askedDateRange, List<WorkingTime> workingTimeList,
                                            FederalState systemDefaultFederalState) {

        final LocalDate start = maxDate(sickNote.getStartDate(), askedDateRange.getStartDate());
        final LocalDate end = minDate(sickNote.getEndDate(), askedDateRange.getEndDate());

        return new DateRange(start, end).stream()
            .map(date -> new DateDayLengthTuple(date, publicHolidayAbsence(date, workingTimeList, systemDefaultFederalState)))
            // ignore full public holiday since it is no "absence".
            // it could still be an official workday with a sick note.
            .filter(tuple -> !tuple.publicHolidayDayLength.equals(DayLength.FULL))
            .map(tuple -> toSickAbsencePeriodRecord(tuple, sickNote))
            .collect(toList());
    }

    private AbsencePeriod.Record toSickAbsencePeriodRecord(DateDayLengthTuple tuple, SickNote sickNote) {

        final Person person = sickNote.getPerson();

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
        }
        else if (DayLength.NOON.equals(sickNote.getDayLength())) {
            morning = null;
            noon = new AbsencePeriod.RecordNoonSick(sickNote.getId());
        }
        else {
            morning = new AbsencePeriod.RecordMorningSick(sickNote.getId());
            noon = new AbsencePeriod.RecordNoonSick(sickNote.getId());
        }

        return new AbsencePeriod.Record(tuple.date, person, morning, noon);
    }
    private DayLength publicHolidayAbsence(LocalDate date, List<WorkingTime> workingTimeList,
                                           FederalState federalStateDefault) {
        final WorkingTime workingTime = workingTime(date, workingTimeList).orElse(workingTimeList.get(0));
        final FederalState federalState = workingTime.getFederalStateOverride().orElse(federalStateDefault);
        return publicHolidaysService.getAbsenceTypeOfDate(date, federalState);
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
