package org.synyx.urlaubsverwaltung.person.web;

import org.synyx.urlaubsverwaltung.person.Person;

final class PersonNotificationsMapper {

    private PersonNotificationsMapper() {
        // ok
    }

    static Person merge(Person person, PersonNotificationsDto personNotificationsDto) {
        person.setNotifications(personNotificationsDto.getNotifications());
        return person;
    }
}
