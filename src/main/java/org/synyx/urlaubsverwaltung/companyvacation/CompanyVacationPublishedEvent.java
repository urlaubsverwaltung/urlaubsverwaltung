package org.synyx.urlaubsverwaltung.companyvacation;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CompanyVacationPublishedEvent(String sourceId, UUID id, Instant createdAt, DayLength dayLength, LocalDate startDate, LocalDate endDate) {
}
