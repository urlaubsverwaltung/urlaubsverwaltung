package org.synyx.urlaubsverwaltung.companyclosure;

import jakarta.annotation.Nullable;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.LocalDate;

/**
 * @param dayLength closed portion of the day, {@link DayLength#ZERO} if there is no closure
 * @param validFrom first date this version applies to, {@code null} for all years before the next version
 */
public record CompanyClosureVersion(DayLength dayLength, @Nullable LocalDate validFrom) {
}
