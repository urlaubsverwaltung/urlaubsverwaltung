package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;


/**
 * Provides department test data.
 */
class DepartmentDataProvider {

    private final DepartmentService departmentService;

    DepartmentDataProvider(DepartmentService departmentService) {

        this.departmentService = departmentService;
    }

    void createTestDepartment(String name, String description, List<Person> members, List<Person> departmentHeads,
                              List<Person> secondStageAuthorities) {

        final Department department = new Department();
        department.setName(name);
        department.setDescription(description);
        department.setLastModification(LocalDate.now(UTC));
        department.setMembers(members);
        department.setDepartmentHeads(departmentHeads);
        department.setSecondStageAuthorities(secondStageAuthorities);

        if (!secondStageAuthorities.isEmpty()) {
            department.setTwoStageApproval(true);
        }

        departmentService.create(department);
    }
}
