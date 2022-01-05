package org.synyx.urlaubsverwaltung.absence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.period.DayLength;
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
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
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
        application.setDayLength(DayLength.MORNING);
        application.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        when(publicHolidaysService.getPublicHoliday(any(), any())).thenReturn(Optional.empty());

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning()).isNotEmpty();
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getId)).hasValue(42);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getStatus)).hasValue(AbsencePeriod.AbsenceStatus.ALLOWED);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getType)).hasValue(AbsencePeriod.AbsenceType.VACATION);
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
        application.setDayLength(DayLength.NOON);
        application.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));
        when(publicHolidaysService.getPublicHoliday(any(), any())).thenReturn(Optional.empty());

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning()).isEmpty();
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon()).isNotEmpty();
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getId)).hasValue(42);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getStatus)).hasValue(AbsencePeriod.AbsenceStatus.ALLOWED);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getType)).hasValue(AbsencePeriod.AbsenceType.VACATION);
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
        sickNote.setDayLength(DayLength.MORNING);

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));
        when(publicHolidaysService.getPublicHoliday(any(), any())).thenReturn(Optional.empty());

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning()).isNotEmpty();
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getId)).hasValue(42);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getStatus)).hasValue(AbsencePeriod.AbsenceStatus.ACTIVE);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getType)).hasValue(AbsencePeriod.AbsenceType.SICK);
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
        sickNote.setDayLength(DayLength.NOON);

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));
        when(publicHolidaysService.getPublicHoliday(any(), any())).thenReturn(Optional.empty());

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning()).isEmpty();
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon()).isNotEmpty();
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getId)).hasValue(42);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getStatus)).hasValue(AbsencePeriod.AbsenceStatus.ACTIVE);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getType)).hasValue(AbsencePeriod.AbsenceType.SICK);
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
        sickNote.setDayLength(DayLength.NOON);

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
        application.setDayLength(DayLength.MORNING);
        application.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final SickNote sickNote = new SickNote();
        sickNote.setId(1337);
        sickNote.setPerson(batman);
        sickNote.setStartDate(start.plusDays(1));
        sickNote.setEndDate(start.plusDays(1));
        sickNote.setDayLength(DayLength.NOON);

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));

        when(publicHolidaysService.getPublicHoliday(any(), any())).thenReturn(Optional.empty());

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(2);

        // vacation
        assertThat(actualAbsences.get(0).getAbsenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning()).isNotEmpty();
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getId)).hasValue(42);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getType)).hasValue(AbsencePeriod.AbsenceType.VACATION);
        assertThat(actualAbsences.get(0).getAbsenceRecords().get(0).getNoon()).isEmpty();

        // sick
        assertThat(actualAbsences.get(1).getAbsenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(1).getAbsenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(1).getAbsenceRecords().get(0).getMorning()).isEmpty();
        assertThat(actualAbsences.get(1).getAbsenceRecords().get(0).getNoon()).isNotEmpty();
        assertThat(actualAbsences.get(1).getAbsenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getId)).hasValue(1337);
        assertThat(actualAbsences.get(1).getAbsenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getType)).hasValue(AbsencePeriod.AbsenceType.SICK);
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
        when(publicHolidaysService.getPublicHoliday(eq(LocalDate.of(2021, DECEMBER, 24)), any())).thenReturn(Optional.of(new PublicHoliday(start, DayLength.NOON, "")));

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
        assertThat(absenceRecords.get(23).getMorning().map(AbsencePeriod.RecordInfo::getType)).hasValue(AbsencePeriod.AbsenceType.VACATION);
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
        when(publicHolidaysService.getPublicHoliday(eq(LocalDate.of(2021, DECEMBER, 24)), any())).thenReturn(Optional.of(new PublicHoliday(start, DayLength.MORNING, "")));

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
        assertThat(absenceRecords.get(23).getNoon().map(AbsencePeriod.RecordInfo::getType)).hasValue(AbsencePeriod.AbsenceType.VACATION);

        // 25. December to 31. December -> vacation
        IntStream.range(24, 30).forEach(index -> {
            assertThat(absenceRecords.get(index).getPerson()).isSameAs(batman);
            assertThat(absenceRecords.get(index).getDate()).isEqualTo(LocalDate.of(2021, DECEMBER, index + 1));
            assertThat(absenceRecords.get(index).getMorning()).isPresent();
            assertThat(absenceRecords.get(index).getNoon()).isPresent();
        });
    }
}
