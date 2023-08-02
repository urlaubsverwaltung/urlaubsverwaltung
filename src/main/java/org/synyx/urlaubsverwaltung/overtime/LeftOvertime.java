package org.synyx.urlaubsverwaltung.overtime;

import java.time.Duration;
import java.util.Map;

/**
 * Provides information about the left overtime for a year and a given date range.
 * Should be used in combination with a {@link Map} to keep relation to a {@link org.synyx.urlaubsverwaltung.person.Person} for example.
 */
public class LeftOvertime {

    private final Duration leftOvertimeOverall;
    private final Duration leftOvertimeDateRange;

    public LeftOvertime(Duration leftOvertimeOverall, Duration leftOvertimeDateRange) {
        this.leftOvertimeOverall = leftOvertimeOverall;
        this.leftOvertimeDateRange = leftOvertimeDateRange;
    }

    public Duration getLeftOvertimeOverall() {
        return leftOvertimeOverall;
    }

    public Duration getLeftOvertimeDateRange() {
        return leftOvertimeDateRange;
    }

    /**
     * @return an empty {@link LeftOvertime} to describe "no overtime information"
     */
    public static LeftOvertime identity() {
        return new LeftOvertime(Duration.ZERO, Duration.ZERO);
    }
}
