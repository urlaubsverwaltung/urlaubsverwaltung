package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationRecipientService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_DEPARTMENTS;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class ApplicationRecipientServiceTest {

    private ApplicationRecipientService sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        sut = new ApplicationRecipientService(personService, departmentService);
    }

    @Test
    void testAllowUserApplicationWithSingleStageDepartment() {
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
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_DEPARTMENTS)).thenReturn(emptyList());


        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(application);
        assertThat(recipientsForAllowAndRemind).contains(departmentHead, boss);
    }

    @Test
    void testAllowDepartmentHeadApplicationWithSingleStageDepartment() {
        // given department head
        final Person departmentHead = createPerson("departmentHead", DEPARTMENT_HEAD);
        final Application application = getHolidayApplication(departmentHead);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(singletonList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, departmentHead)).thenReturn(true);

        when(personService.getPersonsWithNotificationType(NOTIFICATION_SECOND_STAGE_AUTHORITY)).thenReturn(emptyList());

        // given boss
        final Person boss = createPerson("boss", BOSS);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL)).thenReturn(singletonList(boss));
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_DEPARTMENTS)).thenReturn(emptyList());

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(application);
        assertThat(recipientsForAllowAndRemind).doesNotContain(departmentHead).contains(boss);
    }

    @Test
    void testAllowUserApplicationWithSecondStageDepartment() {
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
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_DEPARTMENTS)).thenReturn(emptyList());

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(application);
        assertThat(recipientsForAllowAndRemind).hasSize(2).contains(departmentHead, boss);
    }

    @Test
    void testAllowDepartmentHeadWithSecondStageDepartment() {
        // given department head
        final Person departmentHead = createPerson("departmentHead", DEPARTMENT_HEAD);
        final Application application = getHolidayApplication(departmentHead);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(singletonList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(departmentHead, departmentHead)).thenReturn(true);

        // given second stage
        final Person secondStage = createPerson("secondStage", SECOND_STAGE_AUTHORITY);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_SECOND_STAGE_AUTHORITY)).thenReturn(singletonList(secondStage));
        when(departmentService.isSecondStageAuthorityOfPerson(secondStage, departmentHead)).thenReturn(true);

        // given boss
        final Person boss = createPerson("boss", BOSS);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL)).thenReturn(singletonList(boss));
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_DEPARTMENTS)).thenReturn(emptyList());

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(application);
        assertThat(recipientsForAllowAndRemind).doesNotContain(departmentHead).contains(secondStage, boss);
    }

    @Test
    void testAllowSecondStageApplicationWithSecondStageDepartment() {
        // given second stage
        final Person secondStage = createPerson("secondStage", SECOND_STAGE_AUTHORITY);
        final Application application = getHolidayApplication(secondStage);

        // given boss
        final Person boss = createPerson("boss", BOSS);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL)).thenReturn(singletonList(boss));

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(application);
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
    void testApplicationDepartmentHeadWithTwoDepartmentsWithDifferentRules() {
        final Person head1 = createPerson("head1", DEPARTMENT_HEAD);
        final Application application = getHolidayApplication(head1);

        final Person head2 = createPerson("head2", DEPARTMENT_HEAD);
        final Person secondStage = createPerson("secondStage", SECOND_STAGE_AUTHORITY);

        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL)).thenReturn(emptyList());
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_DEPARTMENTS)).thenReturn(emptyList());
        when(personService.getPersonsWithNotificationType(NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(asList(head1, head2));
        when(personService.getPersonsWithNotificationType(NOTIFICATION_SECOND_STAGE_AUTHORITY)).thenReturn(singletonList(secondStage));
        when(departmentService.isDepartmentHeadOfPerson(head1, head1)).thenReturn(true);
        when(departmentService.isDepartmentHeadOfPerson(head2, head1)).thenReturn(true);
        when(departmentService.isSecondStageAuthorityOfPerson(secondStage, head1)).thenReturn(true);

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(application);
        assertThat(recipientsForAllowAndRemind).contains(head2, secondStage);
    }

    @Test
    void testTemporaryAllowUserApplicationWithSecondStageDepartment() {
        // given user application
        final Person normalUser = createPerson("normalUser", USER);
        final Application application = getHolidayApplication(normalUser);

        // given second stage
        final Person secondStage = createPerson("secondStage", SECOND_STAGE_AUTHORITY);
        when(personService.getPersonsWithNotificationType(NOTIFICATION_SECOND_STAGE_AUTHORITY)).thenReturn(singletonList(secondStage));
        when(departmentService.isSecondStageAuthorityOfPerson(secondStage, normalUser)).thenReturn(true);

        final List<Person> recipientsForTemporaryAllow = sut.getRecipientsForTemporaryAllow(application);
        assertThat(recipientsForTemporaryAllow).hasSize(1).contains(secondStage);
    }

    @Test
    void testSendMailToBossOfDepartment() {

        final Person normalUser = createPerson("normalUser", USER);
        final Application application = getHolidayApplication(normalUser);

        final Person boss = createPerson("boss1", BOSS);
        final Person bossOfDepartment = createPerson("boss2", BOSS);

        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL)).thenReturn(singletonList(boss));
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_DEPARTMENTS)).thenReturn(singletonList(bossOfDepartment));

        final Department department = new Department();
        department.setMembers(asList(normalUser, bossOfDepartment));

        when(departmentService.getAssignedDepartmentsOfMember(bossOfDepartment)).thenReturn(singletonList(department));
        when(departmentService.getAssignedDepartmentsOfMember(normalUser)).thenReturn(singletonList(department));

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(application);
        assertThat(recipientsForAllowAndRemind).contains(boss, bossOfDepartment);
    }

    @Test
    void testSendNoMailToBossOfOtherDepartment() {

        final Person normalUser = createPerson("normalUser", USER);
        final Application application = getHolidayApplication(normalUser);

        final Person boss = createPerson("boss1", BOSS);
        final Person bossOfDepartment = createPerson("boss2", BOSS);

        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL)).thenReturn(singletonList(boss));
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_DEPARTMENTS)).thenReturn(singletonList(bossOfDepartment));

        final Department department = new Department();
        department.setMembers(asList(normalUser, bossOfDepartment));

        when(departmentService.getAssignedDepartmentsOfMember(bossOfDepartment)).thenReturn(emptyList());
        when(departmentService.getAssignedDepartmentsOfMember(normalUser)).thenReturn(singletonList(department));

        final List<Person> recipientsForAllowAndRemind = sut.getRecipientsOfInterest(application);
        assertThat(recipientsForAllowAndRemind).contains(boss);
    }

    private Application getHolidayApplication(Person normalUser) {
        final VacationType vacationType = createVacationType(HOLIDAY, "application.data.vacationType.holiday");
        return createApplication(normalUser, vacationType);
    }
}
