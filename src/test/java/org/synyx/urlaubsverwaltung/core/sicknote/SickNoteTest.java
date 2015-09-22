package org.synyx.urlaubsverwaltung.core.sicknote;

import org.junit.Assert;

import org.joda.time.DateMidnight;

import org.junit.Test;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.sicknote.SickNote}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteTest {

    @Test
    public void ensureAUBIsPresentIfAUBStartDateAndAUBEndDateAreSet() {

        SickNote sickNote = new SickNote();
        sickNote.setAubStartDate(DateMidnight.now());
        sickNote.setAubEndDate(DateMidnight.now());

        Assert.assertTrue("AUB should be present", sickNote.isAubPresent());
    }


    @Test
    public void ensureAUBIsNotPresentIfOnlyAUBStartDateIsSet() {

        SickNote sickNote = new SickNote();
        sickNote.setAubStartDate(DateMidnight.now());

        Assert.assertFalse("AUB should not be present", sickNote.isAubPresent());
    }


    @Test
    public void ensureAUBIsNotPresentIfOnlyAUBEndDateIsSet() {

        SickNote sickNote = new SickNote();
        sickNote.setAubEndDate(DateMidnight.now());

        Assert.assertFalse("AUB should not be present", sickNote.isAubPresent());
    }


    @Test
    public void ensureAUBIsNotPresentIfNoAUBPeriodIsSet() {

        Assert.assertFalse("AUB should not be present", new SickNote().isAubPresent());
    }
}
