package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceExceptionHandler;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;


@ExtendWith(MockitoExtension.class)
class AvailabilityApiControllerTest {

    private static final Integer PERSON_ID = 1;

    private AvailabilityApiController sut;

    @Mock
    private PersonService personService;
    @Mock
    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        sut = new AvailabilityApiController(availabilityService, personService);
    }

    @Test
    void ensurePersonsAvailabilitiesForUnknownPersonResultsInBadRequest() throws Exception {
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.empty());

        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureFetchesAvailabilitiesForGivenPersonIfProvided() throws Exception {
        final Person testPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(testPerson));

        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"))
            .andExpect(status().isOk());

        verify(availabilityService)
            .getPersonsAvailabilities(eq(LocalDate.of(2016, 1, 1)),
                eq(LocalDate.of(2016, 1, 31)), eq(testPerson));
    }

    @Test
    void ensureNoContentAvailabilitiesForGivenPersonWithoutConfiguredWorkingTime() throws Exception {

        final Person testPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(testPerson));

        when(availabilityService.getPersonsAvailabilities(any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenThrow(FreeTimeAbsenceException.class);
        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("from", "2015-01-01")
            .param("to", "2015-01-31"))
            .andExpect(status().isNoContent());
    }

    @Test
    void ensureRequestsAreOnlyAllowedForADateRangeOfMaxOneMonth() throws Exception {
        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("from", "2016-01-01")
            .param("to", "2016-02-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingPersonParameter() throws Exception {
        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("to", "2016-12-31")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingFromParameter() throws Exception {
        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidFromParameter() throws Exception {
        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("from", "foo")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingToParameter() throws Exception {
        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("from", "2016-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidToParameter() throws Exception {
        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("from", "2016-01-01")
            .param("to", "foo"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidPeriod() throws Exception {
        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("from", "2016-01-01")
            .param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build().perform(builder);
    }
}
