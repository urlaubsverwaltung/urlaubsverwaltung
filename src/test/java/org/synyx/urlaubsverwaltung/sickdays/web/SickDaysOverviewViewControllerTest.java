package org.synyx.urlaubsverwaltung.sickdays.web;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

@ExtendWith(MockitoExtension.class)
class SickDaysOverviewViewControllerTest {

    private SickDaysOverviewViewController sut;

    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private PersonBasedataService personBasedataService;
    @Mock
    private WorkDaysCountService workDaysCountService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PersonService personService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new SickDaysOverviewViewController(sickNoteService, personBasedataService, workDaysCountService,
            departmentService, personService, new DateFormatAware(), clock);
    }

    @Test
    void filterSickNotes() throws Exception {

        final int year = Year.now(clock).getValue();
        final LocalDate startDate = LocalDate.parse(year + "-01-01");
        final LocalDate endDate = LocalDate.parse(year + "-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        perform(post("/web/sicknote/filter").flashAttr("period", filterPeriod))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/sicknote?from=" + year + "-01-01&to=" + year + "-12-31"));
    }

    private static Stream<Arguments> dateInputAndIsoDateTuple() {
        return Stream.of(
            Arguments.of("25.03.2022", "2022-03-25"),
            Arguments.of("25.03.22", "2022-03-25"),
            Arguments.of("25.3.2022", "2022-03-25"),
            Arguments.of("25.3.22", "2022-03-25"),
            Arguments.of("1.4.22", "2022-04-01")
        );
    }

    @ParameterizedTest
    @MethodSource("dateInputAndIsoDateTuple")
    void applicationForLeaveStatisticsRedirectsToStatisticsAfterIncorrectPeriodForStartDate(String givenDate, String givenIsoDate) throws Exception {

        perform(post("/web/sicknote/filter")
            .param("startDate", givenDate))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/sicknote?from=" + givenIsoDate + "&to=2022-12-31"));
    }

    @ParameterizedTest
    @MethodSource("dateInputAndIsoDateTuple")
    void applicationForLeaveStatisticsRedirectsToStatisticsForEndDate(String givenDate, String givenIsoDate) throws Exception {

        perform(post("/web/sicknote/filter")
            .param("endDate", givenDate))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/sicknote?from=2022-01-01&to=" + givenIsoDate));
    }

    @Test
    void filterSickNotesWithNullDates() throws Exception {

        final int year = Year.now(clock).getValue();
        final FilterPeriod filterPeriod = new FilterPeriod(null, null);

        perform(post("/web/sicknote/filter")
            .flashAttr("period", filterPeriod))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/sicknote?from=" + year + "-01-01&to=" + year + "-12-31"));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
    void periodsSickNotesWithDateRangeWithRole(Role role) throws Exception {

        final Person office = new Person();
        office.setId(1);
        office.setPermissions(List.of(USER, role));
        when(personService.getSignedInUser()).thenReturn(office);

        final Person person = new Person();
        person.setId(1);
        person.setPermissions(List.of(USER));
        final List<Person> persons = List.of(person);
        when(personService.getActivePersons()).thenReturn(persons);

        final SickNoteType childSickType = new SickNoteType();
        childSickType.setCategory(SICK_NOTE_CHILD);
        final SickNote childSickNote = new SickNote();
        childSickNote.setStartDate(LocalDate.of(2019, 2, 1));
        childSickNote.setEndDate(LocalDate.of(2019, 3, 1));
        childSickNote.setDayLength(FULL);
        childSickNote.setStatus(ACTIVE);
        childSickNote.setSickNoteType(childSickType);
        childSickNote.setPerson(person);
        childSickNote.setAubStartDate(LocalDate.of(2019, 2, 10));
        childSickNote.setAubEndDate(LocalDate.of(2019, 2, 15));
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2019, 2, 11), LocalDate.of(2019, 3, 1), person)).thenReturn(ONE);
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 15), person)).thenReturn(BigDecimal.valueOf(5L));

        final SickNoteType sickType = new SickNoteType();
        sickType.setCategory(SICK_NOTE);
        final SickNote sickNote = new SickNote();
        sickNote.setStartDate(LocalDate.of(2019, 4, 1));
        sickNote.setEndDate(LocalDate.of(2019, 5, 1));
        sickNote.setDayLength(FULL);
        sickNote.setStatus(ACTIVE);
        sickNote.setSickNoteType(sickType);
        sickNote.setPerson(person);
        sickNote.setAubStartDate(LocalDate.of(2019, 4, 10));
        sickNote.setAubEndDate(LocalDate.of(2019, 4, 20));
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 15), person)).thenReturn(TEN);
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2019, 4, 10), LocalDate.of(2019, 4, 15), person)).thenReturn(BigDecimal.valueOf(15L));

        final LocalDate requestStartDate = LocalDate.of(2019, 2, 11);
        final LocalDate requestEndDate = LocalDate.of(2019, 4, 15);
        when(sickNoteService.getForStatesAndPersonAndPersonHasRoles(List.of(ACTIVE), persons, List.of(USER), requestStartDate, requestEndDate)).thenReturn(asList(sickNote, childSickNote));

        perform(get("/web/sicknote")
            .param("from", requestStartDate.toString())
            .param("to", requestEndDate.toString()))
            .andExpect(status().isOk())
            .andExpect(model().attribute("sickDays", hasValue(hasProperty("days", hasEntry("TOTAL", TEN)))))
            .andExpect(model().attribute("sickDays", hasValue(hasProperty("days", hasEntry("WITH_AUB", BigDecimal.valueOf(15L))))))
            .andExpect(model().attribute("childSickDays", hasValue(hasProperty("days", hasEntry("TOTAL", ONE)))))
            .andExpect(model().attribute("childSickDays", hasValue(hasProperty("days", hasEntry("WITH_AUB", BigDecimal.valueOf(5L))))))
            .andExpect(model().attribute("persons", persons))
            .andExpect(model().attribute("from", requestStartDate))
            .andExpect(model().attribute("to", requestEndDate))
            .andExpect(model().attribute("period", hasProperty("startDate", is(requestStartDate))))
            .andExpect(model().attribute("period", hasProperty("endDate", is(requestEndDate))))
            .andExpect(view().name("thymeleaf/sicknote/sick_notes"));
    }

    @Test
    void periodsSickNotesWithDateRangeWithDepartmentHeadRole() throws Exception {

        final Person departmentHead = new Person();
        departmentHead.setId(1);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(departmentHead);

        final Person person = new Person();
        person.setId(1);
        person.setPermissions(List.of(USER));
        final List<Person> persons = List.of(person);
        when(departmentService.getMembersForDepartmentHead(departmentHead)).thenReturn(persons);

        final SickNoteType childSickType = new SickNoteType();
        childSickType.setCategory(SICK_NOTE_CHILD);
        final SickNote childSickNote = new SickNote();
        childSickNote.setStartDate(LocalDate.of(2019, 2, 1));
        childSickNote.setEndDate(LocalDate.of(2019, 3, 1));
        childSickNote.setDayLength(FULL);
        childSickNote.setStatus(ACTIVE);
        childSickNote.setSickNoteType(childSickType);
        childSickNote.setPerson(person);
        childSickNote.setAubStartDate(LocalDate.of(2019, 2, 10));
        childSickNote.setAubEndDate(LocalDate.of(2019, 2, 15));
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2019, 2, 11), LocalDate.of(2019, 3, 1), person)).thenReturn(ONE);
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 15), person)).thenReturn(BigDecimal.valueOf(5L));

        final SickNoteType sickType = new SickNoteType();
        sickType.setCategory(SICK_NOTE);
        final SickNote sickNote = new SickNote();
        sickNote.setStartDate(LocalDate.of(2019, 4, 1));
        sickNote.setEndDate(LocalDate.of(2019, 5, 1));
        sickNote.setDayLength(FULL);
        sickNote.setStatus(ACTIVE);
        sickNote.setSickNoteType(sickType);
        sickNote.setPerson(person);
        sickNote.setAubStartDate(LocalDate.of(2019, 4, 10));
        sickNote.setAubEndDate(LocalDate.of(2019, 4, 20));
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 15), person)).thenReturn(TEN);
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2019, 4, 10), LocalDate.of(2019, 4, 15), person)).thenReturn(BigDecimal.valueOf(15L));

        final LocalDate requestStartDate = LocalDate.of(2019, 2, 11);
        final LocalDate requestEndDate = LocalDate.of(2019, 4, 15);
        when(sickNoteService.getForStatesAndPersonAndPersonHasRoles(List.of(ACTIVE), persons, List.of(USER), requestStartDate, requestEndDate)).thenReturn(asList(sickNote, childSickNote));

        perform(get("/web/sicknote")
            .param("from", requestStartDate.toString())
            .param("to", requestEndDate.toString()))
            .andExpect(status().isOk())
            .andExpect(model().attribute("sickDays", hasValue(hasProperty("days", hasEntry("TOTAL", TEN)))))
            .andExpect(model().attribute("sickDays", hasValue(hasProperty("days", hasEntry("WITH_AUB", BigDecimal.valueOf(15L))))))
            .andExpect(model().attribute("childSickDays", hasValue(hasProperty("days", hasEntry("TOTAL", ONE)))))
            .andExpect(model().attribute("childSickDays", hasValue(hasProperty("days", hasEntry("WITH_AUB", BigDecimal.valueOf(5L))))))
            .andExpect(model().attribute("persons", persons))
            .andExpect(model().attribute("from", requestStartDate))
            .andExpect(model().attribute("to", requestEndDate))
            .andExpect(model().attribute("period", hasProperty("startDate", is(requestStartDate))))
            .andExpect(model().attribute("period", hasProperty("endDate", is(requestEndDate))))
            .andExpect(view().name("thymeleaf/sicknote/sick_notes"));
    }

    @Test
    void periodsSickNotesWithDateRangeWithSecondStageAuthorityRole() throws Exception {

        final Person ssa = new Person();
        ssa.setId(1);
        ssa.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getSignedInUser()).thenReturn(ssa);

        final Person person = new Person();
        person.setId(1);
        person.setPermissions(List.of(USER));
        final List<Person> persons = List.of(person);
        when(departmentService.getMembersForSecondStageAuthority(ssa)).thenReturn(persons);

        final SickNoteType childSickType = new SickNoteType();
        childSickType.setCategory(SICK_NOTE_CHILD);
        final SickNote childSickNote = new SickNote();
        childSickNote.setStartDate(LocalDate.of(2019, 2, 1));
        childSickNote.setEndDate(LocalDate.of(2019, 3, 1));
        childSickNote.setDayLength(FULL);
        childSickNote.setStatus(ACTIVE);
        childSickNote.setSickNoteType(childSickType);
        childSickNote.setPerson(person);
        childSickNote.setAubStartDate(LocalDate.of(2019, 2, 10));
        childSickNote.setAubEndDate(LocalDate.of(2019, 2, 15));
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2019, 2, 11), LocalDate.of(2019, 3, 1), person)).thenReturn(ONE);
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 15), person)).thenReturn(BigDecimal.valueOf(5L));

        final SickNoteType sickType = new SickNoteType();
        sickType.setCategory(SICK_NOTE);
        final SickNote sickNote = new SickNote();
        sickNote.setStartDate(LocalDate.of(2019, 4, 1));
        sickNote.setEndDate(LocalDate.of(2019, 5, 1));
        sickNote.setDayLength(FULL);
        sickNote.setStatus(ACTIVE);
        sickNote.setSickNoteType(sickType);
        sickNote.setPerson(person);
        sickNote.setAubStartDate(LocalDate.of(2019, 4, 10));
        sickNote.setAubEndDate(LocalDate.of(2019, 4, 20));
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 15), person)).thenReturn(TEN);
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2019, 4, 10), LocalDate.of(2019, 4, 15), person)).thenReturn(BigDecimal.valueOf(15L));

        final LocalDate requestStartDate = LocalDate.of(2019, 2, 11);
        final LocalDate requestEndDate = LocalDate.of(2019, 4, 15);
        when(sickNoteService.getForStatesAndPersonAndPersonHasRoles(List.of(ACTIVE), persons, List.of(USER), requestStartDate, requestEndDate)).thenReturn(asList(sickNote, childSickNote));

        perform(get("/web/sicknote")
            .param("from", requestStartDate.toString())
            .param("to", requestEndDate.toString()))
            .andExpect(status().isOk())
            .andExpect(model().attribute("sickDays", hasValue(hasProperty("days", hasEntry("TOTAL", TEN)))))
            .andExpect(model().attribute("sickDays", hasValue(hasProperty("days", hasEntry("WITH_AUB", BigDecimal.valueOf(15L))))))
            .andExpect(model().attribute("childSickDays", hasValue(hasProperty("days", hasEntry("TOTAL", ONE)))))
            .andExpect(model().attribute("childSickDays", hasValue(hasProperty("days", hasEntry("WITH_AUB", BigDecimal.valueOf(5L))))))
            .andExpect(model().attribute("persons", persons))
            .andExpect(model().attribute("from", requestStartDate))
            .andExpect(model().attribute("to", requestEndDate))
            .andExpect(model().attribute("period", hasProperty("startDate", is(requestStartDate))))
            .andExpect(model().attribute("period", hasProperty("endDate", is(requestEndDate))))
            .andExpect(view().name("thymeleaf/sicknote/sick_notes"));
    }

    @Test
    void periodsSickNotesWithDateRangeWithSecondStageAuthorityAndDepartmentRole() throws Exception {

        final Person dhAndSsa = new Person();
        dhAndSsa.setId(1);
        dhAndSsa.setPermissions(List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));
        when(personService.getSignedInUser()).thenReturn(dhAndSsa);

        final Person person = new Person();
        person.setId(1);
        person.setFirstName("FirstName");
        person.setLastName("Lastname");
        person.setPermissions(List.of(USER));
        when(departmentService.getMembersForSecondStageAuthority(dhAndSsa)).thenReturn(List.of(person));

        final SickNoteType childSickType = new SickNoteType();
        childSickType.setCategory(SICK_NOTE_CHILD);
        final SickNote childSickNote = new SickNote();
        childSickNote.setStartDate(LocalDate.of(2019, 2, 1));
        childSickNote.setEndDate(LocalDate.of(2019, 3, 1));
        childSickNote.setDayLength(FULL);
        childSickNote.setStatus(ACTIVE);
        childSickNote.setSickNoteType(childSickType);
        childSickNote.setPerson(person);
        childSickNote.setAubStartDate(LocalDate.of(2019, 2, 10));
        childSickNote.setAubEndDate(LocalDate.of(2019, 2, 15));
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2019, 2, 11), LocalDate.of(2019, 3, 1), person)).thenReturn(ONE);
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 15), person)).thenReturn(BigDecimal.valueOf(5L));


        final Person person2 = new Person();
        person2.setId(2);
        person2.setFirstName("FirstName two");
        person2.setLastName("Lastname two");
        person2.setPermissions(List.of(USER));
        when(departmentService.getMembersForDepartmentHead(dhAndSsa)).thenReturn(List.of(person2));

        final SickNoteType sickType = new SickNoteType();
        sickType.setCategory(SICK_NOTE);
        final SickNote sickNote = new SickNote();
        sickNote.setStartDate(LocalDate.of(2019, 4, 1));
        sickNote.setEndDate(LocalDate.of(2019, 5, 1));
        sickNote.setDayLength(FULL);
        sickNote.setStatus(ACTIVE);
        sickNote.setSickNoteType(sickType);
        sickNote.setPerson(person2);
        sickNote.setAubStartDate(LocalDate.of(2019, 4, 10));
        sickNote.setAubEndDate(LocalDate.of(2019, 4, 20));
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 15), person2)).thenReturn(TEN);
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2019, 4, 10), LocalDate.of(2019, 4, 15), person2)).thenReturn(BigDecimal.valueOf(15L));

        final LocalDate requestStartDate = LocalDate.of(2019, 2, 11);
        final LocalDate requestEndDate = LocalDate.of(2019, 4, 15);
        when(sickNoteService.getForStatesAndPersonAndPersonHasRoles(List.of(ACTIVE), List.of(person, person2), List.of(USER), requestStartDate, requestEndDate)).thenReturn(asList(sickNote, childSickNote));

        perform(get("/web/sicknote")
            .param("from", requestStartDate.toString())
            .param("to", requestEndDate.toString()))
            .andExpect(status().isOk())
            .andExpect(model().attribute("sickDays", hasValue(hasProperty("days", hasEntry("TOTAL", TEN)))))
            .andExpect(model().attribute("sickDays", hasValue(hasProperty("days", hasEntry("WITH_AUB", BigDecimal.valueOf(15L))))))
            .andExpect(model().attribute("childSickDays", hasValue(hasProperty("days", hasEntry("TOTAL", ONE)))))
            .andExpect(model().attribute("childSickDays", hasValue(hasProperty("days", hasEntry("WITH_AUB", BigDecimal.valueOf(5L))))))
            .andExpect(model().attribute("persons", List.of(person, person2)))
            .andExpect(model().attribute("from", requestStartDate))
            .andExpect(model().attribute("to", requestEndDate))
            .andExpect(model().attribute("period", hasProperty("startDate", is(requestStartDate))))
            .andExpect(model().attribute("period", hasProperty("endDate", is(requestEndDate))))
            .andExpect(view().name("thymeleaf/sicknote/sick_notes"));
    }

    @Test
    void periodsSickNotesWithDateWithoutRange() throws Exception {

        final Person office = new Person();
        office.setId(1);
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);

        final int year = Year.now(clock).getValue();
        final LocalDate startDate = ZonedDateTime.now(clock).withYear(year).with(firstDayOfYear()).toLocalDate();
        final LocalDate endDate = ZonedDateTime.now(clock).withYear(year).with(lastDayOfYear()).toLocalDate();

        final ResultActions resultActions = perform(get("/web/sicknote")
            .param("from", "01.01." + year)
            .param("to", "31.12." + year));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("from", startDate));
        resultActions.andExpect(model().attribute("to", endDate));
        resultActions.andExpect(model().attribute("period", hasProperty("startDate", is(startDate))));
        resultActions.andExpect(model().attribute("period", hasProperty("endDate", is(endDate))));
        resultActions.andExpect(view().name("thymeleaf/sicknote/sick_notes"));
    }

    @Test
    void sickNotesWithoutPersonnelNumberColumn() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1);
        signedInUser.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate requestStartDate = LocalDate.of(2019, 2, 11);
        final LocalDate requestEndDate = LocalDate.of(2019, 4, 15);

        perform(get("/web/sicknote")
            .param("from", requestStartDate.toString())
            .param("to", requestEndDate.toString()))
            .andExpect(model().attribute("showPersonnelNumberColumn", false))
            .andExpect(view().name("thymeleaf/sicknote/sick_notes"));
    }

    @Test
    void sickNotesWithPersonnelNumberColumn() throws Exception {

        final Person office = new Person();
        office.setId(1);
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);

        final Person person = new Person();
        person.setId(1);
        final List<Person> persons = List.of(person);
        when(personService.getActivePersons()).thenReturn(persons);
        when(personBasedataService.getBasedataByPersonId(1)).thenReturn(Optional.of(new PersonBasedata(new PersonId(1), "42", null)));

        final LocalDate requestStartDate = LocalDate.of(2019, 2, 11);
        final LocalDate requestEndDate = LocalDate.of(2019, 4, 15);

        perform(get("/web/sicknote")
            .param("from", requestStartDate.toString())
            .param("to", requestEndDate.toString()))
            .andExpect(model().attribute("persons", persons))
            .andExpect(model().attribute("personnelNumberOfPersons", Map.of(1, "42")))
            .andExpect(model().attribute("showPersonnelNumberColumn", true))
            .andExpect(view().name("thymeleaf/sicknote/sick_notes"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
