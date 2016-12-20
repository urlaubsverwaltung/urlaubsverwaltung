package org.synyx.urlaubsverwaltung.core.mail;

import org.springframework.util.StringUtils;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
final class RecipientUtil {

    private RecipientUtil() {

        // HIDDEN
    }

    static List<String> getMailAddresses(Person... persons) {

        return getMailAddresses(Arrays.asList(persons));
    }


    static List<String> getMailAddresses(List<Person> persons) {

        return persons.stream().filter(person -> StringUtils.hasText(person.getEmail())).map(person ->
                    person.getEmail()).collect(Collectors.toList());
    }
}
