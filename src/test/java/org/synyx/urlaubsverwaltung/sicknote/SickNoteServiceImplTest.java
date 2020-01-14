package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.AbsenceSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.ACTIVE;

@ExtendWith(MockitoExtension.class)
class SickNoteServiceImplTest {

    private SickNoteServiceImpl sut;

    @Mock
    private SickNoteRepository sickNoteRepository;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new SickNoteServiceImpl(sickNoteRepository, settingsService, Clock.systemUTC());
    }

    @Test
    void save() {
        final SickNote sickNote = new SickNote();
        sut.save(sickNote);
        verify(sickNoteRepository).save(sickNote);
    }

    @Test
    void getById() {
        final Optional<SickNote> sickNote = Optional.of(new SickNote());
        when(sickNoteRepository.findById(1)).thenReturn(sickNote);

        final Optional<SickNote> actualSickNote = sut.getById(1);
        assertThat(actualSickNote).isEqualTo(sickNote);
    }

    @Test
    void findByPeriod() {
        final LocalDate from = LocalDate.of(2015, 1, 1);
        final LocalDate to = LocalDate.of(2016, 1, 1);
        final SickNote sickNote = new SickNote();
        when(sickNoteRepository.findByPeriod(from, to)).thenReturn(singletonList(sickNote));

        final List<SickNote> sickNotes = sut.getByPeriod(from, to);
        assertThat(sickNotes).contains(sickNote);
    }

    @Test
    void getAllActiveByYear() {
        final SickNote sickNote = new SickNote();
        when(sickNoteRepository.findAllActiveByYear(2017)).thenReturn(singletonList(sickNote));

        final List<SickNote> sickNotes = sut.getAllActiveByYear(2017);
        assertThat(sickNotes).contains(sickNote);
    }

    @Test
    void getNumberOfPersonsWithMinimumOneSickNote() {
        when(sickNoteRepository.findNumberOfPersonsWithMinimumOneSickNote(2017)).thenReturn(5L);

        final Long numberOfPersonsWithMinimumOneSickNote = sut.getNumberOfPersonsWithMinimumOneSickNote(2017);
        assertThat(numberOfPersonsWithMinimumOneSickNote).isSameAs(5L);
    }

    @Test
    void getSickNotesReachingEndOfSickPay() {

        final AbsenceSettings absenceSettings = new AbsenceSettings();
        absenceSettings.setMaximumSickPayDays(5);

        final Settings settings = new Settings();
        settings.setAbsenceSettings(absenceSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        final SickNote sickNote = new SickNote();
        when(sickNoteRepository.findSickNotesByMinimumLengthAndEndDate(eq(5), any(LocalDate.class))).thenReturn(singletonList(sickNote));

        final List<SickNote> sickNotesReachingEndOfSickPay = sut.getSickNotesReachingEndOfSickPay();
        assertThat(sickNotesReachingEndOfSickPay).contains(sickNote);
    }

    @Test
    void getForStates() {
        final List<SickNoteStatus> openSickNoteStatuses = List.of(ACTIVE);

        final SickNote sickNote = new SickNote();
        when(sickNoteRepository.findByStatusIn(openSickNoteStatuses)).thenReturn(List.of(sickNote));

        final List<SickNote> sickNotes = sut.getForStates(openSickNoteStatuses);
        assertThat(sickNotes)
            .hasSize(1)
            .contains(sickNote);
    }


    @Test
    void getForStatesAndPerson() {
        final Person person = new Person();
        final List<Person> persons = List.of(person);
        final List<SickNoteStatus> openSickNoteStatuses = List.of(ACTIVE);

        final SickNote sickNote = new SickNote();
        when(sickNoteRepository.findByStatusInAndPersonIn(openSickNoteStatuses, persons)).thenReturn(List.of(sickNote));

        final List<SickNote> sickNotes = sut.getForStatesAndPerson(openSickNoteStatuses, persons);
        assertThat(sickNotes)
            .hasSize(1)
            .contains(sickNote);
    }
}
