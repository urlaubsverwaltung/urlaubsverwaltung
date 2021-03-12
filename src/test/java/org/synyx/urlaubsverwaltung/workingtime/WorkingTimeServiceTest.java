package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createWorkingTime;
import static org.synyx.urlaubsverwaltung.period.WeekDay.FRIDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.MONDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.THURSDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.TUESDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.WEDNESDAY;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BADEN_WUERTTEMBERG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BAYERN;

@ExtendWith(MockitoExtension.class)
class WorkingTimeServiceTest {

    private WorkingTimeService sut;

    @Mock
    private WorkingTimeProperties workingTimeProperties;
    @Mock
    private WorkingTimeRepository workingTimeRepository;
    @Mock
    private SettingsService settingsService;

    private final Clock fixedClock = Clock.fixed(Instant.parse("2019-08-13T00:00:00.00Z"), UTC);

    @BeforeEach
    void setUp() {
        sut = new WorkingTimeService(workingTimeProperties, workingTimeRepository, settingsService, fixedClock);
    }

    @Test
    void ensureDefaultWorkingTimeCreationFromProperties() {

        when(workingTimeProperties.isDefaultWorkingDaysDeactivated()).thenReturn(false);
        when(workingTimeProperties.getDefaultWorkingDays()).thenReturn(List.of(1, 2, 3, 4, 5));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        sut.createDefaultWorkingTime(person);

        final ArgumentCaptor<WorkingTime> argument = ArgumentCaptor.forClass(WorkingTime.class);
        verify(workingTimeRepository).save(argument.capture());
        final WorkingTime workingTime = argument.getValue();
        assertThat(workingTime.getPerson()).isEqualTo(person);
        assertThat(workingTime.getValidFrom()).isEqualTo(LocalDate.now(fixedClock));
        assertThat(workingTime.getWorkingDays()).isEqualTo(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY));
    }

    @Test
    void ensureDefaultWorkingTimeCreationFromGui() {

        when(workingTimeProperties.isDefaultWorkingDaysDeactivated()).thenReturn(true);

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setMonday(DayLength.ZERO);
        settings.getWorkingTimeSettings().setTuesday(DayLength.ZERO);
        settings.getWorkingTimeSettings().setWednesday(DayLength.ZERO);
        settings.getWorkingTimeSettings().setThursday(DayLength.ZERO);
        settings.getWorkingTimeSettings().setFriday(DayLength.FULL);
        settings.getWorkingTimeSettings().setSaturday(DayLength.ZERO);
        settings.getWorkingTimeSettings().setSunday(DayLength.ZERO);
        when(settingsService.getSettings()).thenReturn(settings);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        sut.createDefaultWorkingTime(person);

        final ArgumentCaptor<WorkingTime> argument = ArgumentCaptor.forClass(WorkingTime.class);
        verify(workingTimeRepository).save(argument.capture());
        final WorkingTime workingTime = argument.getValue();
        assertThat(workingTime.getPerson()).isEqualTo(person);
        assertThat(workingTime.getValidFrom()).isEqualTo(LocalDate.now(fixedClock));
        assertThat(workingTime.getWorkingDays()).isEqualTo(List.of(FRIDAY));
    }

    @Test
    void ensureReturnsOverriddenFederalStateIfPersonHasSpecialFederalState() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(BADEN_WUERTTEMBERG);

        final WorkingTime workingTime = new WorkingTime();
        workingTime.setFederalStateOverride(BAYERN);
        when(workingTimeRepository.findByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(workingTime);

        final LocalDate now = LocalDate.now(UTC);
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final FederalState federalState = sut.getFederalStateForPerson(person, now);

        verifyNoInteractions(settingsService);

        assertThat(federalState).isEqualTo(BAYERN);
    }

    @Test
    void ensureReturnsSystemFederalStateIfPersonHasNoSpecialFederalState() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(BADEN_WUERTTEMBERG);
        when(settingsService.getSettings()).thenReturn(settings);

        final WorkingTime workingTime = new WorkingTime();
        workingTime.setFederalStateOverride(null);
        when(workingTimeRepository.findByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(workingTime);

        final LocalDate now = LocalDate.now(UTC);
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final FederalState federalState = sut.getFederalStateForPerson(person, now);
        assertThat(federalState).isEqualTo(BADEN_WUERTTEMBERG);
    }

    @Test
    void ensureReturnsSystemFederalStateIfPersonHasNoMatchingWorkingTime() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(BADEN_WUERTTEMBERG);
        when(settingsService.getSettings()).thenReturn(settings);

        when(workingTimeRepository.findByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(null);

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        LocalDate now = LocalDate.now(UTC);
        final FederalState federalState = sut.getFederalStateForPerson(person, now);
        assertThat(federalState).isEqualTo(BADEN_WUERTTEMBERG);
    }

    @Test
    void ensureSetsFederalStateOverrideIfGiven() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        sut.touch(List.of(1, 2), Optional.of(BAYERN), LocalDate.now(UTC), person);

        final ArgumentCaptor<WorkingTime> workingTimeArgumentCaptor = ArgumentCaptor.forClass(WorkingTime.class);
        verify(workingTimeRepository).save(workingTimeArgumentCaptor.capture());
        final WorkingTime workingTime = workingTimeArgumentCaptor.getValue();
        assertThat(workingTime.getFederalStateOverride()).hasValue(BAYERN);
    }

    @Test
    void ensureRemovesFederalStateOverrideIfNull() {

        final WorkingTime existentWorkingTime = createWorkingTime();
        existentWorkingTime.setFederalStateOverride(BAYERN);

        when(workingTimeRepository.findByPersonAndValidityDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(existentWorkingTime);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        sut.touch(List.of(1, 2), Optional.empty(), LocalDate.now(UTC), person);

        final ArgumentCaptor<WorkingTime> workingTimeArgumentCaptor = ArgumentCaptor.forClass(WorkingTime.class);
        verify(workingTimeRepository).save(workingTimeArgumentCaptor.capture());

        final WorkingTime workingTime = workingTimeArgumentCaptor.getValue();
        assertThat(workingTime.getFederalStateOverride()).isEmpty();
    }

    @Test
    void ensureGetByPersonsAndDateInterval() {
        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final List<Person> persons = List.of(person);

        final LocalDate start = LocalDate.now();
        final LocalDate end = start.plusDays(1);

        sut.getByPersonsAndDateInterval(persons, start, end);

        verify(workingTimeRepository).findByPersonInAndValidFromForDateInterval(persons, start, end);
    }

    @Test
    void getByPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final WorkingTime workingTime = new WorkingTime();
        when(workingTimeRepository.findByPersonOrderByValidFromDesc(person)).thenReturn(List.of(workingTime));

        final List<WorkingTime> workingTimes = sut.getByPerson(person);
        assertThat(workingTimes)
            .hasSize(1)
            .contains(workingTime);
    }
}
