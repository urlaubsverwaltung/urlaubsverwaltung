package org.synyx.urlaubsverwaltung.vacations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceExceptionHandler;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.of;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.json.JsonCompareMode.STRICT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

@ExtendWith(MockitoExtension.class)
class VacationApiControllerTest {

    private VacationApiController sut;

    @Mock
    private PersonService personService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        sut = new VacationApiController(personService, applicationService, departmentService);
    }

    @Test
    void getVacations() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Application allowedVacation = createApplication(person,
            LocalDate.of(2016, 5, 19), LocalDate.of(2016, 5, 20), FULL, new StaticMessageSource());
        allowedVacation.setStatus(ALLOWED);

        final Application cancellationRequestedVacation = createApplication(person,
            LocalDate.of(2016, 4, 5), LocalDate.of(2016, 4, 10), FULL, new StaticMessageSource());
        cancellationRequestedVacation.setStatus(ALLOWED_CANCELLATION_REQUESTED);

        when(applicationService.getForStatesAndPerson(ApplicationStatus.activeStatuses(), List.of(person), LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31)))
            .thenReturn(List.of(allowedVacation, cancellationRequestedVacation));

        when(personService.getPersonByID(23L)).thenReturn(Optional.of(person));

        perform(get("/api/persons/23/vacations")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.vacations", hasSize(2)))
            .andExpect(jsonPath("$.vacations.[0].from", is("2016-05-19")))
            .andExpect(jsonPath("$.vacations.[0].to", is("2016-05-20")))
            .andExpect(jsonPath("$.vacations.[0].person.firstName", is("Marlene")))
            .andExpect(jsonPath("$.vacations.[1].from", is("2016-04-05")))
            .andExpect(jsonPath("$.vacations.[1].to", is("2016-04-10")))
            .andExpect(jsonPath("$.vacations.[1].person.firstName", is("Marlene")));
    }

    @Test
    void getVacationsNoPersonFound() throws Exception {

        when(personService.getPersonByID(23L)).thenReturn(Optional.empty());

        perform(get("/api/persons/23/vacations")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }


    @Test
    void getVacationsStartDateAfterEndDate() throws Exception {

        perform(get("/api/persons/23/vacations")
            .param("from", "2016-06-01")
            .param("to", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getVacationsMissingEndDate() throws Exception {

        perform(get("/api/persons/23/vacations")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getVacationsWrongStartDate() throws Exception {

        perform(get("/api/persons/23/vacations")
            .param("from", "foo")
            .param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getVacationsNoEndDate() throws Exception {

        perform(get("/api/persons/23/vacations")
            .param("from", "2016-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getVacationsWrongEndDate() throws Exception {

        perform(get("/api/persons/23/vacations")
            .param("from", "2016-01-01")
            .param("to", "foo"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getVacationsWrongPerson() throws Exception {

        perform(get("/api/persons/foo/vacations")
            .param("from", "2016-01-01")
            .param("to", "2016-02-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureToRetrieveApplicationsFromColleagues() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(personService.getPersonByID(23L)).thenReturn(Optional.of(person));

        final Application vacationAllowed = createApplication(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            of(2016, 5, 19), of(2016, 5, 20), FULL, new StaticMessageSource());
        vacationAllowed.setStatus(ALLOWED);
        final Application vacationAllowedCancelRequested = createApplication(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            of(2016, 5, 19), of(2016, 5, 20), FULL, new StaticMessageSource());
        vacationAllowed.setStatus(ALLOWED_CANCELLATION_REQUESTED);

        when(departmentService.getApplicationsFromColleaguesOf(person, LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31)))
            .thenReturn(List.of(vacationAllowedCancelRequested, vacationAllowed));

        perform(get("/api/persons/23/vacations")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31")
            .param("ofDepartmentMembers", "true"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;"))
            .andExpect(content().json("""
                {
                  "vacations": [
                    {
                      "from": "2016-05-19",
                      "to": "2016-05-20",
                      "dayLength": 1,
                      "person": {
                        "id": null,
                        "email": "muster@example.org",
                        "firstName": "Marlene",
                        "lastName": "Muster",
                        "niceName": "Marlene Muster",
                        "active": true,
                        "links": [
                          {
                            "rel": "self",
                            "href": "http://localhost/api/persons/{personId}"
                          },
                          {
                            "rel": "absences",
                            "href": "http://localhost/api/persons/{personId}/absences?from={from}&to={to}&absence-types=vacation&absence-types=sick_note&absence-types=public_holiday&absence-types=no_workday"
                          },
                          {
                            "rel": "sicknotes",
                            "href": "http://localhost/api/persons/{personId}/sicknotes?from={from}&to={to}"
                          },
                          {
                            "rel": "vacations",
                            "href": "http://localhost/api/persons/{personId}/vacations?from={from}&to={to}&status=waiting&status=temporary_allowed&status=allowed&status=allowed_cancellation_requested"
                          },
                          {
                            "rel": "workdays",
                            "href": "http://localhost/api/persons/{personId}/workdays?from={from}&to={to}{&length}"
                          }
                        ]
                      },
                      "type": "HOLIDAY",
                      "status": "WAITING",
                      "links": []
                    },
                    {
                      "from": "2016-05-19",
                      "to": "2016-05-20",
                      "dayLength": 1,
                      "person": {
                        "id": null,
                        "email": "muster@example.org",
                        "firstName": "Marlene",
                        "lastName": "Muster",
                        "niceName": "Marlene Muster",
                        "active": true,
                        "links": [
                          {
                            "rel": "self",
                            "href": "http://localhost/api/persons/{personId}"
                          },
                          {
                            "rel": "absences",
                            "href": "http://localhost/api/persons/{personId}/absences?from={from}&to={to}&absence-types=vacation&absence-types=sick_note&absence-types=public_holiday&absence-types=no_workday"
                          },
                          {
                            "rel": "sicknotes",
                            "href": "http://localhost/api/persons/{personId}/sicknotes?from={from}&to={to}"
                          },
                          {
                            "rel": "vacations",
                            "href": "http://localhost/api/persons/{personId}/vacations?from={from}&to={to}&status=waiting&status=temporary_allowed&status=allowed&status=allowed_cancellation_requested"
                          },
                          {
                            "rel": "workdays",
                            "href": "http://localhost/api/persons/{personId}/workdays?from={from}&to={to}{&length}"
                          }
                        ]
                      },
                      "type": "HOLIDAY",
                      "status": "ALLOWED_CANCELLATION_REQUESTED",
                      "links": []
                    }
                  ],
                  "links": []
                }
                """, STRICT));
    }

    @Test
    void getVacationsOfOthersOrDepartmentColleaguesNoPersonFound() throws Exception {

        when(personService.getPersonByID(23L)).thenReturn(Optional.empty());

        perform(get("/api/persons/23/vacations")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31")
            .param("ofDepartmentMembers", "true"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getVacationsOfOthersOrDepartmentColleaguesStartDateAfterEndDate() throws Exception {

        perform(get("/api/persons/23/vacations")
            .param("from", "2016-06-01")
            .param("to", "2016-01-31")
            .param("ofDepartmentMembers", "true"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getVacationsOfOthersOrDepartmentColleaguesMissingEndDate() throws Exception {

        perform(get("/api/persons/23/vacations")
            .param("to", "2016-12-31")
            .param("ofDepartmentMembers", "true"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getVacationsOfOthersOrDepartmentColleaguesWrongStartDate() throws Exception {

        perform(get("/api/persons/23/vacations")
            .param("from", "foo")
            .param("to", "2016-12-31")
            .param("ofDepartmentMembers", "true"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getVacationsOfOthersOrDepartmentColleaguesNoEndDate() throws Exception {

        perform(get("/api/persons/23/vacations")
            .param("from", "2016-01-01")
            .param("ofDepartmentMembers", "true"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getVacationsOfOthersOrDepartmentColleaguesWrongEndDate() throws Exception {

        perform(get("/api/persons/23/vacations")
            .param("from", "2016-01-01")
            .param("to", "foo")
            .param("ofDepartmentMembers", "true"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getVacationsOfOthersOrDepartmentColleaguesWrongPerson() throws Exception {

        perform(get("/api/persons/foo/vacations")
            .param("from", "2016-01-01")
            .param("to", "2016-02-01")
            .param("ofDepartmentMembers", "true"))
            .andExpect(status().isBadRequest());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build().perform(builder);
    }
}
