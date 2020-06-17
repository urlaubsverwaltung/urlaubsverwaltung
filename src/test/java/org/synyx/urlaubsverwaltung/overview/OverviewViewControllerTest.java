package org.synyx.urlaubsverwaltung.overview;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.domain.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.service.AccountService;
import org.synyx.urlaubsverwaltung.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.Collections;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.REVOKED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;

@RunWith(MockitoJUnitRunner.class)
public class OverviewViewControllerTest {

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
    private WorkDaysService calendarService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private OvertimeService overtimeService;
    @Mock
    private SettingsService settingsService;

    private Person person;
    private Clock clock;

    @Before
    public void setUp() {

        clock = Clock.systemUTC();
        sut = new OverviewViewController(personService, accountService, vacationDaysService,
            applicationService, calendarService, sickNoteService, overtimeService, settingsService, departmentService, clock);

        person = new Person();
        person.setId(1);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(person);
    }

    @Test
    public void showOverviewForUnknownPersonIdThrowsUnknownPersonException() {

        final int unknownPersonId = 437;

        assertThatThrownBy(() ->
            perform(get("/web/person/" + unknownPersonId + "/overview"))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    public void showOverviewForPersonTheSignedInUserIsNotAllowedForThrowsAccessDeniedException() {

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
    public void showOverviewUsesCurrentYearIfNoYearGiven() throws Exception {

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
    public void showOverviewUsesYearParamIfYearGiven() throws Exception {

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
    public void showOverviewAddsHolidayAccountInfoToModel() throws Exception {

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
    public void showOverviewDoesNotAddApplicationsToModelIfThereAreNoApplications() throws Exception {

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(somePerson()));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), any())).thenReturn(Collections.emptyList());

        perform(get("/web/person/" + SOME_PERSON_ID + "/overview"))
            .andExpect(model().attribute("applications", nullValue()));
    }

    @Test
    public void showOverview() throws Exception {
        MockHttpServletRequestBuilder builder = get("/web/overview?year=2017");

        final ResultActions resultActions = perform(builder);
        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/person/1/overview?year=2017"));
    }

    @Test
    public void showOverviewWithoutYear() throws Exception {
        MockHttpServletRequestBuilder builder = get("/web/overview");

        final ResultActions resultActions = perform(builder);
        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/person/1/overview"));
    }

    @Test
    public void showPersonalOverview() throws Exception {
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(calendarService.getWorkDays(any(), any(), any(), eq(person))).thenReturn(ONE);

        final Application revokedApplication = new Application();
        revokedApplication.setStatus(REVOKED);

        final VacationType vacationType = new VacationType();
        vacationType.setCategory(HOLIDAY);

        final Application waitingApplication = new Application();
        waitingApplication.setVacationType(vacationType);
        waitingApplication.setPerson(person);
        waitingApplication.setStatus(WAITING);
        waitingApplication.setStartDate(LocalDate.now(UTC).minusDays(1L));
        waitingApplication.setEndDate(LocalDate.now(UTC).plusDays(1L));

        final Application allowedApplication = new Application();
        allowedApplication.setVacationType(vacationType);
        allowedApplication.setPerson(person);
        allowedApplication.setStatus(ALLOWED);
        allowedApplication.setStartDate(LocalDate.now(UTC).minusDays(10L));
        allowedApplication.setEndDate(LocalDate.now(UTC).plusDays(10L));

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), eq(person)))
            .thenReturn(asList(waitingApplication, revokedApplication, allowedApplication));

        final SickNote sickNote = new SickNote();
        sickNote.setStartDate(LocalDate.now(UTC).minusDays(1L));
        sickNote.setEndDate(LocalDate.now(UTC).plusDays(1L));
        final SickNote sickNote2 = new SickNote();
        sickNote2.setStartDate(LocalDate.now(UTC).minusDays(10L));
        sickNote2.setEndDate(LocalDate.now(UTC).plusDays(10L));
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(), any())).thenReturn(asList(sickNote, sickNote2));

        MockHttpServletRequestBuilder builder = get("/web/person/1/overview");
        final ResultActions resultActions = perform(builder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("person/overview"));
        resultActions.andExpect(model().attribute("applications", hasSize(2)));
        resultActions.andExpect(model().attribute("sickNotes", hasSize(2)));
    }

    private Person somePerson() {

        return new Person();
    }

    private Account someAccount() {

        Account account = new Account();
        account.setPerson(somePerson());
        account.setValidFrom(LocalDate.now().minusDays(10));
        account.setValidTo(LocalDate.now().plusDays(10));

        return account;
    }

    private VacationDaysLeft someVacationDaysLeft() {

        return VacationDaysLeft.builder()
            .withAnnualVacation(BigDecimal.TEN)
            .withRemainingVacation(BigDecimal.TEN)
            .forUsedDaysBeforeApril(BigDecimal.ZERO)
            .forUsedDaysAfterApril(BigDecimal.ZERO)
            .withVacationDaysUsedNextYear(BigDecimal.ZERO)
            .notExpiring(BigDecimal.ZERO)
            .get();
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
