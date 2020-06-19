package org.synyx.urlaubsverwaltung.application.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_DEPARTMENTS;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createPerson;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationRecipientServiceTest {

    private ApplicationRecipientService sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;

    @Before
    public void setUp() {
        sut = new ApplicationRecipientService(personService, departmentService);
    }

    @Test
    public void testAllowUserApplicationWithSingleStageDepartment() {
        // given user application
        final Person normalUser = createPerson("normalUser", USER);
        final Application application = getHolidayApplication(normalUser);

        // given department head
        final Person departmentHead = createPerson("departmentHead", DEPARTMENT_HEAD);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(singletonList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, normalUser)).thenReturn(true);

        // given boss
        final Person boss = createPerson("boss", BOSS);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL)).thenReturn(singletonList(boss));

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).contains(departmentHead, boss);
    }

    @Test
    public void testAllowDepartmentHeadApplicationWithSingleStageDepartment() {
        // given department head
        Person departmentHead = createPerson("departmentHead", DEPARTMENT_HEAD);
        Application application = getHolidayApplication(departmentHead);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(singletonList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, departmentHead)).thenReturn(true);

        // given boss
        Person boss = createPerson("boss", BOSS);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL)).thenReturn(singletonList(boss));

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).doesNotContain(departmentHead).contains(boss);
    }

    @Test
    public void testAllowUserApplicationWithSecondStageDepartment() {
        // given user application
        Person normalUser = createPerson("normalUser", USER);
        Application application = getHolidayApplication(normalUser);

        // given department head
        Person departmentHead = createPerson("departmentHead", DEPARTMENT_HEAD);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(singletonList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, normalUser)).thenReturn(true);

        // given boss
        Person boss = createPerson("boss", BOSS);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL)).thenReturn(singletonList(boss));

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).hasSize(2).contains(departmentHead, boss);
    }

    @Test
    public void testAllowDepartmentHeadWithSecondStageDepartment() {
        // given department head
        Person departmentHead = createPerson("departmentHead", DEPARTMENT_HEAD);
        Application application = getHolidayApplication(departmentHead);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(singletonList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, departmentHead)).thenReturn(true);

        // given second stage
        Person secondStage = createPerson("secondStage", SECOND_STAGE_AUTHORITY);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_SECOND_STAGE_AUTHORITY)).thenReturn(singletonList(secondStage));
        when(departmentService.isSecondStageAuthorityOfPerson(secondStage, departmentHead)).thenReturn(true);

        // given boss
        Person boss = createPerson("boss", BOSS);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL)).thenReturn(singletonList(boss));

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).doesNotContain(departmentHead).contains(secondStage, boss);
    }

    @Test
    public void testAllowSecondStageApplicationWithSecondStageDepartment() {
        // given second stage
        Person secondStage = createPerson("secondStage", SECOND_STAGE_AUTHORITY);
        Application application = getHolidayApplication(secondStage);

        // given boss
        Person boss = createPerson("boss", BOSS);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL)).thenReturn(singletonList(boss));

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).hasSize(1).contains(boss);
    }

    /**
     * GIVEN:
     * Department 1:
     * Members: head1, secondStage
     * head: head1
     * secondStage: secondStage
     * <p>
     * Department 2:
     * Members: head1, head2, secondStage
     * head: head2
     * secondStage: secondStage
     * <p>
     * WHEN:
     * application head1
     * <p>
     * <p>
     * THEN:
     * recipient head2, secondStage
     */
    @Test
    public void testApplicationDepartmentHeadWithTwoDepartmentsWithDifferentRules() {
        Person head1 = createPerson("head1", DEPARTMENT_HEAD);
        Application application = getHolidayApplication(head1);

        Person head2 = createPerson("head2", DEPARTMENT_HEAD);
        Person secondStage = createPerson("secondStage", SECOND_STAGE_AUTHORITY);

        when(personService.getPersonsWithNotificationType(NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(asList(head1, head2));
        when(personService.getPersonsWithNotificationType(NOTIFICATION_SECOND_STAGE_AUTHORITY)).thenReturn(singletonList(secondStage));
        when(departmentService.isDepartmentHeadOfPerson(head1, head1)).thenReturn(true);
        when(departmentService.isDepartmentHeadOfPerson(head2, head1)).thenReturn(true);
        when(departmentService.isSecondStageAuthorityOfPerson(secondStage, head1)).thenReturn(true);

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).contains(head2, secondStage);
    }

    @Test
    public void testTemporaryAllowUserApplicationWithSecondStageDepartment() {
        // given user application
        Person normalUser = createPerson("normalUser", USER);
        Application application = getHolidayApplication(normalUser);

        // given second stage
        Person secondStage = createPerson("secondStage", SECOND_STAGE_AUTHORITY);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_SECOND_STAGE_AUTHORITY)).thenReturn(singletonList(secondStage));
        when(departmentService.isSecondStageAuthorityOfPerson(secondStage, normalUser)).thenReturn(true);

        List<Person> recipientsForTemporaryAllow = sut.getRecipientsForTemporaryAllow(application);

        assertThat(recipientsForTemporaryAllow).hasSize(1).contains(secondStage);
    }

    @Test
    public void testSendMailToBossOfDepartment() {

        Person normalUser = createPerson("normalUser", USER);
        Application application = getHolidayApplication(normalUser);

        Person boss = createPerson("boss1", BOSS);
        Person bossOfDepartment = createPerson("boss2", BOSS);

        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL)).thenReturn(singletonList(boss));
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_DEPARTMENTS)).thenReturn(singletonList(bossOfDepartment));

        Department department = new Department();
        department.setMembers(asList(normalUser, bossOfDepartment));

        when(departmentService.getAssignedDepartmentsOfMember(bossOfDepartment)).thenReturn(singletonList(department));
        when(departmentService.getAssignedDepartmentsOfMember(normalUser)).thenReturn(singletonList(department));

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).contains(boss, bossOfDepartment);
    }

    @Test
    public void testSendNoMailToBossOfOtherDepartment() {

        Person normalUser = createPerson("normalUser", USER);
        Application application = getHolidayApplication(normalUser);

        Person boss = createPerson("boss1", BOSS);
        Person bossOfDepartment = createPerson("boss2", BOSS);

        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL)).thenReturn(singletonList(boss));
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_DEPARTMENTS)).thenReturn(singletonList(bossOfDepartment));

        Department department = new Department();
        department.setMembers(asList(normalUser, bossOfDepartment));

        when(departmentService.getAssignedDepartmentsOfMember(bossOfDepartment)).thenReturn(emptyList());
        when(departmentService.getAssignedDepartmentsOfMember(normalUser)).thenReturn(singletonList(department));

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).contains(boss);
    }

    private Application getHolidayApplication(Person normalUser) {
        VacationType vacationType = DemoDataCreator.createVacationType(VacationCategory.HOLIDAY, "application.data.vacationType.holiday");
        return DemoDataCreator.createApplication(normalUser, vacationType);
    }
}
