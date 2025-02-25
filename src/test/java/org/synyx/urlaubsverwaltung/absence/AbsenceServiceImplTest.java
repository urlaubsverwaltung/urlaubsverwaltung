package org.synyx.urlaubsverwaltung.absence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.time.Month.DECEMBER;
import static java.time.Month.JUNE;
import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.REVOKED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.CONVERTED_TO_VACATION;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.SUBMITTED;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.PUBLIC_HOLIDAY;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

@ExtendWith(MockitoExtension.class)
class AbsenceServiceImplTest {

    private AbsenceServiceImpl sut;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private WorkingTimeCalendarService workingTimeCalendarService;

    @BeforeEach
    void setUp() {
        sut = new AbsenceServiceImpl(applicationService, sickNoteService, workingTimeCalendarService);
    }

    @Test
    void ensureOpenAbsencesCallsApplicationServiceForPersonsAndDateInterval() {

        final Person batman = new Person();
        batman.setId(1L);

        final Person superman = new Person();
        superman.setId(2L);

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        sut.getOpenAbsences(List.of(batman, superman), start, end);

        verify(applicationService).getForStatesAndPerson(activeStatuses(), List.of(batman, superman), start, end);
    }

    @Test
    void ensureOpenAbsencesCallsSickNotServiceForPersonsAndDateInterval() {

        final Person batman = new Person();
        batman.setId(1L);

        final Person superman = new Person();
        superman.setId(2L);

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        sut.getOpenAbsences(List.of(batman, superman), start, end);

        verify(sickNoteService).getForStatesAndPerson(List.of(SUBMITTED, ACTIVE), List.of(batman, superman), start, end);
    }

    @Test
    void ensureVacationMorning() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
                .id(1L)
                .category(VacationCategory.HOLIDAY)
                .visibleToEveryone(true)
                .build();

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(start.plusDays(1));
        application.setEndDate(start.plusDays(1));
        application.setDayLength(DayLength.MORNING);
        application.setStatus(ALLOWED);
        application.setVacationType(vacationType);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning()).isNotEmpty();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().flatMap(AbsencePeriod.RecordInfo::getId)).hasValue(42L);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getStatus)).hasValue(AbsencePeriod.AbsenceStatus.ALLOWED);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.VACATION);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getCategory)).hasValue(Optional.of("HOLIDAY"));
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getTypeId)).hasValue(Optional.of(1L));
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::isVisibleToEveryone)).hasValue(true);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon()).isEmpty();
    }

    @Test
    void ensureVacationNoon() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
                .id(1L)
                .category(VacationCategory.HOLIDAY)
                .visibleToEveryone(false)
                .build();

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(start.plusDays(1));
        application.setEndDate(start.plusDays(1));
        application.setDayLength(DayLength.NOON);
        application.setStatus(ALLOWED);
        application.setVacationType(vacationType);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning()).isEmpty();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon()).isNotEmpty();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().flatMap(AbsencePeriod.RecordInfo::getId)).hasValue(42L);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getStatus)).hasValue(AbsencePeriod.AbsenceStatus.ALLOWED);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.VACATION);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getCategory)).hasValue(Optional.of("HOLIDAY"));
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getTypeId)).hasValue(Optional.of(1L));
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::isVisibleToEveryone)).hasValue(false);
    }

    @Test
    void ensureVacationFull() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
                .id(1L)
                .category(VacationCategory.HOLIDAY)
                .visibleToEveryone(false)
                .build();

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(start.plusDays(1));
        application.setEndDate(start.plusDays(1));
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(vacationType);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning()).isNotEmpty();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().flatMap(AbsencePeriod.RecordInfo::getId)).hasValue(42L);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getStatus)).hasValue(AbsencePeriod.AbsenceStatus.ALLOWED);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.VACATION);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getCategory)).hasValue(Optional.of("HOLIDAY"));
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getTypeId)).hasValue(Optional.of(1L));
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::isVisibleToEveryone)).hasValue(false);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon()).isNotEmpty();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().flatMap(AbsencePeriod.RecordInfo::getId)).hasValue(42L);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getStatus)).hasValue(AbsencePeriod.AbsenceStatus.ALLOWED);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.VACATION);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getCategory)).hasValue(Optional.of("HOLIDAY"));
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getTypeId)).hasValue(Optional.of(1L));
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::isVisibleToEveryone)).hasValue(false);
    }

    @Test
    void ensureVacationFullWithPublicHolidayNoon() {

        final LocalDate start = LocalDate.of(2021, DECEMBER, 1);
        final LocalDate end = LocalDate.of(2021, DECEMBER, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> {
            if (date.equals(LocalDate.of(2021, DECEMBER, 24))) {
                // half day "public holiday" at noon -> working in the morning
                return new WorkingDayInformation(MORNING, WORKDAY, PUBLIC_HOLIDAY);
            } else {
                return fullWorkDay();
            }
        });
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
                .id(1L)
                .category(VacationCategory.HOLIDAY)
                .visibleToEveryone(false)
                .build();

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(LocalDate.of(2021, DECEMBER, 24));
        application.setEndDate(LocalDate.of(2021, DECEMBER, 24));
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(vacationType);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(2);

        assertThat(actualAbsences.get(0)).satisfies(absence -> {
            assertThat(absence.absenceRecords()).hasSize(1);
            assertThat(absence.absenceRecords().getFirst()).satisfies(record -> {
                assertThat(record.isHalfDayAbsence()).isTrue();
                assertThat(record.getPerson()).isSameAs(batman);
                assertThat(record.getMorning()).isPresent();
                assertThat(record.getMorning().get()).satisfies(morning -> {
                    assertThat(morning.getId()).hasValue(42L);
                    assertThat(morning.getStatus()).isEqualTo(AbsencePeriod.AbsenceStatus.ALLOWED);
                    assertThat(morning.getAbsenceType()).isEqualTo(AbsencePeriod.AbsenceType.VACATION);
                    assertThat(morning.getCategory()).hasValue("HOLIDAY");
                    assertThat(morning.getTypeId()).hasValue(1L);
                    assertThat(morning.isVisibleToEveryone()).isFalse();
                    assertThat(morning.hasStatusAllowed()).isTrue();
                    assertThat(morning.hasStatusWaiting()).isFalse();
                    assertThat(morning.hasStatusTemporaryAllowed()).isFalse();
                    assertThat(morning.hasStatusAllowedCancellationRequested()).isFalse();
                });
                assertThat(record.getNoon()).isEmpty();
            });
        });

        assertThat(actualAbsences.get(1)).satisfies(absence -> {
            assertThat(absence.absenceRecords()).hasSize(1);
            assertThat(absence.absenceRecords().getFirst()).satisfies(record -> {
                assertThat(record.isHalfDayAbsence()).isTrue();
                assertThat(record.getPerson()).isSameAs(batman);
                assertThat(record.getMorning()).isEmpty();
                assertThat(record.getNoon()).isPresent();
                assertThat(record.getNoon().get()).satisfies(noon -> {
                    assertThat(noon.getId()).isEmpty();
                    assertThat(noon.getStatus()).isEqualTo(AbsencePeriod.AbsenceStatus.ACTIVE);
                    assertThat(noon.getAbsenceType()).isEqualTo(AbsencePeriod.AbsenceType.PUBLIC_HOLIDAY);
                    assertThat(noon.getCategory()).isEmpty();
                    assertThat(noon.getTypeId()).isEmpty();
                    assertThat(noon.isVisibleToEveryone()).isFalse();
                    assertThat(noon.hasStatusAllowed()).isFalse();
                    assertThat(noon.hasStatusWaiting()).isFalse();
                    assertThat(noon.hasStatusTemporaryAllowed()).isFalse();
                    assertThat(noon.hasStatusAllowedCancellationRequested()).isFalse();
                });
            });
        });
    }

    @Test
    void ensureSickMorning() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final SickNote sickNote = SickNote.builder()
                .id(42L)
                .person(batman)
                .status(ACTIVE)
                .sickNoteType(anySickNoteType())
                .startDate(start.plusDays(1))
                .endDate(start.plusDays(1))
                .dayLength(DayLength.MORNING)
                .build();

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning()).isNotEmpty();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().flatMap(AbsencePeriod.RecordInfo::getId)).hasValue(42L);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getStatus)).hasValue(AbsencePeriod.AbsenceStatus.ACTIVE);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.SICK);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon()).isEmpty();
    }

    @Test
    void ensureSickNoon() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final SickNote sickNote = SickNote.builder()
                .id(42L)
                .person(batman)
                .status(ACTIVE)
                .sickNoteType(anySickNoteType())
                .startDate(start.plusDays(1))
                .endDate(start.plusDays(1))
                .dayLength(DayLength.NOON)
                .build();

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning()).isEmpty();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon()).isNotEmpty();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().flatMap(AbsencePeriod.RecordInfo::getId)).hasValue(42L);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getStatus)).hasValue(AbsencePeriod.AbsenceStatus.ACTIVE);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.SICK);
    }

    @Test
    void ensureSickFull() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final SickNote sickNote = SickNote.builder()
                .id(42L)
                .person(batman)
                .status(ACTIVE)
                .sickNoteType(anySickNoteType())
                .startDate(start.plusDays(1))
                .endDate(start.plusDays(1))
                .dayLength(FULL)
                .build();

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.getFirst()).satisfies(absence -> {
            assertThat(absence.absenceRecords()).hasSize(1);
            assertThat(absence.absenceRecords().getFirst()).satisfies(record -> {
                assertThat(record.isHalfDayAbsence()).isFalse();
                assertThat(record.getPerson()).isSameAs(batman);
                assertThat(record.getMorning()).isPresent();
                assertThat(record.getMorning().get()).satisfies(morning -> {
                    assertThat(morning.getId()).hasValue(42L);
                    assertThat(morning.getStatus()).isEqualTo(AbsencePeriod.AbsenceStatus.ACTIVE);
                    assertThat(morning.getAbsenceType()).isEqualTo(AbsencePeriod.AbsenceType.SICK);
                });
                assertThat(record.getNoon()).isPresent();
                assertThat(record.getNoon().get()).satisfies(noon -> {
                    assertThat(noon.getId()).hasValue(42L);
                    assertThat(noon.getStatus()).isEqualTo(AbsencePeriod.AbsenceStatus.ACTIVE);
                    assertThat(noon.getAbsenceType()).isEqualTo(AbsencePeriod.AbsenceType.SICK);
                });
            });
        });
    }

    @Test
    void ensureSickFullWithPubicHolidayNoon() {

        final LocalDate start = LocalDate.of(2021, DECEMBER, 1);
        final LocalDate end = LocalDate.of(2021, DECEMBER, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> {
            if (date.isEqual(LocalDate.of(2021, DECEMBER, 24))) {
                return new WorkingDayInformation(MORNING, WORKDAY, PUBLIC_HOLIDAY);
            } else {
                return fullWorkDay();
            }
        });
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final SickNote sickNote = SickNote.builder()
                .id(42L)
                .person(batman)
                .status(ACTIVE)
                .sickNoteType(anySickNoteType())
                .startDate(LocalDate.of(2021, DECEMBER, 24))
                .endDate(LocalDate.of(2021, DECEMBER, 24))
                .dayLength(FULL)
                .build();

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(2);

        assertThat(actualAbsences.get(0)).satisfies(absence -> {
            assertThat(absence.absenceRecords()).hasSize(1);
            assertThat(absence.absenceRecords().getFirst()).satisfies(record -> {
                assertThat(record.isHalfDayAbsence()).isTrue();
                assertThat(record.getPerson()).isSameAs(batman);
                assertThat(record.getMorning()).isPresent();
                assertThat(record.getMorning().get()).satisfies(morning -> {
                    assertThat(morning.getId()).hasValue(42L);
                    assertThat(morning.getStatus()).isEqualTo(AbsencePeriod.AbsenceStatus.ACTIVE);
                    assertThat(morning.getAbsenceType()).isEqualTo(AbsencePeriod.AbsenceType.SICK);
                    assertThat(morning.getCategory()).hasValue(SickNoteCategory.SICK_NOTE.name());
                    assertThat(morning.getTypeId()).hasValue(1L);
                    assertThat(morning.isVisibleToEveryone()).isFalse();
                    assertThat(morning.hasStatusAllowed()).isFalse();
                    assertThat(morning.hasStatusWaiting()).isFalse();
                    assertThat(morning.hasStatusTemporaryAllowed()).isFalse();
                    assertThat(morning.hasStatusAllowedCancellationRequested()).isFalse();
                });
                assertThat(record.getNoon()).isEmpty();
            });
        });

        assertThat(actualAbsences.get(1)).satisfies(absence -> {
            assertThat(absence.absenceRecords()).hasSize(1);
            assertThat(absence.absenceRecords().getFirst()).satisfies(record -> {
                assertThat(record.isHalfDayAbsence()).isTrue();
                assertThat(record.getPerson()).isSameAs(batman);
                assertThat(record.getMorning()).isEmpty();
                assertThat(record.getNoon()).isPresent();
                assertThat(record.getNoon().get()).satisfies(noon -> {
                    assertThat(noon.getId()).isEmpty();
                    assertThat(noon.getStatus()).isEqualTo(AbsencePeriod.AbsenceStatus.ACTIVE);
                    assertThat(noon.getAbsenceType()).isEqualTo(AbsencePeriod.AbsenceType.PUBLIC_HOLIDAY);
                });
            });
        });
    }

    @Test
    void ensureVacationMorningAndSickNoon() {
        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(start.plusDays(1));
        application.setEndDate(start.plusDays(1));
        application.setDayLength(DayLength.MORNING);
        application.setStatus(ALLOWED);
        application.setVacationType(anyVacationType());

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final SickNote sickNote = SickNote.builder()
                .id(1337L)
                .person(batman)
                .status(SickNoteStatus.CANCELLED)
                .sickNoteType(anySickNoteType())
                .startDate(start.plusDays(1))
                .endDate(start.plusDays(1))
                .dayLength(DayLength.NOON)
                .build();

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(2);

        // vacation
        assertThat(actualAbsences.get(0).absenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning()).isNotEmpty();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().flatMap(AbsencePeriod.RecordInfo::getId)).hasValue(42L);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.VACATION);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon()).isEmpty();

        // sick
        assertThat(actualAbsences.get(1).absenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(1).absenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(1).absenceRecords().get(0).getMorning()).isEmpty();
        assertThat(actualAbsences.get(1).absenceRecords().get(0).getNoon()).isNotEmpty();
        assertThat(actualAbsences.get(1).absenceRecords().get(0).getNoon().flatMap(AbsencePeriod.RecordInfo::getId)).hasValue(1337L);
        assertThat(actualAbsences.get(1).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.SICK);
    }

    @Test
    void ensureMultipleVacationDaysWithApplicationsOutsideTheAskedDateRange() {
        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(LocalDate.of(2021, MAY, 31));
        application.setEndDate(LocalDate.of(2021, JUNE, 10));
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(anyVacationType());

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);

        assertThat(actualAbsences).hasSize(1);

        final List<AbsencePeriod.Record> absenceRecords = actualAbsences.get(0).absenceRecords();
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
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final SickNote sickNote = SickNote.builder()
            .id(42L)
            .person(batman)
            .status(SickNoteStatus.CANCELLED)
            .sickNoteType(anySickNoteType())
            .startDate(LocalDate.of(2021, MAY, 31))
            .endDate(LocalDate.of(2021, JUNE, 10))
            .dayLength(FULL)
            .build();

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);

        assertThat(actualAbsences).hasSize(1);

        final List<AbsencePeriod.Record> absenceRecords = actualAbsences.get(0).absenceRecords();
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
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> {
            if (date.equals(LocalDate.of(2021, MAY, 20))) {
                // public holiday -> no work 🎉
                return new WorkingDayInformation(ZERO, PUBLIC_HOLIDAY, PUBLIC_HOLIDAY);
            } else {
                return fullWorkDay();
            }
        });
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(LocalDate.of(2021, MAY, 1));
        application.setEndDate(LocalDate.of(2021, MAY, 31));
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(anyVacationType());

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);

        assertThat(actualAbsences).hasSize(2);

        assertThat(actualAbsences.get(0)).satisfies(absence -> {

            final List<AbsencePeriod.Record> absenceRecords = absence.absenceRecords();
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
        });

        // full public_holiday at 20th may
        assertThat(actualAbsences.get(1)).satisfies(absence -> {
            assertThat(absence.absenceRecords()).hasSize(1);
            assertThat(absence.absenceRecords().get(0)).satisfies(absenceRecord -> {
                assertThat(absenceRecord.isHalfDayAbsence()).isFalse();
                assertThat(absenceRecord.getDate()).isEqualTo(LocalDate.of(2021, MAY, 20));
                assertThat(absenceRecord.getMorning()).isPresent();
                assertThat(absenceRecord.getMorning().get()).satisfies(morning -> {
                    assertThat(morning.getAbsenceType()).isEqualTo(AbsencePeriod.AbsenceType.PUBLIC_HOLIDAY);
                    assertThat(morning.getId()).isEmpty();
                    assertThat(morning.getTypeId()).isEmpty();
                    assertThat(morning.getCategory()).isEmpty();
                    assertThat(morning.hasStatusAllowed()).isFalse();
                    assertThat(morning.hasStatusWaiting()).isFalse();
                    assertThat(morning.hasStatusTemporaryAllowed()).isFalse();
                    assertThat(morning.hasStatusAllowedCancellationRequested()).isFalse();
                    assertThat(morning.getStatus()).isEqualTo(AbsencePeriod.AbsenceStatus.ACTIVE);
                });
                assertThat(absenceRecord.getNoon()).isPresent();
                assertThat(absenceRecord.getNoon().get()).satisfies(noon -> {
                    assertThat(noon.getAbsenceType()).isEqualTo(AbsencePeriod.AbsenceType.PUBLIC_HOLIDAY);
                    assertThat(noon.getId()).isEmpty();
                    assertThat(noon.getTypeId()).isEmpty();
                    assertThat(noon.getCategory()).isEmpty();
                    assertThat(noon.hasStatusAllowed()).isFalse();
                    assertThat(noon.hasStatusWaiting()).isFalse();
                    assertThat(noon.hasStatusTemporaryAllowed()).isFalse();
                    assertThat(noon.hasStatusAllowedCancellationRequested()).isFalse();
                    assertThat(noon.getStatus()).isEqualTo(AbsencePeriod.AbsenceStatus.ACTIVE);
                });
            });
        });
    }

    @Test
    void ensureMultipleVacationDaysWithPublicHolidayNoon() {
        final LocalDate start = LocalDate.of(2021, DECEMBER, 1);
        final LocalDate end = LocalDate.of(2021, DECEMBER, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> {
            if (date.equals(LocalDate.of(2021, DECEMBER, 24))) {
                // half day "public holiday" at noon -> working in the morning
                return new WorkingDayInformation(MORNING, WORKDAY, PUBLIC_HOLIDAY);
            } else {
                return fullWorkDay();
            }
        });
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(LocalDate.of(2021, DECEMBER, 1));
        application.setEndDate(LocalDate.of(2021, DECEMBER, 31));
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(anyVacationType());

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);

        assertThat(actualAbsences).hasSize(2);

        assertThat(actualAbsences.get(0)).satisfies(absence -> {

            final List<AbsencePeriod.Record> absenceRecords = absence.absenceRecords();
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
            assertThat(absenceRecords.get(23).getMorning().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.VACATION);
            assertThat(absenceRecords.get(23).getNoon()).isEmpty();

            // 25. December to 31. December -> vacation
            IntStream.range(24, 30).forEach(index -> {
                assertThat(absenceRecords.get(index).getPerson()).isSameAs(batman);
                assertThat(absenceRecords.get(index).getDate()).isEqualTo(LocalDate.of(2021, DECEMBER, index + 1));
                assertThat(absenceRecords.get(index).getMorning()).isPresent();
                assertThat(absenceRecords.get(index).getNoon()).isPresent();
            });
        });

        // half public_holiday at 24th december
        assertThat(actualAbsences.get(1)).satisfies(absence -> {
            assertThat(absence.absenceRecords()).hasSize(1);
            assertThat(absence.absenceRecords().get(0)).satisfies(absenceRecord -> {
                assertThat(absenceRecord.isHalfDayAbsence()).isTrue();
                assertThat(absenceRecord.getDate()).isEqualTo(LocalDate.of(2021, DECEMBER, 24));
                assertThat(absenceRecord.getMorning()).isEmpty();
                assertThat(absenceRecord.getNoon()).isPresent();
                assertThat(absenceRecord.getNoon().get()).satisfies(noon -> {
                    assertThat(noon.getAbsenceType()).isEqualTo(AbsencePeriod.AbsenceType.PUBLIC_HOLIDAY);
                    assertThat(noon.getId()).isEmpty();
                    assertThat(noon.getTypeId()).isEmpty();
                    assertThat(noon.getCategory()).isEmpty();
                    assertThat(noon.hasStatusAllowed()).isFalse();
                    assertThat(noon.hasStatusWaiting()).isFalse();
                    assertThat(noon.hasStatusTemporaryAllowed()).isFalse();
                    assertThat(noon.hasStatusAllowedCancellationRequested()).isFalse();
                    assertThat(noon.getStatus()).isEqualTo(AbsencePeriod.AbsenceStatus.ACTIVE);
                });
            });
        });
    }

    @Test
    void ensureMultipleVacationDaysWithPublicHolidayMorning() {
        final LocalDate start = LocalDate.of(2021, DECEMBER, 1);
        final LocalDate end = LocalDate.of(2021, DECEMBER, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> {
            if (date.equals(LocalDate.of(2021, DECEMBER, 24))) {
                // half day "public holiday" in the morning -> working at noon
                return new WorkingDayInformation(NOON, PUBLIC_HOLIDAY, WORKDAY);
            } else {
                return fullWorkDay();
            }
        });
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(LocalDate.of(2021, DECEMBER, 1));
        application.setEndDate(LocalDate.of(2021, DECEMBER, 31));
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(anyVacationType());

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final List<AbsencePeriod> actualAbsences = sut.getOpenAbsences(List.of(batman), start, end);

        assertThat(actualAbsences).hasSize(2);

        assertThat(actualAbsences.get(0)).satisfies(absence -> {

            final List<AbsencePeriod.Record> absenceRecords = absence.absenceRecords();
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
            assertThat(absenceRecords.get(23).getNoon().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.VACATION);

            // 25. December to 31. December -> vacation
            IntStream.range(24, 30).forEach(index -> {
                assertThat(absenceRecords.get(index).getPerson()).isSameAs(batman);
                assertThat(absenceRecords.get(index).getDate()).isEqualTo(LocalDate.of(2021, DECEMBER, index + 1));
                assertThat(absenceRecords.get(index).getMorning()).isPresent();
                assertThat(absenceRecords.get(index).getNoon()).isPresent();
            });
        });

        // half public_holiday at 24th december
        assertThat(actualAbsences.get(1)).satisfies(absence -> {
            assertThat(absence.absenceRecords()).hasSize(1);
            assertThat(absence.absenceRecords().get(0)).satisfies(absenceRecord -> {
                assertThat(absenceRecord.isHalfDayAbsence()).isTrue();
                assertThat(absenceRecord.getDate()).isEqualTo(LocalDate.of(2021, DECEMBER, 24));
                assertThat(absenceRecord.getMorning()).isPresent();
                assertThat(absenceRecord.getMorning().get()).satisfies(morning -> {
                    assertThat(morning.getAbsenceType()).isEqualTo(AbsencePeriod.AbsenceType.PUBLIC_HOLIDAY);
                    assertThat(morning.getId()).isEmpty();
                    assertThat(morning.getTypeId()).isEmpty();
                    assertThat(morning.getCategory()).isEmpty();
                    assertThat(morning.hasStatusAllowed()).isFalse();
                    assertThat(morning.hasStatusWaiting()).isFalse();
                    assertThat(morning.hasStatusTemporaryAllowed()).isFalse();
                    assertThat(morning.hasStatusAllowedCancellationRequested()).isFalse();
                    assertThat(morning.getStatus()).isEqualTo(AbsencePeriod.AbsenceStatus.ACTIVE);
                });
                assertThat(absenceRecord.getNoon()).isEmpty();
            });
        });
    }

    @Test
    void ensureClosedAbsencesCallsApplicationServiceForPersonsAndDateInterval() {

        final Person batman = new Person();
        batman.setId(1L);

        final Person superman = new Person();
        superman.setId(2L);

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        sut.getClosedAbsences(List.of(batman, superman), start, end);

        verify(applicationService).getForStatesAndPerson(List.of(REVOKED, REJECTED, CANCELLED), List.of(batman, superman), start, end);
    }

    @Test
    void ensureClosedAbsencesCallsSickNotServiceForPersonsAndDateInterval() {

        final Person batman = new Person();
        batman.setId(1L);

        final Person superman = new Person();
        superman.setId(2L);

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        sut.getClosedAbsences(List.of(batman, superman), start, end);

        verify(sickNoteService).getForStatesAndPerson(List.of(CONVERTED_TO_VACATION, SickNoteStatus.CANCELLED), List.of(batman, superman), start, end);
    }

    @Test
    void ensureClosedAbsencesVacationMorning() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
                .id(1L)
                .category(VacationCategory.HOLIDAY)
                .visibleToEveryone(true)
                .build();

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(start.plusDays(1));
        application.setEndDate(start.plusDays(1));
        application.setDayLength(DayLength.MORNING);
        application.setStatus(CANCELLED);
        application.setVacationType(vacationType);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final List<AbsencePeriod> actualAbsences = sut.getClosedAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning()).isNotEmpty();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().flatMap(AbsencePeriod.RecordInfo::getId)).hasValue(42L);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getStatus)).hasValue(AbsencePeriod.AbsenceStatus.CANCELLED);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.VACATION);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getTypeId)).hasValue(Optional.of(1L));
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getCategory)).hasValue(Optional.of("HOLIDAY"));
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::isVisibleToEveryone)).hasValue(true);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon()).isEmpty();
    }

    @Test
    void ensureClosedAbsencesVacationNoon() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
                .id(1L)
                .category(VacationCategory.HOLIDAY)
                .visibleToEveryone(false)
                .build();

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(start.plusDays(1));
        application.setEndDate(start.plusDays(1));
        application.setDayLength(DayLength.NOON);
        application.setStatus(CANCELLED);
        application.setVacationType(vacationType);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final List<AbsencePeriod> actualAbsences = sut.getClosedAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning()).isEmpty();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon()).isNotEmpty();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().flatMap(AbsencePeriod.RecordInfo::getId)).hasValue(42L);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getStatus)).hasValue(AbsencePeriod.AbsenceStatus.CANCELLED);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.VACATION);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getTypeId)).hasValue(Optional.of(1L));
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getCategory)).hasValue(Optional.of("HOLIDAY"));
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::isVisibleToEveryone)).hasValue(false);
    }

    @Test
    void ensureClosedAbsencesVacationFull() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
                .id(1L)
                .category(VacationCategory.HOLIDAY)
                .visibleToEveryone(false)
                .build();

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(start.plusDays(1));
        application.setEndDate(start.plusDays(1));
        application.setDayLength(FULL);
        application.setStatus(CANCELLED);
        application.setVacationType(vacationType);

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final List<AbsencePeriod> actualAbsences = sut.getClosedAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning()).isPresent();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().flatMap(AbsencePeriod.RecordInfo::getId)).hasValue(42L);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getStatus)).hasValue(AbsencePeriod.AbsenceStatus.CANCELLED);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.VACATION);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getTypeId)).hasValue(Optional.of(1L));
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getCategory)).hasValue(Optional.of("HOLIDAY"));
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::isVisibleToEveryone)).hasValue(false);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon()).isPresent();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().flatMap(AbsencePeriod.RecordInfo::getId)).hasValue(42L);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getStatus)).hasValue(AbsencePeriod.AbsenceStatus.CANCELLED);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.VACATION);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getTypeId)).hasValue(Optional.of(1L));
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getCategory)).hasValue(Optional.of("HOLIDAY"));
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::isVisibleToEveryone)).hasValue(false);
    }

    @Test
    void ensureClosedAbsencesSickMorning() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final SickNote sickNote = SickNote.builder()
                .id(42L)
                .person(batman)
                .status(SickNoteStatus.CANCELLED)
                .sickNoteType(anySickNoteType())
                .startDate(start.plusDays(1))
                .endDate(start.plusDays(1))
                .dayLength(DayLength.MORNING)
                .build();

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));

        final List<AbsencePeriod> actualAbsences = sut.getClosedAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning()).isNotEmpty();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().flatMap(AbsencePeriod.RecordInfo::getId)).hasValue(42L);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getStatus)).hasValue(AbsencePeriod.AbsenceStatus.CANCELLED);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.SICK);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon()).isEmpty();
    }

    @Test
    void ensureClosedAbsencesSickNoon() {

        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final SickNote sickNote = SickNote.builder()
                .id(42L)
                .person(batman)
                .status(SickNoteStatus.CANCELLED)
                .sickNoteType(anySickNoteType())
                .startDate(start.plusDays(1))
                .endDate(start.plusDays(1))
                .dayLength(DayLength.NOON)
                .build();

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));

        final List<AbsencePeriod> actualAbsences = sut.getClosedAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning()).isEmpty();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon()).isNotEmpty();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().flatMap(AbsencePeriod.RecordInfo::getId)).hasValue(42L);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getStatus)).hasValue(AbsencePeriod.AbsenceStatus.CANCELLED);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.SICK);
    }

    @Test
    void ensureClosedAbsencesVacationMorningAndSickNoon() {
        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(start.plusDays(1));
        application.setEndDate(start.plusDays(1));
        application.setDayLength(DayLength.MORNING);
        application.setStatus(ALLOWED);
        application.setVacationType(anyVacationType());

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final SickNote sickNote = SickNote.builder()
            .id(1337L)
            .person(batman)
            .status(SickNoteStatus.CANCELLED)
                .sickNoteType(anySickNoteType())
            .startDate(start.plusDays(1))
            .endDate(start.plusDays(1))
            .dayLength(DayLength.NOON)
            .build();

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));

        final List<AbsencePeriod> actualAbsences = sut.getClosedAbsences(List.of(batman), start, end);
        assertThat(actualAbsences).hasSize(2);

        // vacation
        assertThat(actualAbsences.get(0).absenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning()).isNotEmpty();
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().flatMap(AbsencePeriod.RecordInfo::getId)).hasValue(42L);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getMorning().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.VACATION);
        assertThat(actualAbsences.get(0).absenceRecords().get(0).getNoon()).isEmpty();

        // sick
        assertThat(actualAbsences.get(1).absenceRecords()).hasSize(1);
        assertThat(actualAbsences.get(1).absenceRecords().get(0).getPerson()).isSameAs(batman);
        assertThat(actualAbsences.get(1).absenceRecords().get(0).getMorning()).isEmpty();
        assertThat(actualAbsences.get(1).absenceRecords().get(0).getNoon()).isNotEmpty();
        assertThat(actualAbsences.get(1).absenceRecords().get(0).getNoon().flatMap(AbsencePeriod.RecordInfo::getId)).hasValue(1337L);
        assertThat(actualAbsences.get(1).absenceRecords().get(0).getNoon().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.SICK);
    }

    @Test
    void ensureClosedAbsencesMultipleVacationDaysWithApplicationsOutsideTheAskedDateRange() {
        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(LocalDate.of(2021, MAY, 31));
        application.setEndDate(LocalDate.of(2021, JUNE, 10));
        application.setDayLength(FULL);
        application.setStatus(CANCELLED);
        application.setVacationType(anyVacationType());

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final List<AbsencePeriod> actualAbsences = sut.getClosedAbsences(List.of(batman), start, end);

        assertThat(actualAbsences).hasSize(1);

        final List<AbsencePeriod.Record> absenceRecords = actualAbsences.get(0).absenceRecords();
        assertThat(absenceRecords).hasSize(1);
        assertThat(absenceRecords.get(0).getPerson()).isSameAs(batman);
        assertThat(absenceRecords.get(0).getDate()).isEqualTo(LocalDate.of(2021, MAY, 31));
        assertThat(absenceRecords.get(0).getMorning()).isPresent();
        assertThat(absenceRecords.get(0).getNoon()).isPresent();
    }

    @Test
    void ensureClosedAbsencesMultipleSickDaysWithApplicationsOutsideTheAskedDateRange() {
        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> fullWorkDay());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final SickNote sickNote = SickNote.builder()
                .id(42L)
                .person(batman)
                .status(SickNoteStatus.CANCELLED)
                .sickNoteType(anySickNoteType())
                .startDate(LocalDate.of(2021, MAY, 31))
                .endDate(LocalDate.of(2021, JUNE, 10))
                .dayLength(FULL)
                .build();

        when(sickNoteService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(sickNote));

        final List<AbsencePeriod> actualAbsences = sut.getClosedAbsences(List.of(batman), start, end);

        assertThat(actualAbsences).hasSize(1);

        final List<AbsencePeriod.Record> absenceRecords = actualAbsences.get(0).absenceRecords();
        assertThat(absenceRecords).hasSize(1);
        assertThat(absenceRecords.get(0).getPerson()).isSameAs(batman);
        assertThat(absenceRecords.get(0).getDate()).isEqualTo(LocalDate.of(2021, MAY, 31));
        assertThat(absenceRecords.get(0).getMorning()).isPresent();
        assertThat(absenceRecords.get(0).getNoon()).isPresent();
    }

    @Test
    void ensureClosedAbsencesMultipleVacationDaysWithPublicHolidayFull() {
        final LocalDate start = LocalDate.of(2021, MAY, 1);
        final LocalDate end = LocalDate.of(2021, MAY, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> {
            if (date.equals(LocalDate.of(2021, MAY, 20))) {
                // public holiday -> no work 🎉
                return new WorkingDayInformation(ZERO, PUBLIC_HOLIDAY, PUBLIC_HOLIDAY);
            } else {
                return fullWorkDay();
            }
        });
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(LocalDate.of(2021, MAY, 1));
        application.setEndDate(LocalDate.of(2021, MAY, 31));
        application.setDayLength(FULL);
        application.setStatus(CANCELLED);
        application.setVacationType(anyVacationType());

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final List<AbsencePeriod> actualAbsences = sut.getClosedAbsences(List.of(batman), start, end);

        assertThat(actualAbsences).hasSize(2);

        assertThat(actualAbsences.get(0)).satisfies(absence -> {

            final List<AbsencePeriod.Record> absenceRecords = absence.absenceRecords();
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
        });

        // full public_holiday at 20th may
        assertThat(actualAbsences.get(1)).satisfies(absence -> {
            assertThat(absence.absenceRecords()).hasSize(1);
            assertThat(absence.absenceRecords().get(0)).satisfies(absenceRecord -> {
                assertThat(absenceRecord.isHalfDayAbsence()).isFalse();
                assertThat(absenceRecord.getDate()).isEqualTo(LocalDate.of(2021, MAY, 20));
                assertThat(absenceRecord.getMorning()).isPresent();
                assertThat(absenceRecord.getMorning().get()).satisfies(morning -> {
                    assertThat(morning.getAbsenceType()).isEqualTo(AbsencePeriod.AbsenceType.PUBLIC_HOLIDAY);
                    assertThat(morning.getId()).isEmpty();
                    assertThat(morning.getTypeId()).isEmpty();
                    assertThat(morning.getCategory()).isEmpty();
                    assertThat(morning.hasStatusAllowed()).isFalse();
                    assertThat(morning.hasStatusWaiting()).isFalse();
                    assertThat(morning.hasStatusTemporaryAllowed()).isFalse();
                    assertThat(morning.hasStatusAllowedCancellationRequested()).isFalse();
                    assertThat(morning.getStatus()).isEqualTo(AbsencePeriod.AbsenceStatus.ACTIVE);
                });
                assertThat(absenceRecord.getNoon()).isPresent();
                assertThat(absenceRecord.getNoon().get()).satisfies(noon -> {
                    assertThat(noon.getAbsenceType()).isEqualTo(AbsencePeriod.AbsenceType.PUBLIC_HOLIDAY);
                    assertThat(noon.getId()).isEmpty();
                    assertThat(noon.getTypeId()).isEmpty();
                    assertThat(noon.getCategory()).isEmpty();
                    assertThat(noon.hasStatusAllowed()).isFalse();
                    assertThat(noon.hasStatusWaiting()).isFalse();
                    assertThat(noon.hasStatusTemporaryAllowed()).isFalse();
                    assertThat(noon.hasStatusAllowedCancellationRequested()).isFalse();
                    assertThat(noon.getStatus()).isEqualTo(AbsencePeriod.AbsenceStatus.ACTIVE);
                });
            });
        });
    }

    @Test
    void ensureClosedAbsencesMultipleVacationDaysWithPublicHolidayNoon() {
        final LocalDate start = LocalDate.of(2021, DECEMBER, 1);
        final LocalDate end = LocalDate.of(2021, DECEMBER, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> {
            if (date.equals(LocalDate.of(2021, DECEMBER, 24))) {
                // half day "public holiday" at noon -> working in the morning
                return new WorkingDayInformation(MORNING, WORKDAY, PUBLIC_HOLIDAY);
            } else {
                return fullWorkDay();
            }
        });
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(LocalDate.of(2021, DECEMBER, 1));
        application.setEndDate(LocalDate.of(2021, DECEMBER, 31));
        application.setDayLength(FULL);
        application.setStatus(CANCELLED);
        application.setVacationType(anyVacationType());

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final List<AbsencePeriod> actualAbsences = sut.getClosedAbsences(List.of(batman), start, end);

        assertThat(actualAbsences).hasSize(2);

        assertThat(actualAbsences.get(0)).satisfies(absence -> {

            final List<AbsencePeriod.Record> absenceRecords = absence.absenceRecords();
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
            assertThat(absenceRecords.get(23).getMorning().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.VACATION);
            assertThat(absenceRecords.get(23).getNoon()).isEmpty();

            // 25. December to 31. December -> vacation
            IntStream.range(24, 30).forEach(index -> {
                assertThat(absenceRecords.get(index).getPerson()).isSameAs(batman);
                assertThat(absenceRecords.get(index).getDate()).isEqualTo(LocalDate.of(2021, DECEMBER, index + 1));
                assertThat(absenceRecords.get(index).getMorning()).isPresent();
                assertThat(absenceRecords.get(index).getNoon()).isPresent();
            });
        });

        // half public_holiday at 24th december
        assertThat(actualAbsences.get(1)).satisfies(absence -> {
            assertThat(absence.absenceRecords()).hasSize(1);
            assertThat(absence.absenceRecords().get(0)).satisfies(absenceRecord -> {
                assertThat(absenceRecord.isHalfDayAbsence()).isTrue();
                assertThat(absenceRecord.getDate()).isEqualTo(LocalDate.of(2021, DECEMBER, 24));
                assertThat(absenceRecord.getMorning()).isEmpty();
                assertThat(absenceRecord.getNoon()).isPresent();
                assertThat(absenceRecord.getNoon().get()).satisfies(noon -> {
                    assertThat(noon.getAbsenceType()).isEqualTo(AbsencePeriod.AbsenceType.PUBLIC_HOLIDAY);
                    assertThat(noon.getId()).isEmpty();
                    assertThat(noon.getTypeId()).isEmpty();
                    assertThat(noon.getCategory()).isEmpty();
                    assertThat(noon.hasStatusAllowed()).isFalse();
                    assertThat(noon.hasStatusWaiting()).isFalse();
                    assertThat(noon.hasStatusTemporaryAllowed()).isFalse();
                    assertThat(noon.hasStatusAllowedCancellationRequested()).isFalse();
                    assertThat(noon.getStatus()).isEqualTo(AbsencePeriod.AbsenceStatus.ACTIVE);
                });
            });
        });
    }

    @Test
    void ensureClosedAbsencesMultipleVacationDaysWithPublicHolidayMorning() {
        final LocalDate start = LocalDate.of(2021, DECEMBER, 1);
        final LocalDate end = LocalDate.of(2021, DECEMBER, 31);

        final Person batman = new Person();
        batman.setId(1L);

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(start, end, date -> {
            if (date.equals(LocalDate.of(2021, DECEMBER, 24))) {
                // half day "public holiday" in the morning -> working at noon
                return new WorkingDayInformation(NOON, PUBLIC_HOLIDAY, WORKDAY);
            } else {
                return fullWorkDay();
            }
        });
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(batman), new DateRange(start, end))).thenReturn(Map.of(batman, workingTimeCalendar));

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(batman);
        application.setStartDate(LocalDate.of(2021, DECEMBER, 1));
        application.setEndDate(LocalDate.of(2021, DECEMBER, 31));
        application.setDayLength(FULL);
        application.setStatus(CANCELLED);
        application.setVacationType(anyVacationType());

        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(application));

        final List<AbsencePeriod> actualAbsences = sut.getClosedAbsences(List.of(batman), start, end);

        assertThat(actualAbsences).hasSize(2);

        assertThat(actualAbsences.get(0)).satisfies(absence -> {

            final List<AbsencePeriod.Record> absenceRecords = absence.absenceRecords();
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
            assertThat(absenceRecords.get(23).getNoon().map(AbsencePeriod.RecordInfo::getAbsenceType)).hasValue(AbsencePeriod.AbsenceType.VACATION);

            // 25. December to 31. December -> vacation
            IntStream.range(24, 30).forEach(index -> {
                assertThat(absenceRecords.get(index).getPerson()).isSameAs(batman);
                assertThat(absenceRecords.get(index).getDate()).isEqualTo(LocalDate.of(2021, DECEMBER, index + 1));
                assertThat(absenceRecords.get(index).getMorning()).isPresent();
                assertThat(absenceRecords.get(index).getNoon()).isPresent();
            });
        });

        // half public_holiday at 24th december
        assertThat(actualAbsences.get(1)).satisfies(absence -> {
            assertThat(absence.absenceRecords()).hasSize(1);
            assertThat(absence.absenceRecords().get(0)).satisfies(absenceRecord -> {
                assertThat(absenceRecord.isHalfDayAbsence()).isTrue();
                assertThat(absenceRecord.getDate()).isEqualTo(LocalDate.of(2021, DECEMBER, 24));
                assertThat(absenceRecord.getMorning()).isPresent();
                assertThat(absenceRecord.getMorning().get()).satisfies(morning -> {
                    assertThat(morning.getAbsenceType()).isEqualTo(AbsencePeriod.AbsenceType.PUBLIC_HOLIDAY);
                    assertThat(morning.getId()).isEmpty();
                    assertThat(morning.getTypeId()).isEmpty();
                    assertThat(morning.getCategory()).isEmpty();
                    assertThat(morning.hasStatusAllowed()).isFalse();
                    assertThat(morning.hasStatusWaiting()).isFalse();
                    assertThat(morning.hasStatusTemporaryAllowed()).isFalse();
                    assertThat(morning.hasStatusAllowedCancellationRequested()).isFalse();
                    assertThat(morning.getStatus()).isEqualTo(AbsencePeriod.AbsenceStatus.ACTIVE);
                });
                assertThat(absenceRecord.getNoon()).isEmpty();
            });
        });
    }

    private static VacationType<?> anyVacationType() {
        return ProvidedVacationType.builder(new StaticMessageSource())
                .id(1L)
                .category(VacationCategory.HOLIDAY)
                .build();
    }

    private static SickNoteType anySickNoteType() {
        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);
        return sickNoteType;
    }

    private static WorkingDayInformation fullWorkDay() {
        return new WorkingDayInformation(FULL, WORKDAY, WORKDAY);
    }

    private Map<LocalDate, WorkingDayInformation> buildWorkingTimeByDate(LocalDate from, LocalDate to, Function<LocalDate, WorkingDayInformation> dayLengthProvider) {
        Map<LocalDate, WorkingDayInformation> map = new HashMap<>();
        for (LocalDate date : new DateRange(from, to)) {
            map.put(date, dayLengthProvider.apply(date));
        }
        return map;
    }
}
