package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.overlap.OverlapCase;
import org.synyx.urlaubsverwaltung.overlap.OverlapService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createWorkingTime;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.UNPAIDLEAVE;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.NO_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveFormValidatorTest {

    private ApplicationForLeaveFormValidator sut;

    @Mock
    private WorkingTimeService workingTimeService;
    @Mock
    private WorkDaysCountService workDaysCountService;
    @Mock
    private OverlapService overlapService;
    @Mock
    private CalculationService calculationService;
    @Mock
    private OvertimeService overtimeService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private VacationTypeService vacationTypeService;
    @Mock
    private Errors errors;

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveFormValidator(workingTimeService, workDaysCountService, overlapService, calculationService,
            settingsService, overtimeService, vacationTypeService, new ApplicationMapper(vacationTypeService), Clock.systemUTC());
    }

    // Supports --------------------------------------------------------------------------------------------------------
    @Test
    void ensureSupportsAppFormClass() {
        assertThat(sut.supports(ApplicationForLeaveForm.class)).isTrue();
    }

    @Test
    void ensureDoesNotSupportNull() {
        assertThat(sut.supports(null)).isFalse();
    }

    @Test
    void ensureDoesNotSupportOtherClass() {
        assertThat(sut.supports(Person.class)).isFalse();
    }

    // Validate period (date) ------------------------------------------------------------------------------------------
    @Test
    void ensureStartDateIsMandatory() {

        setupOvertimeSettings();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .startDate(null)
            .build();

        sut.validate(appForm, errors);

        verify(errors).rejectValue("startDate", "error.entry.mandatory");
    }

    @Test
    void ensureEndDateIsMandatory() {

        setupOvertimeSettings();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .endDate(null)
            .build();

        when(errors.hasErrors()).thenReturn(true);

        sut.validate(appForm, errors);

        verify(errors).rejectValue("endDate", "error.entry.mandatory");
    }

    @Test
    void ensureStartDateMustBeBeforeEndDate() {

        setupOvertimeSettings();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .startDate(LocalDate.of(2012, 1, 17))
            .endDate(LocalDate.of(2012, 1, 12))
            .build();

        sut.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }

    @Test
    void ensureVeryPastDateIsNotValid() {

        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final Settings settings = setupOvertimeSettings();

        final LocalDate pastDate = LocalDate.now(UTC).minusYears(10);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .startDate(pastDate)
            .endDate(pastDate.plusDays(1))
            .build();

        sut.validate(appForm, errors);

        verify(errors)
            .reject("application.error.tooFarInThePast",
                new Object[]{settings.getApplicationSettings().getMaximumMonthsToApplyForLeaveAfterwards()}, null);
    }

    @Test
    void ensureVeryFutureDateIsNotValid() {

        final Settings settings = setupOvertimeSettings();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final LocalDate futureDate = LocalDate.now(UTC).plusYears(10);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .startDate(futureDate)
            .endDate(futureDate.plusDays(1))
            .build();

        sut.validate(appForm, errors);

        verify(errors)
            .reject("application.error.tooFarInTheFuture",
                new Object[]{settings.getApplicationSettings().getMaximumMonthsToApplyForLeaveInAdvance()}, null);
    }

    @Test
    void ensureMorningApplicationForLeaveMustBeOnSameDate() {

        setupOvertimeSettings();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(MORNING)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC).plusDays(1))
            .build();

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.halfDayPeriod");
    }

    @Test
    void ensureNoonApplicationForLeaveMustBeOnSameDate() {

        setupOvertimeSettings();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(NOON)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC).plusDays(1))
            .build();

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.halfDayPeriod");
    }

    @Test
    void ensureSameDateAsStartAndEndDateIsValidForFullDayPeriod() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final LocalDate date = LocalDate.now(UTC);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(FULL)
            .startDate(date)
            .endDate(date)
            .build();

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
    }

    @Test
    void ensureSameDateAsStartAndEndDateIsValidForMorningPeriod() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final LocalDate date = LocalDate.now(UTC);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(MORNING)
            .startDate(date)
            .endDate(date)
            .build();

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
    }

    @Test
    void ensureSameDateAsStartAndEndDateIsValidForNoonPeriod() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final LocalDate date = LocalDate.of(Year.now(UTC).getValue(), 10, 10);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(NOON)
            .startDate(date)
            .endDate(date)
            .build();

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
    }

    // Validate period (time) ------------------------------------------------------------------------------------------
    @Test
    void ensureTimeIsNotMandatory() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .startTime(null)
            .endTime(null)
            .build();

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
    }

    @Test
    void ensureProvidingStartTimeWithoutEndTimeIsInvalid() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .startTime(LocalTime.of(9, 15, 0))
            .endTime(null)
            .build();

        sut.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }

    @Test
    void ensureProvidingEndTimeWithoutStartTimeIsInvalid() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .startTime(null)
            .endTime(LocalTime.of(9, 15, 0))
            .build();

        sut.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }

    @Test
    void ensureStartTimeMustBeBeforeEndTime() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final LocalDate date = LocalDate.now(UTC);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(MORNING)
            .startDate(date)
            .endDate(date)
            .startTime(LocalTime.of(13, 30, 0))
            .endTime(LocalTime.of(9, 15, 0))
            .build();

        sut.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }

    @Test
    void ensureStartTimeAndEndTimeMustNotBeEqual() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final LocalDate date = LocalDate.now(UTC);
        final LocalTime time = LocalTime.of(13, 30, 0);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(MORNING)
            .startDate(date)
            .endDate(date)
            .startTime(time)
            .endTime(time)
            .build();

        sut.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }

    @Test
    void ensureHalfDayIsRejectedWhenDisabled() {

        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final var settings = setupOvertimeSettings();
        final var appSettings = new ApplicationSettings();
        appSettings.setAllowHalfDays(false);
        settings.setApplicationSettings(appSettings);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(MORNING)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .build();

        sut.validate(appForm, errors);

        verify(errors).rejectValue("dayLength", "application.error.halfDayPeriod.notAllowed");
    }

    // Validate reason -------------------------------------------------------------------------------------------------
    @Test
    void ensureReasonIsNotMandatoryForHoliday() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(HOLIDAY).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(HOLIDAY);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .vacationType(vacationTypeDto)
            .reason("")
            .build();

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(eq("reason"), anyString());
    }

    @Test
    void ensureReasonIsNotMandatoryForUnpaidLeave() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(UNPAIDLEAVE).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(UNPAIDLEAVE);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .vacationType(vacationTypeDto)
            .reason("")
            .build();

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(eq("reason"), anyString());
    }

    @Test
    void ensureReasonIsNotMandatoryForOvertime() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(OVERTIME).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(OVERTIME);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .vacationType(vacationTypeDto)
            .reason("")
            .build();

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(eq("reason"), anyString());
    }

    @Test
    void ensureReasonIsMandatoryForSpecialLeave() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(SPECIALLEAVE).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(SPECIALLEAVE);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .vacationType(vacationTypeDto)
            .reason("")
            .build();

        sut.validate(appForm, errors);

        verify(errors).rejectValue("reason", "application.error.missingReasonForSpecialLeave");
    }

    // Validate vacation days ------------------------------------------------------------------------------------------
    @Test
    void ensureApplicationForLeaveWithZeroVacationDaysIsNotValid() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class)))
            .thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class),
            any(Person.class))).thenReturn(BigDecimal.ZERO);
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults().build();

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.zeroDays");

        verifyNoInteractions(overlapService);
        verifyNoInteractions(calculationService);
    }

    @Test
    void ensureApplyingForLeaveWithNotEnoughVacationDaysIsNotValid() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(HOLIDAY).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(HOLIDAY);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .vacationType(vacationTypeDto)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .build();

        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(FALSE);

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.notEnoughVacationDays");
    }

    @Test
    void ensureApplyingHalfDayForLeaveWithNotEnoughVacationDaysIsNotValid() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(HOLIDAY).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(HOLIDAY);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(NOON)
            .vacationType(vacationTypeDto)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .build();

        when(workDaysCountService.getWorkDaysCount(appForm.getDayLength(), appForm.getStartDate(), appForm.getEndDate(), appForm.getPerson()))
            .thenReturn(ONE);

        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(FALSE);

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.notEnoughVacationDays");
    }

    // Validate overlapping --------------------------------------------------------------------------------------------
    @Test
    void ensureOverlappingApplicationForLeaveIsNotValid() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class),
            any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(OverlapCase.FULLY_OVERLAPPING);
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .build();

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.overlap");

        verifyNoInteractions(calculationService);
    }

    // Validate overtime reduction -------------------------------------------------------------------------------------
    @Test
    void ensureHoursIsMandatoryForOvertime() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(OVERTIME).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(OVERTIME);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .hoursAndMinutes(null)
            .vacationType(vacationTypeDto)
            .build();

        sut.validate(appForm, errors);

        verify(errors).rejectValue("hours", "application.error.missingHoursForOvertime");
    }

    @Test
    void ensureHoursIsNotMandatoryForOtherTypesOfVacation() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        when(vacationTypeService.getById(1L))
            .thenReturn(Optional.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).messageKey("message_key_1").category(HOLIDAY).build()));
        when(vacationTypeService.getById(2L))
            .thenReturn(Optional.of(ProvidedVacationType.builder(new StaticMessageSource()).id(2L).messageKey("message_key_2").category(SPECIALLEAVE).build()));
        when(vacationTypeService.getById(3L))
            .thenReturn(Optional.of(ProvidedVacationType.builder(new StaticMessageSource()).id(3L).messageKey("message_key_3").category(UNPAIDLEAVE).build()));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults().build();

        Consumer<ApplicationForLeaveFormVacationTypeDto> assertHoursNotMandatory = vacationTypeDto -> {
            appForm.setVacationType(vacationTypeDto);
            appForm.setHours(null);

            sut.validate(appForm, errors);

            verify(errors, never()).rejectValue(eq("hours"), anyString());
        };

        final ApplicationForLeaveFormVacationTypeDto holidayDto = new ApplicationForLeaveFormVacationTypeDto();
        holidayDto.setId(1L);
        holidayDto.setLabel("message_key_1");
        holidayDto.setCategory(HOLIDAY);

        final ApplicationForLeaveFormVacationTypeDto specialLeaveDto = new ApplicationForLeaveFormVacationTypeDto();
        specialLeaveDto.setId(2L);
        specialLeaveDto.setLabel("message_key_2");
        specialLeaveDto.setCategory(SPECIALLEAVE);

        final ApplicationForLeaveFormVacationTypeDto unpaidLeaveDto = new ApplicationForLeaveFormVacationTypeDto();
        unpaidLeaveDto.setId(3L);
        unpaidLeaveDto.setLabel("message_key_3");
        unpaidLeaveDto.setCategory(UNPAIDLEAVE);

        assertHoursNotMandatory.accept(holidayDto);
        assertHoursNotMandatory.accept(specialLeaveDto);
        assertHoursNotMandatory.accept(unpaidLeaveDto);
    }

    @Test
    void ensureHoursIsNotMandatoryForOvertimeIfOvertimeFunctionIsDeactivated() {

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(false);
        when(settingsService.getSettings()).thenReturn(settings);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(OVERTIME).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(OVERTIME);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .vacationType(vacationTypeDto)
            .hoursAndMinutes(null)
            .build();

        sut.validate(appForm, errors);

        verify(errors, never()).rejectValue(eq("hours"), anyString());
    }

    @Test
    void ensureHoursCanNotBeZeroIfNoMinutesAreSet() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(OVERTIME).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(OVERTIME);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .vacationType(vacationTypeDto)
            .build();

        appForm.setHours(BigDecimal.ZERO);
        appForm.setMinutes(null);

        sut.validate(appForm, errors);

        verify(errors).rejectValue("hours", "application.error.negativeOvertimeReduction");
    }

    @Test
    void ensureMinutesCanBeZeroIfHoursAreSet() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(OVERTIME).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(OVERTIME);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .vacationType(vacationTypeDto)
            .build();

        appForm.setHours(ONE);
        appForm.setMinutes(0);

        sut.validate(appForm, errors);

        verify(errors).hasErrors();
        verifyNoMoreInteractions(errors);
    }

    @ParameterizedTest
    @MethodSource("overtimeReductionInput")
    void ensureHoursEmptyIfMinutesAreSet(BigDecimal hours, Integer minutes) {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .build();

        appForm.setHours(hours);
        appForm.setMinutes(minutes);

        sut.validate(appForm, errors);

        verify(errors, never()).rejectValue(eq("hours"), anyString());
    }

    @Test
    void ensureOvertimeDurationFailsWhenItIsLowerThanTheConfiguredMinimum() {

        final Settings settings = setupOvertimeSettings();
        settings.getOvertimeSettings().setMinimumOvertimeReduction(4);

        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(OVERTIME).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(OVERTIME);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .vacationType(vacationTypeDto)
            .hoursAndMinutes(Duration.ofHours(3))
            .build();

        sut.validate(appForm, errors);

        verify(errors).rejectValue("hours", "overtime.error.minimumReductionRequired", new Object[]{4}, null);
        verify(errors).hasErrors();
        verifyNoMoreInteractions(errors);
    }

    @Test
    void ensureOvertimeDurationSucceedsWhenItIsEqualToTheConfiguredMinimum() {

        final Settings settings = setupOvertimeSettings();
        settings.getOvertimeSettings().setMinimumOvertimeReduction(4);

        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(OVERTIME).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(OVERTIME);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .vacationType(vacationTypeDto)
            .hoursAndMinutes(Duration.ofHours(4))
            .build();

        sut.validate(appForm, errors);

        verify(errors).hasErrors();
        verifyNoMoreInteractions(errors);
    }

    @Test
    void ensureHoursCanNotBeNegative() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(OVERTIME).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(OVERTIME);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .vacationType(vacationTypeDto)
            .build();
        appForm.setHours(ONE.negate());

        sut.validate(appForm, errors);

        verify(errors).rejectValue("hours", "application.error.negativeOvertimeReduction");
    }

    @Test
    void ensureMinutesCanNotBeNegative() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(OVERTIME).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(OVERTIME);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .vacationType(vacationTypeDto)
            .build();
        appForm.setMinutes(-1);

        sut.validate(appForm, errors);

        verify(errors).rejectValue("minutes", "application.error.negativeOvertimeReduction");
    }

    @Test
    void ensureDecimalHoursAreValid() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ofHours(10));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(OVERTIME).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(OVERTIME);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .vacationType(vacationTypeDto)
            .build();

        appForm.setHours(new BigDecimal("0.5").setScale(1, RoundingMode.HALF_UP));

        sut.validate(appForm, errors);

        verify(errors, never()).rejectValue(eq("hours"), anyString());
    }

    // Validate maximal overtime reduction -----------------------------------------------------------------------------

    @Test
    void ensureNoErrorMessageForMandatoryIfHoursIsNullBecauseOfTypeMismatch() {

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        setupOvertimeSettings();

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(OVERTIME).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(OVERTIME);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .vacationType(vacationTypeDto)
            .hoursAndMinutes(null)
            .build();


        when(errors.hasFieldErrors("hours")).thenReturn(true);

        sut.validate(appForm, errors);

        verify(errors).hasFieldErrors("hours");
        verify(errors, never()).rejectValue("hours", "application.error.missingHoursForOvertime");
    }

    // Validate working time exists ------------------------------------------------------------------------------------
    @Test
    void ensureWorkingTimeConfigurationMustExistForPeriodOfApplicationForLeave() {

        setupOvertimeSettings();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .build();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class),
            eq(appForm.getStartDate()))).thenReturn(Optional.empty());

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.noValidWorkingTime");

        verify(workingTimeService).getWorkingTime(appForm.getPerson(), appForm.getStartDate());
        verifyNoInteractions(workDaysCountService);
        verifyNoInteractions(overlapService);
        verifyNoInteractions(calculationService);
    }

    @Test
    void ensureWorkingTimeConfigurationMustExistForHalfDayApplicationForLeave() {

        setupOvertimeSettings();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(MORNING)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .build();

        when(errors.hasErrors()).thenReturn(FALSE);

        when(workingTimeService.getWorkingTime(any(Person.class),
            eq(appForm.getStartDate())))
            .thenReturn(Optional.empty());

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.noValidWorkingTime");

        verify(workingTimeService).getWorkingTime(appForm.getPerson(), appForm.getStartDate());
        verifyNoInteractions(workDaysCountService);
        verifyNoInteractions(overlapService);
        verifyNoInteractions(calculationService);
    }

    /**
     * User tries to make an application (overtime reduction) but has not enough overtime left and minimum overtime is
     * reached.
     *
     * <p>0h overtime - 6h (application) < -5h overtime minimum</p>
     */
    @Test
    void ensureErrorDueToMinimumOvertimeReached() {

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final ApplicationForLeaveForm appForm = overtimeMinimumTest(Duration.ofHours(6));

        verify(overtimeService).getLeftOvertimeForPerson(appForm.getPerson());
        verify(errors).reject("application.error.notEnoughOvertime");
    }

    /**
     * User tries to make an application (overtime reduction) but has not enough overtime left and minimum overtime is
     * reached exactly -> application is valid.
     *
     * <p>0h overtime - 5h (application) == -5h overtime minimum</p>
     */
    @Test
    void ensureNoErrorDueToExactMinimumOvertimeReached() {

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final ApplicationForLeaveForm appForm = overtimeMinimumTest(Duration.ofHours(5));
        assertThat(errors.hasErrors()).isFalse();

        verify(overtimeService).getLeftOvertimeForPerson(appForm.getPerson());
    }

    @Test
    void ensureOvertimeIsNotValidatedForApplicationForLeave() {
        setupOvertimeSettings();

        final Person person = new Person();
        person.setId(1L);

        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(HOLIDAY).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(HOLIDAY);

        final ApplicationForLeaveForm form = new ApplicationForLeaveForm();
        form.setPerson(person);
        form.setDayLength(FULL);
        form.setStartDate(LocalDate.now());
        form.setEndDate(LocalDate.now().plusDays(1));
        form.setVacationType(vacationTypeDto);
        form.setHours(null);
        form.setMinutes(null);

        sut.validate(form, errors);

        verify(errors).hasErrors();
        verifyNoMoreInteractions(errors);
    }

    @Test
    void ensureAlreadyAbsentOnChristmasEveMorning() {

        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(settingsService.getSettings()).thenReturn(createSettingsForChristmasEveWithAbsence(MORNING));
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final int actualYear = Year.now().getValue();

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(MORNING)
            .startDate(LocalDate.of(actualYear, 12, 24))
            .endDate(LocalDate.of(actualYear, 12, 24))
            .build();

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.christmasEve.morning");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.christmasEve.morning");
    }

    @Test
    void ensureAlreadyAbsentOnChristmasEveMorningIsNotTriggered() {

        setupOvertimeSettings();

        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final int actualYear = Year.now().getValue();

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(MORNING)
            .startDate(LocalDate.of(actualYear, 12, 24))
            .endDate(LocalDate.of(actualYear, 12, 24))
            .build();

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(anyString(), anyString());
    }

    @Test
    void ensureAlreadyAbsentOnChristmasEveNoon() {

        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(settingsService.getSettings()).thenReturn(createSettingsForChristmasEveWithAbsence(NOON));
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final int actualYear = Year.now().getValue();

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(NOON)
            .startDate(LocalDate.of(actualYear, 12, 24))
            .endDate(LocalDate.of(actualYear, 12, 24))
            .build();

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.christmasEve.noon");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.christmasEve.noon");
    }

    @ParameterizedTest
    @EnumSource(value = DayLength.class, names = {"FULL", "NOON", "MORNING"})
    void ensureAlreadyAbsentOnChristmasEveForGivenDayLengthRequest(final DayLength dayLength) {

        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(settingsService.getSettings()).thenReturn(createSettingsForChristmasEveWithAbsence(FULL));
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final int actualYear = Year.now().getValue();

        final LocalDate christmasEve = LocalDate.of(actualYear, 12, 24);
        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(dayLength)
            .startDate(christmasEve)
            .endDate(christmasEve)
            .build();

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.christmasEve.full");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.christmasEve.full");
    }

    @Test
    void ensureAlreadyAbsentOnNewYearsEveMorning() {

        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(settingsService.getSettings()).thenReturn(createSettingsForNewYearsEveWithAbsence(MORNING));
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final int actualYear = Year.now().getValue();

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(MORNING)
            .startDate(LocalDate.of(actualYear, 12, 31))
            .endDate(LocalDate.of(actualYear, 12, 31))
            .build();

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.newYearsEve.morning");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.newYearsEve.morning");
    }

    @Test
    void ensureAlreadyAbsentOnNewYearsEveMorningIsNotTriggered() {

        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(settingsService.getSettings()).thenReturn(createSettingsForNewYearsEveWithAbsence(DayLength.ZERO));
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final int actualYear = Year.now().getValue();

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(MORNING)
            .startDate(LocalDate.of(actualYear, 12, 31))
            .endDate(LocalDate.of(actualYear, 12, 31))
            .build();

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(anyString(), anyString());
    }

    @Test
    void ensureAlreadyAbsentOnNewYearsEveNoon() {

        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(settingsService.getSettings()).thenReturn(createSettingsForNewYearsEveWithAbsence(NOON));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final int actualYear = Year.now().getValue();

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(NOON)
            .startDate(LocalDate.of(actualYear, 12, 31))
            .endDate(LocalDate.of(actualYear, 12, 31))
            .build();

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.newYearsEve.noon");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.newYearsEve.noon");
    }

    @ParameterizedTest
    @EnumSource(value = DayLength.class, names = {"FULL", "NOON", "MORNING"})
    void ensureAlreadyAbsentOnNewYearsEveForGivenDayLengthRequest(final DayLength dayLength) {

        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(settingsService.getSettings()).thenReturn(createSettingsForNewYearsEveWithAbsence(FULL));
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType()));

        final int actualYear = Year.now().getValue();

        final LocalDate christmasEve = LocalDate.of(actualYear, 12, 31);
        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(dayLength)
            .startDate(christmasEve)
            .endDate(christmasEve)
            .build();

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.newYearsEve.full");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.newYearsEve.full");
    }

    private ApplicationForLeaveForm overtimeMinimumTest(Duration hours) {

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(OVERTIME).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(OVERTIME);

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .vacationType(vacationTypeDto)
            .hoursAndMinutes(hours)
            .build();

        final Settings settings = setupOvertimeSettings();
        settings.getOvertimeSettings().setMinimumOvertime(5);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ZERO);

        sut.validate(appForm, errors);

        return appForm;
    }

    private Settings setupOvertimeSettings() {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        return settings;
    }

    private VacationType<?> anyVacationType() {
        return ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(HOLIDAY).messageKey("message_key").build();
    }

    private static Stream<Arguments> overtimeReductionInput() {
        return Stream.of(
            Arguments.of(null, 1),
            Arguments.of(ZERO, 1)
        );
    }

    private static Settings createSettingsForChristmasEveWithAbsence(DayLength absence) {
        final Settings settings = new Settings();
        settings.getPublicHolidaysSettings().setWorkingDurationForChristmasEve(absence.getInverse());
        return settings;
    }

    private static Settings createSettingsForNewYearsEveWithAbsence(DayLength absence) {
        final Settings settings = new Settings();
        settings.getPublicHolidaysSettings().setWorkingDurationForNewYearsEve(absence.getInverse());
        return settings;
    }

    private static ApplicationForLeaveForm.Builder appFormBuilderWithDefaults() {

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(HOLIDAY);

        return new ApplicationForLeaveForm.Builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .vacationType(vacationTypeDto)
            .dayLength(FULL)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC).plusDays(2))
            .holidayReplacements(new ArrayList<>());
    }
}
