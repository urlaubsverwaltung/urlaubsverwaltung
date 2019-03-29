package org.synyx.urlaubsverwaltung.absence;

import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.absence.api.AbsenceController;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.api.ApiExceptionHandlerControllerAdvice;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class AbsenceControllerTest {

    private MockMvc mockMvc;

    private PersonService personServiceMock;
    private SickNoteService sickNoteServiceMock;
    private ApplicationService applicationServiceMock;

    @Before
    public void setUp() {

        personServiceMock = mock(PersonService.class);
        applicationServiceMock = mock(ApplicationService.class);
        sickNoteServiceMock = mock(SickNoteService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new AbsenceController(personServiceMock, applicationServiceMock,
                        sickNoteServiceMock)).setControllerAdvice(new ApiExceptionHandlerControllerAdvice()).build();
    }


    @Test
    public void ensureReturnsAbsencesOfPerson() throws Exception {

        Person person = TestDataCreator.createPerson("muster");
        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        mockMvc.perform(get("/api/absences").param("from", "2016-01-01").param("year", "2016").param("person", "23"))
            .andExpect(status().isOk());

        verify(sickNoteServiceMock)
            .getByPersonAndPeriod(any(Person.class), eq(new DateMidnight(2016, 1, 1)),
                eq(new DateMidnight(2016, 12, 31)));
        verify(applicationServiceMock)
            .getApplicationsForACertainPeriodAndPerson(eq(new DateMidnight(2016, 1, 1)),
                eq(new DateMidnight(2016, 12, 31)), any(Person.class));
        verify(personServiceMock).getPersonByID(23);
    }


    @Test
    public void ensureCorrectConversionOfVacationAndSickNotes() throws Exception {

        Person person = TestDataCreator.createPerson("muster");

        SickNote sickNote = TestDataCreator.createSickNote(person, new DateMidnight(2016, 5, 19),
                new DateMidnight(2016, 5, 20), DayLength.FULL);
        sickNote.setId(1);

        Application vacation = TestDataCreator.createApplication(person, new DateMidnight(2016, 4, 6),
                new DateMidnight(2016, 4, 6), DayLength.FULL);

        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        when(sickNoteServiceMock.getByPersonAndPeriod(any(Person.class),
                    any(DateMidnight.class), any(DateMidnight.class)))
            .thenReturn(Collections.singletonList(sickNote));

        when(applicationServiceMock.getApplicationsForACertainPeriodAndPerson(any(DateMidnight.class),
                    any(DateMidnight.class), any(Person.class)))
            .thenReturn(Collections.singletonList(vacation));

        mockMvc.perform(get("/api/absences").param("year", "2016").param("person", "23"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.response").exists())
            .andExpect(jsonPath("$.response.absences").exists())
            .andExpect(jsonPath("$.response.absences", hasSize(3)))
            .andExpect(jsonPath("$.response.absences[0].date", is("2016-04-06")))
            .andExpect(jsonPath("$.response.absences[0].type", is("VACATION")))
            .andExpect(jsonPath("$.response.absences[1].date", is("2016-05-19")))
            .andExpect(jsonPath("$.response.absences[1].type", is("SICK_NOTE")))
            .andExpect(jsonPath("$.response.absences[1].href", is("1")))
            .andExpect(jsonPath("$.response.absences[2].date", is("2016-05-20")))
            .andExpect(jsonPath("$.response.absences[2].type", is("SICK_NOTE")))
            .andExpect(jsonPath("$.response.absences[2].href", is("1")));
    }


    @Test
    public void ensureTypeFilterIsWorking() throws Exception {

        Person person = TestDataCreator.createPerson("muster");

        Application vacation = TestDataCreator.createApplication(person, new DateMidnight(2016, 4, 6),
                new DateMidnight(2016, 4, 6), DayLength.FULL);

        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        when(sickNoteServiceMock.getByPersonAndPeriod(any(Person.class),
                    any(DateMidnight.class), any(DateMidnight.class)))
            .thenReturn(Collections.singletonList(TestDataCreator.createSickNote(person)));

        when(applicationServiceMock.getApplicationsForACertainPeriodAndPerson(any(DateMidnight.class),
                    any(DateMidnight.class), any(Person.class)))
            .thenReturn(Collections.singletonList(vacation));

        mockMvc.perform(get("/api/absences").param("year", "2016").param("person", "23").param("type", "VACATION"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.response").exists())
            .andExpect(jsonPath("$.response.absences").exists())
            .andExpect(jsonPath("$.response.absences", hasSize(1)))
            .andExpect(jsonPath("$.response.absences[0].date", is("2016-04-06")))
            .andExpect(jsonPath("$.response.absences[0].type", is("VACATION")));
    }


    @Test
    public void ensureMonthFilterIsWorking() throws Exception {

        Person person = TestDataCreator.createPerson("muster");

        Application vacation = TestDataCreator.createApplication(person, new DateMidnight(2016, 5, 30),
                new DateMidnight(2016, 6, 1), DayLength.FULL);

        SickNote sickNote = TestDataCreator.createSickNote(person, new DateMidnight(2016, 6, 30),
                new DateMidnight(2016, 7, 6), DayLength.FULL);

        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        when(sickNoteServiceMock.getByPersonAndPeriod(any(Person.class),
                    any(DateMidnight.class), any(DateMidnight.class)))
            .thenReturn(Collections.singletonList(sickNote));

        when(applicationServiceMock.getApplicationsForACertainPeriodAndPerson(any(DateMidnight.class),
                    any(DateMidnight.class), any(Person.class)))
            .thenReturn(Collections.singletonList(vacation));

        mockMvc.perform(get("/api/absences").param("year", "2016").param("month", "6").param("person", "23"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andDo(print())
            .andExpect(jsonPath("$.response").exists())
            .andExpect(jsonPath("$.response.absences").exists())
            .andExpect(jsonPath("$.response.absences", hasSize(2)))
            .andExpect(jsonPath("$.response.absences[0].date", is("2016-06-01")))
            .andExpect(jsonPath("$.response.absences[0].type", is("VACATION")))
            .andExpect(jsonPath("$.response.absences[1].date", is("2016-06-30")))
            .andExpect(jsonPath("$.response.absences[1].type", is("SICK_NOTE")));
    }


    @Test
    public void ensureBadRequestForMissingYearParameter() throws Exception {

        mockMvc.perform(get("/api/absences").param("person", "23")).andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidYearParameter() throws Exception {

        Person person = TestDataCreator.createPerson("muster");
        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        mockMvc.perform(get("/api/absences").param("year", "foo").param("person", "23"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidMonthParameter() throws Exception {

        Person person = TestDataCreator.createPerson("muster");
        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        mockMvc.perform(get("/api/absences").param("year", "2016").param("month", "foo").param("person", "23"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForOtherInvalidMonthParameter() throws Exception {

        Person person = TestDataCreator.createPerson("muster");
        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        mockMvc.perform(get("/api/absences").param("year", "2016").param("month", "30").param("person", "23"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForMissingPersonParameter() throws Exception {

        mockMvc.perform(get("/api/absences").param("year", "2016")).andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidPersonParameter() throws Exception {

        mockMvc.perform(get("/api/absences").param("year", "2016").param("person", "foo"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestIfThereIsNoPersonForGivenID() throws Exception {

        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/absences").param("year", "2016").param("person", "23"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidTypeParameter() throws Exception {

        when(personServiceMock.getPersonByID(anyInt()))
            .thenReturn(Optional.of(TestDataCreator.createPerson()));

        mockMvc.perform(get("/api/absences").param("year", "2016").param("person", "23").param("type", "FOO"))
            .andExpect(status().isBadRequest());
    }
}
