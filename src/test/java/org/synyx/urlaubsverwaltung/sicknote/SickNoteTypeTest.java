package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory.SICK_NOTE_CHILD;


class SickNoteTypeTest {

    @Test
    void ensureThrowsIfCheckingCategoryWithNull() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new SickNoteType().isOfCategory(null));
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

    @Test
    void equals() {
        final SickNoteType sickNoteTypeOne = new SickNoteType();
        sickNoteTypeOne.setId(1);

        final SickNoteType sickNoteTypeOneOne = new SickNoteType();
        sickNoteTypeOneOne.setId(1);

        final SickNoteType sickNoteTypeTwo = new SickNoteType();
        sickNoteTypeTwo.setId(2);

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
        sickNoteTypeOne.setId(1);

        assertThat(sickNoteTypeOne.hashCode()).isEqualTo(32);
    }
}
