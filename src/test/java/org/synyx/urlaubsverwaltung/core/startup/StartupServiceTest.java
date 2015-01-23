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


    @Test(expected = RuntimeException.class)
    public void ensureThrowsIfInvalidSpringProfileSet() {

        System.getProperties().put("spring.profiles.active", "foo");

        new StartupService("myDbUser", "myDbUrl", "manager@uv.de");
    }


    @Test
    public void ensureSystemInformationIsLoggedCorrectly() {

        System.getProperties().put("spring.profiles.active", "ldap");

        StartupService startupService = new StartupService("myDbUser", "myDbUrl", "manager@uv.de");

        ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(LoggingEvent.class);

        startupService.logStartupInfo();

        Mockito.verify(mockedLogAppender, Mockito.times(4)).doAppend(loggingEventArgumentCaptor.capture());

        List<LoggingEvent> loggingEvents = loggingEventArgumentCaptor.getAllValues();

        Assert.assertNotNull("There should be logging events", loggingEvents);
        Assert.assertEquals("Wrong number of logging events", 4, loggingEvents.size());

        for (LoggingEvent loggingEvent : loggingEvents) {
            Assert.assertEquals("Wrong log level", Level.INFO, loggingEvent.getLevel());
        }

        Assert.assertEquals("Wrong log message", "DATABASE = myDbUrl", loggingEvents.get(0).getRenderedMessage());
        Assert.assertEquals("Wrong log message", "DATABASE USER = myDbUser", loggingEvents.get(1).getRenderedMessage());
        Assert.assertEquals("Wrong log message", "APPLICATION MANAGER EMAIL = manager@uv.de",
            loggingEvents.get(2).getRenderedMessage());
        Assert.assertEquals("Wrong log message", "ACTIVE SPRING PROFILE = ldap",
            loggingEvents.get(3).getRenderedMessage());
    }
}
