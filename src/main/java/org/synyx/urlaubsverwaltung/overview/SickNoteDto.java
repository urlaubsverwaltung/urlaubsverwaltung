package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import java.math.BigDecimal;
import java.time.LocalDate;

record SickNoteDto(
    Long id,
    LocalDate startDate,
    LocalDate endDate,
    DayLength dayLength,
    boolean isAubPresent,
    BigDecimal workDays,
    BigDecimal workDaysWithAub,
    SickNoteStatus status,
    SickNoteType sickNoteType,
    boolean allowedToEdit,
    boolean allowedToCancel
) {
}

