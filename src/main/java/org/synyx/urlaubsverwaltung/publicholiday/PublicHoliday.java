package org.synyx.urlaubsverwaltung.publicholiday;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;

/**
 * Describes a public holiday. Public holidays are always full days, closed half or full days like christmas eve
 * and new year's eve are company closures.
 */
public record PublicHoliday(LocalDate date, DayLength dayLength, String description) {

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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PublicHoliday that = (PublicHoliday) o;
        return Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}
