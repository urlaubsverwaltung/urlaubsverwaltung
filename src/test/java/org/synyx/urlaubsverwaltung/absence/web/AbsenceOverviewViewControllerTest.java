package org.synyx.urlaubsverwaltung.absence.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.absence.AbsencePeriod;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHoliday;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OTHER;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.CYAN;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.ORANGE;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_RHEINLAND_PFALZ;

@ExtendWith(MockitoExtension.class)
class AbsenceOverviewViewControllerTest {

    private AbsenceOverviewViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private MessageSource messageSource;
    @Mock
    private PublicHolidaysService publicHolidaysService;
    @Mock
    private AbsenceService absenceService;
    @Mock
    private WorkingTimeService workingTimeService;
    @Mock
    private VacationTypeService vacationTypeService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new AbsenceOverviewViewController(personService, departmentService, messageSource, clock,
            publicHolidaysService, absenceService, workingTimeService, vacationTypeService);
    }

    @Test
    void ensureWithDepartmentsAndNotInDepartmentOnlyMyInformation() throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setFirstName("sam");
        person.setLastName("smith");
        person.setEmail("smith@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of());

        perform(get("/web/absences"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("visibleDepartments", emptyList()))
            .andExpect(model().attribute("selectedDepartments", nullValue()))
            .andExpect(model().attribute("absenceOverview", hasProperty("months", contains(hasProperty("days", hasSize(YearMonth.now(clock).lengthOfMonth()))))))
            .andExpect(model().attribute("absenceOverview", hasProperty("months", contains(hasProperty("persons", hasSize(1))))))
            .andExpect(view().name("absences/absences-overview"));

        verify(personService, never()).getActivePersons();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureWithDepartmentsAndPrivilegedRolesForAllDepartments(Role role) throws Exception {

        final var person = new Person();
        person.setPermissions(List.of(USER, role));
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of(department));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        perform(get("/web/absences"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("visibleDepartments", hasItem(department)))
            .andExpect(model().attribute("selectedDepartments", hasItem(department.getName())))
            .andExpect(view().name("absences/absences-overview"));

        verify(personService).getActivePersons();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "BOSS", "SECOND_STAGE_AUTHORITY", "DEPARTMENT_HEAD"})
    void ensureNoDepartmentsAndNotInDepartmentGetAllActivePersons(Role role) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("sam");
        person.setLastName("smith");
        person.setEmail("smith@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        when(departmentService.getNumberOfDepartments()).thenReturn(0L);

        final Person activePerson = new Person();
        activePerson.setId(2L);
        activePerson.setFirstName("sandra");
        activePerson.setLastName("smith");
        activePerson.setEmail("sandra@example.org");
        when(personService.getActivePersons()).thenReturn(List.of(activePerson));

        perform(get("/web/absences"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("visibleDepartments", nullValue()))
            .andExpect(model().attribute("selectedDepartments", nullValue()))
            .andExpect(model().attribute("absenceOverview", hasProperty("months", contains(hasProperty("days", hasSize(YearMonth.now(clock).lengthOfMonth()))))))
            .andExpect(model().attribute("absenceOverview", hasProperty("months", contains(hasProperty("persons", contains(hasProperty("firstName", is("sandra"))))))))
            .andExpect(view().name("absences/absences-overview"));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
    void ensureWithDepartmentsAndInDepartmentAndFilterInactivePersonsAsBossOrOffice(Role role) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("sam");
        person.setLastName("smith");
        person.setEmail("smith@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final Person activePerson = new Person();
        activePerson.setId(2L);
        activePerson.setPermissions(List.of(USER));
        activePerson.setFirstName("sandra");
        activePerson.setLastName("smith");
        activePerson.setEmail("sandra@example.org");

        final Person inactivePerson = new Person();
        inactivePerson.setId(2L);
        inactivePerson.setPermissions(List.of(INACTIVE));
        inactivePerson.setFirstName("sandra");
        inactivePerson.setLastName("smith");
        inactivePerson.setEmail("sandra@example.org");

        final var department = department();
        department.setMembers(List.of(activePerson, inactivePerson));

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of(department));

        perform(get("/web/absences"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview", hasProperty("months", contains(hasProperty("days", hasSize(YearMonth.now(clock).lengthOfMonth()))))))
            .andExpect(model().attribute("absenceOverview", hasProperty("months", contains(hasProperty("persons", hasSize(1))))))
            .andExpect(model().attribute("absenceOverview", hasProperty("months", contains(hasProperty("persons", contains(hasProperty("firstName", is("sandra"))))))))
            .andExpect(view().name("absences/absences-overview"));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"SECOND_STAGE_AUTHORITY", "DEPARTMENT_HEAD"})
    void ensureWithDepartmentsAndInDepartmentAndFilterInactivePersonsAsDHOrSSA(Role role) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("sam");
        person.setLastName("smith");
        person.setEmail("smith@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final Person activePerson = new Person();
        activePerson.setId(2L);
        activePerson.setPermissions(List.of(USER));
        activePerson.setFirstName("sandra");
        activePerson.setLastName("smith");
        activePerson.setEmail("sandra@example.org");

        final Person inactivePerson = new Person();
        inactivePerson.setId(2L);
        inactivePerson.setPermissions(List.of(INACTIVE));
        inactivePerson.setFirstName("sandra");
        inactivePerson.setLastName("smith");
        inactivePerson.setEmail("sandra@example.org");

        final var department = department();
        department.setMembers(List.of(activePerson, inactivePerson));

        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of(department));

        perform(get("/web/absences"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview", hasProperty("months", contains(hasProperty("days", hasSize(YearMonth.now(clock).lengthOfMonth()))))))
            .andExpect(model().attribute("absenceOverview", hasProperty("months", contains(hasProperty("persons", hasSize(1))))))
            .andExpect(model().attribute("absenceOverview", hasProperty("months", contains(hasProperty("persons", contains(hasProperty("firstName", is("sandra"))))))))
            .andExpect(view().name("absences/absences-overview"));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"SECOND_STAGE_AUTHORITY", "DEPARTMENT_HEAD"})
    void ensureAbsenceOverviewForDepartmentHeadAndSecondStageAuthority(Role role) throws Exception {

        final Person person = new Person();
        person.setFirstName("firstname");
        person.setLastName("lastname");
        person.setEmail("firstname.lastname@example.org");
        person.setPermissions(List.of(USER, role));
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of(department));

        perform(get("/web/absences"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("visibleDepartments", hasItem(department)))
            .andExpect(view().name("absences/absences-overview"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void ensureDefaultSelectedDepartmentIsTheFirstAvailable(String departmentName) throws Exception {

        final var person = new Person();
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final var superheroes = department("superheroes");
        final var villains = department("villains");
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of(superheroes, villains));
        when(departmentService.getNumberOfDepartments()).thenReturn(2L);

        perform(get("/web/absences")
            .param("department", departmentName))
            .andExpect(status().isOk())
            .andExpect(model().attribute("visibleDepartments", allOf(hasItem(superheroes), hasItem(villains))))
            .andExpect(model().attribute("selectedDepartments", hasItem("superheroes")));
    }

    @Test
    void ensureSelectedDepartment() throws Exception {

        final var person = new Person();
        person.setFirstName("office");
        person.setLastName("user");
        person.setEmail("office@example.org");
        person.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final var superheroes = department("superheroes");
        final var villains = department("villains");
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of(superheroes, villains));
        when(departmentService.getNumberOfDepartments()).thenReturn(2L);

        perform(get("/web/absences")
            .param("department", "villains"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("visibleDepartments", allOf(hasItem(superheroes), hasItem(villains))))
            .andExpect(model().attribute("selectedDepartments", hasItem("villains")));
    }

    @Test
    void ensureMultipleSelectedDepartments() throws Exception {

        final var person = new Person();
        person.setFirstName("office");
        person.setLastName("user");
        person.setEmail("office@example.org");
        person.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final var superheroes = department("superheroes");
        final var villains = department("villains");
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of(superheroes, villains));
        when(departmentService.getNumberOfDepartments()).thenReturn(2L);

        perform(get("/web/absences")
            .param("department", "villains")
            .param("department", "superheroes"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("visibleDepartments", allOf(hasItem(superheroes), hasItem(villains))))
            .andExpect(model().attribute("selectedDepartments", allOf(hasItem("superheroes"), hasItem("villains"))));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {""})
    void ensureDefaultSelectedYearIsTheCurrentYear(String givenYearParam) throws Exception {

        final var expectedCurrentYear = LocalDate.now().getYear();

        final var person = new Person();
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        perform(get("/web/absences")
            .param("year", givenYearParam))
            .andExpect(status().isOk())
            .andExpect(model().attribute("currentYear", expectedCurrentYear))
            .andExpect(model().attribute("selectedYear", expectedCurrentYear));
    }

    @Test
    void ensureSelectedYear() throws Exception {

        final var expectedCurrentYear = LocalDate.now().getYear();
        final var expectedSelectedYear = expectedCurrentYear - 1;

        final var person = new Person();
        person.setFirstName("office");
        person.setLastName("user");
        person.setEmail("boss@example.org");
        person.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of(department));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        perform(get("/web/absences")
            .param("year", String.valueOf(expectedSelectedYear)))
            .andExpect(status().isOk())
            .andExpect(model().attribute("currentYear", expectedCurrentYear))
            .andExpect(model().attribute("selectedYear", expectedSelectedYear));
    }

    @Test
    void ensureSelectedMonthIsTheCurrentMonthWhenParamIsNotDefined() throws Exception {

        final var now = LocalDate.now();

        final var person = new Person();
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        when(departmentService.getNumberOfDepartments()).thenReturn(0L);

        perform(get("/web/absences"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedMonth", String.valueOf(now.getMonthValue())));
    }

    @Test
    void ensureSelectedMonthIsEmptyStringWhenParamIsDefinedAsEmptyString() throws Exception {

        final var person = new Person();
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        when(departmentService.getNumberOfDepartments()).thenReturn(0L);

        perform(get("/web/absences")
            .param("month", ""))
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedMonth", ""));
    }

    @Test
    void ensureSelectedMonthWhenParamIsDefined() throws Exception {

        final var person = new Person();
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getNumberOfDepartments()).thenReturn(0L);

        perform(get("/web/absences")
            .param("month", "1"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedMonth", "1"));
    }

    @Test
    void ensureMonthDayTextIsPadded() throws Exception {

        final var person = new Person();
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        when(departmentService.getNumberOfDepartments()).thenReturn(0L);

        perform(get("/web/absences"))
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

        final Clock fixedClock = Clock.fixed(Instant.parse("2018-10-17T00:00:00.00Z"), ZoneId.systemDefault());

        sut = new AbsenceOverviewViewController(personService, departmentService, messageSource, fixedClock,
            publicHolidaysService, absenceService, workingTimeService, vacationTypeService);

        final var person = new Person();
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        when(departmentService.getNumberOfDepartments()).thenReturn(0L);

        perform(get("/web/absences")
            .param("year", "2018")
            .locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedYear", 2018))
            .andExpect(model().attribute("absenceOverview", hasProperty("months", hasSize(1))));

        verify(messageSource).getMessage("month.october", new Object[]{}, Locale.GERMANY);
        verifyNoMoreInteractions(messageSource);
    }

    @Test
    void ensureOverviewForGivenMonthNovember() throws Exception {

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("awesome month text");

        final var person = new Person();
        person.setId(1L);
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of(department));

        perform(get("/web/absences")
            .param("month", "11")
            .locale(Locale.GERMANY))
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

        verify(messageSource).getMessage("month.november", new Object[]{}, Locale.GERMANY);
        verifyNoMoreInteractions(messageSource);
    }

    @Test
    void ensureOverviewForGivenMonthDecember() throws Exception {

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("awesome month text");

        final var person = new Person();
        person.setId(1L);
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of(department));

        perform(get("/web/absences")
            .param("month", "12")
            .locale(Locale.GERMANY))
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

        verify(messageSource).getMessage("month.december", new Object[]{}, Locale.GERMANY);
        verifyNoMoreInteractions(messageSource);
    }

    @Test
    void ensureOverviewForGivenYearAndGivenMonth() throws Exception {

        sut = new AbsenceOverviewViewController(personService, departmentService, messageSource, clock,
            publicHolidaysService, absenceService, workingTimeService, vacationTypeService);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("awesome month text");

        final var person = new Person();
        person.setId(13L);
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of(department));

        perform(get("/web/absences")
            .param("year", "2018")
            .param("month", "10")
            .locale(Locale.GERMANY))
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

        verify(messageSource).getMessage("month.october", new Object[]{}, Locale.GERMANY);
        verifyNoMoreInteractions(messageSource);
    }

    @Test
    void ensureOverviewForGivenDepartment() throws Exception {

        final var person = new Person();
        person.setFirstName("bruce");
        person.setLastName("wayne");
        person.setEmail("batman@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var heroDepartment = department("heroes");
        heroDepartment.setMembers(List.of(person));

        final var joker = person("joker");
        joker.setId(1L);
        final var lex = person("lex");
        lex.setId(2L);
        final var harley = person("harley");
        harley.setId(3L);
        final var villainsDepartment = department("villains");
        villainsDepartment.setMembers(List.of(joker, lex, harley));

        when(departmentService.getNumberOfDepartments()).thenReturn(2L);
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of(heroDepartment, villainsDepartment));

        perform(get("/web/absences").locale(Locale.GERMANY)
            .param("department", "villains"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedDepartments", hasItem("villains")))
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", hasItem(allOf(
                    hasProperty("persons", hasSize(3))
                )))));
    }

    @Test
    void ensureDistinctPersonOverviewForGivenDepartments() throws Exception {

        final var person = new Person();
        person.setId(5L);
        person.setFirstName("bruce");
        person.setLastName("wayne");
        person.setEmail("batman@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var heroDepartment = department("heroes");
        heroDepartment.setMembers(List.of(person));

        final var joker = person("joker");
        joker.setId(1L);
        final var lex = person("lex");
        lex.setId(2L);
        final var harley = person("harley");
        harley.setId(3L);
        final var villainsDepartment = department("villains");
        villainsDepartment.setMembers(List.of(joker, lex, harley, person));

        when(departmentService.getNumberOfDepartments()).thenReturn(2L);
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of(heroDepartment, villainsDepartment));

        perform(get("/web/absences").locale(Locale.GERMANY)
            .param("department", "villains")
            .param("department", "heroes"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedDepartments", allOf(hasItem("heroes"), hasItem("villains"))))
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", hasItem(allOf(
                    hasProperty("persons", hasSize(4))
                )))));
    }

    @Test
    void ensureOverviewDefaultCurrentYearAndMonth() throws Exception {

        final Clock fixedClock = Clock.fixed(Instant.parse("2020-10-17T00:00:00.00Z"), ZoneId.systemDefault());

        sut = new AbsenceOverviewViewController(personService, departmentService, messageSource, fixedClock,
            publicHolidaysService, absenceService, workingTimeService, vacationTypeService);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("awesome month text");

        final var person = new Person();
        person.setId(1L);
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of(department));

        perform(get("/web/absences").locale(Locale.GERMANY))
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

        verify(messageSource).getMessage("month.october", new Object[]{}, Locale.GERMANY);
        verifyNoMoreInteractions(messageSource);
    }

    @Test
    void ensureOverviewPersonsAreSortedByFirstName() throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var personTwo = new Person();
        personTwo.setId(2L);
        personTwo.setFirstName("aa");
        personTwo.setLastName("aa lastname");
        personTwo.setEmail("person2@example.org");

        final var personThree = new Person();
        personThree.setId(3L);
        personThree.setFirstName("AA");
        personThree.setLastName("AA lastname");
        personThree.setEmail("person3@example.org");

        final var department = department();
        department.setMembers(List.of(person, personTwo, personThree));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of(department));

        perform(get("/web/absences").locale(Locale.GERMANY))
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

    // ---
    // WAITING vacation, privileged user

    private static Stream<Arguments> privilegedRoleAndAbsenceStatusWaitingArguments() {
        return Stream.of(
            Arguments.of(Role.BOSS, AbsencePeriod.AbsenceStatus.WAITING),
            Arguments.of(Role.OFFICE, AbsencePeriod.AbsenceStatus.WAITING)
        );
    }

    @ParameterizedTest
    @MethodSource("privilegedRoleAndAbsenceStatusWaitingArguments")
    void ensureAbsenceMorningWaitingForPrivilegedPerson(Role role, AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(42L).color(ORANGE).category(VacationCategory.HOLIDAY).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, absenceStatus, "HOLIDAY", 42L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, null);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(true)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(ORANGE)),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @MethodSource("privilegedRoleAndAbsenceStatusWaitingArguments")
    void ensureAbsenceNoonWaitingForPrivilegedPerson(Role role, AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(ORANGE).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, null, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(true)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", is(ORANGE)),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @MethodSource("privilegedRoleAndAbsenceStatusWaitingArguments")
    void ensureAbsenceFullWaitingForPrivilegedPerson(Role role, AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(ORANGE).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(true)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", is(ORANGE))
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }
    // waiting vacation, not privileged user

    // ---
    // TEMPORARY_ALLOWED vacation, privileged user
    private static Stream<Arguments> privilegedRoleAndAbsenceStatusTemporaryAllowedArguments() {
        return Stream.of(
            Arguments.of(Role.BOSS, AbsencePeriod.AbsenceStatus.TEMPORARY_ALLOWED),
            Arguments.of(Role.OFFICE, AbsencePeriod.AbsenceStatus.TEMPORARY_ALLOWED)
        );
    }

    @ParameterizedTest
    @MethodSource("privilegedRoleAndAbsenceStatusTemporaryAllowedArguments")
    void ensureAbsenceMorningTemporaryAllowedForPrivilegedPerson(Role role, AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(ORANGE).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, null);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(true)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(ORANGE)),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @MethodSource("privilegedRoleAndAbsenceStatusTemporaryAllowedArguments")
    void ensureAbsenceNoonTemporaryAllowedForPrivilegedPerson(Role role, AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(ORANGE).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, null, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(true)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", is(ORANGE)),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @MethodSource("privilegedRoleAndAbsenceStatusTemporaryAllowedArguments")
    void ensureAbsenceFullTemporaryAllowedForPrivilegedPerson(Role role, AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(ORANGE).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(true)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", is(ORANGE))
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }
    //temporary allowed vacation, not privileged user

    @ParameterizedTest
    @EnumSource(value = AbsencePeriod.AbsenceStatus.class, names = {"WAITING"})
    void ensureAbsenceMorningWaitingForNotPrivilegedPerson(AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, VacationCategory.OTHER, "other", false, false, CYAN, false)));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(other, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), other, morning, null);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(true)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(YELLOW)),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = AbsencePeriod.AbsenceStatus.class, names = {"WAITING"})
    void ensureOwnAbsenceMorningWaitingForNotPrivilegedPerson(AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(OTHER).color(CYAN).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, VacationCategory.OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, null);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(true)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(CYAN)),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = AbsencePeriod.AbsenceStatus.class, names = {"WAITING"})
    void ensureAbsenceNoonWaitingForNotPrivilegedPerson(AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(other, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), other, null, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(true)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", is(YELLOW)),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = AbsencePeriod.AbsenceStatus.class, names = {"WAITING"})
    void ensureOwnAbsenceNoonWaitingForNotPrivilegedPerson(AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, null, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(true)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", is(CYAN)),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = AbsencePeriod.AbsenceStatus.class, names = {"WAITING"})
    void ensureAbsenceFullWaitingForNotPrivilegedPerson(AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(other, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(other, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), other, morning, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(true)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", is(YELLOW))
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = AbsencePeriod.AbsenceStatus.class, names = {"WAITING"})
    void ensureOwnAbsenceFullWaitingForNotPrivilegedPerson(AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(true)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", is(CYAN))
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = AbsencePeriod.AbsenceStatus.class, names = {"TEMPORARY_ALLOWED"})
    void ensureAbsenceMorningTemporaryAllowedForNotPrivilegedPerson(AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, "other", false, false, CYAN, false)));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(other, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), other, morning, null);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(true)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(YELLOW)),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = AbsencePeriod.AbsenceStatus.class, names = {"TEMPORARY_ALLOWED"})
    void ensureOwnAbsenceMorningTemporaryAllowedForNotPrivilegedPerson(AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, null);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(true)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(CYAN)),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = AbsencePeriod.AbsenceStatus.class, names = {"TEMPORARY_ALLOWED"})
    void ensureAbsenceNoonTemporaryAllowedForNotPrivilegedPerson(AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(other, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), other, null, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(true)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", is(YELLOW)),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = AbsencePeriod.AbsenceStatus.class, names = {"TEMPORARY_ALLOWED"})
    void ensureOwnAbsenceNoonTemporaryAllowedForNotPrivilegedPerson(AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, null, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(true)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", is(CYAN)),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = AbsencePeriod.AbsenceStatus.class, names = {"TEMPORARY_ALLOWED"})
    void ensureAbsenceFullTemporaryAllowedForNotPrivilegedPerson(AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(other, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(other, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), other, morning, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(true)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", is(YELLOW))
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = AbsencePeriod.AbsenceStatus.class, names = {"TEMPORARY_ALLOWED"})
    void ensureOwnAbsenceFullTemporaryAllowedForNotPrivilegedPerson(AbsencePeriod.AbsenceStatus absenceStatus) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, absenceStatus, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(true)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", is(CYAN))
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    // ---
    // ALLOWED vacation, privileged user

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureAbsenceMorningApprovedForPrivilegedPerson(Role role) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(ORANGE).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, null);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(true)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(ORANGE)),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureAbsenceNoonApprovedForPrivilegedPerson(Role role) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(ORANGE).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, null, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(true)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", is(ORANGE)),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureAbsenceFullWaitingForPrivilegedPerson(Role role) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(ORANGE).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(true)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", is(ORANGE))
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    // allowed vacation, not privileged user

    @Test
    void ensureAbsenceMorningApprovedForNotPrivilegedPerson() throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(other, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), other, morning, null);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(true)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(YELLOW)),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureOwnAbsenceMorningApprovedForNotPrivilegedPerson() throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, null);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(true)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(CYAN)),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureAbsenceNoonApprovedForNotPrivilegedPerson() throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(other, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), other, null, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(true)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", is(YELLOW)),
                                            hasProperty("full", nullValue())
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureOwnAbsenceNoonApprovedForNotPrivilegedPerson() throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, null, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(true)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", is(CYAN)),
                                            hasProperty("full", nullValue())
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureAbsenceFullApprovedForNotPrivilegedPerson() throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(other, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(other, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), other, morning, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(true)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", is(YELLOW))
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureOwnAbsenceFullApprovedForNotPrivilegedPerson() throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(true)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", is(CYAN))
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    // ---
    // ALLOWED CANCELLATION REQUESTED vacation, privileged user

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureAbsenceMorningCancellationRequestedForPrivilegedPerson(Role role) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(ORANGE).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, null);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(true)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(ORANGE)),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureAbsenceNoonCancellationRequestedForPrivilegedPerson(Role role) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(ORANGE).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, null, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(true)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", is(ORANGE)),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureAbsenceFullCancellationRequestedForPrivilegedPerson(Role role) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(ORANGE).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(true)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", is(ORANGE))
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    // allowed cancellation requested vacation, not privileged user
    @Test
    void ensureAbsenceMorningCancellationRequestedForNotPrivilegedPerson() throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(other, 1L, AbsencePeriod.AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), other, morning, null);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(true)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(YELLOW)),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureOwnAbsenceMorningCancellationRequestedForNotPrivilegedPerson() throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, null);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(true)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(CYAN)),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureAbsenceNoonCancellationRequestedForNotPrivilegedPerson() throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(other, 1L, AbsencePeriod.AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), other, null, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(true)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", is(YELLOW)),
                                            hasProperty("full", nullValue())
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureOwnAbsenceNoonCancellationRequestedForNotPrivilegedPerson() throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, null, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(true)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", is(CYAN)),
                                            hasProperty("full", nullValue())
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureAbsenceFullCancellationRequestedForNotPrivilegedPerson() throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(other, 1L, AbsencePeriod.AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(other, 1L, AbsencePeriod.AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), other, morning, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(true)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", is(YELLOW))
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureOwnAbsenceFullCancellationRequestedForNotPrivilegedPerson() throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build()));
//        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(new VacationType(1L, true, OTHER, null, false, false, CYAN, false)));

        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(true)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", is(CYAN))
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    // ---
    // sick note, privileged user

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureSickNoteMorningForPrivilegedPerson(Role role) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(person, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, null);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureSickNoteNoonForPrivilegedPerson(Role role) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(person, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, null, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(true)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureSickNoteFullForPrivilegedPerson(Role role) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(person, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(person, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(true)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    // sick note, not privileged user

    @Test
    void ensureSickNoteMorningForNotPrivilegedPerson() throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(other, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), other, morning, null);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(true)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(YELLOW)),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureOwnSickNoteMorningForNotPrivilegedPerson() throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(person, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, null);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(true)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureSickNoteNoonForNotPrivilegedPerson() throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(other, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), other, null, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(true)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", is(YELLOW)),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureOwnSickNoteNoonForNotPrivilegedPerson() throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(person, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, null, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(true)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureSickNoteFullForNotPrivilegedPerson() throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(other, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(other, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), other, morning, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(true)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", is(YELLOW))
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureOwnSickNoteFullForNotPrivilegedPerson() throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(person, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(person, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(true)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    // ---
    // half VACATION, half SICK

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureChristmasEveOnMorningForPrivilegedPerson(Role role) throws Exception {

        final LocalDate december24 = LocalDate.of(2024, DECEMBER, 24);
        final LocalDate firstOfMonth = LocalDate.of(2024, DECEMBER, 1);
        final LocalDate lastOfMonth = LocalDate.of(2024, DECEMBER, 31);
        final DateRange dateRange = new DateRange(firstOfMonth, lastOfMonth);

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of());

        when(workingTimeService.getFederalStatesByPersonAndDateRange(person, dateRange))
            .thenReturn(Map.of(dateRange, GERMANY_BADEN_WUERTTEMBERG));

        when(publicHolidaysService.getPublicHolidays(firstOfMonth, lastOfMonth, GERMANY_BADEN_WUERTTEMBERG))
            .thenReturn(List.of(new PublicHoliday(december24, MORNING, "Heiligabend")));

        final AbsencePeriod.RecordMorningPublicHoliday christmasEveMorning = new AbsencePeriod.RecordMorningPublicHoliday(person);
        final AbsencePeriod.Record christmasEveRecord = new AbsencePeriod.Record(december24, person, christmasEveMorning, null);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(christmasEveRecord));
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriod));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        final ResultActions perform = perform(
            get("/web/absences").locale(Locale.GERMANY)
                .param("year", "2024")
                .param("month", "12")
        );
        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("publicHolidayMorning", is(true)),
                                    hasProperty("publicHolidayNoon", is(false)),
                                    hasProperty("publicHolidayFull", is(false))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureChristmasEveOnNoonForPrivilegedPerson(Role role) throws Exception {

        final LocalDate december24 = LocalDate.of(2024, DECEMBER, 24);
        final LocalDate firstOfMonth = LocalDate.of(2024, DECEMBER, 1);
        final LocalDate lastOfMonth = LocalDate.of(2024, DECEMBER, 31);
        final DateRange dateRange = new DateRange(firstOfMonth, lastOfMonth);

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of());

        when(workingTimeService.getFederalStatesByPersonAndDateRange(person, dateRange))
            .thenReturn(Map.of(dateRange, GERMANY_BADEN_WUERTTEMBERG));

        when(publicHolidaysService.getPublicHolidays(firstOfMonth, lastOfMonth, GERMANY_BADEN_WUERTTEMBERG))
            .thenReturn(List.of(new PublicHoliday(december24, NOON, "Heiligabend")));

        final AbsencePeriod.RecordNoonPublicHoliday christmasEveNoon = new AbsencePeriod.RecordNoonPublicHoliday(person);
        final AbsencePeriod.Record christmasEveRecord = new AbsencePeriod.Record(december24, person, null, christmasEveNoon);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(christmasEveRecord));

        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriod));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(
            get("/web/absences").locale(Locale.GERMANY)
                .param("year", "2024")
                .param("month", "12")
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("publicHolidayMorning", is(false)),
                                    hasProperty("publicHolidayNoon", is(true)),
                                    hasProperty("publicHolidayFull", is(false))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureAbsenceMorningAndSickNoteNoonForPrivilegedPerson(Role role) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(ORANGE).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final AbsencePeriod.RecordMorningVacation vacationMorning = new AbsencePeriod.RecordMorningVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record vacationRecord = new AbsencePeriod.Record(LocalDate.now(clock), person, vacationMorning, null);

        final AbsencePeriod.RecordNoonSick sickNoteNoon = new AbsencePeriod.RecordNoonSick(person, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record sickNoteRecord = new AbsencePeriod.Record(LocalDate.now(clock), person, null, sickNoteNoon);

        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(vacationRecord, sickNoteRecord));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(true)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(true)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(ORANGE)),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureAbsenceNoonAndSickNoteMorningForPrivilegedPerson(Role role) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(ORANGE).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final AbsencePeriod.RecordNoonVacation vacationNoon = new AbsencePeriod.RecordNoonVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record vacationRecord = new AbsencePeriod.Record(LocalDate.now(clock), person, null, vacationNoon);

        final AbsencePeriod.RecordMorningSick sickNoteMorning = new AbsencePeriod.RecordMorningSick(person, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record sickNoteRecord = new AbsencePeriod.Record(LocalDate.now(clock), person, sickNoteMorning, null);

        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(vacationRecord, sickNoteRecord));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(true)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(true)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", is(ORANGE)),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    // half vacation, sick note, not privileged user

    @Test
    void ensureAbsenceMorningAndSickNoteNoonForNotPrivilegedPerson() throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        final AbsencePeriod.RecordMorningVacation vacationMorning = new AbsencePeriod.RecordMorningVacation(other, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record vacationRecord = new AbsencePeriod.Record(LocalDate.now(clock), other, vacationMorning, null);

        final AbsencePeriod.RecordNoonSick sickNoteNoon = new AbsencePeriod.RecordNoonSick(other, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record sickNoteRecord = new AbsencePeriod.Record(LocalDate.now(clock), other, null, sickNoteNoon);

        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(vacationRecord, sickNoteRecord));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        final ResultActions perform = perform(get("/web/absences").locale(Locale.GERMANY));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(true)),
                                    hasProperty("absenceNoon", is(true)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(YELLOW)),
                                            hasProperty("noon", is(YELLOW)),
                                            hasProperty("full", nullValue())
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureOwnAbsenceMorningAndSickNoteNoonForNotPrivilegedPerson() throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final AbsencePeriod.RecordMorningVacation vacationMorning = new AbsencePeriod.RecordMorningVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record vacationRecord = new AbsencePeriod.Record(LocalDate.now(clock), person, vacationMorning, null);

        final AbsencePeriod.RecordNoonSick sickNoteNoon = new AbsencePeriod.RecordNoonSick(person, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record sickNoteRecord = new AbsencePeriod.Record(LocalDate.now(clock), person, null, sickNoteNoon);

        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(vacationRecord, sickNoteRecord));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        final ResultActions perform = perform(get("/web/absences").locale(Locale.GERMANY));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(true)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(true)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(CYAN)),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureAbsenceNoonAndSickNoteMorningForNotPrivilegedPerson() throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        final AbsencePeriod.RecordNoonVacation vacationNoon = new AbsencePeriod.RecordNoonVacation(other, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record vacationRecord = new AbsencePeriod.Record(LocalDate.now(clock), other, null, vacationNoon);

        final AbsencePeriod.RecordMorningSick sickNoteMorning = new AbsencePeriod.RecordMorningSick(other, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record sickNoteRecord = new AbsencePeriod.Record(LocalDate.now(clock), other, sickNoteMorning, null);

        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(vacationRecord, sickNoteRecord));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        final ResultActions perform = perform(get("/web/absences").locale(Locale.GERMANY));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(true)),
                                    hasProperty("absenceNoon", is(true)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(YELLOW)),
                                            hasProperty("noon", is(YELLOW)),
                                            hasProperty("full", nullValue())
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureOwnAbsenceNoonAndSickNoteMorningForNotPrivilegedPerson() throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final AbsencePeriod.RecordNoonVacation vacationNoon = new AbsencePeriod.RecordNoonVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record vacationRecord = new AbsencePeriod.Record(LocalDate.now(clock), person, null, vacationNoon);

        final AbsencePeriod.RecordMorningSick sickNoteMorning = new AbsencePeriod.RecordMorningSick(person, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record sickNoteRecord = new AbsencePeriod.Record(LocalDate.now(clock), person, sickNoteMorning, null);

        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(vacationRecord, sickNoteRecord));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        final ResultActions perform = perform(get("/web/absences").locale(Locale.GERMANY));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(true)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(true)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", is(CYAN)),
                                            hasProperty("full", nullValue())
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureColoredAbsenceMorningAndOwnSickNoteNoonForNotPrivilegedPerson() throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(
            List.of(
                ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build()
//                new VacationType(1L, true, null, "", false, false, CYAN, true)
            )
        );

        final AbsencePeriod.RecordMorningVacation vacationMorning = new AbsencePeriod.RecordMorningVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, true);
        final AbsencePeriod.Record vacationRecord = new AbsencePeriod.Record(LocalDate.now(clock), person, vacationMorning, null);

        final AbsencePeriod.RecordNoonSick sickNoteNoon = new AbsencePeriod.RecordNoonSick(person, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record sickNoteRecord = new AbsencePeriod.Record(LocalDate.now(clock), person, null, sickNoteNoon);

        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(vacationRecord, sickNoteRecord));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        final ResultActions perform = perform(get("/web/absences").locale(Locale.GERMANY));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(true)),
                                    hasProperty("absenceNoon", is(false)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(true)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(CYAN)),
                                            hasProperty("noon", nullValue()),
                                            hasProperty("full", nullValue())
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureColoredAbsenceNoonAndSickNoteMorningForNotPrivilegedPerson() throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        when(vacationTypeService.getAllVacationTypes()).thenReturn(
            List.of(
                ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build()
//                new VacationType(1L, true, null, "", false, false, CYAN, true)
            )
        );

        final AbsencePeriod.RecordNoonVacation vacationNoon = new AbsencePeriod.RecordNoonVacation(other, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, true);
        final AbsencePeriod.Record vacationRecord = new AbsencePeriod.Record(LocalDate.now(clock), other, null, vacationNoon);

        final AbsencePeriod.RecordMorningSick sickNoteMorning = new AbsencePeriod.RecordMorningSick(other, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record sickNoteRecord = new AbsencePeriod.Record(LocalDate.now(clock), other, sickNoteMorning, null);

        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(vacationRecord, sickNoteRecord));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        final ResultActions perform = perform(get("/web/absences").locale(Locale.GERMANY));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(true)),
                                    hasProperty("absenceNoon", is(true)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(YELLOW)),
                                            hasProperty("noon", is(CYAN)),
                                            hasProperty("full", nullValue())
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureOwnColoredAbsenceNoonAndSickNoteMorningForNotPrivilegedPerson() throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(
            List.of(
                ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(CYAN).build()
//                new VacationType(1L, true, null, "", false, false, CYAN, true)
            )
        );

        final AbsencePeriod.RecordNoonVacation vacationNoon = new AbsencePeriod.RecordNoonVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, true);
        final AbsencePeriod.Record vacationRecord = new AbsencePeriod.Record(LocalDate.now(clock), person, null, vacationNoon);

        final AbsencePeriod.RecordMorningSick sickNoteMorning = new AbsencePeriod.RecordMorningSick(person, 1L, AbsencePeriod.AbsenceStatus.ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record sickNoteRecord = new AbsencePeriod.Record(LocalDate.now(clock), person, sickNoteMorning, null);

        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(vacationRecord, sickNoteRecord));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        final ResultActions perform = perform(get("/web/absences").locale(Locale.GERMANY));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(false)),
                                    hasProperty("absenceNoon", is(true)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(true)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", nullValue()),
                                            hasProperty("noon", is(CYAN)),
                                            hasProperty("full", nullValue())
                                        ))
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureDifferentAbsenceMorningAndNoonForPrivilegedPerson(Role role) throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes())
            .thenReturn(List.of(
                ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(ORANGE).build(),
                ProvidedVacationType.builder(new StaticMessageSource()).id(2L).color(CYAN).build()
//                new VacationType(1L, true, null, "", false, false, ORANGE, false),
//                new VacationType(2L, true, null, "", false, false, CYAN, false)
            ));

        final AbsencePeriod.RecordMorningVacation vacationMorning = new AbsencePeriod.RecordMorningVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record recordOrange = new AbsencePeriod.Record(LocalDate.now(clock), person, vacationMorning, null);

        final AbsencePeriod.RecordNoonVacation vacationNoon = new AbsencePeriod.RecordNoonVacation(person, 2L, AbsencePeriod.AbsenceStatus.ALLOWED, "Familientag", 2L, false);
        final AbsencePeriod.Record recordCyan = new AbsencePeriod.Record(LocalDate.now(clock), person, null, vacationNoon);

        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(recordOrange, recordCyan));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(true)),
                                    hasProperty("absenceNoon", is(true)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(ORANGE)),
                                            hasProperty("noon", is(CYAN)),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureDifferentAbsenceMorningAndNoonForNotPrivilegedPerson() throws Exception {

        final var signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Springfield");
        signedInUser.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final var other = new Person();
        other.setFirstName("Dorie");
        other.setLastName("Fisch");
        other.setEmail("dorie@example.org");
        other.setId(2L);

        final var department = department();
        department.setMembers(List.of(signedInUser, other));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getDepartmentsPersonHasAccessTo(signedInUser)).thenReturn(List.of(department));

        when(vacationTypeService.getAllVacationTypes())
            .thenReturn(List.of(
                ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(ORANGE).build(),
                ProvidedVacationType.builder(new StaticMessageSource()).id(2L).color(CYAN).build()
//                new VacationType(1L, true, null, "", false, false, ORANGE, false),
//                new VacationType(2L, true, null, "", false, false, CYAN, false)
            ));

        final AbsencePeriod.RecordMorningVacation vacationMorning = new AbsencePeriod.RecordMorningVacation(other, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record recordOrange = new AbsencePeriod.Record(LocalDate.now(clock), other, vacationMorning, null);

        final AbsencePeriod.RecordNoonVacation vacationNoon = new AbsencePeriod.RecordNoonVacation(other, 2L, AbsencePeriod.AbsenceStatus.ALLOWED, "Familientag", 2L, false);
        final AbsencePeriod.Record recordCyan = new AbsencePeriod.Record(LocalDate.now(clock), other, null, vacationNoon);

        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(recordOrange, recordCyan));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(signedInUser, other), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(true)),
                                    hasProperty("absenceNoon", is(true)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(YELLOW)),
                                            hasProperty("noon", is(YELLOW)),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureOwnDifferentAbsenceMorningAndNoonForNotPrivilegedPerson() throws Exception {

        final var person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        when(vacationTypeService.getAllVacationTypes())
            .thenReturn(List.of(
                ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(ORANGE).build(),
                ProvidedVacationType.builder(new StaticMessageSource()).id(2L).color(CYAN).build()
//                new VacationType(1L, true, null, "", false, false, ORANGE, false),
//                new VacationType(2L, true, null, "", false, false, CYAN, false)
            ));

        final AbsencePeriod.RecordMorningVacation vacationMorning = new AbsencePeriod.RecordMorningVacation(person, 1L, AbsencePeriod.AbsenceStatus.ALLOWED, "Erholungsurlaub", 1L, false);
        final AbsencePeriod.Record recordOrange = new AbsencePeriod.Record(LocalDate.now(clock), person, vacationMorning, null);

        final AbsencePeriod.RecordNoonVacation vacationNoon = new AbsencePeriod.RecordNoonVacation(person, 2L, AbsencePeriod.AbsenceStatus.ALLOWED, "Familientag", 2L, false);
        final AbsencePeriod.Record recordCyan = new AbsencePeriod.Record(LocalDate.now(clock), person, null, vacationNoon);

        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(List.of(recordOrange, recordCyan));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodVacation));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                allOf(
                                    hasProperty("absenceFull", is(false)),
                                    hasProperty("absenceMorning", is(true)),
                                    hasProperty("absenceNoon", is(true)),
                                    hasProperty("waitingAbsenceFull", is(false)),
                                    hasProperty("waitingAbsenceMorning", is(false)),
                                    hasProperty("waitingAbsenceNoon", is(false)),
                                    hasProperty("temporaryAllowedAbsenceFull", is(false)),
                                    hasProperty("temporaryAllowedAbsenceMorning", is(false)),
                                    hasProperty("temporaryAllowedAbsenceNoon", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceFull", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceMorning", is(false)),
                                    hasProperty("allowedCancellationRequestedAbsenceNoon", is(false)),
                                    hasProperty("waitingSickNoteFull", is(false)),
                                    hasProperty("waitingSickNoteMorning", is(false)),
                                    hasProperty("waitingSickNoteNoon", is(false)),
                                    hasProperty("activeSickNoteFull", is(false)),
                                    hasProperty("activeSickNoteMorning", is(false)),
                                    hasProperty("activeSickNoteNoon", is(false)),
                                    hasProperty("color",
                                        allOf(
                                            hasProperty("morning", is(ORANGE)),
                                            hasProperty("noon", is(CYAN)),
                                            hasProperty("full", nullValue())
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                ))
            ));
    }

    @Test
    void ensureWeekendsAndHolidays() throws Exception {

        final Clock fixedClock = Clock.fixed(Instant.parse("2020-12-01T00:00:00.00Z"), ZoneId.systemDefault());

        sut = new AbsenceOverviewViewController(personService, departmentService, messageSource, fixedClock,
            publicHolidaysService, absenceService, workingTimeService, vacationTypeService);

        final var person = new Person();
        person.setId(1L);
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        when(departmentService.getNumberOfDepartments()).thenReturn(0L);

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("days", contains(
                        allOf(hasProperty("dayOfMonth", is("01")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("02")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("03")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("04")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("05")), hasProperty("weekend", is(true))),
                        allOf(hasProperty("dayOfMonth", is("06")), hasProperty("weekend", is(true))),
                        allOf(hasProperty("dayOfMonth", is("07")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("08")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("09")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("10")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("11")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("12")), hasProperty("weekend", is(true))),
                        allOf(hasProperty("dayOfMonth", is("13")), hasProperty("weekend", is(true))),
                        allOf(hasProperty("dayOfMonth", is("14")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("15")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("16")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("17")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("18")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("19")), hasProperty("weekend", is(true))),
                        allOf(hasProperty("dayOfMonth", is("20")), hasProperty("weekend", is(true))),
                        allOf(hasProperty("dayOfMonth", is("21")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("22")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("23")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("24")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("25")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("26")), hasProperty("weekend", is(true))),
                        allOf(hasProperty("dayOfMonth", is("27")), hasProperty("weekend", is(true))),
                        allOf(hasProperty("dayOfMonth", is("28")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("29")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("30")), hasProperty("weekend", is(false))),
                        allOf(hasProperty("dayOfMonth", is("31")), hasProperty("weekend", is(false)))
                    ))
                ))
            ));
    }

    @Test
    void ensureToday() throws Exception {

        final Clock fixedClock = Clock.fixed(Instant.parse("2020-12-10T00:00:00.00Z"), ZoneId.systemDefault());

        sut = new AbsenceOverviewViewController(personService, departmentService, messageSource, fixedClock,
            publicHolidaysService, absenceService, workingTimeService, vacationTypeService);

        final var person = new Person();
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        when(departmentService.getNumberOfDepartments()).thenReturn(0L);

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("days", contains(
                        allOf(hasProperty("dayOfMonth", is("01")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("02")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("03")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("04")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("05")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("06")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("07")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("08")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("09")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("10")), hasProperty("today", is(true))),
                        allOf(hasProperty("dayOfMonth", is("11")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("12")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("13")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("14")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("15")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("16")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("17")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("18")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("19")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("20")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("21")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("22")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("23")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("24")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("25")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("26")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("27")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("28")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("29")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("30")), hasProperty("today", is(false))),
                        allOf(hasProperty("dayOfMonth", is("31")), hasProperty("today", is(false)))
                    ))
                ))
            ));
    }

    @Test
    void ensurePublicHolidaysBasedOnCustomPublicHolidays() throws Exception {

        final Person personWithCustomPublicHolidays = person("joker");
        personWithCustomPublicHolidays.setId(1L);
        when(personService.getSignedInUser()).thenReturn(personWithCustomPublicHolidays);

        final Person personDefaultPublicHolidays = person("lex");
        personDefaultPublicHolidays.setId(2L);

        when(departmentService.getNumberOfDepartments()).thenReturn(0L);
        when(personService.getActivePersons()).thenReturn(List.of(personWithCustomPublicHolidays, personDefaultPublicHolidays));

        final LocalDate start = LocalDate.of(2022, JANUARY, 1);
        final LocalDate end = LocalDate.of(2022, JANUARY, 31);
        final DateRange dateRange = new DateRange(start, end);
        when(workingTimeService.getFederalStatesByPersonAndDateRange(personWithCustomPublicHolidays, dateRange)).thenReturn(Map.of(dateRange, GERMANY_BADEN_WUERTTEMBERG));
        when(workingTimeService.getFederalStatesByPersonAndDateRange(personDefaultPublicHolidays, dateRange)).thenReturn(Map.of(dateRange, GERMANY_RHEINLAND_PFALZ));

        when(publicHolidaysService.getPublicHolidays(start, end, GERMANY_BADEN_WUERTTEMBERG)).thenReturn(List.of(new PublicHoliday(LocalDate.of(2022, JANUARY, 6), FULL, "")));
        when(publicHolidaysService.getPublicHolidays(start, end, GERMANY_RHEINLAND_PFALZ)).thenReturn(emptyList());

        perform(get("/web/absences")
            .param("year", "2022")
            .param("month", "1")
            .locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItems(
                        allOf(
                            hasProperty("firstName", is("joker")),
                            hasProperty("days", hasItem(
                                allOf(
                                    hasProperty("type",
                                        allOf(
                                            hasProperty("publicHolidayFull", is(true)),
                                            hasProperty("publicHolidayMorning", is(false)),
                                            hasProperty("publicHolidayNoon", is(false))
                                        )
                                    )
                                )
                            ))
                        ),
                        allOf(
                            hasProperty("firstName", is("lex")),
                            hasProperty("days", hasItem(
                                allOf(
                                    hasProperty("type",
                                        allOf(
                                            hasProperty("publicHolidayFull", is(false)),
                                            hasProperty("publicHolidayMorning", is(false)),
                                            hasProperty("publicHolidayNoon", is(false))
                                        )
                                    )
                                )
                            ))
                        )
                    ))
                ))
            ));
    }

    private static Department department() {
        return department("superheroes");
    }

    private static Department department(String name) {
        var department = new Department();

        department.setName(name);

        return department;
    }

    private static Person person(String firstName) {
        var person = new Person();

        person.setFirstName(firstName);
        person.setLastName(firstName + " lastname");
        person.setEmail(firstName + "@example.org");

        return person;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
