package org.synyx.urlaubsverwaltung.calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@RunWith(SpringRunner.class)
@SpringBootTest
public class ICalViewControllerSecurityIT {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonCalendarService personCalendarService;
    @MockBean
    private DepartmentCalendarService departmentCalendarService;
    @MockBean
    private CompanyCalendarService companyCalendarService;

    @Test
    public void getPersonCalendarUnauthorized() throws Exception {

        final String secret = "eid5ae0zooKu";
        when(personCalendarService.getCalendarForPerson(1, secret)).thenReturn("calendar");

        perform(get("/web/persons/1/calendar").param("secret", secret))
            .andExpect(status().isOk());
    }

    @Test
    public void getDepartmentCalendarUnauthorized() throws Exception {

        final String secret = "eid5ae0zooKu";
        when(departmentCalendarService.getCalendarForDepartment(1, secret)).thenReturn("calendar");

        perform(get("/web/departments/1/calendar").param("secret", secret))
            .andExpect(status().isOk());
    }

    @Test
    public void getCompanyCalendarUnauthorized() throws Exception {

        final String secret = "eid5ae0zooKu";
        when(companyCalendarService.getCalendarForAll(secret)).thenReturn("calendar");

        perform(get("/web/company/calendar").param("secret", secret))
            .andExpect(status().isOk());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
