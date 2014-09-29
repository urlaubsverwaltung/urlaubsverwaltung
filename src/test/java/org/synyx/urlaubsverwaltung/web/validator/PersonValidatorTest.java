
package org.synyx.urlaubsverwaltung.web.validator;

import org.joda.time.DateMidnight;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.web.person.PersonForm;

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
    private Errors errors = Mockito.mock(Errors.class);

    private PersonService personService = Mockito.mock(PersonService.class);

    @Before
    public void setUp() {

        instance = new PersonValidator(personService);
        form = new PersonForm();
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
     * Test of validateName method, of class PersonValidator.
     */
    @Test
    public void testValidateName() {

        // null or empty name

        instance.validateName(null, "nameField", errors);
        Mockito.verify(errors).rejectValue("nameField", "error.mandatory.field");
        Mockito.reset(errors);

        instance.validateName("", "nameField", errors);
        Mockito.verify(errors).rejectValue("nameField", "error.mandatory.field");
        Mockito.reset(errors);

        // invalid String length
        instance.validateName("AAAAAAAAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "nameField",
            errors);
        Mockito.verify(errors).rejectValue("nameField", "error.length");
        Mockito.reset(errors);

        // everything ok
        instance.validateName("Max", "nameField", errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // names with special characters ok too
        instance.validateName("Réne Hûgö λα", "nameField", errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // composed names ok too
        instance.validateName("Hans-Peter", "nameField", errors);
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

        instance.validateEmail(null, errors);
        Mockito.verify(errors).rejectValue("email", "error.mandatory.field");
        Mockito.reset(errors);

        instance.validateEmail("", errors);
        Mockito.verify(errors).rejectValue("email", "error.mandatory.field");
        Mockito.reset(errors);

        // if there is no '@' in the email address, an error message is set

        instance.validateEmail("fraulyoner(at)verwaltung.de", errors);
        Mockito.verify(errors).rejectValue("email", "error.email");
        Mockito.reset(errors);

        // more than one '@'
        instance.validateEmail("fraulyoner@verwa@ltung.de", errors);
        Mockito.verify(errors).rejectValue("email", "error.email");
        Mockito.reset(errors);

        // structure not like: user@host.domain

        // e.g. '@' at start of email address
        instance.validateEmail("@fraulyonerverwaltung.de", errors);
        Mockito.verify(errors).rejectValue("email", "error.email");
        Mockito.reset(errors);

        // e.g. no point after host is
        instance.validateEmail("fraulyoner@verwaltungde", errors);
        Mockito.verify(errors).rejectValue("email", "error.email");
        Mockito.reset(errors);

        // e.g. structure like: xy@host, no domain
        instance.validateEmail("fraulyoner@verwaltung", errors);
        Mockito.verify(errors).rejectValue("email", "error.email");
        Mockito.reset(errors);

        // valid email addresses
        instance.validateEmail("fraulyoner@verwaltung.de", errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // valid is structure like: user@net.de
        instance.validateEmail("fraulyoner@verwaltung.com.de", errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // umlaut
        // should not result in error
        instance.validateEmail("müller@verwaltung.de", errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // ß
        // should not result in error
        instance.validateEmail("maß@mod.de", errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // special cases: strange looking, but valid
        instance.validateEmail("to#m@arbeit.de", errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // invalid String length
        instance.validateEmail("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@net.de", errors);
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
        instance.validateYear("", errors);
        Mockito.verify(errors).rejectValue("year", "error.mandatory.field");
        Mockito.reset(errors);

        instance.validateYear(null, errors);
        Mockito.verify(errors).rejectValue("year", "error.mandatory.field");
        Mockito.reset(errors);

        // error 2: string can't be parsed to int
        instance.validateYear("abc", errors);
        Mockito.verify(errors).rejectValue("year", "error.entry");
        Mockito.reset(errors);

        // error 3: year greater than +10 years
        String year = String.valueOf(DateMidnight.now().getYear() + 20);
        instance.validateYear(year, errors);
        Mockito.verify(errors).rejectValue("year", "error.entry");
        Mockito.reset(errors);

        // error 4: year smaller than -10 years
        year = String.valueOf(DateMidnight.now().getYear() - 12);
        instance.validateYear(year, errors);
        Mockito.verify(errors).rejectValue("year", "error.entry");
        Mockito.reset(errors);

        // correct: year
        year = String.valueOf(DateMidnight.now().getYear());
        instance.validateYear(year, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);
    }


    /**
     * Test of validateNumberOfDays method, of class PersonValidator.
     */
    @Test
    public void testValidateNumberOfDays() {

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

        days = BigDecimal.valueOf(25);
        instance.validateNumberOfDays(days, field, max_days, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

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
