package org.synyx.urlaubsverwaltung.absence;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.max;
import static java.util.Collections.min;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.NO_WORKDAY;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.PUBLIC_HOLIDAY;


@Service
public class AbsenceServiceImpl implements AbsenceService {

    private static final Logger LOG = getLogger(lookup().lookupClass());


    private final ApplicationService applicationService;
    private final SickNoteService sickNoteService;
    private final WorkingTimeCalendarService workingTimeCalendarService;

    @Autowired
    public AbsenceServiceImpl(
        ApplicationService applicationService, SickNoteService sickNoteService,
        WorkingTimeCalendarService workingTimeCalendarService
    ) {
        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
        this.workingTimeCalendarService = workingTimeCalendarService;
    }

    @Override
    public List<AbsencePeriod> getOpenAbsences(Person person, LocalDate start, LocalDate end) {
        return getOpenAbsences(List.of(person), start, end);
    }

    @Override
    public List<AbsencePeriod> getOpenAbsences(List<Person> persons, LocalDate start, LocalDate end) {
        return getAbsences(persons, start, end, ApplicationStatus.activeStatuses(), SickNoteStatus.activeStatuses());
    }

    @Override
    public List<AbsencePeriod> getClosedAbsences(Person person, LocalDate start, LocalDate end) {
        return getClosedAbsences(List.of(person), start, end);
    }

    @Override
    public List<AbsencePeriod> getClosedAbsences(List<Person> persons, LocalDate start, LocalDate end) {
        return getAbsences(persons, start, end, ApplicationStatus.inactiveStatuses(), SickNoteStatus.inactiveStatuses());
    }

    private List<AbsencePeriod> getAbsences(List<Person> persons, LocalDate start, LocalDate end, List<ApplicationStatus> byApplicationStatus, List<SickNoteStatus> bySickNoteStatus) {

        final DateRange askedDateRange = new DateRange(start, end);

        final Map<Person, WorkingTimeCalendar> workingTimeCalendarByPerson = workingTimeCalendarService.getWorkingTimesByPersons(persons, askedDateRange);

        final List<Application> openApplications = applicationService.getForStatesAndPerson(byApplicationStatus, persons, start, end);
        final List<AbsencePeriod> applicationAbsences = generateAbsencePeriodFromApplication(openApplications, askedDateRange, workingTimeCalendarByPerson::get);

        final List<SickNote> openSickNotes = sickNoteService.getForStatesAndPerson(bySickNoteStatus, persons, start, end);
        final List<AbsencePeriod> sickNoteAbsences = generateAbsencePeriodFromSickNotes(openSickNotes, askedDateRange, workingTimeCalendarByPerson::get);

        final List<AbsencePeriod> noWorkingDaysAndPublicHolidays = generateAbsencePeriodFromWorkingTimes(workingTimeCalendarByPerson);

        return Stream.of(applicationAbsences.stream(), sickNoteAbsences.stream(), noWorkingDaysAndPublicHolidays.stream())
            .reduce(Stream.of(), Stream::concat)
            .toList();
    }

    private List<AbsencePeriod> generateAbsencePeriodFromApplication(List<Application> applications, DateRange askedDateRange, Function<Person, WorkingTimeCalendar> workingTimeCalendarSupplier) {
        return applications.stream()
            .map(application -> toAbsencePeriod(application, askedDateRange, workingTimeCalendarSupplier))
            .toList();
    }

    private List<AbsencePeriod> generateAbsencePeriodFromSickNotes(List<SickNote> sickNotes, DateRange askedDateRange, Function<Person, WorkingTimeCalendar> workingTimeCalendarSupplier) {
        return sickNotes.stream()
            .map(sickNote -> toAbsencePeriod(sickNote, askedDateRange, workingTimeCalendarSupplier))
            .toList();
    }

    private List<AbsencePeriod> generateAbsencePeriodFromWorkingTimes(Map<Person, WorkingTimeCalendar> workingTimeCalendars) {
        return workingTimeCalendars.entrySet().stream()
            .map(this::toAbsencePeriod)
            .flatMap(Collection::stream)
            .toList();
    }

    private List<AbsencePeriod> toAbsencePeriod(Map.Entry<Person, WorkingTimeCalendar> workingTimeCalendarEntry) {

        final Person person = workingTimeCalendarEntry.getKey();
        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarEntry.getValue();

        return workingTimeCalendar.workingDays().entrySet().stream()
            .<AbsencePeriod>mapMulti((workingDayInformationEntry, consumer) -> {
                final LocalDate date = workingDayInformationEntry.getKey();
                final WorkingDayInformation workingDayInformation = workingDayInformationEntry.getValue();

                if (!workingDayInformation.dayLength().isFull()) {
                    if (workingDayInformation.morning() == workingDayInformation.noon()) {
                        if (workingDayInformation.morning() == NO_WORKDAY) {
                            consumer.accept(new AbsencePeriod(List.of(new AbsencePeriod.Record(date, person, new AbsencePeriod.RecordMorningNoWorkday(person), new AbsencePeriod.RecordNoonNoWorkday(person)))));
                        } else if (workingDayInformation.morning() == PUBLIC_HOLIDAY) {
                            consumer.accept(new AbsencePeriod(List.of(new AbsencePeriod.Record(date, person, new AbsencePeriod.RecordMorningPublicHoliday(person), new AbsencePeriod.RecordNoonPublicHoliday(person)))));
                        }
                    } else {
                        if (workingDayInformation.morning() == NO_WORKDAY) {
                            consumer.accept(new AbsencePeriod(List.of(new AbsencePeriod.Record(date, person, new AbsencePeriod.RecordMorningNoWorkday(person)))));
                        } else if (workingDayInformation.morning() == PUBLIC_HOLIDAY) {
                            consumer.accept(new AbsencePeriod(List.of(new AbsencePeriod.Record(date, person, new AbsencePeriod.RecordMorningPublicHoliday(person)))));
                        }

                        if (workingDayInformation.noon() == NO_WORKDAY) {
                            consumer.accept(new AbsencePeriod(List.of(new AbsencePeriod.Record(date, person, new AbsencePeriod.RecordNoonNoWorkday(person)))));
                        } else if (workingDayInformation.noon() == PUBLIC_HOLIDAY) {
                            consumer.accept(new AbsencePeriod(List.of(new AbsencePeriod.Record(date, person, new AbsencePeriod.RecordNoonPublicHoliday(person)))));
                        }
                    }
                }

            })
            .toList();
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
        };
    }

    private List<AbsencePeriod.Record> days(Application application, DateRange askedDateRange, Function<Person, WorkingTimeCalendar> workingTimeCalendarSupplier) {

        final LocalDate start = max(List.of(application.getStartDate(), askedDateRange.startDate()));
        final LocalDate end = min(List.of(application.getEndDate(), askedDateRange.endDate()));

        final Person person = application.getPerson();
        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarSupplier.apply(person);

        return new DateRange(start, end).stream()
            .map(date -> Map.entry(date, workingTimeCalendar.workingTimeDayLength(date).orElse(DayLength.ZERO)))
            .filter(entry -> !entry.getValue().equals(DayLength.ZERO))
            .map(entry -> toVacationAbsencePeriodRecord(entry.getKey(), entry.getValue(), application))
            .toList();
    }

    private AbsencePeriod.Record toVacationAbsencePeriodRecord(LocalDate date, DayLength workingDayLength, Application application) {

        final Person person = application.getPerson();
        final Long applicationId = application.getId();
        final AbsencePeriod.AbsenceStatus status = toAbsenceStatus(application.getStatus());
        final String typeCategory = application.getVacationType().getCategory().name();
        final Long typeId = application.getVacationType().getId();
        final boolean visibleToEveryone = application.getVacationType().isVisibleToEveryone();
        final DayLength applicationDayLength = application.getDayLength();

        final AbsencePeriod.RecordMorningVacation morning;
        final AbsencePeriod.RecordNoonVacation noon;

        if (workingDayLength.isMorning()) {
            noon = null;
            if (applicationDayLength.isFull() || applicationDayLength.isMorning()) {
                morning = new AbsencePeriod.RecordMorningVacation(person, applicationId, status, typeCategory, typeId, visibleToEveryone);
            } else {
                LOG.info("calculate absence seems fishy. workingDayLength={} application.dayLength={} application.id={}", workingDayLength, applicationDayLength, applicationId);
                morning = null;
            }
        } else if (workingDayLength.isNoon()) {
            morning = null;
            if (applicationDayLength.isFull() || applicationDayLength.isNoon()) {
                noon = new AbsencePeriod.RecordNoonVacation(person, applicationId, status, typeCategory, typeId, visibleToEveryone);
            } else {
                LOG.info("calculate absence seems fishy. workingDayLength={} application.dayLength={} application.id={} ", workingDayLength, applicationDayLength, applicationId);
                noon = null;
            }
        } else if (applicationDayLength.isMorning()) {
            morning = new AbsencePeriod.RecordMorningVacation(person, applicationId, status, typeCategory, typeId, visibleToEveryone);
            noon = null;
        } else if (applicationDayLength.isNoon()) {
            morning = null;
            noon = new AbsencePeriod.RecordNoonVacation(person, applicationId, status, typeCategory, typeId, visibleToEveryone);
        } else {
            morning = new AbsencePeriod.RecordMorningVacation(person, applicationId, status, typeCategory, typeId, visibleToEveryone);
            noon = new AbsencePeriod.RecordNoonVacation(person, applicationId, status, typeCategory, typeId, visibleToEveryone);
        }

        return new AbsencePeriod.Record(date, person, morning, noon);
    }

    private AbsencePeriod toAbsencePeriod(SickNote sickNote, DateRange askedDateRange, Function<Person, WorkingTimeCalendar> workingTimeCalendarSupplier) {
        return new AbsencePeriod(days(sickNote, askedDateRange, workingTimeCalendarSupplier));
    }

    private AbsencePeriod.AbsenceStatus toAbsenceStatus(SickNoteStatus sickNoteStatus) {
        return switch (sickNoteStatus) {
            case SUBMITTED -> AbsencePeriod.AbsenceStatus.WAITING;
            case ACTIVE -> AbsencePeriod.AbsenceStatus.ACTIVE;
            case CANCELLED -> AbsencePeriod.AbsenceStatus.CANCELLED;
            case CONVERTED_TO_VACATION -> AbsencePeriod.AbsenceStatus.CONVERTED_TO_VACATION;
        };
    }

    private List<AbsencePeriod.Record> days(SickNote sickNote, DateRange askedDateRange, Function<Person, WorkingTimeCalendar> workingTimeCalendarSupplier) {

        final LocalDate start = max(List.of(sickNote.getStartDate(), askedDateRange.startDate()));
        final LocalDate end = min(List.of(sickNote.getEndDate(), askedDateRange.endDate()));

        final Person person = sickNote.getPerson();
        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarSupplier.apply(person);

        return new DateRange(start, end).stream()
            .map(date -> Map.entry(date, workingTimeCalendar.workingTimeDayLength(date).orElse(DayLength.ZERO)))
            // sickNotes are
            .map(entry -> toSickAbsencePeriodRecord(entry.getKey(), entry.getValue(), sickNote))
            .toList();
    }

    private AbsencePeriod.Record toSickAbsencePeriodRecord(LocalDate date, DayLength workingTimeDayLength, SickNote sickNote) {

        final Long sickNoteId = sickNote.getId();
        final Person person = sickNote.getPerson();
        final AbsencePeriod.AbsenceStatus status = toAbsenceStatus(sickNote.getStatus());
        final String typeCategory = sickNote.getSickNoteType().getCategory().name();
        final Long typeId = sickNote.getSickNoteType().getId();

        final AbsencePeriod.RecordMorningSick morning;
        final AbsencePeriod.RecordNoonSick noon;

        if (workingTimeDayLength.isHalfDay()) {
            if (workingTimeDayLength.isMorning()) {
                morning = new AbsencePeriod.RecordMorningSick(person, sickNoteId, status, typeCategory, typeId);
                noon = null;
            } else {
                morning = null;
                noon = new AbsencePeriod.RecordNoonSick(person, sickNoteId, status, typeCategory, typeId);
            }
            return new AbsencePeriod.Record(date, person, morning, noon);
        }

        if (DayLength.MORNING.equals(sickNote.getDayLength())) {
            morning = new AbsencePeriod.RecordMorningSick(person, sickNoteId, status, typeCategory, typeId);
            noon = null;
        } else if (DayLength.NOON.equals(sickNote.getDayLength())) {
            morning = null;
            noon = new AbsencePeriod.RecordNoonSick(person, sickNoteId, status, typeCategory, typeId);
        } else {
            morning = new AbsencePeriod.RecordMorningSick(person, sickNoteId, status, typeCategory, typeId);
            noon = new AbsencePeriod.RecordNoonSick(person, sickNoteId, status, typeCategory, typeId);
        }

        return new AbsencePeriod.Record(date, person, morning, noon);
    }
}
