package org.synyx.urlaubsverwaltung.person;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.Nullable;

public class PersonDisabledEvent extends ApplicationEvent {

    private final Long personId;
    private final String personNiceName;
    private final String username;
    private final String email;

    public PersonDisabledEvent(Object source, Long personId, String personNiceName, @Nullable String username, String email) {
        super(source);
        this.personId = personId;
        this.personNiceName = personNiceName;
        this.username = username;
        this.email = email;
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
}
