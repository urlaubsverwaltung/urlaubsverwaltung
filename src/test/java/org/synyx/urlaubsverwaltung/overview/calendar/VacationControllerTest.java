package org.synyx.urlaubsverwaltung.overview.calendar;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.api.ApiExceptionHandlerControllerAdvice;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class VacationControllerTest {

    private MockMvc mockMvc;

    private PersonService personServiceMock;
    private ApplicationService applicationServiceMock;
    private DepartmentService departmentServiceMock;

    @Before
    public void setUp() {

        personServiceMock = mock(PersonService.class);
        applicationServiceMock = mock(ApplicationService.class);
        departmentServiceMock = mock(DepartmentService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new VacationController(personServiceMock, applicationServiceMock,
                        departmentServiceMock)).setControllerAdvice(new ApiExceptionHandlerControllerAdvice()).build();
    }


    @Test
    public void ensureReturnsAllAllowedVacationsIfNoPersonProvided() throws Exception {

        mockMvc.perform(get("/api/vacations").param("from", "2016-01-01").param("to", "2016-12-31"))
            .andExpect(status().isOk());

        verify(applicationServiceMock)
            .getApplicationsForACertainPeriodAndState(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31),
                ApplicationStatus.ALLOWED);
        verifyZeroInteractions(personServiceMock);
    }


    @Test
    public void ensureReturnsAllowedVacationsOfPersonIfPersonProvided() throws Exception {

        Person person = TestDataCreator.createPerson();
        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        mockMvc.perform(get("/api/vacations").param("from", "2016-01-01")
                .param("to", "2016-12-31")
                .param("person", "23"))
            .andExpect(status().isOk());

        verify(applicationServiceMock)
            .getApplicationsForACertainPeriodAndPersonAndState(LocalDate.of(2016, 1, 1),
                LocalDate.of(2016, 12, 31), person, ApplicationStatus.ALLOWED);
        verify(personServiceMock).getPersonByID(23);
    }


    @Test
    public void ensureCorrectConversionOfVacations() throws Exception {

        Application vacation1 = TestDataCreator.createApplication(TestDataCreator.createPerson("foo"),
                LocalDate.of(2016, 5, 19), LocalDate.of(2016, 5, 20), DayLength.FULL);
        vacation1.setStatus(ApplicationStatus.ALLOWED);

        Application vacation2 = TestDataCreator.createApplication(TestDataCreator.createPerson("bar"),
                LocalDate.of(2016, 4, 5), LocalDate.of(2016, 4, 10), DayLength.FULL);

        when(applicationServiceMock.getApplicationsForACertainPeriodAndState(any(LocalDate.class),
                    any(LocalDate.class), any(ApplicationStatus.class)))
            .thenReturn(Arrays.asList(vacation1, vacation2));

        mockMvc.perform(get("/api/vacations").param("from", "2016-01-01").param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.response").exists())
            .andExpect(jsonPath("$.response.vacations").exists())
            .andExpect(jsonPath("$.response.vacations", hasSize(2)))
            .andExpect(jsonPath("$.response.vacations[0].from", is("2016-05-19")))
            .andExpect(jsonPath("$.response.vacations[0].to", is("2016-05-20")))
            .andExpect(jsonPath("$.response.vacations[0].person").exists())
            .andExpect(jsonPath("$.response.vacations[0].person.ldapName", is("foo")));
    }


    @Test
    public void ensureBadRequestForMissingFromParameter() throws Exception {

        mockMvc.perform(get("/api/vacations").param("to", "2016-12-31")).andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidFromParameter() throws Exception {

        mockMvc.perform(get("/api/vacations").param("from", "foo").param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForMissingToParameter() throws Exception {

        mockMvc.perform(get("/api/vacations").param("from", "2016-01-01")).andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidToParameter() throws Exception {

        mockMvc.perform(get("/api/vacations").param("from", "2016-01-01").param("to", "foo"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidPeriod() throws Exception {

        mockMvc.perform(get("/api/vacations").param("from", "2016-01-01").param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidPersonParameter() throws Exception {

        mockMvc.perform(get("/api/vacations").param("from", "2016-01-01").param("to", "foo").param("person", "foo"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestIfThereIsNoPersonForGivenID() throws Exception {

        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/vacations").param("from", "2016-01-01").param("to", "foo").param("person", "23"))
            .andExpect(status().isBadRequest());
    }
}
