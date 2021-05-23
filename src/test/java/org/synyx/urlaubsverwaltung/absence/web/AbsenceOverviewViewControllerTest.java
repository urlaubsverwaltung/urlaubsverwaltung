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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.absence.AbsencePeriod;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

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
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

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
    private SettingsService settingsService;
    @Mock
    private AbsenceService absenceService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {

        final Settings settings = new Settings();
        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        settings.setWorkingTimeSettings(workingTimeSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        when(publicHolidaysService.getAbsenceTypeOfDate(any(), any())).thenReturn(DayLength.ZERO);

        sut = new AbsenceOverviewViewController(personService, departmentService, messageSource, clock,
            publicHolidaysService, settingsService, absenceService);
    }

    @Test
    void ensureWithDepartmentsAndNotInDepartmentOnlyMyInformation() throws Exception {

        final var person = new Person();
        person.setId(1);
        person.setFirstName("sam");
        person.setLastName("smith");
        person.setEmail("smith@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(List.of());

        perform(get("/web/absences"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("visibleDepartments", emptyList()))
            .andExpect(model().attribute("selectedDepartments", nullValue()))
            .andExpect(model().attribute("absenceOverview", hasProperty("months", contains(hasProperty("days", hasSize(YearMonth.now(clock).lengthOfMonth()))))))
            .andExpect(model().attribute("absenceOverview", hasProperty("months", contains(hasProperty("persons", hasSize(1))))))
            .andExpect(view().name("absences/absences_overview"));

        verify(personService, never()).getActivePersons();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureWithDepartmentsAndPrivilegedRolesForAllDepartments(Role role) throws Exception {

        final var person = new Person();
        person.setPermissions(List.of(role));
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        when(departmentService.getAllDepartments()).thenReturn(List.of(department));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        perform(get("/web/absences"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("visibleDepartments", hasItem(department)))
            .andExpect(model().attribute("selectedDepartments", hasItem(department.getName())))
            .andExpect(view().name("absences/absences_overview"));

        verify(personService, never()).getActivePersons();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "BOSS", "SECOND_STAGE_AUTHORITY", "DEPARTMENT_HEAD"})
    void ensureNoDepartmentsAndNotInDepartmentGetAllActivePersons(Role role) throws Exception {

        final var person = new Person();
        person.setId(1);
        person.setPermissions(List.of(USER, role));
        person.setFirstName("sam");
        person.setLastName("smith");
        person.setEmail("smith@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        when(departmentService.getNumberOfDepartments()).thenReturn(0L);

        final Person activePerson = new Person();
        activePerson.setId(2);
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
            .andExpect(view().name("absences/absences_overview"));
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
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(List.of(department));

        perform(get("/web/absences"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("visibleDepartments", hasItem(department)))
            .andExpect(view().name("absences/absences_overview"));

        verifyNoMoreInteractions(departmentService);
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
        when(departmentService.getAllDepartments()).thenReturn(List.of(superheroes, villains));
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
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final var superheroes = department("superheroes");
        final var villains = department("villains");
        when(departmentService.getAllDepartments()).thenReturn(List.of(superheroes, villains));
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
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final var superheroes = department("superheroes");
        final var villains = department("villains");
        when(departmentService.getAllDepartments()).thenReturn(List.of(superheroes, villains));
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
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        when(departmentService.getAllDepartments()).thenReturn(List.of(department));
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
            publicHolidaysService, settingsService, absenceService);

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
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(List.of(department));

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
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(List.of(department));

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
        final Clock fixedClock = Clock.fixed(Instant.parse("2018-10-17T00:00:00.00Z"), ZoneId.systemDefault());

        sut = new AbsenceOverviewViewController(personService, departmentService, messageSource, clock,
            publicHolidaysService, settingsService, absenceService);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("awesome month text");

        final var person = new Person();
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(List.of(department));

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
        final var lex = person("lex");
        final var harley = person("harley");
        final var villainsDepartment = department("villains");
        villainsDepartment.setMembers(List.of(joker, lex, harley));

        when(departmentService.getNumberOfDepartments()).thenReturn(2L);
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(List.of(heroDepartment, villainsDepartment));

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
        person.setFirstName("bruce");
        person.setLastName("wayne");
        person.setEmail("batman@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var heroDepartment = department("heroes");
        heroDepartment.setMembers(List.of(person));

        final var joker = person("joker");
        final var lex = person("lex");
        final var harley = person("harley");
        final var villainsDepartment = department("villains");
        villainsDepartment.setMembers(List.of(joker, lex, harley, person));

        when(departmentService.getNumberOfDepartments()).thenReturn(2L);
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(List.of(heroDepartment, villainsDepartment));

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
            publicHolidaysService, settingsService, absenceService);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("awesome month text");

        final var person = new Person();
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(List.of(department));

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
        person.setFirstName("boss");
        person.setLastName("the hoss");
        person.setEmail("boss@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var personTwo = new Person();
        personTwo.setFirstName("aa");
        personTwo.setLastName("aa lastname");
        personTwo.setEmail("person2@example.org");

        final var personThree = new Person();
        personThree.setFirstName("AA");
        personThree.setLastName("AA lastname");
        personThree.setEmail("person3@example.org");

        final var department = department();
        department.setMembers(List.of(person, personTwo, personThree));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);
        when(departmentService.getAllowedDepartmentsOfPerson(person)).thenReturn(List.of(department));

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

    private static Stream<Arguments> dayLengthSickNoteTypeData() {
        return Stream.of(
            Arguments.of(BOSS, new AbsencePeriod.RecordMorningSick(1), new AbsencePeriod.RecordNoonSick(1), "sickNoteFull"),
            Arguments.of(BOSS, new AbsencePeriod.RecordMorningSick(1), null, "sickNoteMorning"),
            Arguments.of(BOSS, null, new AbsencePeriod.RecordNoonSick(1), "sickNoteNoon"),
            Arguments.of(USER, new AbsencePeriod.RecordMorningSick(1), new AbsencePeriod.RecordNoonSick(1), "absenceFull"),
            Arguments.of(USER, new AbsencePeriod.RecordMorningSick(1), null, "absenceMorning"),
            Arguments.of(USER, null, new AbsencePeriod.RecordNoonSick(1), "absenceNoon")
        );
    }

    @ParameterizedTest
    @MethodSource("dayLengthSickNoteTypeData")
    void ensureSickNoteOneDay(Role role, AbsencePeriod.RecordMorning morning, AbsencePeriod.RecordNoon noon, String typeProperty) throws Exception {
        final var person = new Person();
        person.setId(1);
        person.setPermissions(List.of(role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, noon);
        final AbsencePeriod absencePeriodSick = new AbsencePeriod(person, List.of(record));

        final LocalDate firstOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
        when(absenceService.getOpenAbsences(List.of(person), firstOfMonth, lastOfMonth)).thenReturn(List.of(absencePeriodSick));

        perform(get("/web/absences").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("absenceOverview",
                hasProperty("months", contains(
                    hasProperty("persons", hasItem(
                        hasProperty("days", hasItems(
                            hasProperty("type",
                                hasProperty(typeProperty, is(true))
                            )
                        ))
                    ))
                ))
            ));
    }

    private static Stream<Arguments> dayLengthVacationTypeData() {
        return Stream.of(
            Arguments.of(BOSS, new AbsencePeriod.RecordMorningVacation(1, AbsencePeriod.AbsenceStatus.ALLOWED), new AbsencePeriod.RecordNoonVacation(1, AbsencePeriod.AbsenceStatus.ALLOWED), "allowedVacationFull"),
            Arguments.of(BOSS, new AbsencePeriod.RecordMorningVacation(1, AbsencePeriod.AbsenceStatus.ALLOWED), null, "allowedVacationMorning"),
            Arguments.of(BOSS, null, new AbsencePeriod.RecordNoonVacation(1, AbsencePeriod.AbsenceStatus.ALLOWED), "allowedVacationNoon"),
            Arguments.of(BOSS, new AbsencePeriod.RecordMorningVacation(1, AbsencePeriod.AbsenceStatus.WAITING), new AbsencePeriod.RecordNoonVacation(1, AbsencePeriod.AbsenceStatus.WAITING), "waitingVacationFull"),
            Arguments.of(BOSS, new AbsencePeriod.RecordMorningVacation(1, AbsencePeriod.AbsenceStatus.WAITING), null, "waitingVacationMorning"),
            Arguments.of(BOSS, null, new AbsencePeriod.RecordNoonVacation(1, AbsencePeriod.AbsenceStatus.WAITING), "waitingVacationNoon"),
            Arguments.of(USER, new AbsencePeriod.RecordMorningVacation(1, AbsencePeriod.AbsenceStatus.ALLOWED), new AbsencePeriod.RecordNoonVacation(1, AbsencePeriod.AbsenceStatus.ALLOWED), "absenceFull"),
            Arguments.of(USER, new AbsencePeriod.RecordMorningVacation(1, AbsencePeriod.AbsenceStatus.ALLOWED), null, "absenceMorning"),
            Arguments.of(USER, null, new AbsencePeriod.RecordNoonVacation(1, AbsencePeriod.AbsenceStatus.ALLOWED), "absenceNoon"),
            Arguments.of(USER, new AbsencePeriod.RecordMorningVacation(1, AbsencePeriod.AbsenceStatus.WAITING), new AbsencePeriod.RecordNoonVacation(1, AbsencePeriod.AbsenceStatus.WAITING), "absenceFull"),
            Arguments.of(USER, new AbsencePeriod.RecordMorningVacation(1, AbsencePeriod.AbsenceStatus.WAITING), null, "absenceMorning"),
            Arguments.of(USER, null, new AbsencePeriod.RecordNoonVacation(1, AbsencePeriod.AbsenceStatus.WAITING), "absenceNoon")
        );
    }

    @ParameterizedTest
    @MethodSource("dayLengthVacationTypeData")
    void ensureVacationOneDay(Role role, AbsencePeriod.RecordMorningVacation morning, AbsencePeriod.RecordNoonVacation noon, String typeProperty) throws Exception {
        final var person = new Person();
        person.setId(1);
        person.setPermissions(List.of(role));
        person.setFirstName("Bruce");
        person.setLastName("Springfield");
        person.setEmail("springfield@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final var department = department();
        department.setMembers(List.of(person));
        when(departmentService.getNumberOfDepartments()).thenReturn(1L);

        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.now(clock), person, morning, noon);
        final AbsencePeriod absencePeriodVacation = new AbsencePeriod(person, List.of(record));

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
                                hasProperty(typeProperty, is(true))
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
            publicHolidaysService, settingsService, absenceService);

        final var person = new Person();
        person.setId(1);
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
            publicHolidaysService, settingsService, absenceService);

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
