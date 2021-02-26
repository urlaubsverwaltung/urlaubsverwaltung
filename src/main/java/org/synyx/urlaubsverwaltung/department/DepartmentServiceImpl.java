package org.synyx.urlaubsverwaltung.department;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.Clock;
import java.time.Instant;
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
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
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
    public boolean departmentExists(Integer departmentId) {
        return departmentRepository.existsById(departmentId);
    }

    @Override
    public Optional<Department> getDepartmentById(Integer departmentId) {
        return departmentRepository.findById(departmentId).map(this::mapToDepartment);
    }

    @Override
    public Department create(Department department) {

        final DepartmentEntity departmentEntity = mapToDepartmentEntityWithoutMembers(department);
        departmentEntity.setCreatedAt(LocalDate.now(clock));
        departmentEntity.setLastModification(LocalDate.now(clock));

        final Instant now = Instant.now(clock);

        final List<DepartmentMemberEmbeddable> departmentMembers = department.getMembers().stream().map(person -> {
            final DepartmentMemberEmbeddable memberEmbeddable = new DepartmentMemberEmbeddable();
            memberEmbeddable.setPerson(person);
            memberEmbeddable.setAccessionDate(now);
            return memberEmbeddable;
        }).collect(toList());

        departmentEntity.setMembers(departmentMembers);

        final DepartmentEntity createdDepartmentEntity = departmentRepository.save(departmentEntity);
        final Department createdDepartment = mapToDepartment(createdDepartmentEntity);

        LOG.info("Created department: {}", createdDepartment);

        return createdDepartment;
    }

    @Override
    public Department update(Department department) {

        final DepartmentEntity currentDepartmentEntity = departmentRepository.findById(department.getId())
            .orElseThrow(() -> new IllegalStateException("cannot update department since it does not exists."));

        final List<DepartmentMemberEmbeddable> departmentMembers =
            updatedDepartmentMembers(department.getMembers(), currentDepartmentEntity.getMembers());

        final DepartmentEntity departmentEntity = mapToDepartmentEntityWithoutMembers(department);
        departmentEntity.setCreatedAt(currentDepartmentEntity.getCreatedAt());
        departmentEntity.setLastModification(LocalDate.now(clock));
        departmentEntity.setMembers(departmentMembers);

        final DepartmentEntity updatedDepartmentEntity = departmentRepository.save(departmentEntity);
        final Department updatedDepartment = mapToDepartment(updatedDepartmentEntity);

        LOG.info("Updated department: {}", updatedDepartment);

        return updatedDepartment;
    }

    @Override
    public void delete(Integer departmentId) {

        if (this.departmentExists(departmentId)) {
            departmentRepository.deleteById(departmentId);
        } else {
            LOG.info("No department found for ID = {}, deletion is not necessary.", departmentId);
        }
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll().stream()
            .map(this::mapToDepartment)
            .collect(toList());
    }

    @Override
    public List<Department> getAssignedDepartmentsOfMember(Person member) {
        return departmentRepository.findByMembersPerson(member).stream()
            .map(this::mapToDepartment)
            .collect(toList());
    }

    @Override
    public List<Department> getManagedDepartmentsOfDepartmentHead(Person departmentHead) {
        return departmentRepository.getManagedDepartments(departmentHead).stream()
            .map(this::mapToDepartment)
            .collect(toList());
    }

    @Override
    public List<Department> getManagedDepartmentsOfSecondStageAuthority(Person secondStageAuthority) {
        return departmentRepository.getDepartmentsForSecondStageAuthority(secondStageAuthority).stream()
            .map(this::mapToDepartment)
            .collect(toList());
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
                        .filter(application -> application.hasStatus(ALLOWED)
                            || application.hasStatus(TEMPORARY_ALLOWED)
                            || application.hasStatus(WAITING)
                            || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED))
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

        final boolean isOwnData = person.getId().equals(signedInUser.getId());
        final boolean isBossOrOffice = signedInUser.hasRole(Role.OFFICE) || signedInUser.hasRole(Role.BOSS);
        final boolean isDepartmentHeadOfPerson = isDepartmentHeadOfPerson(signedInUser, person);
        final boolean isSecondStageAuthorityOfPerson = isSecondStageAuthorityOfPerson(signedInUser, person);

        final boolean isPrivilegedUser = isBossOrOffice || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson;

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

    private Department mapToDepartment(DepartmentEntity departmentEntity) {
        final Department department = new Department();

        department.setId(departmentEntity.getId());
        department.setName(departmentEntity.getName());
        department.setDescription(departmentEntity.getDescription());
        department.setDepartmentHeads(departmentEntity.getDepartmentHeads());
        department.setSecondStageAuthorities(departmentEntity.getSecondStageAuthorities());
        department.setTwoStageApproval(departmentEntity.isTwoStageApproval());
        department.setLastModification(departmentEntity.getLastModification());

        final List<Person> members = departmentEntity.getMembers().stream()
            .map(DepartmentMemberEmbeddable::getPerson)
            .collect(toList());

        department.setMembers(members);

        return department;
    }

    private DepartmentEntity mapToDepartmentEntityWithoutMembers(Department department) {
        final DepartmentEntity departmentEntity = new DepartmentEntity();

        departmentEntity.setId(department.getId());
        departmentEntity.setName(department.getName());
        departmentEntity.setDescription(department.getDescription());
        departmentEntity.setDepartmentHeads(department.getDepartmentHeads());
        departmentEntity.setSecondStageAuthorities(department.getSecondStageAuthorities());
        departmentEntity.setTwoStageApproval(department.isTwoStageApproval());
        departmentEntity.setLastModification(department.getLastModification());

        return departmentEntity;
    }

    private List<DepartmentMemberEmbeddable> updatedDepartmentMembers(List<Person> nextPersons, List<DepartmentMemberEmbeddable> currentMembers) {

        final List<DepartmentMemberEmbeddable> list = new ArrayList<>();
        final Instant now = Instant.now(clock);

        for (Person person : nextPersons) {
            final DepartmentMemberEmbeddable currentMember = currentMembers.stream()
                .filter(departmentMember -> departmentMember.getPerson().equals(person))
                .findFirst()
                .orElse(null);

            if (currentMember == null) {
                final DepartmentMemberEmbeddable departmentMember = new DepartmentMemberEmbeddable();
                departmentMember.setPerson(person);
                departmentMember.setAccessionDate(now);
                list.add(departmentMember);
            } else {
                list.add(currentMember);
            }
        }

        return list;
    }
}
