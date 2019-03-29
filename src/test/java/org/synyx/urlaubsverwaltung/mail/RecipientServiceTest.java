package org.synyx.urlaubsverwaltung.mail;

import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecipientServiceTest {

    RecipientService sut;
    private PersonService personService;
    private DepartmentService departmentService;

    @Before
    public void setUp() {
        personService = mock(PersonService.class);
        departmentService = mock(DepartmentService.class);
        sut = new RecipientService(personService, departmentService);
    }

    @Test
    public void testAllowUserApplicationWithSingleStageDepartment() {
        // given user application
        Person normalUser = TestDataCreator.createPerson("normalUser", Role.USER);
        Application application = getHolidayApplication(normalUser);

        // given department head
        Person departmentHead = TestDataCreator.createPerson("departmentHead", Role.DEPARTMENT_HEAD);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(singletonList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, normalUser)).thenReturn(true);

        // given boss
        Person boss = TestDataCreator.createPerson("boss", Role.BOSS);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS)).thenReturn(singletonList(boss));

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).contains(departmentHead, boss);
    }

    @Test
    public void testAllowDepartmentHeadApplicationWithSingleStageDepartment() {
        // given department head
        Person departmentHead = TestDataCreator.createPerson("departmentHead", Role.DEPARTMENT_HEAD);
        Application application = getHolidayApplication(departmentHead);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(singletonList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, departmentHead)).thenReturn(true);

        // given boss
        Person boss = TestDataCreator.createPerson("boss", Role.BOSS);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS)).thenReturn(singletonList(boss));

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).doesNotContain(departmentHead).contains(boss);
    }

    @Test
    public void testAllowUserApplicationWithSecondStageDepartment() {
        // given user application
        Person normalUser = TestDataCreator.createPerson("normalUser", Role.USER);
        Application application = getHolidayApplication(normalUser);

        // given department head
        Person departmentHead = TestDataCreator.createPerson("departmentHead", Role.DEPARTMENT_HEAD);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(singletonList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, normalUser)).thenReturn(true);

        // given second stage
        Person secondStage = TestDataCreator.createPerson("secondStage", Role.SECOND_STAGE_AUTHORITY);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY)).thenReturn(singletonList(secondStage));
        when(departmentService.isSecondStageAuthorityOfPerson(secondStage, normalUser)).thenReturn(true);

        // given boss
        Person boss = TestDataCreator.createPerson("boss", Role.BOSS);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS)).thenReturn(singletonList(boss));

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).doesNotContain(secondStage).contains(departmentHead, boss);
    }

    @Test
    public void testAllowDepartmentHeadWithSecondStageDepartment() {
        // given department head
        Person departmentHead = TestDataCreator.createPerson("departmentHead", Role.DEPARTMENT_HEAD);
        Application application = getHolidayApplication(departmentHead);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(singletonList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, departmentHead)).thenReturn(true);

        // given second stage
        Person secondStage = TestDataCreator.createPerson("secondStage", Role.SECOND_STAGE_AUTHORITY);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY)).thenReturn(singletonList(secondStage));
        when(departmentService.isSecondStageAuthorityOfPerson(secondStage, departmentHead)).thenReturn(true);

        // given boss
        Person boss = TestDataCreator.createPerson("boss", Role.BOSS);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS)).thenReturn(singletonList(boss));

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).doesNotContain(departmentHead).contains(secondStage, boss);
    }

    @Test
    public void testAllowSecondStageApplicationWithSecondStageDepartment() {
        // given second stage
        Person secondStage = TestDataCreator.createPerson("secondStage", Role.SECOND_STAGE_AUTHORITY);
        Application application = getHolidayApplication(secondStage);

        // given department head
        Person departmentHead = TestDataCreator.createPerson("departmentHead", Role.DEPARTMENT_HEAD);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(singletonList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, secondStage)).thenReturn(true);

        // given boss
        Person boss = TestDataCreator.createPerson("boss", Role.BOSS);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS)).thenReturn(singletonList(boss));

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).doesNotContain(departmentHead, secondStage).contains(boss);
    }

    /**
     * GIVEN:
     * Department 1:
     * Members: head1, secondStage
     * head: head1
     * secondStage: secondStage
     *
     * Department 2:
     * Members: head1, head2, secondStage
     * head: head2
     * secondStage: secondStage
     *
     * WHEN:
     * application head1
     *
     * THEN:
     * recipient head2, secondStage
     */
    @Test
    public void testApplicationDepartmentHeadWithTwoDepartmentsWithDifferentRules() {
        Person head1 = TestDataCreator.createPerson("head1", Role.DEPARTMENT_HEAD);
        Application application = getHolidayApplication(head1);

        Person head2 = TestDataCreator.createPerson("head2", Role.DEPARTMENT_HEAD);
        Person secondStage = TestDataCreator.createPerson("secondStage", Role.SECOND_STAGE_AUTHORITY);

        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(Arrays.asList(head1, head2));
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY)).thenReturn(singletonList(secondStage));
        when(departmentService.isDepartmentHeadOfPerson(head1, head1)).thenReturn(true);
        when(departmentService.isDepartmentHeadOfPerson(head2, head1)).thenReturn(true);
        when(departmentService.isSecondStageAuthorityOfPerson(secondStage, head1)).thenReturn(true);

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).contains(head2, secondStage);
    }

    @Test
    public void testTemporaryAllowUserApplicationWithSecondStageDepartment() {
        // given user application
        Person normalUser = TestDataCreator.createPerson("normalUser", Role.USER);
        Application application = getHolidayApplication(normalUser);

        // given department head
        Person departmentHead = TestDataCreator.createPerson("departmentHead", Role.DEPARTMENT_HEAD);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(singletonList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, normalUser)).thenReturn(true);

        // given second stage
        Person secondStage = TestDataCreator.createPerson("secondStage", Role.SECOND_STAGE_AUTHORITY);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY)).thenReturn(singletonList(secondStage));
        when(departmentService.isSecondStageAuthorityOfPerson(secondStage, normalUser)).thenReturn(true);

        List<Person> recipientsForTemporaryAllow = sut.getRecipientsForTemporaryAllow(application);

        assertThat(recipientsForTemporaryAllow).contains(secondStage).doesNotContain(departmentHead);
    }

    private Application getHolidayApplication(Person normalUser) {
        VacationType vacationType = TestDataCreator.createVacationType(VacationCategory.HOLIDAY, "application.data.vacationType.holiday");
        return TestDataCreator.createApplication(normalUser, vacationType);
    }
}
