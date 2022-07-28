package org.synyx.urlaubsverwaltung.department;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.Month.DECEMBER;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createDepartment;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    private DepartmentServiceImpl sut;

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private final Clock clock = Clock.fixed(Instant.now(), UTC);

    @BeforeEach
    void setUp() {
        sut = new DepartmentServiceImpl(departmentRepository, applicationService, applicationEventPublisher, clock);
    }

    @Test
    void ensureNewDepartmentCreation() {
        final Department department = new Department();
        department.setName("department");

        final DepartmentEntity savedDepartmentEntity = new DepartmentEntity();
        savedDepartmentEntity.setId(42);
        savedDepartmentEntity.setName("department");

        when(departmentRepository.save(any())).thenReturn(savedDepartmentEntity);

        final Department createdDepartment = sut.create(department);

        assertThat(createdDepartment).isNotSameAs(department);
        assertThat(createdDepartment.getId()).isEqualTo(42);
        assertThat(createdDepartment.getName()).isEqualTo("department");
        assertThat(createdDepartment.getLastModification()).isEqualTo(LocalDate.now(clock));
    }

    @Test
    void ensureCreatedDateIsSetForNewDepartment() {

        final Department department = new Department();
        department.setName("department");

        when(departmentRepository.save(any())).thenReturn(new DepartmentEntity());

        sut.create(department);

        final ArgumentCaptor<DepartmentEntity> departmentEntityArgumentCaptor = ArgumentCaptor.forClass(DepartmentEntity.class);
        verify(departmentRepository).save(departmentEntityArgumentCaptor.capture());

        final DepartmentEntity savedDepartmentEntity = departmentEntityArgumentCaptor.getValue();
        assertThat(savedDepartmentEntity.getCreatedAt()).isEqualTo(LocalDate.now(clock));
    }

    @Test
    void ensureCallDepartmentRepositoryFindById() {

        sut.getDepartmentById(42);
        verify(departmentRepository).findById(42);
    }

    @Test
    void ensureUpdateDepartmentFailsWhenDepartmentDoesNotExistYet() {
        final Department department = new Department();
        department.setId(42);
        department.setName("department");

        when(departmentRepository.findById(42)).thenReturn(Optional.empty());

        assertThatIllegalStateException()
            .isThrownBy(() -> sut.update(department));
    }

    @Test
    void ensureUpdateCallDepartmentDAOUpdate() {

        final Department department = new Department();
        department.setId(42);
        department.setName("department");

        when(departmentRepository.findById(42)).thenReturn(Optional.of(new DepartmentEntity()));

        final DepartmentEntity updatedDepartmentEntity = new DepartmentEntity();
        updatedDepartmentEntity.setId(42);
        updatedDepartmentEntity.setName("department");
        when(departmentRepository.save(any())).thenReturn(updatedDepartmentEntity);

        final Department updatedDepartment = sut.update(department);

        assertThat(updatedDepartment).isNotSameAs(department);
        assertThat(updatedDepartment.getName()).isEqualTo("department");

        final ArgumentCaptor<DepartmentEntity> departmentEntityArgumentCaptor = ArgumentCaptor.forClass(DepartmentEntity.class);
        verify(departmentRepository).save(departmentEntityArgumentCaptor.capture());

        final DepartmentEntity departmentEntityToUpdate = departmentEntityArgumentCaptor.getValue();
        assertThat(departmentEntityToUpdate.getId()).isEqualTo(42);
        assertThat(departmentEntityToUpdate.getName()).isEqualTo("department");
    }

    @Test
    void ensureUpdateDoesNotChangeTheCreatedAtDate() {

        final Department department = new Department();
        department.setId(1);
        department.setName("department");

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setCreatedAt(LocalDate.of(2020, DECEMBER, 4));
        departmentEntity.setLastModification(LocalDate.of(2020, DECEMBER, 4));

        when(departmentRepository.findById(1)).thenReturn(Optional.of(departmentEntity));
        when(departmentRepository.save(any())).thenReturn(new DepartmentEntity());

        sut.update(department);

        final ArgumentCaptor<DepartmentEntity> departmentEntityArgumentCaptor = ArgumentCaptor.forClass(DepartmentEntity.class);
        verify(departmentRepository).save(departmentEntityArgumentCaptor.capture());

        final DepartmentEntity savedDepartmentEntity = departmentEntityArgumentCaptor.getValue();
        assertThat(savedDepartmentEntity.getCreatedAt()).isEqualTo(LocalDate.of(2020, DECEMBER, 4));
        assertThat(savedDepartmentEntity.getLastModification()).isEqualTo(LocalDate.now(clock));
    }

    @Test
    void ensureAddingMembersToDepartmentAlsoSetsAccessionDate() {
        final Person existingPerson = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");
        final Person person = new Person("batman", "Wayne", "Bruce", "wayne@example.org");

        final Department department = new Department();
        department.setId(42);
        department.setName("department");
        department.setMembers(List.of(existingPerson, person));

        final DepartmentMemberEmbeddable existingPersonMember = new DepartmentMemberEmbeddable();
        existingPersonMember.setPerson(existingPerson);
        existingPersonMember.setAccessionDate(Instant.now(clock).minus(1, DAYS));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setCreatedAt(LocalDate.of(2020, DECEMBER, 4));
        departmentEntity.setLastModification(LocalDate.of(2020, DECEMBER, 4));
        departmentEntity.setMembers(List.of(existingPersonMember));

        when(departmentRepository.findById(42)).thenReturn(Optional.of(departmentEntity));
        when(departmentRepository.save(any())).then(returnsFirstArg());

        final Department updatedDepartment = sut.update(department);
        assertThat(updatedDepartment.getMembers()).contains(existingPerson, person);

        final ArgumentCaptor<DepartmentEntity> departmentEntityArgumentCaptor = ArgumentCaptor.forClass(DepartmentEntity.class);
        verify(departmentRepository).save(departmentEntityArgumentCaptor.capture());
        final DepartmentEntity savedDepartmentEntity = departmentEntityArgumentCaptor.getValue();

        assertThat(savedDepartmentEntity.getMembers().get(0).getPerson()).isEqualTo(existingPerson);
        assertThat(savedDepartmentEntity.getMembers().get(0).getAccessionDate()).isEqualTo(Instant.now(clock).minus(1, DAYS));
        assertThat(savedDepartmentEntity.getMembers().get(1).getPerson()).isEqualTo(person);
        assertThat(savedDepartmentEntity.getMembers().get(1).getAccessionDate()).isEqualTo(Instant.now(clock));
    }

    @Test
    void ensureRemovingMembersInDepartmentAlsoSentDepartmentLeftEvent() {
        final Person existingPerson = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");
        final Person personThatWillLeft = new Person("batman", "Wayne", "Bruce", "wayne@example.org");
        personThatWillLeft.setId(1);

        final DepartmentMemberEmbeddable existingPersonMember = new DepartmentMemberEmbeddable();
        existingPersonMember.setPerson(existingPerson);
        existingPersonMember.setAccessionDate(Instant.now(clock).minus(1, DAYS));

        final DepartmentMemberEmbeddable willLeftPersonMember = new DepartmentMemberEmbeddable();
        willLeftPersonMember.setPerson(personThatWillLeft);
        willLeftPersonMember.setAccessionDate(Instant.now(clock).minus(1, DAYS));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setCreatedAt(LocalDate.of(2020, DECEMBER, 4));
        departmentEntity.setLastModification(LocalDate.of(2020, DECEMBER, 4));
        departmentEntity.setMembers(List.of(existingPersonMember, willLeftPersonMember));

        when(departmentRepository.findById(42)).thenReturn(Optional.of(departmentEntity));
        when(departmentRepository.save(any())).then(returnsFirstArg());

        final Department department = new Department();
        department.setId(42);
        department.setName("department");
        department.setMembers(List.of(existingPerson));

        final Department updatedDepartment = sut.update(department);
        assertThat(updatedDepartment.getMembers()).contains(existingPerson);

        final ArgumentCaptor<PersonLeftDepartmentEvent> personLeftDepartmentEventCaptor = ArgumentCaptor.forClass(PersonLeftDepartmentEvent.class);
        verify(applicationEventPublisher).publishEvent(personLeftDepartmentEventCaptor.capture());
        final PersonLeftDepartmentEvent departmentLeftEvent = personLeftDepartmentEventCaptor.getValue();

        assertThat(departmentLeftEvent.getDepartmentId()).isEqualTo(42);
        assertThat(departmentLeftEvent.getPersonId()).isEqualTo(1);
    }

    @Test
    void ensureGetAllCallDepartmentDAOFindAll() {

        final DepartmentEntity departmentEntityA = new DepartmentEntity();
        departmentEntityA.setId(1);
        departmentEntityA.setName("Department A");
        final DepartmentEntity departmentEntityB = new DepartmentEntity();
        departmentEntityB.setId(2);
        departmentEntityB.setName("Department B");

        final Department departmentA = new Department();
        departmentA.setId(1);
        final Department departmentB = new Department();
        departmentB.setId(2);

        when(departmentRepository.findAll()).thenReturn(List.of(departmentEntityA, departmentEntityB));

        final List<Department> allDepartments = sut.getAllDepartments();
        assertThat(allDepartments)
            .hasSize(2)
            .containsExactly(departmentA, departmentB);
    }

    @Test
    void ensureGetAllDepartmentSorted() {

        final DepartmentEntity departmentEntityA = new DepartmentEntity();
        departmentEntityA.setId(1);
        departmentEntityA.setName("Department A");
        final DepartmentEntity departmentEntityB = new DepartmentEntity();
        departmentEntityB.setId(2);
        departmentEntityB.setName("Department B");

        final Department departmentA = new Department();
        departmentA.setId(1);
        final Department departmentB = new Department();
        departmentB.setId(2);

        when(departmentRepository.findAll()).thenReturn(List.of(departmentEntityB, departmentEntityA));

        final List<Department> allDepartments = sut.getAllDepartments();
        assertThat(allDepartments)
            .hasSize(2)
            .containsExactly(departmentA, departmentB);
    }

    @Test
    void ensureGetManagedDepartmentsOfDepartmentHeadCallCorrectDAOMethod() {

        final Person person = new Person();

        sut.getManagedDepartmentsOfDepartmentHead(person);

        verify(departmentRepository).findByDepartmentHeads(person);
    }

    @Test
    void ensureGetManagedDepartmentsOfSecondStageAuthorityCallCorrectDAOMethod() {

        final Person person = new Person();

        sut.getManagedDepartmentsOfSecondStageAuthority(person);

        verify(departmentRepository).findBySecondStageAuthorities(person);
    }

    @Test
    void ensureGetAssignedDepartmentsOfMemberCallCorrectDAOMethod() {

        final Person person = new Person();

        sut.getAssignedDepartmentsOfMember(person);

        verify(departmentRepository).findByMembersPerson(person);
    }

    @Test
    void ensureDeletionIsNotExecutedIfDepartmentWithGivenIDDoesNotExist() {

        sut.delete(0);

        verify(departmentRepository, never()).deleteById(anyInt());
    }

    @Test
    void ensureDeleteCallFindOneAndDelete() {

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setName("department");

        when(departmentRepository.existsById(0)).thenReturn(true);

        sut.delete(0);

        verify(departmentRepository).existsById(0);
        verify(departmentRepository).deleteById(0);
    }

    @Test
    void ensureSetLastModificationOnUpdate() {

        final Department department = new Department();

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        final LocalDate expectedModificationDate = LocalDate.of(2020, Month.JANUARY, 1);
        departmentEntity.setLastModification(expectedModificationDate);

        when(departmentRepository.findById(any())).thenReturn(Optional.of(new DepartmentEntity()));
        when(departmentRepository.save(any())).thenReturn(departmentEntity);

        final Department updatedDepartment = sut.update(department);

        assertThat(department.getLastModification()).isEqualTo(LocalDate.now(clock)); // department constructor currently sets the modification date
        assertThat(updatedDepartment.getLastModification()).isEqualTo(expectedModificationDate);
    }

    @Test
    void getMembersForDepartmentHead() {

        final DepartmentMemberEmbeddable departmentHeadMember = departmentMemberEmbeddable("departmentHead", "Department", "Head", "head.department@example.org");
        final DepartmentMemberEmbeddable marleneMember = departmentMemberEmbeddable("muster", "Muster", "Marlene", "marlene.muster@example.org");
        final DepartmentMemberEmbeddable maxMember = departmentMemberEmbeddable("admin2", "Muster", "Max", "max.muster@example.org");

        final DepartmentEntity departmentOne = new DepartmentEntity();
        departmentOne.setName("departmentOne");
        departmentOne.setMembers(List.of(marleneMember, maxMember, departmentHeadMember));

        final DepartmentMemberEmbeddable tomMember = departmentMemberEmbeddable("tom", "Tom", "Baer", "tom.baer@example.org");
        final DepartmentEntity departmentTwo = new DepartmentEntity();
        departmentTwo.setName("departmentTwo");
        departmentTwo.setMembers(List.of(tomMember));

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(departmentRepository.findByDepartmentHeads(departmentHead)).thenReturn(List.of(departmentOne, departmentTwo));

        final List<Person> members = sut.getMembersForDepartmentHead(departmentHead);
        assertThat(members).containsOnly(marleneMember.getPerson(), tomMember.getPerson(), departmentHeadMember.getPerson(), maxMember.getPerson());
    }

    @Test
    void getMembersForDepartmentHeadDistinct() {

        final DepartmentMemberEmbeddable member = departmentMemberEmbeddable("admin2", "Muster", "Max", "max.muster@example.org");

        final DepartmentEntity departmentOne = new DepartmentEntity();
        departmentOne.setName("departmentOne");
        departmentOne.setMembers(List.of(member));

        final DepartmentEntity departmentTwo = new DepartmentEntity();
        departmentTwo.setName("departmentTwo");
        departmentTwo.setMembers(List.of(member));

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(departmentRepository.findByDepartmentHeads(departmentHead)).thenReturn(List.of(departmentOne, departmentTwo));

        final List<Person> members = sut.getMembersForDepartmentHead(departmentHead);
        assertThat(members).containsOnly(member.getPerson());
    }

    @Test
    void getMembersForSecondStageAuthority() {

        final DepartmentMemberEmbeddable departmentHeadMember = departmentMemberEmbeddable("departmentHead", "Department", "Head", "head.department@example.org");
        final DepartmentMemberEmbeddable marleneMember = departmentMemberEmbeddable("muster", "Muster", "Marlene", "marlene.muster@example.org");
        final DepartmentMemberEmbeddable maxMember = departmentMemberEmbeddable("admin2", "Muster", "Max", "max.muster@example.org");

        final DepartmentEntity departmentOne = new DepartmentEntity();
        departmentOne.setName("departmentOne");
        departmentOne.setMembers(List.of(marleneMember, maxMember, departmentHeadMember));

        final DepartmentMemberEmbeddable tomMember = departmentMemberEmbeddable("tom", "Tom", "Baer", "tom.baer@example.org");
        final DepartmentEntity departmentTwo = new DepartmentEntity();
        departmentTwo.setName("departmentTwo");
        departmentTwo.setMembers(List.of(tomMember));

        final Person secondStageAuthority = new Person();
        secondStageAuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(departmentRepository.findBySecondStageAuthorities(secondStageAuthority)).thenReturn(List.of(departmentOne, departmentTwo));

        final List<Person> members = sut.getMembersForSecondStageAuthority(secondStageAuthority);
        assertThat(members).containsOnly(marleneMember.getPerson(), tomMember.getPerson(), departmentHeadMember.getPerson(), maxMember.getPerson());
    }

    @Test
    void getMembersForSecondStageAuthorityDistinct() {

        final DepartmentMemberEmbeddable member = departmentMemberEmbeddable("admin2", "Muster", "Max", "max.muster@example.org");

        final DepartmentEntity departmentOne = new DepartmentEntity();
        departmentOne.setName("departmentOne");
        departmentOne.setMembers(List.of(member));

        final DepartmentEntity departmentTwo = new DepartmentEntity();
        departmentTwo.setName("departmentTwo");
        departmentTwo.setMembers(List.of(member));

        final Person secondStageAuthority = new Person();
        secondStageAuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(departmentRepository.findBySecondStageAuthorities(secondStageAuthority)).thenReturn(List.of(departmentOne, departmentTwo));

        final List<Person> members = sut.getMembersForSecondStageAuthority(secondStageAuthority);
        assertThat(members).containsOnly(member.getPerson());
    }

    @Test
    void ensureReturnsTrueIfIsDepartmentHeadOfTheGivenPerson() {

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final DepartmentMemberEmbeddable departmentHeadMember = departmentMemberEmbeddable(departmentHead);

        final Person marlenePerson = new Person("muster", "Muster", "Marlene", "marlene.muster@example.org");
        final DepartmentMemberEmbeddable marleneMember = departmentMemberEmbeddable(marlenePerson);
        final DepartmentMemberEmbeddable maxMember = departmentMemberEmbeddable("admin2", "Muster", "Max", "max.muster@example.org");

        final DepartmentEntity admins = new DepartmentEntity();
        admins.setName("admins");
        admins.setMembers(List.of(marleneMember, maxMember, departmentHeadMember));

        when(departmentRepository.findByDepartmentHeads(departmentHead)).thenReturn(List.of(admins));

        boolean isDepartmentHead = sut.isDepartmentHeadAllowedToManagePerson(departmentHead, marlenePerson);
        assertThat(isDepartmentHead).isTrue();
    }

    @Test
    void ensureReturnsFalseIfIsNotDepartmentHeadOfTheGivenPerson() {

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        final DepartmentMemberEmbeddable departmentHeadMember = departmentMemberEmbeddable(departmentHead);

        final DepartmentMemberEmbeddable marleneMember = departmentMemberEmbeddable("admin1", "Muster", "Marlene", "marlene.muster@example.org");
        final DepartmentMemberEmbeddable maxMember = departmentMemberEmbeddable("admin2", "Muster", "Max", "max.muster@example.org");

        final DepartmentEntity admins = new DepartmentEntity();
        admins.setName("admins");
        admins.setMembers(List.of(marleneMember, maxMember, departmentHeadMember));

        Person marketing1 = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(departmentRepository.findByDepartmentHeads(departmentHead))
            .thenReturn(List.of(admins));

        boolean isDepartmentHead = sut.isDepartmentHeadAllowedToManagePerson(departmentHead, marketing1);
        assertThat(isDepartmentHead).isFalse();
    }

    @Test
    void ensureReturnsFalseIfIsInTheSameDepartmentButHasNotDepartmentHeadRole() {

        final Person noDepartmentHead = new Person();
        noDepartmentHead.setPermissions(List.of(USER));

        Person admin1 = new Person("muster", "Muster", "Marlene", "muster@example.org");
        Person admin2 = new Person("muster", "Muster", "Marlene", "muster@example.org");

        Department admins = createDepartment("admins");
        admins.setMembers(List.of(admin1, admin2, noDepartmentHead));

        boolean isDepartmentHead = sut.isDepartmentHeadAllowedToManagePerson(noDepartmentHead, admin1);
        assertThat(isDepartmentHead).isFalse();
    }

    @Test
    void ensureReturnsEmptyListOfDepartmentApplicationsIfPersonIsNotAssignedToAnyDepartment() {

        final Person person = new Person();
        person.setPermissions(List.of(USER));

        final LocalDate date = LocalDate.now(UTC);

        when(departmentRepository.findByMembersPerson(person)).thenReturn(emptyList());

        List<Application> applications = sut.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, date, date);
        assertThat(applications).isEmpty();

        verify(departmentRepository).findByMembersPerson(person);
        verifyNoInteractions(applicationService);
    }

    @Test
    void ensureReturnsEmptyListOfDepartmentApplicationsIfNoMatchingApplicationsForLeave() {

        final Person person = new Person();
        person.setPermissions(List.of(USER));

        final DepartmentMemberEmbeddable personMember = departmentMemberEmbeddable(person);

        final LocalDate date = LocalDate.now(UTC);

        final Person admin1 = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person admin2 = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final DepartmentMemberEmbeddable admin1Member = departmentMemberEmbeddable(admin1);
        final DepartmentMemberEmbeddable admin2Member = departmentMemberEmbeddable(admin2);

        final Person marketing1Person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person marketing2Person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person marketing3Person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final DepartmentMemberEmbeddable marketing1Member = departmentMemberEmbeddable(marketing1Person);
        final DepartmentMemberEmbeddable marketing2Member = departmentMemberEmbeddable(marketing2Person);
        final DepartmentMemberEmbeddable marketing3Member = departmentMemberEmbeddable(marketing3Person);

        final DepartmentEntity admins = new DepartmentEntity();
        admins.setName("admins");
        admins.setMembers(List.of(admin1Member, admin2Member, personMember));

        final DepartmentEntity marketing = new DepartmentEntity();
        marketing.setName("marketing");
        marketing.setMembers(List.of(marketing1Member, marketing2Member, marketing3Member, personMember));

        when(departmentRepository.findByMembersPerson(person)).thenReturn(List.of(admins, marketing));
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(emptyList());

        final List<Application> applications = sut.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, date, date);
        assertThat(applications).isEmpty();

        // Ensure fetches departments of person
        verify(departmentRepository).findByMembersPerson(person);

        // Ensure fetches applications for leave for every department member
        verify(applicationService).getApplicationsForACertainPeriodAndPerson(date, date, admin1);
        verify(applicationService).getApplicationsForACertainPeriodAndPerson(date, date, admin2);
        verify(applicationService).getApplicationsForACertainPeriodAndPerson(date, date, marketing1Person);
        verify(applicationService).getApplicationsForACertainPeriodAndPerson(date, date, marketing2Person);
        verify(applicationService).getApplicationsForACertainPeriodAndPerson(date, date, marketing3Person);

        // Ensure does not fetch applications for leave for the given person
        verify(applicationService, never()).getApplicationsForACertainPeriodAndPerson(date, date, person);
    }


    @Test
    void ensureReturnsOnlyWaitingAndAllowedAndCancellationRequestDepartmentApplicationsForLeave() {

        final Person person = new Person();
        person.setPermissions(List.of(USER));

        final DepartmentMemberEmbeddable personMember = departmentMemberEmbeddable(person);

        final LocalDate date = LocalDate.now(UTC);

        final Person admin1 = new Person("shane", "shane", "shane", "shane@example.org");
        final Person marketing1 = new Person("carl", "carl", "carl", "carl@example.org");

        final DepartmentMemberEmbeddable admin1Member = departmentMemberEmbeddable(admin1);
        final DepartmentMemberEmbeddable marketing1Member = departmentMemberEmbeddable(marketing1);

        final DepartmentEntity admins = new DepartmentEntity();
        admins.setName("admins");
        admins.setMembers(List.of(admin1Member, personMember));

        final DepartmentEntity marketing = new DepartmentEntity();
        marketing.setName("marketing");
        marketing.setMembers(List.of(marketing1Member, personMember));

        final Application waitingApplication = new Application();
        waitingApplication.setStatus(TEMPORARY_ALLOWED);

        final Application allowedApplication = new Application();
        allowedApplication.setStatus(ALLOWED);

        final Application cancellationRequestApplication = new Application();
        cancellationRequestApplication.setStatus(ALLOWED_CANCELLATION_REQUESTED);

        final Application otherApplication = new Application();
        otherApplication.setStatus(REJECTED);

        when(departmentRepository.findByMembersPerson(person)).thenReturn(List.of(admins, marketing));

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), eq(admin1)))
            .thenReturn(List.of(waitingApplication, otherApplication));

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), eq(marketing1)))
            .thenReturn(List.of(allowedApplication, cancellationRequestApplication));

        final List<Application> applications = sut.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, date, date);
        assertThat(applications)
            .hasSize(3)
            .contains(waitingApplication, allowedApplication, cancellationRequestApplication)
            .doesNotContain(otherApplication);
    }

    @Test
    void ensureSignedInOfficeUserCanAccessPersonData() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        person.setPermissions(List.of(USER));

        final Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setId(2);
        office.setPermissions(List.of(USER, OFFICE));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(office, person);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureSignedInBossUserCanAccessPersonData() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        person.setPermissions(List.of(USER));

        final Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setId(2);
        boss.setPermissions(List.of(USER, BOSS));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(boss, person);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureSignedInDepartmentHeadOfPersonCanAccessPersonData() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        person.setPermissions(List.of(USER));

        final DepartmentMemberEmbeddable personMember = departmentMemberEmbeddable(person);

        final Person departmentHead = new Person("muster", "Muster", "Marlene", "muster@example.org");
        departmentHead.setId(2);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final DepartmentMemberEmbeddable departmentHeadMember = departmentMemberEmbeddable(departmentHead);

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setName("dep");
        departmentEntity.setMembers(List.of(personMember, departmentHeadMember));

        when(departmentRepository.findByDepartmentHeads(departmentHead))
            .thenReturn(List.of(departmentEntity));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(departmentHead, person);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureSignedInDepartmentHeadThatIsNotDepartmentHeadOfPersonCanNotAccessPersonData() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        person.setPermissions(List.of(USER));

        final Person departmentHead = new Person("muster", "Muster", "Marlene", "muster@example.org");
        departmentHead.setId(2);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final DepartmentMemberEmbeddable departmentHeadMember = departmentMemberEmbeddable(departmentHead);

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setName("dep");
        departmentEntity.setMembers(List.of(departmentHeadMember));

        when(departmentRepository.findByDepartmentHeads(departmentHead))
            .thenReturn(List.of(departmentEntity));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(departmentHead, person);
        assertThat(isAllowed).isFalse();
    }

    @Test
    void ensureSignedInDepartmentHeadCanNotAccessSecondStageAuthorityPersonData() {

        final Person secondStageAuthority = new Person("muster", "Muster", "Marlene", "muster@example.org");
        secondStageAuthority.setId(1);
        secondStageAuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final DepartmentMemberEmbeddable secondStageAuthorityMember = departmentMemberEmbeddable(secondStageAuthority);

        final Person departmentHead = new Person("muster", "Muster", "Marlene", "muster@example.org");
        departmentHead.setId(2);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final DepartmentMemberEmbeddable departmentHeadMember = departmentMemberEmbeddable(departmentHead);

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setName("dep");
        departmentEntity.setMembers(List.of(secondStageAuthorityMember, departmentHeadMember));
        departmentEntity.setSecondStageAuthorities(List.of(secondStageAuthority));

        when(departmentRepository.findByDepartmentHeads(departmentHead)).thenReturn(List.of(departmentEntity));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(departmentHead, secondStageAuthority);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureSignedInSecondStageAuthorityCanAccessDepartmentHeadPersonData() {

        final Person secondStageAuthority = new Person("muster", "Muster", "Marlene", "muster@example.org");
        secondStageAuthority.setId(1);
        secondStageAuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, DEPARTMENT_HEAD));

        final DepartmentMemberEmbeddable secondStageAuthorityMember = departmentMemberEmbeddable(secondStageAuthority);

        final Person departmentHead = new Person("muster", "Muster", "Marlene", "muster@example.org");
        departmentHead.setId(2);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        final DepartmentMemberEmbeddable departmentHeadMember = departmentMemberEmbeddable(departmentHead);

        final DepartmentEntity dep = new DepartmentEntity();
        dep.setName("dep");
        dep.setMembers(List.of(secondStageAuthorityMember, departmentHeadMember));
        dep.setSecondStageAuthorities(List.of(secondStageAuthority));
        dep.setDepartmentHeads(List.of(departmentHead));

        when(departmentRepository.findBySecondStageAuthorities(secondStageAuthority))
            .thenReturn(List.of(dep));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(secondStageAuthority, departmentHead);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureNotPrivilegedUserCanNotAccessPersonData() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        person.setPermissions(List.of(USER));

        Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setId(2);
        user.setPermissions(List.of(USER));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(user, person);
        assertThat(isAllowed).isFalse();
    }

    @Test
    void ensureNotPrivilegedUserCanAccessOwnPersonData() {

        Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setId(1);
        user.setPermissions(List.of(USER));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(user, user);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureBossHasAccessToAllDepartments() {
        Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setPermissions(List.of(USER, BOSS));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setName("dep");
        when(departmentRepository.findAll()).thenReturn(List.of(departmentEntity));

        final Department department = new Department();
        department.setName("dep");

        var departmentsWithAccess = sut.getDepartmentsPersonHasAccessTo(boss);
        assertThat(departmentsWithAccess).containsExactly(department);
    }

    @Test
    void ensureOfficeHasAccessToAllDepartments() {
        Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setPermissions(List.of(USER, OFFICE));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setName("dep");
        when(departmentRepository.findAll()).thenReturn(List.of(departmentEntity));

        final Department department = new Department();
        department.setName("dep");

        var departmentsWithAccess = sut.getDepartmentsPersonHasAccessTo(office);
        assertThat(departmentsWithAccess).containsExactly(department);
    }

    @Test
    void ensureSecondStageAuthorityHasAccessToAllowedDepartments() {
        final Person secondStageAuthority = new Person("muster", "Muster", "Marlene", "muster@example.org");
        secondStageAuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final DepartmentEntity departmentEntityWithSecondStageRole = new DepartmentEntity();
        departmentEntityWithSecondStageRole.setName("Department A");
        departmentEntityWithSecondStageRole.setId(1);
        final DepartmentEntity departmentEntityWithMemberRole = new DepartmentEntity();
        departmentEntityWithMemberRole.setName("Department B");
        departmentEntityWithMemberRole.setId(2);

        when(departmentRepository.findBySecondStageAuthorities(secondStageAuthority)).thenReturn(List.of(departmentEntityWithSecondStageRole));
        when(departmentRepository.findByMembersPerson(secondStageAuthority)).thenReturn(List.of(departmentEntityWithMemberRole));

        final Department expectedDepartmentWithSecondStageRole = new Department();
        expectedDepartmentWithSecondStageRole.setId(1);
        final Department expectedDepartment = new Department();
        expectedDepartment.setId(2);

        var departmentsWithAccess = sut.getDepartmentsPersonHasAccessTo(secondStageAuthority);
        assertThat(departmentsWithAccess).containsExactly(expectedDepartmentWithSecondStageRole, expectedDepartment);
    }

    @Test
    void ensureDepartmentHeadHasAccessToAllowedDepartments() {
        final Person departmentHead = new Person("muster", "Muster", "Marlene", "muster@example.org");
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final DepartmentEntity departmentEntityWithDepartmentHeadRole = new DepartmentEntity();
        departmentEntityWithDepartmentHeadRole.setName("Department A");
        departmentEntityWithDepartmentHeadRole.setId(1);
        final DepartmentEntity departmentEntityWithMemberRole = new DepartmentEntity();
        departmentEntityWithMemberRole.setName("Department B");
        departmentEntityWithMemberRole.setId(2);

        when(departmentRepository.findByDepartmentHeads(departmentHead)).thenReturn(List.of(departmentEntityWithDepartmentHeadRole));
        when(departmentRepository.findByMembersPerson(departmentHead)).thenReturn(List.of(departmentEntityWithMemberRole));

        final Department expectedDepartmentWithDepartmentHeadRole = new Department();
        expectedDepartmentWithDepartmentHeadRole.setId(1);
        final Department expectedDepartment = new Department();
        expectedDepartment.setId(2);

        var departmentsWithAccess = sut.getDepartmentsPersonHasAccessTo(departmentHead);
        assertThat(departmentsWithAccess).containsExactly(expectedDepartmentWithDepartmentHeadRole, expectedDepartment);
    }

    @Test
    void ensurePersonWithSecondStageAuthorityAndDepartmentHeadHasAccessToAllowedDepartments() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        final DepartmentEntity departmentEntityWithSecondStageRole = new DepartmentEntity();
        departmentEntityWithSecondStageRole.setName("Department A");
        departmentEntityWithSecondStageRole.setId(3);
        final DepartmentEntity departmentEntityWithDepartmentHeadRole = new DepartmentEntity();
        departmentEntityWithDepartmentHeadRole.setName("Department B");
        departmentEntityWithDepartmentHeadRole.setId(1);
        final DepartmentEntity departmentEntityWithMemberRole = new DepartmentEntity();
        departmentEntityWithMemberRole.setName("Department C");
        departmentEntityWithMemberRole.setId(2);

        when(departmentRepository.findBySecondStageAuthorities(person)).thenReturn(List.of(departmentEntityWithSecondStageRole));
        when(departmentRepository.findByDepartmentHeads(person)).thenReturn(List.of(departmentEntityWithDepartmentHeadRole));
        when(departmentRepository.findByMembersPerson(person)).thenReturn(List.of(departmentEntityWithMemberRole));

        final Department expectedDepartmentWithSecondStageRole = new Department();
        expectedDepartmentWithSecondStageRole.setId(3);
        final Department expectedDepartmentWithDepartmentHeadRole = new Department();
        expectedDepartmentWithDepartmentHeadRole.setId(1);
        final Department expectedDepartment = new Department();
        expectedDepartment.setId(2);

        var departmentsWithAccess = sut.getDepartmentsPersonHasAccessTo(person);
        assertThat(departmentsWithAccess).containsExactly(expectedDepartmentWithSecondStageRole, expectedDepartmentWithDepartmentHeadRole, expectedDepartment);
    }

    @Test
    void ensurePersonHasAccessToAssignedDepartments() {
        Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setPermissions(List.of(USER));

        final DepartmentEntity dep = new DepartmentEntity();
        dep.setName("dep");
        when(departmentRepository.findByMembersPerson(user)).thenReturn(List.of(dep));

        final Department expectedDepartment = new Department();
        expectedDepartment.setName("dep");

        var allowedDepartments = sut.getDepartmentsPersonHasAccessTo(user);
        assertThat(allowedDepartments).containsExactly(expectedDepartment);
    }

    @Test
    void ensureDepartmentsHasAccessToAreSortedByName() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        final DepartmentEntity departmentEntityWithSecondStageRole = new DepartmentEntity();
        departmentEntityWithSecondStageRole.setName("Department A");
        departmentEntityWithSecondStageRole.setId(1);
        final DepartmentEntity departmentEntityWithDepartmentHeadRole = new DepartmentEntity();
        departmentEntityWithDepartmentHeadRole.setName("Department C");
        departmentEntityWithDepartmentHeadRole.setId(3);
        final DepartmentEntity departmentEntityWithMemberRole = new DepartmentEntity();
        departmentEntityWithMemberRole.setName("department B");
        departmentEntityWithMemberRole.setId(2);

        when(departmentRepository.findBySecondStageAuthorities(person)).thenReturn(List.of(departmentEntityWithSecondStageRole));
        when(departmentRepository.findByDepartmentHeads(person)).thenReturn(List.of(departmentEntityWithDepartmentHeadRole));
        when(departmentRepository.findByMembersPerson(person)).thenReturn(List.of(departmentEntityWithMemberRole));

        final Department expectedDepartmentWithSecondStageRole = new Department();
        expectedDepartmentWithSecondStageRole.setId(1);
        final Department expectedDepartmentWithDepartmentHeadRole = new Department();
        expectedDepartmentWithDepartmentHeadRole.setId(3);
        final Department expectedDepartment = new Department();
        expectedDepartment.setId(2);

        var departmentsWithAccess = sut.getDepartmentsPersonHasAccessTo(person);
        assertThat(departmentsWithAccess).containsExactly(expectedDepartmentWithSecondStageRole, expectedDepartment, expectedDepartmentWithDepartmentHeadRole);
    }

    @Test
    void getNumberOfDepartment() {

        when(departmentRepository.count()).thenReturn(10L);

        final long numberOfDepartments = sut.getNumberOfDepartments();
        assertThat(numberOfDepartments).isEqualTo(10);
    }

    @Test
    void getDepartmentsByMembers() {

        final Person person = new Person();
        person.setId(42);
        List<Person> persons = List.of(person);

        final DepartmentMemberEmbeddable existingPersonMember = new DepartmentMemberEmbeddable();
        existingPersonMember.setPerson(person);

        final DepartmentEntity departmentEntityA = new DepartmentEntity();
        departmentEntityA.setName("Department A");
        departmentEntityA.setId(1);
        departmentEntityA.setMembers(List.of(existingPersonMember));

        final DepartmentEntity departmentEntityB = new DepartmentEntity();
        departmentEntityB.setName("Department B");
        departmentEntityB.setId(2);
        departmentEntityB.setMembers(List.of(existingPersonMember));


        when(departmentRepository.findDistinctByMembersPersonIn(persons)).thenReturn(List.of(departmentEntityA, departmentEntityB));

        final Map<Integer, List<String>> departmentsByMembers = sut.getDepartmentsByMembers(persons);
        assertThat(departmentsByMembers)
            .containsKey(person.getId())
            .containsValue(List.of("Department A", "Department B"));
    }

    private DepartmentMemberEmbeddable departmentMemberEmbeddable(String username, String firstname, String lastname, String email) {
        final Person person = new Person(username, firstname, lastname, email);

        final DepartmentMemberEmbeddable departmentMemberEmbeddable = new DepartmentMemberEmbeddable();
        departmentMemberEmbeddable.setPerson(person);

        return departmentMemberEmbeddable;
    }

    private DepartmentMemberEmbeddable departmentMemberEmbeddable(Person person) {
        final DepartmentMemberEmbeddable departmentMemberEmbeddable = new DepartmentMemberEmbeddable();

        departmentMemberEmbeddable.setPerson(person);

        return departmentMemberEmbeddable;
    }
}
