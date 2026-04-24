package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.HolidayAccountVacationDays;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.department.web.UnknownDepartmentException;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonPageRequest;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.web.html.HtmlOptgroupDto;
import org.synyx.urlaubsverwaltung.web.html.HtmlOptionDto;
import org.synyx.urlaubsverwaltung.web.html.HtmlSelectDto;
import org.synyx.urlaubsverwaltung.web.html.PaginationDto;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.time.LocalDate.of;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    @Mock
    private DataWebProperties dataWebProperties;

    final DataWebProperties.Pageable pageableProperties = new DataWebProperties.Pageable();

    @BeforeEach
    void setUp() {
        clock = Clock.systemUTC();

        pageableProperties.setOneIndexedParameters(true);
        pageableProperties.setPageParameter("page");
        lenient().when(dataWebProperties.getPageable()).thenReturn(pageableProperties);

        sut = new PersonsViewController(personService, accountService, vacationDaysService, departmentService, personBasedataService, dataWebProperties, clock);
    }

    @Test
    void ensurePersonPaginationDefault() throws Exception {

        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(page);

        final PaginationDto<Person> expectedPagination = new PaginationDto<>(
            new PageImpl<>(List.of()),
            "?active=true&query=&sort=person.firstName,ASC&size=20",
            pageableProperties
        );

        perform(get("/web/person"))
            .andExpect(model().attribute("personsPagination", is(expectedPagination)));
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleBossCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(page);

        perform(get("/web/person"))
            .andExpect(model().attribute("personsPagination", hasProperty("page", hasProperty("content", hasSize(0)))));
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleOfficeCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, OFFICE);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(page);

        perform(get("/web/person"))
            .andExpect(model().attribute("personsPagination", hasProperty("page", hasProperty("content", hasSize(0)))));
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleDepartmentHeadCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(departmentService.getManagedMembersOfPerson(signedInUser, defaultPageRequest(), "")).thenReturn(page);

        perform(get("/web/person"))
            .andExpect(model().attribute("personsPagination", hasProperty("page", hasProperty("content", hasSize(0)))));
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(departmentService.getManagedMembersOfPerson(signedInUser, defaultPageRequest(), "")).thenReturn(page);

        perform(get("/web/person"))
            .andExpect(model().attribute("personsPagination", hasProperty("page", hasProperty("content", hasSize(0)))));
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleDepartmentHeadAndSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(departmentService.getManagedMembersOfPerson(signedInUser, defaultPageRequest(), "")).thenReturn(page);

        perform(get("/web/person"))
            .andExpect(model().attribute("personsPagination", hasProperty("page", hasProperty("content", hasSize(0)))));
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleDepartmentHeadAndSecondStageAuthorityDistinctPersons() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person("username", "Cloud", "Sky", "sky@exaple.org");
        person.setId(2L);

        final PageImpl<Person> page = new PageImpl<>(List.of(person));
        when(departmentService.getManagedMembersOfPerson(signedInUser, defaultPageRequest(), "")).thenReturn(page);

        perform(get("/web/person"))
            .andExpect(model().attribute("personsPagination",
                hasProperty("page",
                    hasProperty("content", allOf(
                        hasSize(1),
                        contains(
                            hasProperty("firstName", is("Sky"))
                        )
                    ))
                )
            ));
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleBossCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getInactivePersons(defaultPageRequest(), "")).thenReturn(page);

        perform(get("/web/person").param("active", "false"))
            .andExpect(model().attribute("personsPagination", hasProperty("page", hasProperty("content", hasSize(0)))));
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleOfficeCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, OFFICE);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getInactivePersons(defaultPageRequest(), "")).thenReturn(page);

        perform(get("/web/person").param("active", "false"))
            .andExpect(model().attribute("personsPagination", hasProperty("page", hasProperty("content", hasSize(0)))));
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleDepartmentHeadCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(departmentService.getManagedInactiveMembersOfPerson(signedInUser, defaultPageRequest(), "")).thenReturn(page);

        perform(get("/web/person").param("active", "false"))
            .andExpect(model().attribute("personsPagination", hasProperty("page", hasProperty("content", hasSize(0)))));
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(departmentService.getManagedInactiveMembersOfPerson(signedInUser, defaultPageRequest(), "")).thenReturn(page);

        perform(get("/web/person").param("active", "false"))
            .andExpect(model().attribute("personsPagination", hasProperty("page", hasProperty("content", hasSize(0)))));
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleDepartmentHeadAndSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(departmentService.getManagedInactiveMembersOfPerson(signedInUser, defaultPageRequest(), "")).thenReturn(page);

        perform(get("/web/person").param("active", "false"))
            .andExpect(model().attribute("personsPagination", hasProperty("page", hasProperty("content", hasSize(0)))));
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleDepartmentHeadAndSecondStageAuthorityDistinctPersons() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person("username", "Cloud", "Sky", "sky@exaple.org");
        person.setId(2L);
        person.setPermissions(List.of(INACTIVE));

        final PageImpl<Person> page = new PageImpl<>(List.of(person));
        when(departmentService.getManagedInactiveMembersOfPerson(signedInUser, defaultPageRequest(), "")).thenReturn(page);

        perform(get("/web/person").param("active", "false"))
            .andExpect(model().attribute("personsPagination",
                hasProperty("page",
                    hasProperty("content", allOf(
                        hasSize(1),
                        contains(
                            hasProperty("firstName", is("Sky"))
                        )
                    ))
                )
            ));
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleDepartmentHeadAndSecondStageAuthorityPersonsSortedByFirstname() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person bruce = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        bruce.setId(2L);
        bruce.setPermissions(List.of(INACTIVE));

        final Person clark = new Person("superman", "Kent", "Clark", "superman@example.org");
        clark.setId(3L);
        clark.setPermissions(List.of(INACTIVE));

        final PageImpl<Person> page = new PageImpl<>(List.of(bruce, clark));
        when(departmentService.getManagedInactiveMembersOfPerson(signedInUser, defaultPageRequest(), "")).thenReturn(page);

        perform(get("/web/person").param("active", "false"))
            .andExpect(
                model().attribute("personsPagination",
                    hasProperty("page",
                        hasProperty("content", contains(
                            hasProperty("firstName", is("Bruce")),
                            hasProperty("firstName", is("Clark"))
                        ))
                    )
                )
            );
    }

    @Nested
    class EnsureSortedPersonList {

        private Person thorsten;
        private Person theresa;
        private Person juliane;

        @BeforeEach
        void setUp() {

            final Person office = personWithRole(USER, OFFICE);
            office.setId(1L);
            when(personService.getSignedInUser()).thenReturn(office);

            thorsten = personWithRole(USER);
            thorsten.setId(2L);
            thorsten.setFirstName("Thorsten");

            theresa = personWithRole(USER);
            theresa.setId(3L);
            theresa.setFirstName("Theresa");

            juliane = personWithRole(USER);
            juliane.setId(4L);
            juliane.setFirstName("Juliane");
        }

        @Test
        void ensureSortedByFirstnameAscending() throws Exception {

            final PersonPageRequest expectedPersonPageRequest = PersonPageRequest.ofApiPageable(PageRequest.of(0, 12)
                .withSort(Sort.by("person.firstName")));

            final PageImpl<Person> page = new PageImpl<>(List.of(juliane, theresa, thorsten));
            when(personService.getActivePersons(expectedPersonPageRequest, "")).thenReturn(page);

            perform(get("/web/person")
                .param("sort", "person.firstName%2Casc")
                .param("size", "12")
            )
                .andExpect(status().isOk())
                .andExpect(model().attribute("personsPagination",
                    hasProperty("page",
                        hasProperty("content", allOf(
                            hasSize(3),
                            contains(
                                hasProperty("firstName", is("Juliane")),
                                hasProperty("firstName", is("Theresa")),
                                hasProperty("firstName", is("Thorsten"))
                            ))
                        )
                    )
                ));
        }

        @Test
        void ensureSortedByFirstnameDescending() throws Exception {

            final PersonPageRequest expectedPersonPageRequest = PersonPageRequest.ofApiPageable(PageRequest.of(0, 12)
                .withSort(Sort.by("person.firstName").descending()));

            final PageImpl<Person> page = new PageImpl<>(List.of(thorsten, theresa, juliane));
            when(personService.getActivePersons(expectedPersonPageRequest, "")).thenReturn(page);

            perform(get("/web/person")
                .param("sort", "person.firstName%2Cdesc")
                .param("size", "12")
            )
                .andExpect(status().isOk())
                .andExpect(model().attribute("personsPagination",
                    hasProperty("page",
                        hasProperty("content", allOf(
                            hasSize(3),
                            contains(
                                hasProperty("firstName", is("Thorsten")),
                                hasProperty("firstName", is("Theresa")),
                                hasProperty("firstName", is("Juliane"))
                            ))
                        )
                    )
                ));
        }
    }

    @Test
    void showPersonForUnknownDepartmentIdThrowsUnknownDepartmentException() {
        final Person person = new Person();
        person.setId(1L);
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
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));

        final Person john = new Person();
        john.setId(2L);
        john.setFirstName("John");

        final PageImpl<Person> page = new PageImpl<>(List.of(john));
        when(departmentService.getManagedMembersOfPersonAndDepartment(signedInUser, 1L, defaultPageRequest(), "")).thenReturn(page);

        when(departmentService.isPersonAllowedToManageDepartment(signedInUser, department)).thenReturn(true);

        perform(get("/web/person")
            .param("department", "1")
        )
            .andExpect(model().attribute("department", hasProperty("name", is("awesome-department"))))
            .andExpect(model().attribute("personsPagination",
                hasProperty("page",
                    hasProperty("content", allOf(
                        hasSize(1),
                        contains(
                            hasProperty("firstName", is("John"))
                        ))
                    )
                )
            ));
    }

    @Test
    void showDepartmentHeadUsesDepartmentWithGivenId() throws Exception {

        final Person signedInUser = personWithRole(DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Department department = new Department();
        department.setName("awesome-department");
        department.setMembers(List.of(signedInUser));
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));

        final Person john = new Person();
        john.setId(2L);
        john.setFirstName("John");

        final PageImpl<Person> page = new PageImpl<>(List.of(john));
        when(departmentService.getManagedMembersOfPersonAndDepartment(signedInUser, 1L, defaultPageRequest(), "")).thenReturn(page);

        when(departmentService.isPersonAllowedToManageDepartment(signedInUser, department)).thenReturn(true);

        perform(get("/web/person")
            .param("department", "1")
        )
            .andExpect(model().attribute("department", hasProperty("name", is("awesome-department"))))
            .andExpect(model().attribute("personsPagination", hasProperty("page",
                hasProperty("content", allOf(
                        hasSize(1),
                        contains(
                            hasProperty("firstName", is("John"))
                        )
                    )
                ))));
    }

    @Test
    void showDepartmentHeadUsesDepartmentWithGivenIdButNotHisDepartment() throws Exception {

        final Person signedInUser = personWithRole(DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Department department = new Department();
        department.setName("awesome-department");
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));

        final Person john = new Person();
        john.setId(2L);
        john.setFirstName("John");

        final PageImpl<Person> page = new PageImpl<>(List.of(john));
        when(departmentService.getManagedMembersOfPerson(signedInUser, defaultPageRequest(), "")).thenReturn(page);

        when(departmentService.isPersonAllowedToManageDepartment(signedInUser, department)).thenReturn(false);

        perform(get("/web/person")
            .param("department", "1")
        )
            .andExpect(model().attribute("department", nullValue()))
            .andExpect(model().attribute("personsPagination",
                hasProperty("page",
                    hasProperty("content", allOf(
                            hasSize(1),
                            contains(
                                hasProperty("firstName", is("John"))
                            )
                        )
                    )
                )));
    }

    @Test
    void showPersonUsesDepartmentUsesCorrectView() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        when(personService.getSignedInUser()).thenReturn(person);

        mockDefaultPageRequest(person);

        perform(get("/web/person"))
            .andExpect(view().name("person/persons"));
    }

    @Test
    void showPersonWithActiveFlagUsesGivenYear() throws Exception {

        clock = Clock.fixed(Instant.parse("2022-08-04T06:00:00Z"), ZoneId.of("UTC"));
        sut = new PersonsViewController(personService, accountService, vacationDaysService, departmentService, personBasedataService, dataWebProperties, clock);

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        when(personService.getSignedInUser()).thenReturn(person);

        mockDefaultPageRequest(person);

        perform(get("/web/person")

            .param("year", "1985")
        )
            .andExpect(model().attribute("selectedYear", 1985))
            .andExpect(model().attribute("currentYear", 2022));
    }

    @Test
    void showPersonWithActiveFlagUsesCurrentYearIfNoYearGiven() throws Exception {

        clock = Clock.fixed(Instant.parse("2022-08-04T06:00:00Z"), ZoneId.of("UTC"));
        sut = new PersonsViewController(personService, accountService, vacationDaysService, departmentService, personBasedataService, dataWebProperties, clock);

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        when(personService.getSignedInUser()).thenReturn(person);

        mockDefaultPageRequest(person);

        perform(get("/web/person"))
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
        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(page);

        perform(get("/web/person"))
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
        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(page);

        perform(get("/web/person"))
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

        perform(get("/web/person"))
            .andExpect(model().attribute("departments", hasSize(1)));
    }

    @Test
    void ensureCorrectDepartmentsForSecondStageAuthority() throws Exception {

        final Person secondStageAuthority = personWithRole(USER, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(secondStageAuthority);

        mockDefaultPageRequest(secondStageAuthority);

        final Department department = new Department();
        department.setId(1L);
        department.setName("awesome-department");
        when(departmentService.getManagedDepartmentsOfSecondStageAuthority(secondStageAuthority)).thenReturn(List.of(department));

        perform(get("/web/person"))
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
        department.setId(1L);
        department.setName("1");
        final Department department2 = new Department();
        department2.setId(2L);
        department2.setName("2");
        final Department department3 = new Department();
        department3.setId(3L);
        department3.setName("3");

        when(departmentService.getManagedDepartmentsOfDepartmentHead(departmentHeadAndSecondStageAuthority)).thenReturn(List.of(department, department3));
        when(departmentService.getManagedDepartmentsOfSecondStageAuthority(departmentHeadAndSecondStageAuthority)).thenReturn(List.of(department, department2));

        perform(get("/web/person"))
            .andExpect(model().attribute("departments", hasSize(3)));
    }

    @Test
    void ensureCorrectDepartmentsForUser() throws Exception {

        final Person user = personWithRole(USER);
        when(personService.getSignedInUser()).thenReturn(user);

        mockDefaultPageRequest(user);

        final Department department = new Department();
        when(departmentService.getAssignedDepartmentsOfMember(user)).thenReturn(List.of(department));

        perform(get("/web/person"))
            .andExpect(model().attribute("departments", hasSize(1)));
    }

    @Test
    void showPersonWithPersonnelNumberIfPresent() throws Exception {

        final Person signedInUser = personWithRole(USER, OFFICE);
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person wayne = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        wayne.setId(2L);
        wayne.setPermissions(List.of(USER));

        final Person wolf = new Person("red.wolf", "Xavier", "Bruce", "red.wolf@example.org");
        wolf.setId(3L);
        wolf.setPermissions(List.of(USER));

        final PageImpl<Person> page = new PageImpl<>(List.of(wayne, wolf));
        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(page);

        when(personBasedataService.getBasedataByPersonId(2)).thenReturn(Optional.of(new PersonBasedata(new PersonId(2L), "42", null)));

        perform(get("/web/person"))
            .andExpect(
                model().attribute("personsPagination",
                    hasProperty("page",
                        hasProperty("content", allOf(
                            hasSize(2),
                            contains(
                                hasProperty("personnelNumber", is("42")),
                                hasProperty("lastName", is("Xavier"))
                            )
                        ))
                    )
                )
            ).andExpect(model().attribute("showPersonnelNumberColumn", true));
    }


    @Test
    void showNoPersonnelNumberColumnIfPersonnelNumberIsPresent() throws Exception {

        final Person signedInUser = personWithRole(USER, OFFICE);
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person wayne = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        wayne.setId(2L);
        wayne.setPermissions(List.of(USER));

        final Person wolf = new Person("red.wolf", "Xavier", "Bruce", "red.wolf@example.org");
        wolf.setId(3L);
        wolf.setPermissions(List.of(USER));

        final PageImpl<Person> page = new PageImpl<>(List.of(wayne, wolf));
        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(page);

        perform(get("/web/person"))
            .andExpect(model().attribute("showPersonnelNumberColumn", false));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "true,0",
        "false,5"
    })
    void ensuresThatRemainingVacationDaysLeftAreOnlyDisplayedIfTheyDoNotExpire(final boolean doExpire, final BigDecimal remainingVacationDays) throws Exception {

        clock = Clock.fixed(Instant.parse("2022-04-02T06:00:00Z"), ZoneId.of("UTC"));
        sut = new PersonsViewController(personService, accountService, vacationDaysService, departmentService, personBasedataService, dataWebProperties, clock);

        final Person signedInUser = personWithRole(USER, OFFICE);
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        person.setId(2L);
        person.setPermissions(List.of(USER));

        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(new PageImpl<>(List.of(person)));

        final Year year = Year.now(clock);
        final LocalDate startDate = LocalDate.of(year.getValue(), JANUARY, 1);
        final LocalDate endDate = LocalDate.of(year.getValue(), DECEMBER, 31);
        final LocalDate expiryDate = of(year.getValue(), APRIL, 1);

        final Account account = new Account(person, startDate, endDate, doExpire, expiryDate, valueOf(30), remainingVacationDays, ZERO, null);
        account.setActualVacationDays(valueOf(30));
        when(accountService.getHolidaysAccount(year.getValue(), List.of(person))).thenReturn(List.of(account));

        final Account accountNextYear = new Account(person, startDate, endDate, doExpire, expiryDate, valueOf(30), remainingVacationDays, ZERO, null);
        accountNextYear.setActualVacationDays(valueOf(30));
        when(accountService.getHolidaysAccount(year.plusYears(1).getValue(), List.of(person))).thenReturn(List.of(accountNextYear));

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(valueOf(30))
            .withRemainingVacation(valueOf(5))
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account), year, List.of(accountNextYear)))
            .thenReturn(Map.of(account, new HolidayAccountVacationDays(account, vacationDaysLeft, vacationDaysLeft)));

        perform(get("/web/person"))
            .andExpect(
                model().attribute("personsPagination",
                    hasProperty("page",
                        hasProperty("content",
                            hasItems(
                                allOf(
                                    instanceOf(PersonDto.class),
                                    hasProperty("lastName", is("Wayne")),
                                    hasProperty("vacationDaysLeftRemaining", is(remainingVacationDays.doubleValue()))
                                )
                            )
                        )
                    )
                )
            );
    }

    @Test
    void ensuresThatRemainingVacationDaysLeftAreDisplayedIfBeforeExpireDate() throws Exception {

        clock = Clock.fixed(Instant.parse("2022-03-31T06:00:00Z"), ZoneId.of("UTC"));
        sut = new PersonsViewController(personService, accountService, vacationDaysService, departmentService, personBasedataService, dataWebProperties, clock);

        final Person signedInUser = personWithRole(USER, OFFICE);
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        person.setId(2L);
        person.setPermissions(List.of(USER));

        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(new PageImpl<>(List.of(person)));

        final Year year = Year.now(clock);
        final LocalDate startDate = LocalDate.of(year.getValue(), JANUARY, 1);
        final LocalDate endDate = LocalDate.of(year.getValue(), DECEMBER, 31);
        final LocalDate expiryDate = of(year.getValue(), APRIL, 1);

        final Account account = new Account(person, startDate, endDate, true, expiryDate, valueOf(30), valueOf(5), ZERO, null);
        account.setActualVacationDays(valueOf(30));
        when(accountService.getHolidaysAccount(year.getValue(), List.of(person))).thenReturn(List.of(account));

        final Account accountNextYear = new Account(person, startDate, endDate, true, expiryDate, valueOf(30), valueOf(5), ZERO, null);
        accountNextYear.setActualVacationDays(valueOf(30));
        when(accountService.getHolidaysAccount(year.plusYears(1).getValue(), List.of(person))).thenReturn(List.of(accountNextYear));

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(valueOf(30))
            .withRemainingVacation(valueOf(5))
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account), year, List.of(accountNextYear)))
            .thenReturn(Map.of(account, new HolidayAccountVacationDays(account, vacationDaysLeft, vacationDaysLeft)));

        perform(get("/web/person"))
            .andExpect(
                model().attribute("personsPagination",
                    hasProperty("page",
                        hasProperty("content",
                            hasItems(
                                allOf(
                                    instanceOf(PersonDto.class),
                                    hasProperty("lastName", is("Wayne")),
                                    hasProperty("vacationDaysLeftRemaining", is(5.0d))
                                )
                            )
                        )
                    )
                )
            );
    }

    // ==================== HtmlSelectDto Tests ====================

    @Test
    void ensureHtmlSelectDtoHasCorrectStructure() throws Exception {
        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(page);

        final ResultActions resultActions = perform(get("/web/person"));
        final HtmlSelectDto htmlSelectDto = extractSortSelect(resultActions);

        assertThat(htmlSelectDto).isNotNull();
        assertThat(htmlSelectDto.optgroups()).hasSize(3);
    }

    @Test
    void ensureHtmlSelectDtoPersonOptgroupHasCorrectLabel() throws Exception {
        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(page);

        final ResultActions resultActions = perform(get("/web/person"));
        final HtmlSelectDto htmlSelectDto = extractSortSelect(resultActions);

        final HtmlOptgroupDto personOptgroup = htmlSelectDto.optgroups().get(0);
        assertThat(personOptgroup.labelMessageKey()).isEqualTo("persons.sort.optgroup.person.label");
    }

    @Test
    void ensureHtmlSelectDtoUrlaubOptgroupHasCorrectLabel() throws Exception {
        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(page);

        final ResultActions resultActions = perform(get("/web/person"));
        final HtmlSelectDto htmlSelectDto = extractSortSelect(resultActions);

        final HtmlOptgroupDto urlaubOptgroup = htmlSelectDto.optgroups().get(1);
        assertThat(urlaubOptgroup.labelMessageKey()).isEqualTo("persons.sort.optgroup.urlaub.label");
    }

    @Test
    void ensureHtmlSelectDtoResturlaubOptgroupHasCorrectLabel() throws Exception {
        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(page);

        final ResultActions resultActions = perform(get("/web/person"));
        final HtmlSelectDto htmlSelectDto = extractSortSelect(resultActions);

        final HtmlOptgroupDto resturlaubOptgroup = htmlSelectDto.optgroups().get(2);
        assertThat(resturlaubOptgroup.labelMessageKey()).isEqualTo("persons.sort.optgroup.resturlaub.label");
    }

    @Test
    void ensureHtmlSelectDtoPersonOptgroupHasCorrectOptions() throws Exception {
        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(page);

        final ResultActions resultActions = perform(get("/web/person"));
        final HtmlSelectDto htmlSelectDto = extractSortSelect(resultActions);

        final HtmlOptgroupDto personOptgroup = htmlSelectDto.optgroups().get(0);
        final List<HtmlOptionDto> options = personOptgroup.options();

        assertThat(options).hasSize(4);

        assertThat(options.get(0).textMessageKey()).isEqualTo("persons.sort.firstName.asc");
        assertThat(options.get(0).value()).isEqualTo("person.firstName,asc");
        assertThat(options.get(1).textMessageKey()).isEqualTo("persons.sort.firstName.desc");
        assertThat(options.get(1).value()).isEqualTo("person.firstName,desc");
        assertThat(options.get(2).textMessageKey()).isEqualTo("persons.sort.lastName.asc");
        assertThat(options.get(2).value()).isEqualTo("person.lastName,asc");
        assertThat(options.get(3).textMessageKey()).isEqualTo("persons.sort.lastName.desc");
        assertThat(options.get(3).value()).isEqualTo("person.lastName,desc");
    }

    @Test
    void ensureHtmlSelectDtoUrlaubOptgroupHasCorrectOptions() throws Exception {
        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(page);

        final ResultActions resultActions = perform(get("/web/person"));
        final HtmlSelectDto htmlSelectDto = extractSortSelect(resultActions);

        final HtmlOptgroupDto urlaubOptgroup = htmlSelectDto.optgroups().get(1);
        final List<HtmlOptionDto> options = urlaubOptgroup.options();

        assertThat(options).hasSize(6);

        assertThat(options.get(0).textMessageKey()).isEqualTo("persons.sort.entitlementYear.asc");
        assertThat(options.get(0).value()).isEqualTo("account.entitlementYear,asc");
        assertThat(options.get(1).textMessageKey()).isEqualTo("persons.sort.entitlementYear.desc");
        assertThat(options.get(1).value()).isEqualTo("account.entitlementYear,desc");
        assertThat(options.get(2).textMessageKey()).isEqualTo("persons.sort.entitlementActual.asc");
        assertThat(options.get(2).value()).isEqualTo("account.entitlementActual,asc");
        assertThat(options.get(3).textMessageKey()).isEqualTo("persons.sort.entitlementActual.desc");
        assertThat(options.get(3).value()).isEqualTo("account.entitlementActual,desc");
        assertThat(options.get(4).textMessageKey()).isEqualTo("persons.sort.vacationDaysLeft.asc");
        assertThat(options.get(4).value()).isEqualTo("account.vacationDaysLeft,asc");
        assertThat(options.get(5).textMessageKey()).isEqualTo("persons.sort.vacationDaysLeft.desc");
        assertThat(options.get(5).value()).isEqualTo("account.vacationDaysLeft,desc");
    }

    @Test
    void ensureHtmlSelectDtoResturlaubOptgroupHasCorrectOptions() throws Exception {
        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(page);

        final ResultActions resultActions = perform(get("/web/person"));
        final HtmlSelectDto htmlSelectDto = extractSortSelect(resultActions);

        final HtmlOptgroupDto resturlaubOptgroup = htmlSelectDto.optgroups().get(2);
        final List<HtmlOptionDto> options = resturlaubOptgroup.options();

        assertThat(options).hasSize(4);

        assertThat(options.get(0).textMessageKey()).isEqualTo("persons.sort.entitlementRemaining.asc");
        assertThat(options.get(0).value()).isEqualTo("account.entitlementRemaining,asc");
        assertThat(options.get(1).textMessageKey()).isEqualTo("persons.sort.entitlementRemaining.desc");
        assertThat(options.get(1).value()).isEqualTo("account.entitlementRemaining,desc");
        assertThat(options.get(2).textMessageKey()).isEqualTo("persons.sort.vacationDaysLeftRemaining.asc");
        assertThat(options.get(2).value()).isEqualTo("account.vacationDaysLeftRemaining,asc");
        assertThat(options.get(3).textMessageKey()).isEqualTo("persons.sort.vacationDaysLeftRemaining.desc");
        assertThat(options.get(3).value()).isEqualTo("account.vacationDaysLeftRemaining,desc");
    }

    @Test
    void ensureHtmlSelectDtoWithDefaultSortHasFirstOptionSelected() throws Exception {
        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(personService.getActivePersons(defaultPageRequest(), "")).thenReturn(page);

        final ResultActions resultActions = perform(get("/web/person"));
        final HtmlSelectDto htmlSelectDto = extractSortSelect(resultActions);

        final HtmlOptgroupDto personOptgroup = htmlSelectDto.optgroups().get(0);
        final List<HtmlOptionDto> options = personOptgroup.options();

        assertThat(options.get(0).selected()).isTrue();
        assertThat(options.get(1).selected()).isFalse();
        assertThat(options.get(2).selected()).isFalse();
        assertThat(options.get(3).selected()).isFalse();
    }

    @Test
    void ensureHtmlSelectDtoWithFirstNameDescSortHasCorrectOptionSelected() throws Exception {
        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        final PageRequest pageRequest = PageRequest.of(0, 20).withSort(Sort.by(Sort.Order.desc("person.firstName")));
        when(personService.getActivePersons(PersonPageRequest.ofApiPageable(pageRequest), "")).thenReturn(page);

        final ResultActions resultActions = perform(get("/web/person").param("sort", "person.firstName,DESC"));
        final HtmlSelectDto htmlSelectDto = extractSortSelect(resultActions);

        final HtmlOptgroupDto personOptgroup = htmlSelectDto.optgroups().get(0);
        final List<HtmlOptionDto> options = personOptgroup.options();

        assertThat(options.get(0).selected()).isFalse();
        assertThat(options.get(1).selected()).isTrue();
        assertThat(options.get(1).value()).isEqualTo("person.firstName,desc");
    }

    @Test
    void ensureHtmlSelectDtoWithLastNameAscSortHasCorrectOptionSelected() throws Exception {
        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        final PageRequest pageRequest = PageRequest.of(0, 20).withSort(Sort.by(Sort.Order.asc("person.lastName")));
        when(personService.getActivePersons(PersonPageRequest.ofApiPageable(pageRequest), "")).thenReturn(page);

        final ResultActions resultActions = perform(get("/web/person").param("sort", "person.lastName,ASC"));
        final HtmlSelectDto htmlSelectDto = extractSortSelect(resultActions);

        final HtmlOptgroupDto personOptgroup = htmlSelectDto.optgroups().get(0);
        final List<HtmlOptionDto> options = personOptgroup.options();

        assertThat(options.get(2).selected()).isTrue();
        assertThat(options.get(2).value()).isEqualTo("person.lastName,asc");
    }

    @Test
    void ensureHtmlSelectDtoWithEntitlementYearDescSortHasCorrectOptionSelected() throws Exception {
        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        final PageRequest pageRequest = PageRequest.of(0, 20).withSort(Sort.by(Sort.Order.desc("account.entitlementYear")));
        when(personService.getActivePersons(PersonPageRequest.ofApiPageable(pageRequest), "")).thenReturn(page);

        final ResultActions resultActions = perform(get("/web/person").param("sort", "account.entitlementYear,DESC"));
        final HtmlSelectDto htmlSelectDto = extractSortSelect(resultActions);

        final HtmlOptgroupDto urlaubOptgroup = htmlSelectDto.optgroups().get(1);
        final List<HtmlOptionDto> options = urlaubOptgroup.options();

        assertThat(options.get(1).selected()).isTrue();
        assertThat(options.get(1).value()).isEqualTo("account.entitlementYear,desc");
    }

    @Test
    void ensureHtmlSelectDtoWithVacationDaysLeftRemainingAscSortHasCorrectOptionSelected() throws Exception {
        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final PageImpl<Person> page = new PageImpl<>(List.of());
        final PageRequest pageRequest = PageRequest.of(0, 20).withSort(Sort.by(Sort.Order.asc("account.vacationDaysLeftRemaining")));
        when(personService.getActivePersons(PersonPageRequest.ofApiPageable(pageRequest), "")).thenReturn(page);

        final ResultActions resultActions = perform(get("/web/person").param("sort", "account.vacationDaysLeftRemaining,ASC"));
        final HtmlSelectDto htmlSelectDto = extractSortSelect(resultActions);

        final HtmlOptgroupDto resturlaubOptgroup = htmlSelectDto.optgroups().get(2);
        final List<HtmlOptionDto> options = resturlaubOptgroup.options();

        assertThat(options.get(2).selected()).isTrue();
        assertThat(options.get(2).value()).isEqualTo("account.vacationDaysLeftRemaining,asc");
    }

    private HtmlSelectDto extractSortSelect(ResultActions resultActions) {
        return (HtmlSelectDto) resultActions.andReturn().getModelAndView().getModel().get("sortSelect");
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build()
            .perform(builder);
    }

    /**
     * create a default PageRequest used to invoke services.
     * Note the difference to the pageable consumed by the view controller which has keys like `person.firstName` or `account.someAttribute`.
     *
     * @return {@link PageRequest} passed to the services
     */
    private static PersonPageRequest defaultPageRequest() {

        final PageRequest pageRequest = PageRequest.of(0, 20)
            .withSort(Sort.by("person.firstName"));

        return PersonPageRequest.ofApiPageable(pageRequest);
    }

    private void mockDefaultPageRequest(Person signedInUser) {
        final PageImpl<Person> page = new PageImpl<>(List.of());
        when(departmentService.getManagedMembersOfPerson(signedInUser, defaultPageRequest(), "")).thenReturn(page);
    }

    private static Person personWithRole(Role... role) {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(Arrays.asList(role));

        return person;
    }
}
