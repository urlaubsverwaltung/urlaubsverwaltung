package org.synyx.urlaubsverwaltung.statistics.vacationoverview.web;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveVacationOverviewViewControllerTest {

    private ApplicationForLeaveVacationOverviewViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveVacationOverviewViewController(personService, departmentService, applicationService, sickNoteService, messageSource);
    }

    @Test
    void applicationForLeaveVacationOverviewNoPermissions() throws Exception {

        final var person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(singletonList(department));

        final var resultActions = perform(get("/web/application/vacationoverview"));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("departments", hasItem(department)))
            .andExpect(view().name("application/vacation_overview"));

        verifyNoMoreInteractions(departmentService);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = { "BOSS", "OFFICE" })
    void applicationForLeaveVacationOverviewAllDepartments(Role role) throws Exception {

        final var person = new Person();
        person.setPermissions(singletonList(role));
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(singletonList(department));

        final var resultActions = perform(get("/web/application/vacationoverview"));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("departments", hasItem(department)))
            .andExpect(view().name("application/vacation_overview"));

        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void applicationForLeaveVacationOverviewSECONDSTAGE() throws Exception {

        final Person ssa = new Person();
        ssa.setPermissions(singletonList(SECOND_STAGE_AUTHORITY));
        when(personService.getSignedInUser()).thenReturn(ssa);

        final var department = department();
        when(departmentService.getAllowedDepartmentsOfPerson(ssa)).thenReturn(singletonList(department));

        final var resultActions = perform(get("/web/application/vacationoverview"));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("departments", hasItem(department)))
            .andExpect(view().name("application/vacation_overview"));

        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void applicationForLeaveVacationOverviewDEPARTMENTHEAD() throws Exception {

        final Person departmentHead = new Person();
        departmentHead.setPermissions(singletonList(DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(departmentHead);

        final var department = department();
        when(departmentService.getAllowedDepartmentsOfPerson(departmentHead)).thenReturn(singletonList(department));

        final ResultActions resultActions = perform(get("/web/application/vacationoverview"));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("departments", hasItem(department)))
            .andExpect(view().name("application/vacation_overview"));

        verifyNoMoreInteractions(departmentService);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void ensureDefaultSelectedDepartmentIsTheFirstAvailable(String departmentName) throws Exception {

        final var person = new Person();
        person.setPermissions(singletonList(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final var superheroes = department("superheroes");
        final var villains = department("villains");
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(List.of(superheroes, villains));

        final var resultActions = perform(get("/web/application/vacationoverview")
            .param("department", departmentName));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("departments", allOf(hasItem(superheroes), hasItem(villains))))
            .andExpect(model().attribute("selectedDepartment", "superheroes"));
    }

    @Test
    void ensureSelectedDepartment() throws Exception {

        final var person = new Person();
        person.setPermissions(singletonList(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final var superheroes = department("superheroes");
        final var villains = department("villains");
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(List.of(superheroes, villains));

        final var resultActions = perform(get("/web/application/vacationoverview")
            .param("department", "villains"));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("departments", allOf(hasItem(superheroes), hasItem(villains))))
            .andExpect(model().attribute("selectedDepartment", "villains"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {""})
    void ensureDefaultSelectedYearIsTheCurrentYear(String givenYearParam) throws Exception {

        final var expectedCurrentYear = LocalDate.now().getYear();

        final var person = new Person();
        person.setPermissions(singletonList(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(List.of(department));

        final var resultActions = perform(get("/web/application/vacationoverview")
            .param("year", givenYearParam));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("currentYear", expectedCurrentYear))
            .andExpect(model().attribute("selectedYear", expectedCurrentYear));
    }

    @Test
    void ensureSelectedYear() throws Exception {

        final var expectedCurrentYear = LocalDate.now().getYear();
        final var expectedSelectedYear = expectedCurrentYear - 1;

        final var person = new Person();
        person.setPermissions(singletonList(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(List.of(department));

        final var resultActions = perform(get("/web/application/vacationoverview")
            .param("year", String.valueOf(expectedSelectedYear)));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("currentYear", expectedCurrentYear))
            .andExpect(model().attribute("selectedYear", expectedSelectedYear));
    }

    @Test
    void ensureSelectedMonthIsTheCurrentMonthWhenParamIsNotDefined() throws Exception {

        final var now = LocalDate.now();

        final var person = new Person();
        person.setPermissions(singletonList(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(List.of(department));

        final var resultActions = perform(get("/web/application/vacationoverview"));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedMonth", String.valueOf(now.getMonthValue())));
    }

    @Test
    void ensureSelectedMonthIsEmptyStringWhenParamIsDefinedAsEmptyString() throws Exception {

        final var person = new Person();
        person.setPermissions(singletonList(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(List.of(department));

        final var resultActions = perform(get("/web/application/vacationoverview")
            .param("month", ""));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedMonth", ""));
    }

    @Test
    void ensureSelectedMonthWhenParamIsDefined() throws Exception {

        final var person = new Person();
        person.setPermissions(singletonList(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(List.of(department));

        final var resultActions = perform(get("/web/application/vacationoverview")
            .param("month", "1"));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedMonth", "1"));
    }

    @Test
    void ensureMonthDayTextIsPadded() throws Exception {
        final var person = new Person();
        person.setFirstName("boss");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(singletonList(department));

        final var resultActions = perform(get("/web/application/vacationoverview"));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("days", hasItems(
                        allOf(hasProperty("dayOfMonth", is("01"))),
                        allOf(hasProperty("dayOfMonth", is("02"))),
                        allOf(hasProperty("dayOfMonth", is("03"))),
                        allOf(hasProperty("dayOfMonth", is("04"))),
                        allOf(hasProperty("dayOfMonth", is("05"))),
                        allOf(hasProperty("dayOfMonth", is("06"))),
                        allOf(hasProperty("dayOfMonth", is("07"))),
                        allOf(hasProperty("dayOfMonth", is("08"))),
                        allOf(hasProperty("dayOfMonth", is("09"))),
                        allOf(hasProperty("dayOfMonth", is("10")))
                    ))
                ))
            ));
    }

    @Test
    void ensureOverviewForGivenYear() throws Exception {
        final var givenYear = LocalDate.now().getYear() - 1;

        final var person = new Person();
        person.setFirstName("boss");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(singletonList(department));

        final var resultActions = perform(get("/web/application/vacationoverview")
            .param("year", String.valueOf(givenYear))
            .locale(Locale.GERMANY));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedYear", givenYear))
            .andExpect(model().attribute("absenceOverview", hasProperty("months", hasSize(1))));

        final var currentMonth = LocalDate.now().getMonthValue();

        verify(messageSource).getMessage("month." + currentMonth, new Object[]{}, Locale.GERMANY);
        verifyNoMoreInteractions(messageSource);
    }

    @Test
    void ensureOverviewForGivenMonthNovember() throws Exception {
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("awesome month text");

        final var person = new Person();
        person.setFirstName("boss");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(singletonList(department));

        final var resultActions = perform(get("/web/application/vacationoverview")
            .param("month", "11")
            .locale(Locale.GERMANY));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(allOf(
                    hasProperty("nameOfMonth", is("awesome month text")),
                    hasProperty("persons", hasItem(allOf(
                        hasProperty("firstName", is("boss")),
                        hasProperty("days", hasSize(30))
                    ))),
                    hasProperty("days", hasSize(30)))
                ))));

        verify(messageSource).getMessage("month.11", new Object[]{}, Locale.GERMANY);
        verifyNoMoreInteractions(messageSource);
    }

    @Test
    void ensureOverviewForGivenMonthDecember() throws Exception {
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("awesome month text");

        final var person = new Person();
        person.setFirstName("boss");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(singletonList(department));

        final var resultActions = perform(get("/web/application/vacationoverview")
            .param("month", "12")
            .locale(Locale.GERMANY));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(allOf(
                    hasProperty("nameOfMonth", is("awesome month text")),
                    hasProperty("persons", hasItem(allOf(
                        hasProperty("firstName", is("boss")),
                        hasProperty("days", hasSize(31))
                    ))),
                    hasProperty("days", hasSize(31)))
                ))));

        verify(messageSource).getMessage("month.12", new Object[]{}, Locale.GERMANY);
        verifyNoMoreInteractions(messageSource);
    }

    @Test
    void ensureOverviewForGivenYearAndGivenMonth() throws Exception {
        final var now = LocalDate.now();
        final var givenYear = now.getYear() - 1;
        final var givenMonth = now.getMonthValue() - 1;

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("awesome month text");

        final var person = new Person();
        person.setFirstName("boss");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(singletonList(department));

        final var resultActions = perform(get("/web/application/vacationoverview")
            .param("year", String.valueOf(givenYear))
            .param("month", String.valueOf(givenMonth))
            .locale(Locale.GERMANY));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(allOf(
                    hasProperty("nameOfMonth", is("awesome month text")),
                    hasProperty("persons", hasItem(allOf(
                        hasProperty("firstName", is("boss")),
                        hasProperty("days")
                    ))),
                    hasProperty("days"))
                ))));

        verify(messageSource).getMessage("month." + givenMonth, new Object[]{}, Locale.GERMANY);
        verifyNoMoreInteractions(messageSource);
    }

    @Test
    void ensureOverviewForGivenYearAndAllMonths() throws Exception {
        final var now = LocalDate.now();
        final var givenYear = now.getYear() - 1;

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("awesome month text");

        final var person = new Person();
        person.setFirstName("boss");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(singletonList(department));

        final var resultActions = perform(get("/web/application/vacationoverview")
            .param("year", String.valueOf(givenYear))
            .param("month", "")
            .locale(Locale.GERMANY));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview", hasProperty("months", hasSize(12))));

        verify(messageSource, times(12)).getMessage(anyString(), any(), eq(Locale.GERMANY));
        verifyNoMoreInteractions(messageSource);
    }

    void ensureOverviewForGivenDepartment() {
        // TODO implement me
    }

    @Test
    void ensureOverviewIsEmptyWhenThereAreNoDepartmentsForADepartmentHead() throws Exception {
        final var person = new Person();
        person.setFirstName("department head");
        person.setPermissions(singletonList(SECOND_STAGE_AUTHORITY));
        when(personService.getSignedInUser()).thenReturn(person);

        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(emptyList());

        final var resultActions = perform(get("/web/application/vacationoverview").locale(Locale.GERMANY));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", hasItem(allOf(
                    hasProperty("persons", empty())
                )))));
    }

    @Test
    void ensureOverviewDefault() throws Exception {
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("awesome month text");

        final var person = new Person();
        person.setFirstName("boss");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(singletonList(department));

        final var resultActions = perform(get("/web/application/vacationoverview").locale(Locale.GERMANY));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(allOf(
                    hasProperty("nameOfMonth", is("awesome month text")),
                    hasProperty("persons", hasItem(allOf(
                        hasProperty("firstName", is("boss")),
                        hasProperty("days")
                    ))),
                    hasProperty("days"))
                ))));

        final var currentMonth = LocalDate.now().getMonthValue();

        verify(messageSource).getMessage("month." + currentMonth, new Object[]{}, Locale.GERMANY);
        verifyNoMoreInteractions(messageSource);
    }

    @Test
    void ensureOverviewPersonsAreSortedByFirstName() throws Exception{
        final var person = new Person();
        person.setFirstName("boss");
        when(personService.getSignedInUser()).thenReturn(person);

        final var personTwo = new Person();
        personTwo.setFirstName("aa");

        final var personThree = new Person();
        personThree.setFirstName("AA");

        final var department = department();
        department.setMembers(List.of(person, personTwo, personThree));
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(singletonList(department));

        final var resultActions = perform(get("/web/application/vacationoverview").locale(Locale.GERMANY));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", hasItem(allOf(
                    hasProperty("persons", contains(
                        hasProperty("firstName", is("AA")),
                        hasProperty("firstName", is("aa")),
                        hasProperty("firstName", is("boss"))
                    ))
                )))));
    }

    private static Department department() {
        return department("superheroes");
    }

    private static Department department(String name) {
        var department = new Department();

        department.setName(name);

        return department;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
