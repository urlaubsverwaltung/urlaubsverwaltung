package org.synyx.urlaubsverwaltung.settings;

import org.junit.Assert;
import org.junit.Test;


/**
 * Unit test for {@link MailSettings}.
 */
public class MailSettingsTest {

    @Test
    public void ensureHasSomeDefaultValues() {

        MailSettings mailSettings = new MailSettings();

        // Fields without default values
        Assert.assertNull("Username should not be set", mailSettings.getUsername());
        Assert.assertNull("Password should not be set", mailSettings.getPassword());

        // Fields with default values
        Assert.assertNotNull("Host should be set", mailSettings.getHost());
        Assert.assertNotNull("Port should be set", mailSettings.getPort());
        Assert.assertNotNull("Admin mail address should be set", mailSettings.getAdministrator());
        Assert.assertNotNull("From mail address should be set", mailSettings.getFrom());
        Assert.assertNotNull("Base link URL should be set", mailSettings.getBaseLinkURL());

        Assert.assertFalse("Should be inactive", mailSettings.isActive());
        Assert.assertEquals("Wrong host", "localhost", mailSettings.getHost());
        Assert.assertEquals("Wrong port", (Integer) 25, mailSettings.getPort());
        Assert.assertEquals("Wrong admin mail address", "admin@urlaubsverwaltung.test", mailSettings.getAdministrator());
        Assert.assertEquals("Wrong from mail address", "absender@urlaubsverwaltung.test", mailSettings.getFrom());
        Assert.assertEquals("Wrong base link URL", "http://localhost:8080/", mailSettings.getBaseLinkURL());
    }
}
