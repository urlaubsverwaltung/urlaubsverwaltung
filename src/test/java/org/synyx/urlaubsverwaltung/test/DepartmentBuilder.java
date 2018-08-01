package org.synyx.urlaubsverwaltung.test;

import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;


public class DepartmentBuilder {

    private Department department;

    public DepartmentBuilder() {

        department = new Department();
    }

    public DepartmentBuilder build() {

        return this;
    }


    public DepartmentBuilder withMembers(List<Person> members) {

        this.department.setMembers(members);

        return this;
    }


    public DepartmentBuilder withName(String name) {

        department.setName(name);

        return this;
    }


    public Department get() {

        return department;
    }


    public Department then() {

        return department;
    }
}
