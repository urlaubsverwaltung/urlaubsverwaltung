package org.synyx.urlaubsverwaltung.workingtime.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceExceptionHandler;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.NoValidWorkingTimeException;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;


@ExtendWith(MockitoExtension.class)
class WorkDayApiControllerTest {

    private WorkDayApiController sut;

    @Mock
    private PersonService personServiceMock;
    @Mock
    private WorkDaysService workDaysServiceMock;

    @BeforeEach
    void setUp() {
        sut = new WorkDayApiController(personServiceMock, workDaysServiceMock);
    }

    @Test
    void ensureReturnsWorkDays() throws Exception {

        Person person = createPerson();
        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));
        when(workDaysServiceMock.getWorkDays(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(BigDecimal.ONE);

        perform(get("/api/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL")
            .param("person", "23"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.workDays").exists())
            .andExpect(jsonPath("$.workDays", is("1")));

        verify(personServiceMock).getPersonByID(23);
        verify(workDaysServiceMock).getWorkDays(FULL, LocalDate.of(2016, 1, 4), LocalDate.of(2016, 1, 4), person);
    }

    @Test
    void ensureReturnsNoContentForMissingWorkingDay() throws Exception {

        final Person person = createPerson();
        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));
        when(workDaysServiceMock.getWorkDays(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenThrow(NoValidWorkingTimeException.class);

        perform(get("/api/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL")
            .param("person", "23"))
            .andExpect(status().isNoContent());
    }

    @Test
    void ensureBadRequestForMissingFromParameter() throws Exception {

        perform(get("/api/workdays")
            .param("to", "2016-01-06")
            .param("length", "FULL")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidFromParameter() throws Exception {

        perform(get("/api/workdays")
            .param("from", "foo")
            .param("to", "2016-01-06")
            .param("length", "FULL")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingToParameter() throws Exception {

        perform(get("/api/workdays")
            .param("from", "2016-01-01")
            .param("length", "FULL")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidToParameter() throws Exception {

        perform(get("/api/workdays")
            .param("from", "2016-01-01")
            .param("to", "foo")
            .param("length", "FULL")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingPersonParameter() throws Exception {

        perform(get("/api/workdays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-06")
            .param("length", "FULL"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidPersonParameter() throws Exception {

        perform(get("/api/workdays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-06")
            .param("length", "FULL")
            .param("person", "foo"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestIfThereIsNoPersonForGivenID() throws Exception {

        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.empty());

        perform(get("/api/workdays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-06")
            .param("length", "FULL")
            .param("person", "23"))
            .andExpect(status().isBadRequest());

        verify(personServiceMock).getPersonByID(23);
    }

    @Test
    void ensureBadRequestForMissingLengthParameter() throws Exception {

        perform(get("/api/workdays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-06")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidLengthParameter() throws Exception {

        final Person person = createPerson();
        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        perform(get("/api/workdays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-06")
            .param("length", "FOO")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidPeriod() throws Exception {

        perform(get("/api/workdays")
            .param("from", "2016-01-01")
            .param("to", "2015-01-06")
            .param("length", "FULL")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build().perform(builder);
    }
}
