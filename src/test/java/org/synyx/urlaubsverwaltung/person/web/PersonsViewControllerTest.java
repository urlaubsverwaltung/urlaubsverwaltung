package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.department.web.UnknownDepartmentException;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


@ExtendWith(MockitoExtension.class)
class PersonsViewControllerTest {

    private static Clock clock;

    private PersonsViewController sut;

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

    @BeforeEach
    void setUp() {
        clock = Clock.systemUTC();
        sut = new PersonsViewController(personService, accountService, vacationDaysService, departmentService, personBasedataService, clock);
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleBossCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getActivePersons(defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person"))
            .andExpect(model().attribute("personPage", hasProperty("content", hasSize(0))));
    }

    @Test
    void ensurePersonsPageTurboFrameRenderedWhenHeaderIsSet() throws Exception {

        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getActivePersons(defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person").header("Turbo-Frame", "persons-frame"))
            .andExpect(view().name("thymeleaf/person/persons::#persons-frame"));
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleOfficeCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, OFFICE);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getActivePersons(defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person"))
            .andExpect(model().attribute("personPage", hasProperty("content", hasSize(0))));
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleDepartmentHeadCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(departmentService.getManagedMembersOfPerson(signedInUser, defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person"))
            .andExpect(model().attribute("personPage", hasProperty("content", hasSize(0))));
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(departmentService.getManagedMembersOfPerson(signedInUser, defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person"))
            .andExpect(model().attribute("personPage", hasProperty("content", hasSize(0))));
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleDepartmentHeadAndSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(departmentService.getManagedMembersOfPerson(signedInUser, defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person"))
            .andExpect(model().attribute("personPage", hasProperty("content", hasSize(0))));
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleDepartmentHeadAndSecondStageAuthorityDistinctPersons() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person("username", "Cloud", "Sky", "sky@exaple.org");
        person.setId(2);

        final PageImpl<Person> page = new PageImpl<>(List.of(person));
        when(departmentService.getManagedMembersOfPerson(signedInUser, defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person"))
            .andExpect(model().attribute("personPage", hasProperty("content", allOf(
                hasSize(1),
                contains(
                    hasProperty("firstName", is("Sky"))
                )
            ))));
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleBossCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getInactivePersons(defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person").param("active", "false"))
            .andExpect(model().attribute("personPage", hasProperty("content", hasSize(0))));
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleOfficeCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, OFFICE);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getInactivePersons(defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person").param("active", "false"))
            .andExpect(model().attribute("personPage", hasProperty("content", hasSize(0))));
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleDepartmentHeadCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(departmentService.getManagedInactiveMembersOfPerson(signedInUser, defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person").param("active", "false"))
            .andExpect(model().attribute("personPage", hasProperty("content", hasSize(0))));
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(departmentService.getManagedInactiveMembersOfPerson(signedInUser, defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person").param("active", "false"))
            .andExpect(model().attribute("personPage", hasProperty("content", hasSize(0))));
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleDepartmentHeadAndSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(departmentService.getManagedInactiveMembersOfPerson(signedInUser, defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person").param("active", "false"))
            .andExpect(model().attribute("personPage", hasProperty("content", hasSize(0))));
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleDepartmentHeadAndSecondStageAuthorityDistinctPersons() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person("username", "Cloud", "Sky", "sky@exaple.org");
        person.setId(2);
        person.setPermissions(List.of(INACTIVE));

        final PageImpl<Person> page = new PageImpl<>(List.of(person));
        when(departmentService.getManagedInactiveMembersOfPerson(signedInUser, defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person").param("active", "false"))
            .andExpect(model().attribute("personPage", hasProperty("content", allOf(
                hasSize(1),
                contains(
                    hasProperty("firstName", is("Sky"))
                )
            ))));
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

        final PageImpl<Person> page = new PageImpl<>(List.of(bruce, clark));
        when(departmentService.getManagedInactiveMembersOfPerson(signedInUser, defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person").param("active", "false"))
            .andExpect(
                model().attribute("personPage", hasProperty("content", contains(
                    hasProperty("firstName", is("Bruce")),
                    hasProperty("firstName", is("Clark"))
                )))
            );
    }

    @Test
    void showPersonForUnknownDepartmentIdThrowsUnknownDepartmentException() {
        final Person person = new Person();
        person.setId(1);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        when(personService.getSignedInUser()).thenReturn(person);

        assertThatThrownBy(() ->
            perform(get("/web/person")
                .param("active", "false")
                .param("department", "456"))
        ).hasCauseInstanceOf(UnknownDepartmentException.class);
    }

    @Test
    void showPersonUsesDepartmentWithGivenId() throws Exception {

        final Person signedInUser = personWithRole(BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Department department = new Department();
        department.setName("awesome-department");
        department.setMembers(List.of());
        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(department));

        final Person john = new Person();
        john.setId(2);
        john.setFirstName("John");

        final PageImpl<Person> page = new PageImpl<>(List.of(john));
        when(departmentService.getManagedMembersOfPersonAndDepartment(signedInUser, 1, defaultPersonSearchQuery())).thenReturn(page);

        when(departmentService.isPersonAllowedToManageDepartment(signedInUser, department)).thenReturn(true);

        perform(get("/web/person")
            .param("department", "1")
        )
            .andExpect(model().attribute("department", hasProperty("name", is("awesome-department"))))
            .andExpect(model().attribute("personPage", hasProperty("content", allOf(
                hasSize(1),
                contains(
                    hasProperty("firstName", is("John"))
                )
            ))));
    }

    @Test
    void showDepartmentHeadUsesDepartmentWithGivenId() throws Exception {

        final Person signedInUser = personWithRole(DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Department department = new Department();
        department.setName("awesome-department");
        department.setMembers(List.of(signedInUser));
        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(department));

        final Person john = new Person();
        john.setId(2);
        john.setFirstName("John");

        final PageImpl<Person> page = new PageImpl<>(List.of(john));
        when(departmentService.getManagedMembersOfPersonAndDepartment(signedInUser, 1, defaultPersonSearchQuery())).thenReturn(page);

        when(departmentService.isPersonAllowedToManageDepartment(signedInUser, department)).thenReturn(true);

        perform(get("/web/person")
            .param("department", "1")
        )
            .andExpect(model().attribute("department", hasProperty("name", is("awesome-department"))))
            .andExpect(model().attribute("personPage", hasProperty("content", allOf(
                hasSize(1),
                contains(
                    hasProperty("firstName", is("John"))
                )
            ))));
    }

    @Test
    void showDepartmentHeadUsesDepartmentWithGivenIdButNotHisDepartment() throws Exception {

        final Person signedInUser = personWithRole(DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Department department = new Department();
        department.setName("awesome-department");
        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(department));

        final Person john = new Person();
        john.setId(2);
        john.setFirstName("John");

        final PageImpl<Person> page = new PageImpl<>(List.of(john));
        when(departmentService.getManagedMembersOfPerson(signedInUser, defaultPersonSearchQuery())).thenReturn(page);

        when(departmentService.isPersonAllowedToManageDepartment(signedInUser, department)).thenReturn(false);

        perform(get("/web/person")
            .param("department", "1")
        )
            .andExpect(model().attribute("department", nullValue()))
            .andExpect(model().attribute("personPage", hasProperty("content", allOf(
                hasSize(1),
                contains(
                    hasProperty("firstName", is("John"))
                )
            ))));
    }

    @Test
    void showPersonUsesDepartmentUsesCorrectView() throws Exception {
        final Person person = new Person();
        person.setId(1);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        when(personService.getSignedInUser()).thenReturn(person);

        mockDefaultPageRequest(person);

        perform(get("/web/person"))
            .andExpect(view().name("thymeleaf/person/persons"));
    }

    @Test
    void showPersonWithActiveFlagUsesGivenYear() throws Exception {

        clock = Clock.fixed(Instant.parse("2022-08-04T06:00:00Z"), ZoneId.of("UTC"));
        sut = new PersonsViewController(personService, accountService, vacationDaysService, departmentService, personBasedataService, clock);

        final Person person = new Person();
        person.setId(1);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        when(personService.getSignedInUser()).thenReturn(person);

        mockDefaultPageRequest(person);

        perform(get("/web/person/")

            .param("year", "1985")
        )
            .andExpect(model().attribute("selectedYear", 1985))
            .andExpect(model().attribute("currentYear", 2022));
    }

    @Test
    void showPersonWithActiveFlagUsesCurrentYearIfNoYearGiven() throws Exception {

        clock = Clock.fixed(Instant.parse("2022-08-04T06:00:00Z"), ZoneId.of("UTC"));
        sut = new PersonsViewController(personService, accountService, vacationDaysService, departmentService, personBasedataService, clock);

        final Person person = new Person();
        person.setId(1);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        when(personService.getSignedInUser()).thenReturn(person);

        mockDefaultPageRequest(person);

        perform(get("/web/person/"))
            .andExpect(model().attribute("selectedYear", 2022))
            .andExpect(model().attribute("currentYear", 2022));
    }

    @Test
    void ensureCorrectDepartmentsForBoss() throws Exception {

        final Person boss = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(boss);

        final Department department = new Department();
        when(departmentService.getAllDepartments()).thenReturn(List.of(department));

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getActivePersons(defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person/"))
            .andExpect(model().attribute("departments", hasSize(1)));

        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void ensureCorrectDepartmentsForOffice() throws Exception {

        final Person office = personWithRole(USER, OFFICE);
        when(personService.getSignedInUser()).thenReturn(office);

        final Department department = new Department();
        when(departmentService.getAllDepartments()).thenReturn(List.of(department));

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getActivePersons(defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person/"))
            .andExpect(model().attribute("departments", hasSize(1)));

        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void ensureCorrectDepartmentsForDepartmentHead() throws Exception {

        final Person departmentHead = personWithRole(USER, DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(departmentHead);

        mockDefaultPageRequest(departmentHead);

        final Department department = new Department();
        when(departmentService.getManagedDepartmentsOfDepartmentHead(departmentHead)).thenReturn(List.of(department));

        perform(get("/web/person/"))
            .andExpect(model().attribute("departments", hasSize(1)));
    }

    @Test
    void ensureCorrectDepartmentsForSecondStageAuthority() throws Exception {

        final Person secondStageAuthority = personWithRole(USER, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(secondStageAuthority);

        mockDefaultPageRequest(secondStageAuthority);

        final Department department = new Department();
        department.setId(1);
        department.setName("awesome-department");
        when(departmentService.getManagedDepartmentsOfSecondStageAuthority(secondStageAuthority)).thenReturn(List.of(department));

        perform(get("/web/person/"))
            .andExpect(
                model().attribute("departments", allOf(
                    hasSize(1),
                    contains(
                        hasProperty("name", is("awesome-department"))
                    )
                ))
            );
    }

    @Test
    void ensureCorrectDepartmentsForDepartmentHeadAndSecondStageAuthority() throws Exception {

        final Person departmentHeadAndSecondStageAuthority = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSecondStageAuthority);

        mockDefaultPageRequest(departmentHeadAndSecondStageAuthority);

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

        perform(get("/web/person/"))
            .andExpect(model().attribute("departments", hasSize(3)));
    }

    @Test
    void ensureCorrectDepartmentsForUser() throws Exception {

        final Person user = personWithRole(USER);
        when(personService.getSignedInUser()).thenReturn(user);

        mockDefaultPageRequest(user);

        final Department department = new Department();
        when(departmentService.getAssignedDepartmentsOfMember(user)).thenReturn(List.of(department));

        perform(get("/web/person/"))
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

        final PageImpl<Person> page = new PageImpl<>(List.of(wayne, wolf));
        when(personService.getActivePersons(defaultPersonSearchQuery())).thenReturn(page);

        when(personBasedataService.getBasedataByPersonId(2)).thenReturn(Optional.of(new PersonBasedata(new PersonId(2), "42", null)));

        perform(get("/web/person"))
            .andExpect(
                model()
                    .attribute("personPage", hasProperty("content", allOf(
                        hasSize(2),
                        contains(
                            hasProperty("personnelNumber", is("42")),
                            hasProperty("lastName", is("Xavier"))
                        )
                    )))
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

        final PageImpl<Person> page = new PageImpl<>(List.of(wayne, wolf));
        when(personService.getActivePersons(defaultPersonSearchQuery())).thenReturn(page);

        perform(get("/web/person"))
            .andExpect(model().attribute("showPersonnelNumberColumn", false));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build()
            .perform(builder);
    }

    private static PageableSearchQuery defaultPersonSearchQuery() {
        return new PageableSearchQuery(defaultPageRequest(), "");
    }

    /**
     * create a default PageRequest used to invoke services.
     * Note the difference to the pageable consumed by the view controller which has keys like `person.firstName` or `account.someAttribute`.
     *
     * @return {@link PageRequest} passed to the services
     */
    private static PageRequest defaultPageRequest() {
        return PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "firstName"));
    }

    private void mockDefaultPageRequest(Person signedInUser) {
        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(departmentService.getManagedMembersOfPerson(signedInUser, defaultPersonSearchQuery())).thenReturn(page);
    }

    private static Person personWithRole(Role... role) {
        final Person person = new Person();
        person.setId(1);
        person.setPermissions(Arrays.asList(role));

        return person;
    }
}
