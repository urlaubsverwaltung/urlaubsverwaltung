package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SessionServiceTest {

    private static final String USER_NAME = "username";

    private SessionService sessionService;

    private PersonService personService;
    private DepartmentService departmentService;
    private SecurityContext securityContext;

    @Before
    public void setUp() {

        personService = mock(PersonService.class);
        departmentService = mock(DepartmentService.class);

        sessionService = new SessionService(personService, departmentService);

        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(USER_NAME);

        securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        // Mock department head
        when(departmentService.isDepartmentHeadOfPerson(any(Person.class), any(Person.class)))
            .thenReturn(false);
    }


    // Get signed in user ----------------------------------------------------------------------------------------------

    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIfNoPersonCanBeFoundForTheCurrentlySignedInUser() {

        when(personService.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.empty());

        sessionService.getSignedInUser();
    }


    @Test
    public void ensureReturnsPersonForCurrentlySignedInUser() {

        Person person = TestDataCreator.createPerson();

        when(personService.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.of(person));

        Person signedInUser = sessionService.getSignedInUser();

        verify(personService).getPersonByLogin(USER_NAME);
        Assert.assertEquals("Wrong person", person, signedInUser);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIllegalOnNullAuthentication() {

        when(securityContext.getAuthentication()).thenReturn(null);
        sessionService.getSignedInUser();
    }

    // Access person data ----------------------------------------------------------------------------------------------

    @Test
    public void ensureSignedInOfficeUserCanAccessPersonData() throws IllegalAccessException {

        Person person = TestDataCreator.createPerson(23, "person");
        person.setPermissions(Collections.singletonList(Role.USER));

        Person office = TestDataCreator.createPerson(42, "office");
        office.setPermissions(Arrays.asList(Role.USER, Role.OFFICE));

        boolean isAllowed = sessionService.isSignedInUserAllowedToAccessPersonData(office, person);

        Assert.assertTrue("Office should be able to access any person data", isAllowed);
    }


    @Test
    public void ensureSignedInBossUserCanAccessPersonData() throws IllegalAccessException {

        Person person = TestDataCreator.createPerson(23, "person");
        person.setPermissions(Collections.singletonList(Role.USER));

        Person boss = TestDataCreator.createPerson(42, "boss");
        boss.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        boolean isAllowed = sessionService.isSignedInUserAllowedToAccessPersonData(boss, person);

        Assert.assertTrue("Boss should be able to access any person data", isAllowed);
    }


    @Test
    public void ensureSignedInDepartmentHeadOfPersonCanAccessPersonData() throws IllegalAccessException {

        Person person = TestDataCreator.createPerson(23, "person");
        person.setPermissions(Collections.singletonList(Role.USER));

        Person departmentHead = TestDataCreator.createPerson(42, "departmentHead");
        departmentHead.setPermissions(Arrays.asList(Role.USER, Role.DEPARTMENT_HEAD));

        when(departmentService.isDepartmentHeadOfPerson(departmentHead, person)).thenReturn(true);

        boolean isAllowed = sessionService.isSignedInUserAllowedToAccessPersonData(departmentHead, person);

        verify(departmentService).isDepartmentHeadOfPerson(departmentHead, person);
        Assert.assertTrue("Department head of person should be able to access the person's data", isAllowed);
    }


    @Test
    public void ensureSignedInDepartmentHeadThatIsNotDepartmentHeadOfPersonCanNotAccessPersonData()
        throws IllegalAccessException {

        Person person = TestDataCreator.createPerson(23, "person");
        person.setPermissions(Collections.singletonList(Role.USER));

        Person departmentHead = TestDataCreator.createPerson(42, "departmentHead");
        departmentHead.setPermissions(Arrays.asList(Role.USER, Role.DEPARTMENT_HEAD));

        when(departmentService.isDepartmentHeadOfPerson(departmentHead, person)).thenReturn(false);

        boolean isAllowed = sessionService.isSignedInUserAllowedToAccessPersonData(departmentHead, person);

        verify(departmentService).isDepartmentHeadOfPerson(departmentHead, person);
        Assert.assertFalse("Department head - but not of person - should not be able to access the person's data",
            isAllowed);
    }

    @Test
    public void ensureSignedInDepartmentHeadCanNotAccessSecondStageAuthorityPersonData()
            throws IllegalAccessException {

        Person secondStageAuthority = TestDataCreator.createPerson(23, "secondStageAuthority");
        secondStageAuthority.setPermissions(Arrays.asList(Role.USER, Role.SECOND_STAGE_AUTHORITY));

        Person departmentHead = TestDataCreator.createPerson(42, "departmentHead");
        departmentHead.setPermissions(Arrays.asList(Role.USER, Role.DEPARTMENT_HEAD));

        when(departmentService.isDepartmentHeadOfPerson(departmentHead, secondStageAuthority)).thenReturn(true);

        boolean isAllowed = sessionService.isSignedInUserAllowedToAccessPersonData(departmentHead, secondStageAuthority);

        verify(departmentService).isDepartmentHeadOfPerson(departmentHead, secondStageAuthority);
        Assert.assertFalse("Department head - but not of secondStageAuthority - should not be able to access the secondStageAuthority's data",
                isAllowed);
    }

    @Test
    public void ensureSignedInSecondStageAuthorityCanAccessDepartmentHeadPersonData()
            throws IllegalAccessException {

        Person secondStageAuthority = TestDataCreator.createPerson(23, "secondStageAuthority");
        secondStageAuthority.setPermissions(Arrays.asList(Role.USER, Role.SECOND_STAGE_AUTHORITY));

        Person departmentHead = TestDataCreator.createPerson(42, "departmentHead");
        departmentHead.setPermissions(Arrays.asList(Role.USER, Role.DEPARTMENT_HEAD));

        when(departmentService.isSecondStageAuthorityOfPerson(secondStageAuthority, departmentHead)).thenReturn(true);

        boolean isAllowed = sessionService.isSignedInUserAllowedToAccessPersonData(secondStageAuthority, departmentHead);

        verify(departmentService).isSecondStageAuthorityOfPerson(secondStageAuthority, departmentHead);
        Assert.assertTrue("secondStageAuthority should be able to access the departmentHeads's data",
                isAllowed);
    }

    @Test
    public void ensureNotPrivilegedUserCanNotAccessPersonData() throws IllegalAccessException {

        Person person = TestDataCreator.createPerson(23, "person");
        person.setPermissions(Collections.singletonList(Role.USER));

        Person user = TestDataCreator.createPerson(42, "user");
        user.setPermissions(Collections.singletonList(Role.USER));

        when(departmentService.isDepartmentHeadOfPerson(user, person)).thenReturn(false);

        boolean isAllowed = sessionService.isSignedInUserAllowedToAccessPersonData(user, person);

        Assert.assertFalse("User should not be able to access the data of other person", isAllowed);
    }


    @Test
    public void ensureNotPrivilegedUserCanAccessOwnPersonData() throws IllegalAccessException {

        Person user = TestDataCreator.createPerson(42, "user");
        user.setPermissions(Collections.singletonList(Role.USER));

        boolean isAllowed = sessionService.isSignedInUserAllowedToAccessPersonData(user, user);

        Assert.assertTrue("User should be able to access own data", isAllowed);
    }
}
