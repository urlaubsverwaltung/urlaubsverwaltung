package org.synyx.urlaubsverwaltung.person.web;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_DEPARTMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.web.PersonNotificationsMapper.mapToMailNotifications;

/**
 * This class validate if the notifications of a {@link PersonNotificationsDto} is filled correctly.
 */
@Component
class PersonNotificationsDtoValidator implements Validator {

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

        final Optional<Person> maybePerson = personService.getPersonByID(personNotificationsDto.getPersonId());
        if (maybePerson.isEmpty()) {
            return; // TODO global error
        }

        final Collection<Role> roles = maybePerson.get().getPermissions();
        final Collection<MailNotification> notifications = mapToMailNotifications(personNotificationsDto);

        validateCombinationOfNotificationAndRole(roles, notifications, List.of(OFFICE, BOSS), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL, errors);
        validateCombinationOfNotificationAndRole(roles, notifications, List.of(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_DEPARTMENT, errors);
        validateCombinationOfNotificationAndRole(roles, notifications, List.of(OFFICE, BOSS), NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL, errors);
        validateCombinationOfNotificationAndRole(roles, notifications, List.of(OFFICE, BOSS), NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_ALL, errors);
    }

    private void validateCombinationOfNotificationAndRole(Collection<Role> personRoles, Collection<MailNotification> notifications, List<Role> expectedRoles, MailNotification notification, Errors errors) {
        if (notifications.contains(notification) && personRoles.stream().noneMatch(expectedRoles::contains)) {
            errors.rejectValue("notifications", "error");
        }
    }
}
