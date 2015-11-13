package org.synyx.urlaubsverwaltung.core.department;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.Role;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Implementation for {@link DepartmentService}.
 *
 * @author  Daniel Hammann - <hammann@synyx.de>
 * @author  Aljona Murygina - <murygina@synyx.de>
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger LOG = Logger.getLogger(DepartmentServiceImpl.class);

    private final DepartmentDAO departmentDAO;
    private final ApplicationService applicationService;

    @Autowired
    public DepartmentServiceImpl(DepartmentDAO departmentDAO, ApplicationService applicationService) {

        this.departmentDAO = departmentDAO;
        this.applicationService = applicationService;
    }

    @Override
    public Optional<Department> getDepartmentById(Integer departmentId) {

        return Optional.ofNullable(departmentDAO.findOne(departmentId));
    }


    @Override
    public void create(Department department) {

        departmentDAO.save(department);

        LOG.info("Created department: " + department.toString());
    }


    @Override
    public void update(Department department) {

        department.setLastModification(DateTime.now());

        departmentDAO.save(department);

        LOG.info("Updated department: " + department.toString());
    }


    @Override
    public void delete(Integer departmentId) {

        if (departmentDAO.findOne(departmentId) == null) {
            LOG.info("No department found for ID = " + departmentId + ", deletion is not necessary.");
        } else {
            departmentDAO.delete(departmentId);
        }
    }


    @Override
    public List<Department> getAllDepartments() {

        return departmentDAO.findAll();
    }


    @Override
    public List<Department> getAssignedDepartmentsOfMember(Person member) {

        return departmentDAO.getAssignedDepartments(member);
    }


    @Override
    public List<Department> getManagedDepartmentsOfDepartmentHead(Person departmentHead) {

        return departmentDAO.getManagedDepartments(departmentHead);
    }


    @Override
    public List<Application> getApplicationsForLeaveOfMembersInDepartmentsOfPerson(Person member,
        DateMidnight startDate, DateMidnight endDate) {

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
    public boolean isDepartmentHeadOfPerson(Person departmentHead, Person person) {

        if (departmentHead.hasRole(Role.DEPARTMENT_HEAD)) {
            List<Person> members = getManagedMembersOfDepartmentHead(departmentHead);

            if (members.contains(person)) {
                return true;
            }
        }

        return false;
    }
}
