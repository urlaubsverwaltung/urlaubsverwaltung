package org.synyx.urlaubsverwaltung.overtime;

import java.time.Duration;
import java.util.Map;

/**
 * Provides information about the left overtime for a year and a given date range.
 *
 * Should be used in combination with a {@link Map} to keep relation to a {@link org.synyx.urlaubsverwaltung.person.Person} for example.
 */
public class LeftOvertime {

    private final LeftOvertimeOverall leftOvertimeOverall;
    private final LeftOvertimeDateRange leftOvertimeDateRange;

    public LeftOvertime(LeftOvertimeOverall leftOvertimeOverall, LeftOvertimeDateRange leftOvertimeDateRange) {
        this.leftOvertimeOverall = leftOvertimeOverall;
        this.leftOvertimeDateRange = leftOvertimeDateRange;
    }

    public LeftOvertimeOverall getLeftOvertimeOverall() {
        return leftOvertimeOverall;
    }

    public LeftOvertimeDateRange getLeftOvertimeDateRange() {
        return leftOvertimeDateRange;
    }

    /**
     *
     * @return an empty {@link LeftOvertime} to describe "no overtime information"
     */
    public static LeftOvertime identity() {
        final LeftOvertimeOverall overall = new LeftOvertimeOverall(Duration.ZERO);
        // TODO is dateRange null okay??
        final LeftOvertimeDateRange dateRange = new LeftOvertimeDateRange(null, Duration.ZERO);
        return new LeftOvertime(overall, dateRange);
    }
}
