package org.synyx.urlaubsverwaltung.web;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Consumer;


public class FilterPeriodTest {

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfInitializedWithNullStartDate() {

        new FilterPeriod(null, DateMidnight.now());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfInitializedWithNullEndDate() {

        new FilterPeriod(DateMidnight.now(), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfInitializedWithEndDateThatIsBeforeStartDate() {

        DateMidnight now = DateMidnight.now();

        new FilterPeriod(now, now.minusDays(1));
    }


    @Test
    public void ensureCanBeInitializedWithStartAndEndDate() {

        DateMidnight now = DateMidnight.now();
        DateMidnight later = now.plusDays(2);

        FilterPeriod period = new FilterPeriod(now, later);

        Assert.assertNotNull("Start date should not be null", period.getStartDate());
        Assert.assertEquals("Wrong start date", now, period.getStartDate());

        Assert.assertNotNull("End date should not be null", period.getEndDate());
        Assert.assertEquals("Wrong end date", later, period.getEndDate());
    }


    @Test
    public void ensureCanBeInitializedWithSameStartAndEndDate() {

        DateMidnight now = DateMidnight.now();

        FilterPeriod period = new FilterPeriod(now, now);

        Assert.assertNotNull("Start date should not be null", period.getStartDate());
        Assert.assertEquals("Wrong start date", now, period.getStartDate());

        Assert.assertNotNull("End date should not be null", period.getEndDate());
        Assert.assertEquals("Wrong end date", now, period.getEndDate());
    }


    @Test
    public void ensureHasDefaultStartAndEndDateIfInitializedWithoutStartAndEndDate() {

        DateMidnight now = DateMidnight.now();

        FilterPeriod period = new FilterPeriod();

        Assert.assertNotNull("Start date should not be null", period.getStartDate());
        Assert.assertEquals("Wrong start date", new DateMidnight(now.getYear(), DateTimeConstants.JANUARY, 1),
            period.getStartDate());

        Assert.assertNotNull("End date should not be null", period.getEndDate());
        Assert.assertEquals("Wrong end date", new DateMidnight(now.getYear(), DateTimeConstants.DECEMBER, 31),
            period.getEndDate());
    }


    @Test
    public void ensureReturnsCorrectStringRepresentationOfDates() {

        DateMidnight startDate = new DateMidnight(2015, 12, 13);
        DateMidnight endDate = new DateMidnight(2016, 1, 6);

        FilterPeriod period = new FilterPeriod(startDate, endDate);

        Assert.assertNotNull("Should not be null", period.getStartDateAsString());
        Assert.assertEquals("Wrong string representation of start date", "13.12.2015", period.getStartDateAsString());

        Assert.assertNotNull("Should not be null", period.getEndDateAsString());
        Assert.assertEquals("Wrong string representation of end date", "06.01.2016", period.getEndDateAsString());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfInitializedWithNullStartDateString() {

        new FilterPeriod(null, Optional.of("19.05.2015"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfInitializedWithNullEndDateString() {

        new FilterPeriod(Optional.of("19.05.2015"), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfInitializedWithEndDateStringThatIsBeforeStartDateString() {

        new FilterPeriod(Optional.of("21.12.2015"), Optional.of("19.05.2015"));
    }


    @Test
    public void ensureDatesCanBeParsedFromString() {

        DateMidnight startDate = new DateMidnight(2015, DateTimeConstants.MAY, 19);
        DateMidnight endDate = new DateMidnight(2015, DateTimeConstants.DECEMBER, 21);

        FilterPeriod period = new FilterPeriod(Optional.of("19.05.2015"), Optional.of("21.12.2015"));

        Assert.assertNotNull("Start date should not be null", period.getStartDate());
        Assert.assertEquals("Wrong start date", startDate, period.getStartDate());

        Assert.assertNotNull("End date should not be null", period.getEndDate());
        Assert.assertEquals("Wrong end date", endDate, period.getEndDate());
    }


    @Test
    public void ensureDefaultDatesForEmptyStrings() {

        DateMidnight now = DateMidnight.now();

        FilterPeriod period = new FilterPeriod(Optional.empty(), Optional.empty());

        Assert.assertNotNull("Start date should not be null", period.getStartDate());
        Assert.assertEquals("Wrong start date", new DateMidnight(now.getYear(), DateTimeConstants.JANUARY, 1),
            period.getStartDate());

        Assert.assertNotNull("End date should not be null", period.getEndDate());
        Assert.assertEquals("Wrong end date", new DateMidnight(now.getYear(), DateTimeConstants.DECEMBER, 31),
            period.getEndDate());
    }


    @Test
    public void ensureThrowsIfTryingToParseDatesWithUnsupportedFormat() {

        Consumer<String> assertDateFormatNotSupported = (dateString) -> {
            try {
                new FilterPeriod(Optional.of(dateString), Optional.of(dateString));
                Assert.fail("Should throw for date string = " + dateString);
            } catch (IllegalArgumentException ex) {
                // Expected
            }
        };

        assertDateFormatNotSupported.accept("2015-12-13");
        assertDateFormatNotSupported.accept("12/13/2015");
        assertDateFormatNotSupported.accept("13-12-2015");
    }
}
