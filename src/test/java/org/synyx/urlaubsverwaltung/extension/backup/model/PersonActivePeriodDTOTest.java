package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonActivePeriodEntity;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PersonActivePeriodDTOTest {

    @Test
    void happyPathDTOToEntity() {

        final Instant validFrom = Instant.now();
        final Instant validTo = Instant.now();

        final Person owner = new Person();
        owner.setId(42L);

        final PersonActivePeriodDTO dto = new PersonActivePeriodDTO("username", validFrom, validTo);
        final PersonActivePeriodEntity entity = dto.toEntity(owner);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getPersonId()).isEqualTo(42L);
        assertThat(entity.getValidFrom()).isSameAs(validFrom);
        assertThat(entity.getValidTo()).isSameAs(validTo);
    }
}
