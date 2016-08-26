package org.synyx.urlaubsverwaltung.web.settings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.settings.MailSettings;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class MailSettingsValidatorTest {

    private Validator validator;

    @Before
    public void setUp() throws Exception {

        validator = new MailSettingsValidator();
    }


    // Supported class -------------------------------------------------------------------------------------------------

    @Test
    public void ensureMailSettingsClassIsSupported() throws Exception {

        Assert.assertTrue("Should support MailSettings class", validator.supports(MailSettings.class));
    }


    @Test
    public void ensureOtherClassThanMailSettingsIsNotSupported() throws Exception {

        Assert.assertFalse("Should not support other classes", validator.supports(Object.class));
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThatValidateFailsWithOtherClassThanMailSettings() throws Exception {

        Object o = new Object();
        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(o, mockError);
    }


    // Validation ------------------------------------------------------------------------------------------------------

    @Test
    public void ensureMailSettingsAreNotMandatoryIfDeactivated() {

        MailSettings mailSettings = new MailSettings();
        mailSettings.setActive(false);
        mailSettings.setHost(null);
        mailSettings.setPort(null);
        mailSettings.setAdministrator(null);
        mailSettings.setFrom(null);
        mailSettings.setBaseLinkURL(null);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(mailSettings, mockError);

        Mockito.verifyZeroInteractions(mockError);
    }


    @Test
    public void ensureMandatoryMailSettingsAreMandatoryIfActivated() {

        MailSettings mailSettings = new MailSettings();
        mailSettings.setActive(true);
        mailSettings.setHost(null);
        mailSettings.setPort(null);
        mailSettings.setAdministrator(null);
        mailSettings.setFrom(null);
        mailSettings.setBaseLinkURL(null);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(mailSettings, mockError);
        Mockito.verify(mockError).rejectValue("host", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("port", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("administrator", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("from", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("baseLinkURL", "error.entry.mandatory");
    }


    @Test
    public void ensureFromAndAdministratorMailAddressesMustBeValid() {

        MailSettings mailSettings = new MailSettings();
        mailSettings.setActive(true);
        mailSettings.setAdministrator("foo");
        mailSettings.setFrom("bar");

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(mailSettings, mockError);
        Mockito.verify(mockError).rejectValue("administrator", "error.entry.mail");
        Mockito.verify(mockError).rejectValue("from", "error.entry.mail");
    }


    @Test
    public void ensureMailPortMustBeNotNegative() {

        MailSettings mailSettings = new MailSettings();
        mailSettings.setActive(true);
        mailSettings.setPort(-1);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(mailSettings, mockError);
        Mockito.verify(mockError).rejectValue("port", "error.entry.invalid");
    }


    @Test
    public void ensureMailPortMustBeGreaterThanZero() {

        MailSettings mailSettings = new MailSettings();
        mailSettings.setActive(true);
        mailSettings.setPort(0);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(mailSettings, mockError);
        Mockito.verify(mockError).rejectValue("port", "error.entry.invalid");
    }
}
