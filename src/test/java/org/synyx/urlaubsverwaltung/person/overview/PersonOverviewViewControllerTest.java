package org.synyx.urlaubsverwaltung.person.overview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.department.web.UnknownDepartmentException;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;

import java.time.Clock;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class PersonOverviewViewControllerTest {

    private static final int UNKNOWN_PERSON_ID = 365;
    private static final int PERSON_ID = 1;
    private static final int UNKNOWN_DEPARTMENT_ID = 456;

    private static final String YEAR_ATTRIBUTE = "year";
    private static final String DEPARTMENT_ATTRIBUTE = "department";

    private static Clock clock;

    private PersonOverviewViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private AccountService accountService;
    @Mock
    private VacationDaysService vacationDaysService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PersonBasedataService personBasedataService;

    private Person person;

    @BeforeEach
    void setUp() {

        clock = Clock.systemUTC();
        sut = new PersonOverviewViewController(personService, accountService, vacationDaysService, departmentService,
            personBasedataService, clock);

        person = new Person();
        person.setId(1);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));
    }

    @Test
    void showPersonRedirectsToPersonActiveTrue() throws Exception {

        final ResultActions resultActions = perform(get("/web/person"));

        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(header().string("Location", "/web/person?active=true"));
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleBossCallsCorrectService() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(USER, BOSS));

        perform(get("/web/person").param("active", "true"));

        verify(personService).getActivePersons();
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleOfficeCallsCorrectService() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(USER, OFFICE));

        perform(get("/web/person").param("active", "true"));

        verify(personService).getActivePersons();
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleDepartmentHeadCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/person").param("active", "true"));

        verify(departmentService).getMembersForDepartmentHead(signedInUser);
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/person").param("active", "true"));

        verify(departmentService).getMembersForSecondStageAuthority(signedInUser);
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleDepartmentHeadAndSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/person").param("active", "true"));

        verify(departmentService).getMembersForDepartmentHead(signedInUser);
        verify(departmentService).getMembersForSecondStageAuthority(signedInUser);
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleDepartmentHeadAndSecondStageAuthorityDistinctPersons() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person("username", "Cloud", "Sky", "sky@exaple.org");
        person.setId(2);
        when(departmentService.getMembersForDepartmentHead(signedInUser)).thenReturn(List.of(person));
        when(departmentService.getMembersForSecondStageAuthority(signedInUser)).thenReturn(List.of(person));

        perform(get("/web/person").param("active", "true"))
            .andExpect(model().attribute("persons", hasSize(1)));
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleDepartmentHeadAndSecondStageAuthorityPersonsSortedByFirstname() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        signedInUser.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person bruce = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        bruce.setId(2);

        final Person clark = new Person("superman", "Kent", "Clark", "superman@example.org");
        clark.setId(3);

        when(departmentService.getMembersForDepartmentHead(signedInUser)).thenReturn(List.of(clark));
        when(departmentService.getMembersForSecondStageAuthority(signedInUser)).thenReturn(List.of(clark, bruce));

        perform(get("/web/person").param("active", "true"))
            .andExpect(
                model().attribute("persons", contains(
                    hasProperty("firstName", is("Bruce")),
                    hasProperty("firstName", is("Clark"))
                ))
            );
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleDepartmentHeadAndSecondStageAuthorityPersonsSortedByFirstnameThenLastName() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        signedInUser.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person wayne = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        wayne.setId(2);

        final Person wolf = new Person("red.wolf", "Xavier", "Bruce", "red.wolf@example.org");
        wolf.setId(3);

        when(departmentService.getMembersForDepartmentHead(signedInUser)).thenReturn(List.of(wolf));
        when(departmentService.getMembersForSecondStageAuthority(signedInUser)).thenReturn(List.of(wolf, wayne));

        perform(get("/web/person").param("active", "true"))
            .andExpect(
                model().attribute("persons", contains(
                    hasProperty("lastName", is("Wayne")),
                    hasProperty("lastName", is("Xavier"))
                ))
            );
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleBossCallsCorrectService() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(USER, BOSS));

        perform(get("/web/person").param("active", "false"));

        verify(personService).getInactivePersons();
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleOfficeCallsCorrectService() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(USER, OFFICE));

        perform(get("/web/person").param("active", "false"));

        verify(personService).getInactivePersons();
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleDepartmentHeadCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/person").param("active", "false"));

        verify(departmentService).getMembersForDepartmentHead(signedInUser);
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/person").param("active", "false"));

        verify(departmentService).getMembersForSecondStageAuthority(signedInUser);
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleDepartmentHeadAndSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/person").param("active", "false"));

        verify(departmentService).getMembersForDepartmentHead(signedInUser);
        verify(departmentService).getMembersForSecondStageAuthority(signedInUser);
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleDepartmentHeadAndSecondStageAuthorityDistinctPersons() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person("username", "Cloud", "Sky", "sky@exaple.org");
        person.setId(2);
        person.setPermissions(List.of(INACTIVE));
        when(departmentService.getMembersForDepartmentHead(signedInUser)).thenReturn(List.of(person));
        when(departmentService.getMembersForSecondStageAuthority(signedInUser)).thenReturn(List.of(person));

        perform(get("/web/person").param("active", "false"))
            .andExpect(model().attribute("persons", hasSize(1)));
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleDepartmentHeadAndSecondStageAuthorityPersonsSortedByFirstname() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        signedInUser.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person bruce = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        bruce.setId(2);
        bruce.setPermissions(List.of(INACTIVE));

        final Person clark = new Person("superman", "Kent", "Clark", "superman@example.org");
        clark.setId(3);
        clark.setPermissions(List.of(INACTIVE));

        when(departmentService.getMembersForDepartmentHead(signedInUser)).thenReturn(List.of(clark));
        when(departmentService.getMembersForSecondStageAuthority(signedInUser)).thenReturn(List.of(clark, bruce));

        perform(get("/web/person").param("active", "false"))
            .andExpect(
                model().attribute("persons", contains(
                    hasProperty("firstName", is("Bruce")),
                    hasProperty("firstName", is("Clark"))
                ))
            );
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleDepartmentHeadAndSecondStageAuthorityPersonsSortedByFirstnameThenLastName() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        signedInUser.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person wayne = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        wayne.setId(2);
        wayne.setPermissions(List.of(INACTIVE));

        final Person wolf = new Person("red.wolf", "Xavier", "Bruce", "red.wolf@example.org");
        wolf.setId(3);
        wolf.setPermissions(List.of(INACTIVE));

        when(departmentService.getMembersForDepartmentHead(signedInUser)).thenReturn(List.of(wolf));
        when(departmentService.getMembersForSecondStageAuthority(signedInUser)).thenReturn(List.of(wolf, wayne));

        perform(get("/web/person").param("active", "false"))
            .andExpect(
                model().attribute("persons", contains(
                    hasProperty("lastName", is("Wayne")),
                    hasProperty("lastName", is("Xavier"))
                ))
            );
    }

    @Test
    void showPersonForUnknownDepartmentIdThrowsUnknownDepartmentException() {

        when(personService.getSignedInUser()).thenReturn(person);
        assertThatThrownBy(() ->
            perform(get("/web/person")
                .param("active", "false")
                .param(DEPARTMENT_ATTRIBUTE, Integer.toString(UNKNOWN_DEPARTMENT_ID)))

        ).hasCauseInstanceOf(UnknownDepartmentException.class);
    }

    @Test
    void showPersonUsesDepartmentWithGivenId() throws Exception {

        final Department department = new Department();

        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getDepartmentById(PERSON_ID)).thenReturn(Optional.of(department));

        perform(get("/web/person")
            .param("active", "true")
            .param(DEPARTMENT_ATTRIBUTE, "1")
        ).andExpect(model().attribute(DEPARTMENT_ATTRIBUTE, department));
    }

    @Test
    void showPersonUsesDepartmentUsesCorrectView() throws Exception {

        when(personService.getSignedInUser()).thenReturn(person);
        perform(get("/web/person").param("active", "true"))
            .andExpect(view().name("person/person_view"));
    }

    @Test
    void showPersonWithActiveFlagUsesGivenYear() throws Exception {

        when(personService.getSignedInUser()).thenReturn(person);
        perform(get("/web/person/")
            .param("active", "true")
            .param(YEAR_ATTRIBUTE, "1985")
        ).andExpect(model().attribute(YEAR_ATTRIBUTE, 1985));
    }

    @Test
    void showPersonWithActiveFlagUsesCurrentYearIfNoYearGiven() throws Exception {

        when(personService.getSignedInUser()).thenReturn(person);
        final int currentYear = Year.now(clock).getValue();

        perform(get("/web/person/").param("active", "true"))
            .andExpect(model().attribute(YEAR_ATTRIBUTE, currentYear));
    }

    @Test
    void ensureCorrectDepartmentsForBoss() throws Exception {

        final Person boss = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(boss);

        final Department department = new Department();
        when(departmentService.getAllDepartments()).thenReturn(List.of(department));

        perform(get("/web/person/").param("active", "true"))
            .andExpect(model().attribute("departments", hasSize(1)));

        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void ensureCorrectDepartmentsForOffice() throws Exception {

        final Person office = personWithRole(USER, OFFICE);
        when(personService.getSignedInUser()).thenReturn(office);

        final Department department = new Department();
        when(departmentService.getAllDepartments()).thenReturn(List.of(department));

        perform(get("/web/person/").param("active", "true"))
            .andExpect(model().attribute("departments", hasSize(1)));

        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void ensureCorrectDepartmentsForDepartmentHead() throws Exception {

        final Person departmentHead = personWithRole(USER, DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(departmentHead);

        final Department department = new Department();
        when(departmentService.getManagedDepartmentsOfDepartmentHead(departmentHead)).thenReturn(List.of(department));

        perform(get("/web/person/").param("active", "true"))
            .andExpect(model().attribute("departments", hasSize(1)));
    }

    @Test
    void ensureCorrectDepartmentsForSecondStageAuthority() throws Exception {

        final Person secondStageAuthority = personWithRole(USER, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(secondStageAuthority);

        final Department department = new Department();
        when(departmentService.getManagedDepartmentsOfSecondStageAuthority(secondStageAuthority)).thenReturn(List.of(department));

        perform(get("/web/person/").param("active", "true"))
            .andExpect(model().attribute("departments", hasSize(1)));
    }

    @Test
    void ensureCorrectDepartmentsForDepartmentHeadAndSecondStageAuthority() throws Exception {

        final Person departmentHeadAndSecondStageAuthority = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSecondStageAuthority);

        final Department department = new Department();
        department.setId(1);
        department.setName("1");
        final Department department2 = new Department();
        department2.setId(2);
        department2.setName("2");
        final Department department3 = new Department();
        department3.setId(3);
        department3.setName("3");

        when(departmentService.getManagedDepartmentsOfDepartmentHead(departmentHeadAndSecondStageAuthority)).thenReturn(List.of(department, department3));
        when(departmentService.getManagedDepartmentsOfSecondStageAuthority(departmentHeadAndSecondStageAuthority)).thenReturn(List.of(department, department2));

        perform(get("/web/person/").param("active", "true"))
            .andExpect(model().attribute("departments", hasSize(3)));
    }

    @Test
    void ensureCorrectDepartmentsForUser() throws Exception {

        final Person user = personWithRole(USER);
        when(personService.getSignedInUser()).thenReturn(user);

        final Department department = new Department();
        when(departmentService.getAssignedDepartmentsOfMember(user)).thenReturn(List.of(department));

        perform(get("/web/person/").param("active", "true"))
            .andExpect(model().attribute("departments", hasSize(1)));
    }

    @Test
    void showPersonWithPersonnelNumberIfPresent() throws Exception {

        final Person signedInUser = personWithRole(USER, OFFICE);
        signedInUser.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person wayne = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        wayne.setId(2);
        wayne.setPermissions(List.of(USER));

        final Person wolf = new Person("red.wolf", "Xavier", "Bruce", "red.wolf@example.org");
        wolf.setId(3);
        wolf.setPermissions(List.of(USER));

        when(personService.getActivePersons()).thenReturn(List.of(wayne, wolf));
        when(personBasedataService.getBasedataByPersonId(2)).thenReturn(Optional.of(new PersonBasedata(2, "42", null)));

        perform(get("/web/person").param("active", "true"))
            .andExpect(model().attribute("persons", hasSize(2)))
            .andExpect(
                model()
                    .attribute("persons", contains(
                        hasProperty("personnelNumber", is("42")),
                        hasProperty("lastName", is("Xavier"))
                    ))
            ).andExpect(model().attribute("showPersonnelNumberColumn", true));
    }


    @Test
    void showNoPersonnelNumberColumnIfPersonnelNumberIsPresent() throws Exception {

        final Person signedInUser = personWithRole(USER, OFFICE);
        signedInUser.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person wayne = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        wayne.setId(2);
        wayne.setPermissions(List.of(USER));

        final Person wolf = new Person("red.wolf", "Xavier", "Bruce", "red.wolf@example.org");
        wolf.setId(3);
        wolf.setPermissions(List.of(USER));

        when(personService.getActivePersons()).thenReturn(List.of(wayne, wolf));

        perform(get("/web/person").param("active", "true"))
            .andExpect(model().attribute("showPersonnelNumberColumn", false));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }

    private static Person personWithRole(Role... role) {
        final Person person = new Person();
        person.setId(1);
        person.setPermissions(Arrays.asList(role));

        return person;
    }
}
