package org.synyx.urlaubsverwaltung.calendar;

import liquibase.pro.packaged.F;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.TestContainersBase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Locale.GERMAN;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@SpringBootTest
class ICalViewControllerSecurityIT extends TestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonCalendarService personCalendarService;
    @MockBean
    private DepartmentCalendarService departmentCalendarService;
    @MockBean
    private CompanyCalendarService companyCalendarService;

    @Test
    void getPersonCalendarUnauthorized() throws Exception {

        final String secret = "eid5ae0zooKu";
        when(personCalendarService.getCalendarForPerson(1, secret, GERMAN)).thenReturn(generateFile("calendar"));

        perform(get("/web/persons/1/calendar").param("secret", secret))
            .andExpect(status().isOk());
    }

    @Test
    void getDepartmentCalendarUnauthorized() throws Exception {

        final String secret = "eid5ae0zooKu";
        when(departmentCalendarService.getCalendarForDepartment(1, 2, secret, GERMAN)).thenReturn(generateFile("calendar"));

        perform(get("/web/departments/1/persons/2/calendar").param("secret", secret))
            .andExpect(status().isOk());
    }

    @Test
    void getCompanyCalendarUnauthorized() throws Exception {

        final String secret = "eid5ae0zooKu";
        when(companyCalendarService.getCalendarForAll(1, secret, GERMAN)).thenReturn(generateFile("calendar"));

        perform(get("/web/company/persons/1/calendar").param("secret", secret))
            .andExpect(status().isOk());
    }

    private File generateFile(String... content) throws IOException {
        final Path file = Paths.get("calendar.ics");
        Files.write(file, asList(content.clone()), UTF_8);

        final File iCal = file.toFile();
        iCal.deleteOnExit();

        return iCal;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
