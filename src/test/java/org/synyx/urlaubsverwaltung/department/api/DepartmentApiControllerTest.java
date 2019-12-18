package org.synyx.urlaubsverwaltung.department.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.api.ApiExceptionHandlerControllerAdvice;
import org.synyx.urlaubsverwaltung.department.DepartmentService;

import java.util.Collections;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(MockitoJUnitRunner.class)
public class DepartmentApiControllerTest {

    private DepartmentApiController sut;

    @Mock
    private DepartmentService departmentService;

    @Before
    public void setUp() {
         sut = new DepartmentApiController(departmentService);
    }

    @Test
    public void ensureReturnsAllDepartments() throws Exception {

        when(departmentService.getAllDepartments()).thenReturn(emptyList());

        perform(get("/api/departments")).andExpect(status().isOk());
        verify(departmentService).getAllDepartments();
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.standaloneSetup(sut).setControllerAdvice(new ApiExceptionHandlerControllerAdvice()).build().perform(builder);
    }
}
