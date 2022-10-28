package org.synyx.urlaubsverwaltung.department;

import org.springframework.context.ApplicationEvent;

public class PersonLeftDepartmentEvent extends ApplicationEvent {

    private final int personId;
    private final int departmentId;

    public PersonLeftDepartmentEvent(Object source, int personId, int departmentId) {
        super(source);
        this.personId = personId;
        this.departmentId = departmentId;
    }

    public int getPersonId() {
        return personId;
    }

    public int getDepartmentId() {
        return departmentId;
    }
}
