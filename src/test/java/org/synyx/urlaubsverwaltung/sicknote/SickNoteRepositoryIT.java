package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.CANCELLED;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class SickNoteRepositoryIT {

    @Autowired
    private SickNoteRepository sickNoteRepository;

    @Test
    void findSickNotesByMinimumLengthAndEndDateLessThanLimitAndWrongStatus() {

        final LocalDate endDate = LocalDate.of(2019, 5, 20);

        final SickNote sickNote = createSickNote(LocalDate.of(2019, 5, 19), endDate, ACTIVE);
        sickNoteRepository.save(sickNote);

        final SickNote sickNoteCancelled = createSickNote(LocalDate.of(2019, 5, 10), endDate, CANCELLED);
        sickNoteRepository.save(sickNoteCancelled);

        final List<SickNote> sickNotesByMinimumLengthAndEndDate = sickNoteRepository.findSickNotesByMinimumLengthAndEndDate(2, endDate);
        assertThat(sickNotesByMinimumLengthAndEndDate).isEmpty();
    }

    @Test
    void findSickNotesByMinimumLengthAndEndDateExactlyOnLimitAndWrongStatus() {

        final LocalDate startDate = LocalDate.of(2019, 5, 19);
        final LocalDate endDate = LocalDate.of(2019, 5, 20);

        final SickNote sickNote = createSickNote(startDate, endDate, ACTIVE);
        sickNoteRepository.save(sickNote);

        final SickNote sickNoteCancelled = createSickNote(startDate, endDate, CANCELLED);
        sickNoteRepository.save(sickNoteCancelled);

        final List<SickNote> sickNotesByMinimumLengthAndEndDate = sickNoteRepository.findSickNotesByMinimumLengthAndEndDate(1, endDate);
        assertThat(sickNotesByMinimumLengthAndEndDate)
            .hasSize(1)
            .contains(sickNote)
            .doesNotContain(sickNoteCancelled);
    }

    @Test
    void findSickNotesByMinimumLengthAndEndDateMoreThanLimitAndWrongStatus() {

        final LocalDate startDate = LocalDate.of(2019, 5, 19);
        final LocalDate endDate = LocalDate.of(2019, 5, 25);

        final SickNote sickNote = createSickNote(startDate, endDate, ACTIVE);
        sickNoteRepository.save(sickNote);

        final SickNote sickNoteCancelled = createSickNote(startDate, endDate, CANCELLED);
        sickNoteRepository.save(sickNoteCancelled);

        final List<SickNote> sickNotesByMinimumLengthAndEndDate = sickNoteRepository.findSickNotesByMinimumLengthAndEndDate(1, endDate);
        assertThat(sickNotesByMinimumLengthAndEndDate)
            .hasSize(1)
            .contains(sickNote)
            .doesNotContain(sickNoteCancelled);
    }

    private SickNote createSickNote(LocalDate startDate, LocalDate endDate, SickNoteStatus active) {
        final SickNote sickNote = new SickNote();
        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);
        sickNote.setStatus(active);
        return sickNote;
    }
}
