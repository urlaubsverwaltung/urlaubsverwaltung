package org.synyx.urlaubsverwaltung.publicholiday;

import de.jollyday.Holiday;

import java.math.BigDecimal;
import java.util.Locale;

final class PublicHolidayDto {

    private final String date;
    private final String description;
    private final BigDecimal dayLength;
    private final String absencePeriodName;

    PublicHolidayDto(Holiday holiday, BigDecimal dayLength, String absencePeriodName) {
        this.date = holiday.getDate().toString();
        this.description = holiday.getDescription(Locale.GERMAN);
        this.dayLength = dayLength;
        this.absencePeriodName = absencePeriodName;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getDayLength() {
        return dayLength;
    }

    public String getAbsencePeriodName() {
        return absencePeriodName;
    }
}
