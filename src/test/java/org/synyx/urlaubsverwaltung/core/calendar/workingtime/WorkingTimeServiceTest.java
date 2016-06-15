package org.synyx.urlaubsverwaltung.core.calendar.workingtime;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Date;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class WorkingTimeServiceTest {

    private WorkingTimeService workingTimeService;

    private WorkingTimeDAO workingTimeDAOMock;
    private SettingsService settingsServiceMock;

    @Before
    public void setUp() {

        workingTimeDAOMock = Mockito.mock(WorkingTimeDAO.class);
        settingsServiceMock = Mockito.mock(SettingsService.class);

        workingTimeService = new WorkingTimeService(workingTimeDAOMock, settingsServiceMock);
    }


    @Test
    public void ensureReturnsOverriddenFederalStateIfPersonHasSpecialFederalState() {

        DateMidnight now = DateMidnight.now();

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(FederalState.BADEN_WUERTTEMBERG);

        Person person = TestDataCreator.createPerson();
        WorkingTime workingTime = new WorkingTime();
        workingTime.setFederalStateOverride(FederalState.BAYERN);

        Mockito.when(settingsServiceMock.getSettings()).thenReturn(settings);
        Mockito.when(workingTimeDAOMock.findByPersonAndValidityDateEqualsOrMinorDate(Mockito.any(Person.class),
                    Mockito.any(Date.class)))
            .thenReturn(workingTime);

        FederalState federalState = workingTimeService.getFederalStateForPerson(person, now);

        Mockito.verifyZeroInteractions(settingsServiceMock);
        Mockito.verify(workingTimeDAOMock).findByPersonAndValidityDateEqualsOrMinorDate(person, now.toDate());

        Assert.assertNotNull("Missing federal state", federalState);
        Assert.assertEquals("Wrong federal state", FederalState.BAYERN, federalState);
    }


    @Test
    public void ensureReturnsSystemFederalStateIfPersonHasNoSpecialFederalState() {

        DateMidnight now = DateMidnight.now();

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(FederalState.BADEN_WUERTTEMBERG);

        Person person = TestDataCreator.createPerson();
        WorkingTime workingTime = new WorkingTime();
        workingTime.setFederalStateOverride(null);

        Mockito.when(settingsServiceMock.getSettings()).thenReturn(settings);
        Mockito.when(workingTimeDAOMock.findByPersonAndValidityDateEqualsOrMinorDate(Mockito.any(Person.class),
                    Mockito.any(Date.class)))
            .thenReturn(workingTime);

        FederalState federalState = workingTimeService.getFederalStateForPerson(person, now);

        Mockito.verify(settingsServiceMock).getSettings();
        Mockito.verify(workingTimeDAOMock).findByPersonAndValidityDateEqualsOrMinorDate(person, now.toDate());

        Assert.assertNotNull("Missing federal state", federalState);
        Assert.assertEquals("Wrong federal state", FederalState.BADEN_WUERTTEMBERG, federalState);
    }


    @Test
    public void ensureReturnsSystemFederalStateIfPersonHasNoMatchingWorkingTime() {

        DateMidnight now = DateMidnight.now();
        Person person = TestDataCreator.createPerson();

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(FederalState.BADEN_WUERTTEMBERG);

        Mockito.when(settingsServiceMock.getSettings()).thenReturn(settings);
        Mockito.when(workingTimeDAOMock.findByPersonAndValidityDateEqualsOrMinorDate(Mockito.any(Person.class),
                    Mockito.any(Date.class)))
            .thenReturn(null);

        FederalState federalState = workingTimeService.getFederalStateForPerson(person, now);

        Mockito.verify(settingsServiceMock).getSettings();
        Mockito.verify(workingTimeDAOMock).findByPersonAndValidityDateEqualsOrMinorDate(person, now.toDate());

        Assert.assertNotNull("Missing federal state", federalState);
        Assert.assertEquals("Wrong federal state", FederalState.BADEN_WUERTTEMBERG, federalState);
    }
}
