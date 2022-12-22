package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceExceptionHandler;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static java.time.Month.JANUARY;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;


@ExtendWith(MockitoExtension.class)
class WorkingTimeCalendarApiControllerTest {

    private WorkDaysCountApiController sut;

    @Mock
    private PersonService personService;
    @Mock
    private WorkDaysCountService workDaysCountService;

    @BeforeEach
    void setUp() {
        sut = new WorkDaysCountApiController(personService, workDaysCountService);
    }

    @Test
    void ensureReturnsWorkDays() throws Exception {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(BigDecimal.ONE);

        perform(get("/api/persons/23/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.workDays").exists())
            .andExpect(jsonPath("$.workDays", is("1")));

        verify(personService).getPersonByID(23L);
        verify(workDaysCountService).getWorkDaysCount(FULL, LocalDate.of(2016, 1, 4), LocalDate.of(2016, 1, 4), person);
    }

    @Test
    void ensureReturnsNoContentForMissingWorkingDay() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenThrow(WorkDaysCountException.class);

        perform(get("/api/persons/23/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL"))
            .andExpect(status().isNoContent());
    }

    @Test
    void ensureBadRequestForMissingFromParameter() throws Exception {

        perform(get("/api/persons/23/workdays")
            .param("to", "2016-01-06")
            .param("length", "FULL"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidFromParameter() throws Exception {

        perform(get("/api/persons/23/workdays")
            .param("from", "foo")
            .param("to", "2016-01-06")
            .param("length", "FULL"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingToParameter() throws Exception {

        perform(get("/api/persons/23/workdays")
            .param("from", "2016-01-01")
            .param("length", "FULL"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidToParameter() throws Exception {

        perform(get("/api/persons/23/workdays")
            .param("from", "2016-01-01")
            .param("to", "foo")
            .param("length", "FULL"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingPersonParameter() throws Exception {

        perform(get("/api/persons//workdays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-06")
            .param("length", "FULL"))
            .andExpect(status().isNotFound());
    }

    @Test
    void ensureBadRequestForInvalidPersonParameter() throws Exception {

        perform(get("/api/persons/foo/workdays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-06")
            .param("length", "FULL"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestIfThereIsNoPersonForGivenID() throws Exception {

        when(personService.getPersonByID(anyLong())).thenReturn(Optional.empty());

        perform(get("/api/persons/23/workdays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-06")
            .param("length", "FULL"))
            .andExpect(status().isBadRequest());

        verify(personService).getPersonByID(23L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    @NullSource
    void ensureDayLengthFullFallbackForMissingLengthParameter(String givenLength) throws Exception {

        final Person person = new Person();
        person.setId(23L);

        when(personService.getPersonByID(23L)).thenReturn(Optional.of(person));
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2016, JANUARY, 4), LocalDate.of(2016, JANUARY, 4), person))
            .thenReturn(BigDecimal.ONE);

        perform(get("/api/persons/23/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", givenLength))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.workDays").exists());
    }

    @Test
    void ensureBadRequestForInvalidLengthParameter() throws Exception {

        perform(get("/api/persons/23/workdays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-06")
            .param("length", "FOO"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidPeriod() throws Exception {

        perform(get("/api/persons/23/workdays")
            .param("from", "2016-01-01")
            .param("to", "2015-01-06")
            .param("length", "FULL"))
            .andExpect(status().isBadRequest());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build().perform(builder);
    }
}
