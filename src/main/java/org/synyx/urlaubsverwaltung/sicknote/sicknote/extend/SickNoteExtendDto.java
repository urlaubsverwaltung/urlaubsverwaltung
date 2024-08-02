package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import java.math.BigDecimal;
import java.time.LocalDate;

record SickNoteExtendDto(Long id, LocalDate startDate, LocalDate endDate, BigDecimal workingDays, boolean isAub) {
}
