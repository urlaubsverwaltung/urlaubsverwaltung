package org.synyx.urlaubsverwaltung.department.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(MockitoJUnitRunner.class)
public class DepartmentViewControllerTest {

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

    @Before
    public void setUp() {

        sut = new DepartmentViewController(departmentService, personService, validator);
    }

    @Test
    public void showAllDepartmentsAddsDepartmentsToModel() throws Exception {

        final List<Department> departments = Collections.singletonList(someDepartment());
        when(departmentService.getAllDepartments()).thenReturn(departments);

        perform(get("/web/department"))
            .andExpect(model().attribute("departments", departments));
    }

    @Test
    public void showAllDepartmentsUsesCorrectView() throws Exception {

        perform(get("/web/department")).andExpect(view().name("department/department_list"));
    }

    @Test
    public void getNewDepartmentFormAddsNewDepartmentAndActivePersonsToModel() throws Exception {

        final List<Person> persons = Collections.singletonList(somePerson());
        when(personService.getActivePersons()).thenReturn(persons);

        perform(get("/web/department/new"))
            .andExpect(model().attribute(DEPARTMENT_ATTRIBUTE, hasProperty("new", equalTo(Boolean.TRUE))))
            .andExpect(model().attribute(PERSONS_ATTRIBUTE, persons));
    }

    @Test
    public void getNewDepartmentFormUsesCorrectView() throws Exception {

        perform(get("/web/department/new"))
            .andExpect(view().name("department/department_form"));
    }

    @Test
    public void postNewDepartmentShowsFormIfValidationFails() throws Exception {

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
    public void postNewDepartmentCreatesDepartmentCorrectlyIfValidationSuccessful() throws Exception {

        perform(post("/web/department"));

        verify(departmentService).create(any(Department.class));
    }

    @Test
    public void postNewDepartmentAddsFlashAttributeAndRedirectsToDepartment() throws Exception {

        perform(post("/web/department"))
            .andExpect(flash().attribute("createdDepartment", instanceOf(Department.class)))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/department/"));
    }

    @Test
    public void editDepartmentForUnknownDepartmentIdThrowsUnknownDepartmentException() {

        assertThatThrownBy(() ->
            perform(get("/web/department/" + UNKNOWN_DEPARTMENT_ID + "/edit"))
        ).hasCauseInstanceOf(UnknownDepartmentException.class);
    }

    @Test
    public void editDepartmentAddsDepartmentAndActivePersonsToModel() throws Exception {

        final Department department = someDepartment();
        when(departmentService.getDepartmentById(SOME_DEPARTMENT_ID)).thenReturn(Optional.of(department));

        List<Person> persons = Collections.singletonList(somePerson());
        when(personService.getActivePersons()).thenReturn(persons);

        perform(get("/web/department/" + SOME_DEPARTMENT_ID + "/edit"))
            .andExpect(model().attribute(DEPARTMENT_ATTRIBUTE, department))
            .andExpect(model().attribute(PERSONS_ATTRIBUTE, persons));
    }

    @Test
    public void editDepartmentUsesCorrectView() throws Exception {

        when(departmentService.getDepartmentById(SOME_DEPARTMENT_ID)).thenReturn(Optional.of(someDepartment()));

        perform(get("/web/department/" + SOME_DEPARTMENT_ID + "/edit"))
            .andExpect(view().name("department/department_form"));
    }

    @Test
    public void updateDepartmentForUnknownDepartmentIdThrowsUnknownDepartmentException() {

        assertThatThrownBy(() ->
            perform(post("/web/department/" + UNKNOWN_DEPARTMENT_ID))
        ).hasCauseInstanceOf(UnknownDepartmentException.class);
    }

    @Test
    public void updateDepartmentShowsFormIfValidationFails() throws Exception {

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
    public void updateDepartmentUpdatesDepartmentCorrectIfValidationSuccessful() throws Exception {

        when(departmentService.getDepartmentById(SOME_DEPARTMENT_ID)).thenReturn(Optional.of(someDepartment()));

        perform(post("/web/department/" + SOME_DEPARTMENT_ID));

        verify(departmentService).update(any(Department.class));
    }

    @Test
    public void updateDepartmentAddsFlashAttributeAndRedirectsToDepartment() throws Exception {

        when(departmentService.getDepartmentById(SOME_DEPARTMENT_ID)).thenReturn(Optional.of(someDepartment()));

        perform(post("/web/department/" + SOME_DEPARTMENT_ID))
            .andExpect(flash().attribute("updatedDepartment", instanceOf(Department.class)))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/department/"));
    }

    @Test
    public void deleteDepartmentCallsServiceToDeleteDepartment() throws Exception {

        perform(delete("/web/department/" + SOME_DEPARTMENT_ID));

        verify(departmentService).delete(SOME_DEPARTMENT_ID);
    }

    @Test
    public void deleteDepartmentAddsFlashAttributeForExistingDepartment() throws Exception {

        final Department department = someDepartment();
        when(departmentService.getDepartmentById(SOME_DEPARTMENT_ID)).thenReturn(Optional.of(department));

        perform(delete("/web/department/" + SOME_DEPARTMENT_ID))
            .andExpect(flash().attribute("deletedDepartment", department));
    }

    @Test
    public void deleteDepartmentDoesNotAddFlashAttributeForNotExistingDepartment() throws Exception {

        when(departmentService.getDepartmentById(UNKNOWN_DEPARTMENT_ID)).thenReturn(Optional.empty());

        perform(delete("/web/department/" + UNKNOWN_DEPARTMENT_ID))
            .andExpect(flash().attribute("deletedDepartment", nullValue()));
    }

    @Test
    public void deleteDepartmentRedirectsToDepartment() throws Exception {

        perform(delete("/web/department/" + SOME_DEPARTMENT_ID))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/department/"));
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
