package org.synyx.urlaubsverwaltung.person.web;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Collection;

import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto.OFFICE;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto.USER;

/**
 * This class validate if permissions and notifications of a {@link PersonPermissionsDto} is filled correctly.
 */
@Component
class PersonPermissionsDtoValidator implements Validator {

    private static final String ERROR_PERMISSIONS_MANDATORY = "person.form.permissions.error.mandatory";
    private static final String ERROR_PERMISSIONS_MANDATORY_OFFICE = "person.form.permissions.error.mandatory.office";
    private static final String ERROR_PERMISSIONS_INACTIVE_ROLE = "person.form.permissions.error.inactive";
    private static final String ERROR_PERMISSIONS_USER_ROLE = "person.form.permissions.error.user";

    private static final String ATTRIBUTE_PERMISSIONS = "permissions";

    private final PersonService personService;

    PersonPermissionsDtoValidator(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return PersonPermissionsDto.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

        final PersonPermissionsDto personPermissionsDto = (PersonPermissionsDto) target;
        validateAtLeastOnePersonWithOffice(personPermissionsDto, errors);
        validatePermissions(personPermissionsDto, errors);
    }

    void validateAtLeastOnePersonWithOffice(PersonPermissionsDto personPermissionsDto, Errors errors) {
        if (!personPermissionsDto.getPermissions().contains(OFFICE) &&
            personService.numberOfPersonsWithOfficeRoleExcludingPerson(personPermissionsDto.getId()) == 0) {
            errors.rejectValue(ATTRIBUTE_PERMISSIONS, ERROR_PERMISSIONS_MANDATORY_OFFICE);
        }
    }

    void validatePermissions(PersonPermissionsDto personPermissionsDto, Errors errors) {

        final Collection<PersonPermissionsRoleDto> roles = personPermissionsDto.getPermissions();

        if (roles == null || roles.isEmpty()) {
            errors.rejectValue(ATTRIBUTE_PERMISSIONS, ERROR_PERMISSIONS_MANDATORY);
            return;
        }

        // if role inactive set, then only this role may be selected
        if (roles.contains(INACTIVE) && roles.size() != 1) {
            errors.rejectValue(ATTRIBUTE_PERMISSIONS, ERROR_PERMISSIONS_INACTIVE_ROLE);
            return;
        }

        // user role must always be selected for active user
        if (!roles.contains(INACTIVE) && !roles.contains(USER)) {
            errors.rejectValue(ATTRIBUTE_PERMISSIONS, ERROR_PERMISSIONS_USER_ROLE);
        }
    }
}
