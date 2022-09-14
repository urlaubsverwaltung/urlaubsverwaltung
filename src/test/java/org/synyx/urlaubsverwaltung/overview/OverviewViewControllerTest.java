package org.synyx.urlaubsverwaltung.overview;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.Month.APRIL;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.REVOKED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.ORANGE;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class OverviewViewControllerTest {

    private OverviewViewController sut;

    private static final int SOME_PERSON_ID = 1;

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
    private VacationTypeViewModelService vacationTypeViewModelService;
    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new OverviewViewController(personService, accountService, vacationDaysService,
            applicationService, workDaysCountService, sickNoteService, overtimeService, settingsService,
            departmentService, vacationTypeViewModelService, clock);
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
    void showOverviewForPersonTheSignedInUserIsNotAllowedForThrowsAccessDeniedException() {
        final Person person = somePerson();
        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));

        final Person signedInUser = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)).thenReturn(false);

        assertThatThrownBy(() ->
            perform(get("/web/person/" + SOME_PERSON_ID + "/overview"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void showOverviewUsesCurrentYearIfNoYearGiven() throws Exception {
        final Person person = somePerson();
        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));

        final Person signedInUser = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)).thenReturn(true);

        final int currentYear = Year.now(clock).getValue();

        perform(get("/web/person/" + SOME_PERSON_ID + "/overview"));

        verify(accountService).getHolidaysAccount(eq(currentYear), any());
    }

    @Test
    void showOverviewUsesYearParamIfYearGiven() throws Exception {
        final Person person = somePerson();
        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));

        final Person signedInUser = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)).thenReturn(true);

        final int expectedYear = 1987;

        perform(get("/web/person/" + SOME_PERSON_ID + "/overview")
            .param("year", Integer.toString(expectedYear)));

        verify(accountService).getHolidaysAccount(eq(expectedYear), any());
    }

    @Test
    void showOverviewAddsHolidayAccountInfoToModel() throws Exception {
        final Person person = new Person();
        person.setId(1);
        person.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(somePerson()));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final Account account = someAccount();
        when(accountService.getHolidaysAccount(anyInt(), any())).thenReturn(Optional.of(account));

        final VacationDaysLeft vacationDaysLeft = someVacationDaysLeft();
        when(vacationDaysService.getVacationDaysLeft(account, Optional.of(account))).thenReturn(vacationDaysLeft);

        perform(get("/web/person/" + SOME_PERSON_ID + "/overview"))
            .andExpect(model().attribute("vacationDaysLeft", vacationDaysLeft))
            .andExpect(model().attribute("account", account));
    }

    @Test
    void showOverviewWithoutExistingAccount() throws Exception {
        final Person person = new Person();
        person.setId(1);

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);

        when(accountService.getHolidaysAccount(1984, person)).thenReturn(Optional.empty());

        perform(get("/web/person/1/overview").param("year", "1984"))
            .andExpect(model().attribute("showExpiredVacationDays", false))
            .andExpect(model().attributeDoesNotExist("vacationDaysLeft"))
            .andExpect(model().attributeDoesNotExist("expiredRemainingVacationDays"))
            .andExpect(model().attributeDoesNotExist("expiryDate"))
            .andExpect(model().attributeDoesNotExist("isBeforeExpiryDate"))
            .andExpect(model().attributeDoesNotExist("remainingVacationDays"));
    }

    @Test
    void showOverviewCanAccessAbsenceOverview() throws Exception {
        final Person person = new Person();
        person.setId(1);
        person.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        perform(get("/web/person/" + SOME_PERSON_ID + "/overview"))
            .andExpect(model().attribute("canAccessAbsenceOverview", true));
    }


    @Test
    void showOverviewCanAccessCalendarShareForOwn() throws Exception {
        final Person person = new Person();
        person.setId(1);
        person.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        perform(get("/web/person/" + SOME_PERSON_ID + "/overview"))
            .andExpect(model().attribute("canAccessCalendarShare", true));
    }

    @Test
    void showOverviewCanAccessCalendarShareAssOffice() throws Exception {
        final Person person = new Person();
        person.setId(1);
        person.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        perform(get("/web/person/" + SOME_PERSON_ID + "/overview"))
            .andExpect(model().attribute("canAccessCalendarShare", true));
    }

    @Test
    void showOverviewCanAccessCalendarShareAsBoss() throws Exception {
        final Person person = new Person();
        person.setId(1);
        person.setPermissions(List.of(USER, BOSS));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        perform(get("/web/person/" + SOME_PERSON_ID + "/overview"))
            .andExpect(model().attribute("canAccessCalendarShare", true));
    }

    @Test
    void showOverviewCanAddApplicationForAnotherUser() throws Exception {
        final Person person = new Person();
        person.setId(1);
        person.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        perform(get("/web/person/" + SOME_PERSON_ID + "/overview"))
            .andExpect(model().attribute("canAddApplicationForLeaveForAnotherUser", true));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
    void ensureOverviewCanAddSickNoteForAnotherUserIfRole(Role role) throws Exception {
        final Person personWithRole = new Person();
        personWithRole.setId(1);
        personWithRole.setPermissions(List.of(USER, role, SICK_NOTE_ADD));
        when(personService.getSignedInUser()).thenReturn(personWithRole);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(new Person()));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        perform(get("/web/person/" + SOME_PERSON_ID + "/overview"))
            .andExpect(model().attribute("canAddSickNoteAnotherUser", true));
    }

    @Test
    void ensureOverviewCanAddSickNoteForAnotherUserIfDepartmentRoleAndDepartmentMember() throws Exception {
        final Person departmentHead = new Person();
        departmentHead.setId(1);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_ADD));
        when(personService.getSignedInUser()).thenReturn(departmentHead);

        final Person person = new Person();
        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(true);

        perform(get("/web/person/" + SOME_PERSON_ID + "/overview"))
            .andExpect(model().attribute("canAddSickNoteAnotherUser", true));
    }

    @Test
    void ensureOverviewCanAddSickNoteForAnotherUserIfSAARoleAndDepartmentMember() throws Exception {
        final Person ssa = new Person();
        ssa.setId(1);
        ssa.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, SICK_NOTE_ADD));
        when(personService.getSignedInUser()).thenReturn(ssa);

        final Person person = new Person();
        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(ssa, person)).thenReturn(true);

        perform(get("/web/person/" + SOME_PERSON_ID + "/overview"))
            .andExpect(model().attribute("canAddSickNoteAnotherUser", true));
    }

    @Test
    void ensureModelWhenThereAreNoApplications() throws Exception {
        final Person person = new Person();
        person.setId(1);
        person.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(somePerson()));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), any())).thenReturn(Collections.emptyList());

        perform(get("/web/person/" + SOME_PERSON_ID + "/overview"))
            .andExpect(model().attribute("applications", equalTo(List.of())))
            .andExpect(model().attribute("usedDaysOverview",
                hasProperty("holidayDays",
                    hasProperty("sum", equalTo(ZERO))
                )
            ));
    }

    @Test
    void showOverview() throws Exception {
        final Person person = new Person();
        person.setId(1);
        when(personService.getSignedInUser()).thenReturn(person);

        final ResultActions resultActions = perform(get("/web/overview?year=2017"));
        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/person/1/overview?year=2017"));
    }

    @Test
    void showOverviewWithoutYear() throws Exception {
        final Person person = new Person();
        person.setId(1);
        when(personService.getSignedInUser()).thenReturn(person);

        final ResultActions resultActions = perform(get("/web/overview"));
        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/person/1/overview"));
    }

    @Test
    void showPersonalOverview() throws Exception {

        final Person person = new Person();
        person.setId(1);
        person.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(true);
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), eq(person))).thenReturn(ONE);

        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1, ORANGE)));

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(HOLIDAY);

        final LocalDate localDate = LocalDate.parse("2021-06-10");

        final Application revokedApplication = new Application();
        revokedApplication.setStatus(REVOKED);
        revokedApplication.setVacationType(vacationType);
        revokedApplication.setPerson(person);
        revokedApplication.setStartDate(localDate.plusDays(1L));
        revokedApplication.setEndDate(localDate.plusDays(2L));

        final Application waitingApplication = new Application();
        waitingApplication.setVacationType(vacationType);
        waitingApplication.setPerson(person);
        waitingApplication.setStatus(WAITING);
        waitingApplication.setStartDate(localDate.plusDays(3L));
        waitingApplication.setEndDate(localDate.plusDays(4L));

        final Application allowedApplication = new Application();
        allowedApplication.setVacationType(vacationType);
        allowedApplication.setPerson(person);
        allowedApplication.setStatus(ALLOWED);
        allowedApplication.setStartDate(localDate.plusDays(5L));
        allowedApplication.setEndDate(localDate.plusDays(10L));

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), eq(person)))
            .thenReturn(asList(waitingApplication, revokedApplication, allowedApplication));

        final SickNote sickNote = new SickNote();
        sickNote.setStartDate(localDate.minusDays(1L));
        sickNote.setEndDate(localDate.plusDays(1L));
        final SickNote sickNote2 = new SickNote();
        sickNote2.setStartDate(localDate.minusDays(10L));
        sickNote2.setEndDate(localDate.plusDays(10L));
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(), any())).thenReturn(asList(sickNote, sickNote2));

        final ResultActions resultActions = perform(get("/web/person/1/overview").param("year", "2021"));

        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("thymeleaf/person/person-overview"))
            .andExpect(model().attribute("applications", hasSize(3)))
            .andExpect(model().attribute("sickNotes", hasSize(2)))
            .andExpect(model().attribute("signedInUser", person))
            .andExpect(model().attribute("userIsAllowedToWriteOvertime", true))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1, ORANGE)))));
    }


    @Test
    void showUserPersonalOverviewAndIsNotAllowedToWriteOvertime() throws Exception {

        final Person person = new Person();
        person.setId(1);
        person.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(false);

        MockHttpServletRequestBuilder builder = get("/web/person/1/overview");
        final ResultActions resultActions = perform(builder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("userIsAllowedToWriteOvertime", false));
    }

    private Person somePerson() {
        return new Person();
    }

    private Account someAccount() {
        final LocalDate expiryDate = LocalDate.now(clock).withMonth(APRIL.getValue()).with(firstDayOfMonth());
        final LocalDate validFrom = LocalDate.now().minusDays(10);
        final LocalDate validTo = LocalDate.now().plusDays(10);
        return new Account(somePerson(), validFrom, validTo, expiryDate, TEN, TEN, TEN, "comment");
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

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
