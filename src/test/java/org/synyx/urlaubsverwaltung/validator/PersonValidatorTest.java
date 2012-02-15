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

        // names with special characters
        // should be no error
        form.setFirstName("Réne");
        form.setLastName("Hûgo");

        instance.validateName(form.getFirstName(), "firstName", errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        instance.validateName(form.getLastName(), "lastName", errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        form.setFirstName("Mark");
        form.setLastName("Müller"); // umlaut allowed too

        instance.validateName(form.getFirstName(), "firstName", errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        instance.validateName(form.getLastName(), "lastName", errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        form.setFirstName("λα"); // greek allowed too
        form.setLastName("Müller");

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
        Mockito.verify(errors).rejectValue("email", "error.email");
        Mockito.reset(errors);

        // more than one '@'
        form.setEmail("fraulyoner@verwa@ltung.de");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verify(errors).rejectValue("email", "error.email");
        Mockito.reset(errors);

        // structure not like: user@host.domain

        // e.g. '@' at start of email address
        form.setEmail("@fraulyonerverwaltung.de");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verify(errors).rejectValue("email", "error.email");
        Mockito.reset(errors);

        // e.g. no point after host is
        form.setEmail("fraulyoner@verwaltungde");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verify(errors).rejectValue("email", "error.email");
        Mockito.reset(errors);

        // e.g. structure like: xy@host, no domain
        form.setEmail("fraulyoner@verwaltung");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verify(errors).rejectValue("email", "error.email");
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

        form.setEmail("frauLyoner@verwaltung.de");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // umlaut
        // should not result in error
        form.setEmail("müller@verwaltung.de");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        form.setEmail("tom@müller.de");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // ß
        // should not result in error
        form.setEmail("maß@mod.de");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // special cases: strange looking, but valid
        form.setEmail("to#m@arbeit.de");
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

        double max_days = 365;

        // validate entitlement of vacation days remaining vacation days
        form.setVacationDaysEnt(null);
        instance.validateNumberOfDays(form.getVacationDaysEnt(), "vacationDaysEnt", max_days, errors);
        Mockito.verify(errors).rejectValue("vacationDaysEnt", "error.mandatory.field");
        Mockito.reset(errors);

        form.setVacationDaysEnt(BigDecimal.valueOf(400));
        instance.validateNumberOfDays(form.getVacationDaysEnt(), "vacationDaysEnt", max_days, errors);
        Mockito.verify(errors).rejectValue("vacationDaysEnt", "error.entry");
        Mockito.reset(errors);

        form.setVacationDaysEnt(BigDecimal.valueOf(-1));
        instance.validateNumberOfDays(form.getVacationDaysEnt(), "vacationDaysEnt", max_days, errors);
        Mockito.verify(errors).rejectValue("vacationDaysEnt", "error.entry");
        Mockito.reset(errors);

        // set normal
        form.setVacationDaysEnt(BigDecimal.valueOf(25));
        instance.validateNumberOfDays(form.getVacationDaysEnt(), "vacationDaysEnt", max_days, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // border = 365
        form.setVacationDaysEnt(BigDecimal.valueOf(365));
        instance.validateNumberOfDays(form.getVacationDaysEnt(), "vacationDaysEnt", max_days, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        form.setVacationDaysEnt(BigDecimal.valueOf(366));
        instance.validateNumberOfDays(form.getVacationDaysEnt(), "vacationDaysEnt", max_days, errors);
        Mockito.verify(errors).rejectValue("vacationDaysEnt", "error.entry");
        Mockito.reset(errors);

        // remaining vacation days
        form.setRemainingVacationDaysEnt(null);
        instance.validateNumberOfDays(form.getRemainingVacationDaysEnt(), "remainingVacationDaysEnt", max_days, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDaysEnt", "error.mandatory.field");
        Mockito.reset(errors);

        form.setRemainingVacationDaysEnt(BigDecimal.valueOf(400));
        instance.validateNumberOfDays(form.getRemainingVacationDaysEnt(), "remainingVacationDaysEnt", max_days, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDaysEnt", "error.entry");
        Mockito.reset(errors);

        form.setRemainingVacationDaysEnt(BigDecimal.valueOf(-1));
        instance.validateNumberOfDays(form.getRemainingVacationDaysEnt(), "remainingVacationDaysEnt", max_days, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDaysEnt", "error.entry");
        Mockito.reset(errors);

        // set normal
        form.setRemainingVacationDaysEnt(BigDecimal.valueOf(5));
        instance.validateNumberOfDays(form.getRemainingVacationDaysEnt(), "remainingVacationDaysEnt", max_days, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // border = 365
        form.setRemainingVacationDaysEnt(BigDecimal.valueOf(365));
        instance.validateNumberOfDays(form.getRemainingVacationDaysEnt(), "vacationDaysEnt", max_days, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        form.setRemainingVacationDaysEnt(BigDecimal.valueOf(366));
        instance.validateNumberOfDays(form.getRemainingVacationDaysEnt(), "remainingVacationDaysEnt", max_days, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDaysEnt", "error.entry");
        Mockito.reset(errors);
    }


    /** Test of validateAccountDays method, of class PersonValidator. */
    @Test
    public void testValidateAccountDays() {

        // fields are null

        form.setRemainingVacationDaysAcc(null);
        instance.validateAccountDays(form, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDaysAcc", "error.mandatory.field");
        Mockito.reset(errors);

        form.setVacationDaysAcc(null);
        instance.validateAccountDays(form, errors);
        Mockito.verify(errors).rejectValue("vacationDaysAcc", "error.mandatory.field");
        Mockito.reset(errors);

        // check difference between holidays account's days and holiday entitlement's days
        form.setRemainingVacationDaysAcc(BigDecimal.valueOf(10)); // may not be null for following tests, if in some
                                                                  // cases zero interactions with errors expected/wanted

        // number of holidays account's vacation days is greater than number of holiday entitlement's vacation days
        form.setVacationDaysAcc(BigDecimal.valueOf(24));
        form.setVacationDaysEnt(BigDecimal.valueOf(19));
        instance.validateAccountDays(form, errors);
        Mockito.verify(errors).rejectValue("vacationDaysAcc", "error.number");
        Mockito.reset(errors);

        // number of holidays account's vacation days equals number of holiday entitlement's vacation days
        form.setVacationDaysAcc(BigDecimal.valueOf(19));
        form.setVacationDaysEnt(BigDecimal.valueOf(19));
        instance.validateAccountDays(form, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // number of holidays account's vacation days is smaller than number of holiday entitlement's vacation days
        form.setVacationDaysAcc(BigDecimal.valueOf(18));
        form.setVacationDaysEnt(BigDecimal.valueOf(19));
        instance.validateAccountDays(form, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // number of holidays account's remaining vacation days is greater than number of holiday entitlement's
        // remaining vacation days
        form.setRemainingVacationDaysAcc(BigDecimal.valueOf(25));
        form.setRemainingVacationDaysEnt(BigDecimal.valueOf(20));
        instance.validateAccountDays(form, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDaysAcc", "error.number");
        Mockito.reset(errors);

        // number of holidays account's remaining vacation days is equals number of holiday entitlement's remaining
        // vacation days
        form.setRemainingVacationDaysAcc(BigDecimal.valueOf(25));
        form.setRemainingVacationDaysEnt(BigDecimal.valueOf(25));
        instance.validateAccountDays(form, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // number of holidays account's remaining vacation days is smaller than number of holiday entitlement's
        // remaining vacation days
        form.setRemainingVacationDaysAcc(BigDecimal.valueOf(25));
        form.setRemainingVacationDaysEnt(BigDecimal.valueOf(28));
        instance.validateAccountDays(form, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // negative values
        form.setRemainingVacationDaysAcc(BigDecimal.valueOf(-1));
        instance.validateAccountDays(form, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDaysAcc", "error.entry");
        Mockito.reset(errors);

        form.setVacationDaysAcc(BigDecimal.valueOf(-1));
        instance.validateAccountDays(form, errors);
        Mockito.verify(errors).rejectValue("vacationDaysAcc", "error.entry");
        Mockito.reset(errors);
    }
}
