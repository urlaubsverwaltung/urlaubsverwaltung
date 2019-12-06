package org.synyx.urlaubsverwaltung.person.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.web.MailAddressValidationUtil;

import java.util.Collection;
import java.util.Optional;


/**
 * This class validate if master data of a {@link Person} is filled correctly.
 */
@Component
class PersonValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_LENGTH = "error.entry.tooManyChars";
    private static final String ERROR_EMAIL = "error.entry.mail";
    private static final String ERROR_USERNAME_UNIQUE = "person.form.data.login.error";
    private static final String ERROR_PERMISSIONS_MANDATORY = "person.form.permissions.error.mandatory";
    private static final String ERROR_PERMISSIONS_INACTIVE_ROLE = "person.form.permissions.error.inactive";
    private static final String ERROR_PERMISSIONS_USER_ROLE = "person.form.permissions.error.user";
    private static final String ERROR_PERMISSIONS_COMBINATION = "person.form.permissions.error.combination";
    private static final String ERROR_NOTIFICATIONS_COMBINATION = "person.form.notifications.error.combination";

    private static final String ATTRIBUTE_USERNAME = "username";
    private static final String ATTRIBUTE_FIRST_NAME = "firstName";
    private static final String ATTRIBUTE_LAST_NAME = "lastName";
    private static final String ATTRIBUTE_EMAIL = "email";
    private static final String ATTRIBUTE_PERMISSIONS = "permissions";

    private static final int MAX_CHARS = 50;

    private final PersonService personService;

    @Autowired
    PersonValidator(PersonService personService) {

        this.personService = personService;
    }

    @Override
    public boolean supports(Class<?> clazz) {

        return Person.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {

        Person person = (Person) target;

        if (person.getId() == null) {
            validateUsername(person.getUsername(), errors);
        }

        validateName(person.getFirstName(), ATTRIBUTE_FIRST_NAME, errors);

        validateName(person.getLastName(), ATTRIBUTE_LAST_NAME, errors);

        validateEmail(person.getEmail(), errors);

        validatePermissions(person, errors);

        validateNotifications(person, errors);
    }


    void validateUsername(String username, Errors errors) {

        validateName(username, ATTRIBUTE_USERNAME, errors);

        if (!errors.hasFieldErrors(ATTRIBUTE_USERNAME)) {
            // validate unique username
            Optional<Person> person = personService.getPersonByUsername(username);

            if (person.isPresent()) {
                errors.rejectValue(ATTRIBUTE_USERNAME, ERROR_USERNAME_UNIQUE);
            }
        }
    }


    void validateName(String name, String field, Errors errors) {

        if (StringUtils.hasText(name)) {
            if (!validateStringLength(name)) {
                errors.rejectValue(field, ERROR_LENGTH);
            }
        } else {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
        }
    }


    void validateEmail(String email, Errors errors) {

        if (StringUtils.hasText(email)) {
            if (!validateStringLength(email)) {
                errors.rejectValue(ATTRIBUTE_EMAIL, ERROR_LENGTH);
            }

            if (!MailAddressValidationUtil.hasValidFormat(email)) {
                errors.rejectValue(ATTRIBUTE_EMAIL, ERROR_EMAIL);
            }
        } else {
            errors.rejectValue(ATTRIBUTE_EMAIL, ERROR_MANDATORY_FIELD);
        }
    }


    private boolean validateStringLength(String string) {

        return string.length() <= MAX_CHARS;
    }


    void validatePermissions(Person person, Errors errors) {

        Collection<Role> roles = person.getPermissions();

        if (roles == null || roles.isEmpty()) {
            errors.rejectValue(ATTRIBUTE_PERMISSIONS, ERROR_PERMISSIONS_MANDATORY);

            return;
        }

        // if role inactive set, then only this role may be selected
        if (roles.contains(Role.INACTIVE) && roles.size() != 1) {
            errors.rejectValue(ATTRIBUTE_PERMISSIONS, ERROR_PERMISSIONS_INACTIVE_ROLE);

            return;
        }

        // user role must always be selected for active user
        if (!roles.contains(Role.INACTIVE) && !roles.contains(Role.USER)) {
            errors.rejectValue(ATTRIBUTE_PERMISSIONS, ERROR_PERMISSIONS_USER_ROLE);

            return;
        }

        validateCombinationOfRoles(roles, errors);
    }


    private void validateCombinationOfRoles(Collection<Role> roles, Errors errors) {

        if (roles.contains(Role.DEPARTMENT_HEAD) && (roles.contains(Role.BOSS) || roles.contains(Role.OFFICE))) {
            errors.rejectValue(ATTRIBUTE_PERMISSIONS, ERROR_PERMISSIONS_COMBINATION);

            return;
        }

        if (roles.contains(Role.SECOND_STAGE_AUTHORITY) && (roles.contains(Role.BOSS) || roles.contains(Role.OFFICE))) {
            errors.rejectValue(ATTRIBUTE_PERMISSIONS, ERROR_PERMISSIONS_COMBINATION);
        }
    }


    void validateNotifications(Person person, Errors errors) {

        Collection<Role> roles = person.getPermissions();
        Collection<MailNotification> notifications = person.getNotifications();

        if (roles != null) {
            validateCombinationOfNotificationAndRole(roles, notifications, Role.DEPARTMENT_HEAD,
                MailNotification.NOTIFICATION_DEPARTMENT_HEAD, errors);

            validateCombinationOfNotificationAndRole(roles, notifications, Role.SECOND_STAGE_AUTHORITY,
                MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY, errors);

            validateCombinationOfNotificationAndRole(roles, notifications, Role.BOSS,
                MailNotification.NOTIFICATION_BOSS_ALL, errors);

            validateCombinationOfNotificationAndRole(roles, notifications, Role.BOSS,
                MailNotification.NOTIFICATION_BOSS_DEPARTMENTS, errors);

            validateCombinationOfNotificationAndRole(roles, notifications, Role.OFFICE,
                MailNotification.NOTIFICATION_OFFICE, errors);
        }
    }


    private void validateCombinationOfNotificationAndRole(Collection<Role> roles,
                                                          Collection<MailNotification> notifications, Role role, MailNotification notification, Errors errors) {

        if (notifications.contains(notification) && !roles.contains(role)) {
            errors.rejectValue("notifications", ERROR_NOTIFICATIONS_COMBINATION);
        }
    }
}
