package org.synyx.urlaubsverwaltung.person.web;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Collection;

import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_DEPARTMENTS;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * This class validate if permissions and notifications of a {@link PersonPermissionsDto} is filled correctly.
 */
@Component
class PersonPermissionsDtoValidator implements Validator {

    private static final String ERROR_PERMISSIONS_MANDATORY = "person.form.permissions.error.mandatory";
    private static final String ERROR_PERMISSIONS_INACTIVE_ROLE = "person.form.permissions.error.inactive";
    private static final String ERROR_PERMISSIONS_USER_ROLE = "person.form.permissions.error.user";
    private static final String ERROR_PERMISSIONS_COMBINATION_DEPARTMENT_HEAD_WITH_BOSS = "person.form.permissions.error.combination.departmentHead";
    private static final String ERROR_PERMISSIONS_COMBINATION_SECOND_STAGE_WITH_BOSS = "person.form.permissions.error.combination.secondStage";
    private static final String ERROR_NOTIFICATIONS_COMBINATION = "person.form.notifications.error.combination";

    private static final String ATTRIBUTE_PERMISSIONS = "permissions";

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return PersonPermissionsDto.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

        final PersonPermissionsDto personPermissionsDto = (PersonPermissionsDto) target;
        validatePermissions(personPermissionsDto, errors);
        validateNotifications(personPermissionsDto, errors);
    }

    void validatePermissions(PersonPermissionsDto personPermissionsDto, Errors errors) {

        final Collection<Role> roles = personPermissionsDto.getPermissions();

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
            return;
        }

        validateCombinationOfRoles(roles, errors);
    }

    private void validateCombinationOfRoles(Collection<Role> roles, Errors errors) {

        if (roles.contains(DEPARTMENT_HEAD) && roles.contains(BOSS)) {
            errors.rejectValue(ATTRIBUTE_PERMISSIONS, ERROR_PERMISSIONS_COMBINATION_DEPARTMENT_HEAD_WITH_BOSS);
        }

        if (roles.contains(SECOND_STAGE_AUTHORITY) && roles.contains(BOSS)) {
            errors.rejectValue(ATTRIBUTE_PERMISSIONS, ERROR_PERMISSIONS_COMBINATION_SECOND_STAGE_WITH_BOSS);
        }
    }

    void validateNotifications(PersonPermissionsDto personPermissionsDto, Errors errors) {

        final Collection<Role> roles = personPermissionsDto.getPermissions();
        final Collection<MailNotification> notifications = personPermissionsDto.getNotifications();

        if (roles != null) {
            validateCombinationOfNotificationAndRole(roles, notifications, DEPARTMENT_HEAD, NOTIFICATION_DEPARTMENT_HEAD, errors);
            validateCombinationOfNotificationAndRole(roles, notifications, SECOND_STAGE_AUTHORITY, NOTIFICATION_SECOND_STAGE_AUTHORITY, errors);
            validateCombinationOfNotificationAndRole(roles, notifications, BOSS, NOTIFICATION_BOSS_ALL, errors);
            validateCombinationOfNotificationAndRole(roles, notifications, BOSS, NOTIFICATION_BOSS_DEPARTMENTS, errors);
            validateCombinationOfNotificationAndRole(roles, notifications, OFFICE, NOTIFICATION_OFFICE, errors);
        }
    }

    private void validateCombinationOfNotificationAndRole(Collection<Role> roles, Collection<MailNotification> notifications, Role role, MailNotification notification, Errors errors) {
        if (notifications.contains(notification) && !roles.contains(role)) {
            errors.rejectValue("notifications", ERROR_NOTIFICATIONS_COMBINATION);
        }
    }
}
