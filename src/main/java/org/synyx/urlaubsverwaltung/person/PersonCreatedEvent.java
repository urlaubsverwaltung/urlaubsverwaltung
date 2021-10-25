package org.synyx.urlaubsverwaltung.person;

import org.springframework.context.ApplicationEvent;

class PersonCreatedEvent extends ApplicationEvent {

    private final Integer personId;
    private final String personNiceName;

    PersonCreatedEvent(Object source, Integer personId, String personNiceName) {
        super(source);
        this.personId = personId;
        this.personNiceName = personNiceName;
    }

    Integer getPersonId() {
        return personId;
    }

    String getPersonNiceName() {
        return personNiceName;
    }
}
