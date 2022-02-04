package org.synyx.urlaubsverwaltung.department;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

/**
 * Implementation for {@link DepartmentService}.
 */
@Service
class DepartmentServiceImpl implements DepartmentService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final DepartmentRepository departmentRepository;
    private final ApplicationService applicationService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Clock clock;

    @Autowired
    DepartmentServiceImpl(DepartmentRepository departmentRepository, ApplicationService applicationService, ApplicationEventPublisher applicationEventPublisher, Clock clock) {
        this.departmentRepository = departmentRepository;
        this.applicationService = applicationService;
        this.applicationEventPublisher = applicationEventPublisher;
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
        sendMemberLeftDepartmentEvent(department, currentDepartmentEntity);

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
        return departmentRepository.findByDepartmentHeads(departmentHead).stream()
            .map(this::mapToDepartment)
            .collect(toList());
    }

    @Override
    public List<Department> getManagedDepartmentsOfSecondStageAuthority(Person secondStageAuthority) {
        return departmentRepository.findBySecondStageAuthorities(secondStageAuthority).stream()
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

    @Override
    public List<Person> getMembersForDepartmentHead(Person departmentHead) {
        return getManagedDepartmentsOfDepartmentHead(departmentHead).stream()
            .map(Department::getMembers)
            .flatMap(List::stream)
            .distinct()
            .collect(toList());
    }

    @Override
    public List<Person> getMembersForSecondStageAuthority(Person secondStageAuthority) {
        return getManagedDepartmentsOfSecondStageAuthority(secondStageAuthority).stream()
            .map(Department::getMembers)
            .flatMap(List::stream)
            .distinct()
            .collect(toList());
    }

    @Override
    public boolean isDepartmentHeadAllowedToManagePerson(Person departmentHead, Person person) {
        if (departmentHead.hasRole(DEPARTMENT_HEAD)) {
            return getManagedMembersOfDepartmentHead(departmentHead).contains(person);
        }

        return false;
    }

    @Override
    public boolean isSecondStageAuthorityAllowedToManagePerson(Person secondStageAuthority, Person person) {
        if (secondStageAuthority.hasRole(SECOND_STAGE_AUTHORITY)) {
            return getManagedMembersForSecondStageAuthority(secondStageAuthority).contains(person);
        }

        return false;
    }

    @Override
    public boolean isSignedInUserAllowedToAccessPersonData(Person signedInUser, Person person) {

        final boolean isOwnData = person.getId().equals(signedInUser.getId());
        final boolean isBossOrOffice = signedInUser.hasRole(OFFICE) || signedInUser.hasRole(BOSS);
        final boolean isSecondStageAuthorityAllowedToAccessPersonalData = isSecondStageAuthorityAllowedToAccessPersonData(signedInUser, person);
        final boolean isDepartmentHeadAllowedToAccessPersonalData = isDepartmentHeadAllowedToAccessPersonData(signedInUser, person);

        return isOwnData || isBossOrOffice || isDepartmentHeadAllowedToAccessPersonalData || isSecondStageAuthorityAllowedToAccessPersonalData;
    }

    @Override
    public List<Department> getAllowedDepartmentsOfPerson(Person person) {

        if (person.hasRole(BOSS) || person.hasRole(OFFICE)) {
            return getAllDepartments();
        }

        final List<Department> departments = new ArrayList<>();
        if (person.hasRole(SECOND_STAGE_AUTHORITY)) {
            departments.addAll(getManagedDepartmentsOfSecondStageAuthority(person));
        }

        if (person.hasRole(DEPARTMENT_HEAD)) {
            departments.addAll(getManagedDepartmentsOfDepartmentHead(person));
        }

        departments.addAll(getAssignedDepartmentsOfMember(person));

        return departments.stream()
            .distinct()
            .collect(toList());
    }

    @Override
    public long getNumberOfDepartments() {
        return departmentRepository.count();
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
        department.setCreatedAt(departmentEntity.getCreatedAt());
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

    private void sendMemberLeftDepartmentEvent(Department department, DepartmentEntity currentDepartmentEntity) {
        currentDepartmentEntity.getMembers().stream()
            .map(DepartmentMemberEmbeddable::getPerson)
            .filter(oldMember -> !department.getMembers().contains(oldMember))
            .forEach(person -> applicationEventPublisher.publishEvent(new PersonLeftDepartmentEvent(this, person.getId(), department.getId())));
    }

    private List<Person> getMembersOfAssignedDepartments(Person member) {
        return getAssignedDepartmentsOfMember(member).stream()
            .map(Department::getMembers)
            .flatMap(List::stream)
            .distinct()
            .collect(toList());
    }

    private List<Person> getManagedMembersOfDepartmentHead(Person departmentHead) {
        return getManagedDepartmentsOfDepartmentHead(departmentHead)
            .stream()
            .flatMap(department -> department.getMembers().stream().filter(isNotSecondStageIn(department)))
            .distinct()
            .collect(toList());
    }

    private List<Person> getManagedMembersForSecondStageAuthority(Person secondStageAuthority) {
        return getManagedDepartmentsOfSecondStageAuthority(secondStageAuthority)
            .stream()
            .flatMap(department -> department.getMembers().stream().filter(isNotSecondStageIn(department)))
            .distinct()
            .collect(toList());
    }

    private boolean isSecondStageAuthorityAllowedToAccessPersonData(Person secondStageAuthority, Person person) {
        if (secondStageAuthority.hasRole(SECOND_STAGE_AUTHORITY)) {
            return getMembersForSecondStageAuthority(secondStageAuthority).contains(person);
        }

        return false;
    }

    private boolean isDepartmentHeadAllowedToAccessPersonData(Person departmentHead, Person person) {
        if (departmentHead.hasRole(DEPARTMENT_HEAD)) {
            return getMembersForDepartmentHead(departmentHead).contains(person);
        }

        return false;
    }
}
