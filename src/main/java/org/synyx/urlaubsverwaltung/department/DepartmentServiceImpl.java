package org.synyx.urlaubsverwaltung.department;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.ZoneOffset.UTC;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * Implementation for {@link DepartmentService}.
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final DepartmentRepository departmentRepository;
    private final ApplicationService applicationService;

    @Autowired
    public DepartmentServiceImpl(DepartmentRepository departmentRepository, ApplicationService applicationService) {

        this.departmentRepository = departmentRepository;
        this.applicationService = applicationService;
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

        department.setLastModification(LocalDate.now(UTC));

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
    public List<Application> getApplicationsForLeaveOfMembersInDepartmentsOfPerson(Person member,
                                                                                   LocalDate startDate, LocalDate endDate) {

        List<Person> departmentMembers = getMembersOfAssignedDepartments(member);
        List<Application> departmentApplications = new ArrayList<>();

        departmentMembers.stream()
            .filter(departmentMember -> !departmentMember.equals(member))
            .forEach(departmentMember ->
                departmentApplications.addAll(
                    applicationService.getApplicationsForACertainPeriodAndPerson(startDate, endDate,
                        departmentMember)
                        .stream()
                        .filter(application ->
                            application.hasStatus(ApplicationStatus.ALLOWED)
                                || application.hasStatus(ApplicationStatus.TEMPORARY_ALLOWED)
                                || application.hasStatus(ApplicationStatus.WAITING))
                        .collect(Collectors.toList())));

        return departmentApplications;
    }


    private List<Person> getMembersOfAssignedDepartments(Person member) {

        Set<Person> relevantPersons = new HashSet<>();
        List<Department> departments = getAssignedDepartmentsOfMember(member);

        for (Department department : departments) {
            List<Person> members = department.getMembers();
            relevantPersons.addAll(members);
        }

        return new ArrayList<>(relevantPersons);
    }


    @Override
    public List<Person> getManagedMembersOfDepartmentHead(Person departmentHead) {

        Set<Person> relevantPersons = new HashSet<>();
        List<Department> departments = getManagedDepartmentsOfDepartmentHead(departmentHead);

        for (Department department : departments) {
            List<Person> members = department.getMembers();
            relevantPersons.addAll(members);
        }

        return new ArrayList<>(relevantPersons);
    }


    @Override
    public List<Person> getMembersForSecondStageAuthority(Person secondStageAuthority) {

        Set<Person> relevantPersons = new HashSet<>();
        List<Department> departments = getManagedDepartmentsOfSecondStageAuthority(secondStageAuthority);

        for (Department department : departments) {
            List<Person> members = department.getMembers();
            relevantPersons.addAll(members);
        }

        return new ArrayList<>(relevantPersons);
    }


    @Override
    public boolean isDepartmentHeadOfPerson(Person departmentHead, Person person) {

        if (departmentHead.hasRole(Role.DEPARTMENT_HEAD)) {
            List<Person> members = getManagedMembersOfDepartmentHead(departmentHead);

            return members.contains(person);
        }

        return false;
    }


    @Override
    public boolean isSecondStageAuthorityOfPerson(Person secondStageAuthority, Person person) {

        if (secondStageAuthority.hasRole(Role.SECOND_STAGE_AUTHORITY)) {
            List<Person> members = getMembersForSecondStageAuthority(secondStageAuthority);

            return members.contains(person);
        }

        return false;
    }
}
