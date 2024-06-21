package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.time.Month.APRIL;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


@ExtendWith(MockitoExtension.class)
class PersonDetailsViewControllerTest {

    private static Clock clock;

    private PersonDetailsViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private AccountService accountService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private WorkingTimeService workingTimeService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private PersonBasedataService personBasedataService;

    @BeforeEach
    void setUp() {
        clock = Clock.systemUTC();
        sut = new PersonDetailsViewController(personService, accountService, departmentService, workingTimeService, settingsService, personBasedataService, clock);
    }

    @Test
    void showPersonInformationForUnknownIdThrowsUnknownPersonException() {

        assertThatThrownBy(() ->
            perform(get("/web/person/1"))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void showPersonInformationIfSignedInUserIsNotAllowedToAccessPersonDataThrowsAccessDeniedException() {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(false);

        assertThatThrownBy(() ->
            perform(get("/web/person/1"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void showPersonInformationUsesGivenYear() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        userIsAllowedToSubmitSickNotes(false);

        perform(get("/web/person/1").param("year", "1985"))
            .andExpect(model().attribute("currentYear", Year.now().getValue()))
            .andExpect(model().attribute("selectedYear", 1985));
    }

    @Test
    void showPersonInformationShowsBasedata() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(1L), "42", "additional information");
        when(personBasedataService.getBasedataByPersonId(1)).thenReturn(Optional.of(personBasedata));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);

        userIsAllowedToSubmitSickNotes(false);

        perform(get("/web/person/1"))
            .andExpect(model().attribute("personBasedata", hasProperty("personnelNumber", is("42"))))
            .andExpect(model().attribute("personBasedata", hasProperty("additionalInformation", is("additional information"))));
    }

    @Test
    void showPersonInformationUsesCurrentYearIfNoYearGiven() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);

        userIsAllowedToSubmitSickNotes(false);

        final int currentYear = Year.now(clock).getValue();

        perform(get("/web/person/1"))
            .andExpect(model().attribute("currentYear", currentYear))
            .andExpect(model().attribute("selectedYear", currentYear));
    }

    @Test
    void showPersonInformationUsesAccountIfPresent() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        userIsAllowedToSubmitSickNotes(false);

        perform(get("/web/person/1"))
            .andExpect(model().attribute("account", nullValue()));

        final Account account = accountForPerson(person);
        when(accountService.getHolidaysAccount(Year.now(clock).getValue(), person)).thenReturn(Optional.of(account));

        perform(get("/web/person/1"))
            .andExpect(model().attribute("account", account));
    }

    @Test
    void showPersonInformationUsesCorrectView() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        userIsAllowedToSubmitSickNotes(false);

        perform(get("/web/person/1"))
            .andExpect(view().name("person/person_detail"));
    }

    @Test
    void showPersonInformationOfficeCanEditPermissions() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        final Person office = new Person();
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        userIsAllowedToSubmitSickNotes(false);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(office, person)).thenReturn(true);

        perform(get("/web/person/1"))
            .andExpect(view().name("person/person_detail"))
            .andExpect(model().attribute("canEditPermissions", true));
    }

    @Test
    void showPersonInformationOfficeCanEditDepartments() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        final Person office = new Person();
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        userIsAllowedToSubmitSickNotes(false);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(office, person)).thenReturn(true);

        perform(get("/web/person/1"))
            .andExpect(view().name("person/person_detail"))
            .andExpect(model().attribute("canEditDepartments", true));
    }

    @Test
    void showPersonInformationOfficeCanEditAccounts() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        final Person office = new Person();
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        userIsAllowedToSubmitSickNotes(false);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(office, person)).thenReturn(true);

        perform(get("/web/person/1"))
            .andExpect(view().name("person/person_detail"))
            .andExpect(model().attribute("canEditAccounts", true));
    }

    @Test
    void showPersonInformationOfficeCanEditWorkingtimes() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));

        final Person office = new Person();
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        userIsAllowedToSubmitSickNotes(false);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(office, person)).thenReturn(true);

        perform(get("/web/person/1"))
            .andExpect(view().name("person/person_detail"))
            .andExpect(model().attribute("canEditWorkingtime", true));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build()
            .perform(builder);
    }

    private static Account accountForPerson(Person person) {
        final LocalDate expiryDate = LocalDate.now(clock).withMonth(APRIL.getValue()).with(firstDayOfMonth());
        return new Account(person, LocalDate.now(clock), LocalDate.now(clock), true, expiryDate, ONE, TEN, TEN, "");
    }

    private void userIsAllowedToSubmitSickNotes(boolean userIsAllowedToSubmit) {
        final Settings settings = new Settings();
        final SickNoteSettings sickNoteSettings = new SickNoteSettings();
        sickNoteSettings.setUserIsAllowedToSubmitSickNotes(userIsAllowedToSubmit);
        settings.setSickNoteSettings(sickNoteSettings);
        when(settingsService.getSettings()).thenReturn(settings);
    }
}
