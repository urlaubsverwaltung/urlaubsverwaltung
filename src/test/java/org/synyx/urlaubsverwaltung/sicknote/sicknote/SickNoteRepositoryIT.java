package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.CANCELLED;

@SpringBootTest
@Transactional
class SickNoteRepositoryIT extends TestContainersBase {

    @Autowired
    private SickNoteRepository sickNoteRepository;

    @Autowired
    private PersonService personService;

    @Test
    void findSickNotesByMinimumLengthAndEndDateLessThanLimitAndWrongStatus() {

        final LocalDate endDate = LocalDate.of(2019, 5, 20);

        final SickNote sickNote = createSickNote(null, LocalDate.of(2019, 5, 19), endDate, ACTIVE);
        sickNoteRepository.save(sickNote);

        final SickNote sickNoteCancelled = createSickNote(null, LocalDate.of(2019, 5, 10), endDate, CANCELLED);
        sickNoteRepository.save(sickNoteCancelled);

        final List<SickNote> sickNotesByMinimumLengthAndEndDate = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(2, endDate);
        assertThat(sickNotesByMinimumLengthAndEndDate).isEmpty();
    }

    @Test
    void findSickNotesByMinimumLengthAndEndDateExactlyOnLimitAndWrongStatus() {

        final LocalDate startDate = LocalDate.of(2019, 5, 19);
        final LocalDate endDate = LocalDate.of(2019, 5, 21);

        final SickNote sickNote = createSickNote(null, startDate, endDate, ACTIVE);
        sickNoteRepository.save(sickNote);

        final SickNote sickNoteCancelled = createSickNote(null, startDate, endDate, CANCELLED);
        sickNoteRepository.save(sickNoteCancelled);

        final List<SickNote> sickNotesToNotifyForSickPayEnd = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(1, endDate);
        assertThat(sickNotesToNotifyForSickPayEnd)
            .hasSize(1)
            .contains(sickNote)
            .doesNotContain(sickNoteCancelled);
    }

    @Test
    void findNoSickNotesByMinimumLengthAndEndDateEqualsLimit() {

        final LocalDate startDate = LocalDate.of(2019, 5, 19);
        final LocalDate endDate = LocalDate.of(2019, 5, 20);

        final SickNote sickNote = createSickNote(null, startDate, endDate, ACTIVE);
        sickNoteRepository.save(sickNote);

        final List<SickNote> sickNotesToNotifyForSickPayEnd = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(1, endDate);
        assertThat(sickNotesToNotifyForSickPayEnd).isEmpty();
    }


    @Test
    void findSickNotesByMinimumLengthAndEndDateMoreThanLimitAndWrongStatus() {

        final LocalDate startDate = LocalDate.of(2019, 5, 19);
        final LocalDate endDate = LocalDate.of(2019, 5, 25);

        final SickNote sickNote = createSickNote(null, startDate, endDate, ACTIVE);
        sickNoteRepository.save(sickNote);

        final SickNote sickNoteCancelled = createSickNote(null, startDate, endDate, CANCELLED);
        sickNoteRepository.save(sickNoteCancelled);

        final List<SickNote> sickNotesToNotifyForSickPayEnd = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(1, endDate);
        assertThat(sickNotesToNotifyForSickPayEnd)
            .hasSize(1)
            .contains(sickNote)
            .doesNotContain(sickNoteCancelled);
    }

    @Test
    void findNoSickNoteIfAlreadyNotifiedAfterLastEdit() {

        final LocalDate startDate = LocalDate.of(2019, 5, 19);
        final LocalDate endDate = LocalDate.of(2019, 5, 21);

        final SickNote sickNote = createSickNote(null, startDate, endDate, ACTIVE);
        sickNote.setLastEdited(startDate);
        sickNote.setEndOfSickPayNotificationSend(endDate);
        sickNoteRepository.save(sickNote);

        final List<SickNote> sickNotesToNotifyForSickPayEnd = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(1, endDate);
        assertThat(sickNotesToNotifyForSickPayEnd).isEmpty();
    }

    @Test
    void findSickNoteIfNotYetNotified() {

        final LocalDate startDate = LocalDate.of(2019, 5, 19);
        final LocalDate endDate = LocalDate.of(2019, 5, 21);

        final SickNote sickNote = createSickNote(null, startDate, endDate, ACTIVE);
        sickNote.setLastEdited(startDate);
        sickNoteRepository.save(sickNote);

        final List<SickNote> sickNotesToNotifyForSickPayEnd = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(1, endDate);
        assertThat(sickNotesToNotifyForSickPayEnd)
            .hasSize(1)
            .contains(sickNote);
    }

    @Test
    void findSickNoteIfCurrentEditStateNotNotifiedBefore() {

        final LocalDate startDate = LocalDate.of(2019, 5, 19);
        final LocalDate endDate = LocalDate.of(2019, 5, 21);

        final SickNote sickNote = createSickNote(null, startDate, endDate, ACTIVE);
        sickNote.setLastEdited(endDate);
        sickNote.setEndOfSickPayNotificationSend(startDate);
        sickNoteRepository.save(sickNote);

        final List<SickNote> sickNotesToNotifyForSickPayEnd = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(1, endDate);
        assertThat(sickNotesToNotifyForSickPayEnd)
            .hasSize(1)
            .contains(sickNote);
    }

    @Test
    void findSickNoteIfEndDateIsEqualsOrSmallerThanSearchedEndDate() {

        final LocalDate startDate = LocalDate.of(2019, 5, 19);
        final LocalDate endDate = LocalDate.of(2019, 5, 21);

        final SickNote sickNote = createSickNote(null, startDate, endDate, ACTIVE);
        sickNoteRepository.save(sickNote);

        List<SickNote> sickNotesToNotifyForSickPayEnd = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(1, endDate);
        assertThat(sickNotesToNotifyForSickPayEnd)
            .hasSize(1)
            .contains(sickNote);

        sickNotesToNotifyForSickPayEnd = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(1, endDate.plusDays(1));
        assertThat(sickNotesToNotifyForSickPayEnd)
            .hasSize(1)
            .contains(sickNote);
    }

    @Test
    void findNoSickNoteIfEndDateIsBiggerThanEndDate() {

        final LocalDate startDate = LocalDate.of(2019, 5, 19);
        final LocalDate endDate = LocalDate.of(2019, 5, 21);

        final SickNote sickNote = createSickNote(null, startDate, endDate, ACTIVE);
        sickNoteRepository.save(sickNote);

        final List<SickNote> sickNotesToNotifyForSickPayEnd = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(1, endDate.minusDays(1));
        assertThat(sickNotesToNotifyForSickPayEnd).isEmpty();
    }

    @Test
    void findSickNotesOverlappingWithDateRange() {

        final Person max = personService.save(new Person("muster", "Mustermann", "Max", "mustermann@example.org"));
        final Person marlene = personService.save(new Person("person2", "Musterfrau", "Marlene", "musterfrau@example.org"));

        final LocalDate askedStartDate = LocalDate.now(UTC).with(firstDayOfMonth());
        final LocalDate askedEndDate = LocalDate.now(UTC).with(lastDayOfMonth());

        // sick notes that should not be found
        final SickNote noteNotInPeriodBecausePast = createSickNote(max, askedStartDate.minusDays(10), askedStartDate.minusDays(5), ACTIVE);
        final SickNote noteNotInPeriodBecauseFuture = createSickNote(max, askedEndDate.plusDays(5), askedEndDate.plusDays(10), ACTIVE);

        sickNoteRepository.save(noteNotInPeriodBecausePast);
        sickNoteRepository.save(noteNotInPeriodBecauseFuture);

        // sick notes that should be found
        final SickNote noteStartingBeforePeriod = createSickNote(max, askedStartDate.minusDays(5), askedStartDate.plusDays(1), ACTIVE);
        final SickNote noteEndingAfterPeriod = createSickNote(max, askedEndDate.minusDays(1), askedEndDate.plusDays(1), ACTIVE);
        final SickNote noteInBetween = createSickNote(max, askedStartDate.plusDays(10), askedStartDate.plusDays(12), ACTIVE);
        final SickNote noteStartingAtPeriod = createSickNote(marlene, askedStartDate, askedStartDate.plusDays(2), ACTIVE);
        final SickNote noteEndingAtPeriod = createSickNote(marlene, askedEndDate.minusDays(5), askedEndDate, ACTIVE);

        sickNoteRepository.save(noteStartingBeforePeriod);
        sickNoteRepository.save(noteEndingAfterPeriod);
        sickNoteRepository.save(noteInBetween);
        sickNoteRepository.save(noteStartingAtPeriod);
        sickNoteRepository.save(noteEndingAtPeriod);

        List<SickNoteStatus> statuses = List.of(ACTIVE);
        List<Person> persons = List.of(max, marlene);

        final List<SickNote> actualSickNotes = sickNoteRepository.findByStatusInAndPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(statuses, persons, askedStartDate, askedEndDate);

        assertThat(actualSickNotes).contains(noteStartingBeforePeriod, noteEndingAfterPeriod, noteInBetween, noteStartingAtPeriod, noteEndingAtPeriod);
    }

    private SickNote createSickNote(Person person, LocalDate startDate, LocalDate endDate, SickNoteStatus active) {
        final SickNote sickNote = new SickNote();
        sickNote.setPerson(person);
        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);
        sickNote.setStatus(active);
        return sickNote;
    }
}
