package org.synyx.urlaubsverwaltung.web;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.util.function.Consumer;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;



class FilterPeriodTest {

    @Test
    void ensureThrowsIfInitializedWithNullStartDateString() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new FilterPeriod(null, "19.05.2015"));
    }

    @Test
    void ensureThrowsIfInitializedWithNullEndDateString() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new FilterPeriod("19.05.2015", null));
    }

    @Test
    void ensureThrowsIfInitializedWithEndDateStringThatIsBeforeStartDateString() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new FilterPeriod("21.12.2015", "19.05.2015"));
    }

    @Test
    void ensureDatesCanBeParsedFromString() {

        LocalDate startDate = LocalDate.of(2015, MAY, 19);
        LocalDate endDate = LocalDate.of(2015, DECEMBER, 21);

        FilterPeriod period = new FilterPeriod("19.05.2015", "21.12.2015");

        Assert.assertNotNull("Start date should not be null", period.getStartDate());
        Assert.assertEquals("Wrong start date", startDate, period.getStartDate());

        Assert.assertNotNull("End date should not be null", period.getEndDate());
        Assert.assertEquals("Wrong end date", endDate, period.getEndDate());
    }

    @Test
    void ensureDefaultDatesForEmptyStrings() {

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
    void ensureThrowsIfTryingToParseDatesWithUnsupportedFormat() {

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
