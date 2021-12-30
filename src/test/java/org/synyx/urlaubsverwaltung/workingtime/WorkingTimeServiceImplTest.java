package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.Month.JUNE;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.*;

@ExtendWith(MockitoExtension.class)
class WorkingTimeServiceImplTest {

    private WorkingTimeServiceImpl sut;

    @Mock
    private WorkingTimeProperties workingTimeProperties;
    @Mock
    private WorkingTimeRepository workingTimeRepository;
    @Mock
    private SettingsService settingsService;

    private final Clock fixedClock = Clock.fixed(Instant.parse("2019-08-13T00:00:00.00Z"), UTC);

    @BeforeEach
    void setUp() {
        sut = new WorkingTimeServiceImpl(workingTimeProperties, workingTimeRepository, settingsService, fixedClock);
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
    void ensureGetFederalStateForPerson() {

        final LocalDate date = LocalDate.now(UTC);

        final Person person = new Person();
        person.setId(1);

        final WorkingTimeEntity workingTimeEntity = new WorkingTimeEntity();
        workingTimeEntity.setFederalStateOverride(GERMANY_BAYERN);
        when(workingTimeRepository.findByPersonAndValidityDateEqualsOrMinorDate(person, date)).thenReturn(workingTimeEntity);

        final FederalState federalState = sut.getFederalStateForPerson(person, date);

        assertThat(federalState).isEqualTo(GERMANY_BAYERN);
        verifyNoInteractions(settingsService);
    }

    @Test
    void ensureGetFederalStateForPersonReturnSystemDefaultWhenEntityHasNullValue() {

        final LocalDate date = LocalDate.now(UTC);

        final Person person = new Person();
        person.setId(1);

        final WorkingTimeEntity workingTimeEntity = new WorkingTimeEntity();
        when(workingTimeRepository.findByPersonAndValidityDateEqualsOrMinorDate(person, date)).thenReturn(workingTimeEntity);

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setFederalState(GERMANY_BREMEN);
        final Settings settings = new Settings();
        settings.setWorkingTimeSettings(workingTimeSettings);

        when(settingsService.getSettings()).thenReturn(settings);

        final FederalState federalState = sut.getFederalStateForPerson(person, date);

        assertThat(federalState).isEqualTo(GERMANY_BREMEN);
    }

    @Test
    void ensureGetFederalStateForPersonReturnSystemDefaultWhenNothingFound() {

        when(workingTimeRepository.findByPersonAndValidityDateEqualsOrMinorDate(any(), any())).thenReturn(null);

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setFederalState(GERMANY_BREMEN);
        final Settings settings = new Settings();
        settings.setWorkingTimeSettings(workingTimeSettings);

        when(settingsService.getSettings()).thenReturn(settings);

        final FederalState federalState = sut.getFederalStateForPerson(new Person(), LocalDate.now(UTC));

        assertThat(federalState).isEqualTo(GERMANY_BREMEN);
    }

    @Test
    void ensureTouchSetsFederalStateOverrideOfWorkingTimeEntityToNull() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        sut.touch(List.of(1, 2), LocalDate.now(UTC), person);

        final ArgumentCaptor<WorkingTimeEntity> workingTimeArgumentCaptor = ArgumentCaptor.forClass(WorkingTimeEntity.class);
        verify(workingTimeRepository).save(workingTimeArgumentCaptor.capture());

        final WorkingTimeEntity persistedWorkingTimeEntity = workingTimeArgumentCaptor.getValue();
        assertThat(persistedWorkingTimeEntity.getFederalStateOverride()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = FederalState.class, names = {"GERMANY_BAYERN"})
    @NullSource
    void ensureTouchSetsFederalStateOverrideOfWorkingTimeEntity(FederalState federalState) {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        sut.touch(List.of(1, 2), LocalDate.now(UTC), person, federalState);

        final ArgumentCaptor<WorkingTimeEntity> workingTimeArgumentCaptor = ArgumentCaptor.forClass(WorkingTimeEntity.class);
        verify(workingTimeRepository).save(workingTimeArgumentCaptor.capture());

        final WorkingTimeEntity persistedWorkingTimeEntity = workingTimeArgumentCaptor.getValue();
        assertThat(persistedWorkingTimeEntity.getFederalStateOverride()).isEqualTo(federalState);
    }


    @Test
    void getByPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final WorkingTimeEntity workingTimeEntity = new WorkingTimeEntity();
        workingTimeEntity.setId(1);
        workingTimeEntity.setPerson(person);
        workingTimeEntity.setValidFrom(LocalDate.of(2021, JUNE, 11));
        workingTimeEntity.setFederalStateOverride(GERMANY_BADEN_WUERTTEMBERG);
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
        assertThat(workingTimes.get(0).getPerson()).isSameAs(person);
        assertThat(workingTimes.get(0).getValidFrom()).isEqualTo(LocalDate.of(2021, JUNE, 11));
        assertThat(workingTimes.get(0).getFederalState()).isEqualTo(GERMANY_BADEN_WUERTTEMBERG);
        assertThat(workingTimes.get(0).getMonday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimes.get(0).getTuesday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimes.get(0).getWednesday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimes.get(0).getThursday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimes.get(0).getFriday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimes.get(0).getSaturday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimes.get(0).getSunday()).isEqualTo(DayLength.FULL);
    }

    @Test
    void ensureGetByPersonCallsSystemDefaultFederalStateOnlyOnce() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final WorkingTimeEntity workingTimeEntityOne = new WorkingTimeEntity();
        workingTimeEntityOne.setId(1);
        workingTimeEntityOne.setPerson(person);
        workingTimeEntityOne.setValidFrom(LocalDate.of(2021, JUNE, 11));
        workingTimeEntityOne.setFederalStateOverride(null);

        final WorkingTimeEntity workingTimeEntityTwo = new WorkingTimeEntity();
        workingTimeEntityTwo.setId(2);
        workingTimeEntityTwo.setPerson(person);
        workingTimeEntityTwo.setValidFrom(LocalDate.of(2022, JUNE, 11));
        workingTimeEntityTwo.setFederalStateOverride(null);

        when(workingTimeRepository.findByPersonOrderByValidFromDesc(person)).thenReturn(List.of(workingTimeEntityOne, workingTimeEntityTwo));

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setFederalState(GERMANY_BREMEN);
        final Settings settings = new Settings();
        settings.setWorkingTimeSettings(workingTimeSettings);

        when(settingsService.getSettings()).thenReturn(settings);

        final List<WorkingTime> workingTimes = sut.getByPerson(person);

        assertThat(workingTimes).hasSize(2);
        assertThat(workingTimes.get(0).getFederalState()).isEqualTo(GERMANY_BREMEN);
        assertThat(workingTimes.get(1).getFederalState()).isEqualTo(GERMANY_BREMEN);
        verify(settingsService).getSettings();
    }

    @Test
    void ensureGetByPersonAndValidityDateEqualsOrMinorDate() {
        final Person batman = new Person();
        batman.setId(1);

        final LocalDate date = LocalDate.now();

        final WorkingTimeEntity workingTimeEntity = new WorkingTimeEntity();
        workingTimeEntity.setId(1);
        workingTimeEntity.setPerson(batman);
        workingTimeEntity.setValidFrom(date);
        workingTimeEntity.setFederalStateOverride(GERMANY_BADEN_WUERTTEMBERG);

        when(workingTimeRepository.findByPersonAndValidityDateEqualsOrMinorDate(batman, date))
            .thenReturn(workingTimeEntity);

        final Optional<WorkingTime> actualWorkingTime = sut.getByPersonAndValidityDateEqualsOrMinorDate(batman, date);

        assertThat(actualWorkingTime).isNotEmpty();
        assertThat(actualWorkingTime.get().getPerson()).isEqualTo(batman);
        assertThat(actualWorkingTime.get().getValidFrom()).isEqualTo(date);
        assertThat(actualWorkingTime.get().getFederalState()).isEqualTo(GERMANY_BADEN_WUERTTEMBERG);
    }

    @Test
    void ensureGetByPersonAndValidityDateEqualsOrMinorDateUsesSystemDefaultFederalStateWhenEntityHasNullValue() {
        final Person batman = new Person();
        batman.setId(1);

        final LocalDate date = LocalDate.now();

        final WorkingTimeEntity workingTimeEntity = new WorkingTimeEntity();
        workingTimeEntity.setId(1);
        workingTimeEntity.setPerson(batman);
        workingTimeEntity.setValidFrom(date);

        when(workingTimeRepository.findByPersonAndValidityDateEqualsOrMinorDate(batman, date))
            .thenReturn(workingTimeEntity);

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setFederalState(GERMANY_BREMEN);
        final Settings settings = new Settings();
        settings.setWorkingTimeSettings(workingTimeSettings);

        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<WorkingTime> actualWorkingTime = sut.getByPersonAndValidityDateEqualsOrMinorDate(batman, date);

        assertThat(actualWorkingTime).isNotEmpty();
        assertThat(actualWorkingTime.get().getPerson()).isEqualTo(batman);
        assertThat(actualWorkingTime.get().getValidFrom()).isEqualTo(date);
        assertThat(actualWorkingTime.get().getFederalState()).isEqualTo(GERMANY_BREMEN);
    }

    @Test
    void ensureGetByPersonAndValidityDateEqualsOrMinorDateReturnsEmptyOptionalWhenNothingFound() {
        when(workingTimeRepository.findByPersonAndValidityDateEqualsOrMinorDate(any(), any()))
            .thenReturn(null);

        final Optional<WorkingTime> actualWorkingTime = sut.getByPersonAndValidityDateEqualsOrMinorDate(new Person(), LocalDate.now());

        assertThat(actualWorkingTime).isEmpty();
    }

    @Test
    void ensureGetSystemDefaultFederalState() {

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setFederalState(GERMANY_BREMEN);
        final Settings settings = new Settings();
        settings.setWorkingTimeSettings(workingTimeSettings);

        when(settingsService.getSettings()).thenReturn(settings);

        final FederalState defaultFederalState = sut.getSystemDefaultFederalState();

        assertThat(defaultFederalState).isEqualTo(GERMANY_BREMEN);
    }

    @Test
    void getFederalStatesByPersonAndDateRange() {

        final Person batman = new Person();

        final WorkingTimeEntity workingTimeEntity = new WorkingTimeEntity();
        workingTimeEntity.setId(1);
        workingTimeEntity.setPerson(batman);
        workingTimeEntity.setValidFrom(LocalDate.of(2020, 9, 1));
        workingTimeEntity.setFederalStateOverride(GERMANY_BADEN_WUERTTEMBERG);

        final WorkingTimeEntity workingTimeEntityChanged = new WorkingTimeEntity();
        workingTimeEntityChanged.setId(2);
        workingTimeEntityChanged.setPerson(batman);
        workingTimeEntityChanged.setValidFrom(LocalDate.of(2021, 11, 15));
        workingTimeEntityChanged.setFederalStateOverride(GERMANY_RHEINLAND_PFALZ);

        final WorkingTimeEntity workingTimeEntityFuture = new WorkingTimeEntity();
        workingTimeEntityFuture.setId(3);
        workingTimeEntityFuture.setPerson(batman);
        workingTimeEntityFuture.setValidFrom(LocalDate.of(2022, 1, 1));
        workingTimeEntityFuture.setFederalStateOverride(GERMANY_BERLIN);

        final List<WorkingTimeEntity> timeEntities = List.of(workingTimeEntityFuture, workingTimeEntityChanged, workingTimeEntity );

        when(workingTimeRepository.findByPersonOrderByValidFromDesc(batman)).thenReturn(timeEntities);

        final Map<DateRange, FederalState> federalStatesByPersonAndDateRange = sut.getFederalStatesByPersonAndDateRange(batman,
            new DateRange(
                LocalDate.of(2021, 11, 1),
                LocalDate.of(2021, 11, 30)));

        assertThat(federalStatesByPersonAndDateRange)
            .isNotEmpty()
            .hasSize(2)
            .containsExactly(
                entry(new DateRange(LocalDate.of(2021, 11, 15), LocalDate.of(2021, 11, 30)), GERMANY_RHEINLAND_PFALZ),
                entry(new DateRange(LocalDate.of(2021, 11, 1), LocalDate.of(2021, 11, 14)), GERMANY_BADEN_WUERTTEMBERG)
            );
    }

    @Test
    void getFederalStatesByPersonAndDateRangeWithoutWorkingTimes() {

        final Person batman = new Person();

        when(workingTimeRepository.findByPersonOrderByValidFromDesc(batman)).thenReturn(emptyList());

        assertThat(sut.getFederalStatesByPersonAndDateRange(batman,
            new DateRange(
                LocalDate.of(2021, 11, 1),
                LocalDate.of(2021, 11, 30)))).isEmpty();

    }
}
