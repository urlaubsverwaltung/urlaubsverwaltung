package org.synyx.urlaubsverwaltung.extension.backup.model;

import jakarta.annotation.Nullable;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureVersionEntity;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Instant;
import java.time.LocalDate;

/**
 * @param dayLength                the closed portion of the day
 * @param externalIdOfLastModifier the external id of the person who changed the version last
 */
public record CompanyClosureVersionDTO(DayLengthDTO dayLength, @Nullable LocalDate validFrom, @Nullable Instant lastModifiedAt,
                                       @Nullable String externalIdOfLastModifier) {

    public CompanyClosureVersionEntity toEntity(@Nullable Person lastModifier) {
        final CompanyClosureVersionEntity entity = new CompanyClosureVersionEntity();
        entity.setDayLength(dayLength().toDayLength());
        entity.setValidFrom(validFrom());
        entity.setLastModifiedAt(lastModifiedAt());
        entity.setLastModifiedBy(lastModifier);
        return entity;
    }
}
