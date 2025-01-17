package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.person.Role;

import static org.assertj.core.api.Assertions.assertThat;

class RoleDTOTest {

    @ParameterizedTest
    @EnumSource(Role.class)
    void happyPathRoleToDTO(Role role) {
        RoleDTO roleDTO = RoleDTO.valueOf(role.name());
        assertThat(roleDTO).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(RoleDTO.class)
    void happyPathDTOToRole(RoleDTO roleDTO) {
        Role role = roleDTO.toRole();
        assertThat(role).isNotNull();
    }
}
