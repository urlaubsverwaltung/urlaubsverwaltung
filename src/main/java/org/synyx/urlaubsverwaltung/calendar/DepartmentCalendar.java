package org.synyx.urlaubsverwaltung.calendar;

import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
class DepartmentCalendar extends Calendar {

    @NotNull
    @OneToOne
    private Department department;

    @NotNull
    @OneToOne
    private Person person;

    public DepartmentCalendar() {
        // for hibernate - do not use this
    }

    DepartmentCalendar(Department department, Person person) {
        super();
        this.department = department;
        this.person = person;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
