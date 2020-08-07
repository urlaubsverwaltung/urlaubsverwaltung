package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;
import org.synyx.urlaubsverwaltung.workingtime.config.WorkingTimeProperties;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

@RunWith(MockitoJUnitRunner.class)
public class WorkingTimeServiceTest {

    @Mock
    private WorkingTimeProperties workingTimeProperties;

    @Mock
    private WorkingTimeDAO workingTimeDAOMock;

    @Mock
    private SettingsService settingsServiceMock;

    @Mock
    private Clock clock;

    private WorkingTimeService workingTimeService;

    @Before
    public void setUp() {

        Clock fixedClock = Clock.fixed(Instant.parse("2019-08-13T00:00:00.00Z"), Clock.systemUTC().getZone());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

        when(workingTimeProperties.getDefaultWorkingDays()).thenReturn(List.of(1, 2, 3, 4, 5));

        workingTimeService = new WorkingTimeService(workingTimeProperties, workingTimeDAOMock, settingsServiceMock, clock);
    }

    @Test
    public void ensureDefaultWorkingTimeCreation() {
        ArgumentCaptor<WorkingTime> argument = ArgumentCaptor.forClass(WorkingTime.class);

        Person person = TestDataCreator.createPerson();
        WorkingTime expectedWorkingTime = new WorkingTime();
        expectedWorkingTime.setWorkingDays(List.of(1, 2, 3, 4, 5), FULL);
        expectedWorkingTime.setPerson(person);
        expectedWorkingTime.setValidFrom(LocalDate.now(clock));

        workingTimeService.createDefaultWorkingTime(person);

        verify(workingTimeProperties).getDefaultWorkingDays();
        verify(workingTimeDAOMock).save(argument.capture());
        assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedWorkingTime);
    }

    @Test
    public void ensureReturnsOverriddenFederalStateIfPersonHasSpecialFederalState() {

        Instant now = Instant.now();

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(FederalState.BADEN_WUERTTEMBERG);

        Person person = TestDataCreator.createPerson();
        WorkingTime workingTime = new WorkingTime();
        workingTime.setFederalStateOverride(FederalState.BAYERN);

        when(workingTimeDAOMock.findByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(Instant.class)))
            .thenReturn(workingTime);

        FederalState federalState = workingTimeService.getFederalStateForPerson(person, now);

        verifyZeroInteractions(settingsServiceMock);
        verify(workingTimeDAOMock).findByPersonAndValidityDateEqualsOrMinorDate(person, now);

        Assert.assertNotNull("Missing federal state", federalState);
        Assert.assertEquals("Wrong federal state", FederalState.BAYERN, federalState);
    }


    @Test
    public void ensureReturnsSystemFederalStateIfPersonHasNoSpecialFederalState() {

        Instant now = Instant.now();

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(FederalState.BADEN_WUERTTEMBERG);

        Person person = TestDataCreator.createPerson();
        WorkingTime workingTime = new WorkingTime();
        workingTime.setFederalStateOverride(null);

        when(settingsServiceMock.getSettings()).thenReturn(settings);
        when(workingTimeDAOMock.findByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(Instant.class)))
            .thenReturn(workingTime);

        FederalState federalState = workingTimeService.getFederalStateForPerson(person, now);

        verify(settingsServiceMock).getSettings();
        verify(workingTimeDAOMock).findByPersonAndValidityDateEqualsOrMinorDate(person, now);

        Assert.assertNotNull("Missing federal state", federalState);
        Assert.assertEquals("Wrong federal statecheckCalendarSyncSettingsNoExceptionForEmptyEmail", FederalState.BADEN_WUERTTEMBERG, federalState);
    }


    @Test
    public void ensureReturnsSystemFederalStateIfPersonHasNoMatchingWorkingTime() {

        Instant now = Instant.now();
        Person person = TestDataCreator.createPerson();

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(FederalState.BADEN_WUERTTEMBERG);

        when(settingsServiceMock.getSettings()).thenReturn(settings);
        when(workingTimeDAOMock.findByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(Instant.class)))
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

        workingTimeService.touch(Arrays.asList(1, 2), Optional.of(FederalState.BAYERN), LocalDate.now(UTC), person);

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

        workingTimeService.touch(Arrays.asList(1, 2), Optional.empty(), LocalDate.now(UTC), person);

        verify(workingTimeDAOMock).save(workingTimeArgumentCaptor.capture());

        WorkingTime workingTime = workingTimeArgumentCaptor.getValue();

        Assert.assertFalse("Federal state should be missing", workingTime.getFederalStateOverride().isPresent());
    }
}
