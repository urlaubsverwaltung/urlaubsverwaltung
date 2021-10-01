package org.synyx.urlaubsverwaltung.calendar;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.XProperty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.TestContainersBase;

import static java.util.Locale.GERMAN;
import static net.fortuna.ical4j.model.property.Version.VERSION_2_0;
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
        when(personCalendarService.getCalendarForPerson(1, secret, GERMAN)).thenReturn(generateCalendar("calendar"));

        perform(get("/web/persons/1/calendar").param("secret", secret))
            .andExpect(status().isOk());
    }

    @Test
    void getDepartmentCalendarUnauthorized() throws Exception {

        final String secret = "eid5ae0zooKu";
        when(departmentCalendarService.getCalendarForDepartment(1, 2, secret, GERMAN)).thenReturn(generateCalendar("calendar"));

        perform(get("/web/departments/1/persons/2/calendar").param("secret", secret))
            .andExpect(status().isOk());
    }

    @Test
    void getCompanyCalendarUnauthorized() throws Exception {

        final String secret = "eid5ae0zooKu";
        when(companyCalendarService.getCalendarForAll(1, secret, GERMAN)).thenReturn(generateCalendar("calendar"));

        perform(get("/web/company/persons/1/calendar").param("secret", secret))
            .andExpect(status().isOk());
    }

    private Calendar generateCalendar(String content) {

        final Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//Urlaubsverwaltung//iCal4j 1.0//DE"));
        calendar.getProperties().add(VERSION_2_0);
        calendar.getProperties().add(new XProperty("X-WR-CALNAME", content));
        final VEvent event = new VEvent(new Date(0L), "");
        event.getProperties().add(new Uid("uid"));
        calendar.getComponents().add(event);

        return calendar;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
