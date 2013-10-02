
package org.synyx.urlaubsverwaltung.validator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.web.PersonForm;

import java.math.BigDecimal;

import java.util.Locale;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Unit test for {@link PersonValidator}.
 *
 * @author  Aljona Murygina
 */
public class PersonValidatorTest {

    private PersonValidator instance;
    private PersonForm form;
    Errors errors = Mockito.mock(Errors.class);

    private PropertiesValidator propValidator = Mockito.mock(PropertiesValidator.class);
    private PersonService personService = Mockito.mock(PersonService.class);

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

        instance = new PersonValidator(propValidator, personService);
        form = new PersonForm();
    }


    @After
    public void tearDown() {
    }


    /**
     * Test of supports method, of class PersonValidator.
     */
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


    /**
     * Test of validate method, of class PersonValidator.
     */
    @Test
    public void testValidate() {

        // see the several tests to the different fields

    }


    /**
     * Test of validateName method, of class PersonValidator.
     */
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

        // invalid String length
        form.setFirstName("Vorname");
        form.setLastName("AAAAAAAAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        instance.validateName(form.getFirstName(), "firstName", errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        instance.validateName(form.getLastName(), "lastName", errors);
        Mockito.verify(errors).rejectValue("lastName", "error.length");
        Mockito.reset(errors);

        form.setFirstName("AAAAAAAAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        form.setLastName("Nachname");

        instance.validateName(form.getFirstName(), "firstName", errors);
        Mockito.verify(errors).rejectValue("firstName", "error.length");
        Mockito.reset(errors);

        instance.validateName(form.getLastName(), "lastName", errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);
    }


    /**
     * Test of validateEmail method, of class PersonValidator.
     */
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

        // invalid String length
        form.setEmail("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@net.de");
        instance.validateEmail(form.getEmail(), errors);
        Mockito.verify(errors).rejectValue("email", "error.length");
        Mockito.reset(errors);
    }


    /**
     * Test of validateYear method, of class PersonValidator.
     */
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


    /**
     * Test of validateNumberOfDays method, of class PersonValidator.
     */
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


    /**
     * Test of validateProperties method, of class PersonValidator.
     */
    @Test
    public void testValidateProperties() {

        // is tested in PropertiesValidatorTest: test for method validateAnnualVacationProperty
    }


    /**
     * Test of validateAnnualVacation method, of class PersonValidator.
     */
    @Test
    public void testValidateAnnualVacation() {

        String annualVac;

        // fields are null or empty

        annualVac = null;
        form.setAnnualVacationDays(annualVac);
        instance.validateAnnualVacation(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("annualVacationDays", "error.mandatory.field");
        Mockito.reset(errors);

        annualVac = "";
        form.setAnnualVacationDays(annualVac);
        instance.validateAnnualVacation(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("annualVacationDays", "error.mandatory.field");
        Mockito.reset(errors);

        // invalid values
        annualVac = "a";
        form.setAnnualVacationDays(annualVac);
        instance.validateAnnualVacation(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("annualVacationDays", "error.entry");
        Mockito.reset(errors);

        // not realistic numbers
        annualVac = "367";
        form.setAnnualVacationDays(annualVac);
        instance.validateAnnualVacation(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("annualVacationDays", "error.entry");
        Mockito.reset(errors);

        // everything is alright
        annualVac = "28";
        form.setAnnualVacationDays(annualVac);
        instance.validateAnnualVacation(form, errors, Locale.GERMAN);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);
    }


    /**
     * Test of validateEntitlementRemainingVacationDays method, of class PersonValidator.
     */
    @Test
    public void testValidateEntitlementRemainingVacationDays() {

        String vacRemDaysEnt;

        // null or empty
        vacRemDaysEnt = null;
        form.setRemainingVacationDays(vacRemDaysEnt);
        instance.validateRemainingVacationDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("remainingVacationDays", "error.mandatory.field");
        Mockito.reset(errors);

        vacRemDaysEnt = "";
        form.setRemainingVacationDays(vacRemDaysEnt);
        instance.validateRemainingVacationDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("remainingVacationDays", "error.mandatory.field");
        Mockito.reset(errors);

        // invalid values
        vacRemDaysEnt = "a";
        form.setRemainingVacationDays(vacRemDaysEnt);
        instance.validateRemainingVacationDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("remainingVacationDays", "error.entry");
        Mockito.reset(errors);

        // not realistic numbers
        vacRemDaysEnt = "367";
        form.setRemainingVacationDays(vacRemDaysEnt);
        instance.validateRemainingVacationDays(form, errors, Locale.GERMAN);
        Mockito.verify(errors).rejectValue("remainingVacationDays", "error.entry");
        Mockito.reset(errors);

        // everything is alright
        vacRemDaysEnt = "5";
        form.setRemainingVacationDays(vacRemDaysEnt);
        instance.validateRemainingVacationDays(form, errors, Locale.GERMAN);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);
    }


    @Test
    public void testNotUniqueLoginName() {

        Mockito.reset(errors);
        Mockito.when(personService.getPersonByLogin("foo")).thenReturn(new Person());

        instance.validateLogin("foo", errors);

        Mockito.verify(errors).rejectValue("loginName", "error.login.unique");
    }


    @Test
    public void testUniqueLoginName() {

        Mockito.reset(errors);
        Mockito.when(personService.getPersonByLogin("foo")).thenReturn(null);

        instance.validateLogin("foo", errors);

        Mockito.verify(errors, Mockito.never()).rejectValue(Mockito.anyString(), Mockito.anyString());
    }


    @Test
    public void testInvalidPeriod1() {

        Mockito.reset(errors);

        // invalid period: 5.1.2013 - 1.1.2013

        form.setYear("2013");

        form.setMonthFrom("1");
        form.setMonthTo("1");

        form.setDayFrom("5");
        form.setDayTo("1");

        instance.validatePeriod(form, errors);

        Mockito.verify(errors).reject("error.period");
    }


    @Test
    public void testInvalidPeriod2() {

        Mockito.reset(errors);

        // invalid period: 5.1.2013 - 5.1.2013

        form.setYear("2013");

        form.setMonthFrom("1");
        form.setMonthTo("1");

        form.setDayFrom("5");
        form.setDayTo("5");

        instance.validatePeriod(form, errors);

        Mockito.verify(errors).reject("error.period");
    }


    @Test
    public void testInvalidPeriod3() {

        Mockito.reset(errors);

        // invalid period: 1.5.2013 - 5.1.2013

        form.setYear("2013");

        form.setMonthFrom("5");
        form.setMonthTo("1");

        form.setDayFrom("1");
        form.setDayTo("5");

        instance.validatePeriod(form, errors);

        Mockito.verify(errors).reject("error.period");
    }


    @Test
    public void testValidPeriod() {

        Mockito.reset(errors);

        // valid period: 1.5.2013 - 5.5.2013

        form.setYear("2013");

        form.setMonthFrom("5");
        form.setMonthTo("5");

        form.setDayFrom("1");
        form.setDayTo("5");

        instance.validatePeriod(form, errors);

        Mockito.verifyZeroInteractions(errors);
    }
}
