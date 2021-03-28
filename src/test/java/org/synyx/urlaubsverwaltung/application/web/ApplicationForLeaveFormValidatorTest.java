package org.synyx.urlaubsverwaltung.application.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.ApplicationSettings;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.CalculationService;
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
import java.sql.Time;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createWorkingTime;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.UNPAIDLEAVE;
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
    private Errors errors;

    private ApplicationForLeaveForm appForm;

    @BeforeEach
    void setUp() {

        sut = new ApplicationForLeaveFormValidator(workingTimeService, workDaysCountService, overlapService, calculationService,
            settingsService, overtimeService, Clock.systemUTC());

        appForm = new ApplicationForLeaveForm();
        appForm.setVacationType(createVacationType(HOLIDAY));
        appForm.setDayLength(FULL);
        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(LocalDate.now(UTC).plusDays(2));
        appForm.setPerson(new Person("muster", "Muster", "Marlene", "muster@example.org"));
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

        appForm.setDayLength(FULL);
        appForm.setStartDate(null);

        sut.validate(appForm, errors);

        verify(errors).rejectValue("startDate", "error.entry.mandatory");
    }

    @Test
    void ensureEndDateIsMandatory() {

        setupOvertimeSettings();

        appForm.setDayLength(FULL);
        appForm.setEndDate(null);

        when(errors.hasErrors()).thenReturn(true);

        sut.validate(appForm, errors);

        verify(errors).rejectValue("endDate", "error.entry.mandatory");
    }

    @Test
    void ensureStartDateMustBeBeforeEndDate() {

        setupOvertimeSettings();

        appForm.setDayLength(FULL);
        appForm.setStartDate(LocalDate.of(2012, 1, 17));
        appForm.setEndDate(LocalDate.of(2012, 1, 12));

        sut.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }

    @Test
    void ensureVeryPastDateIsNotValid() {

        setupOvertimeSettings();

        final LocalDate pastDate = LocalDate.now(UTC).minusYears(10);

        appForm.setDayLength(FULL);
        appForm.setStartDate(pastDate);
        appForm.setEndDate(pastDate.plusDays(1));

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.tooFarInThePast");
    }

    @Test
    void ensureVeryFutureDateIsNotValid() {

        final Settings settings = setupOvertimeSettings();

        final LocalDate futureDate = LocalDate.now(UTC).plusYears(10);

        appForm.setDayLength(FULL);
        appForm.setStartDate(futureDate);
        appForm.setEndDate(futureDate.plusDays(1));

        sut.validate(appForm, errors);

        verify(errors)
            .reject("application.error.tooFarInTheFuture",
                new Object[]{settings.getApplicationSettings().getMaximumMonthsToApplyForLeaveInAdvance()}, null);
    }

    @Test
    void ensureMorningApplicationForLeaveMustBeOnSameDate() {

        setupOvertimeSettings();

        appForm.setDayLength(MORNING);
        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(LocalDate.now(UTC).plusDays(1));

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.halfDayPeriod");
    }

    @Test
    void ensureNoonApplicationForLeaveMustBeOnSameDate() {

        setupOvertimeSettings();

        appForm.setDayLength(NOON);
        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(LocalDate.now(UTC).plusDays(1));

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.halfDayPeriod");
    }

    @Test
    void ensureSameDateAsStartAndEndDateIsValidForFullDayPeriod() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        final LocalDate date = LocalDate.now(UTC);

        appForm.setDayLength(FULL);
        appForm.setStartDate(date);
        appForm.setEndDate(date);

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
    }

    @Test
    void ensureSameDateAsStartAndEndDateIsValidForMorningPeriod() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        final LocalDate date = LocalDate.now(UTC);

        appForm.setDayLength(MORNING);
        appForm.setStartDate(date);
        appForm.setEndDate(date);

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
    }

    @Test
    void ensureSameDateAsStartAndEndDateIsValidForNoonPeriod() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        final LocalDate date = LocalDate.of(Year.now(UTC).getValue(), 10, 10);

        appForm.setDayLength(NOON);
        appForm.setStartDate(date);
        appForm.setEndDate(date);

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
    }

    // Validate period (time) ------------------------------------------------------------------------------------------
    @Test
    void ensureTimeIsNotMandatory() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        appForm.setStartTime(null);
        appForm.setEndTime(null);

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
    }

    @Test
    void ensureProvidingStartTimeWithoutEndTimeIsInvalid() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        appForm.setStartTime(Time.valueOf("09:15:00"));
        appForm.setEndTime(null);

        sut.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }

    @Test
    void ensureProvidingEndTimeWithoutStartTimeIsInvalid() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        appForm.setStartTime(null);
        appForm.setEndTime(Time.valueOf("09:15:00"));

        sut.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }

    @Test
    void ensureStartTimeMustBeBeforeEndTime() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        final LocalDate date = LocalDate.now(UTC);

        appForm.setDayLength(MORNING);
        appForm.setStartDate(date);
        appForm.setEndDate(date);
        appForm.setStartTime(Time.valueOf("13:30:00"));
        appForm.setEndTime(Time.valueOf("09:15:00"));

        sut.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }

    @Test
    void ensureStartTimeAndEndTimeMustNotBeEquals() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        final LocalDate date = LocalDate.now(UTC);
        final Time time = Time.valueOf("13:30:00");

        appForm.setDayLength(MORNING);
        appForm.setStartDate(date);
        appForm.setEndDate(date);
        appForm.setStartTime(time);
        appForm.setEndTime(time);

        sut.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }


    @Test
    void ensureHalfDayIsRejectedWhenDisabled() {

        final var settings = setupOvertimeSettings();
        final var appSettings = new ApplicationSettings();
        appSettings.setAllowHalfDays(false);
        settings.setApplicationSettings(appSettings);

        appForm.setDayLength(MORNING);
        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(appForm.getStartDate());

        sut.validate(appForm, errors);

        verify(errors).rejectValue("dayLength", "application.error.halfDayPeriod.notAllowed");
    }


    // Validate reason -------------------------------------------------------------------------------------------------
    @Test
    void ensureReasonIsNotMandatoryForHoliday() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        final VacationType vacationType = createVacationType(HOLIDAY);

        appForm.setVacationType(vacationType);
        appForm.setReason("");

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(eq("reason"), anyString());
    }

    @Test
    void ensureReasonIsNotMandatoryForUnpaidLeave() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final VacationType vacationType = createVacationType(UNPAIDLEAVE);

        appForm.setVacationType(vacationType);
        appForm.setReason("");

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(eq("reason"), anyString());
    }

    @Test
    void ensureReasonIsNotMandatoryForOvertime() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final VacationType vacationType = createVacationType(OVERTIME);

        appForm.setVacationType(vacationType);
        appForm.setReason("");

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(eq("reason"), anyString());
    }

    @Test
    void ensureReasonIsMandatoryForSpecialLeave() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final VacationType vacationType = createVacationType(SPECIALLEAVE);

        appForm.setVacationType(vacationType);
        appForm.setReason("");

        sut.validate(appForm, errors);

        verify(errors).rejectValue("reason", "application.error.missingReasonForSpecialLeave");
    }

    // Validate address ------------------------------------------------------------------------------------------------
    @Test
    void ensureThereIsAMaximumCharLength() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        appForm.setAddress(
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt"
                + " ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud "
                + "exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. ");

        sut.validate(appForm, errors);

        verify(errors).rejectValue("address", "error.entry.tooManyChars");
    }

    // Validate vacation days ------------------------------------------------------------------------------------------
    @Test
    void ensureApplicationForLeaveWithZeroVacationDaysIsNotValid() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class),
            any(Person.class))).thenReturn(BigDecimal.ZERO);

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.zeroDays");

        verifyNoInteractions(overlapService);
        verifyNoInteractions(calculationService);
    }

    @Test
    void ensureApplyingForLeaveWithNotEnoughVacationDaysIsNotValid() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        appForm.setDayLength(FULL);
        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(LocalDate.now(UTC));
        appForm.setVacationType(createVacationType(HOLIDAY));

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
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        appForm.setDayLength(NOON);
        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(LocalDate.now(UTC));
        appForm.setVacationType(createVacationType(HOLIDAY));

        when(workDaysCountService.getWorkDaysCount(eq(appForm.getDayLength()), eq(appForm.getStartDate()),
            eq(appForm.getEndDate()), eq(appForm.getPerson())))
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
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class),
            any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(OverlapCase.FULLY_OVERLAPPING);

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.overlap");

        verifyNoInteractions(calculationService);
    }

    // Validate overtime reduction -------------------------------------------------------------------------------------
    @Test
    void ensureHoursIsMandatoryForOvertime() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        appForm.setVacationType(createVacationType(OVERTIME));
        appForm.setHours(null);

        sut.validate(appForm, errors);

        verify(errors).rejectValue("hours", "application.error.missingHoursForOvertime");
    }

    @Test
    void ensureHoursIsNotMandatoryForOtherTypesOfVacation() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        Consumer<VacationType> assertHoursNotMandatory = (type) -> {
            appForm.setVacationType(type);
            appForm.setHours(null);

            sut.validate(appForm, errors);

            verify(errors, never()).rejectValue(eq("hours"), anyString());
        };

        VacationType holiday = createVacationType(HOLIDAY);
        VacationType specialLeave = createVacationType(SPECIALLEAVE);
        VacationType unpaidLeave = createVacationType(UNPAIDLEAVE);

        assertHoursNotMandatory.accept(holiday);
        assertHoursNotMandatory.accept(specialLeave);
        assertHoursNotMandatory.accept(unpaidLeave);
    }

    @Test
    void ensureHoursIsNotMandatoryForOvertimeIfOvertimeFunctionIsDeactivated() {

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(false);
        when(settingsService.getSettings()).thenReturn(settings);

        appForm.setVacationType(createVacationType(OVERTIME));
        appForm.setHours(null);

        sut.validate(appForm, errors);

        verify(errors, never()).rejectValue(eq("hours"), anyString());
    }

    @Test
    void ensureHoursCanNotBeZeroIfNoMinutesAreSet() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final VacationType overtimeVacationType = new VacationType();
        overtimeVacationType.setCategory(OVERTIME);

        appForm.setVacationType(overtimeVacationType);
        appForm.setHours(BigDecimal.ZERO);
        appForm.setMinutes(null);

        sut.validate(appForm, errors);

        verify(errors).rejectValue("hours", "application.error.negativeOvertimeReduction");
    }

    private static Stream<Arguments> overtimeReductionInput() {
        return Stream.of(
            Arguments.of(null, 1),
            Arguments.of(ZERO, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("overtimeReductionInput")
    void ensureHoursEmptyIfMinutesAreSet(BigDecimal hours, Integer minutes) {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        appForm.setHours(hours);
        appForm.setMinutes(minutes);

        sut.validate(appForm, errors);

        verify(errors, never()).rejectValue(eq("hours"), anyString());
    }

    @Test
    void ensureHoursCanNotBeNegative() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final VacationType overtimeVacationType = new VacationType();
        overtimeVacationType.setCategory(OVERTIME);

        appForm.setVacationType(overtimeVacationType);
        appForm.setHours(ONE.negate());

        sut.validate(appForm, errors);

        verify(errors).rejectValue("hours", "application.error.negativeOvertimeReduction");
    }

    @Test
    void ensureMinutesCanNotBeNegative() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final VacationType overtimeVacationType = new VacationType();
        overtimeVacationType.setCategory(OVERTIME);

        appForm.setVacationType(overtimeVacationType);
        appForm.setMinutes(-1);

        sut.validate(appForm, errors);

        verify(errors).rejectValue("minutes", "application.error.negativeOvertimeReduction");
    }

    @Test
    void ensureDecimalHoursAreValid() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ofHours(10));

        appForm.setVacationType(createVacationType(OVERTIME));
        appForm.setHours(new BigDecimal("0.5").setScale(1, RoundingMode.HALF_UP));

        sut.validate(appForm, errors);

        verify(errors, never()).rejectValue(eq("hours"), anyString());
    }

    @Test
    void ensureNoErrorMessageForMandatoryIfHoursIsNullBecauseOfTypeMismatch() {

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        setupOvertimeSettings();

        appForm.setVacationType(createVacationType(OVERTIME));
        appForm.setHours(null);

        when(errors.hasFieldErrors("hours")).thenReturn(true);

        sut.validate(appForm, errors);

        verify(errors).hasFieldErrors("hours");
        verify(errors, never()).rejectValue("hours", "application.error.missingHoursForOvertime");
    }

    // Validate working time exists ------------------------------------------------------------------------------------
    @Test
    void ensureWorkingTimeConfigurationMustExistForPeriodOfApplicationForLeave() {

        setupOvertimeSettings();

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class),
            eq(appForm.getStartDate()))).thenReturn(Optional.empty());

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.noValidWorkingTime");

        verify(workingTimeService).getByPersonAndValidityDateEqualsOrMinorDate(appForm.getPerson(), appForm.getStartDate());
        verifyNoInteractions(workDaysCountService);
        verifyNoInteractions(overlapService);
        verifyNoInteractions(calculationService);
    }

    @Test
    void ensureWorkingTimeConfigurationMustExistForHalfDayApplicationForLeave() {

        setupOvertimeSettings();

        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(LocalDate.now(UTC));
        appForm.setDayLength(MORNING);

        when(errors.hasErrors()).thenReturn(FALSE);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class),
            eq(appForm.getStartDate())))
            .thenReturn(Optional.empty());

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.noValidWorkingTime");

        verify(workingTimeService).getByPersonAndValidityDateEqualsOrMinorDate(appForm.getPerson(), appForm.getStartDate());
        verifyNoInteractions(workDaysCountService);
        verifyNoInteractions(overlapService);
        verifyNoInteractions(calculationService);
    }

    // Validate maximal overtime reduction -----------------------------------------------------------------------------

    /**
     * User tries to make an application (overtime reduction) but has not enough overtime left and minimum overtime is
     * reached.
     *
     * <p>0h overtime - 6h (application) < -5h overtime minimum</p>
     */
    @Test
    void ensureErrorDueToMinimumOvertimeReached() {

        when(errors.hasErrors()).thenReturn(FALSE);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final BigDecimal overtimeReductionHours = new BigDecimal("6");
        overtimeMinimumTest(overtimeReductionHours);
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
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);

        final BigDecimal overtimeReductionHours = new BigDecimal("5");
        overtimeMinimumTest(overtimeReductionHours);

        assertThat(errors.hasErrors()).isFalse();

        verify(overtimeService).getLeftOvertimeForPerson(appForm.getPerson());
    }

    @Test
    void ensureOvertimeIsNotValidatedForApplicationForLeave() {
        setupOvertimeSettings();

        final Person person = new Person();
        person.setId(1);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        final VacationType holidayVacationType = new VacationType();
        holidayVacationType.setCategory(HOLIDAY);

        final ApplicationForLeaveForm form = new ApplicationForLeaveForm();
        form.setPerson(person);
        form.setDayLength(FULL);
        form.setStartDate(LocalDate.now());
        form.setEndDate(LocalDate.now().plusDays(1));
        form.setVacationType(holidayVacationType);
        form.setHours(null);
        form.setMinutes(null);

        sut.validate(form, errors);

        verify(errors).hasErrors();
        verifyNoMoreInteractions(errors);
    }

    @Test
    void ensureAlreadyAbsentOnChristmasEveMorning() {

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(settingsService.getSettings()).thenReturn(createSettingsForChristmasEveWithAbsence(MORNING));

        final int actualYear = Year.now().getValue();

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(MORNING)
            .startDate(LocalDate.of(actualYear, 12, 24))
            .endDate(LocalDate.of(actualYear, 12, 24))
            .build();

        final Errors errors = mock(Errors.class);

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.christmasEve.morning");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.christmasEve.morning");
    }

    @Test
    void ensureAlreadyAbsentOnChristmasEveMorningIsNotTriggered() {

        setupOvertimeSettings();

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);

        final int actualYear = Year.now().getValue();

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(MORNING)
            .startDate(LocalDate.of(actualYear, 12, 24))
            .endDate(LocalDate.of(actualYear, 12, 24))
            .build();

        final Errors errors = mock(Errors.class);

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(anyString(), anyString());
    }

    @Test
    void ensureAlreadyAbsentOnChristmasEveNoon() {

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(settingsService.getSettings()).thenReturn(createSettingsForChristmasEveWithAbsence(NOON));

        final int actualYear = Year.now().getValue();

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(NOON)
            .startDate(LocalDate.of(actualYear, 12, 24))
            .endDate(LocalDate.of(actualYear, 12, 24))
            .build();

        final Errors errors = mock(Errors.class);

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.christmasEve.noon");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.christmasEve.noon");
    }

    @ParameterizedTest
    @EnumSource(names = {"FULL", "NOON", "MORNING"})
    void ensureAlreadyAbsentOnChristmasEveForGivenDayLengthRequest(final DayLength dayLength) {

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(settingsService.getSettings()).thenReturn(createSettingsForChristmasEveWithAbsence(FULL));

        final int actualYear = Year.now().getValue();

        final LocalDate christmasEve = LocalDate.of(actualYear, 12, 24);
        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(dayLength)
            .startDate(christmasEve)
            .endDate(christmasEve)
            .build();

        final Errors errors = mock(Errors.class);

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.christmasEve.full");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.christmasEve.full");
    }

    @Test
    void ensureAlreadyAbsentOnNewYearsEveMorning() {

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(settingsService.getSettings()).thenReturn(createSettingsForNewYearsEveWithAbsence(MORNING));

        final int actualYear = Year.now().getValue();

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(MORNING)
            .startDate(LocalDate.of(actualYear, 12, 31))
            .endDate(LocalDate.of(actualYear, 12, 31))
            .build();

        final Errors errors = mock(Errors.class);

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.newYearsEve.morning");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.newYearsEve.morning");
    }

    @Test
    void ensureAlreadyAbsentOnNewYearsEveMorningIsNotTriggered() {

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(settingsService.getSettings()).thenReturn(createSettingsForNewYearsEveWithAbsence(DayLength.ZERO));

        final int actualYear = Year.now().getValue();

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(MORNING)
            .startDate(LocalDate.of(actualYear, 12, 31))
            .endDate(LocalDate.of(actualYear, 12, 31))
            .build();

        final Errors errors = mock(Errors.class);

        sut.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(anyString(), anyString());
    }

    @Test
    void ensureAlreadyAbsentOnNewYearsEveNoon() {

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(settingsService.getSettings()).thenReturn(createSettingsForNewYearsEveWithAbsence(NOON));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);

        final int actualYear = Year.now().getValue();

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(NOON)
            .startDate(LocalDate.of(actualYear, 12, 31))
            .endDate(LocalDate.of(actualYear, 12, 31))
            .build();

        final Errors errors = mock(Errors.class);

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.newYearsEve.noon");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.newYearsEve.noon");
    }

    @ParameterizedTest
    @EnumSource(names = {"FULL", "NOON", "MORNING"})
    void ensureAlreadyAbsentOnNewYearsEveForGivenDayLengthRequest(final DayLength dayLength) {

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(TRUE);
        when(settingsService.getSettings()).thenReturn(createSettingsForNewYearsEveWithAbsence(FULL));

        final int actualYear = Year.now().getValue();

        final LocalDate christmasEve = LocalDate.of(actualYear, 12, 31);
        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(dayLength)
            .startDate(christmasEve)
            .endDate(christmasEve)
            .build();

        final Errors errors = mock(Errors.class);

        sut.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.newYearsEve.full");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.newYearsEve.full");
    }

    private static Settings createSettingsForChristmasEveWithAbsence(DayLength absence) {
        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(absence.getInverse());
        return settings;
    }

    private static Settings createSettingsForNewYearsEveWithAbsence(DayLength absence) {
        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(absence.getInverse());
        return settings;
    }

    private static ApplicationForLeaveForm.Builder appFormBuilderWithDefaults() {
        return new ApplicationForLeaveForm.Builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .vacationType(createVacationType(HOLIDAY));
    }

    private void overtimeMinimumTest(BigDecimal hours) {

        final VacationType vacationType = createVacationType(OVERTIME);

        appForm.setHours(hours);
        appForm.setVacationType(vacationType);

        final Settings settings = setupOvertimeSettings();
        settings.getOvertimeSettings().setMinimumOvertime(5);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ZERO);

        sut.validate(appForm, errors);
    }

    private Settings setupOvertimeSettings() {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        return settings;
    }
}
