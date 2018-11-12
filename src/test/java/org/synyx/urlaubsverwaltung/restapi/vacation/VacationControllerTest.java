package org.synyx.urlaubsverwaltung.restapi.vacation;

import org.joda.time.DateMidnight;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.restapi.ApiExceptionHandlerControllerAdvice;
import org.synyx.urlaubsverwaltung.restapi.vacation.VacationController;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class VacationControllerTest {

    private MockMvc mockMvc;

    private PersonService personServiceMock;
    private ApplicationService applicationServiceMock;
    private DepartmentService departmentServiceMock;

    @Before
    public void setUp() {

        personServiceMock = Mockito.mock(PersonService.class);
        applicationServiceMock = Mockito.mock(ApplicationService.class);
        departmentServiceMock = Mockito.mock(DepartmentService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new VacationController(personServiceMock, applicationServiceMock,
                        departmentServiceMock)).setControllerAdvice(new ApiExceptionHandlerControllerAdvice()).build();
    }


    @Test
    public void ensureReturnsAllAllowedVacationsIfNoPersonProvided() throws Exception {

        mockMvc.perform(get("/api/vacations").param("from", "2016-01-01").param("to", "2016-12-31"))
            .andExpect(status().isOk());

        Mockito.verify(applicationServiceMock)
            .getApplicationsForACertainPeriodAndState(new DateMidnight(2016, 1, 1), new DateMidnight(2016, 12, 31),
                ApplicationStatus.ALLOWED);
        Mockito.verifyZeroInteractions(personServiceMock);
    }


    @Test
    public void ensureReturnsAllowedVacationsOfPersonIfPersonProvided() throws Exception {

        Person person = TestDataCreator.createPerson();
        Mockito.when(personServiceMock.getPersonByID(Mockito.anyInt())).thenReturn(Optional.of(person));

        mockMvc.perform(get("/api/vacations").param("from", "2016-01-01")
                .param("to", "2016-12-31")
                .param("person", "23"))
            .andExpect(status().isOk());

        Mockito.verify(applicationServiceMock)
            .getApplicationsForACertainPeriodAndPersonAndState(new DateMidnight(2016, 1, 1),
                new DateMidnight(2016, 12, 31), person, ApplicationStatus.ALLOWED);
        Mockito.verify(personServiceMock).getPersonByID(23);
    }


    @Test
    public void ensureCorrectConversionOfVacations() throws Exception {

        Application vacation1 = TestDataCreator.createApplication(TestDataCreator.createPerson("foo"),
                new DateMidnight(2016, 5, 19), new DateMidnight(2016, 5, 20), DayLength.FULL);
        vacation1.setStatus(ApplicationStatus.ALLOWED);

        Application vacation2 = TestDataCreator.createApplication(TestDataCreator.createPerson("bar"),
                new DateMidnight(2016, 4, 5), new DateMidnight(2016, 4, 10), DayLength.FULL);

        Mockito.when(applicationServiceMock.getApplicationsForACertainPeriodAndState(Mockito.any(DateMidnight.class),
                    Mockito.any(DateMidnight.class), Mockito.any(ApplicationStatus.class)))
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

        Mockito.when(personServiceMock.getPersonByID(Mockito.anyInt())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/vacations").param("from", "2016-01-01").param("to", "foo").param("person", "23"))
            .andExpect(status().isBadRequest());
    }
}
