package org.synyx.urlaubsverwaltung.person.web;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

final class PersonPermissionsMapper {

    private PersonPermissionsMapper() {
    }

    static PersonPermissionsDto mapToPersonPermissionsDto(Person person) {
        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setId(person.getId());
        personPermissionsDto.setNiceName(person.getNiceName());
        personPermissionsDto.setGravatarURL(person.getGravatarURL());
        personPermissionsDto.setEmail(person.getEmail());
        personPermissionsDto.setPermissions(mapRoleToPermissionsDto(List.copyOf(person.getPermissions())));
        personPermissionsDto.setNotifications(List.copyOf(person.getNotifications()));
        return personPermissionsDto;
    }

    static Person merge(Person person, PersonPermissionsDto personPermissionsDto) {
        person.setPermissions(mapPermissionsDtoToRole(personPermissionsDto.getPermissions()));
        person.setNotifications(personPermissionsDto.getNotifications());
        return person;
    }

    private static List<Role> mapPermissionsDtoToRole(List<PersonPermissionsRoleDto> permissionsRoleDto) {
        final List<Role> mappedToRoles = new ArrayList<>();
        permissionsRoleDto.forEach(roleDto -> {
            switch (roleDto) {
                case USER:
                    mappedToRoles.add(Role.USER);
                    break;
                case DEPARTMENT_HEAD:
                    mappedToRoles.add(Role.DEPARTMENT_HEAD);
                    break;
                case SECOND_STAGE_AUTHORITY:
                    mappedToRoles.add(Role.SECOND_STAGE_AUTHORITY);
                    break;
                case BOSS:
                    mappedToRoles.add(Role.BOSS);
                    break;
                case OFFICE:
                    mappedToRoles.add(Role.OFFICE);
                    break;
                case ADMIN:
                    mappedToRoles.add(Role.ADMIN);
                    break;
                case INACTIVE:
                    mappedToRoles.add(Role.INACTIVE);
                    break;
                case SICK_NOTE_VIEW_ADD_EDIT:
                    mappedToRoles.add(Role.SICK_NOTE_VIEW);
                    mappedToRoles.add(Role.SICK_NOTE_ADD);
                    mappedToRoles.add(Role.SICK_NOTE_EDIT);
                    break;
                default:
                    break;
            }
        });

        return mappedToRoles;
    }

    static List<PersonPermissionsRoleDto> mapRoleToPermissionsDto(List<Role> roles) {
        final List<PersonPermissionsRoleDto> mappedToRolesDto = new ArrayList<>();
        roles.forEach(role -> {
            switch (role) {
                case USER:
                    mappedToRolesDto.add(PersonPermissionsRoleDto.USER);
                    break;
                case DEPARTMENT_HEAD:
                    mappedToRolesDto.add(PersonPermissionsRoleDto.DEPARTMENT_HEAD);
                    break;
                case SECOND_STAGE_AUTHORITY:
                    mappedToRolesDto.add(PersonPermissionsRoleDto.SECOND_STAGE_AUTHORITY);
                    break;
                case BOSS:
                    mappedToRolesDto.add(PersonPermissionsRoleDto.BOSS);
                    break;
                case OFFICE:
                    mappedToRolesDto.add(PersonPermissionsRoleDto.OFFICE);
                    break;
                case ADMIN:
                    mappedToRolesDto.add(PersonPermissionsRoleDto.ADMIN);
                    break;
                case INACTIVE:
                    mappedToRolesDto.add(PersonPermissionsRoleDto.INACTIVE);
                    break;
                case SICK_NOTE_VIEW:
                case SICK_NOTE_ADD:
                case SICK_NOTE_EDIT:
                    mappedToRolesDto.add(PersonPermissionsRoleDto.SICK_NOTE_VIEW_ADD_EDIT);
                    break;
                default:
                    break;
            }
        });

        return mappedToRolesDto.stream()
            .distinct()
            .collect(toList());
    }
}
