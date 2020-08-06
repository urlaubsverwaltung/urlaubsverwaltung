package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory.SICK_NOTE_CHILD;


class SickNoteTypeTest {

    @Test
    void ensureThrowsIfCheckingCategoryWithNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new SickNoteType().isOfCategory(null));
    }

    @Test
    void ensureReturnsTrueIfTypeIsOfGivenCategory() {

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SICK_NOTE);

        assertThat(sickNoteType.isOfCategory(SICK_NOTE)).isTrue();
    }

    @Test
    void ensureReturnsFalseIfTypeIsNotOfGivenCategory() {

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SICK_NOTE);

        assertThat(sickNoteType.isOfCategory(SICK_NOTE_CHILD)).isFalse();
    }
}
