package org.synyx.urlaubsverwaltung.restapi.sicknote;

import org.joda.time.DateMidnight;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.restapi.ApiExceptionHandlerControllerAdvice;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteControllerTest {

    private MockMvc mockMvc;

    private PersonService personServiceMock;
    private SickNoteService sickNoteServiceMock;

    @Before
    public void setUp() {

        sickNoteServiceMock = Mockito.mock(SickNoteService.class);
        personServiceMock = Mockito.mock(PersonService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new SickNoteController(sickNoteServiceMock, personServiceMock))
            .setControllerAdvice(new ApiExceptionHandlerControllerAdvice())
                .build();
    }


    @Test
    public void ensureReturnsAllSickNotesIfNoPersonProvided() throws Exception {

        mockMvc.perform(get("/api/sicknotes").param("from", "2016-01-01").param("to", "2016-12-31"))
            .andExpect(status().isOk());

        Mockito.verify(sickNoteServiceMock).getByPeriod(new DateMidnight(2016, 1, 1), new DateMidnight(2016, 12, 31));
        Mockito.verifyZeroInteractions(personServiceMock);
    }


    @Test
    public void ensureReturnsSickNotesOfPersonIfPersonProvided() throws Exception {

        Mockito.when(personServiceMock.getPersonByID(Mockito.anyInt()))
            .thenReturn(Optional.of(TestDataCreator.createPerson()));

        mockMvc.perform(get("/api/sicknotes").param("from", "2016-01-01")
                .param("to", "2016-12-31")
                .param("person", "23"))
            .andExpect(status().isOk());

        Mockito.verify(sickNoteServiceMock)
            .getByPersonAndPeriod(Mockito.any(Person.class), Mockito.eq(new DateMidnight(2016, 1, 1)),
                Mockito.eq(new DateMidnight(2016, 12, 31)));
        Mockito.verify(personServiceMock).getPersonByID(23);
    }


    @Test
    public void ensureCorrectConversionOfSickNotes() throws Exception {

        SickNote sickNote1 = TestDataCreator.createSickNote(TestDataCreator.createPerson("foo"),
                new DateMidnight(2016, 5, 19), new DateMidnight(2016, 5, 20), DayLength.FULL);
        SickNote sickNote2 = TestDataCreator.createSickNote(TestDataCreator.createPerson("bar"));
        SickNote sickNote3 = TestDataCreator.createSickNote(TestDataCreator.createPerson("baz"));

        Mockito.when(sickNoteServiceMock.getByPeriod(Mockito.any(DateMidnight.class), Mockito.any(DateMidnight.class)))
            .thenReturn(Arrays.asList(sickNote1, sickNote2, sickNote3));

        mockMvc.perform(get("/api/sicknotes").param("from", "2016-01-01").param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.response").exists())
            .andExpect(jsonPath("$.response.sickNotes").exists())
            .andExpect(jsonPath("$.response.sickNotes", hasSize(3)))
            .andExpect(jsonPath("$.response.sickNotes[0].from", is("2016-05-19")))
            .andExpect(jsonPath("$.response.sickNotes[0].to", is("2016-05-20")))
            .andExpect(jsonPath("$.response.sickNotes[0].person").exists())
            .andExpect(jsonPath("$.response.sickNotes[0].person.ldapName", is("foo")));
    }


    @Test
    public void ensureBadRequestForMissingFromParameter() throws Exception {

        mockMvc.perform(get("/api/sicknotes").param("to", "2016-12-31")).andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidFromParameter() throws Exception {

        mockMvc.perform(get("/api/sicknotes").param("from", "foo").param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForMissingToParameter() throws Exception {

        mockMvc.perform(get("/api/sicknotes").param("from", "2016-01-01")).andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidToParameter() throws Exception {

        mockMvc.perform(get("/api/sicknotes").param("from", "2016-01-01").param("to", "foo"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidPeriod() throws Exception {

        mockMvc.perform(get("/api/sicknotes").param("from", "2016-01-01").param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidPersonParameter() throws Exception {

        mockMvc.perform(get("/api/sicknotes").param("from", "2016-01-01").param("to", "foo").param("person", "foo"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestIfThereIsNoPersonForGivenID() throws Exception {

        Mockito.when(personServiceMock.getPersonByID(Mockito.anyInt())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/sicknotes").param("from", "2016-01-01").param("to", "foo").param("person", "23"))
            .andExpect(status().isBadRequest());
    }
}
