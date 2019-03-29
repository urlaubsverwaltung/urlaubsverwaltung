package org.synyx.urlaubsverwaltung.department;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    private DepartmentDAO departmentDAO;
    private ApplicationService applicationService;

    @Before
    public void setUp() {

        departmentDAO = mock(DepartmentDAO.class);
        applicationService = mock(ApplicationService.class);

        sut = new DepartmentServiceImpl(departmentDAO, applicationService);
    }


    @Test
    public void ensureCallDepartmentDAOSave() {

        Department department = TestDataCreator.createDepartment();

        sut.create(department);

        verify(departmentDAO).save(eq(department));
    }


    @Test
    public void ensureCallDepartmentDAOfindById() {

        sut.getDepartmentById(42);
        verify(departmentDAO).findById(eq(42));
    }


    @Test
    public void ensureUpdateCallDepartmentDAOUpdate() {

        Department department = TestDataCreator.createDepartment();

        sut.update(department);

        verify(departmentDAO).save(eq(department));
    }


    @Test
    public void ensureGetAllCallDepartmentDAOFindAll() {

        sut.getAllDepartments();

        verify(departmentDAO).findAll();
    }


    @Test
    public void ensureGetManagedDepartmentsOfDepartmentHeadCallCorrectDAOMethod() {

        Person person = mock(Person.class);

        sut.getManagedDepartmentsOfDepartmentHead(person);

        verify(departmentDAO).getManagedDepartments(person);
    }


    @Test
    public void ensureGetManagedDepartmentsOfSecondStageAuthorityCallCorrectDAOMethod() {

        Person person = mock(Person.class);

        sut.getManagedDepartmentsOfSecondStageAuthority(person);

        verify(departmentDAO).getDepartmentsForSecondStageAuthority(person);
    }


    @Test
    public void ensureGetAssignedDepartmentsOfMemberCallCorrectDAOMethod() {

        Person person = mock(Person.class);

        sut.getAssignedDepartmentsOfMember(person);

        verify(departmentDAO).getAssignedDepartments(person);
    }


    @Test
    public void ensureDeletionIsNotExecutedIfDepartmentWithGivenIDDoesNotExist() {

        int id = 0;
        when(departmentDAO.findById(id)).thenReturn(Optional.empty());

        sut.delete(id);

        verify(departmentDAO, never()).deleteById(anyInt());
    }


    @Test
    public void ensureDeleteCallFindOneAndDelete() {

        int id = 0;
        when(departmentDAO.findById(id)).thenReturn(Optional.of(TestDataCreator.createDepartment()));

        sut.delete(id);

        verify(departmentDAO).findById(eq(id));
        verify(departmentDAO).deleteById(eq(id));
    }


    @Test
    public void ensureSetLastModificationOnUpdate() {

        Department department = mock(Department.class);

        sut.update(department);

        verify(department).setLastModification(any(DateTime.class));
    }


    @Test
    public void ensureReturnsAllMembersOfTheManagedDepartmentsOfTheDepartmentHead() {

        Person departmentHead = mock(Person.class);

        Person admin1 = TestDataCreator.createPerson("admin1");
        Person admin2 = TestDataCreator.createPerson("admin2");

        Person marketing1 = TestDataCreator.createPerson("marketing1");
        Person marketing2 = TestDataCreator.createPerson("marketing2");
        Person marketing3 = TestDataCreator.createPerson("marketing3");

        Department admins = TestDataCreator.createDepartment("admins");
        admins.setMembers(Arrays.asList(admin1, admin2, departmentHead));

        Department marketing = TestDataCreator.createDepartment("marketing");
        marketing.setMembers(Arrays.asList(marketing1, marketing2, marketing3, departmentHead));

        when(departmentDAO.getManagedDepartments(departmentHead)).thenReturn(Arrays.asList(admins, marketing));

        List<Person> members = sut.getManagedMembersOfDepartmentHead(departmentHead);

        Assert.assertNotNull("Should not be null", members);
        Assert.assertEquals("Wrong number of members", 6, members.size());
    }


    @Test
    public void ensureReturnsEmptyListIfPersonHasNoManagedDepartment() {

        Person departmentHead = mock(Person.class);

        when(departmentDAO.getManagedDepartments(departmentHead)).thenReturn(Collections.emptyList());

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
        admins.setMembers(Arrays.asList(admin1, admin2, departmentHead));

        when(departmentDAO.getManagedDepartments(departmentHead)).thenReturn(Collections.singletonList(admins));

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
        admins.setMembers(Arrays.asList(admin1, admin2, departmentHead));

        Person marketing1 = TestDataCreator.createPerson("marketing1");

        when(departmentDAO.getManagedDepartments(departmentHead)).thenReturn(Collections.singletonList(admins));

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
        admins.setMembers(Arrays.asList(admin1, admin2, noDepartmentHead));

        when(departmentDAO.getManagedDepartments(noDepartmentHead))
            .thenReturn(Collections.singletonList(admins));

        boolean isDepartmentHead = sut.isDepartmentHeadOfPerson(noDepartmentHead, admin1);

        Assert.assertFalse("Should not be the department head of the given person", isDepartmentHead);
    }


    @Test
    public void ensureReturnsEmptyListOfDepartmentApplicationsIfPersonIsNotAssignedToAnyDepartment() {

        Person person = mock(Person.class);
        DateMidnight date = DateMidnight.now();

        when(departmentDAO.getAssignedDepartments(person)).thenReturn(Collections.emptyList());

        List<Application> applications = sut.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, date, date);

        Assert.assertNotNull("Should not be null", applications);
        Assert.assertTrue("Should be empty", applications.isEmpty());

        verify(departmentDAO).getAssignedDepartments(person);
        verifyZeroInteractions(applicationService);
    }


    @Test
    public void ensureReturnsEmptyListOfDepartmentApplicationsIfNoMatchingApplicationsForLeave() {

        Person person = mock(Person.class);
        DateMidnight date = DateMidnight.now();

        Person admin1 = TestDataCreator.createPerson("admin1");
        Person admin2 = TestDataCreator.createPerson("admin2");

        Person marketing1 = TestDataCreator.createPerson("marketing1");
        Person marketing2 = TestDataCreator.createPerson("marketing2");
        Person marketing3 = TestDataCreator.createPerson("marketing3");

        Department admins = TestDataCreator.createDepartment("admins");
        admins.setMembers(Arrays.asList(admin1, admin2, person));

        Department marketing = TestDataCreator.createDepartment("marketing");
        marketing.setMembers(Arrays.asList(marketing1, marketing2, marketing3, person));

        when(departmentDAO.getAssignedDepartments(person)).thenReturn(Arrays.asList(admins, marketing));
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(DateMidnight.class),
            any(DateMidnight.class), any(Person.class)))
            .thenReturn(Collections.emptyList());

        List<Application> applications = sut.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, date, date);

        // Ensure empty list
        Assert.assertNotNull("Should not be null", applications);
        Assert.assertTrue("Should be empty", applications.isEmpty());

        // Ensure fetches departments of person
        verify(departmentDAO).getAssignedDepartments(person);

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
        DateMidnight date = DateMidnight.now();

        Person admin1 = TestDataCreator.createPerson("admin1");
        Person marketing1 = TestDataCreator.createPerson("marketing1");

        Department admins = TestDataCreator.createDepartment("admins");
        admins.setMembers(Arrays.asList(admin1, person));

        Department marketing = TestDataCreator.createDepartment("marketing");
        marketing.setMembers(Arrays.asList(marketing1, person));

        Application waitingApplication = mock(Application.class);
        when(waitingApplication.hasStatus(ApplicationStatus.WAITING)).thenReturn(true);
        when(waitingApplication.hasStatus(ApplicationStatus.ALLOWED)).thenReturn(false);

        Application allowedApplication = mock(Application.class);
        when(allowedApplication.hasStatus(ApplicationStatus.WAITING)).thenReturn(false);
        when(allowedApplication.hasStatus(ApplicationStatus.ALLOWED)).thenReturn(true);

        Application otherApplication = mock(Application.class);
        when(otherApplication.hasStatus(ApplicationStatus.WAITING)).thenReturn(false);
        when(otherApplication.hasStatus(ApplicationStatus.ALLOWED)).thenReturn(false);

        when(departmentDAO.getAssignedDepartments(person)).thenReturn(Arrays.asList(admins, marketing));

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(DateMidnight.class),
            any(DateMidnight.class), eq(admin1)))
            .thenReturn(Arrays.asList(waitingApplication, otherApplication));

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(DateMidnight.class),
            any(DateMidnight.class), eq(marketing1)))
            .thenReturn(Collections.singletonList(allowedApplication));

        List<Application> applications = sut.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, date, date);

        Assert.assertEquals("Wrong number of applications", 2, applications.size());
        Assert.assertTrue("Should contain the waiting application", applications.contains(waitingApplication));
        Assert.assertTrue("Should contain the allowed application", applications.contains(allowedApplication));
        Assert.assertFalse("Should not contain an application with other status",
            applications.contains(otherApplication));
    }
}
