package org.synyx.urlaubsverwaltung.department.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceExceptionHandler;
import org.synyx.urlaubsverwaltung.department.DepartmentService;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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
    void ensureReturnsAllDepartments() throws Exception {

        when(departmentService.getAllDepartments()).thenReturn(emptyList());

        perform(get("/api/departments")).andExpect(status().isOk());
        verify(departmentService).getAllDepartments();
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build().perform(builder);
    }
}
