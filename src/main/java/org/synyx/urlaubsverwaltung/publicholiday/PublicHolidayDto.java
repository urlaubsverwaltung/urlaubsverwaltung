package org.synyx.urlaubsverwaltung.publicholiday;

import java.math.BigDecimal;

public final class PublicHolidayDto {

    private final String date;
    private final String description;
    private final BigDecimal dayLength;
    private final String absencePeriodName;

    PublicHolidayDto(PublicHoliday publicHoliday, BigDecimal dayLength, String absencePeriodName) {
        this.date = publicHoliday.date().toString();
        this.description = publicHoliday.description();
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
