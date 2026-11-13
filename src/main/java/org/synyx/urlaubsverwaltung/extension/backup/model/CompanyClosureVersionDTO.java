package org.synyx.urlaubsverwaltung.extension.backup.model;

import jakarta.annotation.Nullable;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureVersionEntity;

import java.time.LocalDate;

/**
 * @param dayLength the closed portion of the day
 */
public record CompanyClosureVersionDTO(DayLengthDTO dayLength, @Nullable LocalDate validFrom) {

    public CompanyClosureVersionEntity toEntity() {
        final CompanyClosureVersionEntity entity = new CompanyClosureVersionEntity();
        entity.setDayLength(dayLength().toDayLength());
        entity.setValidFrom(validFrom());
        return entity;
    }
}
