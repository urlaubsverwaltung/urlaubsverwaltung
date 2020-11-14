package org.synyx.urlaubsverwaltung.period;

import org.springframework.util.Assert;

import java.time.DayOfWeek;

/**
 * Represents a day of week.
 */
public enum WeekDay {

    MONDAY(DayOfWeek.MONDAY.getValue()),
    TUESDAY(DayOfWeek.TUESDAY.getValue()),
    WEDNESDAY(DayOfWeek.WEDNESDAY.getValue()),
    THURSDAY(DayOfWeek.THURSDAY.getValue()),
    FRIDAY(DayOfWeek.FRIDAY.getValue()),
    SATURDAY(DayOfWeek.SATURDAY.getValue()),
    SUNDAY(DayOfWeek.SUNDAY.getValue());

    private static final int MIN_WEEK_DAY = 1;
    private static final int MAX_WEEK_DAY = 7;

    private final Integer dayOfWeek;

    WeekDay(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Get the week day for the given day of week representation.
     *
     * @param dayOfWeek to detect the week day for, use {@link java.time.DayOfWeek} by preference.
     * @return the matching week day, never {@code null}
     */
    public static WeekDay getByDayOfWeek(int dayOfWeek) {

        Assert.isTrue(dayOfWeek >= MIN_WEEK_DAY && dayOfWeek <= MAX_WEEK_DAY, "Day of week must be between 1 and 7");

        for (WeekDay weekDay : WeekDay.values()) {
            if (dayOfWeek == weekDay.getDayOfWeek()) {
                return weekDay;
            }
        }

        throw new IllegalStateException("There is no week day for: " + dayOfWeek);
    }
}
