package org.synyx.urlaubsverwaltung.department.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.department.web.DepartmentDepartmentFormMapper.mapToDepartmentForm;
import static org.synyx.urlaubsverwaltung.department.web.DepartmentDepartmentOverviewDtoMapper.mapToDepartmentOverviewDtos;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class DepartmentViewControllerTest {

    private DepartmentViewController sut;

    @Mock
    private DepartmentService departmentService;
    @Mock
    private PersonService personService;
    @Mock
    private DepartmentViewValidator validator;

    @BeforeEach
    void setUp() {
        sut = new DepartmentViewController(departmentService, personService, validator);
    }

    @Test
    void showAllDepartmentsAddsDepartmentsToModel() throws Exception {

        final List<Department> departments = List.of(new Department());
        when(departmentService.getAllDepartments()).thenReturn(departments);

        final Person signedInUser = new Person("muster", "Muster", "Marlene", "muster@example.org");
        signedInUser.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/department"))
            .andExpect(model().attribute("departments", mapToDepartmentOverviewDtos(departments)))
            .andExpect(model().attribute("canCreateAndModifyDepartment", false));
    }

    @Test
    void showAllDepartmentsUsesCorrectView() throws Exception {

        final Person signedInUser = new Person("muster", "Muster", "Marlene", "muster@example.org");
        signedInUser.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/department"))
            .andExpect(view().name("department/department_list"))
            .andExpect(model().attribute("canCreateAndModifyDepartment", false));
    }

    @Test
    void ensureThatOfficeCanCreateAndModifyDepartment() throws Exception {

        final List<Department> departments = List.of(new Department());
        when(departmentService.getAllDepartments()).thenReturn(departments);

        final Person signedInUser = new Person("muster", "Muster", "Marlene", "muster@example.org");
        signedInUser.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/department"))
            .andExpect(model().attribute("canCreateAndModifyDepartment", true));
    }

    @Test
    void getNewDepartmentFormAddsNewDepartmentAndActivePersonsToModel() throws Exception {

        final List<Person> persons = List.of(new Person());
        when(personService.getActivePersons()).thenReturn(persons);

        perform(get("/web/department/new"))
            .andExpect(model().attribute("department", hasProperty("id", is(nullValue()))))
            .andExpect(model().attribute("persons", persons));
    }

    @Test
    void getNewDepartmentFormUsesCorrectView() throws Exception {

        perform(get("/web/department/new"))
            .andExpect(view().name("department/department_form"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"bru", "way"})
    void ensureNewDepartmentMemberSearch(String givenQuery) throws Exception {

        final Person batman = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        batman.setId(1L);

        final Person robin = new Person("robin", "Grayson", "Dick", "robin@example.org");
        robin.setId(2L);

        when(personService.getActivePersons()).thenReturn(List.of(batman, robin));

        // departmentForm values are defined by the POST request
        final DepartmentForm expectedDepartmentForm = new DepartmentForm();
        expectedDepartmentForm.setName("fight club");

        perform(post("/web/department/new")
            .param("do-member-search", "")
            .param("memberQuery", givenQuery)
            .param("name", "fight club")
            .param("id", "1")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("department/department_form"))
            .andExpect(model().attribute("turboFrameRequested", is(false)))
            .andExpect(model().attribute("memberQuery", is(givenQuery)))
            .andExpect(model().attribute("department", is(expectedDepartmentForm)))
            .andExpect(model().attribute("persons", hasSize(1)))
            .andExpect(model().attribute("persons", hasItem(batman)));

        verifyNoMoreInteractions(departmentService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"bru", "way"})
    void ensureNewDepartmentMemberSearchWithJavaScript(String givenQuery) throws Exception {

        final Person batman = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        batman.setId(1L);

        final Person robin = new Person("robin", "Grayson", "Dick", "robin@example.org");
        robin.setId(2L);

        when(personService.getActivePersons()).thenReturn(List.of(batman, robin));

        // departmentForm values are defined by the POST request
        final DepartmentForm expectedDepartmentForm = new DepartmentForm();
        expectedDepartmentForm.setName("fight club");

        perform(post("/web/department/new")
            .header("Turbo-Frame", "awesome-turbo-frame")
            .param("do-member-search", "")
            .param("memberQuery", givenQuery)
            .param("name", "fight club")
            .param("id", "1")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("department/department_form::#awesome-turbo-frame"))
            .andExpect(model().attribute("turboFrameRequested", is(true)))
            .andExpect(model().attribute("memberQuery", is(givenQuery)))
            .andExpect(model().attribute("department", is(expectedDepartmentForm)))
            .andExpect(model().attribute("persons", hasSize(1)))
            .andExpect(model().attribute("persons", hasItem(batman)));

        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void ensureNewDepartmentMemberSearchKeepsMembersState() throws Exception {

        final Person batman = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        batman.setId(1L);

        final Person robin = new Person("robin", "Grayson", "Dick", "robin@example.org");
        robin.setId(2L);

        final Person departmentHead = new Person("head", "Head", "Department", "departmentHead@example.org");
        robin.setId(3L);

        final Person secondStageAuthority = new Person("secondStage", "Authority", "Second Stage", "secondstage@example.org");
        robin.setId(4L);

        when(personService.getPersonByID(2L)).thenReturn(Optional.of(robin));
        when(personService.getPersonByID(3L)).thenReturn(Optional.of(departmentHead));
        when(personService.getPersonByID(4L)).thenReturn(Optional.of(secondStageAuthority));

        when(personService.getActivePersons()).thenReturn(List.of(batman, robin, departmentHead, secondStageAuthority));

        // departmentForm values are defined by the POST request
        // ViewController must keep the state from the DTO, not from the `department` received from `departmentService`
        final DepartmentForm expectedDepartmentForm = new DepartmentForm();
        expectedDepartmentForm.setId(1L);
        expectedDepartmentForm.setName("fight club");
        expectedDepartmentForm.setMembers(List.of(robin));
        expectedDepartmentForm.setDepartmentHeads(List.of(departmentHead));
        expectedDepartmentForm.setSecondStageAuthorities(List.of(secondStageAuthority));

        perform(post("/web/department/new")
            .param("do-member-search", "")
            .param("memberQuery", "bruce")
            .param("name", "fight club")
            .param("id", "1")
            .param("members", "2")
            .param("departmentHeads", "3")
            .param("secondStageAuthorities", "4")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("department/department_form"))
            .andExpect(model().attribute("turboFrameRequested", is(false)))
            .andExpect(model().attribute("memberQuery", is("bruce")))
            .andExpect(model().attribute("department", is(expectedDepartmentForm)))
            .andExpect(model().attribute("persons", hasSize(1)))
            .andExpect(model().attribute("persons", hasItem(batman)))
            .andExpect(model().attribute("hiddenDepartmentMembers", hasSize(1)))
            .andExpect(model().attribute("hiddenDepartmentMembers", hasItems(robin)))
            .andExpect(model().attribute("hiddenDepartmentHeads", hasSize(1)))
            .andExpect(model().attribute("hiddenDepartmentHeads", hasItem(departmentHead)))
            .andExpect(model().attribute("hiddenDepartmentSecondStageAuthorities", hasSize(1)))
            .andExpect(model().attribute("hiddenDepartmentSecondStageAuthorities", hasItems(secondStageAuthority)));

        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void ensureNewDepartmentMemberSearchKeepsMembersStateWithJavaScript() throws Exception {

        final Person batman = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        batman.setId(1L);

        final Person robin = new Person("robin", "Grayson", "Dick", "robin@example.org");
        robin.setId(2L);

        final Person departmentHead = new Person("head", "Head", "Department", "departmentHead@example.org");
        robin.setId(3L);

        final Person secondStageAuthority = new Person("secondStage", "Authority", "Second Stage", "secondstage@example.org");
        robin.setId(4L);

        when(personService.getPersonByID(2L)).thenReturn(Optional.of(robin));
        when(personService.getPersonByID(3L)).thenReturn(Optional.of(departmentHead));
        when(personService.getPersonByID(4L)).thenReturn(Optional.of(secondStageAuthority));

        when(personService.getActivePersons()).thenReturn(List.of(batman, robin, departmentHead, secondStageAuthority));

        // departmentForm values are defined by the POST request
        // ViewController must keep the state from the DTO, not from the `department` received from `departmentService`
        final DepartmentForm expectedDepartmentForm = new DepartmentForm();
        expectedDepartmentForm.setId(1L);
        expectedDepartmentForm.setName("fight club");
        expectedDepartmentForm.setMembers(List.of(robin));
        expectedDepartmentForm.setDepartmentHeads(List.of(departmentHead));
        expectedDepartmentForm.setSecondStageAuthorities(List.of(secondStageAuthority));

        perform(post("/web/department/new")
            .header("Turbo-Frame", "awesome-turbo-frame")
            .param("do-member-search", "")
            .param("memberQuery", "bruce")
            .param("name", "fight club")
            .param("id", "1")
            .param("members", "2")
            .param("departmentHeads", "3")
            .param("secondStageAuthorities", "4")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("department/department_form::#awesome-turbo-frame"))
            .andExpect(model().attribute("turboFrameRequested", is(true)))
            .andExpect(model().attribute("memberQuery", is("bruce")))
            .andExpect(model().attribute("department", is(expectedDepartmentForm)))
            .andExpect(model().attribute("persons", hasSize(1)))
            .andExpect(model().attribute("persons", hasItem(batman)))
            .andExpect(model().attribute("hiddenDepartmentMembers", hasSize(1)))
            .andExpect(model().attribute("hiddenDepartmentMembers", hasItems(robin)))
            .andExpect(model().attribute("hiddenDepartmentHeads", hasSize(1)))
            .andExpect(model().attribute("hiddenDepartmentHeads", hasItem(departmentHead)))
            .andExpect(model().attribute("hiddenDepartmentSecondStageAuthorities", hasSize(1)))
            .andExpect(model().attribute("hiddenDepartmentSecondStageAuthorities", hasItems(secondStageAuthority)));

        verifyNoMoreInteractions(departmentService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"bru", "way"})
    void ensureDepartmentMemberSearch(String givenQuery) throws Exception {

        final Person batman = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        batman.setId(1L);

        final Person robin = new Person("robin", "Grayson", "Dick", "robin@example.org");
        robin.setId(2L);

        when(personService.getActivePersons()).thenReturn(List.of(batman, robin));
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(new Department()));

        // departmentForm values are defined by the POST request
        final DepartmentForm expectedDepartmentForm = new DepartmentForm();
        expectedDepartmentForm.setId(1L);
        expectedDepartmentForm.setName("fight club");

        perform(post("/web/department/1/edit")
            .param("do-member-search", "")
            .param("memberQuery", givenQuery)
            .param("name", "fight club")
            .param("id", "1")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("department/department_form"))
            .andExpect(model().attribute("turboFrameRequested", is(false)))
            .andExpect(model().attribute("memberQuery", is(givenQuery)))
            .andExpect(model().attribute("department", is(expectedDepartmentForm)))
            .andExpect(model().attribute("persons", hasSize(1)))
            .andExpect(model().attribute("persons", hasItem(batman)));

        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void ensureDepartmentMemberSearchKeepsMembersState() throws Exception {

        final Person batman = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        batman.setId(1L);

        final Person robin = new Person("robin", "Grayson", "Dick", "robin@example.org");
        robin.setId(2L);

        final Person departmentHead = new Person("head", "Head", "Department", "departmentHead@example.org");
        robin.setId(3L);

        final Person secondStageAuthority = new Person("secondStage", "Authority", "Second Stage", "secondstage@example.org");
        robin.setId(4L);

        when(personService.getPersonByID(2L)).thenReturn(Optional.of(robin));
        when(personService.getPersonByID(3L)).thenReturn(Optional.of(departmentHead));
        when(personService.getPersonByID(4L)).thenReturn(Optional.of(secondStageAuthority));

        when(personService.getActivePersons()).thenReturn(List.of(batman, robin, departmentHead, secondStageAuthority));
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(new Department()));

        // departmentForm values are defined by the POST request
        // ViewController must keep the state from the DTO, not from the `department` received from `departmentService`
        final DepartmentForm expectedDepartmentForm = new DepartmentForm();
        expectedDepartmentForm.setId(1L);
        expectedDepartmentForm.setName("fight club");
        expectedDepartmentForm.setMembers(List.of(robin));
        expectedDepartmentForm.setDepartmentHeads(List.of(departmentHead));
        expectedDepartmentForm.setSecondStageAuthorities(List.of(secondStageAuthority));

        perform(post("/web/department/1/edit")
            .param("do-member-search", "")
            .param("memberQuery", "bruce")
            .param("name", "fight club")
            .param("id", "1")
            .param("members", "2")
            .param("departmentHeads", "3")
            .param("secondStageAuthorities", "4")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("department/department_form"))
            .andExpect(model().attribute("turboFrameRequested", is(false)))
            .andExpect(model().attribute("memberQuery", is("bruce")))
            .andExpect(model().attribute("department", is(expectedDepartmentForm)))
            .andExpect(model().attribute("persons", hasSize(1)))
            .andExpect(model().attribute("persons", hasItem(batman)))
            .andExpect(model().attribute("hiddenDepartmentMembers", hasSize(1)))
            .andExpect(model().attribute("hiddenDepartmentMembers", hasItems(robin)))
            .andExpect(model().attribute("hiddenDepartmentHeads", hasSize(1)))
            .andExpect(model().attribute("hiddenDepartmentHeads", hasItem(departmentHead)))
            .andExpect(model().attribute("hiddenDepartmentSecondStageAuthorities", hasSize(1)))
            .andExpect(model().attribute("hiddenDepartmentSecondStageAuthorities", hasItems(secondStageAuthority)));

        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void ensureDepartmentMemberSearchWithEmptyQuery() throws Exception {

        final Person batman = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        batman.setId(1L);

        final Person robin = new Person("robin", "Grayson", "Dick", "robin@example.org");
        robin.setId(2L);

        when(personService.getActivePersons()).thenReturn(List.of(batman, robin));
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(new Department()));

        // departmentForm values are defined by the POST request
        final DepartmentForm expectedDepartmentForm = new DepartmentForm();
        expectedDepartmentForm.setId(1L);
        expectedDepartmentForm.setName("fight club");

        perform(post("/web/department/1/edit")
            .param("do-member-search", "")
            .param("memberQuery", "")
            .param("name", "fight club")
            .param("id", "1")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("department/department_form"))
            .andExpect(model().attribute("turboFrameRequested", is(false)))
            .andExpect(model().attribute("memberQuery", emptyString()))
            .andExpect(model().attribute("department", is(expectedDepartmentForm)))
            .andExpect(model().attribute("persons", hasSize(2)))
            .andExpect(model().attribute("persons", hasItems(batman, robin)));

        verifyNoMoreInteractions(departmentService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"bru", "ayne"})
    void ensureDepartmentMemberSearchWithJavaScript(String givenQuery) throws Exception {
        final Person batman = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        batman.setId(1L);

        final Person robin = new Person("robin", "Grayson", "Dick", "robin@example.org");
        robin.setId(2L);

        when(personService.getActivePersons()).thenReturn(List.of(batman, robin));
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(new Department()));

        // departmentForm values are defined by the POST request
        final DepartmentForm expectedDepartmentForm = new DepartmentForm();
        expectedDepartmentForm.setId(1L);
        expectedDepartmentForm.setName("fight club");

        perform(post("/web/department/1/edit")
            .header("Turbo-Frame", "awesome-turbo-frame")
            .param("do-member-search", "")
            .param("memberQuery", givenQuery)
            .param("name", "fight club")
            .param("id", "1")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("department/department_form::#awesome-turbo-frame"))
            .andExpect(model().attribute("turboFrameRequested", is(true)))
            .andExpect(model().attribute("memberQuery", is(givenQuery)))
            .andExpect(model().attribute("department", is(expectedDepartmentForm)))
            .andExpect(model().attribute("persons", hasSize(1)))
            .andExpect(model().attribute("persons", hasItem(batman)));

        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void ensureDepartmentMemberSearchWithJavaScriptKeepsMembersState() throws Exception {

        final Person batman = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        batman.setId(1L);

        final Person robin = new Person("robin", "Grayson", "Dick", "robin@example.org");
        robin.setId(2L);

        final Person departmentHead = new Person("head", "Head", "Department", "departmentHead@example.org");
        robin.setId(3L);

        final Person secondStageAuthority = new Person("secondStage", "Authority", "Second Stage", "secondstage@example.org");
        robin.setId(4L);

        when(personService.getPersonByID(2L)).thenReturn(Optional.of(robin));
        when(personService.getPersonByID(3L)).thenReturn(Optional.of(departmentHead));
        when(personService.getPersonByID(4L)).thenReturn(Optional.of(secondStageAuthority));

        when(personService.getActivePersons()).thenReturn(List.of(batman, robin));
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(new Department()));

        // departmentForm values are defined by the POST request
        // ViewController must keep the state from the DTO, not from the `department` received from `departmentService`
        final DepartmentForm expectedDepartmentForm = new DepartmentForm();
        expectedDepartmentForm.setId(1L);
        expectedDepartmentForm.setName("fight club");
        expectedDepartmentForm.setMembers(List.of(robin));
        expectedDepartmentForm.setDepartmentHeads(List.of(departmentHead));
        expectedDepartmentForm.setSecondStageAuthorities(List.of(secondStageAuthority));

        perform(post("/web/department/1/edit")
            .header("Turbo-Frame", "awesome-turbo-frame")
            .param("do-member-search", "")
            .param("memberQuery", "bruce")
            .param("name", "fight club")
            .param("id", "1")
            .param("members", "2")
            .param("departmentHeads", "3")
            .param("secondStageAuthorities", "4")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("department/department_form::#awesome-turbo-frame"))
            .andExpect(model().attribute("turboFrameRequested", is(true)))
            .andExpect(model().attribute("memberQuery", is("bruce")))
            .andExpect(model().attribute("department", is(expectedDepartmentForm)))
            .andExpect(model().attribute("persons", hasSize(1)))
            .andExpect(model().attribute("persons", hasItem(batman)))
            .andExpect(model().attribute("hiddenDepartmentMembers", hasSize(1)))
            .andExpect(model().attribute("hiddenDepartmentMembers", hasItems(robin)))
            .andExpect(model().attribute("hiddenDepartmentHeads", hasSize(1)))
            .andExpect(model().attribute("hiddenDepartmentHeads", hasItem(departmentHead)))
            .andExpect(model().attribute("hiddenDepartmentSecondStageAuthorities", hasSize(1)))
            .andExpect(model().attribute("hiddenDepartmentSecondStageAuthorities", hasItems(secondStageAuthority)));

        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void ensureDepartmentMemberSearchWithJavaScriptWithEmptyQuery() throws Exception {
        final Person batman = new Person("batman", "Wayne", "Bruce", "batman@example.org");
        batman.setId(1L);

        final Person robin = new Person("robin", "Grayson", "Dick", "robin@example.org");
        robin.setId(2L);

        when(personService.getActivePersons()).thenReturn(List.of(batman, robin));
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(new Department()));

        // departmentForm values are defined by the POST request
        final DepartmentForm expectedDepartmentForm = new DepartmentForm();
        expectedDepartmentForm.setId(1L);
        expectedDepartmentForm.setName("fight club");

        perform(post("/web/department/1/edit")
            .header("Turbo-Frame", "awesome-turbo-frame")
            .param("do-member-search", "")
            .param("memberQuery", "")
            .param("name", "fight club")
            .param("id", "1")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("department/department_form::#awesome-turbo-frame"))
            .andExpect(model().attribute("turboFrameRequested", is(true)))
            .andExpect(model().attribute("memberQuery", emptyString()))
            .andExpect(model().attribute("department", is(expectedDepartmentForm)))
            .andExpect(model().attribute("persons", hasSize(2)))
            .andExpect(model().attribute("persons", hasItems(batman, robin)));

        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void postNewDepartmentShowsFormIfValidationFails() throws Exception {

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("name", "errors");
            return null;

        }).when(validator).validate(any(), any());

        perform(post("/web/department/new"))
            .andExpect(view().name("department/department_form"));

        verify(departmentService, never()).create(any());
    }

    @Test
    void postNewDepartmentCreatesDepartmentCorrectlyIfValidationSuccessful() throws Exception {

        when(departmentService.create(any())).thenReturn(new Department());

        perform(post("/web/department/new"));

        verify(departmentService).create(any(Department.class));
    }

    @Test
    void postNewDepartmentAddsFlashAttributeAndRedirectsToDepartment() throws Exception {

        final Department department = new Department();
        department.setName("department");
        when(departmentService.create(any())).thenReturn(department);

        perform(post("/web/department/new"))
            .andExpect(status().isFound())
            .andExpect(flash().attribute("createdDepartmentName", "department"))
            .andExpect(redirectedUrl("/web/department"));
    }

    @Test
    void editDepartmentForUnknownDepartmentIdThrowsUnknownDepartmentException() {

        assertThatThrownBy(() ->
            perform(get("/web/department/571/edit"))
        ).hasCauseInstanceOf(UnknownDepartmentException.class);
    }

    @Test
    void editDepartmentAddsDepartmentAndActivePersonsToModelGroupedByDepartmentMember() throws Exception {

        final Person activePerson = new Person("username-1", "Quak", "Alfred", "alfred.quak@example.org");
        activePerson.setId(1L);

        final Person inactivePerson = new Person("username-2", "Inaktiv", "Brigitte", "brigitte.inaktiv@example.org");
        inactivePerson.setId(2L);
        inactivePerson.setPermissions(List.of(Role.INACTIVE));

        final Person otherPerson = new Person("username-3", "Roth", "Anne", "anne.roth@example.org");
        otherPerson.setId(3L);

        final Department department = new Department();
        department.setId(1L);
        department.setMembers(List.of(activePerson, inactivePerson));
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));

        when(personService.getActivePersons()).thenReturn(List.of(activePerson, otherPerson));

        perform(get("/web/department/1/edit"))
            .andExpect(model().attribute("department", mapToDepartmentForm(department)))
            .andExpect(model().attribute("persons", List.of(activePerson, inactivePerson, otherPerson)));
    }

    @Test
    void editDepartmentUsesCorrectView() throws Exception {

        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(new Department()));

        perform(get("/web/department/1/edit"))
            .andExpect(view().name("department/department_form"));
    }

    @Test
    void updateDepartmentShowsFormIfValidationFails() throws Exception {

        doAnswer(invocation -> {
            final Errors errors = invocation.getArgument(1);
            errors.rejectValue("name", "errors");
            return null;
        }).when(validator).validate(any(), any());

        perform(post("/web/department/1"))
            .andExpect(view().name("department/department_form"));

        verify(departmentService, never()).update(any());
    }

    @Test
    void updateDepartmentUpdatesDepartmentCorrectIfValidationSuccessful() throws Exception {

        when(departmentService.update(any())).thenReturn(new Department());

        perform(post("/web/department/1"));

        verify(departmentService).update(any(Department.class));
    }

    @Test
    void updateDepartmentAddsFlashAttributeAndRedirectsToDepartment() throws Exception {

        final Department department = new Department();
        department.setName("department");
        when(departmentService.update(any())).thenReturn(department);

        perform(post("/web/department/1"))
            .andExpect(status().isFound())
            .andExpect(flash().attribute("updatedDepartmentName", "department"))
            .andExpect(redirectedUrl("/web/department"));
    }

    @Test
    void deleteDepartment() throws Exception {

        final Department department = new Department();
        department.setId(1L);
        department.setName("department");
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));

        perform(post("/web/department/1/delete"))
            .andExpect(status().isFound())
            .andExpect(flash().attribute("deletedDepartmentName", "department"))
            .andExpect(redirectedUrl("/web/department"));

        verify(departmentService).delete(1L);
    }

    @Test
    void deleteDepartmentButDoesNotExist() throws Exception {

        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.empty());

        perform(post("/web/department/1/delete"))
            .andExpect(status().isFound())
            .andExpect(flash().attribute("deletedDepartment", nullValue()))
            .andExpect(redirectedUrl("/web/department"));

        verify(departmentService, never()).delete(1L);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
