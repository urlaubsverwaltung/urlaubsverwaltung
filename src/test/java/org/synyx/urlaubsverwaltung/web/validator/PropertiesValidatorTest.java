/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.web.validator;

import org.joda.time.DateMidnight;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.web.application.AppForm;

import java.util.Properties;


/**
 * @author  Aljona Murygina
 */
public class PropertiesValidatorTest {

    // property keys (custom properties)
    private static final String MAX_DAYS = "annual.vacation.max";
    private static final String MAX_MONTHS = "maximum.months";

    // error messages (messages properties)
    private static final String ERROR_STH_WRONG = "error.sth.went.wrong";
    private static final String ERROR_TOO_LONG = "error.too.long";

    private PropertiesValidator instance;
    Errors errors = Mockito.mock(Errors.class);

    private MailService mailService = Mockito.mock(MailService.class);

    public PropertiesValidatorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {

        instance = new PropertiesValidator(mailService);
        Mockito.reset(errors);
        Mockito.reset(mailService);
    }


    @After
    public void tearDown() {
    }


    /**
     * Test of validateAnnualVacationProperty method, of class PropertiesValidator.
     */
    @Test
    public void testValidateAnnualVacationProperty() {

        Properties testProperties = new Properties();

        // INVALID ENTRY

        // alphabetic character
        testProperties.setProperty(MAX_DAYS, "a");
        instance.validateAnnualVacationProperty(testProperties, errors);
        Mockito.verify(errors).reject(ERROR_STH_WRONG);
        Mockito.verify(mailService).sendPropertiesErrorNotification(MAX_DAYS);
        Mockito.reset(errors);
        Mockito.reset(mailService);

        // zero
        testProperties.setProperty(MAX_DAYS, "0");
        instance.validateAnnualVacationProperty(testProperties, errors);
        Mockito.verify(errors).reject(ERROR_STH_WRONG);
        Mockito.verify(mailService).sendPropertiesErrorNotification(MAX_DAYS);
        Mockito.reset(errors);
        Mockito.reset(mailService);

        // negative number
        testProperties.setProperty(MAX_DAYS, "-1");
        instance.validateAnnualVacationProperty(testProperties, errors);
        Mockito.verify(errors).reject(ERROR_STH_WRONG);
        Mockito.verify(mailService).sendPropertiesErrorNotification(MAX_DAYS);
        Mockito.reset(errors);
        Mockito.reset(mailService);

        // absurd number
        testProperties.setProperty(MAX_DAYS, "367");
        instance.validateAnnualVacationProperty(testProperties, errors);
        Mockito.verify(errors).reject(ERROR_STH_WRONG);
        Mockito.verify(mailService).sendPropertiesErrorNotification(MAX_DAYS);
        Mockito.reset(errors);
        Mockito.reset(mailService);

        // VALID ENTRY

        testProperties.setProperty(MAX_DAYS, "365");
        instance.validateAnnualVacationProperty(testProperties, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.verifyZeroInteractions(mailService);
        Mockito.reset(errors);
        Mockito.reset(mailService);
    }


    /**
     * Test of validateMaximumVacationProperty method, of class PropertiesValidator.
     */
    @Test
    public void testValidateMaximumVacationProperty() {

        AppForm app = new AppForm();

        Properties testProperties = new Properties();

        // INVALID ENTRY

        // alphabetic character
        testProperties.setProperty(MAX_MONTHS, "a");
        instance.validateMaximumVacationProperty(testProperties, app, errors);
        Mockito.verify(errors).reject(ERROR_STH_WRONG);
        Mockito.verify(mailService).sendPropertiesErrorNotification(MAX_MONTHS);
        Mockito.reset(errors);
        Mockito.reset(mailService);

        testProperties.setProperty(MAX_MONTHS, "&$");
        instance.validateMaximumVacationProperty(testProperties, app, errors);
        Mockito.verify(errors).reject(ERROR_STH_WRONG);
        Mockito.verify(mailService).sendPropertiesErrorNotification(MAX_MONTHS);
        Mockito.reset(errors);
        Mockito.reset(mailService);

        // zero
        testProperties.setProperty(MAX_MONTHS, "0");
        instance.validateMaximumVacationProperty(testProperties, app, errors);
        Mockito.verify(errors).reject(ERROR_STH_WRONG);
        Mockito.verify(mailService).sendPropertiesErrorNotification(MAX_MONTHS);
        Mockito.reset(errors);
        Mockito.reset(mailService);

        // negative number
        testProperties.setProperty(MAX_MONTHS, "-1");
        instance.validateMaximumVacationProperty(testProperties, app, errors);
        Mockito.verify(errors).reject(ERROR_STH_WRONG);
        Mockito.verify(mailService).sendPropertiesErrorNotification(MAX_MONTHS);
        Mockito.reset(errors);
        Mockito.reset(mailService);

        // absurd number - too big
        testProperties.setProperty(MAX_MONTHS, "37");
        instance.validateMaximumVacationProperty(testProperties, app, errors);
        Mockito.verify(errors).reject(ERROR_STH_WRONG);
        Mockito.verify(mailService).sendPropertiesErrorNotification(MAX_MONTHS);
        Mockito.reset(errors);
        Mockito.reset(mailService);

        // absurd number - decimal possibility 1 (point)
        testProperties.setProperty(MAX_MONTHS, "12.5");
        instance.validateMaximumVacationProperty(testProperties, app, errors);
        Mockito.verify(errors).reject(ERROR_STH_WRONG);
        Mockito.verify(mailService).sendPropertiesErrorNotification(MAX_MONTHS);
        Mockito.reset(errors);
        Mockito.reset(mailService);

        // absurd number - decimal possibility 2 (comma)
        testProperties.setProperty(MAX_MONTHS, "12,5");
        instance.validateMaximumVacationProperty(testProperties, app, errors);
        Mockito.verify(errors).reject(ERROR_STH_WRONG);
        Mockito.verify(mailService).sendPropertiesErrorNotification(MAX_MONTHS);
        Mockito.reset(errors);
        Mockito.reset(mailService);

        // VALID ENTRY
        testProperties.setProperty(MAX_MONTHS, "12");

        // application's end date is after maximum permissible period of applying for leave
        app.setEndDate(DateMidnight.now().plusMonths(13));
        instance.validateMaximumVacationProperty(testProperties, app, errors);
        Mockito.verify(errors).reject(ERROR_TOO_LONG);
        Mockito.verifyZeroInteractions(mailService);
        Mockito.reset(errors);
        Mockito.reset(mailService);

        // application's end date is before maximum permissible period of applying for leave
        app.setEndDate(DateMidnight.now().plusMonths(6));
        instance.validateMaximumVacationProperty(testProperties, app, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.verifyZeroInteractions(mailService);
        Mockito.reset(errors);
        Mockito.reset(mailService);
    }
}
