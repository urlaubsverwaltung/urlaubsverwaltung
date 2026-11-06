package org.synyx.urlaubsverwaltung.extension.backup.model;

import jakarta.annotation.Nullable;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntity;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Instant;
import java.util.List;

/**
 * @param externalIdOfLastModifier the external id of the person who changed the closure last
 */
public record CompanyClosureDTO(int month, int dayOfMonth, String messageKey, String name, @Nullable Instant lastModifiedAt,
                                @Nullable String externalIdOfLastModifier, List<CompanyClosureVersionDTO> versions) {

    public CompanyClosureEntity toEntity(@Nullable Person lastModifier) {
        final CompanyClosureEntity entity = new CompanyClosureEntity();
        entity.setMonth(month());
        entity.setDayOfMonth(dayOfMonth());
        entity.setMessageKey(messageKey());
        entity.setName(name());
        entity.setLastModifiedAt(lastModifiedAt());
        entity.setLastModifiedBy(lastModifier);
        return entity;
    }
}
