package org.synyx.urlaubsverwaltung.core.startup;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.security.Authentication;

import java.util.List;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.startup.StartupService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class StartupServiceTest {

    private Appender mockedLogAppender;

    @Before
    public void setUp() {

        mockedLogAppender = Mockito.mock(Appender.class);

        LogManager.getRootLogger().addAppender(mockedLogAppender);
    }


    @After
    public void tearDown() {

        LogManager.getRootLogger().removeAppender(mockedLogAppender);
    }


    @Test
    public void ensureSystemInformationIsLoggedCorrectly() {

        System.getProperties().put(Authentication.PROPERTY_KEY, Authentication.Type.LDAP.getName());
        System.getProperties().put(Environment.PROPERTY_KEY, Environment.Type.TEST.getName());

        StartupService startupService = new StartupService("myDbUser", "myDbUrl");

        ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(LoggingEvent.class);

        startupService.logStartupInfo();

        Mockito.verify(mockedLogAppender, Mockito.times(4)).doAppend(loggingEventArgumentCaptor.capture());

        List<LoggingEvent> loggingEvents = loggingEventArgumentCaptor.getAllValues();

        Assert.assertNotNull("There should be logging events", loggingEvents);
        Assert.assertEquals("Wrong number of logging events", 4, loggingEvents.size());

        for (LoggingEvent loggingEvent : loggingEvents) {
            Assert.assertEquals("Wrong log level", Level.INFO, loggingEvent.getLevel());
        }

        Assert.assertEquals("Wrong log message", "DATABASE=myDbUrl", loggingEvents.get(0).getRenderedMessage());
        Assert.assertEquals("Wrong log message", "DATABASE USER=myDbUser", loggingEvents.get(1).getRenderedMessage());
        Assert.assertEquals("Wrong log message", "AUTHENTICATION=ldap", loggingEvents.get(2).getRenderedMessage());
        Assert.assertEquals("Wrong log message", "ENVIRONMENT=test", loggingEvents.get(3).getRenderedMessage());
    }
}
