package org.synyx.urlaubsverwaltung.web;

import org.junit.Assert;
import org.junit.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.function.Consumer;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThat;


public class FilterPeriodTest {

    @Test
    public void returnFirstDayOfYearWithNullStartDateString() {
        int currentYear = Year.now(Clock.systemUTC()).getValue();

        FilterPeriod filterPeriod = new FilterPeriod(null, "19.05." + currentYear);
        assertThat(filterPeriod.getStartDateAsString()).isEqualTo("01.01." + currentYear);
    }


    @Test
    public void returnLastDayOfYearWithNullEndDateString() {
        int currentYear = Year.now(Clock.systemUTC()).getValue();

        FilterPeriod filterPeriod = new FilterPeriod("19.05." + currentYear, null);
        assertThat(filterPeriod.getEndDateAsString()).isEqualTo("31.12." + currentYear);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfInitializedWithEndDateStringThatIsBeforeStartDateString() {

        new FilterPeriod("21.12.2015", "19.05.2015");
    }


    @Test
    public void ensureDatesCanBeParsedFromString() {

        LocalDate startDate = LocalDate.of(2015, MAY, 19);
        LocalDate endDate = LocalDate.of(2015, DECEMBER, 21);

        FilterPeriod period = new FilterPeriod("19.05.2015", "21.12.2015");

        Assert.assertNotNull("Start date should not be null", period.getStartDate());
        Assert.assertEquals("Wrong start date", startDate, period.getStartDate());

        Assert.assertNotNull("End date should not be null", period.getEndDate());
        Assert.assertEquals("Wrong end date", endDate, period.getEndDate());
    }


    @Test
    public void ensureDefaultDatesForEmptyStrings() {

        LocalDate now = LocalDate.now(Clock.systemUTC());

        FilterPeriod period = new FilterPeriod("", "");

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
                new FilterPeriod(dateString, dateString);
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
