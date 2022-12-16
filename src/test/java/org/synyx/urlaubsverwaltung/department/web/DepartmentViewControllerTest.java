package org.synyx.urlaubsverwaltung.department.web;

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
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
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
            .andExpect(view().name("thymeleaf/department/department_list"))
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
            .andExpect(view().name("thymeleaf/department/department_form"));
    }

    @Test
    void postNewDepartmentShowsFormIfValidationFails() throws Exception {

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("name", "errors");
            return null;

        }).when(validator).validate(any(), any());

        perform(post("/web/department"))
            .andExpect(view().name("thymeleaf/department/department_form"));

        verify(departmentService, never()).create(any());
    }

    @Test
    void postNewDepartmentCreatesDepartmentCorrectlyIfValidationSuccessful() throws Exception {

        when(departmentService.create(any())).thenReturn(new Department());

        perform(post("/web/department"));

        verify(departmentService).create(any(Department.class));
    }

    @Test
    void postNewDepartmentAddsFlashAttributeAndRedirectsToDepartment() throws Exception {

        final Department department = new Department();
        department.setName("department");
        when(departmentService.create(any())).thenReturn(department);

        perform(post("/web/department"))
            .andExpect(status().isFound())
            .andExpect(flash().attribute("createdDepartmentName", "department"))
            .andExpect(redirectedUrl("/web/department/"));
    }

    @Test
    void editDepartmentForUnknownDepartmentIdThrowsUnknownDepartmentException() {

        assertThatThrownBy(() ->
            perform(get("/web/department/571/edit"))
        ).hasCauseInstanceOf(UnknownDepartmentException.class);
    }

    @Test
    void editDepartmentAddsDepartmentAndActivePersonsToModel() throws Exception {

        final Person activePerson = new Person();
        final Person inactivePerson = inactivePerson();

        List<Person> departmentMembers = List.of(activePerson, inactivePerson);
        final Department department = new Department();
        department.setMembers(departmentMembers);
        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(department));

        List<Person> activePersons = List.of(activePerson);
        when(personService.getActivePersons()).thenReturn(activePersons);

        perform(get("/web/department/1/edit"))
            .andExpect(model().attribute("department", mapToDepartmentForm(department)))
            .andExpect(model().attribute("persons", List.of(inactivePerson, activePerson)));
    }

    private Person inactivePerson() {
        final Person inactivePerson = new Person();
        inactivePerson.setPermissions(List.of(Role.INACTIVE));

        return inactivePerson;
    }

    @Test
    void editDepartmentUsesCorrectView() throws Exception {

        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(new Department()));

        perform(get("/web/department/1/edit"))
            .andExpect(view().name("thymeleaf/department/department_form"));
    }

    @Test
    void updateDepartmentForUnknownDepartmentIdThrowsUnknownDepartmentException() {

        assertThatThrownBy(() ->
            perform(post("/web/department/571"))
        ).hasCauseInstanceOf(UnknownDepartmentException.class);
    }

    @Test
    void updateDepartmentShowsFormIfValidationFails() throws Exception {

        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(new Department()));

        doAnswer(invocation -> {

            Errors errors = invocation.getArgument(1);
            errors.rejectValue("name", "errors");
            return null;

        }).when(validator).validate(any(), any());

        perform(post("/web/department/1"))
            .andExpect(view().name("thymeleaf/department/department_form"));

        verify(departmentService, never()).update(any());
    }

    @Test
    void updateDepartmentUpdatesDepartmentCorrectIfValidationSuccessful() throws Exception {

        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(new Department()));
        when(departmentService.update(any())).thenReturn(new Department());

        perform(post("/web/department/1"));

        verify(departmentService).update(any(Department.class));
    }

    @Test
    void updateDepartmentAddsFlashAttributeAndRedirectsToDepartment() throws Exception {

        final Department department = new Department();
        department.setName("department");
        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(department));
        when(departmentService.update(any())).thenReturn(department);

        perform(post("/web/department/1"))
            .andExpect(status().isFound())
            .andExpect(flash().attribute("updatedDepartmentName", "department"))
            .andExpect(redirectedUrl("/web/department/"));
    }

    @Test
    void deleteDepartment() throws Exception {

        final Department department = new Department();
        department.setId(1);
        department.setName("department");
        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(department));

        perform(post("/web/department/1/delete"))
            .andExpect(status().isFound())
            .andExpect(flash().attribute("deletedDepartmentName", "department"))
            .andExpect(redirectedUrl("/web/department/"));

        verify(departmentService).delete(1);
    }

    @Test
    void deleteDepartmentButDoesNotExist() throws Exception {

        when(departmentService.getDepartmentById(1)).thenReturn(Optional.empty());

        perform(post("/web/department/1/delete"))
            .andExpect(status().isFound())
            .andExpect(flash().attribute("deletedDepartment", nullValue()))
            .andExpect(redirectedUrl("/web/department/"));

        verify(departmentService, never()).delete(1);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
