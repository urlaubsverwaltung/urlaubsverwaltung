package org.synyx.urlaubsverwaltung.absence;

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
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.DemoDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.DemoDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.DemoDataCreator.createSickNote;

@ExtendWith(MockitoExtension.class)
class AbsenceApiControllerTest {

    private AbsenceApiController sut;

    @Mock
    private PersonService personService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        sut = new AbsenceApiController(personService, applicationService, sickNoteService);
    }

    @Test
    void ensureReturnsAbsencesOfPerson() throws Exception {
        final Person person = createPerson();
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        perform(get("/api/absences")
            .param("from", "2016-01-01")
            .param("year", "2016")
            .param("person", "23"))
            .andExpect(status().isOk());

        verify(sickNoteService)
            .getByPersonAndPeriod(any(Person.class), eq(LocalDate.of(2016, 1, 1)),
                eq(LocalDate.of(2016, 12, 31)));
        verify(applicationService)
            .getApplicationsForACertainPeriodAndPerson(eq(LocalDate.of(2016, 1, 1)),
                eq(LocalDate.of(2016, 12, 31)), any(Person.class));
        verify(personService).getPersonByID(23);
    }

    @Test
    void ensureCorrectConversionOfVacationAndSickNotes() throws Exception {
        final Person person = createPerson();
        final SickNote sickNote = createSickNote(person, LocalDate.of(2016, 5, 19),
            LocalDate.of(2016, 5, 20), DayLength.FULL);
        sickNote.setId(1);
        final Application vacation = createApplication(person, LocalDate.of(2016, 4, 6),
            LocalDate.of(2016, 4, 6), DayLength.FULL);

        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        when(sickNoteService.getByPersonAndPeriod(any(Person.class),
            any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(singletonList(sickNote));

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(vacation));

        perform(get("/api/absences").param("year", "2016").param("person", "23"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(3)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-04-06")))
            .andExpect(jsonPath("$.absences[0].type", is("VACATION")))
            .andExpect(jsonPath("$.absences[1].date", is("2016-05-19")))
            .andExpect(jsonPath("$.absences[1].type", is("SICK_NOTE")))
            .andExpect(jsonPath("$.absences[1].href", is("1")))
            .andExpect(jsonPath("$.absences[2].date", is("2016-05-20")))
            .andExpect(jsonPath("$.absences[2].type", is("SICK_NOTE")))
            .andExpect(jsonPath("$.absences[2].href", is("1")));
    }

    @Test
    void ensureTypeFilterIsWorking() throws Exception {
        final Person person = createPerson();
        final Application vacation = createApplication(person, LocalDate.of(2016, 4, 6),
            LocalDate.of(2016, 4, 6), DayLength.FULL);

        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(singletonList(vacation));

        perform(get("/api/absences").param("year", "2016").param("person", "23").param("type", "VACATION"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(1)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-04-06")))
            .andExpect(jsonPath("$.absences[0].type", is("VACATION")));
    }

    @Test
    void ensureMonthFilterIsWorking() throws Exception {
        final Person person = createPerson();
        final Application vacation = createApplication(person, LocalDate.of(2016, 5, 30),
            LocalDate.of(2016, 6, 1), DayLength.FULL);
        final SickNote sickNote = createSickNote(person, LocalDate.of(2016, 6, 30),
            LocalDate.of(2016, 7, 6), DayLength.FULL);

        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));
        when(sickNoteService.getByPersonAndPeriod(any(Person.class), any(LocalDate.class), any(LocalDate.class))).thenReturn(singletonList(sickNote));
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(singletonList(vacation));

        perform(get("/api/absences").param("year", "2016").param("month", "6").param("person", "23"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(2)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-06-01")))
            .andExpect(jsonPath("$.absences[0].type", is("VACATION")))
            .andExpect(jsonPath("$.absences[1].date", is("2016-06-30")))
            .andExpect(jsonPath("$.absences[1].type", is("SICK_NOTE")));
    }

    @Test
    void ensureBadRequestForMissingYearParameter() throws Exception {
        perform(get("/api/absences")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidYearParameter() throws Exception {
        final Person person = createPerson();
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        perform(get("/api/absences")
            .param("year", "foo")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidMonthParameter() throws Exception {
        final Person person = createPerson();
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        perform(get("/api/absences")
            .param("year", "2016")
            .param("month", "foo")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForOtherInvalidMonthParameter() throws Exception {
        final Person person = createPerson();
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        perform(get("/api/absences")
            .param("year", "2016")
            .param("month", "30")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingPersonParameter() throws Exception {
        perform(get("/api/absences").param("year", "2016")).andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidPersonParameter() throws Exception {
        perform(get("/api/absences")
            .param("year", "2016")
            .param("person", "foo"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestIfThereIsNoPersonForGivenID() throws Exception {
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.empty());

        perform(get("/api/absences")
            .param("year", "2016")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidTypeParameter() throws Exception {
        when(personService.getPersonByID(anyInt()))
            .thenReturn(Optional.of(createPerson()));

        perform(get("/api/absences")
            .param("year", "2016")
            .param("person", "23")
            .param("type", "FOO"))
            .andExpect(status().isBadRequest());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build().perform(builder);
    }
}
