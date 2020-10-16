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
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createSickNote;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

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
    void getSickNotes() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate from = LocalDate.of(2016, 5, 19);
        final LocalDate to = LocalDate.of(2016, 5, 20);
        final SickNote sickNote1 = createSickNote(person, from, to, FULL);
        final SickNote sickNote2 = createSickNote(person);
        final SickNote sickNote3 = createSickNote(person);

        when(sickNoteService.getByPeriod(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(sickNote1, sickNote2, sickNote3));

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
    void getSickNotesBadRequestForMissingFromParameter() throws Exception {
        perform(get("/api/sicknotes")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getSickNotesBadRequestForInvalidFromParameter() throws Exception {
        perform(get("/api/sicknotes")
            .param("from", "foo")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getSickNotesBadRequestForMissingToParameter() throws Exception {
        perform(get("/api/sicknotes")
            .param("from", "2016-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getSickNotesBadRequestForInvalidToParameter() throws Exception {
        perform(get("/api/sicknotes")
            .param("from", "2016-01-01")
            .param("to", "foo"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getSickNotesBadRequestForInvalidPeriod() throws Exception {
        perform(get("/api/sicknotes")
            .param("from", "2016-01-01")
            .param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getSickNotesBadRequestForInvalidPersonParameter() throws Exception {
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

    @Test
    void personsSickNotes() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(23);
        when(personService.getPersonByID(23)).thenReturn(Optional.of(person));

        final LocalDate from = LocalDate.of(2016, 5, 19);
        final LocalDate to = LocalDate.of(2016, 5, 20);
        final SickNote sickNote1 = createSickNote(person, from, to, FULL);
        final SickNote sickNote2 = createSickNote(person);
        final SickNote sickNote3 = createSickNote(person);

        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(sickNote1, sickNote2, sickNote3));

        perform(get("/api/persons/23/sicknotes")
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
    void personsSickNotesForEmptyPerson() throws Exception {
        when(personService.getPersonByID(23)).thenReturn(Optional.empty());

        perform(get("/api/persons/23/sicknotes")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void personsSickNotesForMissingFromParameter() throws Exception {
        perform(get("/api/persons/23/sicknotes")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void personsSickNotesForInvalidFromParameter() throws Exception {
        perform(get("/api/persons/23/sicknotes")
            .param("from", "foo")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void personsSickNotesForMissingToParameter() throws Exception {
        perform(get("/api/persons/23/sicknotes")
            .param("from", "2016-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void personsSickNotesForInvalidToParameter() throws Exception {
        perform(get("/api/persons/23/sicknotes")
            .param("from", "2016-01-01")
            .param("to", "foo"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void personsSickNotesForInvalidPeriod() throws Exception {
        perform(get("/api/persons/23/sicknotes")
            .param("from", "2016-01-01")
            .param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build().perform(builder);
    }
}
