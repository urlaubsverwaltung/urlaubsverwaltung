package org.synyx.urlaubsverwaltung.calendar;

import org.synyx.urlaubsverwaltung.department.Department;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
class DepartmentCalendar extends Calendar {

    @NotNull
    @OneToOne
    private Department department;

    public DepartmentCalendar() {
        // for hibernate - do not use this
    }

    DepartmentCalendar(Department department) {
        super();
        this.department = department;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
