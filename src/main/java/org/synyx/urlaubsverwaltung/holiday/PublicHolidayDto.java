package org.synyx.urlaubsverwaltung.holiday;

import de.jollyday.Holiday;

import java.math.BigDecimal;
import java.util.Locale;

class PublicHolidayDto {

    private String date;
    private String description;
    private BigDecimal dayLength;
    private String absencePeriodName;

    PublicHolidayDto(Holiday holiday, BigDecimal dayLength, String absencePeriodName) {
        this.date = holiday.getDate().toString();
        this.description = holiday.getDescription(Locale.GERMAN);
        this.dayLength = dayLength;
        this.absencePeriodName = absencePeriodName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getDayLength() {
        return dayLength;
    }

    public void setDayLength(BigDecimal dayLength) {
        this.dayLength = dayLength;
    }

    public String getAbsencePeriodName() {
        return absencePeriodName;
    }
}
