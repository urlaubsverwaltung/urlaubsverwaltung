package org.synyx.urlaubsverwaltung.person;

import org.springframework.context.ApplicationEvent;

public class PersonCreatedEvent extends ApplicationEvent {

    private final Integer personId;
    private final String personNiceName;
    private final String username;
    private final String email;
    private final boolean active;

    PersonCreatedEvent(Object source, Integer personId, String personNiceName, String username, String email, boolean active) {
        super(source);
        this.personId = personId;
        this.personNiceName = personNiceName;
        this.username = username;
        this.email = email;
        this.active = active;
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

    public boolean isActive() {
        return active;
    }
}
