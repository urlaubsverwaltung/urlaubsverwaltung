package org.synyx.urlaubsverwaltung.core.department;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import static org.mockito.Matchers.eq;


/**
 * @author  Daniel Hammann - hammann@synyx.de
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class DepartmentServiceImplTest {

    private DepartmentServiceImpl sut;

    private DepartmentDAO departmentDAO;
    private ApplicationService applicationService;

    @Before
    public void setUp() throws Exception {

        departmentDAO = Mockito.mock(DepartmentDAO.class);
        applicationService = Mockito.mock(ApplicationService.class);

        sut = new DepartmentServiceImpl(departmentDAO, applicationService);
    }


    @Test
    public void ensureCallDepartmentDAOSave() throws Exception {

        Department department = TestDataCreator.createDepartment();

        sut.create(department);

        Mockito.verify(departmentDAO).save(eq(department));
    }


    @Test
    public void ensureCallDepartmentDAOFindOne() throws Exception {

        sut.getDepartmentById(42);
        Mockito.verify(departmentDAO).findOne(eq(42));
    }


    @Test
    public void ensureUpdateCallDepartmentDAOUpdate() throws Exception {

        Department department = TestDataCreator.createDepartment();

        sut.update(department);

        Mockito.verify(departmentDAO).save(eq(department));
    }


    @Test
    public void ensureGetAllCallDepartmentDAOFindAll() throws Exception {

        sut.getAllDepartments();

        Mockito.verify(departmentDAO).findAll();
    }


    @Test
    public void ensureGetManagedDepartmentsOfDepartmentHeadCallCorrectDAOMethod() throws Exception {

        Person person = Mockito.mock(Person.class);

        sut.getManagedDepartmentsOfDepartmentHead(person);

        Mockito.verify(departmentDAO).getManagedDepartments(person);
    }


    @Test
    public void ensureGetAssignedDepartmentsOfMemberCallCorrectDAOMethod() throws Exception {

        Person person = Mockito.mock(Person.class);

        sut.getAssignedDepartmentsOfMember(person);

        Mockito.verify(departmentDAO).getAssignedDepartments(person);
    }


    @Test
    public void ensureDeletionIsNotExecutedIfDepartmentWithGivenIDDoesNotExist() throws Exception {

        int id = 0;
        Mockito.when(departmentDAO.findOne(id)).thenReturn(null);

        sut.delete(id);

        Mockito.verify(departmentDAO, Mockito.never()).delete(Mockito.anyInt());
    }


    @Test
    public void ensureDeleteCallFindOneAndDelete() throws Exception {

        int id = 0;
        Mockito.when(departmentDAO.findOne(id)).thenReturn(TestDataCreator.createDepartment());

        sut.delete(id);

        Mockito.verify(departmentDAO).findOne(eq(id));
        Mockito.verify(departmentDAO).delete(eq(id));
    }


    @Test
    public void ensureSetLastModificationOnUpdate() throws Exception {

        Department department = Mockito.mock(Department.class);

        sut.update(department);

        Mockito.verify(department).setLastModification(Mockito.any(DateTime.class));
    }


    @Test
    public void ensureReturnsAllMembersOfTheManagedDepartmentsOfTheDepartmentHead() {

        Person departmentHead = Mockito.mock(Person.class);

        Person admin1 = TestDataCreator.createPerson("admin1");
        Person admin2 = TestDataCreator.createPerson("admin2");

        Person marketing1 = TestDataCreator.createPerson("marketing1");
        Person marketing2 = TestDataCreator.createPerson("marketing2");
        Person marketing3 = TestDataCreator.createPerson("marketing3");

        Department admins = TestDataCreator.createDepartment("admins");
        admins.setMembers(Arrays.asList(admin1, admin2, departmentHead));

        Department marketing = TestDataCreator.createDepartment("marketing");
        marketing.setMembers(Arrays.asList(marketing1, marketing2, marketing3, departmentHead));

        Mockito.when(departmentDAO.getManagedDepartments(departmentHead)).thenReturn(Arrays.asList(admins, marketing));

        List<Person> members = sut.getManagedMembersOfDepartmentHead(departmentHead);

        Assert.assertNotNull("Should not be null", members);
        Assert.assertEquals("Wrong number of members", 6, members.size());
    }


    @Test
    public void ensureReturnsEmptyListIfPersonHasNoManagedDepartment() {

        Person departmentHead = Mockito.mock(Person.class);

        Mockito.when(departmentDAO.getManagedDepartments(departmentHead)).thenReturn(Collections.emptyList());

        List<Person> members = sut.getManagedMembersOfDepartmentHead(departmentHead);

        Assert.assertNotNull("Should not be null", members);
        Assert.assertTrue("Should be empty", members.isEmpty());
    }


    @Test
    public void ensureReturnsTrueIfIsDepartmentHeadOfTheGivenPerson() {

        Person departmentHead = Mockito.mock(Person.class);
        Mockito.when(departmentHead.hasRole(Role.DEPARTMENT_HEAD)).thenReturn(true);

        Person admin1 = TestDataCreator.createPerson("admin1");
        Person admin2 = TestDataCreator.createPerson("admin2");

        Department admins = TestDataCreator.createDepartment("admins");
        admins.setMembers(Arrays.asList(admin1, admin2, departmentHead));

        Mockito.when(departmentDAO.getManagedDepartments(departmentHead)).thenReturn(Collections.singletonList(admins));

        boolean isDepartmentHead = sut.isDepartmentHeadOfPerson(departmentHead, admin1);

        Assert.assertTrue("Should be the department head of the given person", isDepartmentHead);
    }


    @Test
    public void ensureReturnsFalseIfIsNotDepartmentHeadOfTheGivenPerson() {

        Person departmentHead = Mockito.mock(Person.class);
        Mockito.when(departmentHead.hasRole(Role.DEPARTMENT_HEAD)).thenReturn(true);

        Person admin1 = TestDataCreator.createPerson("admin1");
        Person admin2 = TestDataCreator.createPerson("admin2");

        Department admins = TestDataCreator.createDepartment("admins");
        admins.setMembers(Arrays.asList(admin1, admin2, departmentHead));

        Person marketing1 = TestDataCreator.createPerson("marketing1");

        Mockito.when(departmentDAO.getManagedDepartments(departmentHead)).thenReturn(Collections.singletonList(admins));

        boolean isDepartmentHead = sut.isDepartmentHeadOfPerson(departmentHead, marketing1);

        Assert.assertFalse("Should not be the department head of the given person", isDepartmentHead);
    }


    @Test
    public void ensureReturnsFalseIfIsInTheSameDepartmentButHasNotDepartmentHeadRole() {

        Person noDepartmentHead = Mockito.mock(Person.class);
        Mockito.when(noDepartmentHead.hasRole(Role.DEPARTMENT_HEAD)).thenReturn(false);

        Person admin1 = TestDataCreator.createPerson("admin1");
        Person admin2 = TestDataCreator.createPerson("admin2");

        Department admins = TestDataCreator.createDepartment("admins");
        admins.setMembers(Arrays.asList(admin1, admin2, noDepartmentHead));

        Mockito.when(departmentDAO.getManagedDepartments(noDepartmentHead))
            .thenReturn(Collections.singletonList(admins));

        boolean isDepartmentHead = sut.isDepartmentHeadOfPerson(noDepartmentHead, admin1);

        Assert.assertFalse("Should not be the department head of the given person", isDepartmentHead);
    }


    @Test
    public void ensureReturnsEmptyListOfDepartmentApplicationsIfPersonIsNotAssignedToAnyDepartment() {

        Person person = Mockito.mock(Person.class);
        DateMidnight date = DateMidnight.now();

        Mockito.when(departmentDAO.getAssignedDepartments(person)).thenReturn(Collections.emptyList());

        List<Application> applications = sut.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, date, date);

        Assert.assertNotNull("Should not be null", applications);
        Assert.assertTrue("Should be empty", applications.isEmpty());

        Mockito.verify(departmentDAO).getAssignedDepartments(person);
        Mockito.verifyZeroInteractions(applicationService);
    }


    @Test
    public void ensureReturnsEmptyListOfDepartmentApplicationsIfNoMatchingApplicationsForLeave() {

        Person person = Mockito.mock(Person.class);
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

        Mockito.when(departmentDAO.getAssignedDepartments(person)).thenReturn(Arrays.asList(admins, marketing));
        Mockito.when(applicationService.getApplicationsForACertainPeriodAndPerson(Mockito.any(DateMidnight.class),
                    Mockito.any(DateMidnight.class), Mockito.any(Person.class)))
            .thenReturn(Collections.emptyList());

        List<Application> applications = sut.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, date, date);

        // Ensure empty list
        Assert.assertNotNull("Should not be null", applications);
        Assert.assertTrue("Should be empty", applications.isEmpty());

        // Ensure fetches departments of person
        Mockito.verify(departmentDAO).getAssignedDepartments(person);

        // Ensure fetches applications for leave for every department member
        Mockito.verify(applicationService)
            .getApplicationsForACertainPeriodAndPerson(Mockito.eq(date), Mockito.eq(date), Mockito.eq(admin1));
        Mockito.verify(applicationService)
            .getApplicationsForACertainPeriodAndPerson(Mockito.eq(date), Mockito.eq(date), Mockito.eq(admin2));
        Mockito.verify(applicationService)
            .getApplicationsForACertainPeriodAndPerson(Mockito.eq(date), Mockito.eq(date), Mockito.eq(marketing1));
        Mockito.verify(applicationService)
            .getApplicationsForACertainPeriodAndPerson(Mockito.eq(date), Mockito.eq(date), Mockito.eq(marketing2));
        Mockito.verify(applicationService)
            .getApplicationsForACertainPeriodAndPerson(Mockito.eq(date), Mockito.eq(date), Mockito.eq(marketing3));

        // Ensure does not fetch applications for leave for the given person
        Mockito.verify(applicationService, Mockito.never())
            .getApplicationsForACertainPeriodAndPerson(Mockito.eq(date), Mockito.eq(date), Mockito.eq(person));
    }


    @Test
    public void ensureReturnsOnlyWaitingAndAllowedDepartmentApplicationsForLeave() {

        Person person = Mockito.mock(Person.class);
        DateMidnight date = DateMidnight.now();

        Person admin1 = TestDataCreator.createPerson("admin1");
        Person marketing1 = TestDataCreator.createPerson("marketing1");

        Department admins = TestDataCreator.createDepartment("admins");
        admins.setMembers(Arrays.asList(admin1, person));

        Department marketing = TestDataCreator.createDepartment("marketing");
        marketing.setMembers(Arrays.asList(marketing1, person));

        Application waitingApplication = Mockito.mock(Application.class);
        Mockito.when(waitingApplication.hasStatus(ApplicationStatus.WAITING)).thenReturn(true);
        Mockito.when(waitingApplication.hasStatus(ApplicationStatus.ALLOWED)).thenReturn(false);

        Application allowedApplication = Mockito.mock(Application.class);
        Mockito.when(allowedApplication.hasStatus(ApplicationStatus.WAITING)).thenReturn(false);
        Mockito.when(allowedApplication.hasStatus(ApplicationStatus.ALLOWED)).thenReturn(true);

        Application otherApplication = Mockito.mock(Application.class);
        Mockito.when(otherApplication.hasStatus(ApplicationStatus.WAITING)).thenReturn(false);
        Mockito.when(otherApplication.hasStatus(ApplicationStatus.ALLOWED)).thenReturn(false);

        Mockito.when(departmentDAO.getAssignedDepartments(person)).thenReturn(Arrays.asList(admins, marketing));

        Mockito.when(applicationService.getApplicationsForACertainPeriodAndPerson(Mockito.any(DateMidnight.class),
                    Mockito.any(DateMidnight.class), Mockito.eq(admin1)))
            .thenReturn(Arrays.asList(waitingApplication, otherApplication));

        Mockito.when(applicationService.getApplicationsForACertainPeriodAndPerson(Mockito.any(DateMidnight.class),
                    Mockito.any(DateMidnight.class), Mockito.eq(marketing1)))
            .thenReturn(Collections.singletonList(allowedApplication));

        List<Application> applications = sut.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, date, date);

        Assert.assertEquals("Wrong number of applications", 2, applications.size());
        Assert.assertTrue("Should contain the waiting application", applications.contains(waitingApplication));
        Assert.assertTrue("Should contain the allowed application", applications.contains(allowedApplication));
        Assert.assertFalse("Should not contain an application with other status",
            applications.contains(otherApplication));
    }
}
