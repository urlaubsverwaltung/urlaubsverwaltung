/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.validator;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.view.PersonForm;


/**
 * @author  Aljona Murygina
 */
public class PersonValidatorTest {

    private PersonValidator instance;

    public PersonValidatorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {

        instance = new PersonValidator();
    }


    @After
    public void tearDown() {
    }


    /** Test of supports method, of class PersonValidator. */
    @Test
    public void testSupports() {

        boolean returnValue;

        returnValue = instance.supports(null);
        assertFalse(returnValue);

        returnValue = instance.supports(Application.class);
        assertFalse(returnValue);

        returnValue = instance.supports(PersonForm.class);
        assertTrue(returnValue);
    }


    /** Test of validate method, of class PersonValidator. */
    @Test
    public void testValidate() {

        PersonForm form = new PersonForm();
        Errors errors = Mockito.mock(Errors.class);

        // if the name fields are empty (null or empty String), an error message is set

        form.setFirstName(null);
        form.setLastName(null);

        instance.validate(form, errors);
        Mockito.verify(errors).rejectValue("firstName", "error.mandatory.field");
        Mockito.verify(errors).rejectValue("lastName", "error.mandatory.field");
        Mockito.reset(errors);

        form.setFirstName("");
        form.setLastName("");

        instance.validate(form, errors);
        Mockito.verify(errors).rejectValue("firstName", "error.mandatory.field");
        Mockito.verify(errors).rejectValue("lastName", "error.mandatory.field");
        Mockito.reset(errors);

        // if email field is filled: is the email address valid?

        // if it is an empty String or the email address is correct, no interaction with errors

        form.setFirstName("Vorname");
        form.setLastName("Nachname");
        form.setYear("2011");
        form.setEmail("");
        instance.validate(form, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        form.setEmail("fraulyoner@verwaltung.de");
        instance.validate(form, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // if there is no '@' in the email address, an error message is set

        form.setEmail("fraulyoner(at)verwaltung.de");
        instance.validate(form, errors);
        Mockito.verify(errors).rejectValue("email", "error.entry");

        form.setEmail("fraulyoner@verwaltung.de");

        // is the year field valid?

        // error 1: year not set, empty String
        form.setYear("");
        instance.validate(form, errors);
        Mockito.verify(errors).rejectValue("year", "error.mandatory.field");
        Mockito.reset(errors);

        form.setYear(null);
        instance.validate(form, errors);
        Mockito.verify(errors).rejectValue("year", "error.mandatory.field");
        Mockito.reset(errors);

        // error 2: string can't be parsed to int
        form.setYear("abc");
        instance.validate(form, errors);
        Mockito.verify(errors).rejectValue("year", "error.entry");
        Mockito.reset(errors);

        // error 3: year > 2030
        form.setYear("2032");
        instance.validate(form, errors);
        Mockito.verify(errors).rejectValue("year", "error.entry");
        Mockito.reset(errors);

        // error 4: year < 2010
        form.setYear("2009");
        instance.validate(form, errors);
        Mockito.verify(errors).rejectValue("year", "error.entry");
        Mockito.reset(errors);

        // correct: year = 2010
        form.setYear("2010");
        instance.validate(form, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);
    }
}
