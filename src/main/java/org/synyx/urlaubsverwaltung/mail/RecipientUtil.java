package org.synyx.urlaubsverwaltung.mail;

import org.springframework.util.StringUtils;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;


final class RecipientUtil {

    private RecipientUtil() {

        // HIDDEN
    }

    static List<String> getMailAddresses(Person... persons) {

        return getMailAddresses(Arrays.asList(persons));
    }


    static List<String> getMailAddresses(List<Person> persons) {

        return persons.stream()
            .filter(person -> StringUtils.hasText(person.getEmail()))
            .map(Person::getEmail)
            .collect(toList());
    }
}
