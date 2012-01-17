/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.validator;

import org.joda.time.DateMidnight;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.VacationType;
import org.synyx.urlaubsverwaltung.view.AppForm;


/**
 * @author  Aljona Murygina
 */
public class ApplicationValidatorTest {

    private ApplicationValidator instance;

    public ApplicationValidatorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {

        instance = new ApplicationValidator();
    }


    @After
    public void tearDown() {
    }


    /** Test of supports method, of class ApplicationValidator. */
    @Test
    public void testSupports() {

        boolean returnValue;

        returnValue = instance.supports(null);
        assertFalse(returnValue);

        returnValue = instance.supports(Person.class);
        assertFalse(returnValue);

        returnValue = instance.supports(AppForm.class);
        assertTrue(returnValue);
    }


    /** Test of validate method, of class ApplicationValidator. */
    @Test
    public void testValidate() {

        AppForm app = new AppForm();
        Errors errors = Mockito.mock(Errors.class);

        app.setVacationType(VacationType.HOLIDAY);
        app.setHowLong(DayLength.FULL);

        // if mandatory fields are empty

        // if date fields are null
        app.setEndDate(null);
        instance.validate(app, errors);
        Mockito.verify(errors).rejectValue("endDate", "error.mandatory.field");
        Mockito.reset(errors);

        app.setStartDate(null);
        instance.validate(app, errors);
        Mockito.verify(errors).rejectValue("startDate", "error.mandatory.field");
        Mockito.reset(errors);

        app.setHowLong(DayLength.MORNING);
        app.setStartDateHalf(null);
        instance.validate(app, errors);
        Mockito.verify(errors).rejectValue("startDateHalf", "error.mandatory.field");
        Mockito.reset(errors);

        // if field reason is empty

        app.setVacationType(VacationType.OVERTIME);
        app.setReason(null);
        instance.validate(app, errors);
        Mockito.verify(errors).rejectValue("reason", "error.mandatory.field");
        Mockito.reset(errors);

        app.setReason("");
        instance.validate(app, errors);
        Mockito.verify(errors).rejectValue("reason", "error.mandatory.field");
        Mockito.reset(errors);

        // if from > to
        app.setHowLong(DayLength.FULL);
        app.setStartDate(new DateMidnight(2012, 1, 17));
        app.setEndDate(new DateMidnight(2012, 1, 12));
        instance.validate(app, errors);
        Mockito.verify(errors).reject("error.period");
        Mockito.reset(errors);

        // test if everything is ok
        app.setEndDate(new DateMidnight(2012, 1, 20));
        app.setVacationType(VacationType.SPECIALLEAVE);
        app.setReason("Hochzeit");
        instance.validate(app, errors);
        Mockito.verifyZeroInteractions(errors);
    }


    /** Test of validateForUser method, of class ApplicationValidator. */
    @Test
    public void testValidateForUser() {

        AppForm app = new AppForm();
        Errors errors = Mockito.mock(Errors.class);

        // full day
        app.setHowLong(DayLength.FULL);
        app.setStartDate(new DateMidnight(2012, 1, 16));

        instance.validateForUser(app, errors);
        Mockito.verify(errors).rejectValue("startDate", "error.period.past");
        Mockito.reset(errors);

        // half day
        app.setHowLong(DayLength.MORNING);
        app.setStartDateHalf(new DateMidnight(2012, 1, 16));

        instance.validateForUser(app, errors);
        Mockito.verify(errors).rejectValue("startDateHalf", "error.period.past");
        Mockito.reset(errors);
    }
}
