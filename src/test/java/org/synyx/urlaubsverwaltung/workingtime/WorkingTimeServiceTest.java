package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


public class WorkingTimeServiceTest {

    private WorkingTimeService workingTimeService;

    private WorkingTimeDAO workingTimeDAOMock;
    private SettingsService settingsServiceMock;

    @Before
    public void setUp() {

        workingTimeDAOMock = mock(WorkingTimeDAO.class);
        settingsServiceMock = mock(SettingsService.class);

        workingTimeService = new WorkingTimeService(workingTimeDAOMock, settingsServiceMock);
    }


    @Test
    public void ensureReturnsOverriddenFederalStateIfPersonHasSpecialFederalState() {

        LocalDate now = ZonedDateTime.now(UTC).toLocalDate();

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(FederalState.BADEN_WUERTTEMBERG);

        Person person = TestDataCreator.createPerson();
        WorkingTime workingTime = new WorkingTime();
        workingTime.setFederalStateOverride(FederalState.BAYERN);

        when(settingsServiceMock.getSettings()).thenReturn(settings);
        when(workingTimeDAOMock.findByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(workingTime);

        FederalState federalState = workingTimeService.getFederalStateForPerson(person, now);

        verifyZeroInteractions(settingsServiceMock);
        verify(workingTimeDAOMock).findByPersonAndValidityDateEqualsOrMinorDate(person, now);

        Assert.assertNotNull("Missing federal state", federalState);
        Assert.assertEquals("Wrong federal state", FederalState.BAYERN, federalState);
    }


    @Test
    public void ensureReturnsSystemFederalStateIfPersonHasNoSpecialFederalState() {

        LocalDate now = ZonedDateTime.now(UTC).toLocalDate();

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(FederalState.BADEN_WUERTTEMBERG);

        Person person = TestDataCreator.createPerson();
        WorkingTime workingTime = new WorkingTime();
        workingTime.setFederalStateOverride(null);

        when(settingsServiceMock.getSettings()).thenReturn(settings);
        when(workingTimeDAOMock.findByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(workingTime);

        FederalState federalState = workingTimeService.getFederalStateForPerson(person, now);

        verify(settingsServiceMock).getSettings();
        verify(workingTimeDAOMock).findByPersonAndValidityDateEqualsOrMinorDate(person, now);

        Assert.assertNotNull("Missing federal state", federalState);
        Assert.assertEquals("Wrong federal statecheckCalendarSyncSettingsNoExceptionForEmptyEmail", FederalState.BADEN_WUERTTEMBERG, federalState);
    }


    @Test
    public void ensureReturnsSystemFederalStateIfPersonHasNoMatchingWorkingTime() {

        LocalDate now = ZonedDateTime.now(UTC).toLocalDate();
        Person person = TestDataCreator.createPerson();

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(FederalState.BADEN_WUERTTEMBERG);

        when(settingsServiceMock.getSettings()).thenReturn(settings);
        when(workingTimeDAOMock.findByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(null);

        FederalState federalState = workingTimeService.getFederalStateForPerson(person, now);

        verify(settingsServiceMock).getSettings();
        verify(workingTimeDAOMock).findByPersonAndValidityDateEqualsOrMinorDate(person, now);

        Assert.assertNotNull("Missing federal state", federalState);
        Assert.assertEquals("Wrong federal state", FederalState.BADEN_WUERTTEMBERG, federalState);
    }


    @Test
    public void ensureSetsFederalStateOverrideIfGiven() {

        ArgumentCaptor<WorkingTime> workingTimeArgumentCaptor = ArgumentCaptor.forClass(WorkingTime.class);

        Person person = TestDataCreator.createPerson();

        workingTimeService.touch(Arrays.asList(1, 2), Optional.of(FederalState.BAYERN), ZonedDateTime.now(UTC).toLocalDate(), person);

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

        when(workingTimeDAOMock.findByPersonAndValidityDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(existentWorkingTime);

        ArgumentCaptor<WorkingTime> workingTimeArgumentCaptor = ArgumentCaptor.forClass(WorkingTime.class);

        Person person = TestDataCreator.createPerson();

        workingTimeService.touch(Arrays.asList(1, 2), Optional.empty(), ZonedDateTime.now(UTC).toLocalDate(), person);

        verify(workingTimeDAOMock).save(workingTimeArgumentCaptor.capture());

        WorkingTime workingTime = workingTimeArgumentCaptor.getValue();

        Assert.assertFalse("Federal state should be missing", workingTime.getFederalStateOverride().isPresent());
    }
}
