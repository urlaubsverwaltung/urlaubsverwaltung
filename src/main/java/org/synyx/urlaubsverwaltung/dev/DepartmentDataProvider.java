package org.synyx.urlaubsverwaltung.dev;

import org.joda.time.DateTime;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;


/**
 * Provides department test data.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component
class DepartmentDataProvider {

    private final DepartmentService departmentService;

    @Autowired
    DepartmentDataProvider(DepartmentService departmentService) {

        this.departmentService = departmentService;
    }

    void createTestDepartment(String name, String description, List<Person> members, List<Person> departmentHeads,
        List<Person> secondStageAuthorities) {

        Department department = new Department();
        department.setName(name);
        department.setDescription(description);
        department.setLastModification(DateTime.now());
        department.setMembers(members);
        department.setDepartmentHeads(departmentHeads);
        department.setSecondStageAuthorities(secondStageAuthorities);

        if (!secondStageAuthorities.isEmpty()) {
            department.setTwostageapproval(true);
        }

        departmentService.create(department);
    }
}
