package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.security.SessionService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class PersonPermissionsViewControllerTest {

    private PersonPermissionsViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PersonPermissionsDtoValidator validator;
    @Mock
    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        sut = new PersonPermissionsViewController(personService, departmentService, validator, sessionService);
    }

    @Test
    void showPersonPermissionsFormUsesPersonsWithGivenId() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(get("/web/person/1/permissions"))
            .andExpect(model().attribute("person", hasProperty("id", is(1L))));
    }

    @Test
    void showPersonPermissionsForUnknownIdThrowsUnknownPersonException() {
        assertThatThrownBy(() ->
            perform(get("/web/person/675/permissions"))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void showPersonPermissionsAddsDepartmentsToModel() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final List<Department> departments = List.of(new Department());
        final List<Department> departmentHeadDepartments = List.of(new Department());
        final List<Department> secondStageDepartments = List.of(new Department());

        when(departmentService.getAssignedDepartmentsOfMember(any())).thenReturn(departments);
        when(departmentService.getManagedDepartmentsOfDepartmentHead(any())).thenReturn(departmentHeadDepartments);
        when(departmentService.getManagedDepartmentsOfSecondStageAuthority(any())).thenReturn(secondStageDepartments);

        perform(get("/web/person/1/permissions"))
            .andExpect(model().attribute("departments", departments))
            .andExpect(model().attribute("departmentHeadDepartments", departmentHeadDepartments))
            .andExpect(model().attribute("secondStageDepartments", secondStageDepartments));
    }

    @Test
    void showPersonPermissionsUsesNewPersonUsesCorrectView() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        perform(get("/web/person/1/permissions"))
            .andExpect(view().name("person/person_permissions"));
    }

    @Test
    void editPersonPermissionsCorrectly() throws Exception {

        final Person person = new Person("username", "Meier", "Nina", "nina@example.org");
        person.setPermissions(List.of(USER));

        when(personService.updatePermissions(new PersonId(1L), List.of(USER, OFFICE))).thenReturn(person);

        perform(post("/web/person/1/permissions")
            .param("id", "1")
            .param("permissions[0]", "USER")
            .param("permissions[1]", "OFFICE")
        );
    }

    @Test
    void editPersonPermissionsAndMarkSessionToReloadAuthorities() throws Exception {

        final Person person = new Person("username", "Meier", "Nina", "nina@example.org");
        person.setPermissions(List.of(USER));

        when(personService.updatePermissions(new PersonId(1L), List.of(USER, OFFICE))).thenReturn(person);

        perform(post("/web/person/1/permissions")
            .param("id", "1")
            .param("permissions[0]", "USER")
            .param("permissions[1]", "OFFICE")
        );

        verify(sessionService).markSessionToReloadAuthorities("username");
    }

    @Test
    void editPersonPermissionsForwardsToViewIfValidationFails() throws Exception {

        doAnswer(invocation -> {
            final Errors errors = invocation.getArgument(1);
            errors.rejectValue("permissions", "person.form.permissions.error.inactive");
            return null;
        }).when(validator).validate(any(), any());

        perform(post("/web/person/1/permissions"))
            .andExpect(view().name("person/person_permissions"));
    }

    @Test
    void editPersonPermissionsAddsFlashAttribute() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.updatePermissions(new PersonId(1L), List.of())).thenReturn(person);

        perform(post("/web/person/1/permissions"))
            .andExpect(flash().attribute("updateSuccess", true));
    }

    @Test
    void editPersonPermissionsRedirectsToUpdatedPerson() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.updatePermissions(new PersonId(1L), List.of())).thenReturn(person);

        perform(post("/web/person/1/permissions"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/person/" + 1));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
