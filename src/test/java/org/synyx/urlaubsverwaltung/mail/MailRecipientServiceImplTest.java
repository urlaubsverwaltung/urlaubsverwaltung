package org.synyx.urlaubsverwaltung.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.notification.UserNotificationSettings;
import org.synyx.urlaubsverwaltung.notification.UserNotificationSettingsService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.ResponsiblePersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class MailRecipientServiceImplTest {

    private MailRecipientServiceImpl sut;

    @Mock
    private ResponsiblePersonService responsiblePersonService;
    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private UserNotificationSettingsService userNotificationSettingsService;

    @BeforeEach
    void setUp() {
        sut = new MailRecipientServiceImpl(responsiblePersonService, personService, departmentService, userNotificationSettingsService);
    }

    @Test
    void ensureToMapBossesAndOfficesPersonsCorrectly() {

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        // given user
        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setId(1L);
        normalUser.setPermissions(List.of(USER));

        // given bossAndOffice
        final Person bossAndOffice = new Person("bossAndOffice", "bossAndOffice", "bossAndOffice", "bossAndOffice@example.org");
        bossAndOffice.setId(2L);
        bossAndOffice.setPermissions(List.of(USER, BOSS, OFFICE));
        bossAndOffice.setNotifications(List.of(NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED));
        when(personService.getActivePersonsByRole(BOSS)).thenReturn(List.of(bossAndOffice));
        when(personService.getActivePersonsByRole(OFFICE)).thenReturn(List.of(bossAndOffice));

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(normalUser, NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED);
        assertThat(recipientsForAllowAndRemind)
            .containsOnly(bossAndOffice);
    }

    @Test
    void getRecipientsOfInterestWithDepartmentsFilteredByEnabledMailNotification() {

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        // given user
        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setId(1L);
        normalUser.setPermissions(List.of(USER));

        // given boss all
        final Person bossAll = new Person("boss", "boss", "boss", "boss@example.org");
        bossAll.setId(2L);
        bossAll.setPermissions(List.of(USER, BOSS));
        bossAll.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));
        when(personService.getActivePersonsByRole(BOSS)).thenReturn(List.of(bossAll));

        // given department head
        final Person departmentHead = new Person("departmentHead", "departmentHead", "departmentHead", "departmentHead@example.org");
        departmentHead.setId(5L);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        departmentHead.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        // given second stage
        final Person secondStage = new Person("secondStage", "secondStage", "secondStage", "secondStage@example.org");
        secondStage.setId(6L);
        secondStage.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        secondStage.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Person secondStageWithoutMailNotification = new Person("secondStage", "secondStage", "secondStage", "secondStage@example.org");
        secondStageWithoutMailNotification.setId(7L);
        secondStageWithoutMailNotification.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        secondStageWithoutMailNotification.setNotifications(List.of());

        when(responsiblePersonService.getResponsibleDepartmentHeads(normalUser))
            .thenReturn(List.of(departmentHead));

        when(responsiblePersonService.getResponsibleSecondStageAuthorities(normalUser))
            .thenReturn(List.of(secondStage, secondStageWithoutMailNotification));

        final PersonId bossAllId = new PersonId(bossAll.getId());
        when(userNotificationSettingsService.findNotificationSettings(List.of(bossAllId)))
            .thenReturn(Map.of(bossAllId, new UserNotificationSettings(bossAllId, false)));

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(normalUser, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);
        assertThat(recipientsForAllowAndRemind)
            .doesNotContain(secondStageWithoutMailNotification)
            .containsOnly(bossAll, departmentHead, secondStage);
    }

    @Test
    void getRecipientsOfInterestDoesNotReturnOfficePersonWhenNotGloballyInterestedAndNotSameDepartment() {

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        // given user
        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setId(1L);
        normalUser.setPermissions(List.of(USER));

        // given office all
        final Person officeAll = new Person("office", "office", "office", "office@example.org");
        officeAll.setId(3L);
        officeAll.setPermissions(List.of(USER, OFFICE));
        officeAll.setNotifications(List.of(NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED));
        when(personService.getActivePersonsByRole(OFFICE)).thenReturn(List.of(officeAll));

        when(departmentService.hasDepartmentMatch(officeAll, normalUser)).thenReturn(false);

        final PersonId officeAllId = new PersonId(officeAll.getId());

        when(userNotificationSettingsService.findNotificationSettings(List.of(officeAllId)))
            .thenReturn(Map.of(officeAllId, new UserNotificationSettings(officeAllId, true)));

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(normalUser, NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED);
        assertThat(recipientsForAllowAndRemind).isEmpty();
    }

    @Test
    void getRecipientsOfInterestDoesReturnOfficePersonNotGloballyInterestedButDepartmentHead() {

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        // given user
        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setId(1L);
        normalUser.setPermissions(List.of(USER));

        // given office all
        final Person officeAll = new Person("office", "office", "office", "office@example.org");
        officeAll.setId(3L);
        officeAll.setPermissions(List.of(USER, OFFICE, DEPARTMENT_HEAD));
        officeAll.setNotifications(List.of(NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED));
        when(personService.getActivePersonsByRole(OFFICE)).thenReturn(List.of(officeAll));

        when(responsiblePersonService.getResponsibleDepartmentHeads(normalUser))
            .thenReturn(List.of(officeAll));

        when(departmentService.hasDepartmentMatch(officeAll, normalUser)).thenReturn(true);

        final PersonId officeAllId = new PersonId(officeAll.getId());

        when(userNotificationSettingsService.findNotificationSettings(List.of(officeAllId)))
            .thenReturn(Map.of(officeAllId, new UserNotificationSettings(officeAllId, true)));

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(normalUser, NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED);
        assertThat(recipientsForAllowAndRemind).containsExactly(officeAll);
    }

    @Test
    void getRecipientsOfInterestWithoutDepartments() {

        when(departmentService.getNumberOfDepartments()).thenReturn(0L);

        // given user
        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setId(1L);

        // given boss all
        final Person bossAll = new Person("boss", "boss", "boss", "boss@example.org");
        bossAll.setId(2L);
        bossAll.setPermissions(List.of(USER, BOSS));
        bossAll.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));
        when(personService.getActivePersonsByRole(BOSS)).thenReturn(List.of(bossAll));

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(normalUser, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);
        assertThat(recipientsForAllowAndRemind).containsOnly(bossAll);

        verifyNoInteractions(userNotificationSettingsService);
    }

    @Test
    void getRecipientsOfInterestWithDepartmentsAndDistinctRecipients() {

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        // given user
        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setId(1L);

        // given boss all
        final Person bossAll = new Person("boss", "boss", "boss", "boss@example.org");
        bossAll.setId(2L);
        bossAll.setPermissions(List.of(USER, BOSS));
        bossAll.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));
        when(personService.getActivePersonsByRole(BOSS)).thenReturn(List.of(bossAll));

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(normalUser, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);
        assertThat(recipientsForAllowAndRemind).containsOnly(bossAll);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureToCallDatabaseForPersonsWithRoleIfNotificationIsValidForGivenRole(final Role role) {

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        // given user
        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setId(1L);

        sut.getRecipientsOfInterest(normalUser, NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED);
        verify(personService).getActivePersonsByRole(role);
    }

    @Test
    void ensureToCallDatabaseForPersonsWithRoleIfNotificationIsValidForGivenRoleDepartmentHead() {

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        // given user
        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setId(1L);

        when(responsiblePersonService.getResponsibleDepartmentHeads(normalUser)).thenReturn(List.of());

        sut.getRecipientsOfInterest(normalUser, NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED);

        verify(responsiblePersonService).getResponsibleDepartmentHeads(normalUser);
        verify(personService, never()).getActivePersonsByRole(DEPARTMENT_HEAD);
    }

    @Test
    void ensureToCallDatabaseForPersonsWithRoleIfNotificationIsValidForGivenRoleSecondStageAuthority() {

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        // given user
        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setId(1L);

        when(responsiblePersonService.getResponsibleDepartmentHeads(normalUser)).thenReturn(List.of());

        sut.getRecipientsOfInterest(normalUser, NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED);

        verify(responsiblePersonService).getResponsibleSecondStageAuthorities(normalUser);
        verify(personService, never()).getActivePersonsByRole(SECOND_STAGE_AUTHORITY);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureToNotCallDatabaseForPersonsWithoutRoleIfNotificationIsNotValidForDepartmentRoles(final Role role) {

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        // given user
        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setId(1L);

        sut.getRecipientsOfInterest(normalUser, NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL);
        verify(personService, never()).getActivePersonsByRole(role);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE"})
    void ensureToNotCallDatabaseForPersonsWithoutRoleIfNotificationIsNotValidForRoleOrganisationRoles(final Role role) {

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        // given user
        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setId(1L);

        sut.getRecipientsOfInterest(normalUser, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);
        verify(personService, never()).getActivePersonsByRole(role);
    }

    @Test
    void getRecipientsOfInterestWithDepartmentsAndApplicationCancellationRequestedFilteredByEnabledMailNotification() {

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        // given user
        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setId(1L);
        normalUser.setPermissions(List.of(USER));

        // given office
        final Person office = new Person("office", "office", "office", "office@example.org");
        office.setId(2L);
        office.setPermissions(List.of(USER, OFFICE));
        office.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED));
        when(personService.getActivePersonsByRole(OFFICE)).thenReturn(List.of(office));

        // given department head with application cancellation requested permission
        final Person departmentHeadWithApplicationCancellationRequested = new Person("departmentHead", "departmentHead", "departmentHead", "departmentHead@example.org");
        departmentHeadWithApplicationCancellationRequested.setId(5L);
        departmentHeadWithApplicationCancellationRequested.setPermissions(List.of(USER, DEPARTMENT_HEAD, APPLICATION_CANCELLATION_REQUESTED));
        departmentHeadWithApplicationCancellationRequested.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED));

        // given department head without application cancellation requested permission
        final Person departmentHeadWithoutApplicationCancellationRequested = new Person("departmentHead", "departmentHead", "departmentHead", "departmentHead@example.org");
        departmentHeadWithoutApplicationCancellationRequested.setId(6L);
        departmentHeadWithoutApplicationCancellationRequested.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        departmentHeadWithoutApplicationCancellationRequested.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED));

        // given second stage with application cancellation requested permission
        final Person secondStageWithApplicationCancellationRequested = new Person("secondStage", "secondStage", "secondStage", "secondStage@example.org");
        secondStageWithApplicationCancellationRequested.setId(7L);
        secondStageWithApplicationCancellationRequested.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, APPLICATION_CANCELLATION_REQUESTED));
        secondStageWithApplicationCancellationRequested.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED));

        // given second stage without application cancellation requested permission
        final Person secondStageWithoutApplicationCancellationRequested = new Person("secondStage", "secondStage", "secondStage", "secondStage@example.org");
        secondStageWithoutApplicationCancellationRequested.setId(8L);
        secondStageWithoutApplicationCancellationRequested.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        secondStageWithoutApplicationCancellationRequested.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED));

        final Person secondStageWithoutMailNotification = new Person("secondStage", "secondStage", "secondStage", "secondStage@example.org");
        secondStageWithoutMailNotification.setId(9L);
        secondStageWithoutMailNotification.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, APPLICATION_CANCELLATION_REQUESTED));
        secondStageWithoutMailNotification.setNotifications(List.of());

        when(responsiblePersonService.getResponsibleDepartmentHeads(normalUser))
            .thenReturn(List.of(departmentHeadWithApplicationCancellationRequested, departmentHeadWithoutApplicationCancellationRequested));

        when(responsiblePersonService.getResponsibleSecondStageAuthorities(normalUser))
            .thenReturn(List.of(secondStageWithApplicationCancellationRequested, secondStageWithoutApplicationCancellationRequested, secondStageWithoutMailNotification));

        final PersonId bossAllId = new PersonId(office.getId());
        when(userNotificationSettingsService.findNotificationSettings(List.of(bossAllId)))
            .thenReturn(Map.of(bossAllId, new UserNotificationSettings(bossAllId, false)));

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(normalUser, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED);
        assertThat(recipientsForAllowAndRemind)
            .doesNotContain(departmentHeadWithoutApplicationCancellationRequested, secondStageWithoutApplicationCancellationRequested, secondStageWithoutMailNotification)
            .containsOnly(office, departmentHeadWithApplicationCancellationRequested, secondStageWithApplicationCancellationRequested);
    }

    @Test
    void ensureToGetAllActiveColleaguesWithDepartmentsWithoutDHAndSSA() {

        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setPermissions(List.of(USER));
        normalUser.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED));
        normalUser.setId(1L);

        final Person colleague = new Person("colleague", "colleague", "colleague", "colleague@example.org");
        colleague.setPermissions(List.of(USER));
        colleague.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED));
        colleague.setId(4L);

        final Person departmentHead = new Person("departmentHead", "departmentHead", "departmentHead", "departmentHead@example.org");
        departmentHead.setId(2L);

        final Person secondStageAuthority = new Person("secondStageAuthority", "secondStageAuthority", "secondStageAuthority", "secondStageAuthority@example.org");
        secondStageAuthority.setId(1L);

        final Department department = new Department();
        department.setMembers(List.of(normalUser, colleague, departmentHead));
        department.setDepartmentHeads(List.of(departmentHead));
        department.setSecondStageAuthorities(List.of(secondStageAuthority));

        when(departmentService.getAssignedDepartmentsOfMember(normalUser)).thenReturn(List.of(department));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final List<Person> colleagues = sut.getColleagues(normalUser, NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED);
        assertThat(colleagues).containsExactly(colleague);
    }

    @Test
    void ensureToGetAllActiveColleaguesWithoutDepartments() {

        final Person normalUser = new Person("normalUser", "normalUser", "normalUser", "normalUser@example.org");
        normalUser.setPermissions(List.of(USER));
        normalUser.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED));
        normalUser.setId(1L);

        final Person colleague = new Person("colleague", "colleague", "colleague", "colleague@example.org");
        colleague.setPermissions(List.of(USER));
        colleague.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED));
        colleague.setId(4L);

        final Person colleagueWithoutMailNotification = new Person("colleagueWithoutMailNotification", "colleagueWithoutMailNotification", "colleagueWithoutMailNotification", "colleagueWithoutMailNotification@example.org");
        colleagueWithoutMailNotification.setPermissions(List.of(USER));
        colleagueWithoutMailNotification.setNotifications(List.of());
        colleagueWithoutMailNotification.setId(5L);

        when(personService.getActivePersons()).thenReturn(List.of(normalUser, colleague, colleagueWithoutMailNotification));
        when(departmentService.getNumberOfDepartments()).thenReturn(0L);

        final List<Person> colleagues = sut.getColleagues(normalUser, NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED);
        assertThat(colleagues).containsExactly(colleague);
    }
}
