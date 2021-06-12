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

import static java.time.Month.JUNE;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
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

        final ArgumentCaptor<WorkingTimeEntity> argument = ArgumentCaptor.forClass(WorkingTimeEntity.class);
        verify(workingTimeRepository).save(argument.capture());

        final WorkingTimeEntity persistedWorkingTimeEntity = argument.getValue();
        assertThat(persistedWorkingTimeEntity.getPerson()).isEqualTo(person);
        assertThat(persistedWorkingTimeEntity.getValidFrom()).isEqualTo(LocalDate.now(fixedClock));
        assertThat(persistedWorkingTimeEntity.getMonday()).isEqualTo(DayLength.FULL);
        assertThat(persistedWorkingTimeEntity.getTuesday()).isEqualTo(DayLength.FULL);
        assertThat(persistedWorkingTimeEntity.getWednesday()).isEqualTo(DayLength.FULL);
        assertThat(persistedWorkingTimeEntity.getThursday()).isEqualTo(DayLength.FULL);
        assertThat(persistedWorkingTimeEntity.getFriday()).isEqualTo(DayLength.FULL);
        assertThat(persistedWorkingTimeEntity.getSaturday()).isEqualTo(DayLength.ZERO);
        assertThat(persistedWorkingTimeEntity.getSunday()).isEqualTo(DayLength.ZERO);
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

        final ArgumentCaptor<WorkingTimeEntity> argument = ArgumentCaptor.forClass(WorkingTimeEntity.class);
        verify(workingTimeRepository).save(argument.capture());

        final WorkingTimeEntity persistedWorkingTimeEntity = argument.getValue();
        assertThat(persistedWorkingTimeEntity.getPerson()).isEqualTo(person);
        assertThat(persistedWorkingTimeEntity.getValidFrom()).isEqualTo(LocalDate.now(fixedClock));
        assertThat(persistedWorkingTimeEntity.getMonday()).isEqualTo(DayLength.ZERO);
        assertThat(persistedWorkingTimeEntity.getTuesday()).isEqualTo(DayLength.ZERO);
        assertThat(persistedWorkingTimeEntity.getWednesday()).isEqualTo(DayLength.ZERO);
        assertThat(persistedWorkingTimeEntity.getThursday()).isEqualTo(DayLength.ZERO);
        assertThat(persistedWorkingTimeEntity.getFriday()).isEqualTo(DayLength.FULL);
        assertThat(persistedWorkingTimeEntity.getSaturday()).isEqualTo(DayLength.ZERO);
        assertThat(persistedWorkingTimeEntity.getSunday()).isEqualTo(DayLength.ZERO);
    }

    @Test
    void ensureReturnsOverriddenFederalStateIfPersonHasSpecialFederalState() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(BADEN_WUERTTEMBERG);

        final WorkingTimeEntity workingTimeEntity = new WorkingTimeEntity();
        workingTimeEntity.setFederalStateOverride(BAYERN);
        when(workingTimeRepository.findByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(workingTimeEntity);

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

        final WorkingTimeEntity workingTimeEntity = new WorkingTimeEntity();
        workingTimeEntity.setFederalStateOverride(null);
        when(workingTimeRepository.findByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class))).thenReturn(workingTimeEntity);

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

        final ArgumentCaptor<WorkingTimeEntity> workingTimeArgumentCaptor = ArgumentCaptor.forClass(WorkingTimeEntity.class);
        verify(workingTimeRepository).save(workingTimeArgumentCaptor.capture());

        final WorkingTimeEntity persistedWorkingTimeEntity = workingTimeArgumentCaptor.getValue();
        assertThat(persistedWorkingTimeEntity.getFederalStateOverride()).isEqualTo(BAYERN);
    }

    @Test
    void ensureRemovesFederalStateOverrideIfNull() {

        final WorkingTimeEntity oldWorkingTimeEntity = new WorkingTimeEntity();
        oldWorkingTimeEntity.setFederalStateOverride(BAYERN);

        when(workingTimeRepository.findByPersonAndValidityDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(oldWorkingTimeEntity);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        sut.touch(List.of(1, 2), Optional.empty(), LocalDate.now(UTC), person);

        final ArgumentCaptor<WorkingTimeEntity> workingTimeArgumentCaptor = ArgumentCaptor.forClass(WorkingTimeEntity.class);
        verify(workingTimeRepository).save(workingTimeArgumentCaptor.capture());

        final WorkingTimeEntity persistedWorkingTimeEntity = workingTimeArgumentCaptor.getValue();
        assertThat(persistedWorkingTimeEntity.getFederalStateOverride()).isNull();
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

        final WorkingTimeEntity workingTimeEntity = new WorkingTimeEntity();
        workingTimeEntity.setId(1);
        workingTimeEntity.setPerson(person);
        workingTimeEntity.setValidFrom(LocalDate.of(2021, JUNE, 11));
        workingTimeEntity.setFederalStateOverride(BADEN_WUERTTEMBERG);
        workingTimeEntity.setMonday(DayLength.FULL);
        workingTimeEntity.setTuesday(DayLength.FULL);
        workingTimeEntity.setWednesday(DayLength.FULL);
        workingTimeEntity.setThursday(DayLength.FULL);
        workingTimeEntity.setFriday(DayLength.FULL);
        workingTimeEntity.setSaturday(DayLength.FULL);
        workingTimeEntity.setSunday(DayLength.FULL);

        when(workingTimeRepository.findByPersonOrderByValidFromDesc(person)).thenReturn(List.of(workingTimeEntity));

        final List<WorkingTime> workingTimes = sut.getByPerson(person);
        assertThat(workingTimes).hasSize(1);
        assertThat(workingTimes.get(0).getId()).isEqualTo(1);
        assertThat(workingTimes.get(0).getPerson()).isSameAs(person);
        assertThat(workingTimes.get(0).getValidFrom()).isEqualTo(LocalDate.of(2021, JUNE, 11));
        assertThat(workingTimes.get(0).getFederalStateOverride()).hasValue(BADEN_WUERTTEMBERG);
        assertThat(workingTimes.get(0).getMonday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimes.get(0).getTuesday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimes.get(0).getWednesday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimes.get(0).getThursday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimes.get(0).getFriday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimes.get(0).getSaturday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimes.get(0).getSunday()).isEqualTo(DayLength.FULL);
    }
}
