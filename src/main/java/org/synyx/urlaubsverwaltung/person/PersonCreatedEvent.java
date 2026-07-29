package org.synyx.urlaubsverwaltung.person;

import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

public class PersonCreatedEvent extends ApplicationEvent {

    private final Long personId;
    private final String personNiceName;
    private final String username;
    private final String email;
    private final boolean active;
    private final Instant createdAt;

    public PersonCreatedEvent(Object source, Long personId, String personNiceName, String username, @Nullable String email, boolean active, Instant createdAt) {
        super(source);
        this.personId = personId;
        this.personNiceName = personNiceName;
        this.username = username;
        this.email = email;
        this.active = active;
        this.createdAt = createdAt;
    }

    Long getPersonId() {
        return personId;
    }

    String getPersonNiceName() {
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
