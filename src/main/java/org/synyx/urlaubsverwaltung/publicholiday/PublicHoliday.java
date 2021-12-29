package org.synyx.urlaubsverwaltung.publicholiday;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;

/**
 * Describes a public holiday with the UV specific setting if it is a half public holiday or not.
 * (e.g. christmas eve can be configured to be a NOON public holiday for instance.)
 */
public final class PublicHoliday {

    private final LocalDate date;
    private final DayLength dayLength;
    private final String description;

    public PublicHoliday(LocalDate date, DayLength dayLength) {
        this(date, dayLength, null);
    }

    PublicHoliday(LocalDate date, DayLength dayLength, String description) {
        this.date = date;
        this.dayLength = dayLength;
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMorning() {
        return dayLength.equals(MORNING);
    }

    public boolean isNoon() {
        return dayLength.equals(NOON);
    }

    public boolean isFull() {
        return dayLength.equals(FULL);
    }

    public BigDecimal getWorkingDuration() {
        return dayLength.getInverse().getDuration();
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
