package org.synyx.urlaubsverwaltung.publicholiday;

import de.jollyday.Holiday;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.util.Objects;

/**
 * Describes a public holiday with the UV specific setting if it is a half public holiday or not.
 * (e.g. christmas eve can be configured to be a NOON public holiday for instance.)
 */
public final class PublicHoliday {

    private final Holiday holiday;
    private final DayLength dayLength;

    public PublicHoliday(Holiday holiday, DayLength dayLength) {
        this.holiday = holiday;
        this.dayLength = dayLength;
    }

    public Holiday getHoliday() {
        return holiday;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public boolean isMorning() {
        return dayLength.equals(DayLength.MORNING);
    }

    public boolean isNoon() {
        return dayLength.equals(DayLength.NOON);
    }

    public boolean isFull() {
        return dayLength.equals(DayLength.FULL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicHoliday that = (PublicHoliday) o;
        return Objects.equals(holiday, that.holiday);
    }

    @Override
    public int hashCode() {
        return Objects.hash(holiday);
    }
}
