package org.synyx.urlaubsverwaltung.web.validator;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.OverlapCase;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.core.application.service.OverlapService;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.web.application.ApplicationForLeaveForm;

import java.math.BigDecimal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.web.validator.ApplicationValidator}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationValidatorTest {

    private ApplicationValidator validator;
    private OwnCalendarService calendarService;
    private OverlapService overlapService;
    private CalculationService calculationService;

    private Errors errors;
    private ApplicationForLeaveForm appForm;

    @Before
    public void setUp() {

        calendarService = Mockito.mock(OwnCalendarService.class);
        overlapService = Mockito.mock(OverlapService.class);
        calculationService = Mockito.mock(CalculationService.class);

        validator = new ApplicationValidator(calendarService, overlapService, calculationService);
        errors = Mockito.mock(Errors.class);

        appForm = new ApplicationForLeaveForm();

        appForm.setVacationType(VacationType.HOLIDAY);
        appForm.setHowLong(DayLength.FULL);
        appForm.setStartDate(DateMidnight.now());
        appForm.setEndDate(DateMidnight.now().plusDays(2));

        // if the positive case is tested, override this condition in the test
        Mockito.when(errors.hasErrors()).thenReturn(Boolean.TRUE);
    }


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


    @Test
    public void ensureStartDateIsMandatory() {

        appForm.setHowLong(DayLength.FULL);
        appForm.setStartDate(null);

        validator.validate(appForm, errors);

        Mockito.verify(errors).rejectValue("startDate", "error.mandatory.field");
    }


    @Test
    public void ensureEndDateIsMandatory() {

        appForm.setHowLong(DayLength.FULL);
        appForm.setEndDate(null);

        validator.validate(appForm, errors);

        Mockito.verify(errors).rejectValue("endDate", "error.mandatory.field");
    }


    @Test
    public void ensureStartDateIsNotMandatoryForHalfDays() {

        Mockito.when(errors.hasErrors()).thenReturn(Boolean.FALSE);
        Mockito.when(calendarService.getWorkDays(Mockito.any(DayLength.class), Mockito.any(DateMidnight.class),
                Mockito.any(DateMidnight.class), Mockito.any(Person.class))).thenReturn(BigDecimal.ONE);
        Mockito.when(overlapService.checkOverlap(Mockito.any(Application.class))).thenReturn(
            OverlapCase.NO_OVERLAPPING);
        Mockito.when(calculationService.checkApplication(Mockito.any(Application.class))).thenReturn(Boolean.TRUE);

        appForm.setHowLong(DayLength.MORNING);
        appForm.setStartDateHalf(DateMidnight.now());

        appForm.setStartDate(null);

        validator.validate(appForm, errors);

        Mockito.verify(errors, Mockito.never()).reject(Mockito.anyString());
        Mockito.verify(errors, Mockito.never()).rejectValue(Mockito.anyString(), Mockito.anyString());
    }


    @Test
    public void ensureStartDateHalfIsNotMandatoryForFullDays() {

        Mockito.when(errors.hasErrors()).thenReturn(Boolean.FALSE);
        Mockito.when(calendarService.getWorkDays(Mockito.any(DayLength.class), Mockito.any(DateMidnight.class),
                Mockito.any(DateMidnight.class), Mockito.any(Person.class))).thenReturn(BigDecimal.ONE);
        Mockito.when(overlapService.checkOverlap(Mockito.any(Application.class))).thenReturn(
            OverlapCase.NO_OVERLAPPING);
        Mockito.when(calculationService.checkApplication(Mockito.any(Application.class))).thenReturn(Boolean.TRUE);

        appForm.setHowLong(DayLength.FULL);
        appForm.setStartDateHalf(null);

        validator.validate(appForm, errors);

        Mockito.verify(errors, Mockito.never()).reject(Mockito.anyString());
        Mockito.verify(errors, Mockito.never()).rejectValue(Mockito.anyString(), Mockito.anyString());
    }


    @Test
    public void ensureDoesNotCheckPeriodIfStartDateIsNull() {

        appForm.setHowLong(DayLength.FULL);
        appForm.setStartDate(null);
        appForm.setEndDate(new DateMidnight(2012, 1, 12));

        try {
            validator.validate(appForm, errors);
        } catch (NullPointerException ex) {
            Assert.fail("Should not try to check the period if the date is null");
        }
    }


    @Test
    public void ensureDoesNotCheckPeriodIfEndDateIsNull() {

        appForm.setHowLong(DayLength.FULL);
        appForm.setStartDate(new DateMidnight(2012, 1, 12));
        appForm.setEndDate(null);

        try {
            validator.validate(appForm, errors);
        } catch (NullPointerException ex) {
            Assert.fail("Should not try to check the period if the date is null");
        }
    }


    @Test
    public void ensureDoesNotCheckPeriodIfStartDateHalfIsNull() {

        appForm.setHowLong(DayLength.MORNING);
        appForm.setStartDateHalf(null);

        try {
            validator.validate(appForm, errors);
        } catch (NullPointerException ex) {
            Assert.fail("Should not try to check the period if the date is null");
        }
    }


    @Test
    public void ensureReasonIsNotMandatoryForHoliday() {

        Mockito.when(errors.hasErrors()).thenReturn(Boolean.FALSE);
        Mockito.when(calendarService.getWorkDays(Mockito.any(DayLength.class), Mockito.any(DateMidnight.class),
                Mockito.any(DateMidnight.class), Mockito.any(Person.class))).thenReturn(BigDecimal.ONE);
        Mockito.when(overlapService.checkOverlap(Mockito.any(Application.class))).thenReturn(
            OverlapCase.NO_OVERLAPPING);
        Mockito.when(calculationService.checkApplication(Mockito.any(Application.class))).thenReturn(Boolean.TRUE);

        appForm.setVacationType(VacationType.HOLIDAY);
        appForm.setReason("");

        validator.validate(appForm, errors);

        Mockito.verify(errors, Mockito.never()).reject(Mockito.anyString());
        Mockito.verify(errors, Mockito.never()).rejectValue(Mockito.anyString(), Mockito.anyString());
    }


    @Test
    public void ensureReasonIsMandatoryForOtherVacationTypeThanHoliday() {

        appForm.setVacationType(VacationType.OVERTIME);
        appForm.setReason("");

        validator.validate(appForm, errors);

        Mockito.verify(errors).rejectValue("reason", "error.mandatory.field");
    }


    @Test
    public void ensureThereIsAMaximumCharLength() {

        appForm.setAddress(
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt"
            + " ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud "
            + "exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. ");

        validator.validate(appForm, errors);

        Mockito.verify(errors).rejectValue("address", "error.length");
    }


    @Test
    public void ensureStartDateMustBeBeforeEndDate() {

        appForm.setHowLong(DayLength.FULL);
        appForm.setStartDate(new DateMidnight(2012, 1, 17));
        appForm.setEndDate(new DateMidnight(2012, 1, 12));

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("error.period");
    }


    @Test
    public void ensureVeryPastDateIsNotValid() {

        DateMidnight pastDate = DateMidnight.now().minusYears(10);

        appForm.setHowLong(DayLength.FULL);
        appForm.setStartDate(pastDate);
        appForm.setEndDate(pastDate.plusDays(1));

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("error.period.past");
    }


    @Test
    public void ensureVeryFutureDateIsNotValid() {

        DateMidnight futureDate = DateMidnight.now().plusYears(10);

        appForm.setHowLong(DayLength.FULL);
        appForm.setStartDate(futureDate);
        appForm.setEndDate(futureDate.plusDays(1));

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("error.too.long");
    }


    @Test
    public void ensureVeryPastDateForHalfDayIsNotValid() {

        DateMidnight pastDate = DateMidnight.now().minusYears(10);

        appForm.setHowLong(DayLength.MORNING);
        appForm.setStartDateHalf(pastDate);

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("error.period.past");
    }


    @Test
    public void ensureVeryFutureDateForHalfDayIsNotValid() {

        DateMidnight futureDate = DateMidnight.now().plusYears(10);

        appForm.setHowLong(DayLength.NOON);
        appForm.setStartDateHalf(futureDate);

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("error.too.long");
    }


    @Test
    public void ensureApplicationForLeaveWithZeroVacationDaysIsNotValid() {

        Mockito.when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        Mockito.when(calendarService.getWorkDays(Mockito.any(DayLength.class), Mockito.any(DateMidnight.class),
                Mockito.any(DateMidnight.class), Mockito.any(Person.class))).thenReturn(BigDecimal.ZERO);

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("error.zero.days");

        Mockito.verifyZeroInteractions(overlapService);
        Mockito.verifyZeroInteractions(calculationService);
    }


    @Test
    public void ensureOverlappingApplicationForLeaveIsNotValid() {

        Mockito.when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        Mockito.when(calendarService.getWorkDays(Mockito.any(DayLength.class), Mockito.any(DateMidnight.class),
                Mockito.any(DateMidnight.class), Mockito.any(Person.class))).thenReturn(BigDecimal.ONE);

        Mockito.when(overlapService.checkOverlap(Mockito.any(Application.class))).thenReturn(
            OverlapCase.FULLY_OVERLAPPING);

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("error.overlap");

        Mockito.verifyZeroInteractions(calculationService);
    }


    @Test
    public void ensureApplyingForLeaveWithNotEnoughVacationDaysIsNotValid() {

        Mockito.when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        Mockito.when(calendarService.getWorkDays(Mockito.any(DayLength.class), Mockito.any(DateMidnight.class),
                Mockito.any(DateMidnight.class), Mockito.any(Person.class))).thenReturn(BigDecimal.ONE);

        Mockito.when(overlapService.checkOverlap(Mockito.any(Application.class))).thenReturn(
            OverlapCase.NO_OVERLAPPING);

        Mockito.when(calculationService.checkApplication(Mockito.any(Application.class))).thenReturn(Boolean.FALSE);

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("error.not.enough.days");
    }


    @Test
    public void ensureApplyingHalfDayForLeaveWithNotEnoughVacationDaysIsNotValid() {

        appForm.setHowLong(DayLength.NOON);
        appForm.setStartDateHalf(DateMidnight.now());

        Mockito.when(errors.hasErrors()).thenReturn(Boolean.FALSE);

        Mockito.when(calendarService.getWorkDays(Mockito.eq(appForm.getHowLong()),
                Mockito.eq(appForm.getStartDateHalf()), Mockito.eq(appForm.getStartDateHalf()),
                Mockito.eq(appForm.getPerson()))).thenReturn(BigDecimal.ONE);

        Mockito.when(overlapService.checkOverlap(Mockito.any(Application.class))).thenReturn(
            OverlapCase.NO_OVERLAPPING);

        Mockito.when(calculationService.checkApplication(Mockito.any(Application.class))).thenReturn(Boolean.FALSE);

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("error.not.enough.days");
    }
}
