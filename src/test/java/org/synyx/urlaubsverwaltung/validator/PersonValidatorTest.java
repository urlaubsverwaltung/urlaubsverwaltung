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
import org.synyx.urlaubsverwaltung.util.NumberUtil;
import org.synyx.urlaubsverwaltung.view.PersonForm;

import java.math.BigDecimal;

import java.util.Locale;


/**
 * @author  Aljona Murygina
 */
public class PersonValidatorTest {

    private PersonValidator instance;
    private PersonForm form;
    Errors errors = Mockito.mock(Errors.class);

    private PropertiesValidator propValidator = Mockito.mock(PropertiesValidator.class);

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

        instance = new PersonValidator(propValidator);
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

        // test for
        double max_days = 365;

        BigDecimal days;
        String field = "desiredField";

        days = null;
        instance.validateNumberOfDays(days, field, max_days, errors);
        Mockito.verify(errors).rejectValue(field, "error.mandatory.field");
        Mockito.reset(errors);

        days = BigDecimal.valueOf(400);
        instance.validateNumberOfDays(days, field, max_days, errors);
        Mockito.verify(errors).rejectValue(field, "error.entry");
        Mockito.reset(errors);

        days = BigDecimal.valueOf(-1);
        instance.validateNumberOfDays(days, field, max_days, errors);
        Mockito.verify(errors).rejectValue(field, "error.entry");
        Mockito.reset(errors);

        // set normal
        days = BigDecimal.valueOf(25);
        instance.validateNumberOfDays(days, field, max_days, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // border = 365
        days = BigDecimal.valueOf(365);
        instance.validateNumberOfDays(days, field, max_days, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        days = BigDecimal.valueOf(366);
        instance.validateNumberOfDays(days, field, max_days, errors);
        Mockito.verify(errors).rejectValue(field, "error.entry");
        Mockito.reset(errors);
    }


    /** Test of validateAccountDays method, of class PersonValidator. */
    @Test
    public void testValidateAccountDays() {

        String vacDaysAcc;
        String vacRemDaysAcc;

        String vacDaysEnt;
        String vacRemDaysEnt;

        // fields are null

        vacRemDaysAcc = null;
        form.setRemainingVacationDaysAcc(vacRemDaysAcc);
        instance.validateAccountDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("remainingVacationDaysAcc", "error.mandatory.field");
        Mockito.reset(errors);

        vacDaysAcc = null;
        form.setVacationDaysAcc(vacDaysAcc);
        instance.validateAccountDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("vacationDaysAcc", "error.mandatory.field");
        Mockito.reset(errors);

        // check difference between holidays account's days and holiday entitlement's days
        vacRemDaysAcc = NumberUtil.formatNumber(BigDecimal.TEN, Locale.GERMAN);
        form.setRemainingVacationDaysAcc(vacRemDaysAcc); // may not be null for following tests, if in some
                                                         // cases zero interactions with errors expected/wanted

        // number of holidays account's vacation days is greater than number of holiday entitlement's vacation days
        vacDaysAcc = NumberUtil.formatNumber(BigDecimal.valueOf(24), Locale.GERMAN);
        form.setVacationDaysAcc(vacDaysAcc);
        vacDaysEnt = NumberUtil.formatNumber(BigDecimal.valueOf(19), Locale.GERMAN);
        form.setVacationDaysEnt(vacDaysEnt);
        instance.validateAccountDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("vacationDaysAcc", "error.number");
        Mockito.reset(errors);

        // number of holidays account's vacation days equals number of holiday entitlement's vacation days
        vacDaysAcc = NumberUtil.formatNumber(BigDecimal.valueOf(19), Locale.GERMAN);
        form.setVacationDaysAcc(vacDaysAcc);
        vacDaysEnt = NumberUtil.formatNumber(BigDecimal.valueOf(19), Locale.GERMAN);
        form.setVacationDaysEnt(vacDaysEnt);
        instance.validateAccountDays(form, errors, Locale.GERMAN);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // number of holidays account's vacation days is smaller than number of holiday entitlement's vacation days
        vacDaysAcc = NumberUtil.formatNumber(BigDecimal.valueOf(18), Locale.GERMAN);
        form.setVacationDaysAcc(vacDaysAcc);
        vacDaysEnt = NumberUtil.formatNumber(BigDecimal.valueOf(19), Locale.GERMAN);
        form.setVacationDaysEnt(vacDaysEnt);
        instance.validateAccountDays(form, errors, Locale.GERMAN);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // number of holidays account's remaining vacation days is greater than number of holiday entitlement's
        // remaining vacation days
        vacRemDaysAcc = NumberUtil.formatNumber(BigDecimal.valueOf(25), Locale.GERMAN);
        form.setRemainingVacationDaysAcc(vacRemDaysAcc);
        vacRemDaysEnt = NumberUtil.formatNumber(BigDecimal.valueOf(20), Locale.GERMAN);
        form.setRemainingVacationDaysEnt(vacRemDaysEnt);
        instance.validateAccountDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("remainingVacationDaysAcc", "error.number");
        Mockito.reset(errors);

        // number of holidays account's remaining vacation days is equals number of holiday entitlement's remaining
        // vacation days
        vacRemDaysAcc = NumberUtil.formatNumber(BigDecimal.valueOf(25), Locale.GERMAN);
        form.setRemainingVacationDaysAcc(vacRemDaysAcc);
        vacRemDaysEnt = NumberUtil.formatNumber(BigDecimal.valueOf(25), Locale.GERMAN);
        form.setRemainingVacationDaysEnt(vacRemDaysEnt);
        instance.validateAccountDays(form, errors, Locale.GERMAN);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // number of holidays account's remaining vacation days is smaller than number of holiday entitlement's
        // remaining vacation days
        vacRemDaysAcc = NumberUtil.formatNumber(BigDecimal.valueOf(25), Locale.GERMAN);
        form.setRemainingVacationDaysAcc(vacRemDaysAcc);
        vacRemDaysEnt = NumberUtil.formatNumber(BigDecimal.valueOf(28), Locale.GERMAN);
        form.setRemainingVacationDaysEnt(vacRemDaysEnt);
        instance.validateAccountDays(form, errors, Locale.GERMAN);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // negative values
        vacRemDaysAcc = NumberUtil.formatNumber(BigDecimal.valueOf(-1), Locale.GERMAN);
        form.setRemainingVacationDaysAcc(vacRemDaysAcc);
        instance.validateAccountDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("remainingVacationDaysAcc", "error.entry");
        Mockito.reset(errors);

        vacDaysAcc = NumberUtil.formatNumber(BigDecimal.valueOf(-1), Locale.GERMAN);
        form.setVacationDaysAcc(vacDaysAcc);
        instance.validateAccountDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("vacationDaysAcc", "error.entry");
        Mockito.reset(errors);
    }


    /** Test of validateProperties method, of class PersonValidator. */
    @Test
    public void testValidateProperties() {

        // is tested in PropertiesValidatorTest: test for method validateAnnualVacationProperty
    }


    /** Test of validateAnnualVacation method, of class PersonValidator. */
    @Test
    public void testValidateAnnualVacation() {

        String annualVac;

        // fields are null or empty

        annualVac = null;
        form.setAnnualVacationDaysEnt(annualVac);
        instance.validateAnnualVacation(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("annualVacationDaysEnt", "error.mandatory.field");
        Mockito.reset(errors);

        annualVac = "";
        form.setAnnualVacationDaysEnt(annualVac);
        instance.validateAnnualVacation(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("annualVacationDaysEnt", "error.mandatory.field");
        Mockito.reset(errors);

        // invalid values
        annualVac = "a";
        form.setAnnualVacationDaysEnt(annualVac);
        instance.validateAnnualVacation(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("annualVacationDaysEnt", "error.entry");
        Mockito.reset(errors);

        // not realistic numbers
        annualVac = "367";
        form.setAnnualVacationDaysEnt(annualVac);
        instance.validateAnnualVacation(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("annualVacationDaysEnt", "error.entry");
        Mockito.reset(errors);

        // everything is alright
        annualVac = "28";
        form.setAnnualVacationDaysEnt(annualVac);
        instance.validateAnnualVacation(form, errors, Locale.GERMAN);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);
    }


    /** Test of validateEntitlementVacationDays method, of class PersonValidator. */
    @Test
    public void testValidateEntitlementVacationDays() {

        String vacDaysEnt;

        // null or empty

        vacDaysEnt = null;
        form.setVacationDaysEnt(vacDaysEnt);
        instance.validateEntitlementVacationDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("vacationDaysEnt", "error.mandatory.field");
        Mockito.reset(errors);

        vacDaysEnt = "";
        form.setVacationDaysEnt(vacDaysEnt);
        instance.validateEntitlementVacationDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("vacationDaysEnt", "error.mandatory.field");
        Mockito.reset(errors);

        // invalid values
        vacDaysEnt = "a";
        form.setVacationDaysEnt(vacDaysEnt);
        instance.validateEntitlementVacationDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("vacationDaysEnt", "error.entry");
        Mockito.reset(errors);

        // not realistic numbers
        vacDaysEnt = "367";
        form.setVacationDaysEnt(vacDaysEnt);
        instance.validateEntitlementVacationDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("vacationDaysEnt", "error.entry");
        Mockito.reset(errors);

        // everything is alright
        vacDaysEnt = "28";

        form.setVacationDaysEnt(vacDaysEnt);
        instance.validateEntitlementVacationDays(form, errors, Locale.GERMAN);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);
    }


    /** Test of validateEntitlementRemainingVacationDays method, of class PersonValidator. */
    @Test
    public void testValidateEntitlementRemainingVacationDays() {

        String vacRemDaysEnt;

        // null or empty
        vacRemDaysEnt = null;
        form.setRemainingVacationDaysEnt(vacRemDaysEnt);
        instance.validateEntitlementRemainingVacationDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("remainingVacationDaysEnt", "error.mandatory.field");
        Mockito.reset(errors);

        vacRemDaysEnt = "";
        form.setRemainingVacationDaysEnt(vacRemDaysEnt);
        instance.validateEntitlementRemainingVacationDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("remainingVacationDaysEnt", "error.mandatory.field");
        Mockito.reset(errors);

        // invalid values
        vacRemDaysEnt = "a";
        form.setRemainingVacationDaysEnt(vacRemDaysEnt);
        instance.validateEntitlementRemainingVacationDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("remainingVacationDaysEnt", "error.entry");
        Mockito.reset(errors);

        // not realistic numbers
        vacRemDaysEnt = "367";
        form.setRemainingVacationDaysEnt(vacRemDaysEnt);
        instance.validateEntitlementRemainingVacationDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("remainingVacationDaysEnt", "error.entry");
        Mockito.reset(errors);

        // everything is alright
        vacRemDaysEnt = "5";
        form.setRemainingVacationDaysEnt(vacRemDaysEnt);
        instance.validateEntitlementRemainingVacationDays(form, errors, Locale.GERMAN);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);
    }
}
