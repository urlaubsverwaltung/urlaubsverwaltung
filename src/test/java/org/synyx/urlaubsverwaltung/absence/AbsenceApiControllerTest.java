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
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createSickNote;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

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
    void ensureCorrectConversionOfVacationAndSickNotes() throws Exception {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final LocalDate sickNoteStartDate = LocalDate.of(2016, 5, 19);
        final LocalDate sickNoteEndDate = LocalDate.of(2016, 5, 20);
        final SickNote sickNote = createSickNote(person, sickNoteStartDate, sickNoteEndDate, FULL);
        sickNote.setId(1);
        when(sickNoteService.getByPersonAndPeriod(any(Person.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(singletonList(sickNote));

        final LocalDate waitingApplicationDate = LocalDate.of(2016, 4, 6);
        final Application waitingApplication = createApplication(person, waitingApplicationDate, waitingApplicationDate, FULL);

        final LocalDate allowedApplicationDate = LocalDate.of(2016, 4, 7);
        final Application allowedApplication = createApplication(person, allowedApplicationDate, allowedApplicationDate, FULL);
        allowedApplication.setStatus(ALLOWED);

        final LocalDate tempAllowedApplicationDate = LocalDate.of(2016, 4, 8);
        final Application tempAllowedApplication = createApplication(person, tempAllowedApplicationDate, tempAllowedApplicationDate, FULL);
        tempAllowedApplication.setStatus(TEMPORARY_ALLOWED);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(List.of(waitingApplication, allowedApplication, tempAllowedApplication));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(5)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-04-06")))
            .andExpect(jsonPath("$.absences[0].type", is("VACATION")))
            .andExpect(jsonPath("$.absences[1].date", is("2016-04-07")))
            .andExpect(jsonPath("$.absences[1].type", is("VACATION")))
            .andExpect(jsonPath("$.absences[2].date", is("2016-04-08")))
            .andExpect(jsonPath("$.absences[2].type", is("VACATION")))
            .andExpect(jsonPath("$.absences[3].date", is("2016-05-19")))
            .andExpect(jsonPath("$.absences[3].type", is("SICK_NOTE")))
            .andExpect(jsonPath("$.absences[3].href", is("1")))
            .andExpect(jsonPath("$.absences[4].date", is("2016-05-20")))
            .andExpect(jsonPath("$.absences[4].type", is("SICK_NOTE")))
            .andExpect(jsonPath("$.absences[4].href", is("1")));
    }

    @Test
    void ensureTypeFilterIsWorking() throws Exception {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final Application vacation = createApplication(person, LocalDate.of(2016, 4, 6),
            LocalDate.of(2016, 4, 6), FULL);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(vacation));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31")
            .param("type", "VACATION"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(1)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-04-06")))
            .andExpect(jsonPath("$.absences[0].type", is("VACATION")));

        verifyNoInteractions(sickNoteService);
    }

    @Test
    void ensureFromToFilterIsWorking() throws Exception {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Application vacation = createApplication(person, LocalDate.of(2016, 5, 30),
            LocalDate.of(2016, 6, 1), FULL);
        final SickNote sickNote = createSickNote(person, LocalDate.of(2016, 6, 30),
            LocalDate.of(2016, 7, 6), FULL);

        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));
        when(sickNoteService.getByPersonAndPeriod(any(Person.class), any(LocalDate.class), any(LocalDate.class))).thenReturn(singletonList(sickNote));
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class))).thenReturn(singletonList(vacation));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-06-01")
            .param("to", "2016-06-30"))
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
    void ensureBadRequestForInvalidFromParameter() throws Exception {
        perform(get("/api/persons/23/absences")
            .param("from", "2016-01")
            .param("to", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidToParameter() throws Exception {
        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidPersonParameter() throws Exception {
        perform(get("/api/persons/foo/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingFromParameter() throws Exception {
        perform(get("/api/persons/23/absences")
            .param("to", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingToParameter() throws Exception {

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingPersonParameter() throws Exception {
        perform(get("/api/persons//absences")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31")
        ).andExpect(status().isNotFound());
    }

    @Test
    void ensureBadRequestIfThereIsNoPersonForGivenID() throws Exception {
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.empty());

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidTypeParameter() throws Exception {
        when(personService.getPersonByID(anyInt()))
            .thenReturn(Optional.of(new Person("muster", "Muster", "Marlene", "muster@example.org")));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31")
            .param("type", "FOO"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidPeriod() throws Exception {
        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build().perform(builder);
    }
}
