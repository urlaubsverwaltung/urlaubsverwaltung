package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsMapper.mapPermissionsDtoToRole;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsMapper.mapRoleToPermissionsDto;

class PersonPermissionsMapperTest {

    @Test
    void ensurePermissionsMappingToRolesAreCorrect() {
        final List<PersonPermissionsRoleDto> permissions = Arrays.asList(PersonPermissionsRoleDto.values());
        final List<Role> roles = mapPermissionsDtoToRole(permissions);
        assertThat(roles).containsAll(Arrays.asList(Role.values()));
    }

    @Test
    void ensureRolesMappingToPermissionsAreCorrect() {
        final List<Role> roles = Arrays.asList(Role.values());
        final List<PersonPermissionsRoleDto> permissions = mapRoleToPermissionsDto(roles);
        assertThat(permissions).containsAll(Arrays.asList(PersonPermissionsRoleDto.values()));
    }
}
