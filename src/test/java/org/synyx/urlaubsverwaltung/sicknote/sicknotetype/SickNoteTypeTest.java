package org.synyx.urlaubsverwaltung.sicknote.sicknotetype;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;

class SickNoteTypeTest {

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

    @Test
    void equals() {
        final SickNoteType sickNoteTypeOne = new SickNoteType();
        sickNoteTypeOne.setId(1L);

        final SickNoteType sickNoteTypeOneOne = new SickNoteType();
        sickNoteTypeOneOne.setId(1L);

        final SickNoteType sickNoteTypeTwo = new SickNoteType();
        sickNoteTypeTwo.setId(2L);

        assertThat(sickNoteTypeOne)
            .isEqualTo(sickNoteTypeOne)
            .isEqualTo(sickNoteTypeOneOne)
            .isNotEqualTo(sickNoteTypeTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final SickNoteType sickNoteTypeOne = new SickNoteType();
        sickNoteTypeOne.setId(1L);

        assertThat(sickNoteTypeOne.hashCode()).isEqualTo(32);
    }
}
