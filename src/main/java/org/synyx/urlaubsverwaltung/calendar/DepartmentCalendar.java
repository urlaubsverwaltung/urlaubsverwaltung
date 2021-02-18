package org.synyx.urlaubsverwaltung.calendar;

import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
class DepartmentCalendar extends Calendar {

    @NotNull
    @Column(name = "department_id")
    private Integer departmentId;

    @NotNull
    @OneToOne
    private Person person;

    public DepartmentCalendar() {
        // for hibernate - do not use this
    }

    DepartmentCalendar(Integer departmentId, Person person) {
        super();
        this.departmentId = departmentId;
        this.person = person;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
