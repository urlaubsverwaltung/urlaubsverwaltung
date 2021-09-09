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
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

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

@ExtendWith(MockitoExtension.class)
class PersonPermissionsViewControllerTest {

    private PersonPermissionsViewController sut;

    private static final int PERSON_ID = 1;
    private static final int UNKNOWN_PERSON_ID = 675;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PersonPermissionsDtoValidator validator;

    @BeforeEach
    void setUp() {
        sut = new PersonPermissionsViewController(personService, departmentService, validator);
    }

    @Test
    void showPersonPermissionsAndNotificationsFormUsesPersonsWithGivenId() throws Exception {

        final Person personWithGivenId = personWithId(PERSON_ID);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(personWithGivenId));

        perform(get("/web/person/" + PERSON_ID + "/edit"))
            .andExpect(model().attribute("person", hasProperty("id", is(PERSON_ID))));
    }

    @Test
    void showPersonPermissionsAndNotificationsForUnknownIdThrowsUnknownPersonException() {
        assertThatThrownBy(() ->
            perform(get("/web/person/" + UNKNOWN_PERSON_ID + "/edit"))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void showPersonPermissionsAndNotificationsAddsDepartmentsToModel() throws Exception {

        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(personWithId(PERSON_ID)));

        final List<Department> departments = List.of(new Department());
        final List<Department> secondStageDepartments = List.of(new Department());

        when(departmentService.getManagedDepartmentsOfDepartmentHead(any())).thenReturn(departments);
        when(departmentService.getManagedDepartmentsOfSecondStageAuthority(any())).thenReturn(secondStageDepartments);

        perform(get("/web/person/" + PERSON_ID + "/edit"))
            .andExpect(model().attribute("departments", departments))
            .andExpect(model().attribute("secondStageDepartments", secondStageDepartments));
    }

    @Test
    void showPersonPermissionsAndNotificationsUsesNewPersonUsesCorrectView() throws Exception {

        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(personWithId(PERSON_ID)));
        perform(get("/web/person/" + PERSON_ID + "/edit"))
            .andExpect(view().name("person/person_form"));
    }

    @Test
    void editPersonPermissionsAndNotificationsCorrectly() throws Exception {

        final Person person = new Person("username", "Meier", "Nina", "nina@inter.net");
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));

        perform(post("/web/person/" + PERSON_ID + "/edit")
            .param("id", "1")
            .param("permissions[0]", "USER")
            .param("permissions[1]", "OFFICE")
        );

        verify(personService).update(person);
    }

    @Test
    void editPersonPermissionsAndNotificationsForwardsToViewIfValidationFails() throws Exception {

        doAnswer(invocation -> {
            final Errors errors = invocation.getArgument(1);
            errors.rejectValue("permissions", "person.form.permissions.error.inactive");
            return null;
        }).when(validator).validate(any(), any());

        perform(post("/web/person/" + PERSON_ID + "/edit"))
            .andExpect(view().name("person/person_form"));
    }

    @Test
    void editPersonPermissionsAndNotificationsAddsFlashAttribute() throws Exception {
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(personWithId(PERSON_ID)));

        perform(post("/web/person/" + PERSON_ID + "/edit"))
            .andExpect(flash().attribute("updateSuccess", true));
    }

    @Test
    void editPersonPermissionsAndNotificationsRedirectsToUpdatedPerson() throws Exception {

        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(personWithId(PERSON_ID)));

        perform(post("/web/person/" + PERSON_ID + "/edit"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/person/" + PERSON_ID));
    }

    @Test
    void editPersonPermissionsAndNotificationsThrowsUnknownPersonException() {

        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            perform(post("/web/person/" + PERSON_ID + "/edit"))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    private static Person personWithId(int personId) {
        final Person person = new Person();
        person.setId(personId);
        return person;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
