package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.config.WorkingTimeProperties;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createWorkingTime;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.settings.FederalState.BADEN_WUERTTEMBERG;
import static org.synyx.urlaubsverwaltung.settings.FederalState.BAYERN;

@ExtendWith(MockitoExtension.class)
class WorkingTimeServiceTest {
    private final static LocalDate LOCAL_DATE = LocalDate.of(2019, 9, 13);

    private WorkingTimeService sut;

    @Mock
    private WorkingTimeProperties workingTimeProperties;
    @Mock
    private WorkingTimeRepository workingTimeRepositoryMock;
    @Mock
    private SettingsService settingsServiceMock;
    @Mock
    private Clock clock;

    @BeforeEach
    void setUp() {
        sut = new WorkingTimeService(workingTimeProperties, workingTimeRepositoryMock, settingsServiceMock, clock);
    }

    @Test
    void ensureDefaultWorkingTimeCreation() {

        final Clock fixedClock = Clock.fixed(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

        when(workingTimeProperties.getDefaultWorkingDays()).thenReturn(List.of(1, 2, 3, 4, 5));


        final Person person = createPerson();
        final WorkingTime expectedWorkingTime = new WorkingTime();
        expectedWorkingTime.setWorkingDays(List.of(1, 2, 3, 4, 5), FULL);
        expectedWorkingTime.setPerson(person);
        expectedWorkingTime.setValidFrom(LocalDate.now(clock));

        sut.createDefaultWorkingTime(person);

        ArgumentCaptor<WorkingTime> argument = ArgumentCaptor.forClass(WorkingTime.class);
        verify(workingTimeRepositoryMock).save(argument.capture());
        assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedWorkingTime);
    }

    @Test
    void ensureReturnsOverriddenFederalStateIfPersonHasSpecialFederalState() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(BADEN_WUERTTEMBERG);

        final WorkingTime workingTime = new WorkingTime();
        workingTime.setFederalStateOverride(BAYERN);
        when(workingTimeRepositoryMock.findByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(workingTime);

        final LocalDate now = LocalDate.now(UTC);
        final Person person = createPerson();
        final FederalState federalState = sut.getFederalStateForPerson(person, now);

        verifyNoInteractions(settingsServiceMock);

        assertThat(federalState).isEqualTo(BAYERN);
    }


    @Test
    void ensureReturnsSystemFederalStateIfPersonHasNoSpecialFederalState() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(BADEN_WUERTTEMBERG);
        when(settingsServiceMock.getSettings()).thenReturn(settings);

        final WorkingTime workingTime = new WorkingTime();
        workingTime.setFederalStateOverride(null);
        when(workingTimeRepositoryMock.findByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(workingTime);

        final LocalDate now = LocalDate.now(UTC);
        final Person person = createPerson();
        final FederalState federalState = sut.getFederalStateForPerson(person, now);
        assertThat(federalState).isEqualTo(BADEN_WUERTTEMBERG);
    }


    @Test
    void ensureReturnsSystemFederalStateIfPersonHasNoMatchingWorkingTime() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(BADEN_WUERTTEMBERG);
        when(settingsServiceMock.getSettings()).thenReturn(settings);

        when(workingTimeRepositoryMock.findByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(null);

        Person person = createPerson();
        LocalDate now = LocalDate.now(UTC);
        final FederalState federalState = sut.getFederalStateForPerson(person, now);
        assertThat(federalState).isEqualTo(BADEN_WUERTTEMBERG);
    }


    @Test
    void ensureSetsFederalStateOverrideIfGiven() {

        ArgumentCaptor<WorkingTime> workingTimeArgumentCaptor = ArgumentCaptor.forClass(WorkingTime.class);

        Person person = createPerson();

        sut.touch(Arrays.asList(1, 2), Optional.of(BAYERN), LocalDate.now(UTC), person);

        verify(workingTimeRepositoryMock).save(workingTimeArgumentCaptor.capture());

        WorkingTime workingTime = workingTimeArgumentCaptor.getValue();

        Optional<FederalState> optionalFederalState = workingTime.getFederalStateOverride();
        Assert.assertTrue("Missing federal state", optionalFederalState.isPresent());
        Assert.assertEquals("Wrong federal state", BAYERN, optionalFederalState.get());
    }


    @Test
    void ensureRemovesFederalStateOverrideIfNull() {

        WorkingTime existentWorkingTime = createWorkingTime();
        existentWorkingTime.setFederalStateOverride(BAYERN);

        when(workingTimeRepositoryMock.findByPersonAndValidityDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(existentWorkingTime);

        ArgumentCaptor<WorkingTime> workingTimeArgumentCaptor = ArgumentCaptor.forClass(WorkingTime.class);

        Person person = createPerson();

        sut.touch(Arrays.asList(1, 2), Optional.empty(), LocalDate.now(UTC), person);

        verify(workingTimeRepositoryMock).save(workingTimeArgumentCaptor.capture());

        WorkingTime workingTime = workingTimeArgumentCaptor.getValue();

        Assert.assertFalse("Federal state should be missing", workingTime.getFederalStateOverride().isPresent());
    }
}
