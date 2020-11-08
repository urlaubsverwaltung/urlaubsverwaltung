package org.synyx.urlaubsverwaltung.application.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.TestDataCreator;
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
import java.sql.Time;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Consumer;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link ApplicationForLeaveFormValidator}.
 */
class ApplicationForLeaveFormValidatorTest {

    private ApplicationForLeaveFormValidator validator;

    private WorkingTimeService workingTimeService;
    private WorkDaysCountService workDaysCountService;
    private OverlapService overlapService;
    private CalculationService calculationService;
    private SettingsService settingsService;

    private Errors errors;
    private ApplicationForLeaveForm appForm;

    private Settings settings;
    private OvertimeService overtimeService;

    @BeforeEach
    void setUp() {

        settingsService = mock(SettingsService.class);
        settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        workDaysCountService = mock(WorkDaysCountService.class);
        overlapService = mock(OverlapService.class);
        calculationService = mock(CalculationService.class);
        workingTimeService = mock(WorkingTimeService.class);
        overtimeService = mock(OvertimeService.class);

        validator = new ApplicationForLeaveFormValidator(workingTimeService, workDaysCountService, overlapService, calculationService,
            settingsService, overtimeService, Clock.systemUTC());
        errors = mock(Errors.class);

        appForm = new ApplicationForLeaveForm();

        appForm.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(ZonedDateTime.now(UTC).plusDays(2).toLocalDate());
        appForm.setPerson(new Person("muster", "Muster", "Marlene", "muster@example.org"));

        // Default: everything is alright, override for negative cases
        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(Optional.of(TestDataCreator.createWorkingTime()));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class),
            any(Person.class))).thenReturn(BigDecimal.ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(OverlapCase.NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(Boolean.TRUE);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.TEN);
    }

    // Supports --------------------------------------------------------------------------------------------------------

    @Test
    void ensureSupportsAppFormClass() {

        assertThat(validator.supports(ApplicationForLeaveForm.class)).isTrue();
    }


    @Test
    void ensureDoesNotSupportNull() {

        assertThat(validator.supports(null)).isFalse();
    }


    @Test
    void ensureDoesNotSupportOtherClass() {

        assertThat(validator.supports(Person.class)).isFalse();
    }


    // Validate period (date) ------------------------------------------------------------------------------------------

    @Test
    void ensureStartDateIsMandatory() {

        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(null);

        validator.validate(appForm, errors);

        verify(errors).rejectValue("startDate", "error.entry.mandatory");
    }


    @Test
    void ensureEndDateIsMandatory() {

        appForm.setDayLength(DayLength.FULL);
        appForm.setEndDate(null);

        when(errors.hasErrors()).thenReturn(true);

        validator.validate(appForm, errors);

        verify(errors).rejectValue("endDate", "error.entry.mandatory");
    }


    @Test
    void ensureStartDateMustBeBeforeEndDate() {

        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(LocalDate.of(2012, 1, 17));
        appForm.setEndDate(LocalDate.of(2012, 1, 12));

        validator.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }


    @Test
    void ensureVeryPastDateIsNotValid() {

        LocalDate pastDate = ZonedDateTime.now(UTC).minusYears(10).toLocalDate();

        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(pastDate);
        appForm.setEndDate(pastDate.plusDays(1));

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.tooFarInThePast");
    }


    @Test
    void ensureVeryFutureDateIsNotValid() {

        LocalDate futureDate = ZonedDateTime.now(UTC).plusYears(10).toLocalDate();

        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(futureDate);
        appForm.setEndDate(futureDate.plusDays(1));

        validator.validate(appForm, errors);

        verify(errors)
            .reject("application.error.tooFarInTheFuture",
                new Object[]{settings.getAbsenceSettings().getMaximumMonthsToApplyForLeaveInAdvance()}, null);
    }


    @Test
    void ensureMorningApplicationForLeaveMustBeOnSameDate() {

        appForm.setDayLength(DayLength.MORNING);
        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(ZonedDateTime.now(UTC).plusDays(1).toLocalDate());

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.halfDayPeriod");
    }


    @Test
    void ensureNoonApplicationForLeaveMustBeOnSameDate() {

        appForm.setDayLength(DayLength.NOON);
        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(ZonedDateTime.now(UTC).plusDays(1).toLocalDate());

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.halfDayPeriod");
    }


    @Test
    void ensureSameDateAsStartAndEndDateIsValidForFullDayPeriod() {

        LocalDate date = LocalDate.now(UTC);

        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(date);
        appForm.setEndDate(date);

        validator.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
    }


    @Test
    void ensureSameDateAsStartAndEndDateIsValidForMorningPeriod() {

        LocalDate date = LocalDate.now(UTC);

        appForm.setDayLength(DayLength.MORNING);
        appForm.setStartDate(date);
        appForm.setEndDate(date);

        validator.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
    }


    @Test
    void ensureSameDateAsStartAndEndDateIsValidForNoonPeriod() {

        LocalDate date = LocalDate.now(UTC);

        appForm.setDayLength(DayLength.NOON);
        appForm.setStartDate(date);
        appForm.setEndDate(date);

        validator.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
    }


    // Validate period (time) ------------------------------------------------------------------------------------------

    @Test
    void ensureTimeIsNotMandatory() {

        appForm.setStartTime(null);
        appForm.setEndTime(null);

        validator.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
    }


    @Test
    void ensureProvidingStartTimeWithoutEndTimeIsInvalid() {

        appForm.setStartTime(Time.valueOf("09:15:00"));
        appForm.setEndTime(null);

        validator.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }


    @Test
    void ensureProvidingEndTimeWithoutStartTimeIsInvalid() {

        appForm.setStartTime(null);
        appForm.setEndTime(Time.valueOf("09:15:00"));

        validator.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }


    @Test
    void ensureStartTimeMustBeBeforeEndTime() {

        LocalDate date = LocalDate.now(UTC);

        appForm.setDayLength(DayLength.MORNING);
        appForm.setStartDate(date);
        appForm.setEndDate(date);
        appForm.setStartTime(Time.valueOf("13:30:00"));
        appForm.setEndTime(Time.valueOf("09:15:00"));

        validator.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }


    @Test
    void ensureStartTimeAndEndTimeMustNotBeEquals() {

        LocalDate date = LocalDate.now(UTC);
        Time time = Time.valueOf("13:30:00");

        appForm.setDayLength(DayLength.MORNING);
        appForm.setStartDate(date);
        appForm.setEndDate(date);
        appForm.setStartTime(time);
        appForm.setEndTime(time);

        validator.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }


    // Validate reason -------------------------------------------------------------------------------------------------

    @Test
    void ensureReasonIsNotMandatoryForHoliday() {

        VacationType vacationType = TestDataCreator.createVacationType(VacationCategory.HOLIDAY);

        appForm.setVacationType(vacationType);
        appForm.setReason("");

        validator.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(eq("reason"), anyString());
    }


    @Test
    void ensureReasonIsNotMandatoryForUnpaidLeave() {

        VacationType vacationType = TestDataCreator.createVacationType(VacationCategory.UNPAIDLEAVE);

        appForm.setVacationType(vacationType);
        appForm.setReason("");

        validator.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(eq("reason"), anyString());
    }


    @Test
    void ensureReasonIsNotMandatoryForOvertime() {

        VacationType vacationType = TestDataCreator.createVacationType(VacationCategory.OVERTIME);

        appForm.setVacationType(vacationType);
        appForm.setReason("");

        validator.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(eq("reason"), anyString());
    }


    @Test
    void ensureReasonIsMandatoryForSpecialLeave() {

        VacationType vacationType = TestDataCreator.createVacationType(VacationCategory.SPECIALLEAVE);

        appForm.setVacationType(vacationType);
        appForm.setReason("");

        validator.validate(appForm, errors);

        verify(errors).rejectValue("reason", "application.error.missingReasonForSpecialLeave");
    }


    // Validate address ------------------------------------------------------------------------------------------------

    @Test
    void ensureThereIsAMaximumCharLength() {

        appForm.setAddress(
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt"
                + " ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud "
                + "exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. ");

        validator.validate(appForm, errors);

        verify(errors).rejectValue("address", "error.entry.tooManyChars");
    }


    // Validate vacation days ------------------------------------------------------------------------------------------

    @Test
    void ensureApplicationForLeaveWithZeroVacationDaysIsNotValid() {

        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class),
            any(Person.class))).thenReturn(BigDecimal.ZERO);

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.zeroDays");

        verifyNoInteractions(overlapService);
        verifyNoInteractions(calculationService);
    }


    @Test
    void ensureApplyingForLeaveWithNotEnoughVacationDaysIsNotValid() {

        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(LocalDate.now(UTC));
        appForm.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));

        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class),
            any(Person.class))).thenReturn(BigDecimal.ONE);

        when(overlapService.checkOverlap(any(Application.class))).thenReturn(OverlapCase.NO_OVERLAPPING);

        when(calculationService.checkApplication(any(Application.class))).thenReturn(Boolean.FALSE);

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.notEnoughVacationDays");
    }


    @Test
    void ensureApplyingHalfDayForLeaveWithNotEnoughVacationDaysIsNotValid() {

        appForm.setDayLength(DayLength.NOON);
        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(LocalDate.now(UTC));
        appForm.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));

        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(workDaysCountService.getWorkDaysCount(eq(appForm.getDayLength()), eq(appForm.getStartDate()),
            eq(appForm.getEndDate()), eq(appForm.getPerson())))
            .thenReturn(BigDecimal.ONE);

        when(overlapService.checkOverlap(any(Application.class))).thenReturn(OverlapCase.NO_OVERLAPPING);

        when(calculationService.checkApplication(any(Application.class))).thenReturn(Boolean.FALSE);

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.notEnoughVacationDays");
    }


    // Validate overlapping --------------------------------------------------------------------------------------------

    @Test
    void ensureOverlappingApplicationForLeaveIsNotValid() {

        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class),
            any(Person.class))).thenReturn(BigDecimal.ONE);

        when(overlapService.checkOverlap(any(Application.class))).thenReturn(OverlapCase.FULLY_OVERLAPPING);

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.overlap");

        verifyNoInteractions(calculationService);
    }


    // Validate hours --------------------------------------------------------------------------------------------------

    @Test
    void ensureHoursIsMandatoryForOvertime() {

        appForm.setVacationType(TestDataCreator.createVacationType(VacationCategory.OVERTIME));
        appForm.setHours(null);

        validator.validate(appForm, errors);

        verify(errors).rejectValue("hours", "application.error.missingHoursForOvertime");
    }


    @Test
    void ensureHoursIsNotMandatoryForOtherTypesOfVacation() {

        Consumer<VacationType> assertHoursNotMandatory = (type) -> {
            appForm.setVacationType(type);
            appForm.setHours(null);

            validator.validate(appForm, errors);

            verify(errors, never()).rejectValue(eq("hours"), anyString());
        };

        VacationType holiday = TestDataCreator.createVacationType(VacationCategory.HOLIDAY);
        VacationType specialLeave = TestDataCreator.createVacationType(VacationCategory.SPECIALLEAVE);
        VacationType unpaidLeave = TestDataCreator.createVacationType(VacationCategory.UNPAIDLEAVE);

        assertHoursNotMandatory.accept(holiday);
        assertHoursNotMandatory.accept(specialLeave);
        assertHoursNotMandatory.accept(unpaidLeave);
    }


    @Test
    void ensureHoursIsNotMandatoryForOvertimeIfOvertimeFunctionIsDeactivated() {

        settings.getWorkingTimeSettings().setOvertimeActive(false);

        appForm.setVacationType(TestDataCreator.createVacationType(VacationCategory.OVERTIME));
        appForm.setHours(null);

        validator.validate(appForm, errors);

        verify(errors, never()).rejectValue(eq("hours"), anyString());
    }


    @Test
    void ensureHoursCanNotBeZero() {

        appForm.setHours(BigDecimal.ZERO);

        validator.validate(appForm, errors);

        verify(errors).rejectValue("hours", "application.error.invalidHoursForOvertime");
    }


    @Test
    void ensureHoursCanNotBeNegative() {

        appForm.setHours(BigDecimal.ONE.negate());

        validator.validate(appForm, errors);

        verify(errors).rejectValue("hours", "application.error.invalidHoursForOvertime");
    }


    @Test
    void ensureDecimalHoursAreValid() {

        appForm.setVacationType(TestDataCreator.createVacationType(VacationCategory.OVERTIME));
        appForm.setHours(new BigDecimal("0.5"));

        validator.validate(appForm, errors);

        verify(errors, never()).rejectValue(eq("hours"), anyString());
    }


    @Test
    void ensureNoErrorMessageForMandatoryIfHoursIsNullBecauseOfTypeMismatch() {

        settings.getWorkingTimeSettings().setOvertimeActive(true);

        appForm.setVacationType(TestDataCreator.createVacationType(VacationCategory.OVERTIME));
        appForm.setHours(null);

        when(errors.hasFieldErrors("hours")).thenReturn(true);

        validator.validate(appForm, errors);

        verify(errors).hasFieldErrors("hours");
        verify(errors, never()).rejectValue("hours", "application.error.missingHoursForOvertime");
    }


    // Validate working time exists ------------------------------------------------------------------------------------

    @Test
    void ensureWorkingTimeConfigurationMustExistForPeriodOfApplicationForLeave() {

        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class),
            eq(appForm.getStartDate()))).thenReturn(Optional.empty());

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.noValidWorkingTime");

        verify(workingTimeService)
            .getByPersonAndValidityDateEqualsOrMinorDate(appForm.getPerson(), appForm.getStartDate());
        verifyNoInteractions(workDaysCountService);
        verifyNoInteractions(overlapService);
        verifyNoInteractions(calculationService);
    }


    @Test
    void ensureWorkingTimeConfigurationMustExistForHalfDayApplicationForLeave() {

        // Yes, this can really happen...
        appForm.setStartDate(null);
        appForm.setEndDate(null);

        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(LocalDate.now(UTC));
        appForm.setDayLength(DayLength.MORNING);

        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class),
            eq(appForm.getStartDate())))
            .thenReturn(Optional.empty());

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.noValidWorkingTime");

        verify(workingTimeService)
            .getByPersonAndValidityDateEqualsOrMinorDate(appForm.getPerson(), appForm.getStartDate());
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

        BigDecimal overtimeReductionHours = new BigDecimal("6");
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

        BigDecimal overtimeReductionHours = new BigDecimal("5");
        overtimeMinimumTest(overtimeReductionHours);

        assertThat(errors.hasErrors()).isFalse();

        verify(overtimeService).getLeftOvertimeForPerson(appForm.getPerson());
    }

    @Test
    void ensureAlreadyAbsentOnChristmasEveMorning() {

        when(settingsService.getSettings())
            .thenReturn(createSettingsForChristmasEveWithAbsence(DayLength.MORNING));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(DayLength.MORNING)
            .startDate(LocalDate.of(2019, 12, 24))
            .endDate(LocalDate.of(2019, 12, 24))
            .build();

        final Errors errors = mock(Errors.class);

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.christmasEve.morning");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.christmasEve.morning");
    }

    @Test
    void ensureAlreadyAbsentOnChristmasEveMorningIsNotTriggered() {

        when(settingsService.getSettings())
            .thenReturn(createSettingsForChristmasEveWithAbsence(DayLength.ZERO));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(DayLength.MORNING)
            .startDate(LocalDate.of(2019, 12, 24))
            .endDate(LocalDate.of(2019, 12, 24))
            .build();

        final Errors errors = mock(Errors.class);

        validator.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(anyString(), anyString());
    }

    @Test
    void ensureAlreadyAbsentOnChristmasEveNoon() {

        when(settingsService.getSettings())
            .thenReturn(createSettingsForChristmasEveWithAbsence(DayLength.NOON));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(DayLength.NOON)
            .startDate(LocalDate.of(2019, 12, 24))
            .endDate(LocalDate.of(2019, 12, 24))
            .build();

        final Errors errors = mock(Errors.class);

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.christmasEve.noon");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.christmasEve.noon");
    }

    @Test
    void ensureAlreadyAbsentOnChristmasEveFullForDayLengthFullRequest() {
        ensureAlreadyAbsentOnChristmasEveForGivenDayLengthRequest(DayLength.FULL);
    }

    @Test
    void ensureAlreadyAbsentOnChristmasEveFullForDayLengthMorningRequest() {
        ensureAlreadyAbsentOnChristmasEveForGivenDayLengthRequest(DayLength.MORNING);
    }

    @Test
    void ensureAlreadyAbsentOnChristmasEveFullForDayLengthNoonRequest() {
        ensureAlreadyAbsentOnChristmasEveForGivenDayLengthRequest(DayLength.NOON);
    }

    private void ensureAlreadyAbsentOnChristmasEveForGivenDayLengthRequest(final DayLength dayLengthRequest) {

        when(settingsService.getSettings())
            .thenReturn(createSettingsForChristmasEveWithAbsence(DayLength.FULL));

        final LocalDate christmasEve = LocalDate.of(2019, 12, 24);
        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(dayLengthRequest)
            .startDate(christmasEve)
            .endDate(christmasEve)
            .build();

        final Errors errors = mock(Errors.class);

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.christmasEve.full");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.christmasEve.full");
    }

    @Test
    void ensureAlreadyAbsentOnNewYearsEveMorning() {

        when(settingsService.getSettings())
            .thenReturn(createSettingsForNewYearsEveWithAbsence(DayLength.MORNING));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(DayLength.MORNING)
            .startDate(LocalDate.of(2019, 12, 31))
            .endDate(LocalDate.of(2019, 12, 31))
            .build();

        final Errors errors = mock(Errors.class);

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.newYearsEve.morning");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.newYearsEve.morning");
    }

    @Test
    void ensureAlreadyAbsentOnNewYearsEveMorningIsNotTriggered() {

        when(settingsService.getSettings())
            .thenReturn(createSettingsForNewYearsEveWithAbsence(DayLength.ZERO));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(DayLength.MORNING)
            .startDate(LocalDate.of(2019, 12, 31))
            .endDate(LocalDate.of(2019, 12, 31))
            .build();

        final Errors errors = mock(Errors.class);

        validator.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(anyString(), anyString());
    }

    @Test
    void ensureAlreadyAbsentOnNewYearsEveNoon() {

        when(settingsService.getSettings())
            .thenReturn(createSettingsForNewYearsEveWithAbsence(DayLength.NOON));

        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(DayLength.NOON)
            .startDate(LocalDate.of(2019, 12, 31))
            .endDate(LocalDate.of(2019, 12, 31))
            .build();

        final Errors errors = mock(Errors.class);

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.alreadyAbsentOn.newYearsEve.noon");
        verify(errors).rejectValue("dayLength", "application.error.alreadyAbsentOn.newYearsEve.noon");
    }

    @Test
    void ensureAlreadyAbsentOnNewYearsEveFullForDayLengthFullRequest() {
        ensureAlreadyAbsentOnNewYearsEveForGivenDayLengthRequest(DayLength.FULL);
    }

    @Test
    void ensureAlreadyAbsentOnNewYearsEveFullForDayLengthMorningRequest() {
        ensureAlreadyAbsentOnNewYearsEveForGivenDayLengthRequest(DayLength.MORNING);
    }

    @Test
    void ensureAlreadyAbsentOnNewYearsEveFullForDayLengthNoonRequest() {
        ensureAlreadyAbsentOnNewYearsEveForGivenDayLengthRequest(DayLength.NOON);
    }

    private void ensureAlreadyAbsentOnNewYearsEveForGivenDayLengthRequest(final DayLength dayLengthRequest) {

        when(settingsService.getSettings())
            .thenReturn(createSettingsForNewYearsEveWithAbsence(DayLength.FULL));

        final LocalDate christmasEve = LocalDate.of(2019, 12, 31);
        final ApplicationForLeaveForm appForm = appFormBuilderWithDefaults()
            .dayLength(dayLengthRequest)
            .startDate(christmasEve)
            .endDate(christmasEve)
            .build();

        final Errors errors = mock(Errors.class);

        validator.validate(appForm, errors);

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
            .vacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
    }

    private void overtimeMinimumTest(BigDecimal hours) {

        VacationType vacationType = TestDataCreator.createVacationType(VacationCategory.OVERTIME);

        appForm.setHours(hours);
        appForm.setVacationType(vacationType);

        settings.getWorkingTimeSettings().setOvertimeActive(true);
        settings.getWorkingTimeSettings().setMinimumOvertime(5);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.ZERO);

        validator.validate(appForm, errors);
    }
}
