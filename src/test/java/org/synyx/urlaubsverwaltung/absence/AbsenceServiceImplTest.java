package org.synyx.urlaubsverwaltung.absence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHoliday;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.Month.DECEMBER;
import static java.time.Month.JUNE;
import static java.time.Month.MAY;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createSickNote;
import static org.synyx.urlaubsverwaltung.absence.AbsencePeriod.RecordInfo;
import static org.synyx.urlaubsverwaltung.absence.AbsencePeriod.RecordMorningNoWorkday;
import static org.synyx.urlaubsverwaltung.absence.AbsencePeriod.RecordMorningSick;
import static org.synyx.urlaubsverwaltung.absence.AbsencePeriod.RecordMorningVacation;
import static org.synyx.urlaubsverwaltung.absence.AbsencePeriod.RecordNoonNoWorkday;
import static org.synyx.urlaubsverwaltung.absence.AbsencePeriod.RecordNoonSick;
import static org.synyx.urlaubsverwaltung.absence.AbsencePeriod.RecordNoonVacation;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BERLIN;

@ExtendWith(MockitoExtension.class)
class AbsenceServiceImplTest {

    private AbsenceServiceImpl sut;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private WorkingTimeService workingTimeService;
    @Mock
    private PublicHolidaysService publicHolidaysService;

    @BeforeEach
    void setUp() {
        sut = new AbsenceServiceImpl(applicationService, sickNoteService, settingsService, workingTimeService, publicHolidaysService);
    }

    @Test
    void ensureGetOpenAbsencesForPersonsContainsNoWorkday() {

        final Person person = new Person();
        person.setId(1);

        final Person workaholic = new Person();
        workaholic.setId(2);

        final LocalDate start = LocalDate.of(2022, 5, 2);
        final LocalDate end = LocalDate.of(2022, 5, 8);

        // no applications
        when(applicationService.getForStatesAndPerson(eq(List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED)), any(), eq(start), eq(end)))
            .thenReturn(emptyList());

        // no sickNotes
        when(sickNoteService.getForStatesAndPerson(eq(List.of(ACTIVE)), any(), eq(start), eq(end)))
            .thenReturn(emptyList());

        final WorkingTime personWorkingTime = new WorkingTime(person, start, GERMANY_BADEN_WUERTTEMBERG, true);
        personWorkingTime.setDayLengthForWeekDay(MONDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(TUESDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(WEDNESDAY, ZERO);
        personWorkingTime.setDayLengthForWeekDay(THURSDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(FRIDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(SATURDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(SUNDAY, ZERO);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(person, new DateRange(start, end)))
            .thenReturn(Map.of(new DateRange(start, end), personWorkingTime));

        final WorkingTime workaholicWorkingTime = new WorkingTime(workaholic, start, GERMANY_BADEN_WUERTTEMBERG, true);
        workaholicWorkingTime.setDayLengthForWeekDay(MONDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(TUESDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(WEDNESDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(THURSDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(FRIDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(SATURDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(SUNDAY, FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(workaholic, new DateRange(start, end)))
            .thenReturn(Map.of(new DateRange(start, end), workaholicWorkingTime));

        final Map<Person, Map<LocalDate, List<AbsencePeriod>>> actual = sut.getOpenAbsencesForPersons(List.of(person, workaholic), start, end);

        //
        // person assertions
        //

        assertThat(actual).containsKey(person);

        final RecordMorningNoWorkday morningWednesday = new RecordMorningNoWorkday();
        final RecordNoonNoWorkday noonWednesday = new RecordNoonNoWorkday();
        final AbsencePeriod.Record absenceRecordWednesday = new AbsencePeriod.Record(LocalDate.of(2022, 5, 4), person, morningWednesday, noonWednesday);
        final AbsencePeriod absenceWednesday = new AbsencePeriod(List.of(absenceRecordWednesday));

        final RecordMorningNoWorkday morningSunday = new RecordMorningNoWorkday();
        final RecordNoonNoWorkday noonSunday = new RecordNoonNoWorkday();
        final AbsencePeriod.Record absenceRecordSunday = new AbsencePeriod.Record(LocalDate.of(2022, 5, 8), person, morningSunday, noonSunday);
        final AbsencePeriod absenceSunday = new AbsencePeriod(List.of(absenceRecordSunday));

        final Map<LocalDate, List<AbsencePeriod>> actualAbsencesForPerson = actual.get(person);
        // monday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 2))).isEmpty();
        // tuesday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 3))).isEmpty();
        // wednesday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 4))).contains(absenceWednesday);
        // thursday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 5))).isEmpty();
        // friday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 6))).isEmpty();
        // saturday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 7))).isEmpty();
        // sunday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 8))).contains(absenceSunday);

        //
        // workaholic assertions
        //

        assertThat(actual).containsKey(workaholic);

        final Map<LocalDate, List<AbsencePeriod>> actualAbsencesForWorkaholic = actual.get(workaholic);
        // monday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 2))).isEmpty();
        // tuesday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 3))).isEmpty();
        // wednesday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 4))).isEmpty();
        // thursday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 5))).isEmpty();
        // friday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 6))).isEmpty();
        // saturday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 7))).isEmpty();
        // sunday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 8))).isEmpty();
    }

    @Test
    void ensureGetOpenAbsencesForPersonsContainsFullDayAbsence() {

        final Person person = new Person();
        person.setId(1);

        final Person workaholic = new Person();
        workaholic.setId(2);

        final LocalDate start = LocalDate.of(2022, 5, 2);
        final LocalDate end = LocalDate.of(2022, 5, 8);

        // no applications for workaholic ¯\_(ツ)_/¯
        final Application application = new Application();
        application.setId(1);
        application.setPerson(person);
        application.setStartDate(LocalDate.of(2022, 5, 2));
        application.setEndDate(LocalDate.of(2022, 5, 3));
        application.setStatus(ALLOWED);
        when(applicationService.getForStatesAndPerson(List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(person, workaholic), start, end))
            .thenReturn(List.of(application));

        // no sickNotes for workaholic ¯\_(ツ)_/¯
        final SickNote sickNote = new SickNote();
        sickNote.setId(1);
        sickNote.setPerson(person);
        sickNote.setStartDate(LocalDate.of(2022, 5, 4));
        sickNote.setEndDate(LocalDate.of(2022, 5, 5));
        sickNote.setStatus(ACTIVE);
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(person, workaholic), start, end))
            .thenReturn(List.of(sickNote));

        final WorkingTime personWorkingTime = new WorkingTime(person, start, GERMANY_BADEN_WUERTTEMBERG, true);
        personWorkingTime.setDayLengthForWeekDay(MONDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(TUESDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(WEDNESDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(THURSDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(FRIDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(SATURDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(SUNDAY, FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(person, new DateRange(start, end)))
            .thenReturn(Map.of(new DateRange(start, end), personWorkingTime));

        final WorkingTime workaholicWorkingTime = new WorkingTime(workaholic, start, GERMANY_BADEN_WUERTTEMBERG, true);
        workaholicWorkingTime.setDayLengthForWeekDay(MONDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(TUESDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(WEDNESDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(THURSDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(FRIDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(SATURDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(SUNDAY, FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(workaholic, new DateRange(start, end)))
            .thenReturn(Map.of(new DateRange(start, end), workaholicWorkingTime));

        final Map<Person, Map<LocalDate, List<AbsencePeriod>>> actual = sut.getOpenAbsencesForPersons(List.of(person, workaholic), start, end);

        //
        // person assertions
        //

        assertThat(actual).containsKey(person);

        final RecordMorningVacation morningApplication1 = new RecordMorningVacation(1, AbsencePeriod.Record.AbsenceStatus.ALLOWED);
        final RecordNoonVacation noonApplication1 = new RecordNoonVacation(1, AbsencePeriod.Record.AbsenceStatus.ALLOWED);
        final RecordMorningVacation morningApplication2 = new RecordMorningVacation(1, AbsencePeriod.Record.AbsenceStatus.ALLOWED);
        final RecordNoonVacation noonApplication2 = new RecordNoonVacation(1, AbsencePeriod.Record.AbsenceStatus.ALLOWED);
        final AbsencePeriod.Record applicationRecord1 = new AbsencePeriod.Record(LocalDate.of(2022, 5, 2), person, morningApplication1, noonApplication1);
        final AbsencePeriod.Record applicationRecord2 = new AbsencePeriod.Record(LocalDate.of(2022, 5, 3), person, morningApplication2, noonApplication2);
        final AbsencePeriod applicationAbsencePeriod = new AbsencePeriod(List.of(applicationRecord1, applicationRecord2));

        final RecordMorningSick morningSickNote1 = new RecordMorningSick(1);
        final RecordNoonSick noonAppSickNote1 = new RecordNoonSick(1);
        final RecordMorningSick morningSickNote2 = new RecordMorningSick(1);
        final RecordNoonSick noonSickNote2 = new RecordNoonSick(1);
        final AbsencePeriod.Record sickNoteRecord1 = new AbsencePeriod.Record(LocalDate.of(2022, 5, 4), person, morningSickNote1, noonAppSickNote1);
        final AbsencePeriod.Record sickNoteRecord2 = new AbsencePeriod.Record(LocalDate.of(2022, 5, 5), person, morningSickNote2, noonSickNote2);
        final AbsencePeriod sickNoteAbsencePeriod = new AbsencePeriod(List.of(sickNoteRecord1, sickNoteRecord2));

        final Map<LocalDate, List<AbsencePeriod>> actualAbsencesForPerson = actual.get(person);
        // monday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 2))).contains(applicationAbsencePeriod);
        // tuesday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 3))).isEmpty();
        // wednesday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 4))).contains(sickNoteAbsencePeriod);
        // thursday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 5))).isEmpty();
        // friday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 6))).isEmpty();
        // saturday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 7))).isEmpty();
        // sunday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 8))).isEmpty();

        //
        // workaholic assertions
        //

        assertThat(actual).containsKey(workaholic);

        final Map<LocalDate, List<AbsencePeriod>> actualAbsencesForWorkaholic = actual.get(workaholic);
        // monday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 2))).isEmpty();
        // tuesday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 3))).isEmpty();
        // wednesday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 4))).isEmpty();
        // thursday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 5))).isEmpty();
        // friday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 6))).isEmpty();
        // saturday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 7))).isEmpty();
        // sunday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 8))).isEmpty();
    }

    @Test
    void ensureGetOpenAbsencesForPersonsContainsMorningApplicationAndNoonSickNoteIsSortedByDayLength() {

        final Person person = new Person();
        person.setId(1);

        final Person workaholic = new Person();
        workaholic.setId(2);

        final LocalDate start = LocalDate.of(2022, 5, 2);
        final LocalDate end = LocalDate.of(2022, 5, 8);

        // no applications for workaholic ¯\_(ツ)_/¯
        final Application application = new Application();
        application.setId(1);
        application.setPerson(person);
        application.setStartDate(LocalDate.of(2022, 5, 2));
        application.setEndDate(LocalDate.of(2022, 5, 2));
        application.setDayLength(NOON); // first entry noon -> should be sorted to the end
        application.setStatus(ALLOWED);
        when(applicationService.getForStatesAndPerson(List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(person, workaholic), start, end))
            .thenReturn(List.of(application));

        // no sickNotes for workaholic ¯\_(ツ)_/¯
        final SickNote sickNote = new SickNote();
        sickNote.setId(1);
        sickNote.setPerson(person);
        sickNote.setStartDate(LocalDate.of(2022, 5, 2));
        sickNote.setEndDate(LocalDate.of(2022, 5, 2));
        sickNote.setDayLength(MORNING); // first entry morning -> should be sorted to the front
        sickNote.setStatus(ACTIVE);
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(person, workaholic), start, end))
            .thenReturn(List.of(sickNote));

        final WorkingTime personWorkingTime = new WorkingTime(person, start, GERMANY_BADEN_WUERTTEMBERG, true);
        personWorkingTime.setDayLengthForWeekDay(MONDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(TUESDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(WEDNESDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(THURSDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(FRIDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(SATURDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(SUNDAY, FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(person, new DateRange(start, end)))
            .thenReturn(Map.of(new DateRange(start, end), personWorkingTime));

        final WorkingTime workaholicWorkingTime = new WorkingTime(workaholic, start, GERMANY_BADEN_WUERTTEMBERG, true);
        workaholicWorkingTime.setDayLengthForWeekDay(MONDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(TUESDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(WEDNESDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(THURSDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(FRIDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(SATURDAY, FULL);
        workaholicWorkingTime.setDayLengthForWeekDay(SUNDAY, FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(workaholic, new DateRange(start, end)))
            .thenReturn(Map.of(new DateRange(start, end), workaholicWorkingTime));

        final Map<Person, Map<LocalDate, List<AbsencePeriod>>> actual = sut.getOpenAbsencesForPersons(List.of(person, workaholic), start, end);

        //
        // person assertions
        //

        assertThat(actual).containsKey(person);

        final RecordMorningVacation morningApplication = new RecordMorningVacation(1, AbsencePeriod.Record.AbsenceStatus.ALLOWED);
        final AbsencePeriod.Record applicationRecord = new AbsencePeriod.Record(LocalDate.of(2022, 5, 2), person, morningApplication);
        final AbsencePeriod applicationAbsencePeriod = new AbsencePeriod(List.of(applicationRecord));

        final RecordNoonSick noonAppSickNote = new RecordNoonSick(1);
        final AbsencePeriod.Record sickNoteRecord = new AbsencePeriod.Record(LocalDate.of(2022, 5, 2), person, noonAppSickNote);
        final AbsencePeriod sickNoteAbsencePeriod = new AbsencePeriod(List.of(sickNoteRecord));

        final Map<LocalDate, List<AbsencePeriod>> actualAbsencesForPerson = actual.get(person);
        // monday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 2))).containsExactly(applicationAbsencePeriod, sickNoteAbsencePeriod);
        // tuesday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 3))).isEmpty();
        // wednesday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 4))).isEmpty();
        // thursday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 5))).isEmpty();
        // friday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 6))).isEmpty();
        // saturday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 7))).isEmpty();
        // sunday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 8))).isEmpty();

        //
        // workaholic assertions
        //

        assertThat(actual).containsKey(workaholic);

        final Map<LocalDate, List<AbsencePeriod>> actualAbsencesForWorkaholic = actual.get(workaholic);
        // monday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 2))).isEmpty();
        // tuesday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 3))).isEmpty();
        // wednesday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 4))).isEmpty();
        // thursday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 5))).isEmpty();
        // friday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 6))).isEmpty();
        // saturday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 7))).isEmpty();
        // sunday
        assertThat(actualAbsencesForWorkaholic.get(LocalDate.of(2022, 5, 8))).isEmpty();
    }

    @Test
    void ensureGetOpenAbsencesForPersonsNoWorkdayTrumpsAbsence() {

        final Person person = new Person();
        person.setId(1);

        final LocalDate start = LocalDate.of(2022, 5, 2);
        final LocalDate end = LocalDate.of(2022, 5, 8);

        final Application application = new Application();
        application.setId(1);
        application.setPerson(person);
        application.setStartDate(LocalDate.of(2022, 5, 4));
        application.setEndDate(LocalDate.of(2022, 5, 4));
        application.setStatus(ALLOWED);
        when(applicationService.getForStatesAndPerson(List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(person), start, end))
            .thenReturn(List.of(application));

        // no sick notes
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(person), start, end))
            .thenReturn(List.of());

        final WorkingTime personWorkingTime = new WorkingTime(person, start, GERMANY_BADEN_WUERTTEMBERG, true);
        personWorkingTime.setDayLengthForWeekDay(MONDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(TUESDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(WEDNESDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(THURSDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(FRIDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(SATURDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(SUNDAY, FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(person, new DateRange(start, end)))
            .thenReturn(Map.of(new DateRange(start, end), personWorkingTime));

        final Map<Person, Map<LocalDate, List<AbsencePeriod>>> actual = sut.getOpenAbsencesForPersons(List.of(person), start, end);
        assertThat(actual).containsKey(person);

        final RecordMorningNoWorkday morningWednesday = new RecordMorningNoWorkday();
        final RecordNoonNoWorkday noonWednesday = new RecordNoonNoWorkday();
        final AbsencePeriod.Record absenceRecordWednesday = new AbsencePeriod.Record(LocalDate.of(2022, 5, 4), person, morningWednesday, noonWednesday);
        final AbsencePeriod absenceWednesday = new AbsencePeriod(List.of(absenceRecordWednesday));

        final Map<LocalDate, List<AbsencePeriod>> actualAbsencesForPerson = actual.get(person);
        // monday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 2))).isEmpty();
        // tuesday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 3))).isEmpty();
        // wednesday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 4))).contains(absenceWednesday);
        // thursday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 5))).isEmpty();
        // friday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 6))).isEmpty();
        // saturday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 7))).isEmpty();
        // sunday
        assertThat(actualAbsencesForPerson.get(LocalDate.of(2022, 5, 8))).isEmpty();
    }

    @Test
    void ensureGetOpenAbsencesForPersonsClampsStartDateOfApplication() {

        final Person person = new Person();
        person.setId(1);

        final LocalDate start = LocalDate.of(2022, 5, 2);
        final LocalDate end = LocalDate.of(2022, 5, 8);

        final Application application = new Application();
        application.setId(1);
        application.setPerson(person);
        application.setStartDate(LocalDate.of(2022, 4, 20));
        application.setEndDate(LocalDate.of(2022, 5, 4));
        application.setStatus(ALLOWED);
        when(applicationService.getForStatesAndPerson(List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(person), start, end))
            .thenReturn(List.of(application));

        // no sick notes
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(person), start, end))
            .thenReturn(List.of());

        final WorkingTime personWorkingTime = new WorkingTime(person, start, GERMANY_BADEN_WUERTTEMBERG, true);
        personWorkingTime.setDayLengthForWeekDay(MONDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(TUESDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(WEDNESDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(THURSDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(FRIDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(SATURDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(SUNDAY, FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(person, new DateRange(start, end)))
            .thenReturn(Map.of(new DateRange(start, end), personWorkingTime));

        final Map<Person, Map<LocalDate, List<AbsencePeriod>>> actual = sut.getOpenAbsencesForPersons(List.of(person), start, end);
        assertThat(actual).containsKey(person);

        final Map<LocalDate, List<AbsencePeriod>> actualAbsencesForPerson = actual.get(person);
        // asked start date to end date -> 7 days
        assertThat(actualAbsencesForPerson).hasSize(7);
    }

    @Test
    void ensureGetOpenAbsencesForPersonsClampsEndDateOfApplication() {

        final Person person = new Person();
        person.setId(1);

        final LocalDate start = LocalDate.of(2022, 5, 2);
        final LocalDate end = LocalDate.of(2022, 5, 8);

        final Application application = new Application();
        application.setId(1);
        application.setPerson(person);
        application.setStartDate(LocalDate.of(2022, 5, 4));
        application.setEndDate(LocalDate.of(2022, 5, 20));
        application.setStatus(ALLOWED);
        when(applicationService.getForStatesAndPerson(List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(person), start, end))
            .thenReturn(List.of(application));

        // no sick notes
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(person), start, end))
            .thenReturn(List.of());

        final WorkingTime personWorkingTime = new WorkingTime(person, start, GERMANY_BADEN_WUERTTEMBERG, true);
        personWorkingTime.setDayLengthForWeekDay(MONDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(TUESDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(WEDNESDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(THURSDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(FRIDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(SATURDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(SUNDAY, FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(person, new DateRange(start, end)))
            .thenReturn(Map.of(new DateRange(start, end), personWorkingTime));

        final Map<Person, Map<LocalDate, List<AbsencePeriod>>> actual = sut.getOpenAbsencesForPersons(List.of(person), start, end);
        assertThat(actual).containsKey(person);

        final Map<LocalDate, List<AbsencePeriod>> actualAbsencesForPerson = actual.get(person);
        // asked start date to end date -> 7 days
        assertThat(actualAbsencesForPerson).hasSize(7);
    }

    @Test
    void ensureGetOpenAbsencesForPersonsClampsStartDateOfSickNote() {

        final Person person = new Person();
        person.setId(1);

        final LocalDate start = LocalDate.of(2022, 5, 2);
        final LocalDate end = LocalDate.of(2022, 5, 8);

        final SickNote sickNote = new SickNote();
        sickNote.setId(1);
        sickNote.setPerson(person);
        sickNote.setStartDate(LocalDate.of(2022, 4, 20));
        sickNote.setEndDate(LocalDate.of(2022, 5, 4));
        sickNote.setStatus(ACTIVE);
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(person), start, end))
            .thenReturn(List.of(sickNote));

        // no applications
        when(applicationService.getForStatesAndPerson(List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(person), start, end))
            .thenReturn(List.of());

        final WorkingTime personWorkingTime = new WorkingTime(person, start, GERMANY_BADEN_WUERTTEMBERG, true);
        personWorkingTime.setDayLengthForWeekDay(MONDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(TUESDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(WEDNESDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(THURSDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(FRIDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(SATURDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(SUNDAY, FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(person, new DateRange(start, end)))
            .thenReturn(Map.of(new DateRange(start, end), personWorkingTime));

        final Map<Person, Map<LocalDate, List<AbsencePeriod>>> actual = sut.getOpenAbsencesForPersons(List.of(person), start, end);
        assertThat(actual).containsKey(person);

        final Map<LocalDate, List<AbsencePeriod>> actualAbsencesForPerson = actual.get(person);
        // asked start date to end date -> 7 days
        assertThat(actualAbsencesForPerson).hasSize(7);
    }

    @Test
    void ensureGetOpenAbsencesForPersonsClampsEndDateOfSickNote() {

        final Person person = new Person();
        person.setId(1);

        final LocalDate start = LocalDate.of(2022, 5, 2);
        final LocalDate end = LocalDate.of(2022, 5, 8);

        final SickNote sickNote = new SickNote();
        sickNote.setId(1);
        sickNote.setPerson(person);
        sickNote.setStartDate(LocalDate.of(2022, 5, 4));
        sickNote.setEndDate(LocalDate.of(2022, 5, 20));
        sickNote.setStatus(ACTIVE);
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(person), start, end))
            .thenReturn(List.of(sickNote));

        // no applications
        when(applicationService.getForStatesAndPerson(List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(person), start, end))
            .thenReturn(List.of());

        final WorkingTime personWorkingTime = new WorkingTime(person, start, GERMANY_BADEN_WUERTTEMBERG, true);
        personWorkingTime.setDayLengthForWeekDay(MONDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(TUESDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(WEDNESDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(THURSDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(FRIDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(SATURDAY, FULL);
        personWorkingTime.setDayLengthForWeekDay(SUNDAY, FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(person, new DateRange(start, end)))
            .thenReturn(Map.of(new DateRange(start, end), personWorkingTime));

        final Map<Person, Map<LocalDate, List<AbsencePeriod>>> actual = sut.getOpenAbsencesForPersons(List.of(person), start, end);
        assertThat(actual).containsKey(person);

        final Map<LocalDate, List<AbsencePeriod>> actualAbsencesForPerson = actual.get(person);
        // asked start date to end date -> 7 days
        assertThat(actualAbsencesForPerson).hasSize(7);
    }

    @Test
    void getOpenAbsencesSinceForPersons() {

        final Settings settings = new Settings();
        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setTimeZoneId("Etc/UTC");
        settings.setTimeSettings(timeSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate since = LocalDate.of(2020, 10, 13);

        final LocalDate startDate = LocalDate.of(2019, 12, 10);
        final LocalDate endDate = LocalDate.of(2019, 12, 23);
        final Application application = createApplication(person, startDate, endDate, FULL);
        when(applicationService.getForStatesAndPersonSince(List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(person), since)).thenReturn(List.of(application));

        final LocalDate startDateSickNote = LocalDate.of(2019, 10, 10);
        final LocalDate endDateSickNote = LocalDate.of(2019, 10, 23);
        final SickNote sickNote = createSickNote(person, startDateSickNote, endDateSickNote, FULL);
        when(sickNoteService.getForStatesAndPersonSince(List.of(ACTIVE), List.of(person), since)).thenReturn(List.of(sickNote));

        final List<Absence> openAbsences = sut.getOpenAbsencesSince(List.of(person), since);
        assertThat(openAbsences).hasSize(2);
        assertThat(openAbsences.get(0).getPerson()).isEqualTo(person);
        assertThat(openAbsences.get(0).getStartDate()).isEqualTo(ZonedDateTime.parse("2019-12-10T00:00Z[Etc/UTC]"));
        assertThat(openAbsences.get(0).getEndDate()).isEqualTo(ZonedDateTime.parse("2019-12-24T00:00Z[Etc/UTC]"));
        assertThat(openAbsences.get(1).getPerson()).isEqualTo(person);
        assertThat(openAbsences.get(1).getStartDate()).isEqualTo(ZonedDateTime.parse("2019-10-10T00:00Z[Etc/UTC]"));
        assertThat(openAbsences.get(1).getEndDate()).isEqualTo(ZonedDateTime.parse("2019-10-24T00:00Z[Etc/UTC]"));
    }

    @Test
    void getOpenAbsencesSince() {

        final Settings settings = new Settings();
        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setTimeZoneId("Etc/UTC");
        settings.setTimeSettings(timeSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate since = LocalDate.of(2020, 11, 13);

        final LocalDate startDate = LocalDate.of(2019, 11, 10);
        final LocalDate endDate = LocalDate.of(2019, 11, 23);
        final Application application = createApplication(person, startDate, endDate, FULL);
        when(applicationService.getForStatesSince(List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), since)).thenReturn(List.of(application));

        final LocalDate startDateSickNote = LocalDate.of(2019, 10, 10);
        final LocalDate endDateSickNote = LocalDate.of(2019, 10, 23);
        final SickNote sickNote = createSickNote(person, startDateSickNote, endDateSickNote, FULL);
        when(sickNoteService.getForStates(List.of(ACTIVE))).thenReturn(List.of(sickNote));

        final List<Absence> openAbsences = sut.getOpenAbsencesSince(since);
        assertThat(openAbsences).hasSize(2);
        assertThat(openAbsences.get(0).getPerson()).isEqualTo(person);
        assertThat(openAbsences.get(0).getStartDate()).isEqualTo(ZonedDateTime.parse("2019-11-10T00:00Z[Etc/UTC]"));
        assertThat(openAbsences.get(0).getEndDate()).isEqualTo(ZonedDateTime.parse("2019-11-24T00:00Z[Etc/UTC]"));
        assertThat(openAbsences.get(1).getPerson()).isEqualTo(person);
        assertThat(openAbsences.get(1).getStartDate()).isEqualTo(ZonedDateTime.parse("2019-10-10T00:00Z[Etc/UTC]"));
        assertThat(openAbsences.get(1).getEndDate()).isEqualTo(ZonedDateTime.parse("2019-10-24T00:00Z[Etc/UTC]"));
    }

    @Test
    void ensureOpenAbsencesCallsWorkingTimeServiceForPersonsAndDateInterval() {

        final Person batman = new Person();
        batman.setId(1);

        final Person superman = new Person();
        superman.setId(2);

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        sut.getOpenAbsences(List.of(batman, superman), start, end);

        verify(workingTimeService).getByPersons(List.of(batman, superman));
    }

    @Test
    void ensureOpenAbsencesCallsApplicationServiceForPersonsAndDateInterval() {

        final Person batman = new Person();
        batman.setId(1);

        final Person superman = new Person();
        superman.setId(2);

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        sut.getOpenAbsences(List.of(batman, superman), start, end);

        verify(applicationService).getForStatesAndPerson(List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(batman, superman), start, end);
    }

    @Test
    void ensureOpenAbsencesCallsSickNotServiceForPersonsAndDateInterval() {

        final Person batman = new Person();
        batman.setId(1);

        final Person superman = new Person();
        superman.setId(2);

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        sut.getOpenAbsences(List.of(batman, superman), start, end);

        verify(sickNoteService).getForStatesAndPerson(List.of(ACTIVE), List.of(batman, superman), start, end);
    }

    @Test
    void ensureOpenAbsencesCallsPublicHolidaysServiceGetDayLengthForEachDateOfApplicationInterval() {

        final Person person = new Person();
        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final WorkingTime workingTime = new WorkingTime(person, start.minusDays(1), GERMANY_BADEN_WUERTTEMBERG, false);

        when(workingTimeService.getByPersons(any())).thenReturn(List.of(workingTime));
        when(workingTimeService.getSystemDefaultFederalState()).thenReturn(GERMANY_BADEN_WUERTTEMBERG);

        final Application application = new Application();
        application.setStartDate(LocalDate.of(2021, MAY, 10));
        application.setEndDate(LocalDate.of(2021, MAY, 12));
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));
        when(publicHolidaysService.getPublicHoliday(any(), any())).thenReturn(Optional.empty());

        sut.getOpenAbsences(List.of(person), start, end);

        verify(publicHolidaysService).getPublicHoliday(LocalDate.of(2021, MAY, 10), GERMANY_BADEN_WUERTTEMBERG);
        verify(publicHolidaysService).getPublicHoliday(LocalDate.of(2021, MAY, 11), GERMANY_BADEN_WUERTTEMBERG);
        verify(publicHolidaysService).getPublicHoliday(LocalDate.of(2021, MAY, 12), GERMANY_BADEN_WUERTTEMBERG);

        verifyNoMoreInteractions(publicHolidaysService);
    }

    @Test
    void ensureVacationMorning() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1);

        final WorkingTime workingTime = new WorkingTime(batman, start, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY), FULL);
        when(workingTimeService.getByPersons(any())).thenReturn(List.of(workingTime));

        final Application application = new Application();
        application.setId(42);
        application.setPerson(batman);
        application.setStartDate(start.plusDays(1));
        application.setEndDate(start.plusDays(1));
        application.setDayLength(MORNING);
        application.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        when(publicHolidaysService.getPublicHoliday(any(), any())).thenReturn(Optional.empty());

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning()).isNotEmpty();
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning().map(RecordInfo::getId)).hasValue(42);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning().map(RecordInfo::getStatus)).hasValue(AbsencePeriod.Record.AbsenceStatus.ALLOWED);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning().map(RecordInfo::getType)).hasValue(AbsencePeriod.Record.AbsenceType.VACATION);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon()).isEmpty();
    }

    @Test
    void ensureVacationNoon() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1);

        final WorkingTime workingTime = new WorkingTime(batman, start.minusDays(1), GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY), FULL);
        when(workingTimeService.getByPersons(any())).thenReturn(List.of(workingTime));

        final Application application = new Application();
        application.setId(42);
        application.setPerson(batman);
        application.setStartDate(start.plusDays(1));
        application.setEndDate(start.plusDays(1));
        application.setDayLength(NOON);
        application.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));
        when(publicHolidaysService.getPublicHoliday(any(), any())).thenReturn(Optional.empty());

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning()).isEmpty();
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon()).isNotEmpty();
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon().map(RecordInfo::getId)).hasValue(42);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon().map(RecordInfo::getStatus)).hasValue(AbsencePeriod.Record.AbsenceStatus.ALLOWED);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon().map(RecordInfo::getType)).hasValue(AbsencePeriod.Record.AbsenceType.VACATION);
    }

    @Test
    void ensureVacationWithEmptyWorkingTimeFallsBackToSystemDefaultFederalState() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1);

        when(workingTimeService.getByPersons(any())).thenReturn(emptyList());
        when(workingTimeService.getSystemDefaultFederalState()).thenReturn(GERMANY_BERLIN);

        final Application application = new Application();
        application.setId(42);
        application.setPerson(batman);
        application.setStartDate(start.plusDays(1));
        application.setEndDate(start.plusDays(1));
        application.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));
        when(publicHolidaysService.getPublicHoliday(any(), eq(GERMANY_BERLIN))).thenReturn(Optional.empty());

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
    }

    @Test
    void ensureVacationConsidersWorkingTimeOfPerson() {
        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1);

        final WorkingTime workingTimePastToNow = new WorkingTime(batman, start, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTimePastToNow.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);

        final WorkingTime workingTimeFuture = new WorkingTime(batman, start.plusDays(10), GERMANY_BADEN_WUERTTEMBERG, false);
        workingTimeFuture.setWorkingDays(List.of(WEDNESDAY, THURSDAY, FRIDAY), FULL);

        when(workingTimeService.getByPersons(any())).thenReturn(List.of(workingTimePastToNow, workingTimeFuture));

        final Application application = new Application();
        application.setId(42);
        application.setPerson(batman);
        // application for leave from monday to friday. while monday and thursday are both no workday.
        application.setStartDate(LocalDate.of(2021, MAY, 17));
        application.setEndDate(LocalDate.of(2021, MAY, 21));
        application.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));
        when(publicHolidaysService.getPublicHoliday(any(), eq(GERMANY_BADEN_WUERTTEMBERG))).thenReturn(Optional.empty());

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);

        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords()).hasSize(3);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getDate()).isEqualTo(LocalDate.of(2021, MAY, 19));
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(1).getDate()).isEqualTo(LocalDate.of(2021, MAY, 20));
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(2).getDate()).isEqualTo(LocalDate.of(2021, MAY, 21));
    }

    @Test
    void ensureSickMorning() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1);

        final WorkingTime workingTime = new WorkingTime(batman, start.minusDays(1), GERMANY_BADEN_WUERTTEMBERG, false);
        when(workingTimeService.getByPersons(any())).thenReturn(List.of(workingTime));

        final SickNote sickNote = new SickNote();
        sickNote.setId(42);
        sickNote.setPerson(batman);
        sickNote.setStartDate(start.plusDays(1));
        sickNote.setEndDate(start.plusDays(1));
        sickNote.setDayLength(MORNING);

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));
        when(publicHolidaysService.getPublicHoliday(any(), any())).thenReturn(Optional.empty());

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning()).isNotEmpty();
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning().map(RecordInfo::getId)).hasValue(42);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning().map(RecordInfo::getStatus)).hasValue(AbsencePeriod.Record.AbsenceStatus.ACTIVE);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning().map(RecordInfo::getType)).hasValue(AbsencePeriod.Record.AbsenceType.SICK);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon()).isEmpty();
    }

    @Test
    void ensureSickNoon() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1);

        final WorkingTime workingTime = new WorkingTime(batman, start.minusDays(1), GERMANY_BADEN_WUERTTEMBERG, false);
        when(workingTimeService.getByPersons(any())).thenReturn(List.of(workingTime));

        final SickNote sickNote = new SickNote();
        sickNote.setId(42);
        sickNote.setPerson(batman);
        sickNote.setStartDate(start.plusDays(1));
        sickNote.setEndDate(start.plusDays(1));
        sickNote.setDayLength(NOON);

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));
        when(publicHolidaysService.getPublicHoliday(any(), any())).thenReturn(Optional.empty());

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning()).isEmpty();
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon()).isNotEmpty();
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon().map(RecordInfo::getId)).hasValue(42);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon().map(RecordInfo::getStatus)).hasValue(AbsencePeriod.Record.AbsenceStatus.ACTIVE);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon().map(RecordInfo::getType)).hasValue(AbsencePeriod.Record.AbsenceType.SICK);
    }

    @Test
    void ensureSickWithEmptyWorkingTimeFallsBackToSystemDefaultFederalState() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1);

        when(workingTimeService.getByPersons(any())).thenReturn(emptyList());
        when(workingTimeService.getSystemDefaultFederalState()).thenReturn(GERMANY_BERLIN);
        when(publicHolidaysService.getPublicHoliday(any(), eq(GERMANY_BERLIN))).thenReturn(Optional.empty());

        final SickNote sickNote = new SickNote();
        sickNote.setId(42);
        sickNote.setPerson(batman);
        sickNote.setStartDate(start.plusDays(1));
        sickNote.setEndDate(start.plusDays(1));
        sickNote.setDayLength(NOON);

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
    }

    @Test
    void ensureVacationMorningAndSickNoon() {
        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1);

        final WorkingTime workingTime = new WorkingTime(batman, start.minusDays(1), GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY), FULL);
        when(workingTimeService.getByPersons(any())).thenReturn(List.of(workingTime));

        final Application application = new Application();
        application.setId(42);
        application.setPerson(batman);
        application.setStartDate(start.plusDays(1));
        application.setEndDate(start.plusDays(1));
        application.setDayLength(MORNING);
        application.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final SickNote sickNote = new SickNote();
        sickNote.setId(1337);
        sickNote.setPerson(batman);
        sickNote.setStartDate(start.plusDays(1));
        sickNote.setEndDate(start.plusDays(1));
        sickNote.setDayLength(NOON);

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));

        when(publicHolidaysService.getPublicHoliday(any(), any())).thenReturn(Optional.empty());

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(2);

        // vacation
        assertThat(actualAbsences.get(0).getAbsenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning()).isNotEmpty();
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning().map(RecordInfo::getId)).hasValue(42);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning().map(RecordInfo::getType)).hasValue(AbsencePeriod.Record.AbsenceType.VACATION);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon()).isEmpty();

        // sick
        assertThat(actualAbsences.get(1).getAbsenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(1).getAbsenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(1).getAbsenceRecords().get(0).getMorning()).isEmpty();
        assertThat(actualAbsences.get(1).getAbsenceRecords().get(0).getNoon()).isNotEmpty();
        assertThat(actualAbsences.get(1).getAbsenceRecords().get(0).getNoon().map(RecordInfo::getId)).hasValue(1337);
        assertThat(actualAbsences.get(1).getAbsenceRecords().get(0).getNoon().map(RecordInfo::getType)).hasValue(AbsencePeriod.Record.AbsenceType.SICK);
    }

    @Test
    void ensureMultipleVacationDaysWithApplicationsOutsideTheAskedDateRange() {
        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1);

        final WorkingTime workingTime = new WorkingTime(batman, start.minusDays(1), GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY), FULL);
        when(workingTimeService.getByPersons(any())).thenReturn(List.of(workingTime));

        final Application application = new Application();
        application.setId(42);
        application.setPerson(batman);
        application.setStartDate(LocalDate.of(2021, MAY, 31));
        application.setEndDate(LocalDate.of(2021, JUNE, 10));
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));
        when(publicHolidaysService.getPublicHoliday(any(), any())).thenReturn(Optional.empty());

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);

        assertThat(actualAbsences).hasSize(1);

        final List<AbsencePeriod.Record> absenceRecords = actualAbsences.get(0).getAbsenceRecords();
        assertThat(absenceRecords).hasSize(1);
        assertThat(absenceRecords.get(0).getPerson()).isSameAs(batman);
        assertThat(absenceRecords.get(0).getDate()).isEqualTo(LocalDate.of(2021, MAY, 31));
        assertThat(absenceRecords.get(0).getMorning()).isPresent();
        assertThat(absenceRecords.get(0).getNoon()).isPresent();
    }

    @Test
    void ensureMultipleSickDaysWithApplicationsOutsideTheAskedDateRange() {
        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1);

        final WorkingTime workingTime = new WorkingTime(batman, start.minusDays(1), GERMANY_BADEN_WUERTTEMBERG, false);
        when(workingTimeService.getByPersons(any())).thenReturn(List.of(workingTime));

        final SickNote sickNote = new SickNote();
        sickNote.setId(42);
        sickNote.setPerson(batman);
        sickNote.setStartDate(LocalDate.of(2021, MAY, 31));
        sickNote.setEndDate(LocalDate.of(2021, JUNE, 10));
        sickNote.setDayLength(FULL);

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));
        when(publicHolidaysService.getPublicHoliday(any(), any())).thenReturn(Optional.empty());

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);

        assertThat(actualAbsences).hasSize(1);

        final List<AbsencePeriod.Record> absenceRecords = actualAbsences.get(0).getAbsenceRecords();
        assertThat(absenceRecords).hasSize(1);
        assertThat(absenceRecords.get(0).getPerson()).isSameAs(batman);
        assertThat(absenceRecords.get(0).getDate()).isEqualTo(LocalDate.of(2021, MAY, 31));
        assertThat(absenceRecords.get(0).getMorning()).isPresent();
        assertThat(absenceRecords.get(0).getNoon()).isPresent();
    }

    @Test
    void ensureMultipleVacationDaysWithPublicHolidayFull() {
        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1);

        final WorkingTime workingTime = new WorkingTime(batman, start.minusDays(1), GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY), FULL);
        when(workingTimeService.getByPersons(any())).thenReturn(List.of(workingTime));

        final Application application = new Application();
        application.setId(42);
        application.setPerson(batman);
        application.setStartDate(LocalDate.of(2021, MAY, 1));
        application.setEndDate(LocalDate.of(2021, MAY, 31));
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));
        when(publicHolidaysService.getPublicHoliday(any(), any())).thenReturn(Optional.empty());
        when(publicHolidaysService.getPublicHoliday(eq(LocalDate.of(2021, MAY, 20)), any())).thenReturn(Optional.of(new PublicHoliday(start, FULL, "")));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);

        assertThat(actualAbsences).hasSize(1);

        final List<AbsencePeriod.Record> absenceRecords = actualAbsences.get(0).getAbsenceRecords();
        assertThat(absenceRecords).hasSize(30);

        // 1. May to 19. May -> vacation
        IntStream.range(0, 19).forEach(index -> {
            assertThat(absenceRecords.get(index).getPerson()).isSameAs(batman);
            assertThat(absenceRecords.get(index).getDate()).isEqualTo(LocalDate.of(2021, MAY, index + 1));
            assertThat(absenceRecords.get(index).getMorning()).isPresent();
            assertThat(absenceRecords.get(index).getNoon()).isPresent();
        });

        // 20. May -> public holiday
        // is not existent in absences

        // 21. May to 31. May -> vacation
        IntStream.range(19, 30).forEach(index -> {
            assertThat(absenceRecords.get(index).getPerson()).isSameAs(batman);
            assertThat(absenceRecords.get(index).getDate()).isEqualTo(LocalDate.of(2021, MAY, index + 2));
            assertThat(absenceRecords.get(index).getMorning()).isPresent();
            assertThat(absenceRecords.get(index).getNoon()).isPresent();
        });
    }

    @Test
    void ensureMultipleVacationDaysWithPublicHolidayNoon() {
        final LocalDate start = LocalDate.of(2021, DECEMBER, 1);
        final LocalDate end = LocalDate.of(2021, DECEMBER, 31);

        final Person batman = new Person();
        batman.setId(1);

        final WorkingTime workingTime = new WorkingTime(batman, start.minusDays(1), GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY), FULL);
        when(workingTimeService.getByPersons(any())).thenReturn(List.of(workingTime));

        final Application application = new Application();
        application.setId(42);
        application.setPerson(batman);
        application.setStartDate(LocalDate.of(2021, DECEMBER, 1));
        application.setEndDate(LocalDate.of(2021, DECEMBER, 31));
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        when(publicHolidaysService.getPublicHoliday(any(), any())).thenReturn(Optional.empty());
        when(publicHolidaysService.getPublicHoliday(eq(LocalDate.of(2021, DECEMBER, 24)), any())).thenReturn(Optional.of(new PublicHoliday(start, NOON, "")));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);

        assertThat(actualAbsences).hasSize(1);

        final List<AbsencePeriod.Record> absenceRecords = actualAbsences.get(0).getAbsenceRecords();
        assertThat(absenceRecords).hasSize(31);

        // 1. December to 23. December -> vacation
        IntStream.range(0, 23).forEach(index -> {
            assertThat(absenceRecords.get(index).getPerson()).isSameAs(batman);
            assertThat(absenceRecords.get(index).getDate()).isEqualTo(LocalDate.of(2021, DECEMBER, index + 1));
            assertThat(absenceRecords.get(index).getMorning()).isPresent();
            assertThat(absenceRecords.get(index).getNoon()).isPresent();
        });

        // 24. December -> morning: vacation, noon: public holiday
        assertThat(absenceRecords.get(23).getPerson()).isSameAs(batman);
        assertThat(absenceRecords.get(23).getDate()).isEqualTo(LocalDate.of(2021, DECEMBER, 24));
        assertThat(absenceRecords.get(23).getMorning().map(RecordInfo::getType)).hasValue(AbsencePeriod.Record.AbsenceType.VACATION);
        assertThat(absenceRecords.get(23).getNoon()).isEmpty();

        // 25. December to 31. December -> vacation
        IntStream.range(24, 30).forEach(index -> {
            assertThat(absenceRecords.get(index).getPerson()).isSameAs(batman);
            assertThat(absenceRecords.get(index).getDate()).isEqualTo(LocalDate.of(2021, DECEMBER, index + 1));
            assertThat(absenceRecords.get(index).getMorning()).isPresent();
            assertThat(absenceRecords.get(index).getNoon()).isPresent();
        });
    }

    @Test
    void ensureMultipleVacationDaysWithPublicHolidayMorning() {
        final LocalDate start = LocalDate.of(2021, DECEMBER, 1);
        final LocalDate end = LocalDate.of(2021, DECEMBER, 31);

        final Person batman = new Person();
        batman.setId(1);

        final WorkingTime workingTime = new WorkingTime(batman, start.minusDays(1), GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY), FULL);
        when(workingTimeService.getByPersons(any())).thenReturn(List.of(workingTime));

        final Application application = new Application();
        application.setId(42);
        application.setPerson(batman);
        application.setStartDate(LocalDate.of(2021, DECEMBER, 1));
        application.setEndDate(LocalDate.of(2021, DECEMBER, 31));
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        when(publicHolidaysService.getPublicHoliday(any(), any())).thenReturn(Optional.empty());
        when(publicHolidaysService.getPublicHoliday(eq(LocalDate.of(2021, DECEMBER, 24)), any())).thenReturn(Optional.of(new PublicHoliday(start, MORNING, "")));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);

        assertThat(actualAbsences).hasSize(1);

        final List<AbsencePeriod.Record> absenceRecords = actualAbsences.get(0).getAbsenceRecords();
        assertThat(absenceRecords).hasSize(31);

        // 1. December to 23. December -> vacation
        IntStream.range(0, 23).forEach(index -> {
            assertThat(absenceRecords.get(index).getPerson()).isSameAs(batman);
            assertThat(absenceRecords.get(index).getDate()).isEqualTo(LocalDate.of(2021, DECEMBER, index + 1));
            assertThat(absenceRecords.get(index).getMorning()).isPresent();
            assertThat(absenceRecords.get(index).getNoon()).isPresent();
        });

        // 24. December -> morning: vacation, noon: public holiday
        assertThat(absenceRecords.get(23).getPerson()).isSameAs(batman);
        assertThat(absenceRecords.get(23).getDate()).isEqualTo(LocalDate.of(2021, DECEMBER, 24));
        assertThat(absenceRecords.get(23).getMorning()).isEmpty();
        assertThat(absenceRecords.get(23).getNoon().map(RecordInfo::getType)).hasValue(AbsencePeriod.Record.AbsenceType.VACATION);

        // 25. December to 31. December -> vacation
        IntStream.range(24, 30).forEach(index -> {
            assertThat(absenceRecords.get(index).getPerson()).isSameAs(batman);
            assertThat(absenceRecords.get(index).getDate()).isEqualTo(LocalDate.of(2021, DECEMBER, index + 1));
            assertThat(absenceRecords.get(index).getMorning()).isPresent();
            assertThat(absenceRecords.get(index).getNoon()).isPresent();
        });
    }
}
