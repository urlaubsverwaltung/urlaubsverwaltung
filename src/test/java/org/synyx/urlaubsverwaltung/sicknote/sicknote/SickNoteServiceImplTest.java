package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.SUBMITTED;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

@ExtendWith(MockitoExtension.class)
class SickNoteServiceImplTest {

    private SickNoteServiceImpl sut;

    @Mock
    private SickNoteRepository sickNoteRepository;
    @Mock
    private SettingsService settingsService;
    @Mock
    private WorkingTimeCalendarService workingTimeCalendarService;
    @Mock
    private SickNoteMapper sickNoteMapper;

    private final Clock fixedClock = Clock.fixed(Instant.parse("2021-06-28T00:00:00.00Z"), UTC);

    @BeforeEach
    void setUp() {
        sut = new SickNoteServiceImpl(sickNoteRepository, settingsService, workingTimeCalendarService, sickNoteMapper, fixedClock);
    }

    @Test
    void save() {

        final Person person = new Person();
        final Person applier = new Person();
        final SickNoteType sickNoteType = new SickNoteType();
        final LocalDate startDate = LocalDate.of(2022, 12, 5);
        final LocalDate endDate = LocalDate.of(2022, 12, 9);
        final LocalDate aubStartDate = LocalDate.of(2022, 12, 7);

        when(sickNoteRepository.save(any(SickNoteEntity.class))).thenAnswer(returnsFirstArg());

        final SickNote sickNote = SickNote.builder()
            .id(42L)
            .person(person)
            .applier(applier)
            .sickNoteType(sickNoteType)
            .startDate(startDate)
            .endDate(endDate)
            .dayLength(DayLength.FULL)
            .aubStartDate(aubStartDate)
            .aubEndDate(endDate)
            .lastEdited(LocalDate.of(2022, 12, 5))
            .endOfSickPayNotificationSend(endDate)
            .status(ACTIVE)
            .build();

        final SickNote expectedSickNote = SickNote.builder().build();
        when(sickNoteMapper.toSickNote(any(SickNoteEntity.class))).thenReturn(expectedSickNote);

        final SickNote actual = sut.save(sickNote);
        assertThat(actual).isSameAs(expectedSickNote);

        final ArgumentCaptor<SickNoteEntity> captor = ArgumentCaptor.forClass(SickNoteEntity.class);
        verify(sickNoteRepository).save(captor.capture());
        assertThat(captor.getValue()).satisfies(entityToSave -> {
            assertThat(entityToSave.getId()).isEqualTo(42);
            assertThat(entityToSave.getPerson()).isSameAs(person);
            assertThat(entityToSave.getApplier()).isSameAs(applier);
            assertThat(entityToSave.getSickNoteType()).isEqualTo(sickNoteType);
            assertThat(entityToSave.getStartDate()).isEqualTo(startDate);
            assertThat(entityToSave.getEndDate()).isEqualTo(endDate);
            assertThat(entityToSave.getDayLength()).isEqualTo(DayLength.FULL);
            assertThat(entityToSave.getAubStartDate()).isEqualTo(aubStartDate);
            assertThat(entityToSave.getAubEndDate()).isEqualTo(endDate);
            assertThat(entityToSave.getLastEdited()).isEqualTo(LocalDate.now(fixedClock));
            assertThat(entityToSave.getEndOfSickPayNotificationSend()).isEqualTo(endDate);
            assertThat(entityToSave.getStatus()).isEqualTo(ACTIVE);
        });
    }

    @Test
    void getById() {

        final Person person = new Person();
        person.setId(1L);

        final LocalDate now = LocalDate.now(fixedClock);
        final LocalDate startDate = now.minusDays(10);
        final LocalDate endDate = now.minusDays(6);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1L);
        entity.setPerson(person);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);

        when(sickNoteRepository.findById(1L)).thenReturn(Optional.of(entity));

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(startDate, endDate, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(startDate, endDate)))
            .thenReturn(Map.of(person, workingTimeCalendar));

        final SickNote sickNote = SickNote.builder().build();
        when(sickNoteMapper.toSickNote(entity, workingTimeCalendar)).thenReturn(sickNote);

        final Optional<SickNote> actualMaybe = sut.getById(1L);
        assertThat(actualMaybe).isPresent().get().isSameAs(sickNote);
    }

    @Test
    void getAllActiveByYear() {

        final LocalDate from = LocalDate.now(fixedClock).minusDays(11);
        final LocalDate to = LocalDate.now(fixedClock);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1L);

        final SickNoteEntity entity2 = new SickNoteEntity();
        entity2.setId(2L);

        when(sickNoteRepository.findByPersonPermissionsIsInAndStatusInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(List.of(USER), List.of(ACTIVE), from, to))
            .thenReturn(List.of(entity, entity2));

        final SickNote sickNote1 = SickNote.builder().build();
        final SickNote sickNote2 = SickNote.builder().build();
        when(sickNoteMapper.toSickNoteWithWorkDays(List.of(entity, entity2), new DateRange(from, to)))
            .thenReturn(List.of(sickNote1, sickNote2));

        final List<SickNote> sickNotes = sut.getAllActiveByPeriod(from, to);
        assertThat(sickNotes).hasSize(2);
        assertThat(sickNotes.getFirst()).isSameAs(sickNote1);
        assertThat(sickNotes.get(1)).isSameAs(sickNote2);
    }

    @Test
    void ensureSickNoteOfYesterdayWithPersonWorkingYesterday() {

        final Person person = new Person();
        person.setId(1L);

        final LocalDate now = LocalDate.now(fixedClock);
        final LocalDate startDate = now.minusDays(1);
        final LocalDate endDate = now.minusDays(1);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1L);
        entity.setPerson(person);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);

        when(sickNoteRepository.findFirstByPersonAndStatusInAndEndDateIsLessThanOrderByEndDateDesc(person, List.of(SUBMITTED, ACTIVE), now))
            .thenReturn(Optional.of(entity));

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(endDate, now, date -> fullWorkingDayInformation());
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(endDate, now)))
            .thenReturn(Map.of(person, new WorkingTimeCalendar(personWorkingTimeByDate)));

        final Map<LocalDate, WorkingDayInformation> sickNoteWorkDayInfo = buildWorkingTimeByDate(startDate, endDate, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar entityWorkingTimeCalendar = new WorkingTimeCalendar(sickNoteWorkDayInfo);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(startDate, endDate)))
            .thenReturn(Map.of(person, entityWorkingTimeCalendar));

        final SickNote sickNote = SickNote.builder().id(1L).build();
        when(sickNoteMapper.toSickNote(entity, entityWorkingTimeCalendar)).thenReturn(sickNote);

        final Optional<SickNote> actual = sut.getSickNoteOfYesterdayOrLastWorkDay(person);
        assertThat(actual).isPresent().get().isSameAs(sickNote);
    }

    @Test
    void ensureSickNoteOfYesterdayWithPersonNotWorkingYesterday() {
        final Person person = new Person();
        person.setId(1L);

        final LocalDate now = LocalDate.now(fixedClock);
        final LocalDate startDate = now.minusDays(1);
        final LocalDate endDate = now.minusDays(1);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1L);
        entity.setPerson(person);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);

        when(sickNoteRepository.findFirstByPersonAndStatusInAndEndDateIsLessThanOrderByEndDateDesc(person, List.of(SUBMITTED, ACTIVE), now))
            .thenReturn(Optional.of(entity));

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(endDate.minusDays(1), now, date -> fullWorkingDayInformation());
        personWorkingTimeByDate.remove(endDate);

        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(endDate, now)))
            .thenReturn(Map.of(person, workingTimeCalendar));

        final Map<LocalDate, WorkingDayInformation> sickNoteWorkDayInfo = buildWorkingTimeByDate(startDate, endDate, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar entityWorkingTimeCalendar = new WorkingTimeCalendar(sickNoteWorkDayInfo);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(startDate, endDate)))
            .thenReturn(Map.of(person, entityWorkingTimeCalendar));

        final SickNote sickNote = SickNote.builder().build();
        when(sickNoteMapper.toSickNote(entity, entityWorkingTimeCalendar)).thenReturn(sickNote);

        final Optional<SickNote> actual = sut.getSickNoteOfYesterdayOrLastWorkDay(person);
        assertThat(actual).isPresent().get().isSameAs(sickNote);
    }

    @Test
    void ensureSickNoteOfLastWorkDayWithPersonNotWorkingYesterday() {

        final Person person = new Person();
        person.setId(1L);

        final LocalDate now = LocalDate.now(fixedClock);
        final LocalDate startDate = now.minusDays(2);
        final LocalDate endDate = now.minusDays(2);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1L);
        entity.setPerson(person);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);

        when(sickNoteRepository.findFirstByPersonAndStatusInAndEndDateIsLessThanOrderByEndDateDesc(person, List.of(SUBMITTED, ACTIVE), now))
            .thenReturn(Optional.of(entity));

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(endDate, now, date -> fullWorkingDayInformation());
        personWorkingTimeByDate.remove(now.minusDays(1));
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(endDate, now)))
            .thenReturn(Map.of(person, workingTimeCalendar));

        final Map<LocalDate, WorkingDayInformation> sickNoteWorkDayInfo = buildWorkingTimeByDate(startDate, endDate, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar entityWorkingTimeCalendar = new WorkingTimeCalendar(sickNoteWorkDayInfo);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(startDate, endDate)))
            .thenReturn(Map.of(person, entityWorkingTimeCalendar));

        final SickNote sickNote = SickNote.builder().build();
        when(sickNoteMapper.toSickNote(entity, entityWorkingTimeCalendar)).thenReturn(sickNote);

        final Optional<SickNote> actual = sut.getSickNoteOfYesterdayOrLastWorkDay(person);
        assertThat(actual).isPresent().get().isSameAs(sickNote);
    }

    @Test
    void ensureNoSickNoteIfBetweenLastSickNoteAndTodayPersonWasWorking() {
        final Person person = new Person();
        person.setId(1L);

        final LocalDate now = LocalDate.now(fixedClock);
        final LocalDate startDate = now.minusDays(2);
        final LocalDate endDate = now.minusDays(2);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1L);
        entity.setPerson(person);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);

        when(sickNoteRepository.findFirstByPersonAndStatusInAndEndDateIsLessThanOrderByEndDateDesc(person, List.of(SUBMITTED, ACTIVE), now)).thenReturn(Optional.of(entity));

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(endDate, now, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(endDate, now))).thenReturn(Map.of(person, workingTimeCalendar));

        assertThat(sut.getSickNoteOfYesterdayOrLastWorkDay(person)).isEmpty();
    }

    @Test
    void ensureNoSickNoteOfYesterdayOrLastWorkDayIfNoSickNoteExists() {

        final LocalDate now = LocalDate.now(fixedClock);

        final Person person = new Person();
        person.setId(1L);

        when(sickNoteRepository.findFirstByPersonAndStatusInAndEndDateIsLessThanOrderByEndDateDesc(person, List.of(SUBMITTED, ACTIVE), now)).thenReturn(Optional.empty());
        assertThat(sut.getSickNoteOfYesterdayOrLastWorkDay(person)).isEmpty();
    }

    @Test
    void getSickNotesReachingEndOfSickPay() {

        final Person person = new Person();
        final LocalDate startDate = LocalDate.of(2022, 12, 5);
        final LocalDate endDate = LocalDate.of(2022, 12, 9);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1L);
        entity.setPerson(person);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setEndOfSickPayNotificationSend(endDate);

        final SickNoteSettings sickNoteSettings = new SickNoteSettings();
        sickNoteSettings.setMaximumSickPayDays(5);
        sickNoteSettings.setDaysBeforeEndOfSickPayNotification(2);

        final Settings settings = new Settings();
        settings.setSickNoteSettings(sickNoteSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        when(sickNoteRepository.findSickNotesToNotifyForSickPayEnd(5, 2, LocalDate.of(2021, 6, 28)))
            .thenReturn(List.of(entity));

        final SickNote sickNote = SickNote.builder().build();
        when(sickNoteMapper.toSickNote(entity)).thenReturn(sickNote);

        final List<SickNote> actual = sut.getSickNotesReachingEndOfSickPay();
        assertThat(actual).hasSize(1).first().isSameAs(sickNote);
    }

    @Test
    void getForStatesSince() {

        final LocalDate now = LocalDate.now(fixedClock);
        final LocalDate since = now.minusDays(30);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1L);

        when(sickNoteRepository.findByStatusInAndEndDateGreaterThanEqual(List.of(ACTIVE), since))
            .thenReturn(List.of(entity));

        final SickNote sickNote = SickNote.builder().build();
        when(sickNoteMapper.toSickNoteWithWorkDays(List.of(entity), new DateRange(since, now)))
            .thenReturn(List.of(sickNote));

        final List<SickNote> actual = sut.getForStatesSince(List.of(ACTIVE), since);
        assertThat(actual).hasSize(1).first().isSameAs(sickNote);
    }

    @Test
    void getForStatesAndPerson() {

        final Person person = new Person();
        final LocalDate now = LocalDate.now(fixedClock);
        final LocalDate startDate = now.minusDays(10);
        final LocalDate endDate = now.minusDays(6);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1L);
        entity.setPerson(person);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);

        final List<Person> persons = List.of(person);
        final List<SickNoteStatus> openSickNoteStatuses = List.of(ACTIVE);

        when(sickNoteRepository.findByStatusInAndPersonIn(openSickNoteStatuses, persons))
                .thenReturn(List.of(entity));

        final SickNote sickNote = SickNote.builder().build();
        when(sickNoteMapper.toSickNoteWithWorkDays(List.of(entity), new DateRange(startDate, endDate)))
            .thenReturn(List.of(sickNote));

        final List<SickNote> sickNotes = sut.getForStatesAndPerson(openSickNoteStatuses, persons);
        assertThat(sickNotes).hasSize(1).first().isSameAs(sickNote);
    }

    @Test
    void getForStatesAndPersonWithEmptyResult() {

        final Person person = new Person();
        final List<Person> persons = List.of(person);
        final List<SickNoteStatus> openSickNoteStatuses = List.of(ACTIVE);

        when(sickNoteRepository.findByStatusInAndPersonIn(openSickNoteStatuses, persons))
                .thenReturn(List.of());

        final List<SickNote> sickNotes = sut.getForStatesAndPerson(openSickNoteStatuses, persons);
        assertThat(sickNotes).isEmpty();
    }

    @Test
    void getForStatesAndPersonSince() {

        final Person person = new Person();
        final LocalDate now = LocalDate.now(fixedClock);
        final LocalDate startDate = now.minusDays(10);
        final LocalDate endDate = now.minusDays(6);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1L);
        entity.setPerson(person);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);

        final List<Person> persons = List.of(person);
        final List<SickNoteStatus> openSickNoteStatuses = List.of(ACTIVE);
        final LocalDate since = LocalDate.of(2020, 10, 3);

        when(sickNoteRepository.findByStatusInAndPersonInAndEndDateIsGreaterThanEqual(openSickNoteStatuses, persons, since))
            .thenReturn(List.of(entity));

        final SickNote sickNote = SickNote.builder().build();
        when(sickNoteMapper.toSickNoteWithWorkDays(List.of(entity), new DateRange(since, now)))
            .thenReturn(List.of(sickNote));

        final List<SickNote> sickNotes = sut.getForStatesAndPersonSince(openSickNoteStatuses, persons, since);
        assertThat(sickNotes).hasSize(1).first().isSameAs(sickNote);
    }

    @Test
    void setEndOfSickPayNotificationSend() {

        final SickNote sickNote = SickNote.builder().build();

        sut.setEndOfSickPayNotificationSend(sickNote);

        final ArgumentCaptor<SickNoteEntity> captor = ArgumentCaptor.forClass(SickNoteEntity.class);
        verify(sickNoteRepository).save(captor.capture());

        final SickNoteEntity entityToSave = captor.getValue();
        assertThat(entityToSave.getEndOfSickPayNotificationSend()).isEqualTo(LocalDate.now(fixedClock));
    }

    @Test
    void deleteAll() {
        final Person person = new Person();

        sut.deleteAllByPerson(person);

        verify(sickNoteRepository).deleteByPerson(person);
    }

    private static WorkingDayInformation fullWorkingDayInformation() {
        return new WorkingDayInformation(FULL, WORKDAY, WORKDAY);
    }

    private static Map<LocalDate, WorkingDayInformation> buildWorkingTimeByDate(LocalDate from, LocalDate to, Function<LocalDate, WorkingDayInformation> dayLengthProvider) {
        Map<LocalDate, WorkingDayInformation> map = new HashMap<>();
        for (LocalDate date : new DateRange(from, to)) {
            map.put(date, dayLengthProvider.apply(date));
        }
        return map;
    }
}
