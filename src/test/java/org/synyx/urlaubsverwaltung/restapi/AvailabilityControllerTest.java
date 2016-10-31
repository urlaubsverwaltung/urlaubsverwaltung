package org.synyx.urlaubsverwaltung.restapi;

import org.joda.time.DateMidnight;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author  Timo Eifler - eifler@synyx.de
 */
public class AvailabilityControllerTest {

    private MockMvc mockMvc;

    private PersonService personServiceMock;
    private AvailabilityService availabilityServiceMock;
    private List<Person> activePersons;
    private Person testPerson1;

    @Before
    public void setUp() {

        preparePersonServiceMock();

        availabilityServiceMock = Mockito.mock(AvailabilityService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new AvailabilityController(availabilityServiceMock,
                        personServiceMock)).setControllerAdvice(new ApiExceptionHandlerControllerAdvice()).build();
    }


    private void preparePersonServiceMock() {

        personServiceMock = Mockito.mock(PersonService.class);

        testPerson1 = TestDataCreator.createPerson("testPerson1");

        Mockito.when(personServiceMock.getPersonByID(Mockito.anyInt())).thenReturn(Optional.of(testPerson1));

        Person testPerson2 = TestDataCreator.createPerson("testPerson2");
        activePersons = Arrays.asList(testPerson1, testPerson2);
        Mockito.when(personServiceMock.getActivePersons()).thenReturn(activePersons);
    }


    @Test
    public void ensureFetchesAvailabilitiesForAllActivePersonsIfNoPersonProvided() throws Exception {

        mockMvc.perform(get("/api/availability").param("from", "2016-01-01").param("to", "2016-12-31"))
            .andExpect(status().isOk());

        Mockito.verify(personServiceMock).getActivePersons();

        int numberOfActivePersons = activePersons.size();
        Mockito.verify(availabilityServiceMock, Mockito.times(numberOfActivePersons))
            .getPersonsAvailabilities(Mockito.eq(new DateMidnight(2016, 1, 1)),
                Mockito.eq(new DateMidnight(2016, 12, 31)), Mockito.any(Person.class));
    }


    @Test
    public void ensureFetchesAvailabilitiesForGivenPersonIfProvided() throws Exception {

        int personId = 1;

        mockMvc.perform(get("/api/availability").param("from", "2016-01-01")
                .param("to", "2016-12-31")
                .param("person", "" + personId))
            .andExpect(status().isOk());

        Mockito.verify(personServiceMock).getPersonByID(personId);

        Mockito.verify(availabilityServiceMock)
            .getPersonsAvailabilities(Mockito.eq(new DateMidnight(2016, 1, 1)),
                Mockito.eq(new DateMidnight(2016, 12, 31)), Mockito.eq(testPerson1));
    }


    @Test
    public void ensureBadRequestForMissingFromParameter() throws Exception {

        mockMvc.perform(get("/api/availability").param("to", "2016-12-31")).andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidFromParameter() throws Exception {

        mockMvc.perform(get("/api/availability").param("from", "foo").param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForMissingToParameter() throws Exception {

        mockMvc.perform(get("/api/availability").param("from", "2016-01-01")).andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidToParameter() throws Exception {

        mockMvc.perform(get("/api/availability").param("from", "2016-01-01").param("to", "foo"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidPeriod() throws Exception {

        mockMvc.perform(get("/api/availability").param("from", "2016-01-01").param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }
}
