package org.synyx.urlaubsverwaltung.department;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

/**
 * Implementation for {@link DepartmentService}.
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final DepartmentRepository departmentRepository;
    private final ApplicationService applicationService;
    private final Clock clock;

    @Autowired
    public DepartmentServiceImpl(DepartmentRepository departmentRepository, ApplicationService applicationService, Clock clock) {

        this.departmentRepository = departmentRepository;
        this.applicationService = applicationService;
        this.clock = clock;
    }

    @Override
    public Optional<Department> getDepartmentById(Integer departmentId) {
        return departmentRepository.findById(departmentId);
    }

    @Override
    public void create(Department department) {

        departmentRepository.save(department);

        LOG.info("Created department: {}", department);
    }

    @Override
    public void update(Department department) {

        department.setLastModification(LocalDate.now(clock));

        departmentRepository.save(department);

        LOG.info("Updated department: {}", department);
    }

    @Override
    public void delete(Integer departmentId) {

        if (departmentRepository.findById(departmentId).isPresent()) {
            departmentRepository.deleteById(departmentId);
        } else {
            LOG.info("No department found for ID = {}, deletion is not necessary.", departmentId);
        }
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public List<Department> getAssignedDepartmentsOfMember(Person member) {
        return departmentRepository.getAssignedDepartments(member);
    }

    @Override
    public List<Department> getManagedDepartmentsOfDepartmentHead(Person departmentHead) {
        return departmentRepository.getManagedDepartments(departmentHead);
    }

    @Override
    public List<Department> getManagedDepartmentsOfSecondStageAuthority(Person secondStageAuthority) {
        return departmentRepository.getDepartmentsForSecondStageAuthority(secondStageAuthority);
    }

    @Override
    public List<Application> getApplicationsForLeaveOfMembersInDepartmentsOfPerson(Person member, LocalDate startDate, LocalDate endDate) {

        final List<Person> departmentMembers = getMembersOfAssignedDepartments(member);
        final List<Application> departmentApplications = new ArrayList<>();

        departmentMembers.stream()
            .filter(departmentMember -> !departmentMember.equals(member))
            .forEach(departmentMember ->
                departmentApplications.addAll(
                    applicationService.getApplicationsForACertainPeriodAndPerson(startDate, endDate, departmentMember)
                        .stream()
                        .filter(application -> application.hasStatus(ALLOWED) || application.hasStatus(TEMPORARY_ALLOWED) || application.hasStatus(WAITING))
                        .collect(toList())));

        return departmentApplications;
    }

    private List<Person> getMembersOfAssignedDepartments(Person member) {

        final Set<Person> relevantPersons = new HashSet<>();
        final List<Department> departments = getAssignedDepartmentsOfMember(member);

        for (Department department : departments) {
            relevantPersons.addAll(department.getMembers());
        }

        return new ArrayList<>(relevantPersons);
    }


    @Override
    public List<Person> getManagedMembersOfDepartmentHead(Person departmentHead) {

        final Set<Person> relevantPersons = new HashSet<>();
        final List<Department> departments = getManagedDepartmentsOfDepartmentHead(departmentHead);

        departments.forEach(department ->
            relevantPersons.addAll(department.getMembers().stream().filter(isNotSecondStageIn(department)).collect(toSet()))
        );

        return new ArrayList<>(relevantPersons);
    }

    @Override
    public List<Person> getManagedMembersForSecondStageAuthority(Person secondStageAuthority) {

        final Set<Person> relevantPersons = new HashSet<>();
        final List<Department> departments = getManagedDepartmentsOfSecondStageAuthority(secondStageAuthority);

        departments.forEach(department ->
            relevantPersons.addAll(department.getMembers().stream().filter(isNotSecondStageIn(department)).collect(toSet()))
        );

        return new ArrayList<>(relevantPersons);
    }

    @Override
    public boolean isDepartmentHeadOfPerson(Person departmentHead, Person person) {

        if (departmentHead.hasRole(DEPARTMENT_HEAD)) {
            return getManagedMembersOfDepartmentHead(departmentHead).contains(person);
        }

        return false;
    }

    @Override
    public boolean isSecondStageAuthorityOfPerson(Person secondStageAuthority, Person person) {

        if (secondStageAuthority.hasRole(SECOND_STAGE_AUTHORITY)) {
            return getManagedMembersForSecondStageAuthority(secondStageAuthority).contains(person);
        }

        return false;
    }

    @Override
    public boolean isSignedInUserAllowedToAccessPersonData(Person signedInUser, Person person) {

        boolean isOwnData = person.getId().equals(signedInUser.getId());
        boolean isBossOrOffice = signedInUser.hasRole(Role.OFFICE) || signedInUser.hasRole(Role.BOSS);
        boolean isDepartmentHeadOfPerson = isDepartmentHeadOfPerson(signedInUser, person);
        boolean isSecondStageAuthorityOfPerson = isSecondStageAuthorityOfPerson(signedInUser, person);

        boolean isPrivilegedUser = isBossOrOffice || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson;

        return isOwnData || isPrivilegedUser;
    }

    @Override
    public List<Department> getAllowedDepartmentsOfPerson(Person person) {

        if (person.hasRole(BOSS) || person.hasRole(OFFICE)) {
            return getAllDepartments();
        } else if (person.hasRole(SECOND_STAGE_AUTHORITY)) {
            return getManagedDepartmentsOfSecondStageAuthority(person);
        } else if (person.hasRole(DEPARTMENT_HEAD)) {
            return getManagedDepartmentsOfDepartmentHead(person);
        } else {
            return getAssignedDepartmentsOfMember(person);
        }
    }

    private Predicate<Person> isNotSecondStageIn(Department department) {
        return person -> !department.getSecondStageAuthorities().contains(person);
    }
}
