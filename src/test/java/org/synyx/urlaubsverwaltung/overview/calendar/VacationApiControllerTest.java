package org.synyx.urlaubsverwaltung.overview.calendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.api.ApiExceptionHandlerControllerAdvice;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCEL_RE;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;


@RunWith(MockitoJUnitRunner.class)
public class VacationApiControllerTest {

    private VacationApiController sut;

    @Mock
    private PersonService personService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private DepartmentService departmentService;

    @Before
    public void setUp() {
        sut = new VacationApiController(personService, applicationService, departmentService);
    }

    @Test
    public void ensureReturnsAllAllowedVacationsIfNoPersonProvided() throws Exception {

        perform(get("/api/vacations")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk());

        verify(applicationService).getApplicationsForACertainPeriodAndState(
            LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31), ALLOWED);
        verifyZeroInteractions(personService);
    }

    @Test
    public void ensureReturnsAllowedVacationsOfPersonIfPersonProvided() throws Exception {

        final Person person = createPerson();
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        perform(get("/api/vacations")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31")
            .param("person", "23"))
            .andExpect(status().isOk());

        verify(applicationService)
            .getApplicationsForACertainPeriodAndPersonAndState(LocalDate.of(2016, 1, 1),
                LocalDate.of(2016, 12, 31), person, ALLOWED);
        verify(personService).getPersonByID(23);
    }

    @Test
    public void ensureCorrectConversionOfVacations() throws Exception {

        final Person personOne = createPerson("personOne");
        final Application vacation1 = createApplication(personOne, LocalDate.of(2016, 5, 19), LocalDate.of(2016, 5, 20), DayLength.FULL);
        vacation1.setStatus(ALLOWED);

        final Person personTwo = createPerson("personTwo");
        final Application vacation2 = createApplication(personTwo, LocalDate.of(2016, 4, 5), LocalDate.of(2016, 4, 10), DayLength.FULL);
        vacation2.setStatus(ALLOWED_CANCEL_RE);

        when(applicationService.getApplicationsForACertainPeriodAndState(any(LocalDate.class), any(LocalDate.class), eq(ALLOWED))).thenReturn(List.of(vacation1));
        when(applicationService.getApplicationsForACertainPeriodAndState(any(LocalDate.class), any(LocalDate.class), eq(ALLOWED_CANCEL_RE))).thenReturn(List.of( vacation2));

        perform(get("/api/vacations").param("from", "2016-01-01").param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.response").exists())
            .andExpect(jsonPath("$.response.vacations").exists())
            .andExpect(jsonPath("$.response.vacations", hasSize(2)))
            .andExpect(jsonPath("$.response.vacations[0].from", is("2016-05-19")))
            .andExpect(jsonPath("$.response.vacations[0].to", is("2016-05-20")))
            .andExpect(jsonPath("$.response.vacations[0].person").exists());
    }

    @Test
    public void ensureBadRequestForMissingFromParameter() throws Exception {

        perform(get("/api/vacations")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void ensureBadRequestForInvalidFromParameter() throws Exception {

        perform(get("/api/vacations")
            .param("from", "foo")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void ensureBadRequestForMissingToParameter() throws Exception {

        perform(get("/api/vacations")
            .param("from", "2016-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void ensureBadRequestForInvalidToParameter() throws Exception {

        perform(get("/api/vacations")
            .param("from", "2016-01-01")
            .param("to", "foo"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void ensureBadRequestForInvalidPeriod() throws Exception {

        perform(get("/api/vacations")
            .param("from", "2016-01-01")
            .param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void ensureBadRequestForInvalidPersonParameter() throws Exception {

        perform(get("/api/vacations")
            .param("from", "2016-01-01")
            .param("to", "foo")
            .param("person", "foo"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void ensureBadRequestIfThereIsNoPersonForGivenID() throws Exception {
        perform(get("/api/vacations")
            .param("from", "2016-01-01")
            .param("to", "foo")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.standaloneSetup(sut).setControllerAdvice(new ApiExceptionHandlerControllerAdvice()).build().perform(builder);
    }
}
