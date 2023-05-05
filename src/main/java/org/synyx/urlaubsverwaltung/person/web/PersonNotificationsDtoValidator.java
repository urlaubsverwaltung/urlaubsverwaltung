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
import java.util.Optional;

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
        validateNotifications((PersonNotificationsDto) target, errors);
    }

    void validateNotifications(PersonNotificationsDto personNotificationsDto, Errors errors) {

        final Optional<Person> maybePerson = personService.getPersonByID(personNotificationsDto.getPersonId());
        if (maybePerson.isEmpty()) {
            errors.reject("error");
            return;
        }

        final Collection<Role> roles = maybePerson.get().getPermissions();
        for (MailNotification mailNotification : mapToMailNotifications(personNotificationsDto)) {
            if (!mailNotification.isValidWith(roles)) {
                errors.reject("error");
            }
        }
    }
}
