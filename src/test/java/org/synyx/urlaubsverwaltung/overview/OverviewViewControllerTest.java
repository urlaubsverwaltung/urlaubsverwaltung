package org.synyx.urlaubsverwaltung.overview;


import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.HolidayAccountVacationDays;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeId;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.search.SearchContext;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.Month.APRIL;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.util.Arrays.asList;
import static java.util.Locale.GERMAN;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.REVOKED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.ORANGE;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeType.EXTERNAL;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;

@ExtendWith(MockitoExtension.class)
class OverviewViewControllerTest {

    private OverviewViewController sut;

    private static final long SOME_PERSON_ID = 1;

    @Mock
    private PersonService personService;
    @Mock
    private AccountService accountService;
    @Mock
    private VacationDaysService vacationDaysService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private WorkDaysCountService workDaysCountService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private OvertimeService overtimeService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;

    @Mock
    private VacationTypeViewModelService vacationTypeViewModelService;
    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new OverviewViewController(personService, accountService, vacationDaysService,
            workDaysCountService, applicationService, sickNoteService, overtimeService, settingsService,
            departmentService, vacationTypeViewModelService, personSearchUiFragmentSupplier, clock);
    }

    private void stubWorkDaysCountForApplications(BigDecimal daysPerApplication) {
        // sick notes still use the single-application variant
        lenient().when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any(Person.class)))
            .thenReturn(daysPerApplication);
        lenient().when(workDaysCountService.getWorkDaysCountForApplications(anyCollection()))
            .thenAnswer(invocation -> mapEachApplicationTo(invocation, daysPerApplication));
        lenient().when(workDaysCountService.getWorkDaysCountForApplications(anyCollection(), any(DateRange.class)))
            .thenAnswer(invocation -> mapEachApplicationTo(invocation, daysPerApplication));
    }

    private static Map<Application, BigDecimal> mapEachApplicationTo(InvocationOnMock invocation, BigDecimal value) {
        final Collection<Application> applications = invocation.getArgument(0);
        return applications.stream().collect(toMap(identity(), application -> value));
    }

    @Nested
    class PersonSearch {

        @Test
        void personSearchUiFragmentSupplier() {
            assertThat(sut.personSearchUiFragmentSupplier()).isSameAs(personSearchUiFragmentSupplier);
        }

        @Test
        void linksToOverviewOfPerson() {

            final Person suggestion = new Person();
            suggestion.setId(42L);

            final PersonSuggestionUrlStrategy strategy = sut.personSuggestionUrlStrategy();

            final String actual = strategy.buildSuggestionMainLink(suggestion, searchContext());
            assertThat(actual).isEqualTo("/web/person/42/overview");
        }

        @Test
        void preservesYear() {

            final MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter("year", "2026");

            final Person suggestion = new Person();
            suggestion.setId(42L);

            final PersonSuggestionUrlStrategy strategy = sut.personSuggestionUrlStrategy();

            final String actual = strategy.buildSuggestionMainLink(suggestion, searchContext(request));
            assertThat(actual).isEqualTo("/web/person/42/overview?year=2026");
        }

        @Test
        void ignoresBlankYear() {

            final MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter("year", "  ");

            final Person suggestion = new Person();
            suggestion.setId(42L);

            final PersonSuggestionUrlStrategy strategy = sut.personSuggestionUrlStrategy();

            final String actual = strategy.buildSuggestionMainLink(suggestion, searchContext(request));
            assertThat(actual).isEqualTo("/web/person/42/overview");
        }
    }

    @Test
    void indexRedirectsToOverview() throws Exception {
        perform(get("/"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/overview"));
    }

    @Test
    void showOverviewForUnknownPersonIdThrowsUnknownPersonException() {
        final int unknownPersonId = 437;

        assertThatThrownBy(() ->
            perform(get("/web/person/" + unknownPersonId + "/overview"))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void showOverviewForPersonTheSignedInUserIsNotAllowedForShowsReducedOverview() throws Exception {
        final Person person = somePerson();
        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));

        final Person signedInUser = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Department department = new Department();
        department.setName("Buchhaltung");
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of(department));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)).thenReturn(false);

        final ResultActions actions = perform(get("/web/person/" + SOME_PERSON_ID + "/overview"));
        actions.andExpect(view().name("person/person-overview-reduced"));

        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        assertThat(mav.getModel()).containsEntry("departmentsOfPerson", List.of(department));

        verify(personService).getSignedInUser();
        verify(personService).getPersonByID(SOME_PERSON_ID);
        verify(departmentService).isSignedInUserAllowedToAccessPersonData(signedInUser, person);
        verify(departmentService).getAssignedDepartmentsOfMember(person);
        verifyNoMoreInteractions(personService, accountService, vacationDaysService, applicationService, workDaysCountService, sickNoteService, overtimeService, settingsService, departmentService, vacationTypeViewModelService);
    }

    @Test
    void showOverviewUsesCurrentYearIfNoYearGiven() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = somePerson();
        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));

        final Person signedInUser = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)).thenReturn(true);

        final int currentYear = Year.now(clock).getValue();

        perform(get("/web/person/" + SOME_PERSON_ID + "/overview"));

        verify(accountService).getHolidaysAccount(currentYear, person);
    }

    @Test
    void showOverviewUsesYearParamIfYearGiven() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = somePerson();
        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));

        final Person signedInUser = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)).thenReturn(true);

        final int expectedYear = 1987;

        perform(get("/web/person/" + SOME_PERSON_ID + "/overview")
            .param("year", Integer.toString(expectedYear)));

        verify(accountService).getHolidaysAccount(expectedYear, person);
    }

    @Test
    void showOverviewAddsHolidayAccountInfoToModel() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final Year year = Year.now(clock);
        final Account account = someAccount();
        when(accountService.getHolidaysAccount(year.getValue(), person)).thenReturn(Optional.of(account));
        final Account accountNextYear = someAccount();
        when(accountService.getHolidaysAccount(year.plusYears(1).getValue(), person)).thenReturn(Optional.of(accountNextYear));

        final VacationDaysLeft vacationDaysLeft = someVacationDaysLeft();

        when(vacationDaysService.getVacationDaysLeft(List.of(account), year, List.of(accountNextYear)))
            .thenReturn(Map.of(account, new HolidayAccountVacationDays(account, vacationDaysLeft, vacationDaysLeft)));

        final ResultActions actions = perform(get("/web/person/1/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        assertThat(mav.getModel())
            .containsEntry("vacationDaysLeft", vacationDaysLeft)
            .containsEntry("account", account);
    }

    @Test
    void showOverviewAddsHolidayAccountDetailedAttributes() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());


        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final Year year = Year.now(clock);
        final LocalDate now = LocalDate.now(clock);
        final LocalDate expiryDate = now.minusDays(1);

        final Account account = new Account(person, now.minusDays(10), now.plusDays(10), true, expiryDate,
            BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(1), "");

        when(accountService.getHolidaysAccount(year.getValue(), person)).thenReturn(Optional.of(account));
        when(accountService.getHolidaysAccount(year.plusYears(1).getValue(), person)).thenReturn(Optional.empty());

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(BigDecimal.valueOf(10))
            .withRemainingVacation(BigDecimal.valueOf(5))
            .notExpiring(BigDecimal.valueOf(1))
            .withVacationDaysUsedNextYear(BigDecimal.ZERO)
            .forUsedVacationDaysBeforeExpiry(BigDecimal.ZERO)
            .forUsedVacationDaysAfterExpiry(BigDecimal.ZERO)
            .build();

        when(vacationDaysService.getVacationDaysLeft(List.of(account), year, List.of()))
            .thenReturn(Map.of(account, new HolidayAccountVacationDays(account, vacationDaysLeft, vacationDaysLeft)));

        final ResultActions actions = perform(get("/web/person/1/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        assertThat(mav.getModel())
            .containsEntry("account", account)
            .containsEntry("vacationDaysLeftDays", vacationDaysLeft.getLeftVacationDays(now, account.doRemainingVacationDaysExpire(), account.getExpiryDate()))
            .containsEntry("remainingVacationDaysLeftDays", vacationDaysLeft.getRemainingVacationDaysLeft(now, account.doRemainingVacationDaysExpire(), account.getExpiryDate()))
            .containsEntry("expiredRemainingVacationDays", vacationDaysLeft.getExpiredRemainingVacationDays(now, account.getExpiryDate()))
            .containsEntry("showExpiredVacationDays", true);
    }

    @Test
    void showOverviewWithoutExistingAccount() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person();
        person.setId(1L);

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);

        when(accountService.getHolidaysAccount(1984, person)).thenReturn(Optional.empty());

        final ResultActions actions = perform(get("/web/person/1/overview").param("year", "1984"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        assertThat(mav.getModel()).containsEntry("showExpiredVacationDays", false).doesNotContainKeys("vacationDaysLeft", "expiredRemainingVacationDays", "expiryDate", "isBeforeExpiryDate");
    }

    @Test
    void showOverviewCanAccessCalendarShareForOwn() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final ResultActions actions = perform(get("/web/person/" + SOME_PERSON_ID + "/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        assertThat(mav.getModel()).containsEntry("canAccessCalendarShare", true);
    }

    @Test
    void showOverviewCanAccessCalendarShareAssOffice() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final ResultActions actions = perform(get("/web/person/" + SOME_PERSON_ID + "/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        assertThat(mav.getModel()).containsEntry("canAccessCalendarShare", true);
    }

    @Test
    void showOverviewCanAccessCalendarShareAsBoss() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, BOSS));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final ResultActions actions = perform(get("/web/person/" + SOME_PERSON_ID + "/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        assertThat(mav.getModel()).containsEntry("canAccessCalendarShare", true);
    }

    @Test
    void ensureOverviewCanViewSickNoteForAnotherUserIfOffice() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person personWithRole = new Person();
        personWithRole.setId(1L);
        personWithRole.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(personWithRole);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(new Person()));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final ResultActions actions = perform(get("/web/person/" + SOME_PERSON_ID + "/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        final Object sickNotesOverview = mav.getModel().get("sickNotesOverview");
        assertThat(sickNotesOverview).isNotNull().hasFieldOrPropertyWithValue("canViewSickNoteOfMyselfAndAnotherUser", true);
    }

    @Test
    void ensureOverviewCanViewSickNoteForAnotherUserIfBossAndSickNoteView() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person personWithRole = new Person();
        personWithRole.setId(1L);
        personWithRole.setPermissions(List.of(USER, BOSS, SICK_NOTE_VIEW));
        when(personService.getSignedInUser()).thenReturn(personWithRole);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(new Person()));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final ResultActions actions = perform(get("/web/person/" + SOME_PERSON_ID + "/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        final Object sickNotesOverview = mav.getModel().get("sickNotesOverview");
        assertThat(sickNotesOverview).isNotNull().hasFieldOrPropertyWithValue("canViewSickNoteOfMyselfAndAnotherUser", true);
    }

    @Test
    void ensureOverviewCanNotViewSickNoteForAnotherUserIfBossAndSickNoteView() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person personWithRole = new Person();
        personWithRole.setId(1L);
        personWithRole.setPermissions(List.of(USER, BOSS));
        when(personService.getSignedInUser()).thenReturn(personWithRole);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(new Person()));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final ResultActions actions = perform(get("/web/person/" + SOME_PERSON_ID + "/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        final Object sickNotesOverview = mav.getModel().get("sickNotesOverview");
        assertThat(sickNotesOverview).isNotNull().hasFieldOrPropertyWithValue("canViewSickNoteOfMyselfAndAnotherUser", false);
    }

    @Test
    void ensureOverviewCanViewSickNoteForAnotherUserIfDepartmentRoleAndSickNoteAddRoleAndDepartmentMember() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person departmentHead = new Person();
        departmentHead.setId(1L);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_ADD));
        when(personService.getSignedInUser()).thenReturn(departmentHead);

        final Person person = new Person();
        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(true);

        final ResultActions actions = perform(get("/web/person/" + SOME_PERSON_ID + "/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        final Object sickNotesOverview = mav.getModel().get("sickNotesOverview");
        assertThat(sickNotesOverview).isNotNull().hasFieldOrPropertyWithValue("canViewSickNoteOfMyselfAndAnotherUser", true);
    }

    @Test
    void ensureOverviewCanViewSickNoteForAnotherUserIfDepartmentRoleAndDepartmentMember() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person departmentHead = new Person();
        departmentHead.setId(1L);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(departmentHead);

        final Person person = new Person();
        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(true);

        final ResultActions actions = perform(get("/web/person/" + SOME_PERSON_ID + "/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        final Object sickNotesOverview = mav.getModel().get("sickNotesOverview");
        assertThat(sickNotesOverview).isNotNull().hasFieldOrPropertyWithValue("canViewSickNoteOfMyselfAndAnotherUser", true);
    }


    @Test
    void ensureOverviewCanViewSickNoteForAnotherUserIfSAARoleAndSickNoteAddRoleDepartmentMember() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person ssa = new Person();
        ssa.setId(1L);
        ssa.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, SICK_NOTE_ADD));
        when(personService.getSignedInUser()).thenReturn(ssa);

        final Person person = new Person();
        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(ssa, person)).thenReturn(true);

        final ResultActions actions = perform(get("/web/person/" + SOME_PERSON_ID + "/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        final Object sickNotesOverview = mav.getModel().get("sickNotesOverview");
        assertThat(sickNotesOverview)
            .isNotNull()
            .hasFieldOrPropertyWithValue("canViewSickNoteOfMyselfAndAnotherUser", true);
    }

    @Test
    void ensureOverviewCanViewSickNoteForAnotherUserIfSAARoleAndDepartmentMember() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person ssa = new Person();
        ssa.setId(1L);
        ssa.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getSignedInUser()).thenReturn(ssa);

        final Person person = new Person();
        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(ssa, person)).thenReturn(true);

        final ResultActions actions = perform(get("/web/person/" + SOME_PERSON_ID + "/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        final Object sickNotesOverview = mav.getModel().get("sickNotesOverview");
        assertThat(sickNotesOverview).isNotNull().hasFieldOrPropertyWithValue("canViewSickNoteOfMyselfAndAnotherUser", true);
    }

    @Test
    void ensureModelWhenThereAreNoApplications() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(somePerson()));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), any())).thenReturn(Collections.emptyList());

        final ResultActions actions = perform(get("/web/person/" + SOME_PERSON_ID + "/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        final Object applicationOverviewInformation = mav.getModel().get("applicationOverviewInformation");
        assertThat(applicationOverviewInformation).isNotNull();
        final ApplicationOverviewDto overviewDto = (ApplicationOverviewDto) applicationOverviewInformation;
        assertThat(overviewDto.usedDaysOverview().getHolidayDays().getSum()).isEqualByComparingTo(ZERO);
    }

    @Test
    void showOverview() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);

        final ResultActions resultActions = perform(get("/web/overview?year=2017"));
        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/person/1/overview?year=2017"));
    }

    @Test
    void showOverviewWithoutYear() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);

        final ResultActions resultActions = perform(get("/web/overview"));
        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/person/1/overview"));
    }

    @Test
    void showPersonalOverview() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());


        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        stubWorkDaysCountForApplications(ONE);

        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));

        final LocalDate localDate = LocalDate.parse("2021-06-10");

        final Application revokedApplication = new Application();
        revokedApplication.setStatus(REVOKED);
        revokedApplication.setVacationType(mock(VacationType.class));
        revokedApplication.setPerson(person);
        revokedApplication.setStartDate(localDate.plusDays(1L));
        revokedApplication.setEndDate(localDate.plusDays(2L));

        final Application waitingApplication = new Application();
        waitingApplication.setVacationType(mock(VacationType.class));
        waitingApplication.setPerson(person);
        waitingApplication.setStatus(WAITING);
        waitingApplication.setStartDate(localDate.plusDays(3L));
        waitingApplication.setEndDate(localDate.plusDays(4L));

        final Application allowedApplication = new Application();
        allowedApplication.setVacationType(mock(VacationType.class));
        allowedApplication.setPerson(person);
        allowedApplication.setStatus(ALLOWED);
        allowedApplication.setStartDate(localDate.plusDays(5L));
        allowedApplication.setEndDate(localDate.plusDays(10L));

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), eq(person)))
            .thenReturn(asList(waitingApplication, revokedApplication, allowedApplication));

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SICK_NOTE);

        final SickNote sickNote = SickNote.builder()
            .startDate(localDate.minusDays(1L))
            .endDate(localDate.plusDays(1L))
            .status(SickNoteStatus.ACTIVE)
            .sickNoteType(sickNoteType)
            .person(person)
            .build();

        final SickNote sickNote2 = SickNote.builder()
            .startDate(localDate.minusDays(10L))
            .endDate(localDate.plusDays(10L))
            .status(SickNoteStatus.SUBMITTED)
            .sickNoteType(sickNoteType)
            .person(person)
            .build();

        when(sickNoteService.getByPersonAndPeriod(eq(person), any(), any())).thenReturn(asList(sickNote, sickNote2));

        perform(
            get("/web/person/1/overview").param("year", "2021")
                .locale(GERMAN)
        )
            .andExpect(status().isOk())
            .andExpect(view().name("person/person-overview"));

        final ModelAndView mav = perform(get("/web/person/1/overview").param("year", "2021").locale(GERMAN)).andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        assertThat(mav.getModel()).containsEntry("vacationTypeColors", List.of(new VacationTypeDto(1L, ORANGE)));
    }

    // OVERTIME TESTS

    @Test
    void ensureOverviewAddsOvertimeInformationToModel() throws Exception {
        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);

        when(overtimeService.getOvertimeRecordsForPersonAndYear(person, 2021)).thenReturn(List.of());
        when(overtimeService.isUserIsAllowedToCreateOvertime(person, person)).thenReturn(true);
        when(overtimeService.getTotalOvertimeForPersonAndYear(person, 2021)).thenReturn(Duration.ZERO);
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ZERO);

        final ResultActions actions = perform(get("/web/person/1/overview").param("year", "2021"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();
        assertThat(mav.getModel()).containsKey("overtimeOverviewInformation");
    }

    @Test
    void ensureOverviewOvertimeInformationIsAddedWithCorrectValues() throws Exception {
        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);

        final LocalDate startDate = LocalDate.of(2021, 6, 1);
        final LocalDate endDate = LocalDate.of(2021, 6, 2);
        final Overtime overtime = new Overtime(
            new OvertimeId(1L),
            new PersonId(person.getId()),
            new DateRange(startDate, endDate),
            Duration.ofHours(5),
            EXTERNAL,
            java.time.Instant.now()
        );

        when(overtimeService.getOvertimeRecordsForPersonAndYear(person, 2021)).thenReturn(List.of(overtime));
        when(overtimeService.isUserIsAllowedToCreateOvertime(person, person)).thenReturn(true);
        when(overtimeService.getTotalOvertimeForPersonAndYear(person, 2021)).thenReturn(Duration.ofHours(10));
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ofHours(3));

        final ResultActions actions = perform(get("/web/person/1/overview").param("year", "2021"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();

        final OvertimeOverviewDto overtimeOverview = (OvertimeOverviewDto) mav.getModel().get("overtimeOverviewInformation");
        assertThat(overtimeOverview).isNotNull();
        assertThat(overtimeOverview.overtimeTotal()).isEqualTo(Duration.ofHours(10));
        assertThat(overtimeOverview.overtimeLeft()).isEqualTo(Duration.ofHours(3));
        assertThat(overtimeOverview.userIsAllowedToCreateOvertime()).isTrue();
        assertThat(overtimeOverview.numberOfShownOvertimes()).isEqualTo(1);
        assertThat(overtimeOverview.numberOfTotalOvertimes()).isEqualTo(1);
    }

    @Test
    void ensureOverviewOvertimeInformationUserNotAllowedToCreateOvertime() throws Exception {
        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);

        when(overtimeService.getOvertimeRecordsForPersonAndYear(person, 2021)).thenReturn(List.of());
        when(overtimeService.isUserIsAllowedToCreateOvertime(person, person)).thenReturn(false);
        when(overtimeService.getTotalOvertimeForPersonAndYear(person, 2021)).thenReturn(Duration.ZERO);
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ZERO);

        final ResultActions actions = perform(get("/web/person/1/overview").param("year", "2021"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();

        final OvertimeOverviewDto overtimeOverview = (OvertimeOverviewDto) mav.getModel().get("overtimeOverviewInformation");
        assertThat(overtimeOverview).isNotNull();
        assertThat(overtimeOverview.userIsAllowedToCreateOvertime()).isFalse();
    }

    @Test
    void ensureOverviewOvertimeInformationIsNotActive() throws Exception {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(false);
        when(settingsService.getSettings()).thenReturn(settings);

        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);

        when(overtimeService.getOvertimeRecordsForPersonAndYear(person, 2021)).thenReturn(List.of());
        when(overtimeService.isUserIsAllowedToCreateOvertime(person, person)).thenReturn(false);
        when(overtimeService.getTotalOvertimeForPersonAndYear(person, 2021)).thenReturn(Duration.ZERO);
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ZERO);

        final ResultActions actions = perform(get("/web/person/1/overview").param("year", "2021"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();

        final OvertimeOverviewDto overtimeOverview = (OvertimeOverviewDto) mav.getModel().get("overtimeOverviewInformation");
        assertThat(overtimeOverview).isNotNull();
        assertThat(overtimeOverview.isOvertimeActive()).isFalse();
    }

    // APPLICATION PERMISSION TESTS

    @Test
    void ensureApplicationOverviewCanAddApplicationForLeaveForMyselfWhenPersonEqualsSignedInUser() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), eq(person))).thenReturn(Collections.emptyList());

        final ResultActions actions = perform(get("/web/person/1/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();

        final ApplicationOverviewDto applicationOverview = (ApplicationOverviewDto) mav.getModel().get("applicationOverviewInformation");
        assertThat(applicationOverview).isNotNull();
        assertThat(applicationOverview.canAddApplicationForLeaveForMyself()).isTrue();
    }

    @Test
    void ensureApplicationOverviewCanAddApplicationForLeaveForAnotherUserWhenOffice() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        when(personService.getPersonByID(2L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)).thenReturn(true);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), eq(person))).thenReturn(Collections.emptyList());

        final ResultActions actions = perform(get("/web/person/2/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();

        final ApplicationOverviewDto applicationOverview = (ApplicationOverviewDto) mav.getModel().get("applicationOverviewInformation");
        assertThat(applicationOverview).isNotNull();
        assertThat(applicationOverview.canAddApplicationForLeaveForAnotherUser()).isTrue();
    }

    @Test
    void ensureApplicationOverviewCanAddApplicationForLeaveForAnotherUserWhenDepartmentHeadAndApplicationAdd() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER, DEPARTMENT_HEAD, APPLICATION_ADD));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        when(personService.getPersonByID(2L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)).thenReturn(true);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person)).thenReturn(true);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), eq(person))).thenReturn(Collections.emptyList());

        final ResultActions actions = perform(get("/web/person/2/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();

        final ApplicationOverviewDto applicationOverview = (ApplicationOverviewDto) mav.getModel().get("applicationOverviewInformation");
        assertThat(applicationOverview).isNotNull();
        assertThat(applicationOverview.canAddApplicationForLeaveForAnotherUser()).isTrue();
    }

    @Test
    void ensureApplicationOverviewCanAddApplicationForLeaveForAnotherUserWhenSecondStageAuthorityAndApplicationAdd() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, APPLICATION_ADD));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        when(personService.getPersonByID(2L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)).thenReturn(true);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person)).thenReturn(true);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), eq(person))).thenReturn(Collections.emptyList());

        final ResultActions actions = perform(get("/web/person/2/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();

        final ApplicationOverviewDto applicationOverview = (ApplicationOverviewDto) mav.getModel().get("applicationOverviewInformation");
        assertThat(applicationOverview).isNotNull();
        assertThat(applicationOverview.canAddApplicationForLeaveForAnotherUser()).isTrue();
    }

    @Test
    void ensureApplicationOverviewCannotAddApplicationForLeaveForAnotherUserWhenOnlyApplicationAdd() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER, APPLICATION_ADD));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        when(personService.getPersonByID(2L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)).thenReturn(true);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), eq(person))).thenReturn(Collections.emptyList());

        final ResultActions actions = perform(get("/web/person/2/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();

        final ApplicationOverviewDto applicationOverview = (ApplicationOverviewDto) mav.getModel().get("applicationOverviewInformation");
        assertThat(applicationOverview).isNotNull();
        assertThat(applicationOverview.canAddApplicationForLeaveForAnotherUser()).isFalse();
    }

    @Test
    void ensureApplicationOverviewCanAddForMyselfIsFalseWhenViewingAnotherPerson() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        when(personService.getPersonByID(2L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)).thenReturn(true);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), eq(person))).thenReturn(Collections.emptyList());

        final ResultActions actions = perform(get("/web/person/2/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();

        final ApplicationOverviewDto applicationOverview = (ApplicationOverviewDto) mav.getModel().get("applicationOverviewInformation");
        assertThat(applicationOverview).isNotNull();
        assertThat(applicationOverview.canAddApplicationForLeaveForMyself()).isFalse();
    }

    // YEAR AND SELECTED YEAR MODEL ATTRIBUTES TESTS

    @Test
    void ensureOverviewAddsCurrentYearAndSelectedYearToModel() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);

        final ResultActions actions = perform(get("/web/person/1/overview"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();

        final int currentYear = Year.now(clock).getValue();
        assertThat(mav.getModel()).containsEntry("currentYear", currentYear);
        assertThat(mav.getModel()).containsEntry("selectedYear", currentYear);
    }

    @Test
    void ensureOverviewAddsSelectedYearDifferentFromCurrentYearToModel() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);

        final ResultActions actions = perform(get("/web/person/1/overview").param("year", "2019"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();

        assertThat(mav.getModel()).containsEntry("selectedYear", 2019);
        final int currentYear = Year.now(clock).getValue();
        assertThat(mav.getModel()).containsEntry("currentYear", currentYear);
    }

    // SICK NOTES OVERVIEW COUNT TESTS

    @Test
    void ensureSickNotesOverviewContainsCorrectCounts() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        stubWorkDaysCountForApplications(BigDecimal.valueOf(2));


        final LocalDate localDate = LocalDate.parse("2021-06-10");
        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SICK_NOTE);

        final SickNote sickNote1 = SickNote.builder()
            .startDate(localDate.minusDays(5))
            .endDate(localDate.minusDays(4))
            .status(SickNoteStatus.ACTIVE)
            .sickNoteType(sickNoteType)
            .person(person)
            .build();

        final SickNote sickNote2 = SickNote.builder()
            .startDate(localDate.plusDays(1))
            .endDate(localDate.plusDays(2))
            .status(SickNoteStatus.ACTIVE)
            .sickNoteType(sickNoteType)
            .person(person)
            .build();

        when(sickNoteService.getByPersonAndPeriod(eq(person), any(), any())).thenReturn(List.of(sickNote1, sickNote2));

        final ResultActions actions = perform(get("/web/person/1/overview").param("year", "2021"));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();

        final Object sickNotesOverview = mav.getModel().get("sickNotesOverview");
        assertThat(sickNotesOverview)
            .isNotNull()
            .hasFieldOrPropertyWithValue("numberOfShownSickNotes", 2)
            .hasFieldOrPropertyWithValue("numberOfTotalSickNotes", 2);
    }

    // APPLICATION OVERVIEW COUNT TESTS

    @Test
    void ensureApplicationOverviewContainsCorrectCounts() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);

        final LocalDate localDate = LocalDate.parse("2021-06-10");

        final Application application1 = new Application();
        application1.setVacationType(mock(VacationType.class));
        application1.setPerson(person);
        application1.setStartDate(localDate.minusDays(5));
        application1.setEndDate(localDate.minusDays(4));

        final Application application2 = new Application();
        application2.setVacationType(mock(VacationType.class));
        application2.setPerson(person);
        application2.setStartDate(localDate.plusDays(1));
        application2.setEndDate(localDate.plusDays(2));

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), eq(person)))
            .thenReturn(List.of(application1, application2));

        final ResultActions actions = perform(get("/web/person/1/overview").param("year", "2021").locale(GERMAN));
        final ModelAndView mav = actions.andReturn().getModelAndView();
        assertThat(mav).isNotNull();

        final ApplicationOverviewDto applicationOverview = (ApplicationOverviewDto) mav.getModel().get("applicationOverviewInformation");
        assertThat(applicationOverview).isNotNull();
        assertThat(applicationOverview.numberOfShownApplications()).isLessThanOrEqualTo(5);
        assertThat(applicationOverview.numberOfTotalApplications()).isEqualTo(2);
    }

    private Person somePerson() {
        return new Person();
    }

    private Account someAccount() {
        final LocalDate expiryDate = LocalDate.now(clock).withMonth(APRIL.getValue()).with(firstDayOfMonth());
        final LocalDate validFrom = LocalDate.now().minusDays(10);
        final LocalDate validTo = LocalDate.now().plusDays(10);
        return new Account(somePerson(), validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
    }

    private VacationDaysLeft someVacationDaysLeft() {

        return VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(TEN)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .withVacationDaysUsedNextYear(ZERO)
            .notExpiring(ZERO)
            .build();
    }

    private static SearchContext searchContext() {
        return searchContext(new MockHttpServletRequest());
    }

    private static SearchContext searchContext(HttpServletRequest request) {
        return SearchContext.of(request, null);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
