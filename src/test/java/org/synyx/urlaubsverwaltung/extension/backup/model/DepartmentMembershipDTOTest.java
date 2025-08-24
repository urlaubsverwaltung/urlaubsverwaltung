package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.department.DepartmentMembershipEntity;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.department.DepartmentMembershipKind.DEPARTMENT_HEAD;

class DepartmentMembershipDTOTest {

    @Test
    void happyPathDTOToEntity() {

        final Instant validFrom = Instant.now();
        final Instant validTo = Instant.now();

        final Person owner = new Person();
        owner.setId(42L);

        final DepartmentMembershipDTO dto = new DepartmentMembershipDTO("username", 1L, DEPARTMENT_HEAD, validFrom, validTo);
        final DepartmentMembershipEntity entity = dto.toEntity(owner);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getPersonId()).isEqualTo(42L);
        assertThat(entity.getDepartmentId()).isEqualTo(1L);
        assertThat(entity.getMembershipKind()).isEqualTo(DEPARTMENT_HEAD);
        assertThat(entity.getValidFrom()).isSameAs(validFrom);
        assertThat(entity.getValidTo()).isSameAs(validTo);
    }
}
