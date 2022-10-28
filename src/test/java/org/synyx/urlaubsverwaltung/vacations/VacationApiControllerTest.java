package org.synyx.urlaubsverwaltung.vacations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceExceptionHandler;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.of;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
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
        final Application vacation1 = createApplication(person,
            LocalDate.of(2016, 5, 19), LocalDate.of(2016, 5, 20), DayLength.FULL);
        vacation1.setStatus(ALLOWED);
        final Application vacation2 = createApplication(person,
            LocalDate.of(2016, 4, 5), LocalDate.of(2016, 4, 10), DayLength.FULL);
        vacation2.setStatus(ALLOWED_CANCELLATION_REQUESTED);

        when(applicationService.getApplicationsForACertainPeriodAndPersonAndState(any(LocalDate.class), any(LocalDate.class), eq(person), eq(ALLOWED)))
            .thenReturn(List.of(vacation1));
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndState(any(LocalDate.class), any(LocalDate.class), eq(person), eq(ALLOWED_CANCELLATION_REQUESTED)))
            .thenReturn(List.of(vacation2));

        when(personService.getPersonByID(23)).thenReturn(Optional.of(person));

        perform(get("/api/persons/23/vacations")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;"))
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

        when(personService.getPersonByID(23)).thenReturn(Optional.empty());

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
    void getVacationsOfOthersOrDepartmentColleaguesWithDepartments() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final List<Department> departments = List.of(new Department(), new Department());
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(departments);

        when(personService.getPersonByID(23)).thenReturn(Optional.of(person));

        final Application vacationAllowed = createApplication(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            of(2016, 5, 19), of(2016, 5, 20), FULL);
        vacationAllowed.setStatus(ALLOWED);
        final Application vacationWaiting = createApplication(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            of(2016, 5, 19), of(2016, 5, 20), FULL);
        vacationAllowed.setStatus(WAITING);
        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(eq(person), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(vacationAllowed, vacationWaiting));

        perform(get("/api/persons/23/vacations")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31")
            .param("ofDepartmentMembers", "true"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;"))
            .andExpect(jsonPath("$.vacations", hasSize(2)))
            .andExpect(jsonPath("$.vacations.[0].from", is("2016-05-19")))
            .andExpect(jsonPath("$.vacations.[0].to", is("2016-05-20")))
            .andExpect(jsonPath("$.vacations.[0].person.firstName", is("Marlene")));
    }

    @Test
    void getVacationsOfOthersOrDepartmentColleaguesWithoutDepartments() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());

        when(personService.getPersonByID(23)).thenReturn(Optional.of(person));

        final Application vacationAllowed = createApplication(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            of(2016, 5, 19), of(2016, 5, 20), FULL);
        vacationAllowed.setStatus(ALLOWED);
        when(applicationService.getApplicationsForACertainPeriodAndState(any(LocalDate.class), any(LocalDate.class), eq(ALLOWED)))
            .thenReturn(List.of(vacationAllowed));

        final Application vacationAllowedCancelRequested = createApplication(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            of(2016, 5, 19), of(2016, 5, 20), FULL);
        vacationAllowed.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getApplicationsForACertainPeriodAndState(any(LocalDate.class), any(LocalDate.class), eq(ALLOWED_CANCELLATION_REQUESTED)))
            .thenReturn(List.of(vacationAllowedCancelRequested));

        perform(get("/api/persons/23/vacations")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31")
            .param("ofDepartmentMembers", "true"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;"))
            .andExpect(jsonPath("$.vacations", hasSize(2)))
            .andExpect(jsonPath("$.vacations.[0].from", is("2016-05-19")))
            .andExpect(jsonPath("$.vacations.[0].to", is("2016-05-20")))
            .andExpect(jsonPath("$.vacations.[0].person.firstName", is("Marlene")));
    }

    @Test
    void getVacationsOfOthersOrDepartmentColleaguesNoPersonFound() throws Exception {

        when(personService.getPersonByID(23)).thenReturn(Optional.empty());

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
