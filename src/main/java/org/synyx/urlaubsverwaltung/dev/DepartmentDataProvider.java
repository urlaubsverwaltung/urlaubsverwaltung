package org.synyx.urlaubsverwaltung.dev;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.ZonedDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;


/**
 * Provides department test data.
 */
@Component
@ConditionalOnProperty("testdata.create")
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
        department.setLastModification(ZonedDateTime.now(UTC).toLocalDate());
        department.setMembers(members);
        department.setDepartmentHeads(departmentHeads);
        department.setSecondStageAuthorities(secondStageAuthorities);

        if (!secondStageAuthorities.isEmpty()) {
            department.setTwoStageApproval(true);
        }

        departmentService.create(department);
    }
}
