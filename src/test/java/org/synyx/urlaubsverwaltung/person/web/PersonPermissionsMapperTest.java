package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsMapper.mapPermissionsDtoToRole;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsMapper.mapRoleToPermissionsDto;

class PersonPermissionsMapperTest {

    static Stream<Arguments> personPermissionMapping() {
        return Stream.of(
            arguments(PersonPermissionsRoleDto.USER, List.of(Role.USER)),
            arguments(PersonPermissionsRoleDto.DEPARTMENT_HEAD, List.of(Role.DEPARTMENT_HEAD)),
            arguments(PersonPermissionsRoleDto.SECOND_STAGE_AUTHORITY, List.of(Role.SECOND_STAGE_AUTHORITY)),
            arguments(PersonPermissionsRoleDto.BOSS, List.of(Role.BOSS)),
            arguments(PersonPermissionsRoleDto.OFFICE, List.of(Role.OFFICE)),
            arguments(PersonPermissionsRoleDto.INACTIVE, List.of(Role.INACTIVE)),
            arguments(PersonPermissionsRoleDto.SICK_NOTE_VIEW_ADD_EDIT, List.of(
                Role.SICK_NOTE_VIEW,
                Role.SICK_NOTE_ADD,
                Role.SICK_NOTE_EDIT,
                Role.SICK_NOTE_CANCEL,
                Role.SICK_NOTE_COMMENT
            )),
            arguments(PersonPermissionsRoleDto.APPLICATION_ADD_CANCEL, List.of(
                Role.APPLICATION_ADD,
                Role.APPLICATION_CANCEL,
                Role.APPLICATION_CANCELLATION_REQUESTED
            ))
        );
    }

    @ParameterizedTest
    @MethodSource("personPermissionMapping")
    void ensurePermissionToRoleMapping(PersonPermissionsRoleDto personPermission, List<Role> expectedRoles) {
        final List<Role> roles = mapPermissionsDtoToRole(List.of(personPermission));
        assertThat(roles).containsAll(expectedRoles);
    }

    @Test
    void ensurePermissionsMappingToRolesContainsAllRolesInSortedOrder() {
        final List<PersonPermissionsRoleDto> permissions = Arrays.asList(PersonPermissionsRoleDto.values());
        final List<Role> roles = mapPermissionsDtoToRole(permissions);
        final List<Role> defaultRoles = Arrays.stream(Role.values()).filter(role -> !role.equals(Role.PERSON_ADD)).toList();
        assertThat(roles).containsExactlyElementsOf(defaultRoles);
    }

    static Stream<Arguments> roleMapping() {
        return Stream.of(
            arguments(Role.USER, List.of(PersonPermissionsRoleDto.USER)),
            arguments(Role.DEPARTMENT_HEAD, List.of(PersonPermissionsRoleDto.DEPARTMENT_HEAD)),
            arguments(Role.SECOND_STAGE_AUTHORITY, List.of(PersonPermissionsRoleDto.SECOND_STAGE_AUTHORITY)),
            arguments(Role.BOSS, List.of(PersonPermissionsRoleDto.BOSS)),
            arguments(Role.OFFICE, List.of(PersonPermissionsRoleDto.OFFICE)),
            arguments(Role.INACTIVE, List.of(PersonPermissionsRoleDto.INACTIVE)),
            arguments(Role.SICK_NOTE_VIEW, List.of(PersonPermissionsRoleDto.SICK_NOTE_VIEW_ADD_EDIT)),
            arguments(Role.SICK_NOTE_ADD, List.of(PersonPermissionsRoleDto.SICK_NOTE_VIEW_ADD_EDIT)),
            arguments(Role.SICK_NOTE_EDIT, List.of(PersonPermissionsRoleDto.SICK_NOTE_VIEW_ADD_EDIT)),
            arguments(Role.SICK_NOTE_CANCEL, List.of(PersonPermissionsRoleDto.SICK_NOTE_VIEW_ADD_EDIT)),
            arguments(Role.SICK_NOTE_COMMENT, List.of(PersonPermissionsRoleDto.SICK_NOTE_VIEW_ADD_EDIT)),
            arguments(Role.APPLICATION_ADD, List.of(PersonPermissionsRoleDto.APPLICATION_ADD_CANCEL)),
            arguments(Role.APPLICATION_CANCEL, List.of(PersonPermissionsRoleDto.APPLICATION_ADD_CANCEL)),
            arguments(Role.APPLICATION_CANCELLATION_REQUESTED, List.of(PersonPermissionsRoleDto.APPLICATION_ADD_CANCEL))
        );
    }

    @ParameterizedTest
    @MethodSource("roleMapping")
    void ensureRoleToPermissionMapping(Role role, List<PersonPermissionsRoleDto> expectedPermissions) {
        final List<PersonPermissionsRoleDto> roles = mapRoleToPermissionsDto(List.of(role));
        assertThat(roles).containsAll(expectedPermissions);
    }

    @Test
    void ensureRolesMappingToPermissionsContainsAllPermissions() {
        final List<Role> roles = Arrays.asList(Role.values());
        final List<PersonPermissionsRoleDto> permissions = mapRoleToPermissionsDto(roles);
        assertThat(permissions).containsAll(Arrays.asList(PersonPermissionsRoleDto.values()));
    }
}
