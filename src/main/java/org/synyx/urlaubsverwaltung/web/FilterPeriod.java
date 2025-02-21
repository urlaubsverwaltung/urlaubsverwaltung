package org.synyx.urlaubsverwaltung.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Represents a period of time to filter requests by.
 */
public record FilterPeriod(
    LocalDate startDate,
    LocalDate endDate
) {
    public String getStartDateIsoValue() {
        return Optional.ofNullable(startDate)
            .map(date -> date.format(DateTimeFormatter.ISO_DATE))
            .orElse("");
    }

    public String getEndDateIsoValue() {
        return Optional.ofNullable(endDate)
            .map(date -> date.format(DateTimeFormatter.ISO_DATE))
            .orElse("");
    }
}
