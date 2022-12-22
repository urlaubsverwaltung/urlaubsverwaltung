package org.synyx.urlaubsverwaltung.department;

import org.springframework.context.ApplicationEvent;

public class PersonLeftDepartmentEvent extends ApplicationEvent {

    private final long personId;
    private final long departmentId;

    public PersonLeftDepartmentEvent(Object source, long personId, long departmentId) {
        super(source);
        this.personId = personId;
        this.departmentId = departmentId;
    }

    public long getPersonId() {
        return personId;
    }

    public long getDepartmentId() {
        return departmentId;
    }
}
