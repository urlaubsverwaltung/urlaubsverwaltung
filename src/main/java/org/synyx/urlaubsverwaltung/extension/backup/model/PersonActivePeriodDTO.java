package org.synyx.urlaubsverwaltung.extension.backup.model;

import jakarta.annotation.Nullable;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonActivePeriodEntity;

import java.time.Instant;

public record PersonActivePeriodDTO(
    String personExternalId,
    Instant validFrom,
    @Nullable Instant validTo
) {

    public PersonActivePeriodEntity toEntity(Person owner) {
        final PersonActivePeriodEntity entity = new PersonActivePeriodEntity();
        entity.setPersonId(owner.getId());
        entity.setValidFrom(this.validFrom());
        entity.setValidTo(this.validTo());
        return entity;
    }
}
