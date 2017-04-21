package org.synyx.urlaubsverwaltung.core.mail;

import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecipientServiceTest {

    RecipientService sut;
    private PersonService personService;
    private DepartmentService departmentService;

    @Before
    public void setUp() throws Exception {
        personService = mock(PersonService.class);
        departmentService = mock(DepartmentService.class);
        sut = new RecipientService(personService, departmentService);
    }

    @Test
    public void testAllowUserApplicationWithSingleStageDepartment() throws Exception {
        // given user application
        Person normalUser = TestDataCreator.createPerson("normalUser", Role.USER);
        Application application = getHolidayApplication(normalUser);

        // given department head
        Person departmentHead = TestDataCreator.createPerson("departmentHead", Role.DEPARTMENT_HEAD);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(Arrays.asList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, normalUser)).thenReturn(true);

        // given boss
        Person boss = TestDataCreator.createPerson("boss", Role.BOSS);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS)).thenReturn(Arrays.asList(boss));

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).contains(departmentHead, boss);
    }

    @Test
    public void testAllowDepartmentHeadApplicationWithSingleStageDepartment() throws Exception {
        // given department head
        Person departmentHead = TestDataCreator.createPerson("departmentHead", Role.DEPARTMENT_HEAD);
        Application application = getHolidayApplication(departmentHead);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(Arrays.asList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, departmentHead)).thenReturn(true);

        // given boss
        Person boss = TestDataCreator.createPerson("boss", Role.BOSS);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS)).thenReturn(Arrays.asList(boss));

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).doesNotContain(departmentHead).contains(boss);
    }

    @Test
    public void testAllowUserApplicationWithSecondStageDepartment() throws Exception {
        // given user application
        Person normalUser = TestDataCreator.createPerson("normalUser", Role.USER);
        Application application = getHolidayApplication(normalUser);

        // given department head
        Person departmentHead = TestDataCreator.createPerson("departmentHead", Role.DEPARTMENT_HEAD);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(Arrays.asList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, normalUser)).thenReturn(true);

        // given second stage
        Person secondStage = TestDataCreator.createPerson("secondStage", Role.SECOND_STAGE_AUTHORITY);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY)).thenReturn(Arrays.asList(secondStage));
        when(departmentService.isSecondStageAuthorityOfPerson(secondStage, normalUser)).thenReturn(true);

        // given boss
        Person boss = TestDataCreator.createPerson("boss", Role.BOSS);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS)).thenReturn(Arrays.asList(boss));

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).doesNotContain(secondStage).contains(departmentHead, boss);
    }

    @Test
    public void testAllowDepartmentHeadWithSecondStageDepartment() throws Exception {
        // given department head
        Person departmentHead = TestDataCreator.createPerson("departmentHead", Role.DEPARTMENT_HEAD);
        Application application = getHolidayApplication(departmentHead);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(Arrays.asList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, departmentHead)).thenReturn(true);

        // given second stage
        Person secondStage = TestDataCreator.createPerson("secondStage", Role.SECOND_STAGE_AUTHORITY);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY)).thenReturn(Arrays.asList(secondStage));
        when(departmentService.isSecondStageAuthorityOfPerson(secondStage, departmentHead)).thenReturn(true);

        // given boss
        Person boss = TestDataCreator.createPerson("boss", Role.BOSS);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS)).thenReturn(Arrays.asList(boss));

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).doesNotContain(departmentHead).contains(secondStage, boss);
    }

    @Test
    public void testAllowSecondStageApplicationWithSecondStageDepartment() throws Exception {
        // given second stage
        Person secondStage = TestDataCreator.createPerson("secondStage", Role.SECOND_STAGE_AUTHORITY);
        Application application = getHolidayApplication(secondStage);

        // given department head
        Person departmentHead = TestDataCreator.createPerson("departmentHead", Role.DEPARTMENT_HEAD);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(Arrays.asList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, secondStage)).thenReturn(true);

        // given boss
        Person boss = TestDataCreator.createPerson("boss", Role.BOSS);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS)).thenReturn(Arrays.asList(boss));

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
    public void testApplicationDepartmentHeadWithTwoDepartmentsWithDifferentRules() throws Exception {
        Person head1 = TestDataCreator.createPerson("head1", Role.DEPARTMENT_HEAD);
        Application application = getHolidayApplication(head1);

        Person head2 = TestDataCreator.createPerson("head2", Role.DEPARTMENT_HEAD);
        Person secondStage = TestDataCreator.createPerson("secondStage", Role.SECOND_STAGE_AUTHORITY);

        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(Arrays.asList(head1, head2));
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY)).thenReturn(Collections.singletonList(secondStage));
        when(departmentService.isDepartmentHeadOfPerson(head1, head1)).thenReturn(true);
        when(departmentService.isDepartmentHeadOfPerson(head2, head1)).thenReturn(true);
        when(departmentService.isSecondStageAuthorityOfPerson(secondStage, head1)).thenReturn(true);

        List<Person> recipientsForAllowAndRemind = sut.getRecipientsForAllowAndRemind(application);

        assertThat(recipientsForAllowAndRemind).contains(head2, secondStage);
    }

    @Test
    public void testTemporaryAllowUserApplicationWithSecondStageDepartment() throws Exception {
        // given user application
        Person normalUser = TestDataCreator.createPerson("normalUser", Role.USER);
        Application application = getHolidayApplication(normalUser);

        // given department head
        Person departmentHead = TestDataCreator.createPerson("departmentHead", Role.DEPARTMENT_HEAD);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(Arrays.asList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, normalUser)).thenReturn(true);

        // given second stage
        Person secondStage = TestDataCreator.createPerson("secondStage", Role.SECOND_STAGE_AUTHORITY);
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY)).thenReturn(Arrays.asList(secondStage));
        when(departmentService.isSecondStageAuthorityOfPerson(secondStage, normalUser)).thenReturn(true);

        List<Person> recipientsForTemporaryAllow = sut.getRecipientsForTemporaryAllow(application);

        assertThat(recipientsForTemporaryAllow).contains(secondStage).doesNotContain(departmentHead);
    }

    private Application getHolidayApplication(Person normalUser) {
        VacationType vacationType = TestDataCreator.createVacationType(VacationCategory.HOLIDAY, "Erholungsurlaub");
        return TestDataCreator.createApplication(normalUser, vacationType);
    }
}