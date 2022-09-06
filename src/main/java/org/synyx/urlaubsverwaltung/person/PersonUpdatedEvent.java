package org.synyx.urlaubsverwaltung.person;

import org.springframework.context.ApplicationEvent;

public class PersonUpdatedEvent extends ApplicationEvent {

    private final Integer personId;
    private final String personNiceName;
    private final String username;
    private final String email;
    private boolean active;

    PersonUpdatedEvent(Object source, Integer personId, String personNiceName, String username, String email, boolean active) {
        super(source);
        this.personId = personId;
        this.personNiceName = personNiceName;
        this.username = username;
        this.email = email;
        this.active = active;
    }

    public Integer getPersonId() {
        return personId;
    }

    public String getPersonNiceName() {
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
