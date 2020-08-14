package org.synyx.urlaubsverwaltung.department.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceExceptionHandler;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.DemoDataCreator.createPerson;


@ExtendWith(MockitoExtension.class)
class DepartmentApiControllerTest {

    private DepartmentApiController sut;

    @Mock
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        sut = new DepartmentApiController(departmentService);
    }

    @Test
    void departments() throws Exception {

        final Department departmentOne = new Department();
        departmentOne.setName("departmentOne");
        departmentOne.setDescription("Description One");
        departmentOne.setMembers(List.of(createPerson("One")));
        departmentOne.setDepartmentHeads(List.of(createPerson("OneDH")));

        final Department departmentTwo = new Department();
        departmentTwo.setName("departmentTwo");
        departmentTwo.setDescription("Description Two");
        departmentTwo.setMembers(List.of(createPerson("Two")));
        departmentTwo.setDepartmentHeads(List.of(createPerson("TwoDH")));

        final List<Department> departments = List.of(departmentOne, departmentTwo);
        when(departmentService.getAllDepartments()).thenReturn(departments);

        perform(get("/api/departments"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.departments").exists())
            .andExpect(jsonPath("$.departments", hasSize(2)))
            .andExpect(jsonPath("$.departments[0].name", is("departmentOne")))
            .andExpect(jsonPath("$.departments[0].description", is("Description One")))
            .andExpect(jsonPath("$.departments[0].members.persons[0].firstName", is("One")))
            .andExpect(jsonPath("$.departments[0].departmentHeads.persons[0].firstName", is("OneDH")))
            .andExpect(jsonPath("$.departments[1].name", is("departmentTwo")))
            .andExpect(jsonPath("$.departments[1].description", is("Description Two")))
            .andExpect(jsonPath("$.departments[1].members.persons[0].firstName", is("Two")))
            .andExpect(jsonPath("$.departments[1].departmentHeads.persons[0].firstName", is("TwoDH")));
    }

    @Test
    void departmentsWithEmptyResponse() throws Exception {

        when(departmentService.getAllDepartments()).thenReturn(emptyList());

        perform(get("/api/departments"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.departments").exists())
            .andExpect(jsonPath("$.departments", hasSize(0)));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {

        return standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build()
            .perform(builder);
    }
}
