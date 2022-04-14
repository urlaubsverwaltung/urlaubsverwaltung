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
    void ensureToFindSickNotesForNotificationOfSickPayEnd() {

        // lohnfortzahlung from 01.02.2022 until 14.03.2022
        // 7 days before notification on 07.03.2022
        // --> SickNote will be found
        final LocalDate startDate = LocalDate.of(2022, 2, 1);
        final LocalDate endDate = LocalDate.of(2022, 3, 30);

        final SickNote activeSickNote = createSickNote(null, startDate, endDate, ACTIVE);
        sickNoteRepository.save(activeSickNote);

        final int maximumSickPayDays = 42;
        final int daysBeforeEndOfSickPayNotification = 7;
        final LocalDate today = LocalDate.of(2022, 3, 7);

        final List<SickNote> sickNotes = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(maximumSickPayDays, daysBeforeEndOfSickPayNotification, today);
        assertThat(sickNotes)
            .containsExactly(activeSickNote);
    }

    @Test
    void ensureNotToFindSickNotesForNotificationOfSickPayEndBeforeNotificationDate() {

        // lohnfortzahlung from 01.02.2022 until 14.03.2022
        // 7 days before notification on 07.03.2022
        // but asks on 06.03.2022
        // --> No SickNote will be found
        final LocalDate startDate = LocalDate.of(2022, 2, 1);
        final LocalDate endDate = LocalDate.of(2022, 3, 30);

        final SickNote activeSickNote = createSickNote(null, startDate, endDate, ACTIVE);
        sickNoteRepository.save(activeSickNote);

        final int maximumSickPayDays = 42;
        final int daysBeforeEndOfSickPayNotification = 7;
        final LocalDate today = LocalDate.of(2022, 3, 6);

        final List<SickNote> sickNotes = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(maximumSickPayDays, daysBeforeEndOfSickPayNotification, today);
        assertThat(sickNotes).isEmpty();
    }

    @Test
    void ensureToFindSickNotesForNotificationOfSickPayEndIsFurtherInThePast() {

        // lohnfortzahlung from 01.02.2022 until 14.03.2022
        // 7 days before notification on 07.03.2022
        // and asks on 08.03.2022
        // --> SickNote will be found
        final LocalDate startDate = LocalDate.of(2022, 2, 1);
        final LocalDate endDate = LocalDate.of(2022, 3, 30);

        final SickNote activeSickNote = createSickNote(null, startDate, endDate, ACTIVE);
        sickNoteRepository.save(activeSickNote);

        final int maximumSickPayDays = 42;
        final int daysBeforeEndOfSickPayNotification = 7;
        final LocalDate today = LocalDate.of(2022, 3, 8);

        final List<SickNote> sickNotes = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(maximumSickPayDays, daysBeforeEndOfSickPayNotification, today);
        assertThat(sickNotes)
            .containsExactly(activeSickNote);
    }

    @Test
    void ensureNotToFindSickNotesForNotificationOfSickPayEndBecauseMaximumSickPayDaysNotReached() {

        // lohnfortzahlung from 01.02.2022 until 14.03.2022
        // 7 days before notification on 07.03.2022
        // but sick note period only until 14.03.2022
        // --> No sick note will be found
        final LocalDate startDate = LocalDate.of(2022, 2, 1);
        final LocalDate endDate = LocalDate.of(2022, 3, 14);

        final SickNote activeSickNote = createSickNote(null, startDate, endDate, ACTIVE);
        sickNoteRepository.save(activeSickNote);

        final int maximumSickPayDays = 42;
        final int daysBeforeEndOfSickPayNotification = 7;
        final LocalDate today = LocalDate.of(2022, 3, 7);

        final List<SickNote> sickNotes = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(maximumSickPayDays, daysBeforeEndOfSickPayNotification, today);
        assertThat(sickNotes).isEmpty();
    }

    @Test
    void ensureToNotFindSickNotesWithWrongStatus() {

        // lohnfortzahlung from 01.02.2022 until 14.03.2022
        // 7 days before notification on 07.03.2022
        // but sick notes not of type active
        // --> No sick note will be found
        final LocalDate startDate = LocalDate.of(2022, 2, 1);
        final LocalDate endDate = LocalDate.of(2022, 3, 15);

        final SickNote activeSickNote = createSickNote(null, startDate, endDate, ACTIVE);
        sickNoteRepository.save(activeSickNote);

        final SickNote cancelledSickNote = createSickNote(null, startDate, endDate, CANCELLED);
        sickNoteRepository.save(cancelledSickNote);

        final int maximumSickPayDays = 42;
        final int daysBeforeEndOfSickPayNotification = 7;
        final LocalDate today = LocalDate.of(2022, 3, 7);

        final List<SickNote> sickNotes = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(maximumSickPayDays, daysBeforeEndOfSickPayNotification, today);
        assertThat(sickNotes)
            .hasSize(1)
            .contains(activeSickNote)
            .doesNotContain(cancelledSickNote);
    }


    @Test
    void findNoSickNoteIfAlreadyNotifiedAfterLastEdit() {

        // lohnfortzahlung from 01.02.2022 until 14.03.2022
        // 7 days before notification on 07.03.2022
        // but already notified is after last edit
        // --> No sick note will be found
        final LocalDate startDate = LocalDate.of(2022, 2, 1);
        final LocalDate endDate = LocalDate.of(2022, 3, 15);

        final SickNote sickNote = createSickNote(null, startDate, endDate, ACTIVE);
        sickNote.setLastEdited(startDate);
        sickNote.setEndOfSickPayNotificationSend(endDate);
        sickNoteRepository.save(sickNote);

        final int maximumSickPayDays = 42;
        final int daysBeforeEndOfSickPayNotification = 7;
        final LocalDate today = LocalDate.of(2022, 3, 7);

        final List<SickNote> sickNotes = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(maximumSickPayDays, daysBeforeEndOfSickPayNotification, today);
        assertThat(sickNotes).isEmpty();
    }

    @Test
    void findSickNoteIfNotYetNotified() {

        // lohnfortzahlung from 01.02.2022 until 14.03.2022
        // 7 days before notification on 07.03.2022
        // and was not already sent
        // --> sick note will be found
        final LocalDate startDate = LocalDate.of(2022, 2, 1);
        final LocalDate endDate = LocalDate.of(2022, 3, 15);

        final SickNote sickNote = createSickNote(null, startDate, endDate, ACTIVE);
        sickNote.setLastEdited(startDate);
        sickNoteRepository.save(sickNote);

        final int maximumSickPayDays = 42;
        final int daysBeforeEndOfSickPayNotification = 7;
        final LocalDate today = LocalDate.of(2022, 3, 7);

        final List<SickNote> sickNotes = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(maximumSickPayDays, daysBeforeEndOfSickPayNotification, today);
        assertThat(sickNotes)
            .hasSize(1)
            .contains(sickNote);
    }

    @Test
    void findSickNoteIfCurrentEditStateNotNotifiedBefore() {

        // lohnfortzahlung from 01.02.2022 until 14.03.2022
        // 7 days before notification on 07.03.2022
        // and was not already sent
        // --> sick note will be found
        final LocalDate startDate = LocalDate.of(2022, 2, 1);
        final LocalDate endDate = LocalDate.of(2022, 3, 15);

        final SickNote sickNote = createSickNote(null, startDate, endDate, ACTIVE);
        sickNote.setLastEdited(endDate);
        sickNote.setEndOfSickPayNotificationSend(startDate);
        sickNoteRepository.save(sickNote);

        final int maximumSickPayDays = 42;
        final int daysBeforeEndOfSickPayNotification = 7;
        final LocalDate today = LocalDate.of(2022, 3, 7);

        final List<SickNote> sickNotes = sickNoteRepository.findSickNotesToNotifyForSickPayEnd(maximumSickPayDays, daysBeforeEndOfSickPayNotification, today);
        assertThat(sickNotes)
            .hasSize(1)
            .contains(sickNote);
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
