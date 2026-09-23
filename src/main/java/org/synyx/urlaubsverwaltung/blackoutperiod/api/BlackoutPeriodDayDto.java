package org.synyx.urlaubsverwaltung.blackoutperiod.api;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.synyx.urlaubsverwaltung.api.RestApiDateFormat.DATE_PATTERN;

/**
 * A single day that is blocked by a blackout period, for calendar rendering purposes.
 */
public class BlackoutPeriodDayDto {

    private final String date;
    private final String title;
    private final String description;

    BlackoutPeriodDayDto(LocalDate date, String title, String description) {
        this.date = date.format(ofPattern(DATE_PATTERN));
        this.title = title;
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
