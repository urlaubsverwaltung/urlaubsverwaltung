package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


class SickNoteTypeTest {

    @Test
    void ensureThrowsIfCheckingCategoryWithNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new SickNoteType().isOfCategory(null));
    }

    @Test
    void ensureReturnsTrueIfTypeIsOfGivenCategory() {

        SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);

        Assert.assertTrue("Categories should match", sickNoteType.isOfCategory(SickNoteCategory.SICK_NOTE));
    }

    @Test
    void ensureReturnsFalseIfTypeIsNotOfGivenCategory() {

        SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);

        Assert.assertFalse("Categories should not match", sickNoteType.isOfCategory(SickNoteCategory.SICK_NOTE_CHILD));
    }
}
