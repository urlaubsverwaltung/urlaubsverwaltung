package org.synyx.urlaubsverwaltung.dev;

import org.slf4j.Logger;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Stream.concat;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Provides department demo data.
 */
class DepartmentDataProvider {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final DepartmentService departmentService;
    private final Clock clock;

    DepartmentDataProvider(DepartmentService departmentService, Clock clock) {
        this.departmentService = departmentService;
        this.clock = clock;
    }

    void createTestDepartment(String name, String description) {

        if (departmentService.getDepartmentByName(name).isEmpty()) {
            final LocalDate now = LocalDate.now(clock);

            final Department department = new Department();
            department.setName(name);
            department.setDescription(description);
            department.setCreatedAt(now);
            department.setLastModification(now);
            departmentService.create(department);
        } else {
            LOG.info("department with name={} already exists - nothing to do", name);
        }
    }

    void addDepartmentMember(String departmentName, Person person) {

        final Optional<Department> optionalDepartment = retrieveDepartmentByName(departmentName);
        if (optionalDepartment.isEmpty()){
            return;
        }

        final Department department = optionalDepartment.get();
        final List<Person> members = department.getMembers();
        if (isNotInList(person, members)) {
            department.setMembers(addPersonToList(person, members));
            departmentService.update(department);
        } else {
            LOG.info("person.id={} already member of department with name={} - nothing to do!", person.getId(), departmentName);
        }
    }

    void addDepartmentHead(String departmentName, Person person) {

        final Optional<Department> optionalDepartment = retrieveDepartmentByName(departmentName);
        if (optionalDepartment.isEmpty()){
            return;
        }

        final Department department = optionalDepartment.get();
        final List<Person> departmentHeads = department.getDepartmentHeads();
        if (isNotInList(person, departmentHeads)) {
            department.setDepartmentHeads(addPersonToList(person, departmentHeads));
            departmentService.update(department);
        } else {
            LOG.info("person.id={} already departmentHeads of department with name={} - nothing to do!", person.getId(), departmentName);
        }
    }

    void addDepartmentSecondStageAuthority(String departmentName, Person person) {

        final Optional<Department> optionalDepartment = retrieveDepartmentByName(departmentName);
        if (optionalDepartment.isEmpty()){
            return;
        }

        final Department department = optionalDepartment.get();
        final List<Person> secondStageAuthorities = department.getSecondStageAuthorities();
        if (isNotInList(person, secondStageAuthorities)) {
            department.setSecondStageAuthorities(addPersonToList(person, secondStageAuthorities));
            department.setTwoStageApproval(true);
            departmentService.update(department);
        } else {
            LOG.info("person.id={} already secondStageAuthority of department with name={} - nothing to do!", person.getId(), departmentName);
        }
    }

    private Optional<Department> retrieveDepartmentByName(String departmentName) {
        final Optional<Department> optionalDepartment = departmentService.getDepartmentByName(departmentName);
        if (optionalDepartment.isEmpty()) {
            LOG.info("department with name={} doesn't exists!", departmentName);
            return Optional.empty();
        }
        return optionalDepartment;
    }

    private static boolean isNotInList(Person person, List<Person> department) {
        return department.stream().noneMatch(member -> member.getEmail().equalsIgnoreCase(person.getEmail()));
    }

    private static List<Person> addPersonToList(Person person, List<Person> members) {
        return concat(members.stream(), Stream.of(person)).toList();
    }
}
