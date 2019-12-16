package org.synyx.urlaubsverwaltung.application.web;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;
import org.synyx.urlaubsverwaltung.workingtime.OverlapCase;
import org.synyx.urlaubsverwaltung.workingtime.OverlapService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Consumer;

import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link ApplicationForLeaveFormValidator}.
 */
public class ApplicationForLeaveFormValidatorTest {

    private ApplicationForLeaveFormValidator validator;

    private WorkingTimeService workingTimeService;
    private WorkDaysService calendarService;
    private OverlapService overlapService;
    private CalculationService calculationService;
    private SettingsService settingsService;

    private Errors errors;
    private ApplicationForLeaveForm appForm;

    private Settings settings;
    private OvertimeService overtimeService;

    @Before
    public void setUp() {

        settingsService = mock(SettingsService.class);
        settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        calendarService = mock(WorkDaysService.class);
        overlapService = mock(OverlapService.class);
        calculationService = mock(CalculationService.class);
        workingTimeService = mock(WorkingTimeService.class);
        overtimeService = mock(OvertimeService.class);

        validator = new ApplicationForLeaveFormValidator(workingTimeService, calendarService, overlapService, calculationService,
            settingsService, overtimeService);
        errors = mock(Errors.class);

        appForm = new ApplicationForLeaveForm();

        appForm.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(ZonedDateTime.now(UTC).plusDays(2).toLocalDate());
        appForm.setPerson(TestDataCreator.createPerson());

        // Default: everything is alright, override for negative cases
        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(Optional.of(TestDataCreator.createWorkingTime()));
        when(calendarService.getWorkDays(any(DayLength.class), any(LocalDate.class), any(LocalDate.class),
            any(Person.class))).thenReturn(BigDecimal.ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(OverlapCase.NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(Boolean.TRUE);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.TEN);
    }

    // Supports --------------------------------------------------------------------------------------------------------

    @Test
    public void ensureSupportsAppFormClass() {

        assertTrue(validator.supports(ApplicationForLeaveForm.class));
    }


    @Test
    public void ensureDoesNotSupportNull() {

        assertFalse(validator.supports(null));
    }


    @Test
    public void ensureDoesNotSupportOtherClass() {

        assertFalse(validator.supports(Person.class));
    }


    // Validate period (date) ------------------------------------------------------------------------------------------

    @Test
    public void ensureStartDateIsMandatory() {

        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(null);

        validator.validate(appForm, errors);

        verify(errors).rejectValue("startDate", "error.entry.mandatory");
    }


    @Test
    public void ensureEndDateIsMandatory() {

        appForm.setDayLength(DayLength.FULL);
        appForm.setEndDate(null);

        when(errors.hasErrors()).thenReturn(true);

        validator.validate(appForm, errors);

        verify(errors).rejectValue("endDate", "error.entry.mandatory");
    }


    @Test
    public void ensureStartDateMustBeBeforeEndDate() {

        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(LocalDate.of(2012, 1, 17));
        appForm.setEndDate(LocalDate.of(2012, 1, 12));

        validator.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }


    @Test
    public void ensureVeryPastDateIsNotValid() {

        LocalDate pastDate = ZonedDateTime.now(UTC).minusYears(10).toLocalDate();

        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(pastDate);
        appForm.setEndDate(pastDate.plusDays(1));

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.tooFarInThePast");
    }


    @Test
    public void ensureVeryFutureDateIsNotValid() {

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
    public void ensureMorningApplicationForLeaveMustBeOnSameDate() {

        appForm.setDayLength(DayLength.MORNING);
        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(ZonedDateTime.now(UTC).plusDays(1).toLocalDate());

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.halfDayPeriod");
    }


    @Test
    public void ensureNoonApplicationForLeaveMustBeOnSameDate() {

        appForm.setDayLength(DayLength.NOON);
        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(ZonedDateTime.now(UTC).plusDays(1).toLocalDate());

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.halfDayPeriod");
    }


    @Test
    public void ensureSameDateAsStartAndEndDateIsValidForFullDayPeriod() {

        LocalDate date = LocalDate.now(UTC);

        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(date);
        appForm.setEndDate(date);

        validator.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
    }


    @Test
    public void ensureSameDateAsStartAndEndDateIsValidForMorningPeriod() {

        LocalDate date = LocalDate.now(UTC);

        appForm.setDayLength(DayLength.MORNING);
        appForm.setStartDate(date);
        appForm.setEndDate(date);

        validator.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
    }


    @Test
    public void ensureSameDateAsStartAndEndDateIsValidForNoonPeriod() {

        LocalDate date = LocalDate.now(UTC);

        appForm.setDayLength(DayLength.NOON);
        appForm.setStartDate(date);
        appForm.setEndDate(date);

        validator.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
    }


    // Validate period (time) ------------------------------------------------------------------------------------------

    @Test
    public void ensureTimeIsNotMandatory() {

        appForm.setStartTime(null);
        appForm.setEndTime(null);

        validator.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
    }


    @Test
    public void ensureProvidingStartTimeWithoutEndTimeIsInvalid() {

        appForm.setStartTime(Time.valueOf("09:15:00"));
        appForm.setEndTime(null);

        validator.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }


    @Test
    public void ensureProvidingEndTimeWithoutStartTimeIsInvalid() {

        appForm.setStartTime(null);
        appForm.setEndTime(Time.valueOf("09:15:00"));

        validator.validate(appForm, errors);

        verify(errors).reject("error.entry.invalidPeriod");
    }


    @Test
    public void ensureStartTimeMustBeBeforeEndTime() {

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
    public void ensureStartTimeAndEndTimeMustNotBeEquals() {

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
    public void ensureReasonIsNotMandatoryForHoliday() {

        VacationType vacationType = TestDataCreator.createVacationType(VacationCategory.HOLIDAY);

        appForm.setVacationType(vacationType);
        appForm.setReason("");

        validator.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(eq("reason"), anyString());
    }


    @Test
    public void ensureReasonIsNotMandatoryForUnpaidLeave() {

        VacationType vacationType = TestDataCreator.createVacationType(VacationCategory.UNPAIDLEAVE);

        appForm.setVacationType(vacationType);
        appForm.setReason("");

        validator.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(eq("reason"), anyString());
    }


    @Test
    public void ensureReasonIsNotMandatoryForOvertime() {

        VacationType vacationType = TestDataCreator.createVacationType(VacationCategory.OVERTIME);

        appForm.setVacationType(vacationType);
        appForm.setReason("");

        validator.validate(appForm, errors);

        verify(errors, never()).reject(anyString());
        verify(errors, never()).rejectValue(eq("reason"), anyString());
    }


    @Test
    public void ensureReasonIsMandatoryForSpecialLeave() {

        VacationType vacationType = TestDataCreator.createVacationType(VacationCategory.SPECIALLEAVE);

        appForm.setVacationType(vacationType);
        appForm.setReason("");

        validator.validate(appForm, errors);

        verify(errors).rejectValue("reason", "application.error.missingReasonForSpecialLeave");
    }


    // Validate address ------------------------------------------------------------------------------------------------

    @Test
    public void ensureThereIsAMaximumCharLength() {

        appForm.setAddress(
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt"
                + " ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud "
                + "exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. ");

        validator.validate(appForm, errors);

        verify(errors).rejectValue("address", "error.entry.tooManyChars");
    }


    // Validate vacation days ------------------------------------------------------------------------------------------

    @Test
    public void ensureApplicationForLeaveWithZeroVacationDaysIsNotValid() {

        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(calendarService.getWorkDays(any(DayLength.class), any(LocalDate.class), any(LocalDate.class),
            any(Person.class))).thenReturn(BigDecimal.ZERO);

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.zeroDays");

        verifyZeroInteractions(overlapService);
        verifyZeroInteractions(calculationService);
    }


    @Test
    public void ensureApplyingForLeaveWithNotEnoughVacationDaysIsNotValid() {

        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(LocalDate.now(UTC));
        appForm.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));

        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(calendarService.getWorkDays(any(DayLength.class), any(LocalDate.class), any(LocalDate.class),
            any(Person.class))).thenReturn(BigDecimal.ONE);

        when(overlapService.checkOverlap(any(Application.class))).thenReturn(OverlapCase.NO_OVERLAPPING);

        when(calculationService.checkApplication(any(Application.class))).thenReturn(Boolean.FALSE);

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.notEnoughVacationDays");
    }


    @Test
    public void ensureApplyingHalfDayForLeaveWithNotEnoughVacationDaysIsNotValid() {

        appForm.setDayLength(DayLength.NOON);
        appForm.setStartDate(LocalDate.now(UTC));
        appForm.setEndDate(LocalDate.now(UTC));
        appForm.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));

        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(calendarService.getWorkDays(eq(appForm.getDayLength()), eq(appForm.getStartDate()),
            eq(appForm.getEndDate()), eq(appForm.getPerson())))
            .thenReturn(BigDecimal.ONE);

        when(overlapService.checkOverlap(any(Application.class))).thenReturn(OverlapCase.NO_OVERLAPPING);

        when(calculationService.checkApplication(any(Application.class))).thenReturn(Boolean.FALSE);

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.notEnoughVacationDays");
    }


    // Validate overlapping --------------------------------------------------------------------------------------------

    @Test
    public void ensureOverlappingApplicationForLeaveIsNotValid() {

        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(calendarService.getWorkDays(any(DayLength.class), any(LocalDate.class), any(LocalDate.class),
            any(Person.class))).thenReturn(BigDecimal.ONE);

        when(overlapService.checkOverlap(any(Application.class))).thenReturn(OverlapCase.FULLY_OVERLAPPING);

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.overlap");

        verifyZeroInteractions(calculationService);
    }


    // Validate hours --------------------------------------------------------------------------------------------------

    @Test
    public void ensureHoursIsMandatoryForOvertime() {

        appForm.setVacationType(TestDataCreator.createVacationType(VacationCategory.OVERTIME));
        appForm.setHours(null);

        validator.validate(appForm, errors);

        verify(errors).rejectValue("hours", "application.error.missingHoursForOvertime");
    }


    @Test
    public void ensureHoursIsNotMandatoryForOtherTypesOfVacation() {

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
    public void ensureHoursIsNotMandatoryForOvertimeIfOvertimeFunctionIsDeactivated() {

        settings.getWorkingTimeSettings().setOvertimeActive(false);

        appForm.setVacationType(TestDataCreator.createVacationType(VacationCategory.OVERTIME));
        appForm.setHours(null);

        validator.validate(appForm, errors);

        verify(errors, never()).rejectValue(eq("hours"), anyString());
    }


    @Test
    public void ensureHoursCanNotBeZero() {

        appForm.setHours(BigDecimal.ZERO);

        validator.validate(appForm, errors);

        verify(errors).rejectValue("hours", "application.error.invalidHoursForOvertime");
    }


    @Test
    public void ensureHoursCanNotBeNegative() {

        appForm.setHours(BigDecimal.ONE.negate());

        validator.validate(appForm, errors);

        verify(errors).rejectValue("hours", "application.error.invalidHoursForOvertime");
    }


    @Test
    public void ensureDecimalHoursAreValid() {

        appForm.setVacationType(TestDataCreator.createVacationType(VacationCategory.OVERTIME));
        appForm.setHours(new BigDecimal("0.5"));

        validator.validate(appForm, errors);

        verify(errors, never()).rejectValue(eq("hours"), anyString());
    }


    @Test
    public void ensureNoErrorMessageForMandatoryIfHoursIsNullBecauseOfTypeMismatch() {

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
    public void ensureWorkingTimeConfigurationMustExistForPeriodOfApplicationForLeave() {

        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class),
            eq(appForm.getStartDate()))).thenReturn(Optional.empty());

        validator.validate(appForm, errors);

        verify(errors).reject("application.error.noValidWorkingTime");

        verify(workingTimeService)
            .getByPersonAndValidityDateEqualsOrMinorDate(appForm.getPerson(), appForm.getStartDate());
        verifyZeroInteractions(calendarService);
        verifyZeroInteractions(overlapService);
        verifyZeroInteractions(calculationService);
    }


    @Test
    public void ensureWorkingTimeConfigurationMustExistForHalfDayApplicationForLeave() {

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
        verifyZeroInteractions(calendarService);
        verifyZeroInteractions(overlapService);
        verifyZeroInteractions(calculationService);
    }

    // Validate maximal overtime reduction -----------------------------------------------------------------------------


    /**
     * User tries to make an application (overtime reduction) but has not enough overtime left and minimum overtime is
     * reached.
     *
     * <p>0h overtime - 6h (application) < -5h overtime minimum</p>
     */
    @Test
    public void ensureErrorDueToMinimumOvertimeReached() {

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
    public void ensureNoErrorDueToExactMinimumOvertimeReached() {

        BigDecimal overtimeReductionHours = new BigDecimal("5");
        overtimeMinimumTest(overtimeReductionHours);
        verify(overtimeService).getLeftOvertimeForPerson(appForm.getPerson());
        assertFalse(errors.hasErrors());
    }

    @Test
    public void ensureAlreadyAbsentOnChristmasEveMorning() {

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
    public void ensureAlreadyAbsentOnChristmasEveMorningIsNotTriggered() {

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
    public void ensureAlreadyAbsentOnChristmasEveNoon() {

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
    public void ensureAlreadyAbsentOnChristmasEveFullForDayLengthFullRequest() {
        ensureAlreadyAbsentOnChristmasEveForGivenDayLengthRequest(DayLength.FULL);
    }

    @Test
    public void ensureAlreadyAbsentOnChristmasEveFullForDayLengthMorningRequest() {
        ensureAlreadyAbsentOnChristmasEveForGivenDayLengthRequest(DayLength.MORNING);
    }

    @Test
    public void ensureAlreadyAbsentOnChristmasEveFullForDayLengthNoonRequest() {
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
    public void ensureAlreadyAbsentOnNewYearsEveMorning() {

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
    public void ensureAlreadyAbsentOnNewYearsEveMorningIsNotTriggered() {

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
    public void ensureAlreadyAbsentOnNewYearsEveNoon() {

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
    public void ensureAlreadyAbsentOnNewYearsEveFullForDayLengthFullRequest() {
        ensureAlreadyAbsentOnNewYearsEveForGivenDayLengthRequest(DayLength.FULL);
    }

    @Test
    public void ensureAlreadyAbsentOnNewYearsEveFullForDayLengthMorningRequest() {
        ensureAlreadyAbsentOnNewYearsEveForGivenDayLengthRequest(DayLength.MORNING);
    }

    @Test
    public void ensureAlreadyAbsentOnNewYearsEveFullForDayLengthNoonRequest() {
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
            .person(TestDataCreator.createPerson())
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
