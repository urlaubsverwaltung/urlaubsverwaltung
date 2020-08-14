package org.synyx.urlaubsverwaltung.sicknote.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceExceptionHandler;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.DemoDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.DemoDataCreator.createSickNote;

@ExtendWith(MockitoExtension.class)
class SickNoteApiControllerTest {

    private SickNoteApiController sut;

    @Mock
    private PersonService personService;
    @Mock
    private SickNoteService sickNoteService;

    @BeforeEach
    void setUp() {
        sut = new SickNoteApiController(sickNoteService, personService);
    }

    @Test
    void ensureReturnsAllSickNotesIfNoPersonProvided() throws Exception {
        perform(get("/api/sicknotes")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk());

        verify(sickNoteService).getByPeriod(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31));
        verifyNoInteractions(personService);
    }

    @Test
    void ensureReturnsSickNotesOfPersonIfPersonProvided() throws Exception {
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(createPerson()));

        perform(get("/api/sicknotes").param("from", "2016-01-01")
            .param("to", "2016-12-31")
            .param("person", "23"))
            .andExpect(status().isOk());

        verify(sickNoteService)
            .getByPersonAndPeriod(any(Person.class), eq(LocalDate.of(2016, 1, 1)),
                eq(LocalDate.of(2016, 12, 31)));
        verify(personService).getPersonByID(23);
    }

    @Test
    void ensureCorrectConversionOfSickNotes() throws Exception {

        SickNote sickNote1 = createSickNote(createPerson("foo"),
            LocalDate.of(2016, 5, 19), LocalDate.of(2016, 5, 20), DayLength.FULL);
        SickNote sickNote2 = createSickNote(createPerson("bar"));
        SickNote sickNote3 = createSickNote(createPerson("baz"));

        when(sickNoteService.getByPeriod(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList(sickNote1, sickNote2, sickNote3));

        perform(get("/api/sicknotes")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.sickNotes").exists())
            .andExpect(jsonPath("$.sickNotes", hasSize(3)))
            .andExpect(jsonPath("$.sickNotes[0].from", is("2016-05-19")))
            .andExpect(jsonPath("$.sickNotes[0].to", is("2016-05-20")))
            .andExpect(jsonPath("$.sickNotes[0].person").exists());
    }

    @Test
    void ensureBadRequestForMissingFromParameter() throws Exception {
        perform(get("/api/sicknotes")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidFromParameter() throws Exception {
        perform(get("/api/sicknotes")
            .param("from", "foo")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingToParameter() throws Exception {
        perform(get("/api/sicknotes")
            .param("from", "2016-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidToParameter() throws Exception {
        perform(get("/api/sicknotes")
            .param("from", "2016-01-01")
            .param("to", "foo"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidPeriod() throws Exception {
        perform(get("/api/sicknotes")
            .param("from", "2016-01-01")
            .param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidPersonParameter() throws Exception {
        perform(get("/api/sicknotes")
            .param("from", "2016-01-01")
            .param("to", "foo")
            .param("person", "foo"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestIfThereIsNoPersonForGivenID() throws Exception {
        perform(get("/api/sicknotes")
            .param("from", "2016-01-01")
            .param("to", "foo")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build().perform(builder);
    }
}
