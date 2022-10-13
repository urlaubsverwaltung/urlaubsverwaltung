package org.synyx.urlaubsverwaltung.person.web;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Collection;

import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_DEPARTMENTS;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.MailNotification.OVERTIME_NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

/**
 * This class validate if the notifications of a {@link PersonNotificationsDto} is filled correctly.
 */
@Component
class PersonNotificationsDtoValidator implements Validator {

    private static final String ERROR_NOTIFICATIONS_COMBINATION = "person.form.notifications.error.combination";

    private final PersonService personService;

    PersonNotificationsDtoValidator(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return PersonNotificationsDto.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        final PersonNotificationsDto personNotificationsDto = (PersonNotificationsDto) target;
        validateNotifications(personNotificationsDto, errors);
    }

    void validateNotifications(PersonNotificationsDto personNotificationsDto, Errors errors) {

        final Collection<Role> roles = personService.getSignedInUser().getPermissions();
        final Collection<MailNotification> notifications = personNotificationsDto.getNotifications();

        if (roles != null) {
            validateCombinationOfNotificationAndRole(roles, notifications, DEPARTMENT_HEAD, NOTIFICATION_DEPARTMENT_HEAD, errors);
            validateCombinationOfNotificationAndRole(roles, notifications, SECOND_STAGE_AUTHORITY, NOTIFICATION_SECOND_STAGE_AUTHORITY, errors);
            validateCombinationOfNotificationAndRole(roles, notifications, BOSS, NOTIFICATION_BOSS_ALL, errors);
            validateCombinationOfNotificationAndRole(roles, notifications, BOSS, NOTIFICATION_BOSS_DEPARTMENTS, errors);
            validateCombinationOfNotificationAndRole(roles, notifications, OFFICE, NOTIFICATION_OFFICE, errors);
            validateCombinationOfNotificationAndRole(roles, notifications, OFFICE, OVERTIME_NOTIFICATION_OFFICE, errors);
        }
    }

    private void validateCombinationOfNotificationAndRole(Collection<Role> roles, Collection<MailNotification> notifications, Role role, MailNotification notification, Errors errors) {
        if (notifications.contains(notification) && !roles.contains(role)) {
            errors.rejectValue("notifications", ERROR_NOTIFICATIONS_COMBINATION);
        }
    }
}
