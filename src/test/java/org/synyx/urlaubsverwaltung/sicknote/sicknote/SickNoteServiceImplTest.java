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
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
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

@ExtendWith(MockitoExtension.class)
class SickNoteServiceImplTest {

    private SickNoteServiceImpl sut;

    @Mock
    private SickNoteRepository sickNoteRepository;
    @Mock
    private SettingsService settingsService;
    @Mock
    private WorkingTimeCalendarService workingTimeCalendarService;

    private final Clock fixedClock = Clock.fixed(Instant.parse("2021-06-28T00:00:00.00Z"), UTC);

    @BeforeEach
    void setUp() {
        sut = new SickNoteServiceImpl(sickNoteRepository, settingsService, workingTimeCalendarService, fixedClock);
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

        final SickNote actualSavedSickNote = sut.save(sickNote);

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

        assertThat(actualSavedSickNote.getId()).isEqualTo(42);
        assertThat(actualSavedSickNote.getPerson()).isSameAs(person);
        assertThat(actualSavedSickNote.getApplier()).isSameAs(applier);
        assertThat(actualSavedSickNote.getSickNoteType()).isEqualTo(sickNoteType);
        assertThat(actualSavedSickNote.getStartDate()).isEqualTo(startDate);
        assertThat(actualSavedSickNote.getEndDate()).isEqualTo(endDate);
        assertThat(actualSavedSickNote.getDayLength()).isEqualTo(DayLength.FULL);
        assertThat(actualSavedSickNote.getAubStartDate()).isEqualTo(aubStartDate);
        assertThat(actualSavedSickNote.getAubEndDate()).isEqualTo(endDate);
        assertThat(actualSavedSickNote.getLastEdited()).isEqualTo(LocalDate.now(fixedClock));
        assertThat(actualSavedSickNote.getEndOfSickPayNotificationSend()).isEqualTo(endDate);
        assertThat(actualSavedSickNote.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    void getById() {
        final Person person = new Person();
        person.setId(1L);

        final Person applier = new Person();
        applier.setId(2L);

        final SickNoteType sickNoteType = new SickNoteType();
        final LocalDate now = LocalDate.now(fixedClock);
        final LocalDate startDate = now.minusDays(10);
        final LocalDate endDate = now.minusDays(6);
        final LocalDate aubStartDate = endDate.minusDays(2);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1L);
        entity.setPerson(person);
        entity.setApplier(applier);
        entity.setSickNoteType(sickNoteType);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setDayLength(DayLength.FULL);
        entity.setAubStartDate(aubStartDate);
        entity.setAubEndDate(endDate);
        entity.setLastEdited(now);
        entity.setEndOfSickPayNotificationSend(endDate);
        entity.setStatus(ACTIVE);

        when(sickNoteRepository.findById(1L)).thenReturn(Optional.of(entity));

        final Map<LocalDate, DayLength> personWorkingTimeByDate = buildWorkingTimeByDate(startDate, endDate, date -> FULL);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(startDate, endDate))).thenReturn(Map.of(person, workingTimeCalendar));

        final Optional<SickNote> actualMaybe = sut.getById(1L);
        assertThat(actualMaybe).isPresent();

        final SickNote actual = actualMaybe.get();
        assertThat(actual.getId()).isEqualTo(1);
        assertThat(actual.getPerson()).isSameAs(person);
        assertThat(actual.getApplier()).isSameAs(applier);
        assertThat(actual.getSickNoteType()).isEqualTo(sickNoteType);
        assertThat(actual.getStartDate()).isEqualTo(startDate);
        assertThat(actual.getEndDate()).isEqualTo(endDate);
        assertThat(actual.getDayLength()).isEqualTo(DayLength.FULL);
        assertThat(actual.getAubStartDate()).isEqualTo(aubStartDate);
        assertThat(actual.getAubEndDate()).isEqualTo(endDate);
        assertThat(actual.getLastEdited()).isEqualTo(now);
        assertThat(actual.getEndOfSickPayNotificationSend()).isEqualTo(endDate);
        assertThat(actual.getStatus()).isEqualTo(ACTIVE);
        assertThat(actual.getWorkDays()).isEqualTo(BigDecimal.valueOf(5));
    }

    @Test
    void getAllActiveByYear() {
        final Person person = new Person();
        final Person applier = new Person();
        final SickNoteType sickNoteType = new SickNoteType();
        final LocalDate now = LocalDate.now(fixedClock);
        final LocalDate startDate1 = now.minusDays(10);
        final LocalDate endDate1 = now.minusDays(8);
        final LocalDate aubStartDate1 = endDate1.minusDays(8);
        final LocalDate startDate2 = now.minusDays(5);
        final LocalDate endDate2 = now.minusDays(5);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1L);
        entity.setPerson(person);
        entity.setApplier(applier);
        entity.setSickNoteType(sickNoteType);
        entity.setStartDate(startDate1);
        entity.setEndDate(endDate1);
        entity.setDayLength(DayLength.FULL);
        entity.setAubStartDate(aubStartDate1);
        entity.setAubEndDate(endDate1);
        entity.setLastEdited(now);
        entity.setEndOfSickPayNotificationSend(endDate1);
        entity.setStatus(ACTIVE);

        final SickNoteEntity entity2 = new SickNoteEntity();
        entity2.setId(2L);
        entity2.setPerson(person);
        entity2.setApplier(applier);
        entity2.setSickNoteType(sickNoteType);
        entity2.setStartDate(startDate2);
        entity2.setEndDate(endDate2);
        entity2.setDayLength(DayLength.FULL);
        entity2.setLastEdited(now);
        entity2.setStatus(ACTIVE);

        final LocalDate from = startDate1.minusDays(1);
        final LocalDate to = now;

        when(sickNoteRepository.findByPersonPermissionsIsInAndStatusInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(List.of(USER), List.of(ACTIVE), from, to))
            .thenReturn(List.of(entity, entity2));

        final Map<LocalDate, DayLength> personWorkingTimeByDate = buildWorkingTimeByDate(from, to, date -> FULL);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(from, to))).thenReturn(Map.of(person, workingTimeCalendar));

        final List<SickNote> sickNotes = sut.getAllActiveByPeriod(from, to);
        assertThat(sickNotes).hasSize(2);
        assertThat(sickNotes.get(0)).satisfies(sickNote -> {
            assertThat(sickNote.getId()).isEqualTo(1);
            assertThat(sickNote.getPerson()).isSameAs(person);
            assertThat(sickNote.getApplier()).isSameAs(applier);
            assertThat(sickNote.getSickNoteType()).isEqualTo(sickNoteType);
            assertThat(sickNote.getStartDate()).isEqualTo(startDate1);
            assertThat(sickNote.getEndDate()).isEqualTo(endDate1);
            assertThat(sickNote.getDayLength()).isEqualTo(DayLength.FULL);
            assertThat(sickNote.getAubStartDate()).isEqualTo(aubStartDate1);
            assertThat(sickNote.getAubEndDate()).isEqualTo(endDate1);
            assertThat(sickNote.getLastEdited()).isEqualTo(now);
            assertThat(sickNote.getEndOfSickPayNotificationSend()).isEqualTo(endDate1);
            assertThat(sickNote.getStatus()).isEqualTo(ACTIVE);
            assertThat(sickNote.getWorkDays()).isEqualTo(BigDecimal.valueOf(3));
        });
        assertThat(sickNotes.get(1)).satisfies(sickNote -> {
            assertThat(sickNote.getId()).isEqualTo(2);
            assertThat(sickNote.getPerson()).isSameAs(person);
            assertThat(sickNote.getApplier()).isSameAs(applier);
            assertThat(sickNote.getSickNoteType()).isEqualTo(sickNoteType);
            assertThat(sickNote.getStartDate()).isEqualTo(startDate2);
            assertThat(sickNote.getEndDate()).isEqualTo(endDate2);
            assertThat(sickNote.getDayLength()).isEqualTo(DayLength.FULL);
            assertThat(sickNote.getAubStartDate()).isNull();
            assertThat(sickNote.getAubEndDate()).isNull();
            assertThat(sickNote.getLastEdited()).isEqualTo(now);
            assertThat(sickNote.getEndOfSickPayNotificationSend()).isNull();
            assertThat(sickNote.getStatus()).isEqualTo(ACTIVE);
            assertThat(sickNote.getWorkDays()).isEqualTo(BigDecimal.valueOf(1));
        });
    }

    @Test
    void getSickNotesReachingEndOfSickPay() {
        final Person person = new Person();
        final Person applier = new Person();
        final SickNoteType sickNoteType = new SickNoteType();
        final LocalDate startDate = LocalDate.of(2022, 12, 5);
        final LocalDate endDate = LocalDate.of(2022, 12, 9);
        final LocalDate aubStartDate = LocalDate.of(2022, 12, 7);
        final LocalDate aubEndDate = LocalDate.of(2022, 12, 9);
        final LocalDate lastEditedDate = LocalDate.of(2022, 12, 10);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1L);
        entity.setPerson(person);
        entity.setApplier(applier);
        entity.setSickNoteType(sickNoteType);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setDayLength(DayLength.FULL);
        entity.setAubStartDate(aubStartDate);
        entity.setAubEndDate(aubEndDate);
        entity.setLastEdited(lastEditedDate);
        entity.setEndOfSickPayNotificationSend(endDate);
        entity.setStatus(ACTIVE);

        final SickNoteSettings sickNoteSettings = new SickNoteSettings();
        sickNoteSettings.setMaximumSickPayDays(5);
        sickNoteSettings.setDaysBeforeEndOfSickPayNotification(2);

        final Settings settings = new Settings();
        settings.setSickNoteSettings(sickNoteSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        when(sickNoteRepository.findSickNotesToNotifyForSickPayEnd(5, 2, LocalDate.of(2021, 6, 28)))
            .thenReturn(List.of(entity));

        final List<SickNote> sickNotesReachingEndOfSickPay = sut.getSickNotesReachingEndOfSickPay();
        assertThat(sickNotesReachingEndOfSickPay).hasSize(1);

        final SickNote actual = sickNotesReachingEndOfSickPay.get(0);
        assertThat(actual.getId()).isEqualTo(1);
        assertThat(actual.getPerson()).isSameAs(person);
        assertThat(actual.getApplier()).isSameAs(applier);
        assertThat(actual.getSickNoteType()).isEqualTo(sickNoteType);
        assertThat(actual.getStartDate()).isEqualTo(startDate);
        assertThat(actual.getEndDate()).isEqualTo(endDate);
        assertThat(actual.getDayLength()).isEqualTo(DayLength.FULL);
        assertThat(actual.getAubStartDate()).isEqualTo(aubStartDate);
        assertThat(actual.getAubEndDate()).isEqualTo(endDate);
        assertThat(actual.getLastEdited()).isEqualTo(lastEditedDate);
        assertThat(actual.getEndOfSickPayNotificationSend()).isEqualTo(endDate);
        assertThat(actual.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    void getForStatesSince() {
        final Person person = new Person();
        person.setId(1L);

        final Person applier = new Person();
        applier.setId(2L);

        final SickNoteType sickNoteType = new SickNoteType();
        final LocalDate now = LocalDate.now(fixedClock);
        final LocalDate startDate = now.minusDays(10);
        final LocalDate endDate = now.minusDays(6);
        final LocalDate aubStartDate = endDate.minusDays(2);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1L);
        entity.setPerson(person);
        entity.setApplier(applier);
        entity.setSickNoteType(sickNoteType);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setDayLength(DayLength.FULL);
        entity.setAubStartDate(aubStartDate);
        entity.setAubEndDate(endDate);
        entity.setLastEdited(now);
        entity.setEndOfSickPayNotificationSend(endDate);
        entity.setStatus(ACTIVE);

        final LocalDate since = now.minusDays(30);

        when(sickNoteRepository.findByStatusInAndEndDateGreaterThanEqual(List.of(ACTIVE), since)).thenReturn(List.of(entity));

        final Map<LocalDate, DayLength> personWorkingTimeByDate = buildWorkingTimeByDate(startDate, endDate, date -> FULL);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(since, now))).thenReturn(Map.of(person, workingTimeCalendar));

        final List<SickNote> sickNotes = sut.getForStatesSince(List.of(ACTIVE), since);
        assertThat(sickNotes).hasSize(1);

        final SickNote actual = sickNotes.get(0);
        assertThat(actual.getId()).isEqualTo(1);
        assertThat(actual.getPerson()).isSameAs(person);
        assertThat(actual.getApplier()).isSameAs(applier);
        assertThat(actual.getSickNoteType()).isEqualTo(sickNoteType);
        assertThat(actual.getStartDate()).isEqualTo(startDate);
        assertThat(actual.getEndDate()).isEqualTo(endDate);
        assertThat(actual.getDayLength()).isEqualTo(DayLength.FULL);
        assertThat(actual.getAubStartDate()).isEqualTo(aubStartDate);
        assertThat(actual.getAubEndDate()).isEqualTo(endDate);
        assertThat(actual.getLastEdited()).isEqualTo(now);
        assertThat(actual.getEndOfSickPayNotificationSend()).isEqualTo(endDate);
        assertThat(actual.getStatus()).isEqualTo(ACTIVE);
        assertThat(actual.getWorkDays()).isEqualTo(BigDecimal.valueOf(5));
    }


    @Test
    void getForStatesAndPerson() {
        final Person person = new Person();
        final Person applier = new Person();
        final SickNoteType sickNoteType = new SickNoteType();
        final LocalDate now = LocalDate.now(fixedClock);
        final LocalDate startDate = now.minusDays(10);
        final LocalDate endDate = now.minusDays(6);
        final LocalDate aubStartDate = endDate.minusDays(2);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1L);
        entity.setPerson(person);
        entity.setApplier(applier);
        entity.setSickNoteType(sickNoteType);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setDayLength(DayLength.FULL);
        entity.setAubStartDate(aubStartDate);
        entity.setAubEndDate(endDate);
        entity.setLastEdited(now);
        entity.setEndOfSickPayNotificationSend(endDate);
        entity.setStatus(ACTIVE);

        final List<Person> persons = List.of(person);
        final List<SickNoteStatus> openSickNoteStatuses = List.of(ACTIVE);
        final LocalDate since = LocalDate.of(2020, 10, 3);

        when(sickNoteRepository.findByStatusInAndPersonInAndEndDateIsGreaterThanEqual(openSickNoteStatuses, persons, since))
            .thenReturn(List.of(entity));

        final Map<LocalDate, DayLength> personWorkingTimeByDate = buildWorkingTimeByDate(since, now, date -> FULL);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(since, now))).thenReturn(Map.of(person, workingTimeCalendar));

        final List<SickNote> sickNotes = sut.getForStatesAndPersonSince(openSickNoteStatuses, persons, since);
        assertThat(sickNotes).hasSize(1);

        final SickNote actual = sickNotes.get(0);
        assertThat(actual.getId()).isEqualTo(1);
        assertThat(actual.getPerson()).isSameAs(person);
        assertThat(actual.getApplier()).isSameAs(applier);
        assertThat(actual.getSickNoteType()).isEqualTo(sickNoteType);
        assertThat(actual.getStartDate()).isEqualTo(startDate);
        assertThat(actual.getEndDate()).isEqualTo(endDate);
        assertThat(actual.getDayLength()).isEqualTo(DayLength.FULL);
        assertThat(actual.getAubStartDate()).isEqualTo(aubStartDate);
        assertThat(actual.getAubEndDate()).isEqualTo(endDate);
        assertThat(actual.getLastEdited()).isEqualTo(now);
        assertThat(actual.getEndOfSickPayNotificationSend()).isEqualTo(endDate);
        assertThat(actual.getStatus()).isEqualTo(ACTIVE);
        assertThat(actual.getWorkDays()).isEqualTo(BigDecimal.valueOf(5));
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

    private Map<LocalDate, DayLength> buildWorkingTimeByDate(LocalDate from, LocalDate to, Function<LocalDate, DayLength> dayLengthProvider) {
        Map<LocalDate, DayLength> map = new HashMap<>();
        for (LocalDate date : new DateRange(from, to)) {
            map.put(date, dayLengthProvider.apply(date));
        }
        return map;
    }
}
