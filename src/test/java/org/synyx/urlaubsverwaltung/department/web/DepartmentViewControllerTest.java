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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class DepartmentViewControllerTest {

    private DepartmentViewController sut;

    private static final String DEPARTMENT_ATTRIBUTE = "department";
    private static final String PERSONS_ATTRIBUTE = "persons";
    private static final int UNKNOWN_DEPARTMENT_ID = 571;
    private static final int SOME_DEPARTMENT_ID = 1;

    @Mock
    private DepartmentService departmentService;
    @Mock
    private PersonService personService;
    @Mock
    private DepartmentValidator validator;

    @BeforeEach
    void setUp() {

        sut = new DepartmentViewController(departmentService, personService, validator);
    }

    @Test
    void showAllDepartmentsAddsDepartmentsToModel() throws Exception {

        final List<Department> departments = Collections.singletonList(someDepartment());
        when(departmentService.getAllDepartments()).thenReturn(departments);

        perform(get("/web/department"))
            .andExpect(model().attribute("departments", departments));
    }

    @Test
    void showAllDepartmentsUsesCorrectView() throws Exception {

        perform(get("/web/department")).andExpect(view().name("department/department_list"));
    }

    @Test
    void getNewDepartmentFormAddsNewDepartmentAndActivePersonsToModel() throws Exception {

        final List<Person> persons = Collections.singletonList(somePerson());
        when(personService.getActivePersons()).thenReturn(persons);

        perform(get("/web/department/new"))
            .andExpect(model().attribute(DEPARTMENT_ATTRIBUTE, hasProperty("new", equalTo(Boolean.TRUE))))
            .andExpect(model().attribute(PERSONS_ATTRIBUTE, persons));
    }

    @Test
    void getNewDepartmentFormUsesCorrectView() throws Exception {

        perform(get("/web/department/new"))
            .andExpect(view().name("department/department_form"));
    }

    @Test
    void postNewDepartmentShowsFormIfValidationFails() throws Exception {

        doAnswer(invocation -> {

            Errors errors = invocation.getArgument(1);
            errors.rejectValue("name", "errors");
            return null;

        }).when(validator).validate(any(), any());

        perform(post("/web/department"))
            .andExpect(view().name("department/department_form"));

        verify(departmentService, never()).create(any());
    }

    @Test
    void postNewDepartmentCreatesDepartmentCorrectlyIfValidationSuccessful() throws Exception {

        perform(post("/web/department"));

        verify(departmentService).create(any(Department.class));
    }

    @Test
    void postNewDepartmentAddsFlashAttributeAndRedirectsToDepartment() throws Exception {

        perform(post("/web/department"))
            .andExpect(flash().attribute("createdDepartment", instanceOf(Department.class)))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/department/"));
    }

    @Test
    void editDepartmentForUnknownDepartmentIdThrowsUnknownDepartmentException() {

        assertThatThrownBy(() ->
            perform(get("/web/department/" + UNKNOWN_DEPARTMENT_ID + "/edit"))
        ).hasCauseInstanceOf(UnknownDepartmentException.class);
    }

    @Test
    void editDepartmentAddsDepartmentAndActivePersonsToModel() throws Exception {

        final Department department = someDepartment();
        when(departmentService.getDepartmentById(SOME_DEPARTMENT_ID)).thenReturn(Optional.of(department));

        List<Person> persons = Collections.singletonList(somePerson());
        when(personService.getActivePersons()).thenReturn(persons);

        perform(get("/web/department/" + SOME_DEPARTMENT_ID + "/edit"))
            .andExpect(model().attribute(DEPARTMENT_ATTRIBUTE, department))
            .andExpect(model().attribute(PERSONS_ATTRIBUTE, persons));
    }

    @Test
    void editDepartmentUsesCorrectView() throws Exception {

        when(departmentService.getDepartmentById(SOME_DEPARTMENT_ID)).thenReturn(Optional.of(someDepartment()));

        perform(get("/web/department/" + SOME_DEPARTMENT_ID + "/edit"))
            .andExpect(view().name("department/department_form"));
    }

    @Test
    void updateDepartmentForUnknownDepartmentIdThrowsUnknownDepartmentException() {

        assertThatThrownBy(() ->
            perform(post("/web/department/" + UNKNOWN_DEPARTMENT_ID))
        ).hasCauseInstanceOf(UnknownDepartmentException.class);
    }

    @Test
    void updateDepartmentShowsFormIfValidationFails() throws Exception {

        when(departmentService.getDepartmentById(SOME_DEPARTMENT_ID)).thenReturn(Optional.of(someDepartment()));

        doAnswer(invocation -> {

            Errors errors = invocation.getArgument(1);
            errors.rejectValue("name", "errors");
            return null;

        }).when(validator).validate(any(), any());

        perform(post("/web/department/" + SOME_DEPARTMENT_ID))
            .andExpect(view().name("department/department_form"));

        verify(departmentService, never()).update(any());
    }

    @Test
    void updateDepartmentUpdatesDepartmentCorrectIfValidationSuccessful() throws Exception {

        when(departmentService.getDepartmentById(SOME_DEPARTMENT_ID)).thenReturn(Optional.of(someDepartment()));

        perform(post("/web/department/" + SOME_DEPARTMENT_ID));

        verify(departmentService).update(any(Department.class));
    }

    @Test
    void updateDepartmentAddsFlashAttributeAndRedirectsToDepartment() throws Exception {

        when(departmentService.getDepartmentById(SOME_DEPARTMENT_ID)).thenReturn(Optional.of(someDepartment()));

        perform(post("/web/department/" + SOME_DEPARTMENT_ID))
            .andExpect(flash().attribute("updatedDepartment", instanceOf(Department.class)))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/department/"));
    }

    @Test
    void deleteDepartmentCallsServiceToDeleteDepartment() throws Exception {

        perform(delete("/web/department/" + SOME_DEPARTMENT_ID));

        verify(departmentService).delete(SOME_DEPARTMENT_ID);
    }

    @Test
    void deleteDepartmentAddsFlashAttributeForExistingDepartment() throws Exception {

        final Department department = someDepartment();
        when(departmentService.getDepartmentById(SOME_DEPARTMENT_ID)).thenReturn(Optional.of(department));

        perform(delete("/web/department/" + SOME_DEPARTMENT_ID))
            .andExpect(flash().attribute("deletedDepartment", department));
    }

    @Test
    void deleteDepartmentDoesNotAddFlashAttributeForNotExistingDepartment() throws Exception {

        when(departmentService.getDepartmentById(UNKNOWN_DEPARTMENT_ID)).thenReturn(Optional.empty());

        perform(delete("/web/department/" + UNKNOWN_DEPARTMENT_ID))
            .andExpect(flash().attribute("deletedDepartment", nullValue()));
    }

    @Test
    void deleteDepartmentRedirectsToDepartment() throws Exception {

        perform(delete("/web/department/" + SOME_DEPARTMENT_ID))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/department/"));
    }

    private Department someDepartment() {

        return new Department();
    }

    private Person somePerson() {

        return new Person();
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {

        return standaloneSetup(sut).build().perform(builder);
    }
}
