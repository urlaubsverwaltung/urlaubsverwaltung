package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;

import java.time.LocalDate;
import java.util.function.Consumer;

import static java.time.ZoneOffset.UTC;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.sicknote.SickNote}.
 */
public class SickNoteTest {

    @Test
    public void ensureLastModificationDateIsSetOnInitialization() {

        SickNote sickNote = new SickNote();

        Assert.assertNotNull("Last modification date should be set", sickNote.getLastEdited());
        Assert.assertEquals("Wrong last modification date", LocalDate.now(UTC),
            sickNote.getLastEdited());
    }


    @Test
    public void ensureAUBIsPresentIfAUBStartDateAndAUBEndDateAreSet() {

        SickNote sickNote = new SickNote();
        sickNote.setAubStartDate(LocalDate.now(UTC));
        sickNote.setAubEndDate(LocalDate.now(UTC));

        Assert.assertTrue("AUB should be present", sickNote.isAubPresent());
    }


    @Test
    public void ensureAUBIsNotPresentIfOnlyAUBStartDateIsSet() {

        SickNote sickNote = new SickNote();
        sickNote.setAubStartDate(LocalDate.now(UTC));

        Assert.assertFalse("AUB should not be present", sickNote.isAubPresent());
    }


    @Test
    public void ensureAUBIsNotPresentIfOnlyAUBEndDateIsSet() {

        SickNote sickNote = new SickNote();
        sickNote.setAubEndDate(LocalDate.now(UTC));

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
        sickNote.setEndDate(LocalDate.now(UTC));
        sickNote.setDayLength(DayLength.FULL);

        sickNote.getPeriod();
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetPeriodForSickNoteWithoutEndDate() {

        SickNote sickNote = new SickNote();
        sickNote.setStartDate(LocalDate.now(UTC));
        sickNote.setEndDate(null);
        sickNote.setDayLength(DayLength.FULL);

        sickNote.getPeriod();
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetPeriodForSickNoteWithoutDayLength() {

        SickNote sickNote = new SickNote();
        sickNote.setStartDate(LocalDate.now(UTC));
        sickNote.setEndDate(LocalDate.now(UTC));
        sickNote.setDayLength(null);

        sickNote.getPeriod();
    }


    @Test
    public void ensureGetPeriodReturnsCorrectPeriod() {

        LocalDate startDate = LocalDate.now(UTC);
        LocalDate endDate = startDate.plusDays(2);

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
