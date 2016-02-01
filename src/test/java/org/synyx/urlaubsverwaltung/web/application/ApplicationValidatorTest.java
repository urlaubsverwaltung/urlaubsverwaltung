package org.synyx.urlaubsverwaltung.web.application;

import org.joda.time.DateMidnight;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.core.calendar.OverlapCase;
import org.synyx.urlaubsverwaltung.core.calendar.OverlapService;
import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;

import java.sql.Time;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.any;

import static org.mockito.Mockito.when;


/**
 * Unit test for {@link ApplicationValidator}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationValidatorTest {

    private ApplicationValidator validator;

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

        settingsService = Mockito.mock(SettingsService.class);
        settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        calendarService = Mockito.mock(WorkDaysService.class);
        overlapService = Mockito.mock(OverlapService.class);
        calculationService = Mockito.mock(CalculationService.class);
        workingTimeService = Mockito.mock(WorkingTimeService.class);
        overtimeService = Mockito.mock(OvertimeService.class);

        validator = new ApplicationValidator(workingTimeService, calendarService, overlapService, calculationService,
                settingsService, overtimeService);
        errors = Mockito.mock(Errors.class);

        appForm = new ApplicationForLeaveForm();

        appForm.setVacationType(TestDataCreator.getVacationType(VacationType.HOLIDAY));
        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(DateMidnight.now());
        appForm.setEndDate(DateMidnight.now().plusDays(2));

        // Default: everything is alright, override for negative cases
        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(DateMidnight.class)))
            .thenReturn(Optional.of(TestDataCreator.createWorkingTime()));
        when(calendarService.getWorkDays(any(DayLength.class), any(DateMidnight.class), any(DateMidnight.class),
                    any(Person.class))).thenReturn(BigDecimal.ONE);
        when(overlapService.checkOverlap(any(Application.class))).thenReturn(OverlapCase.NO_OVERLAPPING);
        when(calculationService.checkApplication(any(Application.class))).thenReturn(Boolean.TRUE);
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

        Mockito.verify(errors).rejectValue("startDate", "error.entry.mandatory");
    }


    @Test
    public void ensureEndDateIsMandatory() {

        appForm.setDayLength(DayLength.FULL);
        appForm.setEndDate(null);

        validator.validate(appForm, errors);

        Mockito.verify(errors).rejectValue("endDate", "error.entry.mandatory");
    }


    @Test
    public void ensureStartDateMustBeBeforeEndDate() {

        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(new DateMidnight(2012, 1, 17));
        appForm.setEndDate(new DateMidnight(2012, 1, 12));

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("error.entry.invalidPeriod");
    }


    @Test
    public void ensureVeryPastDateIsNotValid() {

        DateMidnight pastDate = DateMidnight.now().minusYears(10);

        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(pastDate);
        appForm.setEndDate(pastDate.plusDays(1));

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("application.error.tooFarInThePast");
    }


    @Test
    public void ensureVeryFutureDateIsNotValid() {

        DateMidnight futureDate = DateMidnight.now().plusYears(10);

        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(futureDate);
        appForm.setEndDate(futureDate.plusDays(1));

        validator.validate(appForm, errors);

        Mockito.verify(errors)
            .reject("application.error.tooFarInTheFuture",
                new Object[] { settings.getAbsenceSettings().getMaximumMonthsToApplyForLeaveInAdvance() }, null);
    }


    @Test
    public void ensureMorningApplicationForLeaveMustBeOnSameDate() {

        appForm.setDayLength(DayLength.MORNING);
        appForm.setStartDate(DateMidnight.now());
        appForm.setEndDate(DateMidnight.now().plusDays(1));

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("application.error.halfDayPeriod");
    }


    @Test
    public void ensureNoonApplicationForLeaveMustBeOnSameDate() {

        appForm.setDayLength(DayLength.NOON);
        appForm.setStartDate(DateMidnight.now());
        appForm.setEndDate(DateMidnight.now().plusDays(1));

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("application.error.halfDayPeriod");
    }


    @Test
    public void ensureSameDateAsStartAndEndDateIsValidForFullDayPeriod() {

        DateMidnight date = DateMidnight.now();

        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(date);
        appForm.setEndDate(date);

        validator.validate(appForm, errors);

        Mockito.verify(errors, Mockito.never()).reject(Mockito.anyString());
    }


    @Test
    public void ensureSameDateAsStartAndEndDateIsValidForMorningPeriod() {

        DateMidnight date = DateMidnight.now();

        appForm.setDayLength(DayLength.MORNING);
        appForm.setStartDate(date);
        appForm.setEndDate(date);

        validator.validate(appForm, errors);

        Mockito.verify(errors, Mockito.never()).reject(Mockito.anyString());
    }


    @Test
    public void ensureSameDateAsStartAndEndDateIsValidForNoonPeriod() {

        DateMidnight date = DateMidnight.now();

        appForm.setDayLength(DayLength.NOON);
        appForm.setStartDate(date);
        appForm.setEndDate(date);

        validator.validate(appForm, errors);

        Mockito.verify(errors, Mockito.never()).reject(Mockito.anyString());
    }


    // Validate period (time) ------------------------------------------------------------------------------------------

    @Test
    public void ensureTimeIsNotMandatory() {

        appForm.setStartTime(null);
        appForm.setEndTime(null);

        validator.validate(appForm, errors);

        Mockito.verify(errors, Mockito.never()).reject(Mockito.anyString());
    }


    @Test
    public void ensureProvidingStartTimeWithoutEndTimeIsInvalid() {

        appForm.setStartTime(Time.valueOf("09:15:00"));
        appForm.setEndTime(null);

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("error.entry.invalidPeriod");
    }


    @Test
    public void ensureProvidingEndTimeWithoutStartTimeIsInvalid() {

        appForm.setStartTime(null);
        appForm.setEndTime(Time.valueOf("09:15:00"));

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("error.entry.invalidPeriod");
    }


    @Test
    public void ensureStartTimeMustBeBeforeEndTime() {

        DateMidnight date = DateMidnight.now();

        appForm.setDayLength(DayLength.MORNING);
        appForm.setStartDate(date);
        appForm.setEndDate(date);
        appForm.setStartTime(Time.valueOf("13:30:00"));
        appForm.setEndTime(Time.valueOf("09:15:00"));

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("error.entry.invalidPeriod");
    }


    @Test
    public void ensureStartTimeAndEndTimeMustNotBeEquals() {

        DateMidnight date = DateMidnight.now();
        Time time = Time.valueOf("13:30:00");

        appForm.setDayLength(DayLength.MORNING);
        appForm.setStartDate(date);
        appForm.setEndDate(date);
        appForm.setStartTime(time);
        appForm.setEndTime(time);

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("error.entry.invalidPeriod");
    }


    // Validate reason -------------------------------------------------------------------------------------------------

    @Test
    public void ensureReasonIsNotMandatoryForHoliday() {

        VacationType vacationType = TestDataCreator.getVacationType(VacationType.HOLIDAY);

        appForm.setVacationType(vacationType);
        appForm.setReason("");

        validator.validate(appForm, errors);

        Mockito.verify(errors, Mockito.never()).reject(Mockito.anyString());
        Mockito.verify(errors, Mockito.never()).rejectValue(Mockito.eq("reason"), Mockito.anyString());
    }


    @Test
    public void ensureReasonIsNotMandatoryForUnpaidLeave() {

        VacationType vacationType = TestDataCreator.getVacationType(VacationType.UNPAIDLEAVE);

        appForm.setVacationType(vacationType);
        appForm.setReason("");

        validator.validate(appForm, errors);

        Mockito.verify(errors, Mockito.never()).reject(Mockito.anyString());
        Mockito.verify(errors, Mockito.never()).rejectValue(Mockito.eq("reason"), Mockito.anyString());
    }


    @Test
    public void ensureReasonIsNotMandatoryForOvertime() {

        VacationType vacationType = TestDataCreator.getVacationType(VacationType.OVERTIME);

        appForm.setVacationType(vacationType);
        appForm.setReason("");

        validator.validate(appForm, errors);

        Mockito.verify(errors, Mockito.never()).reject(Mockito.anyString());
        Mockito.verify(errors, Mockito.never()).rejectValue(Mockito.eq("reason"), Mockito.anyString());
    }


    @Test
    public void ensureReasonIsMandatoryForSpecialLeave() {

        VacationType vacationType = TestDataCreator.getVacationType(VacationType.SPECIALLEAVE);

        appForm.setVacationType(vacationType);
        appForm.setReason("");

        validator.validate(appForm, errors);

        Mockito.verify(errors).rejectValue("reason", "application.error.missingReasonForSpecialLeave");
    }


    // Validate address ------------------------------------------------------------------------------------------------

    @Test
    public void ensureThereIsAMaximumCharLength() {

        appForm.setAddress(
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt"
            + " ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud "
            + "exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. ");

        validator.validate(appForm, errors);

        Mockito.verify(errors).rejectValue("address", "error.entry.tooManyChars");
    }


    // Validate vacation days ------------------------------------------------------------------------------------------

    @Test
    public void ensureApplicationForLeaveWithZeroVacationDaysIsNotValid() {

        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(calendarService.getWorkDays(any(DayLength.class), any(DateMidnight.class), any(DateMidnight.class),
                    any(Person.class))).thenReturn(BigDecimal.ZERO);

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("application.error.zeroDays");

        Mockito.verifyZeroInteractions(overlapService);
        Mockito.verifyZeroInteractions(calculationService);
    }


    @Test
    public void ensureApplyingForLeaveWithNotEnoughVacationDaysIsNotValid() {

        appForm.setDayLength(DayLength.FULL);
        appForm.setStartDate(DateMidnight.now());
        appForm.setEndDate(DateMidnight.now());
        appForm.setVacationType(TestDataCreator.getVacationType(VacationType.HOLIDAY));

        Mockito.when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(calendarService.getWorkDays(any(DayLength.class), any(DateMidnight.class), any(DateMidnight.class),
                    any(Person.class))).thenReturn(BigDecimal.ONE);

        when(overlapService.checkOverlap(any(Application.class))).thenReturn(OverlapCase.NO_OVERLAPPING);

        when(calculationService.checkApplication(any(Application.class))).thenReturn(Boolean.FALSE);

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("application.error.notEnoughVacationDays");
    }


    @Test
    public void ensureApplyingHalfDayForLeaveWithNotEnoughVacationDaysIsNotValid() {

        appForm.setDayLength(DayLength.NOON);
        appForm.setStartDate(DateMidnight.now());
        appForm.setEndDate(DateMidnight.now());
        appForm.setVacationType(TestDataCreator.getVacationType(VacationType.HOLIDAY));

        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        Mockito.when(calendarService.getWorkDays(Mockito.eq(appForm.getDayLength()), Mockito.eq(appForm.getStartDate()),
                    Mockito.eq(appForm.getEndDate()), Mockito.eq(appForm.getPerson())))
            .thenReturn(BigDecimal.ONE);

        when(overlapService.checkOverlap(any(Application.class))).thenReturn(OverlapCase.NO_OVERLAPPING);

        when(calculationService.checkApplication(any(Application.class))).thenReturn(Boolean.FALSE);

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("application.error.notEnoughVacationDays");
    }


    // Validate overlapping --------------------------------------------------------------------------------------------

    @Test
    public void ensureOverlappingApplicationForLeaveIsNotValid() {

        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(calendarService.getWorkDays(any(DayLength.class), any(DateMidnight.class), any(DateMidnight.class),
                    any(Person.class))).thenReturn(BigDecimal.ONE);

        when(overlapService.checkOverlap(any(Application.class))).thenReturn(OverlapCase.FULLY_OVERLAPPING);

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("application.error.overlap");

        Mockito.verifyZeroInteractions(calculationService);
    }


    // Validate hours --------------------------------------------------------------------------------------------------

    @Test
    public void ensureHoursIsMandatoryForOvertime() {

        appForm.setVacationType(TestDataCreator.getVacationType(VacationType.OVERTIME));
        appForm.setHours(null);

        validator.validate(appForm, errors);

        Mockito.verify(errors).rejectValue("hours", "application.error.missingHoursForOvertime");
    }


    @Test
    public void ensureHoursIsNotMandatoryForOtherTypesOfVacation() {

        Consumer<VacationType> assertHoursNotMandatory = (type) -> {
            appForm.setVacationType(type);
            appForm.setHours(null);

            validator.validate(appForm, errors);

            Mockito.verify(errors, Mockito.never()).rejectValue(Mockito.eq("hours"), Mockito.anyString());
        };

        VacationType holiday = TestDataCreator.getVacationType(VacationType.HOLIDAY);
        VacationType specialLeave = TestDataCreator.getVacationType(VacationType.SPECIALLEAVE);
        VacationType unpaidLeave = TestDataCreator.getVacationType(VacationType.UNPAIDLEAVE);

        assertHoursNotMandatory.accept(holiday);
        assertHoursNotMandatory.accept(specialLeave);
        assertHoursNotMandatory.accept(unpaidLeave);
    }


    @Test
    public void ensureHoursCanNotBeZero() {

        appForm.setHours(BigDecimal.ZERO);

        validator.validate(appForm, errors);

        Mockito.verify(errors).rejectValue("hours", "application.error.invalidHoursForOvertime");
    }


    @Test
    public void ensureHoursCanNotBeNegative() {

        appForm.setHours(BigDecimal.ONE.negate());

        validator.validate(appForm, errors);

        Mockito.verify(errors).rejectValue("hours", "application.error.invalidHoursForOvertime");
    }


    @Test
    public void ensureDecimalHoursAreValid() {

        appForm.setVacationType(TestDataCreator.getVacationType(VacationType.OVERTIME));
        appForm.setHours(new BigDecimal("0.5"));

        validator.validate(appForm, errors);

        Mockito.verify(errors, Mockito.never()).rejectValue(Mockito.eq("hours"), Mockito.anyString());
    }


    @Test
    public void ensureNoErrorMessageForMandatoryIfHoursIsNullBecauseOfTypeMismatch() {

        appForm.setVacationType(TestDataCreator.getVacationType(VacationType.OVERTIME));
        appForm.setHours(null);

        when(errors.hasFieldErrors("hours")).thenReturn(true);

        validator.validate(appForm, errors);

        Mockito.verify(errors).hasFieldErrors("hours");
        Mockito.verify(errors, Mockito.never()).rejectValue("hours", "application.error.missingHoursForOvertime");
    }


    // Validate working time exists ------------------------------------------------------------------------------------

    @Test
    public void ensureWorkingTimeConfigurationMustExistForPeriodOfApplicationForLeave() {

        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class),
                    Mockito.eq(appForm.getStartDate()))).thenReturn(Optional.empty());

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("application.error.noValidWorkingTime");

        Mockito.verify(workingTimeService)
            .getByPersonAndValidityDateEqualsOrMinorDate(appForm.getPerson(), appForm.getStartDate());
        Mockito.verifyZeroInteractions(calendarService);
        Mockito.verifyZeroInteractions(overlapService);
        Mockito.verifyZeroInteractions(calculationService);
    }


    @Test
    public void ensureWorkingTimeConfigurationMustExistForHalfDayApplicationForLeave() {

        // Yes, this can really happen...
        appForm.setStartDate(null);
        appForm.setEndDate(null);

        appForm.setStartDate(DateMidnight.now());
        appForm.setEndDate(DateMidnight.now());
        appForm.setDayLength(DayLength.MORNING);

        when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        Mockito.when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(Mockito.any(Person.class),
                    Mockito.eq(appForm.getStartDate())))
            .thenReturn(Optional.empty());

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("application.error.noValidWorkingTime");

        Mockito.verify(workingTimeService)
            .getByPersonAndValidityDateEqualsOrMinorDate(appForm.getPerson(), appForm.getStartDate());
        Mockito.verifyZeroInteractions(calendarService);
        Mockito.verifyZeroInteractions(overlapService);
        Mockito.verifyZeroInteractions(calculationService);
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
        Mockito.verify(overtimeService).getLeftOvertimeForPerson(appForm.getPerson());
        Mockito.verify(errors).reject("application.error.notEnoughOvertime");
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
        Mockito.verify(overtimeService).getLeftOvertimeForPerson(appForm.getPerson());
        assertFalse(errors.hasErrors());
    }


    private void overtimeMinimumTest(BigDecimal hours) {

        VacationType vacationType = TestDataCreator.getVacationType(VacationType.OVERTIME);

        appForm.setHours(hours);
        appForm.setVacationType(vacationType);

        settings.getWorkingTimeSettings().setOvertimeActive(true);
        settings.getWorkingTimeSettings().setMinimumOvertime(5);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.ZERO);

        validator.validate(appForm, errors);
    }
}
