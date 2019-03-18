package org.synyx.urlaubsverwaltung.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class RestApiSecurityConfigIT {

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    public void getAbsencesWithoutBasicAuthIsUnauthorized() throws Exception {
        mvc.perform(get("/api/absences"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void getAbsencesWithBasicAuthIsOk() throws Exception {
        mvc.perform(get("/api/absences")
                .param("year", String.valueOf(LocalDate.now().getYear()))
                .param("person", "4")
                .with(httpBasic("test", "secret")))
            .andExpect(status().isOk());
    }

    @Test
    public void getAvailabilitiesWithoutBasicAuthIsUnauthorized() throws Exception {
        mvc.perform(get("/api/availabilities"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void getAvailabilitiesWithBasicAuthIsOk() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        mvc.perform(get("/api/availabilities")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .param("person", "test")
                .with(httpBasic("test", "secret")))
            .andExpect(status().isOk());
    }

    @Test
    public void getDepartmentsWithoutBasicAuthIsUnauthorized() throws Exception {
        mvc.perform(get("/api/departments"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void getDepartmentsWithBasicAuthIsOk() throws Exception {
        mvc.perform(get("/api/departments")
            .with(httpBasic("test", "secret")))
            .andExpect(status().isOk());

    }

    @Test
    public void getPersonsWithoutBasicAuthIsUnauthorized() throws Exception {
        mvc.perform(get("/api/persons"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getPersonsWithBasicAuthIsOk() throws Exception {
        mvc.perform(get("/api/persons")
                .with(httpBasic("test", "secret")))
                .andExpect(status().isOk());

    }

    @Test
    public void getHolidaysWithoutBasicAuthIsUnauthorized() throws Exception {
        mvc.perform(get("/api/holidays"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void getHolidaysWithBasicAuthIsOk() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        mvc.perform(get("/api/holidays")
                .param("year", String.valueOf(now.getYear()))
                .param("month", String.valueOf(now.getMonthValue()))
                .param("personId", "4")
                .with(httpBasic("test", "secret")))
                .andExpect(status().isOk());

    }

    @Test
    public void getSicknotesWithoutBasicAuthIsUnauthorized() throws Exception {
        mvc.perform(get("/api/sicknotes"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void getSicknotesWithBasicAuthIsOk() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        mvc.perform(get("/api/sicknotes")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(httpBasic("test", "secret")))
                .andExpect(status().isOk());

    }

    @Test
    public void getVacationsWithoutBasicAuthIsUnauthorized() throws Exception {
        mvc.perform(get("/api/vacations"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void getVacationsWithBasicAuthIsOk() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        mvc.perform(get("/api/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(httpBasic("test", "secret")))
                .andExpect(status().isOk());

    }

    @Test
    public void getVacationOverviewWithoutBasicAuthIsUnauthorized() throws Exception {
        mvc.perform(get("/api/vacationoverview"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void getVacationOverviewWithBasicAuthIsOk() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        mvc.perform(get("/api/vacationoverview")
                .param("selectedYear", String.valueOf(now.getYear()))
                .param("selectedMonth", String.valueOf(now.getMonthValue()))
                .param("selectedDepartment", "1")
                .with(httpBasic("test", "secret")))
                .andExpect(status().isOk());

    }

    @Test
    public void getWorkdaysWithoutBasicAuthIsUnauthorized() throws Exception {
        mvc.perform(get("/api/vacationoverview"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void getWorkdaysWithBasicAuthIsOk() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        mvc.perform(get("/api/workdays")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .param("length", "FULL")
                .param("person", "1")
                .with(httpBasic("test", "secret")))
                .andExpect(status().isOk());

    }
}
