package org.synyx.urlaubsverwaltung.web.overtime;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;

import java.util.function.Consumer;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class OvertimeValidatorTest {

    private OvertimeValidator validator;

    private Errors errors;
    private OvertimeForm overtimeForm;

    @Before
    public void setUp() {

        validator = new OvertimeValidator();
        errors = Mockito.mock(Errors.class);

        Person person = TestDataCreator.createPerson();
        overtimeForm = new OvertimeForm(person);
        overtimeForm.setStartDate(DateMidnight.now());
        overtimeForm.setEndDate(DateMidnight.now().plusDays(1));
        overtimeForm.setNumberOfHours(BigDecimal.ONE);
        overtimeForm.setComment("Lorem ipsum");
    }


    // Support method --------------------------------------------------------------------------------------------------

    @Test
    public void ensureSupportsOvertimeFormClass() {

        Assert.assertTrue("Should support overtime form class", validator.supports(OvertimeForm.class));
    }


    @Test
    public void ensureDoesNotSupportOtherClass() {

        Assert.assertFalse("Should not support other class than overtime form class", validator.supports(Person.class));
    }


    // Validate method -------------------------------------------------------------------------------------------------

    @Test
    public void ensureNoErrorsIfValid() {

        validator.validate(overtimeForm, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    // Validate period -------------------------------------------------------------------------------------------------

    @Test
    public void ensureStartDateIsMandatory() {

        overtimeForm.setStartDate(null);

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).rejectValue("startDate", "error.entry.mandatory");
    }


    @Test
    public void ensureEndDateIsMandatory() {

        overtimeForm.setEndDate(null);

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).rejectValue("endDate", "error.entry.mandatory");
    }


    @Test
    public void ensureStartAndEndDateCanBeEquals() {

        DateMidnight now = DateMidnight.now();

        overtimeForm.setStartDate(now);
        overtimeForm.setEndDate(now);

        validator.validate(overtimeForm, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureStartDateCanNotBeAfterEndDate() {

        overtimeForm.setStartDate(overtimeForm.getEndDate().plusDays(3));

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).rejectValue("endDate", "error.entry.invalidPeriod");
    }


    @Test
    public void ensureNoErrorMessageForMandatoryIfStartDateIsNullBecauseOfTypeMismatch() {

        Mockito.when(errors.hasFieldErrors("startDate")).thenReturn(true);

        overtimeForm.setStartDate(null);

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).hasFieldErrors("startDate");
        Mockito.verify(errors, Mockito.never()).rejectValue("startDate", "error.entry.mandatory");
    }


    @Test
    public void ensureNoErrorMessageForMandatoryIfEndDateIsNullBecauseOfTypeMismatch() {

        Mockito.when(errors.hasFieldErrors("endDate")).thenReturn(true);

        overtimeForm.setEndDate(null);

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).hasFieldErrors("endDate");
        Mockito.verify(errors, Mockito.never()).rejectValue("endDate", "error.entry.mandatory");
    }


    // Validate number of hours ----------------------------------------------------------------------------------------

    @Test
    public void ensureNumberOfHoursIsMandatory() {

        overtimeForm.setNumberOfHours(null);

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).rejectValue("numberOfHours", "error.entry.mandatory");
    }


    @Test
    public void ensureNumberOfHoursCanNotBeNegative() {

        overtimeForm.setNumberOfHours(BigDecimal.ONE.negate());

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).rejectValue("numberOfHours", "overtime.data.numberOfHours.error");
    }


    @Test
    public void ensureNumberOfHoursCanNotBeZero() {

        overtimeForm.setNumberOfHours(BigDecimal.ZERO);

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).rejectValue("numberOfHours", "overtime.data.numberOfHours.error");
    }


    @Test
    public void ensureNumberOfHoursMustBePositive() {

        overtimeForm.setNumberOfHours(new BigDecimal("0.5"));

        validator.validate(overtimeForm, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureNoErrorMessageForMandatoryIfNumberOfHoursIsNullBecauseOfTypeMismatch() {

        Mockito.when(errors.hasFieldErrors("numberOfHours")).thenReturn(true);

        overtimeForm.setNumberOfHours(null);

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).hasFieldErrors("numberOfHours");
        Mockito.verify(errors, Mockito.never()).rejectValue("endDate", "error.entry.mandatory");
    }


    // Validate comment ------------------------------------------------------------------------------------------------

    @Test
    public void ensureCommentIsNotMandatory() {

        Consumer<String> assertMayBeEmpty = (comment) -> {
            overtimeForm.setComment(comment);

            validator.validate(overtimeForm, errors);

            Mockito.verifyZeroInteractions(errors);
        };

        assertMayBeEmpty.accept(null);
        assertMayBeEmpty.accept("");
    }


    @Test
    public void ensureCommentHasMaximumCharacterLength() {

        overtimeForm.setComment(
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore "
            + "et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores e");

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).rejectValue("comment", "error.entry.tooManyChars");
    }
}
