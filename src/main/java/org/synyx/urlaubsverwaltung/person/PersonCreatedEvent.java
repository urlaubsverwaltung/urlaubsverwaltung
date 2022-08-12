package org.synyx.urlaubsverwaltung.person;

import org.springframework.context.ApplicationEvent;

class PersonCreatedEvent extends ApplicationEvent {

    private final Integer personId;
    private final String personNiceName;
    private final String username;
    private final String email;

    PersonCreatedEvent(Object source, Integer personId, String personNiceName, String username, String email) {
        super(source);
        this.personId = personId;
        this.personNiceName = personNiceName;
        this.username = username;
        this.email = email;
    }

    Integer getPersonId() {
        return personId;
    }

    String getPersonNiceName() {
        return personNiceName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
