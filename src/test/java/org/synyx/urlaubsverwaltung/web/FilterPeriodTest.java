package org.synyx.urlaubsverwaltung.web;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.MAY;
import static java.time.ZoneOffset.UTC;


public class FilterPeriodTest {

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfInitializedWithNullStartDate() {

        new FilterPeriod(null, LocalDate.now(UTC));
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfInitializedWithNullEndDate() {

        new FilterPeriod(LocalDate.now(UTC), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfInitializedWithEndDateThatIsBeforeStartDate() {

        LocalDate now = LocalDate.now(UTC);

        new FilterPeriod(now, now.minusDays(1));
    }


    @Test
    public void ensureCanBeInitializedWithStartAndEndDate() {

        LocalDate now = LocalDate.now(UTC);
        LocalDate later = now.plusDays(2);

        FilterPeriod period = new FilterPeriod(now, later);

        Assert.assertNotNull("Start date should not be null", period.getStartDate());
        Assert.assertEquals("Wrong start date", now, period.getStartDate());

        Assert.assertNotNull("End date should not be null", period.getEndDate());
        Assert.assertEquals("Wrong end date", later, period.getEndDate());
    }


    @Test
    public void ensureCanBeInitializedWithSameStartAndEndDate() {

        LocalDate now = LocalDate.now(UTC);

        FilterPeriod period = new FilterPeriod(now, now);

        Assert.assertNotNull("Start date should not be null", period.getStartDate());
        Assert.assertEquals("Wrong start date", now, period.getStartDate());

        Assert.assertNotNull("End date should not be null", period.getEndDate());
        Assert.assertEquals("Wrong end date", now, period.getEndDate());
    }


    @Test
    public void ensureHasDefaultStartAndEndDateIfInitializedWithoutStartAndEndDate() {

        LocalDate now = LocalDate.now(UTC);

        FilterPeriod period = new FilterPeriod();

        Assert.assertNotNull("Start date should not be null", period.getStartDate());
        Assert.assertEquals("Wrong start date", LocalDate.of(now.getYear(), JANUARY, 1),
            period.getStartDate());

        Assert.assertNotNull("End date should not be null", period.getEndDate());
        Assert.assertEquals("Wrong end date", LocalDate.of(now.getYear(), DECEMBER, 31),
            period.getEndDate());
    }


    @Test
    public void ensureReturnsCorrectStringRepresentationOfDates() {

        LocalDate startDate = LocalDate.of(2015, 12, 13);
        LocalDate endDate = LocalDate.of(2016, 1, 6);

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

        LocalDate startDate = LocalDate.of(2015, MAY, 19);
        LocalDate endDate = LocalDate.of(2015, DECEMBER, 21);

        FilterPeriod period = new FilterPeriod(Optional.of("19.05.2015"), Optional.of("21.12.2015"));

        Assert.assertNotNull("Start date should not be null", period.getStartDate());
        Assert.assertEquals("Wrong start date", startDate, period.getStartDate());

        Assert.assertNotNull("End date should not be null", period.getEndDate());
        Assert.assertEquals("Wrong end date", endDate, period.getEndDate());
    }


    @Test
    public void ensureDefaultDatesForEmptyStrings() {

        LocalDate now = LocalDate.now(UTC);

        FilterPeriod period = new FilterPeriod(Optional.empty(), Optional.empty());

        Assert.assertNotNull("Start date should not be null", period.getStartDate());
        Assert.assertEquals("Wrong start date", LocalDate.of(now.getYear(), JANUARY, 1),
            period.getStartDate());

        Assert.assertNotNull("End date should not be null", period.getEndDate());
        Assert.assertEquals("Wrong end date", LocalDate.of(now.getYear(), DECEMBER, 31),
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
