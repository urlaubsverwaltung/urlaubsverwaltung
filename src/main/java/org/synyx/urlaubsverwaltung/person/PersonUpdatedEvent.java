package org.synyx.urlaubsverwaltung.person;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.Nullable;

public class PersonUpdatedEvent extends ApplicationEvent {

    private final Long personId;
    private final String personNiceName;
    private final String username;
    private final String email;
    private final boolean active;

    public PersonUpdatedEvent(Object source, Long personId, String personNiceName, String username, @Nullable String email, boolean active) {
        super(source);
        this.personId = personId;
        this.personNiceName = personNiceName;
        this.username = username;
        this.email = email;
        this.active = active;
    }

    public Long getPersonId() {
        return personId;
    }

    public String getPersonNiceName() {
        return personNiceName;
    }

    public String getUsername() {
        return username;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }
}
