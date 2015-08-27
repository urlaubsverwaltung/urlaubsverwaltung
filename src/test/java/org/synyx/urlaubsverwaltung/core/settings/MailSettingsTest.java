package org.synyx.urlaubsverwaltung.core.settings;

import org.junit.Assert;
import org.junit.Test;


/**
 * Unit test for {@link MailSettings}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
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

        Assert.assertFalse("Should be inactive", mailSettings.isActive());
        Assert.assertEquals("Wrong host", "localhost", mailSettings.getHost());
        Assert.assertEquals("Wrong port", (Integer) 25, mailSettings.getPort());
        Assert.assertEquals("Wrong admin mail address", "admin@uv.de", mailSettings.getAdministrator());
        Assert.assertEquals("Wrong from mail address", "absender@uv.de", mailSettings.getFrom());
    }
}
