package org.synyx.urlaubsverwaltung.overview.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceExceptionHandler;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;


@ExtendWith(MockitoExtension.class)
class VacationApiControllerTest {

    private VacationApiController sut;

    @Mock
    private PersonService personService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        sut = new VacationApiController(personService, applicationService, departmentService);
    }

    @Test
    void ensureReturnsAllAllowedVacationsIfNoPersonProvided() throws Exception {

        perform(get("/api/vacations")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk());

        verify(applicationService).getApplicationsForACertainPeriodAndState(
            LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31), ALLOWED);
        verifyNoInteractions(personService);
    }

    @Test
    void ensureReturnsAllowedVacationsOfPersonIfPersonProvided() throws Exception {

        final Person person = DemoDataCreator.createPerson();
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
    void ensureCorrectConversionOfVacations() throws Exception {

        final Application vacation1 = DemoDataCreator.createApplication(DemoDataCreator.createPerson("foo"),
            LocalDate.of(2016, 5, 19), LocalDate.of(2016, 5, 20), DayLength.FULL);
        vacation1.setStatus(ALLOWED);

        final Application vacation2 = DemoDataCreator.createApplication(DemoDataCreator.createPerson("bar"),
            LocalDate.of(2016, 4, 5), LocalDate.of(2016, 4, 10), DayLength.FULL);

        when(applicationService.getApplicationsForACertainPeriodAndState(any(LocalDate.class),
            any(LocalDate.class), any(ApplicationStatus.class)))
            .thenReturn(Arrays.asList(vacation1, vacation2));

        perform(get("/api/vacations").param("from", "2016-01-01").param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;"))
            .andExpect(jsonPath("$.response").exists())
            .andExpect(jsonPath("$.response.vacations").exists())
            .andExpect(jsonPath("$.response.vacations", hasSize(2)))
            .andExpect(jsonPath("$.response.vacations[0].from", is("2016-05-19")))
            .andExpect(jsonPath("$.response.vacations[0].to", is("2016-05-20")))
            .andExpect(jsonPath("$.response.vacations[0].person").exists());
    }

    @Test
    void ensureBadRequestForMissingFromParameter() throws Exception {

        perform(get("/api/vacations")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidFromParameter() throws Exception {

        perform(get("/api/vacations")
            .param("from", "foo")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingToParameter() throws Exception {

        perform(get("/api/vacations")
            .param("from", "2016-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidToParameter() throws Exception {

        perform(get("/api/vacations")
            .param("from", "2016-01-01")
            .param("to", "foo"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidPeriod() throws Exception {

        perform(get("/api/vacations")
            .param("from", "2016-01-01")
            .param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidPersonParameter() throws Exception {

        perform(get("/api/vacations")
            .param("from", "2016-01-01")
            .param("to", "foo")
            .param("person", "foo"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestIfThereIsNoPersonForGivenID() throws Exception {
        perform(get("/api/vacations")
            .param("from", "2016-01-01")
            .param("to", "foo")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build().perform(builder);
    }
}
