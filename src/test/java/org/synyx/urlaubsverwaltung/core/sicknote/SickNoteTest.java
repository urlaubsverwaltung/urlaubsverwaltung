package org.synyx.urlaubsverwaltung.core.sicknote;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.junit.Assert;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.period.Period;

import java.util.function.Consumer;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.sicknote.SickNote}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteTest {

    @Test
    public void ensureLastModificationDateIsSetOnInitialization() {

        SickNote sickNote = new SickNote();

        Assert.assertNotNull("Last modification date should be set", sickNote.getLastEdited());
        Assert.assertEquals("Wrong last modification date", DateTime.now().withTimeAtStartOfDay(),
            sickNote.getLastEdited());
    }


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


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetPeriodForSickNoteWithoutStartDate() {

        SickNote sickNote = new SickNote();
        sickNote.setStartDate(null);
        sickNote.setEndDate(DateMidnight.now());
        sickNote.setDayLength(DayLength.FULL);

        sickNote.getPeriod();
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetPeriodForSickNoteWithoutEndDate() {

        SickNote sickNote = new SickNote();
        sickNote.setStartDate(DateMidnight.now());
        sickNote.setEndDate(null);
        sickNote.setDayLength(DayLength.FULL);

        sickNote.getPeriod();
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetPeriodForSickNoteWithoutDayLength() {

        SickNote sickNote = new SickNote();
        sickNote.setStartDate(DateMidnight.now());
        sickNote.setEndDate(DateMidnight.now());
        sickNote.setDayLength(null);

        sickNote.getPeriod();
    }


    @Test
    public void ensureGetPeriodReturnsCorrectPeriod() {

        DateMidnight startDate = DateMidnight.now();
        DateMidnight endDate = startDate.plusDays(2);

        SickNote sickNote = new SickNote();
        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);
        sickNote.setDayLength(DayLength.FULL);

        Period period = sickNote.getPeriod();

        Assert.assertNotNull("Period should not be null", period);
        Assert.assertEquals("Wrong period start date", startDate, period.getStartDate());
        Assert.assertEquals("Wrong period end date", endDate, period.getEndDate());
        Assert.assertEquals("Wrong period day length", DayLength.FULL, period.getDayLength());
    }
}
