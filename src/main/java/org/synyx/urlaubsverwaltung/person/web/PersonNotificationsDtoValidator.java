package org.synyx.urlaubsverwaltung.person.web;

import org.slf4j.Logger;
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

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER;
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

    private static final Logger LOG = getLogger(lookup().lookupClass());

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
            LOG.warn("Cannot validate persons notification without person with id {}.", personNotificationsDto.getPersonId());
            errors.reject("error");
            return;
        }

        final Person person = maybePerson.get();
        final Collection<Role> roles = person.getPermissions();
        final Collection<MailNotification> notifications = mapToMailNotifications(personNotificationsDto);

        final List<Role> officeOrBoss = List.of(OFFICE, BOSS);
        final List<Role> bossOrDHOrSSA = List.of(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        validateCombinationOfNotificationAndRole(roles, notifications, officeOrBoss, NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL, errors);
        validateCombinationOfNotificationAndRole(roles, notifications, officeOrBoss, NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_ALL, errors);
        validateCombinationOfNotificationAndRole(roles, notifications, bossOrDHOrSSA, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED, errors);
        validateCombinationOfNotificationAndRole(roles, notifications, bossOrDHOrSSA, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED, errors);
        validateCombinationOfNotificationAndRole(roles, notifications, bossOrDHOrSSA, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED, errors);
        validateCombinationOfNotificationAndRole(roles, notifications, bossOrDHOrSSA, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED, errors);
        validateCombinationOfNotificationAndRole(roles, notifications, bossOrDHOrSSA, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED, errors);
        validateCombinationOfNotificationAndRole(roles, notifications, bossOrDHOrSSA, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION, errors);
        validateCombinationOfNotificationAndRole(roles, notifications, bossOrDHOrSSA, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED, errors);
        validateCombinationOfNotificationAndRole(roles, notifications, bossOrDHOrSSA, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED, errors);
        validateCombinationOfNotificationAndRole(roles, notifications, bossOrDHOrSSA, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER, errors);

        final boolean cannotHaveNotificationManagementCancellationRequested = !person.hasRole(OFFICE) &&
            ((person.hasRole(BOSS) || person.hasRole(DEPARTMENT_HEAD) || person.hasRole(SECOND_STAGE_AUTHORITY)) && !person.hasRole(Role.APPLICATION_CANCELLATION_REQUESTED));
        if (notifications.contains(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED) && cannotHaveNotificationManagementCancellationRequested) {
            errors.reject("error");
        }
    }

    private void validateCombinationOfNotificationAndRole(Collection<Role> personRoles, Collection<MailNotification> notifications, List<Role> expectedRoles, MailNotification notification, Errors errors) {
        if (notifications.contains(notification) && personRoles.stream().noneMatch(expectedRoles::contains)) {
            errors.reject("error");
        }
    }
}
