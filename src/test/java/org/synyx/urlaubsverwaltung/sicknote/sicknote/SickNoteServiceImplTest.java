package org.synyx.urlaubsverwaltung.sicknote.sicknote;

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
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

@ExtendWith(MockitoExtension.class)
class SickNoteServiceImplTest {

    private SickNoteServiceImpl sut;

    @Mock
    private SickNoteRepository sickNoteRepository;
    @Mock
    private SettingsService settingsService;

    private final Clock fixedClock = Clock.fixed(Instant.parse("2021-06-28T00:00:00.00Z"), UTC);

    @BeforeEach
    void setUp() {
        sut = new SickNoteServiceImpl(sickNoteRepository, settingsService, fixedClock);
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
                .id(42)
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
        final Person applier = new Person();
        final SickNoteType sickNoteType = new SickNoteType();
        final LocalDate startDate = LocalDate.of(2022, 12, 5);
        final LocalDate endDate = LocalDate.of(2022, 12, 9);
        final LocalDate aubStartDate = LocalDate.of(2022, 12, 7);
        final LocalDate aubEndDate = LocalDate.of(2022, 12, 9);
        final LocalDate lastEditedDate = LocalDate.of(2022, 12, 10);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1);
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

        when(sickNoteRepository.findById(1)).thenReturn(Optional.of(entity));

        final Optional<SickNote> actualMaybe = sut.getById(1);
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
        assertThat(actual.getLastEdited()).isEqualTo(lastEditedDate);
        assertThat(actual.getEndOfSickPayNotificationSend()).isEqualTo(endDate);
        assertThat(actual.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    void getAllActiveByYear() {
        final Person person = new Person();
        final Person applier = new Person();
        final SickNoteType sickNoteType = new SickNoteType();
        final LocalDate startDate = LocalDate.of(2022, 12, 5);
        final LocalDate endDate = LocalDate.of(2022, 12, 9);
        final LocalDate aubStartDate = LocalDate.of(2022, 12, 7);
        final LocalDate aubEndDate = LocalDate.of(2022, 12, 9);
        final LocalDate lastEditedDate = LocalDate.of(2022, 12, 10);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1);
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

        final LocalDate from = LocalDate.of(2022, 1, 1);
        final LocalDate to = LocalDate.of(2022, 12, 31);

        when(sickNoteRepository.findByPersonPermissionsIsInAndStatusInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(List.of(USER), List.of(ACTIVE), from, to))
                .thenReturn(List.of(entity));

        final List<SickNote> sickNotes = sut.getAllActiveByPeriod(from, to);
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
        assertThat(actual.getLastEdited()).isEqualTo(lastEditedDate);
        assertThat(actual.getEndOfSickPayNotificationSend()).isEqualTo(endDate);
        assertThat(actual.getStatus()).isEqualTo(ACTIVE);
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
        entity.setId(1);
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
        final Person applier = new Person();
        final SickNoteType sickNoteType = new SickNoteType();
        final LocalDate startDate = LocalDate.of(2022, 12, 5);
        final LocalDate endDate = LocalDate.of(2022, 12, 9);
        final LocalDate aubStartDate = LocalDate.of(2022, 12, 7);
        final LocalDate aubEndDate = LocalDate.of(2022, 12, 9);
        final LocalDate lastEditedDate = LocalDate.of(2022, 12, 10);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1);
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

        when(sickNoteRepository.findByStatusInAndEndDateGreaterThanEqual(List.of(ACTIVE), LocalDate.of(2022, 11, 30)))
                .thenReturn(List.of(entity));

        final List<SickNote> sickNotes = sut.getForStatesSince(List.of(ACTIVE), LocalDate.of(2022, 11, 30));
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
        assertThat(actual.getLastEdited()).isEqualTo(lastEditedDate);
        assertThat(actual.getEndOfSickPayNotificationSend()).isEqualTo(endDate);
        assertThat(actual.getStatus()).isEqualTo(ACTIVE);
    }


    @Test
    void getForStatesAndPerson() {
        final Person person = new Person();
        final Person applier = new Person();
        final SickNoteType sickNoteType = new SickNoteType();
        final LocalDate startDate = LocalDate.of(2022, 12, 5);
        final LocalDate endDate = LocalDate.of(2022, 12, 9);
        final LocalDate aubStartDate = LocalDate.of(2022, 12, 7);
        final LocalDate aubEndDate = LocalDate.of(2022, 12, 9);
        final LocalDate lastEditedDate = LocalDate.of(2022, 12, 10);

        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(1);
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

        final List<Person> persons = List.of(person);
        final List<SickNoteStatus> openSickNoteStatuses = List.of(ACTIVE);
        final LocalDate since = LocalDate.of(2020, 10, 3);

        when(sickNoteRepository.findByStatusInAndPersonInAndEndDateIsGreaterThanEqual(openSickNoteStatuses, persons, since))
                .thenReturn(List.of(entity));

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
        assertThat(actual.getLastEdited()).isEqualTo(lastEditedDate);
        assertThat(actual.getEndOfSickPayNotificationSend()).isEqualTo(endDate);
        assertThat(actual.getStatus()).isEqualTo(ACTIVE);
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
}
