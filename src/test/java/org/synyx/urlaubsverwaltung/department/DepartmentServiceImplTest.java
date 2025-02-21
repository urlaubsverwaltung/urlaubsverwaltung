package org.synyx.urlaubsverwaltung.department;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.time.Month.DECEMBER;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createDepartment;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
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
    void ensureGetManagedMembersOfPersonReturnsPageOfDistinctActivePersonsForDepartmentHeadAndSecondStageAuthority() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        final Person max = new Person();
        max.setId(2L);
        max.setFirstName("Max");
        max.setLastName("Mustermann");
        final DepartmentMemberEmbeddable maxMember = new DepartmentMemberEmbeddable();
        maxMember.setPerson(max);

        final Person jane = new Person();
        jane.setId(3L);
        jane.setFirstName("Jane");
        jane.setLastName("Doe");
        final DepartmentMemberEmbeddable janeMember = new DepartmentMemberEmbeddable();
        janeMember.setPerson(jane);

        final DepartmentEntity admins = new DepartmentEntity();
        admins.setName("admins");
        admins.setMembers(List.of(maxMember, janeMember));

        final DepartmentEntity developers = new DepartmentEntity();
        developers.setName("developers");
        developers.setMembers(List.of(janeMember));

        when(departmentRepository.findByDepartmentHeadsOrSecondStageAuthorities(person, person)).thenReturn(List.of(admins, developers));

        final Page<Person> actual = sut.getManagedMembersOfPerson(person, defaultPersonSearchQuery());

        assertThat(actual.getContent()).containsExactly(jane, max);
    }

    @Test
    void ensureGetManagedMembersOfPersonReturnsPageOfDistinctActivePersonsForDepartmentHead() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(DEPARTMENT_HEAD));

        final Person max = new Person();
        max.setId(2L);
        max.setFirstName("Max");
        max.setLastName("Mustermann");
        final DepartmentMemberEmbeddable maxMember = new DepartmentMemberEmbeddable();
        maxMember.setPerson(max);

        final Person jane = new Person();
        jane.setId(3L);
        jane.setFirstName("Jane");
        jane.setLastName("Doe");
        final DepartmentMemberEmbeddable janeMember = new DepartmentMemberEmbeddable();
        janeMember.setPerson(jane);

        final DepartmentEntity admins = new DepartmentEntity();
        admins.setName("admins");
        admins.setMembers(List.of(maxMember, janeMember));

        final DepartmentEntity developers = new DepartmentEntity();
        developers.setName("developers");
        developers.setMembers(List.of(janeMember));

        when(departmentRepository.findByDepartmentHeads(person)).thenReturn(List.of(admins, developers));

        final Page<Person> actual = sut.getManagedMembersOfPerson(person, defaultPersonSearchQuery());

        assertThat(actual.getContent()).containsExactly(jane, max);
    }

    @Test
    void ensureGetManagedMembersOfPersonReturnsPageOfDistinctActivePersonsForSecondStageAuthority() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        final Person max = new Person();
        max.setId(2L);
        max.setFirstName("Max");
        max.setLastName("Mustermann");
        final DepartmentMemberEmbeddable maxMember = new DepartmentMemberEmbeddable();
        maxMember.setPerson(max);

        final Person jane = new Person();
        jane.setId(3L);
        jane.setFirstName("Jane");
        jane.setLastName("Doe");
        final DepartmentMemberEmbeddable janeMember = new DepartmentMemberEmbeddable();
        janeMember.setPerson(jane);

        final DepartmentEntity admins = new DepartmentEntity();
        admins.setName("admins");
        admins.setMembers(List.of(maxMember, janeMember));

        final DepartmentEntity developers = new DepartmentEntity();
        developers.setName("developers");
        developers.setMembers(List.of(janeMember));

        when(departmentRepository.findBySecondStageAuthorities(person)).thenReturn(List.of(admins, developers));

        final Page<Person> actual = sut.getManagedMembersOfPerson(person, defaultPersonSearchQuery());

        assertThat(actual.getContent()).containsExactly(jane, max);
    }

    @Test
    void ensureGetManagedMembersOfPersonReturnsPageOfEmptyList() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of());

        final Page<Person> actual = sut.getManagedMembersOfPerson(person, defaultPersonSearchQuery());

        assertThat(actual.getContent()).isEmpty();
        verifyNoInteractions(departmentRepository);
    }


    @Test
    void ensureGetManagedActiveMembersOfPersonReturnsDistinctActivePersonsForDepartmentHeadAndSecondStageAuthority() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        final Person max = new Person();
        max.setId(2L);
        max.setFirstName("Max");
        max.setLastName("Mustermann");
        final DepartmentMemberEmbeddable maxMember = new DepartmentMemberEmbeddable();
        maxMember.setPerson(max);

        final Person jane = new Person();
        jane.setId(3L);
        jane.setFirstName("Jane");
        jane.setLastName("Doe");
        final DepartmentMemberEmbeddable janeMember = new DepartmentMemberEmbeddable();
        janeMember.setPerson(jane);

        final DepartmentEntity admins = new DepartmentEntity();
        admins.setName("admins");
        admins.setMembers(List.of(maxMember, janeMember));

        final DepartmentEntity developers = new DepartmentEntity();
        developers.setName("developers");
        developers.setMembers(List.of(janeMember));

        when(departmentRepository.findByDepartmentHeadsOrSecondStageAuthorities(person, person)).thenReturn(List.of(admins, developers));

        final List<Person> actual = sut.getManagedActiveMembersOfPerson(person);

        assertThat(actual).containsExactlyInAnyOrder(jane, max);
    }

    @Test
    void ensureGetManagedActiveMembersOfPersonReturnsDistinctActivePersonsForDepartmentHead() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(DEPARTMENT_HEAD));

        final Person max = new Person();
        max.setId(2L);
        max.setFirstName("Max");
        max.setLastName("Mustermann");
        final DepartmentMemberEmbeddable maxMember = new DepartmentMemberEmbeddable();
        maxMember.setPerson(max);

        final Person jane = new Person();
        jane.setId(3L);
        jane.setFirstName("Jane");
        jane.setLastName("Doe");
        final DepartmentMemberEmbeddable janeMember = new DepartmentMemberEmbeddable();
        janeMember.setPerson(jane);

        final DepartmentEntity admins = new DepartmentEntity();
        admins.setName("admins");
        admins.setMembers(List.of(maxMember, janeMember));

        final DepartmentEntity developers = new DepartmentEntity();
        developers.setName("developers");
        developers.setMembers(List.of(janeMember));

        when(departmentRepository.findByDepartmentHeads(person)).thenReturn(List.of(admins, developers));

        final List<Person> actual = sut.getManagedActiveMembersOfPerson(person);

        assertThat(actual).containsExactlyInAnyOrder(jane, max);
    }

    @Test
    void ensureGetManagedActiveMembersOfPersonReturnsDistinctActivePersonsForSecondStageAuthority() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        final Person max = new Person();
        max.setId(2L);
        max.setFirstName("Max");
        max.setLastName("Mustermann");
        final DepartmentMemberEmbeddable maxMember = new DepartmentMemberEmbeddable();
        maxMember.setPerson(max);

        final Person jane = new Person();
        jane.setId(3L);
        jane.setFirstName("Jane");
        jane.setLastName("Doe");
        final DepartmentMemberEmbeddable janeMember = new DepartmentMemberEmbeddable();
        janeMember.setPerson(jane);

        final DepartmentEntity admins = new DepartmentEntity();
        admins.setName("admins");
        admins.setMembers(List.of(maxMember, janeMember));

        final DepartmentEntity developers = new DepartmentEntity();
        developers.setName("developers");
        developers.setMembers(List.of(janeMember));

        when(departmentRepository.findBySecondStageAuthorities(person)).thenReturn(List.of(admins, developers));

        final List<Person> actual = sut.getManagedActiveMembersOfPerson(person);

        assertThat(actual).containsExactlyInAnyOrder(jane, max);
    }

    @Test
    void ensureGetManagedActiveMembersOfPersonReturnsEmptyList() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of());

        final List<Person> actual = sut.getManagedActiveMembersOfPerson(person);

        assertThat(actual).isEmpty();
        verifyNoInteractions(departmentRepository);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonReturnsDistinctInactivePersonsForDepartmentHeadAndSecondStageAuthority() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        final Person max = new Person();
        max.setId(2L);
        max.setFirstName("Max");
        max.setLastName("Mustermann");
        max.setPermissions(List.of(INACTIVE));
        final DepartmentMemberEmbeddable maxMember = new DepartmentMemberEmbeddable();
        maxMember.setPerson(max);

        final Person jane = new Person();
        jane.setId(3L);
        jane.setFirstName("Jane");
        jane.setLastName("Doe");
        final DepartmentMemberEmbeddable janeMember = new DepartmentMemberEmbeddable();
        janeMember.setPerson(jane);

        final Person john = new Person();
        john.setId(4L);
        john.setFirstName("John");
        john.setLastName("Doe");
        john.setPermissions(List.of(INACTIVE));
        final DepartmentMemberEmbeddable johnMember = new DepartmentMemberEmbeddable();
        johnMember.setPerson(john);

        final DepartmentEntity admins = new DepartmentEntity();
        admins.setName("admins");
        admins.setMembers(List.of(maxMember, janeMember, johnMember));

        final DepartmentEntity developers = new DepartmentEntity();
        developers.setName("developers");
        developers.setMembers(List.of(janeMember, johnMember));

        when(departmentRepository.findByDepartmentHeadsOrSecondStageAuthorities(person, person)).thenReturn(List.of(admins, developers));

        final Page<Person> actual = sut.getManagedInactiveMembersOfPerson(person, defaultPersonSearchQuery());

        assertThat(actual.getContent()).containsExactly(john, max);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonReturnsDistinctInactivePersonsForDepartmentHead() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(DEPARTMENT_HEAD));

        final Person max = new Person();
        max.setId(2L);
        max.setFirstName("Max");
        max.setLastName("Mustermann");
        max.setPermissions(List.of(INACTIVE));
        final DepartmentMemberEmbeddable maxMember = new DepartmentMemberEmbeddable();
        maxMember.setPerson(max);

        final Person jane = new Person();
        jane.setId(3L);
        jane.setFirstName("Jane");
        jane.setLastName("Doe");
        final DepartmentMemberEmbeddable janeMember = new DepartmentMemberEmbeddable();
        janeMember.setPerson(jane);

        final Person john = new Person();
        john.setId(4L);
        john.setFirstName("John");
        john.setLastName("Doe");
        john.setPermissions(List.of(INACTIVE));
        final DepartmentMemberEmbeddable johnMember = new DepartmentMemberEmbeddable();
        johnMember.setPerson(john);

        final DepartmentEntity admins = new DepartmentEntity();
        admins.setName("admins");
        admins.setMembers(List.of(maxMember, janeMember, johnMember));

        final DepartmentEntity developers = new DepartmentEntity();
        developers.setName("developers");
        developers.setMembers(List.of(janeMember, johnMember));

        when(departmentRepository.findByDepartmentHeads(person)).thenReturn(List.of(admins, developers));

        final Page<Person> actual = sut.getManagedInactiveMembersOfPerson(person, defaultPersonSearchQuery());

        assertThat(actual.getContent()).containsExactly(john, max);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonReturnsDistinctInactivePersonsForSecondStageAuthority() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        final Person max = new Person();
        max.setId(2L);
        max.setFirstName("Max");
        max.setLastName("Mustermann");
        max.setPermissions(List.of(INACTIVE));
        final DepartmentMemberEmbeddable maxMember = new DepartmentMemberEmbeddable();
        maxMember.setPerson(max);

        final Person jane = new Person();
        jane.setId(3L);
        jane.setFirstName("Jane");
        jane.setLastName("Doe");
        final DepartmentMemberEmbeddable janeMember = new DepartmentMemberEmbeddable();
        janeMember.setPerson(jane);

        final Person john = new Person();
        john.setId(4L);
        john.setFirstName("John");
        john.setLastName("Doe");
        john.setPermissions(List.of(INACTIVE));
        final DepartmentMemberEmbeddable johnMember = new DepartmentMemberEmbeddable();
        johnMember.setPerson(john);

        final DepartmentEntity admins = new DepartmentEntity();
        admins.setName("admins");
        admins.setMembers(List.of(maxMember, janeMember, johnMember));

        final DepartmentEntity developers = new DepartmentEntity();
        developers.setName("developers");
        developers.setMembers(List.of(janeMember, johnMember));

        when(departmentRepository.findBySecondStageAuthorities(person)).thenReturn(List.of(admins, developers));

        final Page<Person> actual = sut.getManagedInactiveMembersOfPerson(person, defaultPersonSearchQuery());

        assertThat(actual.getContent()).containsExactly(john, max);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonReturnsEmptyList() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of());

        final Page<Person> actual = sut.getManagedInactiveMembersOfPerson(person, defaultPersonSearchQuery());

        assertThat(actual.getContent()).isEmpty();
        verifyNoInteractions(departmentRepository);
    }

    @Test
    void ensureGetManagedActiveMembersOfPersonReturnsPageSecond() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(DEPARTMENT_HEAD));

        final List<DepartmentMemberEmbeddable> activeMembers =
            anyDepartmentMembers(14, 1, p -> p.setPermissions(List.of(USER)));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(activeMembers);

        when(departmentRepository.findByDepartmentHeads(person)).thenReturn(List.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(1, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedMembersOfPerson(person, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(2);
        assertThat(actual.getPageable().getPageNumber()).isEqualTo(1);
        assertThat(actual.getContent()).hasSize(4);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonReturnsPageSecond() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(DEPARTMENT_HEAD));

        final List<DepartmentMemberEmbeddable> inactiveMembers =
            anyDepartmentMembers(14, 1, p -> p.setPermissions(List.of(INACTIVE)));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(inactiveMembers);

        when(departmentRepository.findByDepartmentHeads(person)).thenReturn(List.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(1, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPerson(person, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(2);
        assertThat(actual.getPageable().getPageNumber()).isEqualTo(1);
        assertThat(actual.getContent()).hasSize(4);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureGetManagedMembersOfPersonAndDepartmentForRole(Role role) {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(role));

        final Person member = new Person();
        member.setId(2L);

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(List.of(departmentMemberEmbeddable(member)));

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getPageable().getPageNumber()).isZero();
        assertThat(actual.getContent()).hasSize(1);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonAndDepartmentForDepartmentHead() {

        final Person departmentHead = new Person();
        departmentHead.setId(1L);
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));

        final Person inactiveMember = new Person();
        inactiveMember.setId(2L);
        inactiveMember.setPermissions(List.of(INACTIVE));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(List.of(departmentMemberEmbeddable(inactiveMember)));
        departmentEntity.setDepartmentHeads(List.of(departmentHead));

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPersonAndDepartment(departmentHead, 1L, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getPageable().getPageNumber()).isZero();
        assertThat(actual.getContent()).hasSize(1);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonAndDepartmentForSecondStageAuthority() {

        final Person secondStageAuthority = new Person();
        secondStageAuthority.setId(1L);
        secondStageAuthority.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        final Person inactiveMember = new Person();
        inactiveMember.setId(2L);
        inactiveMember.setPermissions(List.of(INACTIVE));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(List.of(departmentMemberEmbeddable(inactiveMember)));
        departmentEntity.setSecondStageAuthorities(List.of(secondStageAuthority));

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPersonAndDepartment(secondStageAuthority, 1L, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getPageable().getPageNumber()).isZero();
        assertThat(actual.getContent()).hasSize(1);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonAndDepartmentForMember() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final Person inactiveMember = new Person();
        inactiveMember.setId(2L);
        inactiveMember.setPermissions(List.of(INACTIVE));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(List.of(departmentMemberEmbeddable(person), departmentMemberEmbeddable(inactiveMember)));

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        assertThat(actual).isEqualTo(Page.empty());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureGetManagedMembersOfPersonAndDepartmentReturnsPageSecond(Role role) {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(role));

        final List<DepartmentMemberEmbeddable> activeMembers =
            anyDepartmentMembers(14, 2, p -> p.setPermissions(List.of(USER)));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(activeMembers);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(1, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(2);
        assertThat(actual.getPageable().getPageNumber()).isEqualTo(1);
        assertThat(actual.getContent()).hasSize(4);
    }

    @Test
    void ensureGetManagedMembersOfPersonAndDepartmentReturnsEmptyPageWhenDepartmentHeadIsNotResponsible() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(DEPARTMENT_HEAD));

        final List<DepartmentMemberEmbeddable> activeMembers =
            // do not start with personId=1 to exclude person from members list
            anyDepartmentMembers(14, 2, p -> p.setPermissions(List.of(USER)));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(activeMembers);
        // person is not department head of THIS department
        departmentEntity.setDepartmentHeads(List.of());

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(1, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);
        assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    void ensureGetManagedMembersOfPersonAndDepartmentReturnsEmptyPageWhenSecondStageAuthorityIsNotResponsible() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        final List<DepartmentMemberEmbeddable> activeMembers =
            // do not start with personId=1 to exclude person from members list
            anyDepartmentMembers(14, 2, p -> p.setPermissions(List.of(USER)));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(activeMembers);
        // person is not second stage authority of THIS department
        departmentEntity.setSecondStageAuthorities(List.of());

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(1, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);
        assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    void ensureGetManagedMembersOfPersonAndDepartmentReturnsEmptyPageWhenGivenUserIsNotAMember() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final List<DepartmentMemberEmbeddable> activeMembers =
            // do not start with personId=1 to exclude person from members list
            anyDepartmentMembers(14, 2, p -> p.setPermissions(List.of(USER)));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(activeMembers);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(1, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);
        assertThat(actual).isEqualTo(Page.empty());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureGetManagedInactiveMembersOfPersonAndDepartmentForRole(Role role) {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(role));

        final Person inactiveMember = new Person();
        inactiveMember.setId(2L);
        inactiveMember.setPermissions(List.of(INACTIVE));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(List.of(departmentMemberEmbeddable(inactiveMember)));

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getPageable().getPageNumber()).isZero();
        assertThat(actual.getContent()).hasSize(1);
    }

    @Test
    void ensureGetManagedMembersOfPersonAndDepartmentForDepartmentHead() {

        final Person departmentHead = new Person();
        departmentHead.setId(1L);
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));

        final Person member = new Person();
        member.setId(2L);

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(List.of(departmentMemberEmbeddable(member)));
        departmentEntity.setDepartmentHeads(List.of(departmentHead));

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedMembersOfPersonAndDepartment(departmentHead, 1L, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getPageable().getPageNumber()).isZero();
        assertThat(actual.getContent()).hasSize(1);
    }

    @Test
    void ensureGetManagedMembersOfPersonAndDepartmentForSecondStageAuthority() {

        final Person secondStageAuthority = new Person();
        secondStageAuthority.setId(1L);
        secondStageAuthority.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        final Person member = new Person();
        member.setId(2L);

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(List.of(departmentMemberEmbeddable(member)));
        departmentEntity.setSecondStageAuthorities(List.of(secondStageAuthority));

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedMembersOfPersonAndDepartment(secondStageAuthority, 1L, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getPageable().getPageNumber()).isZero();
        assertThat(actual.getContent()).hasSize(1);
    }

    @Test
    void ensureGetManagedMembersOfPersonAndDepartmentForMember() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final Person member = new Person();
        member.setId(2L);

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(List.of(departmentMemberEmbeddable(person), departmentMemberEmbeddable(member)));

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonAndDepartmentReturnsPageSecond() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(BOSS));

        final List<DepartmentMemberEmbeddable> inactiveMembers =
            anyDepartmentMembers(14, 2, p -> p.setPermissions(List.of(INACTIVE)));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(inactiveMembers);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(1, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(2);
        assertThat(actual.getPageable().getPageNumber()).isEqualTo(1);
        assertThat(actual.getContent()).hasSize(4);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonAndDepartmentReturnsEmptyPageWhenDepartmentHeadIsNotResponsible() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(DEPARTMENT_HEAD));

        final List<DepartmentMemberEmbeddable> inactiveMembers =
            // do not start with personId=1 to exclude person from members list
            anyDepartmentMembers(14, 2, p -> p.setPermissions(List.of(INACTIVE)));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(inactiveMembers);
        // person is not department head of THIS department
        departmentEntity.setDepartmentHeads(List.of());

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(1, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);
        assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonAndDepartmentReturnsEmptyPageWhenSecondStageAuthorityIsNotResponsible() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        final List<DepartmentMemberEmbeddable> inactiveMembers =
            // do not start with personId=1 to exclude person from members list
            anyDepartmentMembers(14, 2, p -> p.setPermissions(List.of(INACTIVE)));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(inactiveMembers);
        // person is not second stage authority of THIS department
        departmentEntity.setSecondStageAuthorities(List.of());

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(1, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);
        assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonAndDepartmentReturnsEmptyPageWhenGivenUserIsNotAMember() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final List<DepartmentMemberEmbeddable> inactiveMembers =
            // do not start with personId=1 to exclude person from members list
            anyDepartmentMembers(14, 2, p -> p.setPermissions(List.of(INACTIVE)));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(inactiveMembers);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));

        final PageRequest pageRequest = PageRequest.of(1, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);
        assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    void ensureNewDepartmentCreation() {
        final Department department = new Department();
        department.setName("department");

        final DepartmentEntity savedDepartmentEntity = new DepartmentEntity();
        savedDepartmentEntity.setId(42L);
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

        sut.getDepartmentById(42L);
        verify(departmentRepository).findById(42L);
    }

    @Test
    void ensureUpdateDepartmentFailsWhenDepartmentDoesNotExistYet() {
        final Department department = new Department();
        department.setId(42L);
        department.setName("department");

        when(departmentRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatIllegalStateException()
            .isThrownBy(() -> sut.update(department));
    }

    @Test
    void ensureUpdateCallDepartmentDAOUpdate() {

        final Department department = new Department();
        department.setId(42L);
        department.setName("department");

        when(departmentRepository.findById(42L)).thenReturn(Optional.of(new DepartmentEntity()));

        final DepartmentEntity updatedDepartmentEntity = new DepartmentEntity();
        updatedDepartmentEntity.setId(42L);
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
        department.setId(1L);
        department.setName("department");

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setCreatedAt(LocalDate.of(2020, DECEMBER, 4));
        departmentEntity.setLastModification(LocalDate.of(2020, DECEMBER, 4));

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));
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
        department.setId(42L);
        department.setName("department");
        department.setMembers(List.of(existingPerson, person));

        final DepartmentMemberEmbeddable existingPersonMember = new DepartmentMemberEmbeddable();
        existingPersonMember.setPerson(existingPerson);
        existingPersonMember.setAccessionDate(Instant.now(clock).minus(1, DAYS));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setCreatedAt(LocalDate.of(2020, DECEMBER, 4));
        departmentEntity.setLastModification(LocalDate.of(2020, DECEMBER, 4));
        departmentEntity.setMembers(List.of(existingPersonMember));

        when(departmentRepository.findById(42L)).thenReturn(Optional.of(departmentEntity));
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
        personThatWillLeft.setId(1L);

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

        when(departmentRepository.findById(42L)).thenReturn(Optional.of(departmentEntity));
        when(departmentRepository.save(any())).then(returnsFirstArg());

        final Department department = new Department();
        department.setId(42L);
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
        departmentEntityA.setId(1L);
        departmentEntityA.setName("Department A");
        final DepartmentEntity departmentEntityB = new DepartmentEntity();
        departmentEntityB.setId(2L);
        departmentEntityB.setName("Department B");

        final Department departmentA = new Department();
        departmentA.setId(1L);
        final Department departmentB = new Department();
        departmentB.setId(2L);

        when(departmentRepository.findAll()).thenReturn(List.of(departmentEntityA, departmentEntityB));

        final List<Department> allDepartments = sut.getAllDepartments();
        assertThat(allDepartments)
            .hasSize(2)
            .containsExactly(departmentA, departmentB);
    }

    @Test
    void ensureGetAllDepartmentSorted() {

        final DepartmentEntity departmentEntityA = new DepartmentEntity();
        departmentEntityA.setId(1L);
        departmentEntityA.setName("Department A");
        final DepartmentEntity departmentEntityB = new DepartmentEntity();
        departmentEntityB.setId(2L);
        departmentEntityB.setName("Department B");

        final Department departmentA = new Department();
        departmentA.setId(1L);
        final Department departmentB = new Department();
        departmentB.setId(2L);

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
    void ensureGetManagedDepartmentsOfDepartmentHeadDepartmentSorted() {

        final DepartmentEntity departmentEntityA = new DepartmentEntity();
        departmentEntityA.setId(1L);
        departmentEntityA.setName("Department A");
        final DepartmentEntity departmentEntityB = new DepartmentEntity();
        departmentEntityB.setId(2L);
        departmentEntityB.setName("Department B");

        final Department departmentA = new Department();
        departmentA.setId(1L);
        final Department departmentB = new Department();
        departmentB.setId(2L);

        final Person person = new Person();

        when(departmentRepository.findByDepartmentHeads(person)).thenReturn(List.of(departmentEntityB, departmentEntityA));

        final List<Department> allDepartments = sut.getManagedDepartmentsOfDepartmentHead(person);
        assertThat(allDepartments)
            .hasSize(2)
            .containsExactly(departmentA, departmentB);
    }

    @Test
    void ensureGetManagedDepartmentsOfSecondStageAuthorityCallCorrectDAOMethod() {
        final Person person = new Person();
        sut.getManagedDepartmentsOfSecondStageAuthority(person);
        verify(departmentRepository).findBySecondStageAuthorities(person);
    }

    @Test
    void ensureGetManagedDepartmentsOfSecondStageAuthorityDepartmentSorted() {

        final DepartmentEntity departmentEntityA = new DepartmentEntity();
        departmentEntityA.setId(1L);
        departmentEntityA.setName("Department A");
        final DepartmentEntity departmentEntityB = new DepartmentEntity();
        departmentEntityB.setId(2L);
        departmentEntityB.setName("Department B");

        final Department departmentA = new Department();
        departmentA.setId(1L);
        final Department departmentB = new Department();
        departmentB.setId(2L);

        final Person person = new Person();

        when(departmentRepository.findBySecondStageAuthorities(person)).thenReturn(List.of(departmentEntityB, departmentEntityA));

        final List<Department> allDepartments = sut.getManagedDepartmentsOfSecondStageAuthority(person);
        assertThat(allDepartments)
            .hasSize(2)
            .containsExactly(departmentA, departmentB);
    }

    @Test
    void ensureGetAssignedDepartmentsOfMemberCallCorrectDAOMethod() {
        final Person person = new Person();
        sut.getAssignedDepartmentsOfMember(person);
        verify(departmentRepository).findByMembersPerson(person);
    }

    @Test
    void ensureGetAssignedDepartmentsOfMemberDepartmentSorted() {

        final DepartmentEntity departmentEntityA = new DepartmentEntity();
        departmentEntityA.setId(1L);
        departmentEntityA.setName("Department A");
        final DepartmentEntity departmentEntityB = new DepartmentEntity();
        departmentEntityB.setId(2L);
        departmentEntityB.setName("Department B");

        final Department departmentA = new Department();
        departmentA.setId(1L);
        final Department departmentB = new Department();
        departmentB.setId(2L);

        final Person person = new Person();

        when(departmentRepository.findByMembersPerson(person)).thenReturn(List.of(departmentEntityB, departmentEntityA));

        final List<Department> allDepartments = sut.getAssignedDepartmentsOfMember(person);
        assertThat(allDepartments)
            .hasSize(2)
            .containsExactly(departmentA, departmentB);
    }

    @Test
    void ensureDeletionIsNotExecutedIfDepartmentWithGivenIDDoesNotExist() {

        sut.delete(0L);

        verify(departmentRepository, never()).deleteById(anyLong());
    }

    @Test
    void ensureDeleteCallFindOneAndDelete() {

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setName("department");

        when(departmentRepository.existsById(0L)).thenReturn(true);

        sut.delete(0L);

        verify(departmentRepository).existsById(0L);
        verify(departmentRepository).deleteById(0L);
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

        when(departmentRepository.count()).thenReturn(1L);

        final Person person = new Person();
        person.setPermissions(List.of(USER));

        final LocalDate date = LocalDate.now(UTC);

        when(departmentRepository.findByMembersPerson(person)).thenReturn(emptyList());

        final List<Application> applications = sut.getApplicationsFromColleaguesOf(person, date, date);
        assertThat(applications).isEmpty();

        verify(applicationService).getForStatesAndPerson(ApplicationStatus.activeStatuses(), List.of(), date, date);
    }

    @Test
    void ensureReturnsEmptyListOfDepartmentApplicationsIfNoMatchingApplicationsForLeave() {

        when(departmentRepository.count()).thenReturn(1L);

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
        when(applicationService.getForStatesAndPerson(ApplicationStatus.activeStatuses(), List.of(admin1, admin2, marketing1Person, marketing2Person, marketing3Person), date, date))
            .thenReturn(emptyList());

        final List<Application> applications = sut.getApplicationsFromColleaguesOf(person, date, date);
        assertThat(applications).isEmpty();
    }


    @Test
    void ensureReturnsOnlyWaitingAndAllowedAndCancellationRequestDepartmentApplicationsForLeave() {

        when(departmentRepository.count()).thenReturn(1L);

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
        waitingApplication.setStatus(WAITING);
        waitingApplication.setStartDate(LocalDate.of(2022, 10, 2));

        final Application allowedApplication = new Application();
        allowedApplication.setStatus(ALLOWED);
        allowedApplication.setStartDate(LocalDate.of(2022, 11, 2));

        final Application cancellationRequestApplication = new Application();
        cancellationRequestApplication.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        cancellationRequestApplication.setStartDate(LocalDate.of(2022, 9, 12));

        when(departmentRepository.findByMembersPerson(person)).thenReturn(List.of(admins, marketing));
        when(applicationService.getForStatesAndPerson(ApplicationStatus.activeStatuses(), List.of(admin1, marketing1), date, date))
            .thenReturn(List.of(waitingApplication, allowedApplication, cancellationRequestApplication));

        final List<Application> applications = sut.getApplicationsFromColleaguesOf(person, date, date);
        assertThat(applications).containsExactly(cancellationRequestApplication, waitingApplication, allowedApplication);
    }

    @Test
    void ensuresApplicationsFromOthersInDepartmentAreSortedByStartDate() {

        when(departmentRepository.count()).thenReturn(1L);

        final Person person = new Person();
        person.setPermissions(List.of(USER));

        final LocalDate date = LocalDate.now(UTC);

        final Person marketingPerson = new Person("carl", "carl", "carl", "carl@example.org");
        final DepartmentMemberEmbeddable memberEmbeddable = departmentMemberEmbeddable(marketingPerson);

        final DepartmentEntity marketing = new DepartmentEntity();
        marketing.setName("marketing");
        marketing.setMembers(List.of(memberEmbeddable, departmentMemberEmbeddable(person)));

        final Application waitingApplication = new Application();
        waitingApplication.setStatus(WAITING);
        waitingApplication.setStartDate(LocalDate.of(2022, 10, 2));

        final Application allowedApplication = new Application();
        allowedApplication.setStatus(ALLOWED);
        allowedApplication.setStartDate(LocalDate.of(2022, 11, 2));

        final Application cancellationRequestApplication = new Application();
        cancellationRequestApplication.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        cancellationRequestApplication.setStartDate(LocalDate.of(2022, 9, 12));

        when(departmentRepository.findByMembersPerson(person)).thenReturn(List.of(marketing));
        when(applicationService.getForStatesAndPerson(ApplicationStatus.activeStatuses(), List.of(marketingPerson), date, date))
            .thenReturn(List.of(waitingApplication, allowedApplication, cancellationRequestApplication));

        final List<Application> applications = sut.getApplicationsFromColleaguesOf(person, date, date);
        assertThat(applications).containsExactly(cancellationRequestApplication, waitingApplication, allowedApplication);
    }

    @Test
    void ensuresApplicationsFromOthersIfNoDepartmentIsAllApplicationsWithoutApplicationFromRequestedPerson() {

        when(departmentRepository.count()).thenReturn(0L);

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final Person colleague = new Person();
        colleague.setId(2L);

        final LocalDate date = LocalDate.now(UTC);

        final Application waitingApplication = new Application();
        waitingApplication.setPerson(colleague);
        waitingApplication.setStatus(WAITING);
        waitingApplication.setStartDate(LocalDate.of(2022, 10, 2));

        final Application allowedApplication = new Application();
        allowedApplication.setPerson(person);
        allowedApplication.setStatus(ALLOWED);
        allowedApplication.setStartDate(LocalDate.of(2022, 11, 2));

        final Application cancellationRequestApplication = new Application();
        cancellationRequestApplication.setPerson(colleague);
        cancellationRequestApplication.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        cancellationRequestApplication.setStartDate(LocalDate.of(2022, 9, 12));

        when(applicationService.getForStates(ApplicationStatus.activeStatuses(), date, date))
            .thenReturn(List.of(waitingApplication, allowedApplication, cancellationRequestApplication));

        final List<Application> applications = sut.getApplicationsFromColleaguesOf(person, date, date);
        assertThat(applications).containsExactly(cancellationRequestApplication, waitingApplication);
    }

    @Test
    void ensureSignedInOfficeUserCanAccessPersonData() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setId(2L);
        office.setPermissions(List.of(USER, OFFICE));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(office, person);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureSignedInBossUserCanAccessPersonData() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setId(2L);
        boss.setPermissions(List.of(USER, BOSS));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(boss, person);
        assertThat(isAllowed).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureIsPersonAllowedToManageDepartment(Role givenRole) {

        final Department department = new Department();
        department.setId(1L);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(2L);
        person.setPermissions(List.of(USER, givenRole));

        boolean isAllowed = sut.isPersonAllowedToManageDepartment(person, department);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureIsPersonAllowedToManageDepartmentWhenDepartmentHead() {

        final Person departmentHead = new Person("muster", "Muster", "Marlene", "muster@example.org");
        departmentHead.setId(2L);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final Department department = new Department();
        department.setId(1L);
        department.setDepartmentHeads(List.of(departmentHead));

        boolean isAllowed = sut.isPersonAllowedToManageDepartment(departmentHead, department);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureIsPersonAllowedToManageDepartmentWhenSecondStageAuthority() {

        final Person secondStageuthority = new Person("muster", "Muster", "Marlene", "muster@example.org");
        secondStageuthority.setId(2L);
        secondStageuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final Department department = new Department();
        department.setId(1L);
        department.setSecondStageAuthorities(List.of(secondStageuthority));

        boolean isAllowed = sut.isPersonAllowedToManageDepartment(secondStageuthority, department);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureIsPersonAllowedToManageDepartmentIsFalseForUser() {

        final Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setId(2L);
        user.setPermissions(List.of(USER));

        final Department department = new Department();
        department.setId(1L);

        boolean isAllowed = sut.isPersonAllowedToManageDepartment(user, department);
        assertThat(isAllowed).isFalse();
    }

    @Test
    void ensureIsPersonAllowedToManageDepartmentIsFalseForNormalDepartmentMember() {

        final Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setId(2L);
        user.setPermissions(List.of(USER));

        final Department department = new Department();
        department.setId(1L);
        department.setMembers(List.of(user));

        boolean isAllowed = sut.isPersonAllowedToManageDepartment(user, department);
        assertThat(isAllowed).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureIsPersonAllowedToManageDepartmentIsFalseForRoleDepartmentHeadButNotThisDepartment(Role givenRole) {

        final Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setId(2L);
        user.setPermissions(List.of(USER, givenRole));

        final Department department = new Department();
        department.setId(1L);

        boolean isAllowed = sut.isPersonAllowedToManageDepartment(user, department);
        assertThat(isAllowed).isFalse();
    }

    @Test
    void ensureSignedInDepartmentHeadOfPersonCanAccessPersonData() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final DepartmentMemberEmbeddable personMember = departmentMemberEmbeddable(person);

        final Person departmentHead = new Person("muster", "Muster", "Marlene", "muster@example.org");
        departmentHead.setId(2L);
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
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final Person departmentHead = new Person("muster", "Muster", "Marlene", "muster@example.org");
        departmentHead.setId(2L);
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
        secondStageAuthority.setId(1L);
        secondStageAuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final DepartmentMemberEmbeddable secondStageAuthorityMember = departmentMemberEmbeddable(secondStageAuthority);

        final Person departmentHead = new Person("muster", "Muster", "Marlene", "muster@example.org");
        departmentHead.setId(2L);
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
        secondStageAuthority.setId(1L);
        secondStageAuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, DEPARTMENT_HEAD));

        final DepartmentMemberEmbeddable secondStageAuthorityMember = departmentMemberEmbeddable(secondStageAuthority);

        final Person departmentHead = new Person("muster", "Muster", "Marlene", "muster@example.org");
        departmentHead.setId(2L);
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
        person.setId(1L);
        person.setPermissions(List.of(USER));

        Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setId(2L);
        user.setPermissions(List.of(USER));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(user, person);
        assertThat(isAllowed).isFalse();
    }

    @Test
    void ensureNotPrivilegedUserCanAccessOwnPersonData() {

        Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setId(1L);
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
        departmentEntityWithSecondStageRole.setId(1L);
        final DepartmentEntity departmentEntityWithMemberRole = new DepartmentEntity();
        departmentEntityWithMemberRole.setName("Department B");
        departmentEntityWithMemberRole.setId(2L);

        when(departmentRepository.findBySecondStageAuthorities(secondStageAuthority)).thenReturn(List.of(departmentEntityWithSecondStageRole));
        when(departmentRepository.findByMembersPerson(secondStageAuthority)).thenReturn(List.of(departmentEntityWithMemberRole));

        final Department expectedDepartmentWithSecondStageRole = new Department();
        expectedDepartmentWithSecondStageRole.setId(1L);
        final Department expectedDepartment = new Department();
        expectedDepartment.setId(2L);

        var departmentsWithAccess = sut.getDepartmentsPersonHasAccessTo(secondStageAuthority);
        assertThat(departmentsWithAccess).containsExactly(expectedDepartmentWithSecondStageRole, expectedDepartment);
    }

    @Test
    void ensureDepartmentHeadHasAccessToAllowedDepartments() {
        final Person departmentHead = new Person("muster", "Muster", "Marlene", "muster@example.org");
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final DepartmentEntity departmentEntityWithDepartmentHeadRole = new DepartmentEntity();
        departmentEntityWithDepartmentHeadRole.setName("Department A");
        departmentEntityWithDepartmentHeadRole.setId(1L);
        final DepartmentEntity departmentEntityWithMemberRole = new DepartmentEntity();
        departmentEntityWithMemberRole.setName("Department B");
        departmentEntityWithMemberRole.setId(2L);

        when(departmentRepository.findByDepartmentHeads(departmentHead)).thenReturn(List.of(departmentEntityWithDepartmentHeadRole));
        when(departmentRepository.findByMembersPerson(departmentHead)).thenReturn(List.of(departmentEntityWithMemberRole));

        final Department expectedDepartmentWithDepartmentHeadRole = new Department();
        expectedDepartmentWithDepartmentHeadRole.setId(1L);
        final Department expectedDepartment = new Department();
        expectedDepartment.setId(2L);

        var departmentsWithAccess = sut.getDepartmentsPersonHasAccessTo(departmentHead);
        assertThat(departmentsWithAccess).containsExactly(expectedDepartmentWithDepartmentHeadRole, expectedDepartment);
    }

    @Test
    void ensurePersonWithSecondStageAuthorityAndDepartmentHeadHasAccessToAllowedDepartments() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        final DepartmentEntity departmentEntityWithSecondStageRole = new DepartmentEntity();
        departmentEntityWithSecondStageRole.setName("Department A");
        departmentEntityWithSecondStageRole.setId(3L);
        final DepartmentEntity departmentEntityWithDepartmentHeadRole = new DepartmentEntity();
        departmentEntityWithDepartmentHeadRole.setName("Department B");
        departmentEntityWithDepartmentHeadRole.setId(1L);
        final DepartmentEntity departmentEntityWithMemberRole = new DepartmentEntity();
        departmentEntityWithMemberRole.setName("Department C");
        departmentEntityWithMemberRole.setId(2L);

        when(departmentRepository.findBySecondStageAuthorities(person)).thenReturn(List.of(departmentEntityWithSecondStageRole));
        when(departmentRepository.findByDepartmentHeads(person)).thenReturn(List.of(departmentEntityWithDepartmentHeadRole));
        when(departmentRepository.findByMembersPerson(person)).thenReturn(List.of(departmentEntityWithMemberRole));

        final Department expectedDepartmentWithSecondStageRole = new Department();
        expectedDepartmentWithSecondStageRole.setId(3L);
        final Department expectedDepartmentWithDepartmentHeadRole = new Department();
        expectedDepartmentWithDepartmentHeadRole.setId(1L);
        final Department expectedDepartment = new Department();
        expectedDepartment.setId(2L);

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
        departmentEntityWithSecondStageRole.setId(1L);
        final DepartmentEntity departmentEntityWithDepartmentHeadRole = new DepartmentEntity();
        departmentEntityWithDepartmentHeadRole.setName("Department C");
        departmentEntityWithDepartmentHeadRole.setId(3L);
        final DepartmentEntity departmentEntityWithMemberRole = new DepartmentEntity();
        departmentEntityWithMemberRole.setName("department B");
        departmentEntityWithMemberRole.setId(2L);

        when(departmentRepository.findBySecondStageAuthorities(person)).thenReturn(List.of(departmentEntityWithSecondStageRole));
        when(departmentRepository.findByDepartmentHeads(person)).thenReturn(List.of(departmentEntityWithDepartmentHeadRole));
        when(departmentRepository.findByMembersPerson(person)).thenReturn(List.of(departmentEntityWithMemberRole));

        final Department expectedDepartmentWithSecondStageRole = new Department();
        expectedDepartmentWithSecondStageRole.setId(1L);
        final Department expectedDepartmentWithDepartmentHeadRole = new Department();
        expectedDepartmentWithDepartmentHeadRole.setId(3L);
        final Department expectedDepartment = new Department();
        expectedDepartment.setId(2L);

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
        person.setId(42L);

        final DepartmentMemberEmbeddable existingPersonMember = new DepartmentMemberEmbeddable();
        existingPersonMember.setPerson(person);

        final DepartmentEntity departmentEntityA = new DepartmentEntity();
        departmentEntityA.setName("Department A");
        departmentEntityA.setId(1L);
        departmentEntityA.setMembers(List.of(existingPersonMember));

        final DepartmentEntity departmentEntityB = new DepartmentEntity();
        departmentEntityB.setName("Department B");
        departmentEntityB.setId(2L);
        departmentEntityB.setMembers(List.of(existingPersonMember));

        when(departmentRepository.findDistinctByMembersPersonIn(List.of(person))).thenReturn(List.of(departmentEntityA, departmentEntityB));

        final Map<PersonId, List<String>> departmentsByMembers = sut.getDepartmentNamesByMembers(List.of(person));
        assertThat(departmentsByMembers).containsEntry(new PersonId(42L), List.of("Department A", "Department B"));
    }

    @Test
    void getDepartmentsByMembersForDifferentDepartmentsAndPersons() {

        final Person person = new Person();
        person.setId(42L);

        final Person personTwo = new Person();
        personTwo.setId(1337L);

        final DepartmentMemberEmbeddable existingPersonMember = new DepartmentMemberEmbeddable();
        existingPersonMember.setPerson(person);

        final DepartmentMemberEmbeddable existingPersonMemberTwo = new DepartmentMemberEmbeddable();
        existingPersonMemberTwo.setPerson(personTwo);

        final DepartmentEntity departmentEntityA = new DepartmentEntity();
        departmentEntityA.setName("Department A");
        departmentEntityA.setId(1L);
        departmentEntityA.setMembers(List.of(existingPersonMember));

        final DepartmentEntity departmentEntityB = new DepartmentEntity();
        departmentEntityB.setName("Department B");
        departmentEntityB.setId(2L);
        departmentEntityB.setMembers(List.of(existingPersonMemberTwo));

        when(departmentRepository.findDistinctByMembersPersonIn(List.of(person, personTwo))).thenReturn(List.of(departmentEntityA, departmentEntityB));

        final Map<PersonId, List<String>> departmentsByMembers = sut.getDepartmentNamesByMembers(List.of(person, personTwo));

        assertThat(departmentsByMembers)
            .containsEntry(new PersonId(42L), List.of("Department A"))
            .containsEntry(new PersonId(1337L), List.of("Department B"));
    }

    @Test
    void getDepartmentsByMembersReturnsOnlyRequestedPersons() {

        final Person personOne = anyPerson(1);
        final Person personTwo = anyPerson(2);

        final DepartmentMemberEmbeddable departmentMemberOne = new DepartmentMemberEmbeddable();
        departmentMemberOne.setPerson(personOne);

        final DepartmentMemberEmbeddable departmentMemberTwo = new DepartmentMemberEmbeddable();
        departmentMemberTwo.setPerson(personTwo);

        final DepartmentEntity departmentEntityA = new DepartmentEntity();
        departmentEntityA.setName("Department A");
        departmentEntityA.setId(1L);
        departmentEntityA.setMembers(List.of(departmentMemberOne, departmentMemberTwo));

        when(departmentRepository.findDistinctByMembersPersonIn(List.of(personOne))).thenReturn(List.of(departmentEntityA));

        final Map<PersonId, List<String>> departmentsByMembers = sut.getDepartmentNamesByMembers(List.of(personOne));

        assertThat(departmentsByMembers)
            .hasSize(1)
            .containsEntry(new PersonId(1L), List.of("Department A"));
    }

    @Test
    void getDepartmentsByMembersWithDepartmentMemberIntersection() {

        final Person personOne = anyPerson(1);
        final Person personTwo = anyPerson(2);
        final Person personThree = anyPerson(3);

        final DepartmentMemberEmbeddable departmentMemberOne = new DepartmentMemberEmbeddable();
        departmentMemberOne.setPerson(personOne);

        final DepartmentMemberEmbeddable departmentMemberTwo = new DepartmentMemberEmbeddable();
        departmentMemberTwo.setPerson(personTwo);

        final DepartmentMemberEmbeddable departmentMemberThree = new DepartmentMemberEmbeddable();
        departmentMemberThree.setPerson(personThree);

        final DepartmentEntity departmentEntityA = new DepartmentEntity();
        departmentEntityA.setId(1L);
        departmentEntityA.setName("Department A");
        departmentEntityA.setMembers(List.of(departmentMemberOne, departmentMemberTwo));

        final DepartmentEntity departmentEntityB = new DepartmentEntity();
        departmentEntityB.setId(2L);
        departmentEntityB.setName("Department B");
        departmentEntityB.setMembers(List.of(departmentMemberOne, departmentMemberTwo));

        final DepartmentEntity departmentEntityC = new DepartmentEntity();
        departmentEntityC.setId(3L);
        departmentEntityC.setName("Department C");
        departmentEntityC.setMembers(List.of(departmentMemberTwo, departmentMemberThree));

        when(departmentRepository.findDistinctByMembersPersonIn(List.of(personOne, personTwo, personThree))).thenReturn(List.of(departmentEntityA, departmentEntityB, departmentEntityC));

        final Map<PersonId, List<String>> departmentsByMembers = sut.getDepartmentNamesByMembers(List.of(personOne, personTwo, personThree));
        assertThat(departmentsByMembers)
            .containsEntry(new PersonId(1L), List.of("Department A", "Department B"))
            .containsEntry(new PersonId(2L), List.of("Department C", "Department A", "Department B"))
            .containsEntry(new PersonId(3L), List.of("Department C"));
    }

    @Test
    void ensureDeletionOfMembershipOnPersonDeletionEvent() {
        final Person person = new Person();
        person.setId(42L);
        final Person other = new Person();
        other.setId(21L);

        final DepartmentEntity department = new DepartmentEntity();
        department.setId(1L);
        final DepartmentMemberEmbeddable personEmbeddable = new DepartmentMemberEmbeddable();
        personEmbeddable.setPerson(person);
        final DepartmentMemberEmbeddable otherEmbeddable = new DepartmentMemberEmbeddable();
        otherEmbeddable.setPerson(other);

        department.setMembers(List.of(personEmbeddable, otherEmbeddable));
        when(departmentRepository.findByMembersPerson(person)).thenReturn(List.of(department));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.save(department)).thenReturn(department);

        sut.deleteAssignedDepartmentsOfMember(new PersonDeletedEvent(person));

        verify(departmentRepository).findByMembersPerson(person);

        ArgumentCaptor<DepartmentEntity> argument = ArgumentCaptor.forClass(DepartmentEntity.class);
        verify(departmentRepository).save(argument.capture());
        assertThat(argument.getValue().getMembers()).containsExactly(otherEmbeddable);
    }

    @Test
    void ensureDeletionOfDepartmentHeadAssignmentOnPersonDeletionEvent() {
        final Person departmentHead = new Person();
        departmentHead.setId(42L);
        final Person other = new Person();
        other.setId(21L);

        final DepartmentEntity department = new DepartmentEntity();
        department.setId(1L);
        department.setDepartmentHeads(List.of(departmentHead, other));
        when(departmentRepository.findByDepartmentHeads(departmentHead)).thenReturn(List.of(department));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.save(department)).thenReturn(department);

        sut.deleteDepartmentHead(new PersonDeletedEvent(departmentHead));

        verify(departmentRepository).findByDepartmentHeads(departmentHead);

        ArgumentCaptor<DepartmentEntity> argument = ArgumentCaptor.forClass(DepartmentEntity.class);
        verify(departmentRepository).save(argument.capture());
        assertThat(argument.getValue().getDepartmentHeads()).containsExactly(other);
    }

    @Test
    void ensureDeletionOfSecondStageAuthorityAssignmentOnPersonDeletionEvent() {
        final Person secondStageAuthority = new Person();
        secondStageAuthority.setId(42L);
        final Person other = new Person();
        other.setId(21L);

        final DepartmentEntity department = new DepartmentEntity();
        department.setId(1L);
        department.setSecondStageAuthorities(List.of(secondStageAuthority, other));
        when(departmentRepository.findBySecondStageAuthorities(secondStageAuthority)).thenReturn(List.of(department));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.save(department)).thenReturn(department);

        sut.deleteSecondStageAuthority(new PersonDeletedEvent(secondStageAuthority));

        verify(departmentRepository).findBySecondStageAuthorities(secondStageAuthority);

        ArgumentCaptor<DepartmentEntity> argument = ArgumentCaptor.forClass(DepartmentEntity.class);
        verify(departmentRepository).save(argument.capture());
        assertThat(argument.getValue().getSecondStageAuthorities()).containsExactly(other);
    }

    @Test
    void ensureDepartmentMatchFalse() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final Person otherPerson = new Person();
        otherPerson.setId(2L);
        otherPerson.setPermissions(List.of(USER));

        final DepartmentMemberEmbeddable memberEmbeddable = new DepartmentMemberEmbeddable();
        memberEmbeddable.setPerson(person);

        final DepartmentMemberEmbeddable otherMemberEmbeddable = new DepartmentMemberEmbeddable();
        otherMemberEmbeddable.setPerson(otherPerson);

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(List.of(memberEmbeddable));

        final DepartmentEntity otherDepartmentEntity = new DepartmentEntity();
        otherDepartmentEntity.setId(2L);
        otherDepartmentEntity.setMembers(List.of(otherMemberEmbeddable));

        when(departmentRepository.findByMembersPerson(person)).thenReturn(List.of(departmentEntity));
        when(departmentRepository.findByMembersPerson(otherPerson)).thenReturn(List.of(otherDepartmentEntity));

        final boolean actual = sut.hasDepartmentMatch(person, otherPerson);
        assertThat(actual).isFalse();
    }

    @Test
    void ensureDepartmentMatchWhenBothAreMembers() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final Person otherPerson = new Person();
        otherPerson.setId(2L);
        otherPerson.setPermissions(List.of(USER));

        final DepartmentMemberEmbeddable memberEmbeddable = new DepartmentMemberEmbeddable();
        memberEmbeddable.setPerson(person);

        final DepartmentMemberEmbeddable otherMemberEmbeddable = new DepartmentMemberEmbeddable();
        otherMemberEmbeddable.setPerson(otherPerson);

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(List.of(memberEmbeddable, otherMemberEmbeddable));

        when(departmentRepository.findByMembersPerson(person)).thenReturn(List.of(departmentEntity));
        when(departmentRepository.findByMembersPerson(otherPerson)).thenReturn(List.of(departmentEntity));

        final boolean actual = sut.hasDepartmentMatch(person, otherPerson);
        assertThat(actual).isTrue();
    }

    @Test
    void ensureDepartmentMatchWhenPersonIsDepartmentHeadOfOtherPersonButNotMember() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final Person otherPerson = new Person();
        otherPerson.setId(2L);
        otherPerson.setPermissions(List.of(USER));

        final DepartmentMemberEmbeddable otherMemberEmbeddable = new DepartmentMemberEmbeddable();
        otherMemberEmbeddable.setPerson(otherPerson);

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(List.of(otherMemberEmbeddable));
        departmentEntity.setDepartmentHeads(List.of(person));

        when(departmentRepository.findByDepartmentHeadsOrSecondStageAuthorities(person, person))
            .thenReturn(List.of(departmentEntity));

        when(departmentRepository.findByMembersPerson(person)).thenReturn(List.of());
        when(departmentRepository.findByMembersPerson(otherPerson)).thenReturn(List.of(departmentEntity));

        final boolean actual = sut.hasDepartmentMatch(person, otherPerson);
        assertThat(actual).isTrue();
    }

    @Test
    void ensureDepartmentMatchWhenPersonIsSecondStageAuthorityOfOtherPersonButNotMember() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final Person otherPerson = new Person();
        otherPerson.setId(2L);
        otherPerson.setPermissions(List.of(USER));

        final DepartmentMemberEmbeddable otherMemberEmbeddable = new DepartmentMemberEmbeddable();
        otherMemberEmbeddable.setPerson(otherPerson);

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(List.of(otherMemberEmbeddable));
        departmentEntity.setSecondStageAuthorities(List.of(person));

        when(departmentRepository.findByDepartmentHeadsOrSecondStageAuthorities(person, person))
            .thenReturn(List.of(departmentEntity));

        when(departmentRepository.findByMembersPerson(person)).thenReturn(List.of());
        when(departmentRepository.findByMembersPerson(otherPerson)).thenReturn(List.of(departmentEntity));

        final boolean actual = sut.hasDepartmentMatch(person, otherPerson);
        assertThat(actual).isTrue();
    }

    @Test
    void ensureDepartmentMatchWhenOtherPersonIsDepartmentHeadOfPersonButNotMember() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final Person otherPerson = new Person();
        otherPerson.setId(2L);
        otherPerson.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final DepartmentMemberEmbeddable memberEmbeddable = new DepartmentMemberEmbeddable();
        memberEmbeddable.setPerson(person);

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(List.of(memberEmbeddable));
        departmentEntity.setDepartmentHeads(List.of(otherPerson));

        when(departmentRepository.findByDepartmentHeadsOrSecondStageAuthorities(otherPerson, otherPerson))
            .thenReturn(List.of(departmentEntity));

        when(departmentRepository.findByMembersPerson(person)).thenReturn(List.of(departmentEntity));
        when(departmentRepository.findByMembersPerson(otherPerson)).thenReturn(List.of());

        final boolean actual = sut.hasDepartmentMatch(person, otherPerson);
        assertThat(actual).isTrue();
    }

    @Test
    void ensureDepartmentMatchWhenOtherPersonIsSecondStageAuthorityOfPersonButNotMember() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final Person otherPerson = new Person();
        otherPerson.setId(2L);
        otherPerson.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final DepartmentMemberEmbeddable memberEmbeddable = new DepartmentMemberEmbeddable();
        memberEmbeddable.setPerson(person);

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setMembers(List.of(memberEmbeddable));
        departmentEntity.setSecondStageAuthorities(List.of(otherPerson));

        when(departmentRepository.findByDepartmentHeadsOrSecondStageAuthorities(otherPerson, otherPerson))
            .thenReturn(List.of(departmentEntity));

        when(departmentRepository.findByMembersPerson(person)).thenReturn(List.of(departmentEntity));
        when(departmentRepository.findByMembersPerson(otherPerson)).thenReturn(List.of());

        final boolean actual = sut.hasDepartmentMatch(person, otherPerson);
        assertThat(actual).isTrue();
    }

    private static PageableSearchQuery defaultPersonSearchQuery() {
        return new PageableSearchQuery(defaultPageRequest(), "");
    }

    private static PageRequest defaultPageRequest() {
        return PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "firstName"));
    }

    private static Person anyPerson(int id) {
        final Person person = new Person();
        person.setId((long) id);
        return person;
    }

    private static List<Person> anyPersons(int size, int firstPersonId) {
        final List<Integer> personIds = IntStream.range(firstPersonId, firstPersonId + size).boxed().toList();
        return IntStream.range(0, size).boxed().map(index -> anyPerson(personIds.get(index))).toList();
    }

    private static List<DepartmentMemberEmbeddable> anyDepartmentMembers(int size, int firstPersonId, Consumer<Person> personMutator) {
        return anyPersons(size, firstPersonId).stream()
            .map(DepartmentServiceImplTest::departmentMemberForPerson)
            .peek(departmentMemberEmbeddable -> personMutator.accept(departmentMemberEmbeddable.getPerson()))
            .toList();
    }

    private static DepartmentMemberEmbeddable departmentMemberForPerson(Person person) {
        final DepartmentMemberEmbeddable departmentMember = new DepartmentMemberEmbeddable();
        departmentMember.setPerson(person);
        return departmentMember;
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
