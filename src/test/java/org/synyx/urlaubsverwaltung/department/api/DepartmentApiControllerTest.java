package org.synyx.urlaubsverwaltung.department.api;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.api.ApiExceptionHandlerControllerAdvice;
import org.synyx.urlaubsverwaltung.department.DepartmentService;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class DepartmentApiControllerTest {

    private MockMvc mockMvc;

    private DepartmentService departmentServiceMock;

    @Before
    public void setUp() {

        departmentServiceMock = mock(DepartmentService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new DepartmentApiController(departmentServiceMock))
            .setControllerAdvice(new ApiExceptionHandlerControllerAdvice())
            .build();
    }


    @Test
    public void ensureReturnsAllDepartments() throws Exception {

        when(departmentServiceMock.getAllDepartments()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/departments")).andExpect(status().isOk());

        verify(departmentServiceMock).getAllDepartments();
    }
}
