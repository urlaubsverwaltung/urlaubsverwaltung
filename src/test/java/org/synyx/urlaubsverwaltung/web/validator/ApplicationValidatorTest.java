package org.synyx.urlaubsverwaltung.web.validator;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.ui.Model;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.web.application.AppForm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * @author  Aljona Murygina
 */
public class ApplicationValidatorTest {

    private ApplicationValidator validator;
    private Errors errors;

    private AppForm appForm;

    @Before
    public void setUp() {

        validator = new ApplicationValidator();
        errors = Mockito.mock(Errors.class);

        appForm = new AppForm();

        appForm.setVacationType(VacationType.HOLIDAY);
        appForm.setHowLong(DayLength.FULL);
        appForm.setStartDate(DateMidnight.now());
        appForm.setEndDate(DateMidnight.now().plusDays(2));
    }


    @Test
    public void ensureSupportsAppFormClass() {

        assertTrue(validator.supports(AppForm.class));
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
        Mockito.reset(errors);
    }


    @Test
    public void ensureEndDateIsMandatory() {

        appForm.setHowLong(DayLength.FULL);
        appForm.setEndDate(null);

        validator.validate(appForm, errors);

        Mockito.verify(errors).rejectValue("endDate", "error.mandatory.field");
        Mockito.reset(errors);
    }


    @Test
    public void ensureStartDateIsNotMandatoryForHalfDays() {

        appForm.setHowLong(DayLength.MORNING);
        appForm.setStartDate(null);

        validator.validate(appForm, errors);

        assertTrue(errors.getFieldErrors("startDate").isEmpty());
    }


    @Test
    public void ensureStartDateHalfIsNotMandatoryForFullDays() {

        appForm.setHowLong(DayLength.FULL);
        appForm.setStartDateHalf(null);

        validator.validate(appForm, errors);

        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);
    }


    @Test
    public void ensureReasonIsNotMandatoryForHoliday() {

        appForm.setVacationType(VacationType.HOLIDAY);
        appForm.setReason("");

        validator.validate(appForm, errors);

        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);
    }


    @Test
    public void ensureReasonIsMandatoryForOtherVacationTypeThanHoliday() {

        appForm.setVacationType(VacationType.OVERTIME);
        appForm.setReason("");

        validator.validate(appForm, errors);

        Mockito.verify(errors).rejectValue("reason", "error.mandatory.field");
        Mockito.reset(errors);
    }


    @Test
    public void ensureThereIsAMaximumCharLength() {

        appForm.setAddress(
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt"
            + " ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud "
            + "exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. ");

        validator.validate(appForm, errors);

        Mockito.verify(errors).rejectValue("address", "error.length");
        Mockito.reset(errors);
    }


    @Test
    public void ensureValidatingStringLengthReturnsTrueForValidString() {

        boolean isValid = validator.validateStringLength("kurze knackige Begründung", 50);
        assertNotNull(isValid);
        assertTrue(isValid);
    }


    @Test
    public void ensureValidatingStringLengthReturnsFalseForInvalidString() {

        String text =
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt";

        boolean isValid = validator.validateStringLength(text, 50);
        assertNotNull(isValid);
        assertFalse(isValid);
    }


    @Test
    public void ensureStartDateMustBeBeforeEndDate() {

        appForm.setHowLong(DayLength.FULL);
        appForm.setStartDate(new DateMidnight(2012, 1, 17));
        appForm.setEndDate(new DateMidnight(2012, 1, 12));

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("error.period");
        Mockito.reset(errors);
    }


    @Test
    public void ensureVeryPastDateIsNotValid() {

        Model model = Mockito.mock(Model.class);

        appForm.setHowLong(DayLength.FULL);
        appForm.setStartDate(new DateMidnight(2011, DateTimeConstants.SEPTEMBER, 1));

        validator.validatePast(appForm, errors, model);

        Mockito.verify(model).addAttribute("timeError", "error.period.past");
    }


    @Test
    public void ensureVeryFutureDateIsNotValid() {

        DateMidnight futureDate = DateMidnight.now().plusYears(10);

        appForm.setHowLong(DayLength.FULL);
        appForm.setStartDate(futureDate);
        appForm.setEndDate(futureDate.plusDays(1));

        validator.validate(appForm, errors);

        Mockito.verify(errors).reject("error.too.long");
        Mockito.reset(errors);
    }
}
