package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.department.DepartmentService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class ResponsiblePersonServiceImplTest {

    private ResponsiblePersonServiceImpl sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        sut = new ResponsiblePersonServiceImpl(personService, departmentService);
    }

    @Test
    void ensureToReturnResponsibleManagerWithDepartments() {

        // given person of interest
        final Person person = new Person("person", "person", "person", "person@example.org");

        // given department head
        final Person departmentHead = new Person("departmentHead", "departmentHead", "departmentHead", "departmentHead@example.org");
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getActivePersonsByRole(DEPARTMENT_HEAD)).thenReturn(List.of(departmentHead));
        when(departmentService.getDepartmentHeadsAllowedToManagePersons(List.of(departmentHead), List.of(person))).thenReturn(Map.of(person, List.of(departmentHead)));

        // given second stage
        final Person secondStage = new Person("secondStage", "secondStage", "secondStage", "secondStage@example.org");
        secondStage.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getActivePersonsByRole(SECOND_STAGE_AUTHORITY)).thenReturn(List.of(secondStage));
        when(departmentService.getSecondStageAuthoritiesAllowedToManagePersons(List.of(secondStage), List.of(person))).thenReturn(Map.of(person, List.of(secondStage)));

        final Person boss = new Person("boss", "boss", "senior", "boss@example.org");
        boss.setPermissions(List.of(USER, BOSS));
        when(personService.getActivePersonsByRole(BOSS)).thenReturn(List.of(boss));

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final List<Person> responsibleManagersOf = sut.getResponsibleManagersOf(person);
        assertThat(responsibleManagersOf)
            .containsExactly(departmentHead, secondStage, boss);
    }

    @Test
    void ensureToReturnOnlyBossAsResponsibleManagerWithoutDepartments() {

        // given person of interest
        final Person person = new Person("person", "person", "person", "person@example.org");

        final Person boss = new Person("boss", "boss", "senior", "boss@example.org");
        boss.setPermissions(List.of(USER, BOSS));
        when(personService.getActivePersonsByRole(BOSS)).thenReturn(List.of(boss));

        when(departmentService.getNumberOfDepartments()).thenReturn(0L);

        final List<Person> responsibleManagersOf = sut.getResponsibleManagersOf(person);
        assertThat(responsibleManagersOf)
            .containsExactly(boss);

        verify(personService, never()).getActivePersonsByRole(SECOND_STAGE_AUTHORITY);
        verify(personService, never()).getActivePersonsByRole(DEPARTMENT_HEAD);
    }

    @Test
    void ensureGetResponsibleDepartmentHeadsOfManyPersonsLoadsTheDepartmentHeadsOnce() {

        final Person person = new Person("person", "person", "person", "person@example.org");
        person.setId(1L);
        final Person departmentHead = new Person("departmentHead", "departmentHead", "departmentHead", "departmentHead@example.org");
        departmentHead.setId(2L);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        when(personService.getActivePersonsByRole(DEPARTMENT_HEAD)).thenReturn(List.of(departmentHead));
        // the department head is a member of the own department as well
        when(departmentService.getDepartmentHeadsAllowedToManagePersons(List.of(departmentHead), List.of(person, departmentHead)))
            .thenReturn(Map.of(person, List.of(departmentHead), departmentHead, List.of(departmentHead)));

        final Map<Person, List<Person>> responsibleDepartmentHeads = sut.getResponsibleDepartmentHeads(List.of(person, departmentHead));

        assertThat(responsibleDepartmentHeads)
            .containsEntry(person, List.of(departmentHead))
            .containsEntry(departmentHead, List.of());
        verify(personService).getActivePersonsByRole(DEPARTMENT_HEAD);
    }

    @Test
    void ensureGetResponsibleSecondStageAuthoritiesOfManyPersonsLoadsTheSecondStageAuthoritiesOnce() {

        final Person person = new Person("person", "person", "person", "person@example.org");
        person.setId(1L);
        final Person otherPerson = new Person("other", "other", "other", "other@example.org");
        otherPerson.setId(3L);
        final Person secondStage = new Person("secondStage", "secondStage", "secondStage", "secondStage@example.org");
        secondStage.setId(2L);
        secondStage.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        when(personService.getActivePersonsByRole(SECOND_STAGE_AUTHORITY)).thenReturn(List.of(secondStage));
        when(departmentService.getSecondStageAuthoritiesAllowedToManagePersons(List.of(secondStage), List.of(person, otherPerson)))
            .thenReturn(Map.of(person, List.of(secondStage), otherPerson, List.of()));

        final Map<Person, List<Person>> responsibleSecondStageAuthorities = sut.getResponsibleSecondStageAuthorities(List.of(person, otherPerson));

        assertThat(responsibleSecondStageAuthorities)
            .containsEntry(person, List.of(secondStage))
            .containsEntry(otherPerson, List.of());
        verify(personService).getActivePersonsByRole(SECOND_STAGE_AUTHORITY);
    }
}
