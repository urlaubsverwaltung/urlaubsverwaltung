package org.synyx.urlaubsverwaltung.person.web;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class PersonPermissionsMapper {

    private PersonPermissionsMapper() {
    }

    static PersonPermissionsDto mapToPersonPermissionsDto(Person person) {
        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setId(person.getId());
        personPermissionsDto.setNiceName(person.getNiceName());
        personPermissionsDto.setInitials(person.getInitials());
        personPermissionsDto.setGravatarURL(person.getGravatarURL());
        personPermissionsDto.setEmail(person.getEmail());
        personPermissionsDto.setIsInactive(person.isInactive());
        personPermissionsDto.setPermissions(mapRoleToPermissionsDto(List.copyOf(person.getPermissions())));
        return personPermissionsDto;
    }

    static Person merge(Person person, PersonPermissionsDto personPermissionsDto) {
        person.setPermissions(mapPermissionsDtoToRole(personPermissionsDto.getPermissions()));
        return person;
    }

    static List<Role> mapPermissionsDtoToRole(List<PersonPermissionsRoleDto> permissionsRoleDto) {
        final List<Role> mappedToRoles = new ArrayList<>();
        permissionsRoleDto.forEach(roleDto -> {
            switch (roleDto) {
                case USER -> mappedToRoles.add(Role.USER);
                case DEPARTMENT_HEAD -> mappedToRoles.add(Role.DEPARTMENT_HEAD);
                case SECOND_STAGE_AUTHORITY -> mappedToRoles.add(Role.SECOND_STAGE_AUTHORITY);
                case BOSS -> mappedToRoles.add(Role.BOSS);
                case OFFICE -> mappedToRoles.add(Role.OFFICE);
                case INACTIVE -> mappedToRoles.add(Role.INACTIVE);
                case SICK_NOTE_VIEW_ADD_EDIT -> {
                    mappedToRoles.add(Role.SICK_NOTE_VIEW);
                    mappedToRoles.add(Role.SICK_NOTE_ADD);
                    mappedToRoles.add(Role.SICK_NOTE_EDIT);
                    mappedToRoles.add(Role.SICK_NOTE_CANCEL);
                    mappedToRoles.add(Role.SICK_NOTE_COMMENT);
                }
                case APPLICATION_ADD_CANCEL -> {
                    mappedToRoles.add(Role.APPLICATION_ADD);
                    mappedToRoles.add(Role.APPLICATION_CANCEL);
                    mappedToRoles.add(Role.APPLICATION_CANCELLATION_REQUESTED);
                }
            }
        });

        return mappedToRoles;
    }

    static List<PersonPermissionsRoleDto> mapRoleToPermissionsDto(List<Role> roles) {
        final Set<PersonPermissionsRoleDto> mappedToRolesDto = new HashSet<>();
        roles.forEach(role -> {
            switch (role) {
                case USER -> mappedToRolesDto.add(PersonPermissionsRoleDto.USER);
                case DEPARTMENT_HEAD -> mappedToRolesDto.add(PersonPermissionsRoleDto.DEPARTMENT_HEAD);
                case SECOND_STAGE_AUTHORITY -> mappedToRolesDto.add(PersonPermissionsRoleDto.SECOND_STAGE_AUTHORITY);
                case BOSS -> mappedToRolesDto.add(PersonPermissionsRoleDto.BOSS);
                case OFFICE -> mappedToRolesDto.add(PersonPermissionsRoleDto.OFFICE);
                case INACTIVE -> mappedToRolesDto.add(PersonPermissionsRoleDto.INACTIVE);
                case SICK_NOTE_VIEW, SICK_NOTE_ADD, SICK_NOTE_EDIT, SICK_NOTE_CANCEL, SICK_NOTE_COMMENT ->
                    mappedToRolesDto.add(PersonPermissionsRoleDto.SICK_NOTE_VIEW_ADD_EDIT);
                case APPLICATION_ADD, APPLICATION_CANCEL, APPLICATION_CANCELLATION_REQUESTED ->
                    mappedToRolesDto.add(PersonPermissionsRoleDto.APPLICATION_ADD_CANCEL);
            }
        });

        return new ArrayList<>(mappedToRolesDto).stream().sorted().toList();
    }
}
