package org.synyx.urlaubsverwaltung.sicknote.api;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.api.ApiExceptionHandlerControllerAdvice;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class SickNoteApiControllerTest {

    private MockMvc mockMvc;

    private PersonService personServiceMock;
    private SickNoteService sickNoteServiceMock;

    @Before
    public void setUp() {

        sickNoteServiceMock = mock(SickNoteService.class);
        personServiceMock = mock(PersonService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new SickNoteApiController(sickNoteServiceMock, personServiceMock))
            .setControllerAdvice(new ApiExceptionHandlerControllerAdvice())
            .build();
    }


    @Test
    public void ensureReturnsAllSickNotesIfNoPersonProvided() throws Exception {

        mockMvc.perform(get("/api/sicknotes").param("from", "2016-01-01").param("to", "2016-12-31"))
            .andExpect(status().isOk());

        verify(sickNoteServiceMock).getByPeriod(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31));
        verifyZeroInteractions(personServiceMock);
    }


    @Test
    public void ensureReturnsSickNotesOfPersonIfPersonProvided() throws Exception {

        when(personServiceMock.getPersonByID(anyInt()))
            .thenReturn(Optional.of(TestDataCreator.createPerson()));

        mockMvc.perform(get("/api/sicknotes").param("from", "2016-01-01")
            .param("to", "2016-12-31")
            .param("person", "23"))
            .andExpect(status().isOk());

        verify(sickNoteServiceMock)
            .getByPersonAndPeriod(any(Person.class), eq(LocalDate.of(2016, 1, 1)),
                eq(LocalDate.of(2016, 12, 31)));
        verify(personServiceMock).getPersonByID(23);
    }


    @Test
    public void ensureCorrectConversionOfSickNotes() throws Exception {

        SickNote sickNote1 = TestDataCreator.createSickNote(TestDataCreator.createPerson("foo"),
            LocalDate.of(2016, 5, 19), LocalDate.of(2016, 5, 20), DayLength.FULL);
        SickNote sickNote2 = TestDataCreator.createSickNote(TestDataCreator.createPerson("bar"));
        SickNote sickNote3 = TestDataCreator.createSickNote(TestDataCreator.createPerson("baz"));

        when(sickNoteServiceMock.getByPeriod(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList(sickNote1, sickNote2, sickNote3));

        mockMvc.perform(get("/api/sicknotes").param("from", "2016-01-01").param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.response").exists())
            .andExpect(jsonPath("$.response.sickNotes").exists())
            .andExpect(jsonPath("$.response.sickNotes", hasSize(3)))
            .andExpect(jsonPath("$.response.sickNotes[0].from", is("2016-05-19")))
            .andExpect(jsonPath("$.response.sickNotes[0].to", is("2016-05-20")))
            .andExpect(jsonPath("$.response.sickNotes[0].person").exists());
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

        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/sicknotes").param("from", "2016-01-01").param("to", "foo").param("person", "23"))
            .andExpect(status().isBadRequest());
    }
}
