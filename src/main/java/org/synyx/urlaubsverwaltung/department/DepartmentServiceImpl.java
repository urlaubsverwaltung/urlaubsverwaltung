package org.synyx.urlaubsverwaltung.department;

import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.search.SortComparator;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
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
    private final DepartmentMembershipService departmentMembershipService;
    private final ApplicationService applicationService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Clock clock;

    DepartmentServiceImpl(
        DepartmentRepository departmentRepository,
        DepartmentMembershipService departmentMembershipService,
        ApplicationService applicationService,
        ApplicationEventPublisher applicationEventPublisher,
        Clock clock
    ) {
        this.departmentRepository = departmentRepository;
        this.departmentMembershipService = departmentMembershipService;
        this.applicationService = applicationService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.clock = clock;
    }

    @Override
    public List<Person> getManagedMembersOfPerson(Person person, Year year) {
        return filterMembersByYear(year, managedDepartmentEntitiesOfPerson(person));
    }

    @Override
    public Page<Person> getManagedMembersOfPerson(Person person, PageableSearchQuery personPageableSearchQuery) {
        return getManagedMembersOfPerson(person, personPageableSearchQuery, not(Person::isInactive));
    }

    @Override
    public List<Person> getManagedActiveMembersOfPerson(Person person) {
        return getManagedMembersOfPerson(person).stream().filter(Person::isActive).toList();
    }

    @Override
    public Page<Person> getManagedInactiveMembersOfPerson(Person person, PageableSearchQuery personPageableSearchQuery) {
        return getManagedMembersOfPerson(person, personPageableSearchQuery, Person::isInactive);
    }

    @Override
    public Page<Person> getManagedMembersOfPersonAndDepartment(Person person, Long departmentId, PageableSearchQuery pageableSearchQuery) {
        final Predicate<Person> filter = nameContains(pageableSearchQuery.getQuery()).and(not(Person::isInactive));
        return managedMembersOfPersonAndDepartment(person, departmentId, pageableSearchQuery, filter);
    }

    @Override
    public Page<Person> getManagedInactiveMembersOfPersonAndDepartment(Person person, Long departmentId, PageableSearchQuery pageableSearchQuery) {
        final Predicate<Person> filter = nameContains(pageableSearchQuery.getQuery()).and(Person::isInactive);
        return managedMembersOfPersonAndDepartment(person, departmentId, pageableSearchQuery, filter);
    }

    private Page<Person> getManagedMembersOfPerson(Person person, PageableSearchQuery personPageableSearchQuery, Predicate<Person> predicate) {

        final Pageable pageable = personPageableSearchQuery.getPageable();
        final List<DepartmentEntity> departments = managedDepartmentEntitiesOfPerson(person);

        final List<Person> managedMembers = departments.stream()
            .map(DepartmentEntity::getMembers)
            .flatMap(List::stream)
            .map(DepartmentMemberEmbeddable::getPerson)
            .distinct()
            .filter(nameContains(personPageableSearchQuery.getQuery()).and(predicate))
            .sorted(new SortComparator<>(Person.class, pageable.getSort()))
            .toList();

        final List<Person> content = managedMembers.stream()
            .skip((long) pageable.getPageNumber() * pageable.getPageSize())
            .limit(pageable.getPageSize())
            .toList();

        return new PageImpl<>(content, pageable, managedMembers.size());
    }

    @Override
    public boolean departmentExists(Long departmentId) {
        return departmentRepository.existsById(departmentId);
    }

    @Override
    public Optional<Department> getDepartmentById(Long departmentId) {
        return departmentRepository.findById(departmentId).map(this::mapToDepartment);
    }

    @Override
    public Optional<Department> getDepartmentByName(String departmentName) {
        return departmentRepository.findFirstByName(departmentName).map(this::mapToDepartment);
    }

    @Override
    @Transactional
    public Department create(Department department) {

        final DepartmentEntity departmentEntity = mapToDepartmentEntityWithoutMembers(department);
        departmentEntity.setCreatedAt(LocalDate.now(clock));
        departmentEntity.setLastModification(LocalDate.now(clock));

        final Instant now = Instant.now(clock);

        final List<DepartmentMemberEmbeddable> departmentMembers = department.getMembers().stream()
            .map(person -> {
                final DepartmentMemberEmbeddable memberEmbeddable = new DepartmentMemberEmbeddable();
                memberEmbeddable.setPerson(person);
                memberEmbeddable.setAccessionDate(now);
                return memberEmbeddable;
            })
            .collect(toList());

        departmentEntity.setMembers(departmentMembers);

        final DepartmentEntity createdDepartmentEntity = departmentRepository.save(departmentEntity);
        final Department createdDepartment = mapToDepartment(createdDepartmentEntity);

        departmentMembershipService.createInitialMemberships(
            createdDepartmentEntity.getId(),
            toPersonIds(createdDepartment.getMembers()),
            toPersonIds(createdDepartment.getDepartmentHeads()),
            toPersonIds(createdDepartment.getSecondStageAuthorities())
        );

        LOG.info("Created department: {}", createdDepartment);

        return createdDepartment;
    }

    @Override
    @Transactional
    public Department update(Department department) {

        final DepartmentEntity currentDepartmentEntity = departmentRepository.findById(department.getId())
            .orElseThrow(() -> new IllegalStateException("cannot update department since it does not exists."));
        final Department currentDepartment = mapToDepartment(currentDepartmentEntity);

        final List<DepartmentMemberEmbeddable> departmentMembers =
            updatedDepartmentMembers(department.getMembers(), currentDepartmentEntity.getMembers());

        final DepartmentEntity departmentEntity = mapToDepartmentEntityWithoutMembers(department);
        departmentEntity.setCreatedAt(currentDepartmentEntity.getCreatedAt());
        departmentEntity.setLastModification(LocalDate.now(clock));
        departmentEntity.setMembers(departmentMembers);

        final DepartmentEntity updatedDepartmentEntity = departmentRepository.save(departmentEntity);
        final Department updatedDepartment = mapToDepartment(updatedDepartmentEntity);

        final DepartmentMembershipBucket currentBucket = membershipBucketOfDepartment(currentDepartment);
        final DepartmentMembershipBucket newBucket = membershipBucketOfDepartment(updatedDepartment);
        departmentMembershipService.updateDepartmentMemberships(newBucket, currentBucket);

        sendMemberLeftDepartmentEvent(department, currentDepartmentEntity);

        LOG.info("Updated department: {}", updatedDepartment);

        return updatedDepartment;
    }

    /**
     * Deletes all department head assignments of the given person.
     *
     * @param event the person who is deleted
     */
    @EventListener
    @Transactional
    void deleteAssignedDepartmentsOfMember(PersonDeletedEvent event) {
        getAssignedDepartmentsOfMember(event.person())
            .forEach(department -> {
                department.setMembers(
                    department.getMembers().stream()
                        .filter(not(isEqual(event.person())))
                        .collect(toList())
                );

                update(department);
            });
    }

    /**
     * Deletes all department head assignments of the given person.
     *
     * @param event the person who is deleted
     */
    @EventListener
    @Transactional
    void deleteDepartmentHead(PersonDeletedEvent event) {
        getManagedDepartmentsOfDepartmentHead(event.person())
            .forEach(department -> {
                department.setDepartmentHeads(
                    department.getDepartmentHeads().stream()
                        .filter(not(isEqual(event.person())))
                        .collect(toList())
                );

                update(department);
            });
    }


    /**
     * Deletes all second stage authorities assignments of the given person.
     *
     * @param event the person who is deleted
     */
    @EventListener
    @Transactional
    void deleteSecondStageAuthority(PersonDeletedEvent event) {
        getManagedDepartmentsOfSecondStageAuthority(event.person())
            .forEach(department -> {
                department.setSecondStageAuthorities(
                    department.getSecondStageAuthorities().stream()
                        .filter(not(isEqual(event.person())))
                        .collect(toList())
                );

                update(department);
            });
    }

    @Override
    public void delete(Long departmentId) {

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
            .sorted(departmentComparator())
            .toList();
    }

    @Override
    public List<Department> getAssignedDepartmentsOfMember(Person member) {
        return departmentRepository.findByMembersPerson(member).stream()
            .map(this::mapToDepartment)
            .sorted(departmentComparator())
            .toList();
    }

    @Override
    public List<Department> getManagedDepartmentsOfDepartmentHead(Person departmentHead) {
        return departmentRepository.findByDepartmentHeads(departmentHead).stream()
            .map(this::mapToDepartment)
            .sorted(departmentComparator())
            .toList();
    }

    @Override
    public List<Department> getManagedDepartmentsOfSecondStageAuthority(Person secondStageAuthority) {
        return departmentRepository.findBySecondStageAuthorities(secondStageAuthority).stream()
            .map(this::mapToDepartment)
            .sorted(departmentComparator())
            .toList();
    }

    @Override
    public List<Department> getDepartmentsPersonHasAccessTo(Person person) {

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
            .sorted(departmentComparator())
            .toList();
    }

    @Override
    public List<Application> getApplicationsFromColleaguesOf(Person person, LocalDate startDate, LocalDate endDate) {

        final List<Application> colleaguesApplications;

        if (getNumberOfDepartments() == 0) {
            colleaguesApplications = applicationService.getForStates(activeStatuses(), startDate, endDate).stream()
                .filter(application -> !application.getPerson().equals(person))
                .toList();
        } else {
            final List<Person> colleagues = getMembersOfAssignedDepartments(person).stream()
                .filter(not(isEqual(person)))
                .toList();
            colleaguesApplications = applicationService.getForStatesAndPerson(activeStatuses(), colleagues, startDate, endDate);
        }

        return colleaguesApplications.stream()
            .sorted(comparing(Application::getStartDate))
            .toList();
    }

    @Override
    public List<Person> getMembersForDepartmentHead(Person departmentHead) {
        return getManagedDepartmentsOfDepartmentHead(departmentHead).stream()
            .map(Department::getMembers)
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

    @Override
    public List<Person> getMembersForSecondStageAuthority(Person secondStageAuthority) {
        return getManagedDepartmentsOfSecondStageAuthority(secondStageAuthority).stream()
            .map(Department::getMembers)
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

    @Override
    public boolean isDepartmentHeadAllowedToManagePerson(Person departmentHead, Person person) {
        if (departmentHead.hasRole(DEPARTMENT_HEAD)) {
            return getManagedMembersOfDepartmentHead(departmentHead).contains(person);
        }

        return false;
    }

    @Override
    public List<Person> getManagedMembersOfDepartmentHead(Person departmentHead) {
        return getManagedDepartmentsOfDepartmentHead(departmentHead)
            .stream()
            .flatMap(department -> department.getMembers().stream().filter(isNotSecondStageIn(department)))
            .distinct()
            .toList();
    }

    @Override
    public boolean isSecondStageAuthorityAllowedToManagePerson(Person secondStageAuthority, Person person) {
        if (secondStageAuthority.hasRole(SECOND_STAGE_AUTHORITY)) {
            return getManagedMembersForSecondStageAuthority(secondStageAuthority).contains(person);
        }

        return false;
    }

    @Override
    public List<Person> getManagedMembersForSecondStageAuthority(Person secondStageAuthority) {
        return getManagedDepartmentsOfSecondStageAuthority(secondStageAuthority)
            .stream()
            .flatMap(department -> department.getMembers().stream())
            .distinct()
            .toList();
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
    public boolean isPersonAllowedToManageDepartment(Person person, Department department) {

        return person.hasRole(OFFICE) || person.hasRole(BOSS) ||
            (department.getDepartmentHeads().contains(person) && person.hasRole(DEPARTMENT_HEAD)) ||
            (department.getSecondStageAuthorities().contains(person) && person.hasRole(SECOND_STAGE_AUTHORITY));
    }

    @Override
    public long getNumberOfDepartments() {
        return departmentRepository.count();
    }

    @Override
    public Map<PersonId, List<String>> getDepartmentNamesByMembers(List<Person> persons) {

        final Map<List<Person>, List<Department>> personDepartmentList = departmentRepository.findDistinctByMembersPersonIn(persons).stream()
            .map(this::mapToDepartment)
            .collect(groupingBy(Department::getMembers));

        final Map<PersonId, List<String>> departmentsByPerson = new HashMap<>();
        personDepartmentList.forEach((personList, departmentList) -> {

            final List<String> departmentNames = departmentList.stream()
                .map(Department::getName)
                .toList();

            personList.forEach(person -> {
                if (persons.contains(person)) {
                    final PersonId personId = new PersonId(person.getId());
                    final List<String> bucket = departmentsByPerson.getOrDefault(personId, List.of());
                    departmentsByPerson.put(personId, merge(departmentNames, bucket));
                }
            });
        });

        return departmentsByPerson;
    }

    @Override
    public boolean hasDepartmentMatch(Person person, Person otherPerson) {

        final Set<DepartmentEntity> personDepartments = new HashSet<>(departmentRepository.findByMembersPerson(person));
        if (person.hasAnyRole(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)) {
            personDepartments.addAll(
                departmentRepository.findByDepartmentHeadsOrSecondStageAuthorities(person, person)
            );
        }

        final Set<DepartmentEntity> otherPersonDepartments = new HashSet<>(departmentRepository.findByMembersPerson(otherPerson));
        if (otherPerson.hasAnyRole(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)) {
            otherPersonDepartments.addAll(
                departmentRepository.findByDepartmentHeadsOrSecondStageAuthorities(otherPerson, otherPerson)
            );
        }

        return personDepartments.stream().anyMatch(otherPersonDepartments::contains);
    }

    private static DepartmentMembershipBucket membershipBucketOfDepartment(Department department) {

        final List<PersonId> newMemberIds = department.getMembers().stream().map(Person::getId).map(PersonId::new).toList();
        final List<PersonId> newDepartmentHeadIds = department.getDepartmentHeads().stream().map(Person::getId).map(PersonId::new).toList();
        final List<PersonId> newSecondStageIds = department.getSecondStageAuthorities().stream().map(Person::getId).map(PersonId::new).toList();

        return new DepartmentMembershipBucket(department.getId(),
            newMemberIds.stream().map(id -> new DepartmentMembership(id, department.getId(), DepartmentMembershipKind.MEMBER, null)).toList(),
            newDepartmentHeadIds.stream().map(id -> new DepartmentMembership(id, department.getId(), DepartmentMembershipKind.DEPARTMENT_HEAD, null)).toList(),
            newSecondStageIds.stream().map(id -> new DepartmentMembership(id, department.getId(), DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, null)).toList()
        );
    }

    private List<PersonId> toPersonIds(List<Person> persons) {
        return persons.stream().map(Person::getId).map(PersonId::new).toList();
    }

    private static List<String> merge(Collection<String> departmentNames, Collection<String> bucket) {
        return Stream.concat(bucket.stream(), departmentNames.stream()).toList();
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
            .toList();

        department.setMembers(members);

        return department;
    }

    private Department mapToDepartment(DepartmentEntity departmentEntity, DepartmentMembershipBucket membershipBucket, List<Person> persons) {

        final Map<Long, Person> personById = persons.stream().collect(toMap(Person::getId, identity()));

        final List<Person> members = membershipsToPersons(membershipBucket.members(), personById);
        final List<Person> departmentHeads = membershipsToPersons(membershipBucket.departmentHeads(), personById);
        final List<Person> secondStageAuthorities = membershipsToPersons(membershipBucket.secondStageAuthorities(), personById);

        final Department department = new Department();

        department.setId(departmentEntity.getId());
        department.setName(departmentEntity.getName());
        department.setDescription(departmentEntity.getDescription());
        department.setMembers(members);
        department.setDepartmentHeads(departmentHeads);
        department.setSecondStageAuthorities(secondStageAuthorities);
        department.setTwoStageApproval(departmentEntity.isTwoStageApproval());
        department.setCreatedAt(departmentEntity.getCreatedAt());
        department.setLastModification(departmentEntity.getLastModification());

        return department;
    }

    private List<Person> membershipsToPersons(Collection<DepartmentMembership> memberships, Map<Long, Person> personById) {

        final List<Person> persons = new ArrayList<>();

        for (DepartmentMembership membership : memberships) {
            final PersonId personId = membership.personId();
            final Person person = personById.get(personId.value());
            if (person == null) {
                LOG.warn("Department id={} membership with personId={} but no active person found. Skipping this membership.", membership.departmentId(), personId);
            } else {
                persons.add(person);
            }
        }

        return unmodifiableList(persons);
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

    /**
     * @param person person to get departments for
     * @return all departments managed by the given person
     */
    private List<DepartmentEntity> managedDepartmentEntitiesOfPerson(Person person) {
        final List<DepartmentEntity> departments;
        if (person.hasRole(OFFICE) || person.hasRole(BOSS)) {
            departments = departmentRepository.findAll();
        } else if (person.hasRole(DEPARTMENT_HEAD) && person.hasRole(SECOND_STAGE_AUTHORITY)) {
            departments = departmentRepository.findByDepartmentHeadsOrSecondStageAuthorities(person, person);
        } else if (person.hasRole(DEPARTMENT_HEAD)) {
            departments = departmentRepository.findByDepartmentHeads(person);
        } else if (person.hasRole(SECOND_STAGE_AUTHORITY)) {
            departments = departmentRepository.findBySecondStageAuthorities(person);
        } else {
            departments = List.of();
        }
        return departments;
    }

    private List<Person> getManagedMembersOfPerson(Person person) {
        return managedDepartmentEntitiesOfPerson(person).stream()
            .map(DepartmentEntity::getMembers)
            .flatMap(Collection::stream)
            .map(DepartmentMemberEmbeddable::getPerson)
            .distinct()
            .toList();
    }

    private Page<Person> managedMembersOfPersonAndDepartment(Person person, Long departmentId, PageableSearchQuery pageableSearchQuery, Predicate<Person> filter) {
        final Pageable pageable = pageableSearchQuery.getPageable();

        final DepartmentEntity departmentEntity = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new IllegalArgumentException("could not find department with id=" + departmentId));

        if (!doesPersonManageDepartment(person, departmentEntity)) {
            return Page.empty();
        }

        final List<DepartmentMemberEmbeddable> departmentMembers = departmentEntity.getMembers();

        final List<Person> content = departmentMembers.stream()
            .map(DepartmentMemberEmbeddable::getPerson)
            .filter(filter)
            .sorted(new SortComparator<>(Person.class, pageable.getSort()))
            .skip((long) pageable.getPageNumber() * pageable.getPageSize())
            .limit(pageable.getPageSize())
            .toList();

        return new PageImpl<>(content, pageable, departmentMembers.size());
    }

    private static boolean doesPersonManageDepartment(Person person, DepartmentEntity departmentEntity) {
        if (person.hasRole(BOSS) || person.hasRole(OFFICE)) {
            return true;
        }

        if (person.hasRole(DEPARTMENT_HEAD)) {
            return departmentEntity.getDepartmentHeads().stream().anyMatch(person::equals);
        }

        if (person.hasRole(SECOND_STAGE_AUTHORITY)) {
            return departmentEntity.getSecondStageAuthorities().stream().anyMatch(person::equals);
        }

        return false;
    }

    private void sendMemberLeftDepartmentEvent(Department department, DepartmentEntity currentDepartmentEntity) {
        currentDepartmentEntity.getMembers().stream()
            .map(DepartmentMemberEmbeddable::getPerson)
            .filter(oldMember -> !department.getMembers().contains(oldMember))
            .forEach(person -> applicationEventPublisher.publishEvent(new PersonLeftDepartmentEvent(this, person.getId(), department.getId())));
    }

    private List<Person> filterMembersByYear(Year year, List<DepartmentEntity> departmentEntities) {
        return departmentEntities.stream()
            .map(DepartmentEntity::getMembers)
            .flatMap(List::stream)
            .filter(departmentMember -> isMemberOfDepartmentInYear(departmentMember, year))
            .map(DepartmentMemberEmbeddable::getPerson)
            .distinct()
            .toList();
    }

    private boolean isMemberOfDepartmentInYear(DepartmentMemberEmbeddable departmentMemberEmbeddable, Year year) {
        final LocalDate accessionDate = LocalDate.ofInstant(departmentMemberEmbeddable.getAccessionDate(), clock.getZone());
        // We currently consider a member to be part of the department if they joined in the given year or earlier
        // as we don't track when a member leaves a department and when a member joins a department again.
        // This means that a member, which rejoined is only tracked for the latest membership.
        return accessionDate.getYear() <= year.getValue();
    }

    private List<Person> getMembersOfAssignedDepartments(Person member) {
        return getAssignedDepartmentsOfMember(member).stream()
            .map(Department::getMembers)
            .flatMap(List::stream)
            .distinct()
            .toList();
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

    private static Predicate<Person> nameContains(String query) {
        return person -> person.getNiceName().toLowerCase().contains(query.toLowerCase());
    }

    private Comparator<Department> departmentComparator() {
        return comparing(department -> department.getName().toLowerCase());
    }
}
