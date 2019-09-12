package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.api.ApiExceptionHandlerControllerAdvice;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;
import org.synyx.urlaubsverwaltung.workingtime.NoValidWorkingTimeException;

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


@RunWith(MockitoJUnitRunner.class)
public class AvailabilityApiControllerTest {

    private static final Integer PERSON_ID = 1;

    private AvailabilityApiController sut;

    @Mock
    private PersonService personService;
    @Mock
    private AvailabilityService availabilityService;

    private Person testPerson;

    @Before
    public void setUp() {

        sut = new AvailabilityApiController(availabilityService, personService);

        testPerson = TestDataCreator.createPerson("testPerson");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(testPerson));
    }

    @Test
    public void ensurePersonsAvailabilitiesForUnknownPersonResultsInBadRequest() throws Exception {

        when(personService.getPersonByID(anyInt())).thenReturn(Optional.empty());

        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void ensureFetchesAvailabilitiesForGivenPersonIfProvided() throws Exception {

        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"))
            .andExpect(status().isOk());

        verify(personService).getPersonByID(PERSON_ID);

        verify(availabilityService)
            .getPersonsAvailabilities(eq(LocalDate.of(2016, 1, 1)),
                eq(LocalDate.of(2016, 1, 31)), eq(testPerson));
    }

    @Test
    public void ensureNoContentAvailabilitiesForGivenPersonWithoutConfiguredWorkingTime() throws Exception {

        when(availabilityService.getPersonsAvailabilities(any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenThrow(NoValidWorkingTimeException.class);
        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("from", "2015-01-01")
            .param("to", "2015-01-31"))
            .andExpect(status().isNoContent());
    }


    @Test
    public void ensureRequestsAreOnlyAllowedForADateRangeOfMaxOneMonth() throws Exception {

        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("from", "2016-01-01")
            .param("to", "2016-02-01"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForMissingPersonParameter() throws Exception {

        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("to", "2016-12-31")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForMissingFromParameter() throws Exception {

        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidFromParameter() throws Exception {

        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("from", "foo")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForMissingToParameter() throws Exception {

        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("from", "2016-01-01"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidToParameter() throws Exception {

        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("from", "2016-01-01")
            .param("to", "foo"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidPeriod() throws Exception {

        perform(get("/api/persons/" + PERSON_ID + "/availabilities")
            .param("from", "2016-01-01")
            .param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).setControllerAdvice(new ApiExceptionHandlerControllerAdvice()).build().perform(builder);
    }
}
