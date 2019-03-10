package org.synyx.urlaubsverwaltung.core.workingtime;

import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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

        when(settingsServiceMock.getSettings()).thenReturn(settings);
        when(workingTimeDAOMock.findByPersonAndValidityDateEqualsOrMinorDate(Mockito.any(Person.class),
                    Mockito.any(Date.class)))
            .thenReturn(workingTime);

        FederalState federalState = workingTimeService.getFederalStateForPerson(person, now);

        Mockito.verifyZeroInteractions(settingsServiceMock);
        verify(workingTimeDAOMock).findByPersonAndValidityDateEqualsOrMinorDate(person, now.toDate());

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

        when(settingsServiceMock.getSettings()).thenReturn(settings);
        when(workingTimeDAOMock.findByPersonAndValidityDateEqualsOrMinorDate(Mockito.any(Person.class),
                    Mockito.any(Date.class)))
            .thenReturn(workingTime);

        FederalState federalState = workingTimeService.getFederalStateForPerson(person, now);

        verify(settingsServiceMock).getSettings();
        verify(workingTimeDAOMock).findByPersonAndValidityDateEqualsOrMinorDate(person, now.toDate());

        Assert.assertNotNull("Missing federal state", federalState);
        Assert.assertEquals("Wrong federal state", FederalState.BADEN_WUERTTEMBERG, federalState);
    }


    @Test
    public void ensureReturnsSystemFederalStateIfPersonHasNoMatchingWorkingTime() {

        DateMidnight now = DateMidnight.now();
        Person person = TestDataCreator.createPerson();

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(FederalState.BADEN_WUERTTEMBERG);

        when(settingsServiceMock.getSettings()).thenReturn(settings);
        when(workingTimeDAOMock.findByPersonAndValidityDateEqualsOrMinorDate(Mockito.any(Person.class),
                    Mockito.any(Date.class)))
            .thenReturn(null);

        FederalState federalState = workingTimeService.getFederalStateForPerson(person, now);

        verify(settingsServiceMock).getSettings();
        verify(workingTimeDAOMock).findByPersonAndValidityDateEqualsOrMinorDate(person, now.toDate());

        Assert.assertNotNull("Missing federal state", federalState);
        Assert.assertEquals("Wrong federal state", FederalState.BADEN_WUERTTEMBERG, federalState);
    }


    @Test
    public void ensureSetsFederalStateOverrideIfGiven() {

        ArgumentCaptor<WorkingTime> workingTimeArgumentCaptor = ArgumentCaptor.forClass(WorkingTime.class);

        Person person = TestDataCreator.createPerson();

        workingTimeService.touch(Arrays.asList(1, 2), Optional.of(FederalState.BAYERN), DateMidnight.now(), person);

        verify(workingTimeDAOMock).save(workingTimeArgumentCaptor.capture());

        WorkingTime workingTime = workingTimeArgumentCaptor.getValue();

        Optional<FederalState> optionalFederalState = workingTime.getFederalStateOverride();
        Assert.assertTrue("Missing federal state", optionalFederalState.isPresent());
        Assert.assertEquals("Wrong federal state", FederalState.BAYERN, optionalFederalState.get());
    }


    @Test
    public void ensureRemovesFederalStateOverrideIfNull() {

        WorkingTime existentWorkingTime = TestDataCreator.createWorkingTime();
        existentWorkingTime.setFederalStateOverride(FederalState.BAYERN);

        when(workingTimeDAOMock.findByPersonAndValidityDate(Mockito.any(Person.class), Mockito.any(Date.class)))
            .thenReturn(existentWorkingTime);

        ArgumentCaptor<WorkingTime> workingTimeArgumentCaptor = ArgumentCaptor.forClass(WorkingTime.class);

        Person person = TestDataCreator.createPerson();

        workingTimeService.touch(Arrays.asList(1, 2), Optional.empty(), DateMidnight.now(), person);

        verify(workingTimeDAOMock).save(workingTimeArgumentCaptor.capture());

        WorkingTime workingTime = workingTimeArgumentCaptor.getValue();

        Assert.assertFalse("Federal state should be missing", workingTime.getFederalStateOverride().isPresent());
    }
}
