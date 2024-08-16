package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SickNoteExtendPreviewDto(LocalDate startDate, LocalDate endDate, BigDecimal workingDays) {
}
