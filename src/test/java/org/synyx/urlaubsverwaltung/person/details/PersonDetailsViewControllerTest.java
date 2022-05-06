package org.synyx.urlaubsverwaltung.person.details;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;


@ExtendWith(MockitoExtension.class)
class PersonDetailsViewControllerTest {

    private static final int UNKNOWN_PERSON_ID = 365;
    private static final int PERSON_ID = 1;
    private static final int UNKNOWN_DEPARTMENT_ID = 456;

    private static final String YEAR_ATTRIBUTE = "year";
    private static final String DEPARTMENT_ATTRIBUTE = "department";

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

    private Person person;

    @BeforeEach
    void setUp() {

        clock = Clock.systemUTC();
        sut = new PersonDetailsViewController(personService, accountService, departmentService,
            workingTimeService, settingsService, personBasedataService, clock);

        person = new Person();
        person.setId(1);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));
    }

    @Test
    void showPersonInformationForUnknownIdThrowsUnknownPersonException() {
        assertThatThrownBy(() ->
            perform(get("/web/person/" + UNKNOWN_PERSON_ID))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void showPersonInformationIfSignedInUserIsNotAllowedToAccessPersonDataThrowsAccessDeniedException() {
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(false);

        assertThatThrownBy(() ->
            perform(get("/web/person/" + PERSON_ID))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void showPersonInformationUsesGivenYear() throws Exception {

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(settingsService.getSettings()).thenReturn(settingsWithFederalState(GERMANY_BADEN_WUERTTEMBERG));

        perform(get("/web/person/" + PERSON_ID).param(YEAR_ATTRIBUTE, "1985"))
            .andExpect(model().attribute(YEAR_ATTRIBUTE, 1985));
    }

    @Test
    void showPersonInformationShowsBasedata() throws Exception {

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));
        final PersonBasedata personBasedata = new PersonBasedata(PERSON_ID, "42", "additional information");
        when(personBasedataService.getBasedataByPersonId(PERSON_ID)).thenReturn(Optional.of(personBasedata));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(settingsService.getSettings()).thenReturn(settingsWithFederalState(GERMANY_BADEN_WUERTTEMBERG));

        perform(get("/web/person/" + PERSON_ID))
            .andExpect(model().attribute("personBasedata", hasProperty("personnelNumber", is("42"))))
            .andExpect(model().attribute("personBasedata", hasProperty("additionalInformation", is("additional information"))));
    }

    @Test
    void showPersonInformationUsesCurrentYearIfNoYearGiven() throws Exception {

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(settingsService.getSettings()).thenReturn(settingsWithFederalState(GERMANY_BADEN_WUERTTEMBERG));

        final int currentYear = Year.now(clock).getValue();

        perform(get("/web/person/" + PERSON_ID))
            .andExpect(model().attribute(YEAR_ATTRIBUTE, currentYear));
    }

    @Test
    void showPersonInformationUsesAccountIfPresent() throws Exception {

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(settingsService.getSettings()).thenReturn(settingsWithFederalState(GERMANY_BADEN_WUERTTEMBERG));

        perform(get("/web/person/" + PERSON_ID))
            .andExpect(model().attribute("account", nullValue()));

        final Account account = accountForPerson(person);
        when(accountService.getHolidaysAccount(anyInt(), any())).thenReturn(Optional.of(account));

        perform(get("/web/person/" + PERSON_ID))
            .andExpect(model().attribute("account", account));
    }

    @Test
    void showPersonInformationUsesCorrectView() throws Exception {

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(settingsService.getSettings()).thenReturn(settingsWithFederalState(GERMANY_BADEN_WUERTTEMBERG));

        perform(get("/web/person/" + PERSON_ID))
            .andExpect(view().name("person/person_detail"));
    }

    @Test
    void showPersonInformationOfficeCanEditPermissions() throws Exception {

        final Person office = new Person();
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));
        when(settingsService.getSettings()).thenReturn(settingsWithFederalState(GERMANY_BADEN_WUERTTEMBERG));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(office, person)).thenReturn(true);

        perform(get("/web/person/" + PERSON_ID))
            .andExpect(view().name("person/person_detail"))
            .andExpect(model().attribute("canEditPermissions", true));
    }

    @Test
    void showPersonInformationOfficeCanEditDepartments() throws Exception {

        final Person office = new Person();
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));
        when(settingsService.getSettings()).thenReturn(settingsWithFederalState(GERMANY_BADEN_WUERTTEMBERG));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(office, person)).thenReturn(true);

        perform(get("/web/person/" + PERSON_ID))
            .andExpect(view().name("person/person_detail"))
            .andExpect(model().attribute("canEditDepartments", true));
    }

    @Test
    void showPersonInformationOfficeCanEditAccounts() throws Exception {

        final Person office = new Person();
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));
        when(settingsService.getSettings()).thenReturn(settingsWithFederalState(GERMANY_BADEN_WUERTTEMBERG));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(office, person)).thenReturn(true);

        perform(get("/web/person/" + PERSON_ID))
            .andExpect(view().name("person/person_detail"))
            .andExpect(model().attribute("canEditAccounts", true));
    }

    @Test
    void showPersonInformationOfficeCanEditWorkingTimes() throws Exception {

        final Person office = new Person();
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));
        when(settingsService.getSettings()).thenReturn(settingsWithFederalState(GERMANY_BADEN_WUERTTEMBERG));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(office, person)).thenReturn(true);

        perform(get("/web/person/" + PERSON_ID))
            .andExpect(view().name("person/person_detail"))
            .andExpect(model().attribute("canEditWorkingtime", true));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }

    private static Settings settingsWithFederalState(FederalState federalState) {

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setFederalState(federalState);

        final Settings settings = new Settings();
        settings.setWorkingTimeSettings(workingTimeSettings);

        return settings;
    }

    private static Account accountForPerson(Person person) {
        return new Account(person, LocalDate.now(clock), LocalDate.now(clock), ONE, TEN, TEN, "");
    }
}
