package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;

import static java.util.Locale.GERMAN;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class ICalViewControllerSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonCalendarService personCalendarService;
    @MockitoBean
    private DepartmentCalendarService departmentCalendarService;
    @MockitoBean
    private CompanyCalendarService companyCalendarService;

    @Test
    void getPersonCalendarUnauthorized() throws Exception {

        final String secret = "eid5ae0zooKu";
        when(personCalendarService.getCalendarForPerson(1L, secret, GERMAN)).thenReturn(new ByteArrayResource("calendar".getBytes()));

        perform(get("/web/persons/1/calendar")
            .locale(GERMAN).param("secret", secret))
            .andExpect(status().isOk());
    }

    @Test
    void getDepartmentCalendarUnauthorized() throws Exception {

        final String secret = "eid5ae0zooKu";
        when(departmentCalendarService.getCalendarForDepartment(1L, 2L, secret, GERMAN)).thenReturn(new ByteArrayResource("calendar".getBytes()));

        perform(get("/web/departments/1/persons/2/calendar")
            .locale(GERMAN).param("secret", secret))
            .andExpect(status().isOk());
    }

    @Test
    void getCompanyCalendarUnauthorized() throws Exception {

        final String secret = "eid5ae0zooKu";
        when(companyCalendarService.getCalendarForAll(1L, secret, GERMAN)).thenReturn(new ByteArrayResource("calendar".getBytes()));

        perform(get("/web/company/persons/1/calendar")
            .locale(GERMAN).param("secret", secret))
            .andExpect(status().isOk());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
