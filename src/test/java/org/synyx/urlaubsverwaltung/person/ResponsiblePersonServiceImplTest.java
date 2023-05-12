package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.department.DepartmentService;

import java.util.List;

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
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(true);

        // given second stage
        final Person secondStage = new Person("secondStage", "secondStage", "secondStage", "secondStage@example.org");
        secondStage.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getActivePersonsByRole(SECOND_STAGE_AUTHORITY)).thenReturn(List.of(secondStage));
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStage, person)).thenReturn(true);

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
}
