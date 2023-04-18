package org.synyx.urlaubsverwaltung.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class MailRecipientServiceImplTest {

    private MailRecipientServiceImpl sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        sut = new MailRecipientServiceImpl(personService, departmentService);
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

    @Test
    void getRecipientsOfInterestWithDepartmentsFilteredByEnabledMailNotification() {

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final Department department = new Department();
        department.setId(1);

        // given user application
        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setId(1);
        normalUser.setPermissions(List.of(USER));
        when(departmentService.getAssignedDepartmentsOfMember(normalUser)).thenReturn(List.of(department));

        // given office all
        final Person officeAll = new Person("office", "office", "office", "office@example.org");
        officeAll.setId(3);
        officeAll.setPermissions(List.of(USER, OFFICE));
        officeAll.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL));
        when(personService.getActivePersonsByRole(OFFICE)).thenReturn(List.of(officeAll));

        // given boss all
        final Person bossAll = new Person("boss", "boss", "boss", "boss@example.org");
        bossAll.setId(2);
        bossAll.setPermissions(List.of(USER, BOSS));
        bossAll.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL));

        // given boss department
        final Person bossDepartment = new Person("boss", "boss", "boss", "boss@example.org");
        bossDepartment.setId(4);
        bossDepartment.setPermissions(List.of(USER, BOSS));
        bossDepartment.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));
        when(personService.getActivePersonsByRole(BOSS)).thenReturn(List.of(bossAll)).thenReturn(List.of(bossDepartment));
        when(departmentService.getAssignedDepartmentsOfMember(bossDepartment)).thenReturn(List.of(department));

        // given department head
        final Person departmentHead = new Person("departmentHead", "departmentHead", "departmentHead", "departmentHead@example.org");
        departmentHead.setId(5);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        departmentHead.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));
        when(personService.getActivePersonsByRole(DEPARTMENT_HEAD)).thenReturn(List.of(departmentHead));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, normalUser)).thenReturn(true);

        // given second stage
        final Person secondStage = new Person("secondStage", "secondStage", "secondStage", "secondStage@example.org");
        secondStage.setId(6);
        secondStage.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        secondStage.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStage, normalUser)).thenReturn(true);

        final Person secondStageWithoutMailtNotification = new Person("secondStage", "secondStage", "secondStage", "secondStage@example.org");
        secondStageWithoutMailtNotification.setId(7);
        secondStageWithoutMailtNotification.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        secondStageWithoutMailtNotification.setNotifications(List.of());
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStageWithoutMailtNotification, normalUser)).thenReturn(true);

        when(personService.getActivePersonsByRole(SECOND_STAGE_AUTHORITY)).thenReturn(List.of(secondStage, secondStageWithoutMailtNotification));

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(normalUser, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);
        assertThat(recipientsForAllowAndRemind)
            .doesNotContain(secondStageWithoutMailtNotification)
            .containsOnly(officeAll, bossAll, bossDepartment, departmentHead, secondStage);
    }

    @Test
    void getRecipientsOfInterestWithoutDepartments() {

        when(departmentService.getNumberOfDepartments()).thenReturn(0L);

        // given user application
        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setId(1);

        // given office all
        when(personService.getActivePersonsByRole(OFFICE)).thenReturn(List.of());

        // given boss all
        final Person bossAll = new Person("boss", "boss", "boss", "boss@example.org");
        bossAll.setId(2);
        bossAll.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL));
        when(personService.getActivePersonsByRole(BOSS)).thenReturn(List.of(bossAll));

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(normalUser, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);
        assertThat(recipientsForAllowAndRemind).containsOnly(bossAll);
    }

    @Test
    void getRecipientsOfInterestWithDepartmentsAndDistinctRecipients() {

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final Department department = new Department();
        department.setId(1);

        // given user application
        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setId(1);
        when(departmentService.getAssignedDepartmentsOfMember(normalUser)).thenReturn(List.of(department));

        // given boss all
        final Person bossAll = new Person("boss", "boss", "boss", "boss@example.org");
        bossAll.setId(2);
        bossAll.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));
        when(personService.getActivePersonsByRole(BOSS)).thenReturn(List.of(bossAll));

        // given office all
        when(personService.getActivePersonsByRole(OFFICE)).thenReturn(List.of());

        // given boss department
        when(personService.getActivePersonsByRole(BOSS)).thenReturn(List.of(bossAll));
        when(departmentService.getAssignedDepartmentsOfMember(bossAll)).thenReturn(List.of(department));

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(normalUser, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);
        assertThat(recipientsForAllowAndRemind).containsOnly(bossAll);
    }
}
