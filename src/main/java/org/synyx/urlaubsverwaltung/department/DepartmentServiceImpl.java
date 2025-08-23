package org.synyx.urlaubsverwaltung.department;

import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.search.SortComparator;

import java.time.Clock;
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
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
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
    private final PersonService personService;
    private final ApplicationService applicationService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Clock clock;

    DepartmentServiceImpl(
        DepartmentRepository departmentRepository,
        DepartmentMembershipService departmentMembershipService,
        PersonService personService,
        ApplicationService applicationService,
        ApplicationEventPublisher applicationEventPublisher,
        Clock clock
    ) {
        this.departmentRepository = departmentRepository;
        this.departmentMembershipService = departmentMembershipService;
        this.personService = personService;
        this.applicationService = applicationService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.clock = clock;
    }

    @Override
    public List<Person> getManagedMembersOfPerson(Person person, Year year) {

        final Map<PersonId, List<DepartmentMembership>> activeMembershipsOfYear = departmentMembershipService.getActiveMembershipsOfYear(year);
        final Set<DepartmentMembership> onlyMembers = extractMemberMemberships(activeMembershipsOfYear);

        if (onlyMembers.isEmpty()) {
            return List.of();
        }

        final Set<PersonId> managedPersonIds;

        if (person.hasRole(OFFICE) || person.hasRole(BOSS)) {
            // office or boss is allowed to manage all other persons
            managedPersonIds = onlyMembers.stream().map(DepartmentMembership::personId).collect(toSet());
        } else {
            // otherwise we have to collect departments where the person is a department head or second stage authority
            final Set<Long> managedDepartmentIds = activeMembershipsOfYear.get(person.getIdAsPersonId()).stream()
                .filter(DepartmentMembership::isManagementMembership)
                .map(DepartmentMembership::departmentId)
                .collect(toSet());
            managedPersonIds = onlyMembers.stream()
                .filter(m -> managedDepartmentIds.contains(m.departmentId())).map(DepartmentMembership::personId)
                .collect(toSet());
        }

        return personService.getAllPersonsByIds(managedPersonIds);
    }

    private Set<DepartmentMembership> extractMemberMemberships(Map<PersonId, List<DepartmentMembership>> membershipsByPersonId) {
        return membershipsByPersonId.values()
            .stream()
            .flatMap(Collection::stream)
            .filter(m -> m.membershipKind().equals(DepartmentMembershipKind.MEMBER))
            .collect(toSet());
    }

    @Override
    public Page<Person> getManagedMembersOfPerson(Person person, PageableSearchQuery personPageableSearchQuery) {
        final PersonId personId = person.getIdAsPersonId();
        return managedMembersOfPerson(personId, personPageableSearchQuery, not(Person::isInactive));
    }

    @Override
    public List<Person> getManagedActiveMembersOfPerson(Person person) {
        final PersonId personId = person.getIdAsPersonId();
        final List<DepartmentMembership> members = getManagedMemberMembershipsOfPerson(personId);
        return membershipsToPersons(members).stream().filter(Person::isActive).toList();
    }

    @Override
    public Page<Person> getManagedInactiveMembersOfPerson(Person person, PageableSearchQuery personPageableSearchQuery) {
        final PersonId personId = person.getIdAsPersonId();
        return managedMembersOfPerson(personId, personPageableSearchQuery, Person::isInactive);
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

    @Override
    public boolean departmentExists(Long departmentId) {
        return departmentRepository.existsById(departmentId);
    }

    @Override
    public Optional<Department> getDepartmentById(Long departmentId) {
        return departmentRepository.findById(departmentId)
            .map(entity -> {
                final DepartmentStaff staff = departmentMembershipService.getDepartmentStaff(entity.getId());
                final List<Person> persons = personService.getAllPersonsByIds(staff.allPersonIds());
                return mapToDepartment(entity, staff, persons);
            });
    }

    @Override
    public Optional<Department> getDepartmentByName(String departmentName) {
        return departmentRepository.findFirstByName(departmentName)
            .map(entity -> {
                final DepartmentStaff staff = departmentMembershipService.getDepartmentStaff(entity.getId());
                final List<Person> persons = personService.getAllPersonsByIds(staff.allPersonIds());
                return mapToDepartment(entity, staff, persons);
            });
    }

    @Override
    @Transactional
    public Department create(Department department) {

        final DepartmentEntity departmentEntity = mapToDepartmentEntity(department);
        departmentEntity.setCreatedAt(LocalDate.now(clock));
        departmentEntity.setLastModification(LocalDate.now(clock));

        final DepartmentEntity savedEntity = departmentRepository.save(departmentEntity);

        final DepartmentStaff staff = departmentMembershipService.createInitialMemberships(
            savedEntity.getId(),
            personIdsOfPersons(department.getMembers()),
            personIdsOfPersons(department.getDepartmentHeads()),
            personIdsOfPersons(department.getSecondStageAuthorities())
        );

        final List<Person> persons = personService.getAllPersonsByIds(staff.allPersonIds());
        final Department createdDepartment = mapToDepartment(savedEntity, staff, persons);

        LOG.info("Created department: {}", createdDepartment);

        return createdDepartment;
    }

    @Override
    @Transactional
    public Department update(Department department) {

        final DepartmentEntity currentDepartmentEntity = departmentRepository.findById(department.getId())
            .orElseThrow(() -> new IllegalStateException("cannot update department since it does not exists."));

        final DepartmentStaff currentStaff = departmentMembershipService.getDepartmentStaff(currentDepartmentEntity.getId());

        final Set<PersonId> oldAndNewPersonIds = Stream.concat(personIdsOfDepartment(department).stream(), currentStaff.allPersonIds().stream()).collect(toSet());
        final List<Person> persons = personService.getAllPersonsByIds(oldAndNewPersonIds);

        final DepartmentStaff updatedStaff = departmentMembershipService.updateDepartmentMemberships(
            department.getId(),
            currentStaff,
            department.getMembers().stream().map(Person::getIdAsPersonId).toList(),
            department.getDepartmentHeads().stream().map(Person::getIdAsPersonId).toList(),
            department.getSecondStageAuthorities().stream().map(Person::getIdAsPersonId).toList()
        );

        final DepartmentEntity departmentEntity = mapToDepartmentEntity(department);
        departmentEntity.setCreatedAt(currentDepartmentEntity.getCreatedAt());
        departmentEntity.setLastModification(LocalDate.now(clock));

        final DepartmentEntity updatedEntity = departmentRepository.save(departmentEntity);
        final Department updatedDepartment = mapToDepartment(updatedEntity, updatedStaff, persons);

        sendMemberLeftDepartmentEvent(updatedStaff, currentStaff);

        LOG.info("Updated department: {}", updatedDepartment);

        return updatedDepartment;
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
        final List<DepartmentEntity> entities = departmentRepository.findAll();
        return mapToDepartments(entities, departmentComparator());
    }

    @Override
    public List<Department> getAssignedDepartmentsOfMember(Person member) {
        final PersonId personId = member.getIdAsPersonId();
        return getDepartmentsOfPersonWithMembershipKind(personId, DepartmentMembershipKind.MEMBER);
    }

    @Override
    public List<Department> getManagedDepartmentsOfDepartmentHead(Person departmentHead) {
        final PersonId personId = departmentHead.getIdAsPersonId();
        return getDepartmentsOfPersonWithMembershipKind(personId, DepartmentMembershipKind.DEPARTMENT_HEAD);
    }

    @Override
    public List<Department> getManagedDepartmentsOfSecondStageAuthority(Person secondStageAuthority) {
        final PersonId personId = secondStageAuthority.getIdAsPersonId();
        return getDepartmentsOfPersonWithMembershipKind(personId, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY);
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
            final List<Person> colleagues = getMembersOfAssignedDepartments(person).stream().filter(not(isEqual(person))).toList();
            if (colleagues.isEmpty()) {
                colleaguesApplications = List.of();
            } else {
                colleaguesApplications = applicationService.getForStatesAndPerson(activeStatuses(), colleagues, startDate, endDate);
            }
        }

        return colleaguesApplications.stream()
            .sorted(comparing(Application::getStartDate))
            .toList();
    }

    @Override
    public List<Person> getMembersForDepartmentHead(Person departmentHead) {
        final PersonId personId = departmentHead.getIdAsPersonId();
        return getDepartmentsOfPersonWithMembershipKind(personId, DepartmentMembershipKind.DEPARTMENT_HEAD).stream()
            .map(Department::getMembers)
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

    @Override
    public List<Person> getMembersForSecondStageAuthority(Person secondStageAuthority) {
        final PersonId personId = secondStageAuthority.getIdAsPersonId();
        return getDepartmentsOfPersonWithMembershipKind(personId, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY).stream()
            .map(Department::getMembers)
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

    @Override
    public boolean isDepartmentHeadAllowedToManagePerson(Person departmentHead, Person person) {
        if (!departmentHead.hasRole(DEPARTMENT_HEAD)) {
            return false;
        }

        final PersonId departmentHeadId = departmentHead.getIdAsPersonId();
        final PersonId personId = person.getIdAsPersonId();

        final Map<PersonId, List<DepartmentMembership>> memberships =
            departmentMembershipService.getActiveMembershipsOfPersons(List.of(departmentHeadId, personId));

        final Set<Long> departmentHeadDepartmentIds = memberships.get(departmentHeadId).stream()
            .filter(m -> m.membershipKind().equals(DepartmentMembershipKind.DEPARTMENT_HEAD))
            .map(DepartmentMembership::departmentId)
            .collect(toSet());

        final Set<Long> personMemberDepartmentIds = memberships.get(personId).stream()
            .filter(m -> m.membershipKind().equals(DepartmentMembershipKind.MEMBER))
            .map(DepartmentMembership::departmentId)
            .collect(toSet());

        return departmentHeadDepartmentIds.stream().anyMatch(personMemberDepartmentIds::contains);
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
        if (isOwnData) {
            return true;
        }

        final boolean isBossOrOffice = signedInUser.hasRole(OFFICE) || signedInUser.hasRole(BOSS);
        if (isBossOrOffice) {
            return true;
        }

        final boolean isDepartmentHeadAllowed = isDepartmentHeadAllowedToAccessPersonData(signedInUser, person);
        if (isDepartmentHeadAllowed) {
            return true;
        }

        return isSecondStageAuthorityAllowedToAccessPersonData(signedInUser, person);
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

        final List<PersonId> personIds = personIdsOfPersons(persons);
        final Map<PersonId, List<DepartmentMembership>> membershipsByPersonId = departmentMembershipService.getActiveMembershipsOfPersons(personIds);

        final Set<Long> departmentIds = membershipsByPersonId.values().stream()
            .flatMap(Collection::stream)
            .filter(DepartmentMembership::isMemberMembership)
            .map(DepartmentMembership::departmentId)
            .collect(toSet());

        final List<DepartmentEntity> departmentEntities = departmentRepository.findAllById(departmentIds);
        final Map<Long, String> departmentNameById = departmentEntities.stream().collect(toMap(DepartmentEntity::getId, DepartmentEntity::getName));

        final Map<Long, DepartmentStaff> staffByDepartmentId = departmentMembershipService.getDepartmentStaff(departmentIds);

        final Map<PersonId, Set<String>> departmentsByPerson = new HashMap<>();

        staffByDepartmentId.forEach((departmentId, staff) -> {
            for (DepartmentMembership member : staff.members()) {
                final Set<String> departmentNames = departmentsByPerson.computeIfAbsent(member.personId(), (key) -> new HashSet<>());
                if (departmentNameById.containsKey(departmentId)) {
                    departmentNames.add(departmentNameById.get(departmentId));
                }
            }
        });

        return departmentsByPerson.entrySet().stream()
            .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().stream().sorted().toList()));
    }

    @Override
    public boolean hasDepartmentMatch(Person person, Person otherPerson) {

        final PersonId personId = person.getIdAsPersonId();
        final PersonId otherPersonId = otherPerson.getIdAsPersonId();

        final Map<PersonId, List<DepartmentMembership>> membershipsByPersonId =
            departmentMembershipService.getActiveMembershipsOfPersons(List.of(personId, otherPersonId));

        final List<Long> personDepartmentIds = membershipsByPersonId.get(personId).stream().map(DepartmentMembership::departmentId).toList();
        final List<Long> otherPersonDepartmentIds = membershipsByPersonId.get(otherPersonId).stream().map(DepartmentMembership::departmentId).toList();

        return personDepartmentIds.stream().anyMatch(otherPersonDepartmentIds::contains);
    }

    private static DepartmentStaff departmentToStaff(Department department) {

        final List<PersonId> newMemberIds = department.getMembers().stream().map(Person::getId).map(PersonId::new).toList();
        final List<PersonId> newDepartmentHeadIds = department.getDepartmentHeads().stream().map(Person::getId).map(PersonId::new).toList();
        final List<PersonId> newSecondStageIds = department.getSecondStageAuthorities().stream().map(Person::getId).map(PersonId::new).toList();

        return new DepartmentStaff(department.getId(),
            newMemberIds.stream().map(id -> new DepartmentMembership(id, department.getId(), DepartmentMembershipKind.MEMBER, null)).toList(),
            newDepartmentHeadIds.stream().map(id -> new DepartmentMembership(id, department.getId(), DepartmentMembershipKind.DEPARTMENT_HEAD, null)).toList(),
            newSecondStageIds.stream().map(id -> new DepartmentMembership(id, department.getId(), DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, null)).toList()
        );
    }

    private List<DepartmentMembership> getManagedMemberMembershipsOfPerson(PersonId personId) {

        final List<DepartmentMembership> personsMemberships = departmentMembershipService.getActiveMemberships(personId);

        final Set<Long> managedDepartmentIds = personsMemberships.stream()
            .filter(DepartmentMembership::isManagementMembership)
            .map(DepartmentMembership::departmentId)
            .collect(toSet());

        return departmentMembershipService.getDepartmentStaff(managedDepartmentIds).values().stream()
            .map(DepartmentStaff::members)
            .flatMap(Collection::stream)
            .toList();
    }

    private static List<PersonId> personIdsOfPersons(List<Person> persons) {
        return persons.stream().map(Person::getId).map(PersonId::new).toList();
    }

    private static List<PersonId> personIdsOfDepartment(Department department) {
        final List<Long> memberIds = department.getMembers().stream().map(Person::getId).toList();
        final List<Long> departmentHeadIds = department.getDepartmentHeads().stream().map(Person::getId).toList();
        final List<Long> secondStageAuthorityIds = department.getSecondStageAuthorities().stream().map(Person::getId).toList();
        return Stream.concat(Stream.concat(memberIds.stream(), departmentHeadIds.stream()), secondStageAuthorityIds.stream()).map(PersonId::new).distinct().toList();
    }

    private Predicate<Person> isNotSecondStageIn(Department department) {
        return person -> !department.getSecondStageAuthorities().contains(person);
    }

    private Department mapToDepartment(DepartmentEntity departmentEntity, DepartmentStaff staff, List<Person> persons) {

        final Map<Long, Person> personById = persons.stream().collect(toMap(Person::getId, identity()));

        final List<Person> members = membershipsToPersons(staff.members(), personById);
        final List<Person> departmentHeads = membershipsToPersons(staff.departmentHeads(), personById);
        final List<Person> secondStageAuthorities = membershipsToPersons(staff.secondStageAuthorities(), personById);

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

    private List<Department> mapToDepartments(List<DepartmentEntity> entities, Comparator<Department> comparator) {
        if (entities.isEmpty()) {
            return List.of();
        }

        final Set<Long> departmentIds = entities.stream().map(DepartmentEntity::getId).collect(toSet());
        final Map<Long, DepartmentStaff> staffByDepartmentId = departmentMembershipService.getDepartmentStaff(departmentIds);

        final Set<PersonId> personIds = staffByDepartmentId.values().stream().map(DepartmentStaff::allPersonIds).flatMap(Collection::stream).collect(toSet());
        final List<Person> persons = personService.getAllPersonsByIds(personIds);

        return entities.stream()
            .map(entity -> mapToDepartment(entity, staffByDepartmentId.get(entity.getId()), persons))
            .sorted(comparator)
            .toList();
    }

    private List<Department> getDepartmentsOfPersonWithMembershipKind(PersonId personId, DepartmentMembershipKind membershipKind) {

        final List<DepartmentMembership> memberships = departmentMembershipService.getActiveMemberships(personId);

        final Set<Long> departmentIds = memberships.stream()
            .filter(m -> m.membershipKind().equals(membershipKind))
            .map(DepartmentMembership::departmentId)
            .collect(toSet());

        if (departmentIds.isEmpty()) {
            return List.of();
        }

        final List<DepartmentEntity> entities = departmentRepository.findAllById(departmentIds);
        return mapToDepartments(entities, departmentComparator());
    }

    private List<Person> membershipsToPersons(Collection<DepartmentMembership> memberships) {

        final Set<PersonId> personIds = memberships.stream()
            .map(DepartmentMembership::personId)
            .collect(toSet());

        return personService.getAllPersonsByIds(personIds);
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

    private DepartmentEntity mapToDepartmentEntity(Department department) {

        final DepartmentEntity departmentEntity = new DepartmentEntity();

        departmentEntity.setId(department.getId());
        departmentEntity.setName(department.getName());
        departmentEntity.setDescription(department.getDescription());
        departmentEntity.setTwoStageApproval(department.isTwoStageApproval());
        departmentEntity.setLastModification(department.getLastModification());

        return departmentEntity;
    }

    private Page<Person> managedMembersOfPerson(PersonId personId, PageableSearchQuery personPageableSearchQuery, Predicate<Person> predicate) {

        final Pageable pageable = personPageableSearchQuery.getPageable();
        final List<DepartmentMembership> memberships = getManagedMemberMembershipsOfPerson(personId);

        final List<Person> managedMembers = membershipsToPersons(memberships)
            .stream()
            .filter(nameContains(personPageableSearchQuery.getQuery()).and(predicate))
            .sorted(new SortComparator<>(Person.class, pageable.getSort()))
            .toList();

        final List<Person> content = managedMembers.stream()
            .skip((long) pageable.getPageNumber() * pageable.getPageSize())
            .limit(pageable.getPageSize())
            .toList();

        return new PageImpl<>(content, pageable, managedMembers.size());
    }

    private Page<Person> managedMembersOfPersonAndDepartment(Person person, Long departmentId, PageableSearchQuery pageableSearchQuery, Predicate<Person> filter) {

        final DepartmentStaff staff = departmentMembershipService.getDepartmentStaff(departmentId);
        if (!doesPersonManageDepartment(person, staff)) {
            return Page.empty();
        }

        final Set<PersonId> memberPersonIds = staff.members().stream()
            .filter(m -> m.membershipKind().equals(DepartmentMembershipKind.MEMBER))
            .map(DepartmentMembership::personId)
            .collect(toSet());

        final List<Person> members = personService.getAllPersonsByIds(memberPersonIds);

        final Pageable pageable = pageableSearchQuery.getPageable();
        final List<Person> content = members.stream()
            .filter(filter)
            .sorted(new SortComparator<>(Person.class, pageable.getSort()))
            .skip((long) pageable.getPageNumber() * pageable.getPageSize())
            .limit(pageable.getPageSize())
            .toList();

        return new PageImpl<>(content, pageable, members.size());
    }

    private static boolean doesPersonManageDepartment(Person person, DepartmentStaff staff) {
        if (person.hasRole(BOSS) || person.hasRole(OFFICE)) {
            return true;
        }

        final PersonId personId = person.getIdAsPersonId();

        final boolean isDepartmentHead = person.hasRole(DEPARTMENT_HEAD) && staff.hasDepartmentHead(personId);
        final boolean isSecondStage = person.hasRole(SECOND_STAGE_AUTHORITY) && staff.hasSecondStageAuthority(personId);

        return isDepartmentHead || isSecondStage;
    }

    private void sendMemberLeftDepartmentEvent(DepartmentStaff newStaff, DepartmentStaff previousStaff) {
        final Long departmentId = newStaff.departmentId();

        previousStaff.members().stream()
            .filter(newStaff.members()::contains)
            .map(DepartmentMembership::personId)
            .forEach(personId -> applicationEventPublisher.publishEvent(new PersonLeftDepartmentEvent(this, personId.value(), departmentId)));

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
