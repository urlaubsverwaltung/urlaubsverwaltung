package org.synyx.urlaubsverwaltung.core.sicknote;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Consumer;


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


    @Test
    public void ensureIsNotActiveForInactiveStatus() {

        Consumer<SickNoteStatus> assertNotActive = (status) -> {
            SickNote sickNote = new SickNote();
            sickNote.setStatus(status);

            Assert.assertFalse("Should be inactive for status " + status, sickNote.isActive());
        };

        assertNotActive.accept(SickNoteStatus.CANCELLED);
        assertNotActive.accept(SickNoteStatus.CONVERTED_TO_VACATION);
    }


    @Test
    public void ensureIsActiveForActiveStatus() {

        Consumer<SickNoteStatus> assertActive = (status) -> {
            SickNote sickNote = new SickNote();
            sickNote.setStatus(status);

            Assert.assertTrue("Should be active for status " + status, sickNote.isActive());
        };

        assertActive.accept(SickNoteStatus.ACTIVE);
    }
}
