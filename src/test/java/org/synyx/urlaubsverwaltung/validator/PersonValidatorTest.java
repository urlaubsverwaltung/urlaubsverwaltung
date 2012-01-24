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

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina
 */
public class PersonValidatorTest {

    private PersonValidator instance;
    private PersonForm form;
    Errors errors = Mockito.mock(Errors.class);

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
        form = new PersonForm();
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

        // see the several tests to the different fields

    }


    /** Test of validateName method, of class PersonValidator. */
    @Test
    public void testValidateName() {

        // if the name fields are empty (null or empty String), an error message is set

        // field is null
        form.setFirstName(null);
        form.setLastName(null);

        instance.validateName(form.getFirstName(), "firstName", errors);
        Mockito.verify(errors).rejectValue("firstName", "error.mandatory.field");
        Mockito.reset(errors);

        instance.validateName(form.getLastName(), "lastName", errors);
        Mockito.verify(errors).rejectValue("lastName", "error.mandatory.field");
        Mockito.reset(errors);

        // field is empty
        form.setFirstName("");
        form.setLastName("");

        instance.validateName(form.getFirstName(), "firstName", errors);
        Mockito.verify(errors).rejectValue("firstName", "error.mandatory.field");
        Mockito.reset(errors);

        instance.validateName(form.getLastName(), "lastName", errors);
        Mockito.verify(errors).rejectValue("lastName", "error.mandatory.field");
        Mockito.reset(errors);

        // numbers or any other invalid character in name (allowed only a-z, A-Z)
        form.setFirstName("123hakdl");
        form.setLastName("fh@.-");

        instance.validateName(form.getFirstName(), "firstName", errors);
        Mockito.verify(errors).rejectValue("firstName", "error.entry");
        Mockito.reset(errors);

        instance.validateName(form.getLastName(), "lastName", errors);
        Mockito.verify(errors).rejectValue("lastName", "error.entry");
        Mockito.reset(errors);

        form.setFirstName("Vorn5ame");
        form.setLastName("Nachna%me");

        instance.validateName(form.getFirstName(), "firstName", errors);
        Mockito.verify(errors).rejectValue("firstName", "error.entry");
        Mockito.reset(errors);

        instance.validateName(form.getLastName(), "lastName", errors);
        Mockito.verify(errors).rejectValue("lastName", "error.entry");
        Mockito.reset(errors);

        // everything ok
        form.setFirstName("Vorname");
        form.setLastName("Nachname");

        instance.validateName(form.getFirstName(), "firstName", errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        instance.validateName(form.getLastName(), "lastName", errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);
    }


    /** Test of validateEmail method, of class PersonValidator. */
    @Test
    public void testValidateEmail() {

        // if email field is filled: is the email address valid?

        // if it is an empty String or null

        form.setEmail(null);
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verify(errors).rejectValue("email", "error.mandatory.field");
        Mockito.reset(errors);

        form.setEmail("");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verify(errors).rejectValue("email", "error.mandatory.field");
        Mockito.reset(errors);

        // if there is no '@' in the email address, an error message is set

        form.setEmail("fraulyoner(at)verwaltung.de");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verify(errors).rejectValue("email", "error.entry");
        Mockito.reset(errors);

        // more than one '@'
        form.setEmail("fraulyoner@verwa@ltung.de");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verify(errors).rejectValue("email", "error.entry");
        Mockito.reset(errors);

        // structure not like: user@host.domain

        // e.g. '@' at start of email address
        form.setEmail("@fraulyonerverwaltung.de");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verify(errors).rejectValue("email", "error.entry");
        Mockito.reset(errors);

        // e.g. no point after host is
        form.setEmail("fraulyoner@verwaltungde");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verify(errors).rejectValue("email", "error.entry");
        Mockito.reset(errors);

        // e.g. structure like: xy@host, no domain
        form.setEmail("fraulyoner@verwaltung");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verify(errors).rejectValue("email", "error.entry");
        Mockito.reset(errors);

        // valid email addresses
        form.setEmail("fraulyoner@verwaltung.de");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // valid is structure like: user@net.de
        form.setEmail("fraulyoner@verwaltung.com.de");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        form.setEmail("fraulyoner@verwaltung.de");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);
    }


    /** Test of validateYear method, of class PersonValidator. */
    @Test
    public void testValidateYear() {

        // is the year field valid?

        // error 1: year not set, empty String
        form.setYear("");
        instance.validateYear(form.getYear(), errors);
        Mockito.verify(errors).rejectValue("year", "error.mandatory.field");
        Mockito.reset(errors);

        form.setYear(null);
        instance.validateYear(form.getYear(), errors);
        Mockito.verify(errors).rejectValue("year", "error.mandatory.field");
        Mockito.reset(errors);

        // error 2: string can't be parsed to int
        form.setYear("abc");
        instance.validateYear(form.getYear(), errors);
        Mockito.verify(errors).rejectValue("year", "error.entry");
        Mockito.reset(errors);

        // error 3: year > 2030
        form.setYear("2032");
        instance.validateYear(form.getYear(), errors);
        Mockito.verify(errors).rejectValue("year", "error.entry");
        Mockito.reset(errors);

        // error 4: year < 2010
        form.setYear("2009");
        instance.validateYear(form.getYear(), errors);
        Mockito.verify(errors).rejectValue("year", "error.entry");
        Mockito.reset(errors);

        // correct: year = 2010
        form.setYear("2010");
        instance.validateYear(form.getYear(), errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);
    }


    /** Test of validateNumberOfDays method, of class PersonValidator. */
    @Test
    public void testValidateNumberOfDays() {

        // validate entitlement of vacation days remaining vacation days
        form.setVacationDays(null);
        instance.validateNumberOfDays(form.getVacationDays(), "vacationDays", 40, errors);
        Mockito.verify(errors).rejectValue("vacationDays", "error.mandatory.field");
        Mockito.reset(errors);

        form.setVacationDays(BigDecimal.valueOf(400));
        instance.validateNumberOfDays(form.getVacationDays(), "vacationDays", 40, errors);
        Mockito.verify(errors).rejectValue("vacationDays", "error.entry");
        Mockito.reset(errors);

        form.setVacationDays(BigDecimal.valueOf(-1));
        instance.validateNumberOfDays(form.getVacationDays(), "vacationDays", 40, errors);
        Mockito.verify(errors).rejectValue("vacationDays", "error.entry");
        Mockito.reset(errors);

        // set normal
        form.setVacationDays(BigDecimal.valueOf(25));
        instance.validateNumberOfDays(form.getVacationDays(), "vacationDays", 40, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // remaining vacation days
        form.setRemainingVacationDays(null);
        instance.validateNumberOfDays(form.getRemainingVacationDays(), "remainingVacationDays", 20, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDays", "error.mandatory.field");
        Mockito.reset(errors);

        form.setRemainingVacationDays(BigDecimal.valueOf(400));
        instance.validateNumberOfDays(form.getRemainingVacationDays(), "remainingVacationDays", 20, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDays", "error.entry");
        Mockito.reset(errors);

        form.setRemainingVacationDays(BigDecimal.valueOf(-1));
        instance.validateNumberOfDays(form.getRemainingVacationDays(), "remainingVacationDays", 20, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDays", "error.entry");
        Mockito.reset(errors);

        // set normal
        form.setRemainingVacationDays(BigDecimal.valueOf(5));
        instance.validateNumberOfDays(form.getRemainingVacationDays(), "remainingVacationDays", 20, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);
    }
}
