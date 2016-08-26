package org.synyx.urlaubsverwaltung.core.settings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SettingsServiceImplTest {

    private SettingsService settingsService;

    private SettingsDAO settingsDaoMock;

    @Before
    public void setUp() {

        settingsDaoMock = Mockito.mock(SettingsDAO.class);
        settingsService = new SettingsServiceImpl(settingsDaoMock);
    }


    @Test
    public void ensureAbsenceSettingsCanBePersisted() {

        Settings settings = new Settings();
        Mockito.when(settingsDaoMock.findOne(Mockito.anyInt())).thenReturn(settings);

        AbsenceSettings absenceSettings = new AbsenceSettings();
        absenceSettings.setMaximumAnnualVacationDays(1000);

        settingsService.save(absenceSettings);

        Mockito.verify(settingsDaoMock).findOne(1);
        Mockito.verify(settingsDaoMock).save(settings);

        Assert.assertEquals("Should have been changed", absenceSettings, settings.getAbsenceSettings());
    }


    @Test
    public void ensureWorkingTimeSettingsCanBePersisted() {

        Settings settings = new Settings();
        Mockito.when(settingsDaoMock.findOne(Mockito.anyInt())).thenReturn(settings);

        WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();

        settingsService.save(workingTimeSettings);

        Mockito.verify(settingsDaoMock).findOne(1);
        Mockito.verify(settingsDaoMock).save(settings);

        Assert.assertEquals("Should have been changed", workingTimeSettings, settings.getWorkingTimeSettings());
    }


    @Test
    public void ensureMailSettingsCanBePersisted() {

        Settings settings = new Settings();
        Mockito.when(settingsDaoMock.findOne(Mockito.anyInt())).thenReturn(settings);

        MailSettings mailSettings = new MailSettings();

        settingsService.save(mailSettings);

        Mockito.verify(settingsDaoMock).findOne(1);
        Mockito.verify(settingsDaoMock).save(settings);

        Assert.assertEquals("Should have been changed", mailSettings, settings.getMailSettings());
    }


    @Test
    public void ensureCalendarSettingsCanBePersisted() {

        Settings settings = new Settings();
        Mockito.when(settingsDaoMock.findOne(Mockito.anyInt())).thenReturn(settings);

        CalendarSettings calendarSettings = new CalendarSettings();

        settingsService.save(calendarSettings);

        Mockito.verify(settingsDaoMock).findOne(1);
        Mockito.verify(settingsDaoMock).save(settings);

        Assert.assertEquals("Should have been changed", calendarSettings, settings.getCalendarSettings());
    }
}
