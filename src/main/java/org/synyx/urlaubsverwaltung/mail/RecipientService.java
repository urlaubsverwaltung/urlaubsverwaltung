package org.synyx.urlaubsverwaltung.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.hasText;


/**
 * Provides functionality to get the correct mail recipients for different use cases.
 */
@Service
class RecipientService {

    private final PersonService personService;

    @Autowired
    RecipientService(PersonService personService) {

        this.personService = personService;
    }

    /**
     * Get all persons with the given notification type.
     *
     * @param notification to get all persons for
     * @return list of recipients with the given notification type
     */
    List<Person> getRecipientsWithNotificationType(MailNotification notification) {

        return personService.getPersonsWithNotificationType(notification);
    }


    List<String> getMailAddresses(Person... persons) {

        return getMailAddresses(asList(persons));
    }


    List<String> getMailAddresses(List<Person> persons) {

        return persons.stream()
            .filter(person -> hasText(person.getEmail()))
            .map(Person::getEmail)
            .collect(toList());
    }
}
