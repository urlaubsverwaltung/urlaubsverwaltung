package org.synyx.urlaubsverwaltung.publicholiday;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Describes a public holiday with the UV specific setting if it is a half public holiday or not.
 * (e.g. christmas eve can be configured to be a NOON public holiday for instance.)
 */
public final class PublicHoliday {

    private final LocalDate date;
    private final DayLength dayLength;

    public PublicHoliday(LocalDate date, DayLength dayLength) {
        this.date = date;
        this.dayLength = dayLength;
    }

    public LocalDate getDate() {
        return date;
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
        return Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}
