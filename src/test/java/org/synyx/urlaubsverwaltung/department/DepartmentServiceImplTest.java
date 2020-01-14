package org.synyx.urlaubsverwaltung.department;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


public class DepartmentServiceImplTest {

    private DepartmentServiceImpl sut;

    private DepartmentRepository departmentRepository;
    private ApplicationService applicationService;

    @Before
    public void setUp() {

        departmentRepository = mock(DepartmentRepository.class);
        applicationService = mock(ApplicationService.class);

        sut = new DepartmentServiceImpl(departmentRepository, applicationService, Clock.systemUTC());
    }


    @Test
    public void ensureCallDepartmentDAOSave() {

        Department department = TestDataCreator.createDepartment();

        sut.create(department);

        verify(departmentRepository).save(eq(department));
    }


    @Test
    public void ensureCallDepartmentDAOfindById() {

        sut.getDepartmentById(42);
        verify(departmentRepository).findById(eq(42));
    }


    @Test
    public void ensureUpdateCallDepartmentDAOUpdate() {

        Department department = TestDataCreator.createDepartment();

        sut.update(department);

        verify(departmentRepository).save(eq(department));
    }


    @Test
    public void ensureGetAllCallDepartmentDAOFindAll() {

        sut.getAllDepartments();

        verify(departmentRepository).findAll();
    }


    @Test
    public void ensureGetManagedDepartmentsOfDepartmentHeadCallCorrectDAOMethod() {

        Person person = mock(Person.class);

        sut.getManagedDepartmentsOfDepartmentHead(person);

        verify(departmentRepository).getManagedDepartments(person);
    }


    @Test
    public void ensureGetManagedDepartmentsOfSecondStageAuthorityCallCorrectDAOMethod() {

        Person person = mock(Person.class);

        sut.getManagedDepartmentsOfSecondStageAuthority(person);

        verify(departmentRepository).getDepartmentsForSecondStageAuthority(person);
    }


    @Test
    public void ensureGetAssignedDepartmentsOfMemberCallCorrectDAOMethod() {

        Person person = mock(Person.class);

        sut.getAssignedDepartmentsOfMember(person);

        verify(departmentRepository).getAssignedDepartments(person);
    }


    @Test
    public void ensureDeletionIsNotExecutedIfDepartmentWithGivenIDDoesNotExist() {

        int id = 0;
        when(departmentRepository.findById(id)).thenReturn(Optional.empty());

        sut.delete(id);

        verify(departmentRepository, never()).deleteById(anyInt());
    }


    @Test
    public void ensureDeleteCallFindOneAndDelete() {

        int id = 0;
        when(departmentRepository.findById(id)).thenReturn(Optional.of(TestDataCreator.createDepartment()));

        sut.delete(id);

        verify(departmentRepository).findById(eq(id));
        verify(departmentRepository).deleteById(eq(id));
    }


    @Test
    public void ensureSetLastModificationOnUpdate() {

        Department department = mock(Department.class);

        sut.update(department);

        verify(department).setLastModification(any(LocalDate.class));
    }


    @Test
    public void ensureReturnsAllMembersOfTheManagedDepartmentsOfTheDepartmentHead() {

        Person departmentHead = mock(Person.class);
        Person secondDepartmentHead = mock(Person.class);

        Person admin1 = TestDataCreator.createPerson("admin1");
        Person admin2 = TestDataCreator.createPerson("admin2");

        Person marketing1 = TestDataCreator.createPerson("marketing1");
        Person marketing2 = TestDataCreator.createPerson("marketing2");
        Person marketing3 = TestDataCreator.createPerson("marketing3");

        Department admins = TestDataCreator.createDepartment("admins");
        admins.setMembers(asList(admin1, admin2, departmentHead, secondDepartmentHead));
        admins.setDepartmentHeads(asList(departmentHead, secondDepartmentHead));

        Department marketing = TestDataCreator.createDepartment("marketing");
        Person secondStageAuth = mock(Person.class);
        marketing.setMembers(asList(marketing1, marketing2, marketing3, departmentHead, secondStageAuth));
        marketing.setSecondStageAuthorities(singletonList(secondStageAuth));

        when(departmentRepository.getManagedDepartments(departmentHead)).thenReturn(asList(admins, marketing));

        List<Person> members = sut.getManagedMembersOfDepartmentHead(departmentHead);

        Assert.assertNotNull("Should not be null", members);
        Assert.assertEquals("Wrong number of members", 7, members.size());
    }


    @Test
    public void ensureReturnsEmptyListIfPersonHasNoManagedDepartment() {

        Person departmentHead = mock(Person.class);

        when(departmentRepository.getManagedDepartments(departmentHead)).thenReturn(emptyList());

        List<Person> members = sut.getManagedMembersOfDepartmentHead(departmentHead);

        Assert.assertNotNull("Should not be null", members);
        Assert.assertTrue("Should be empty", members.isEmpty());
    }


    @Test
    public void ensureReturnsTrueIfIsDepartmentHeadOfTheGivenPerson() {

        Person departmentHead = mock(Person.class);
        when(departmentHead.hasRole(Role.DEPARTMENT_HEAD)).thenReturn(true);

        Person admin1 = TestDataCreator.createPerson("admin1");
        Person admin2 = TestDataCreator.createPerson("admin2");

        Department admins = TestDataCreator.createDepartment("admins");
        admins.setMembers(asList(admin1, admin2, departmentHead));

        when(departmentRepository.getManagedDepartments(departmentHead)).thenReturn(singletonList(admins));

        boolean isDepartmentHead = sut.isDepartmentHeadOfPerson(departmentHead, admin1);

        Assert.assertTrue("Should be the department head of the given person", isDepartmentHead);
    }


    @Test
    public void ensureReturnsFalseIfIsNotDepartmentHeadOfTheGivenPerson() {

        Person departmentHead = mock(Person.class);
        when(departmentHead.hasRole(Role.DEPARTMENT_HEAD)).thenReturn(true);

        Person admin1 = TestDataCreator.createPerson("admin1");
        Person admin2 = TestDataCreator.createPerson("admin2");

        Department admins = TestDataCreator.createDepartment("admins");
        admins.setMembers(asList(admin1, admin2, departmentHead));

        Person marketing1 = TestDataCreator.createPerson("marketing1");

        when(departmentRepository.getManagedDepartments(departmentHead)).thenReturn(singletonList(admins));

        boolean isDepartmentHead = sut.isDepartmentHeadOfPerson(departmentHead, marketing1);

        Assert.assertFalse("Should not be the department head of the given person", isDepartmentHead);
    }


    @Test
    public void ensureReturnsFalseIfIsInTheSameDepartmentButHasNotDepartmentHeadRole() {

        Person noDepartmentHead = mock(Person.class);
        when(noDepartmentHead.hasRole(Role.DEPARTMENT_HEAD)).thenReturn(false);

        Person admin1 = TestDataCreator.createPerson("admin1");
        Person admin2 = TestDataCreator.createPerson("admin2");

        Department admins = TestDataCreator.createDepartment("admins");
        admins.setMembers(asList(admin1, admin2, noDepartmentHead));

        when(departmentRepository.getManagedDepartments(noDepartmentHead))
            .thenReturn(singletonList(admins));

        boolean isDepartmentHead = sut.isDepartmentHeadOfPerson(noDepartmentHead, admin1);

        Assert.assertFalse("Should not be the department head of the given person", isDepartmentHead);
    }


    @Test
    public void ensureReturnsEmptyListOfDepartmentApplicationsIfPersonIsNotAssignedToAnyDepartment() {

        Person person = mock(Person.class);
        LocalDate date = LocalDate.now(UTC);

        when(departmentRepository.getAssignedDepartments(person)).thenReturn(emptyList());

        List<Application> applications = sut.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, date, date);

        Assert.assertNotNull("Should not be null", applications);
        Assert.assertTrue("Should be empty", applications.isEmpty());

        verify(departmentRepository).getAssignedDepartments(person);
        verifyZeroInteractions(applicationService);
    }


    @Test
    public void ensureReturnsEmptyListOfDepartmentApplicationsIfNoMatchingApplicationsForLeave() {

        Person person = mock(Person.class);
        LocalDate date = LocalDate.now(UTC);

        Person admin1 = TestDataCreator.createPerson("admin1");
        Person admin2 = TestDataCreator.createPerson("admin2");

        Person marketing1 = TestDataCreator.createPerson("marketing1");
        Person marketing2 = TestDataCreator.createPerson("marketing2");
        Person marketing3 = TestDataCreator.createPerson("marketing3");

        Department admins = TestDataCreator.createDepartment("admins");
        admins.setMembers(asList(admin1, admin2, person));

        Department marketing = TestDataCreator.createDepartment("marketing");
        marketing.setMembers(asList(marketing1, marketing2, marketing3, person));

        when(departmentRepository.getAssignedDepartments(person)).thenReturn(asList(admins, marketing));
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(emptyList());

        List<Application> applications = sut.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, date, date);

        // Ensure empty list
        Assert.assertNotNull("Should not be null", applications);
        Assert.assertTrue("Should be empty", applications.isEmpty());

        // Ensure fetches departments of person
        verify(departmentRepository).getAssignedDepartments(person);

        // Ensure fetches applications for leave for every department member
        verify(applicationService)
            .getApplicationsForACertainPeriodAndPerson(eq(date), eq(date), eq(admin1));
        verify(applicationService)
            .getApplicationsForACertainPeriodAndPerson(eq(date), eq(date), eq(admin2));
        verify(applicationService)
            .getApplicationsForACertainPeriodAndPerson(eq(date), eq(date), eq(marketing1));
        verify(applicationService)
            .getApplicationsForACertainPeriodAndPerson(eq(date), eq(date), eq(marketing2));
        verify(applicationService)
            .getApplicationsForACertainPeriodAndPerson(eq(date), eq(date), eq(marketing3));

        // Ensure does not fetch applications for leave for the given person
        verify(applicationService, never())
            .getApplicationsForACertainPeriodAndPerson(eq(date), eq(date), eq(person));
    }


    @Test
    public void ensureReturnsOnlyWaitingAndAllowedDepartmentApplicationsForLeave() {

        Person person = mock(Person.class);
        LocalDate date = LocalDate.now(UTC);

        Person admin1 = TestDataCreator.createPerson("admin1");
        Person marketing1 = TestDataCreator.createPerson("marketing1");

        Department admins = TestDataCreator.createDepartment("admins");
        admins.setMembers(asList(admin1, person));

        Department marketing = TestDataCreator.createDepartment("marketing");
        marketing.setMembers(asList(marketing1, person));

        Application waitingApplication = mock(Application.class);
        when(waitingApplication.hasStatus(ApplicationStatus.WAITING)).thenReturn(true);
        when(waitingApplication.hasStatus(ApplicationStatus.ALLOWED)).thenReturn(false);

        Application allowedApplication = mock(Application.class);
        when(allowedApplication.hasStatus(ApplicationStatus.WAITING)).thenReturn(false);
        when(allowedApplication.hasStatus(ApplicationStatus.ALLOWED)).thenReturn(true);

        Application otherApplication = mock(Application.class);
        when(otherApplication.hasStatus(ApplicationStatus.WAITING)).thenReturn(false);
        when(otherApplication.hasStatus(ApplicationStatus.ALLOWED)).thenReturn(false);

        when(departmentRepository.getAssignedDepartments(person)).thenReturn(asList(admins, marketing));

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class),
            any(LocalDate.class), eq(admin1)))
            .thenReturn(asList(waitingApplication, otherApplication));

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class),
            any(LocalDate.class), eq(marketing1)))
            .thenReturn(singletonList(allowedApplication));

        List<Application> applications = sut.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, date, date);

        Assert.assertEquals("Wrong number of applications", 2, applications.size());
        Assert.assertTrue("Should contain the waiting application", applications.contains(waitingApplication));
        Assert.assertTrue("Should contain the allowed application", applications.contains(allowedApplication));
        Assert.assertFalse("Should not contain an application with other status",
            applications.contains(otherApplication));
    }

    @Test
    public void ensureSignedInOfficeUserCanAccessPersonData() throws IllegalAccessException {

        Person person = TestDataCreator.createPerson(23, "person");
        person.setPermissions(singletonList(Role.USER));

        Person office = TestDataCreator.createPerson(42, "office");
        office.setPermissions(asList(Role.USER, Role.OFFICE));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(office, person);

        Assert.assertTrue("Office should be able to access any person data", isAllowed);
    }


    @Test
    public void ensureSignedInBossUserCanAccessPersonData() throws IllegalAccessException {

        Person person = TestDataCreator.createPerson(23, "person");
        person.setPermissions(singletonList(Role.USER));

        Person boss = TestDataCreator.createPerson(42, "boss");
        boss.setPermissions(asList(Role.USER, Role.BOSS));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(boss, person);

        Assert.assertTrue("Boss should be able to access any person data", isAllowed);
    }


    @Test
    public void ensureSignedInDepartmentHeadOfPersonCanAccessPersonData() throws IllegalAccessException {

        Person person = TestDataCreator.createPerson(23, "person");
        person.setPermissions(singletonList(Role.USER));

        Person departmentHead = TestDataCreator.createPerson(42, "departmentHead");
        departmentHead.setPermissions(asList(Role.USER, Role.DEPARTMENT_HEAD));

        Department dep = TestDataCreator.createDepartment("dep");
        dep.setMembers(asList(person, departmentHead));

        when(departmentRepository.getManagedDepartments(departmentHead))
            .thenReturn(singletonList(dep));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(departmentHead, person);

        Assert.assertTrue("Department head of person should be able to access the person's data", isAllowed);
    }


    @Test
    public void ensureSignedInDepartmentHeadThatIsNotDepartmentHeadOfPersonCanNotAccessPersonData()
        throws IllegalAccessException {

        Person person = TestDataCreator.createPerson(23, "person");
        person.setPermissions(singletonList(Role.USER));

        Person departmentHead = TestDataCreator.createPerson(42, "departmentHead");
        departmentHead.setPermissions(asList(Role.USER, Role.DEPARTMENT_HEAD));

        Department dep = TestDataCreator.createDepartment("dep");
        dep.setMembers(singletonList(departmentHead));

        when(departmentRepository.getManagedDepartments(departmentHead))
            .thenReturn(singletonList(dep));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(departmentHead, person);

        Assert.assertFalse("Department head - but not of person - should not be able to access the person's data",
            isAllowed);
    }

    @Test
    public void ensureSignedInDepartmentHeadCanNotAccessSecondStageAuthorityPersonData()
        throws IllegalAccessException {

        Person secondStageAuthority = TestDataCreator.createPerson(23, "secondStageAuthority");
        secondStageAuthority.setPermissions(asList(Role.USER, Role.SECOND_STAGE_AUTHORITY));

        Person departmentHead = TestDataCreator.createPerson(42, "departmentHead");
        departmentHead.setPermissions(asList(Role.USER, Role.DEPARTMENT_HEAD));

        Department dep = TestDataCreator.createDepartment("dep");
        dep.setMembers(asList(secondStageAuthority, departmentHead));
        dep.setSecondStageAuthorities(singletonList(secondStageAuthority));

        when(departmentRepository.getManagedDepartments(departmentHead))
            .thenReturn(singletonList(dep));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(departmentHead, secondStageAuthority);

        Assert.assertFalse("Department head - but not of secondStageAuthority - should not be able to access the secondStageAuthority's data",
            isAllowed);
    }

    @Test
    public void ensureSignedInSecondStageAuthorityCanAccessDepartmentHeadPersonData()
        throws IllegalAccessException {

        Person secondStageAuthority = TestDataCreator.createPerson(23, "secondStageAuthority");
        secondStageAuthority.setPermissions(asList(Role.USER, Role.SECOND_STAGE_AUTHORITY, Role.DEPARTMENT_HEAD));

        Person departmentHead = TestDataCreator.createPerson(42, "departmentHead");
        departmentHead.setPermissions(asList(Role.USER, Role.DEPARTMENT_HEAD, Role.SECOND_STAGE_AUTHORITY));

        Department dep = TestDataCreator.createDepartment("dep");
        dep.setMembers(asList(secondStageAuthority, departmentHead));
        dep.setSecondStageAuthorities(singletonList(secondStageAuthority));
        dep.setDepartmentHeads(singletonList(departmentHead));

        when(departmentRepository.getDepartmentsForSecondStageAuthority(secondStageAuthority)).thenReturn(singletonList(dep));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(secondStageAuthority, departmentHead);

        Assert.assertTrue("secondStageAuthority should be able to access the departmentHeads's data",
            isAllowed);
    }

    @Test
    public void ensureNotPrivilegedUserCanNotAccessPersonData() throws IllegalAccessException {

        Person person = TestDataCreator.createPerson(23, "person");
        person.setPermissions(singletonList(Role.USER));

        Person user = TestDataCreator.createPerson(42, "user");
        user.setPermissions(singletonList(Role.USER));

        Department dep = TestDataCreator.createDepartment("dep");
        dep.setMembers(asList(person, user));
        when(departmentRepository.getManagedDepartments(user))
            .thenReturn(singletonList(dep));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(user, person);

        Assert.assertFalse("User should not be able to access the data of other person", isAllowed);
    }


    @Test
    public void ensureNotPrivilegedUserCanAccessOwnPersonData() throws IllegalAccessException {

        Person user = TestDataCreator.createPerson(42, "user");
        user.setPermissions(singletonList(Role.USER));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(user, user);

        Assert.assertTrue("User should be able to access own data", isAllowed);
    }
}
