package org.synyx.urlaubsverwaltung.person;

import org.springframework.context.ApplicationEvent;

public class PersonDisabledEvent extends ApplicationEvent {

    private final Integer personId;
    private final String personNiceName;
    private final String username;
    private final String email;

    public PersonDisabledEvent(Object source, int personId, String personNiceName, String username, String email) {
        super(source);
        this.personId = personId;
        this.personNiceName = personNiceName;
        this.username = username;
        this.email = email;
    }

    public int getPersonId() {
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
}
