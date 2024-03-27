package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.stream.Stream.concat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.CONVERTED_TO_VACATION;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.SUBMITTED;

class SickNoteStatusTest {

    @Test
    void ensureActiveApplicationStatusAreCorrectlyDefined() {
        assertThat(SickNoteStatus.activeStatuses())
            .containsExactly(SUBMITTED, ACTIVE);
    }

    @Test
    void ensureInactiveApplicationStatusAreCorrectlyDefined() {
        assertThat(SickNoteStatus.inactiveStatuses())
            .containsExactly(CONVERTED_TO_VACATION, CANCELLED);
    }

    @Test
    void ensureThatEveryStatusWillBePartOfActiveOrInactive() {
        final List<SickNoteStatus> combinedList = concat(SickNoteStatus.inactiveStatuses().stream(), SickNoteStatus.activeStatuses().stream()).toList();
        assertThat(SickNoteStatus.values()).hasSameElementsAs(combinedList);
    }

}
