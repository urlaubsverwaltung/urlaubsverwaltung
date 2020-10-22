package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;


/**
 * Provides department demo data.
 */
class DepartmentDataProvider {

    private final DepartmentService departmentService;
    private final Clock clock;

    DepartmentDataProvider(DepartmentService departmentService, Clock clock) {

        this.departmentService = departmentService;
        this.clock = clock;
    }

    void createTestDepartment(String name, String description, List<Person> members, List<Person> departmentHeads,
                              List<Person> secondStageAuthorities) {

        final Department department = new Department();
        department.setName(name);
        department.setDescription(description);
        department.setLastModification(LocalDate.now(clock));
        department.setMembers(members);
        department.setDepartmentHeads(departmentHeads);
        department.setSecondStageAuthorities(secondStageAuthorities);

        if (!secondStageAuthorities.isEmpty()) {
            department.setTwoStageApproval(true);
        }

        departmentService.create(department);
    }
}
