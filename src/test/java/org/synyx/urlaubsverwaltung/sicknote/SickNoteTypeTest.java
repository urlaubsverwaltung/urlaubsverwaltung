package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.Assert;
import org.junit.Test;


public class SickNoteTypeTest {

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfCheckingCategoryWithNull() {

        new SickNoteType().isOfCategory(null);
    }


    @Test
    public void ensureReturnsTrueIfTypeIsOfGivenCategory() {

        SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);

        Assert.assertTrue("Categories should match", sickNoteType.isOfCategory(SickNoteCategory.SICK_NOTE));
    }


    @Test
    public void ensureReturnsFalseIfTypeIsNotOfGivenCategory() {

        SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);

        Assert.assertFalse("Categories should not match", sickNoteType.isOfCategory(SickNoteCategory.SICK_NOTE_CHILD));
    }
}
