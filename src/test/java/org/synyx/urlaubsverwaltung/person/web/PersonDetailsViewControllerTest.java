package org.synyx.urlaubsverwaltung.person.web;

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
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.department.web.UnknownDepartmentException;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
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
    private VacationDaysService vacationDaysService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private WorkingTimeService workingTimeService;
    @Mock
    private SettingsService settingsService;

    private Person person;

    @BeforeEach
    void setUp() {

        clock = Clock.systemUTC();
        sut = new PersonDetailsViewController(personService, accountService, vacationDaysService, departmentService,
            workingTimeService, settingsService, clock);

        person = new Person();
        person.setId(1);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));
    }

    @Test
    void showPersonRedirectsToPersonActiveTrue() throws Exception {

        final ResultActions resultActions = perform(get("/web/person"));

        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(header().string("Location", "/web/person?active=true"));
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
    void showPersonWithActiveTrueForUserWithRoleBossCallsCorrectService() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(USER, BOSS));

        perform(get("/web/person").param("active", "true"));

        verify(personService).getActivePersons();
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleOfficeCallsCorrectService() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(USER, OFFICE));

        perform(get("/web/person").param("active", "true"));

        verify(personService).getActivePersons();
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleDepartmentHeadCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/person").param("active", "true"));

        verify(departmentService).getMembersForDepartmentHead(signedInUser);
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/person").param("active", "true"));

        verify(departmentService).getMembersForSecondStageAuthority(signedInUser);
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleDepartmentHeadAndSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/person").param("active", "true"));

        verify(departmentService).getMembersForDepartmentHead(signedInUser);
        verify(departmentService).getMembersForSecondStageAuthority(signedInUser);
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleDepartmentHeadAndSecondStageAuthorityDistinctPersons() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person("username", "Cloud", "Sky", "sky@exaple.org");
        person.setId(2);
        when(departmentService.getMembersForDepartmentHead(signedInUser)).thenReturn(List.of(person));
        when(departmentService.getMembersForSecondStageAuthority(signedInUser)).thenReturn(List.of(person));

        perform(get("/web/person").param("active", "true"))
            .andExpect(model().attribute("persons", hasSize(1)));;
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleBossCallsCorrectService() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(USER, BOSS));

        perform(get("/web/person").param("active", "false"));

        verify(personService).getInactivePersons();
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleOfficeCallsCorrectService() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(USER, OFFICE));

        perform(get("/web/person").param("active", "false"));

        verify(personService).getInactivePersons();
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleDepartmentHeadCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/person").param("active", "false"));

        verify(departmentService).getMembersForDepartmentHead(signedInUser);
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/person").param("active", "false"));

        verify(departmentService).getMembersForSecondStageAuthority(signedInUser);
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleDepartmentHeadAndSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/person").param("active", "false"));

        verify(departmentService).getMembersForDepartmentHead(signedInUser);
        verify(departmentService).getMembersForSecondStageAuthority(signedInUser);
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleDepartmentHeadAndSecondStageAuthorityDistinctPersons() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person("username", "Cloud", "Sky", "sky@exaple.org");
        person.setId(2);
        person.setPermissions(List.of(INACTIVE));
        when(departmentService.getMembersForDepartmentHead(signedInUser)).thenReturn(List.of(person));
        when(departmentService.getMembersForSecondStageAuthority(signedInUser)).thenReturn(List.of(person));

        perform(get("/web/person").param("active", "false"))
            .andExpect(model().attribute("persons", hasSize(1)));;
    }

    @Test
    void showPersonForUnknownDepartmentIdThrowsUnknownDepartmentException() {

        when(personService.getSignedInUser()).thenReturn(person);
        assertThatThrownBy(() ->

            perform(get("/web/person")
                .param("active", "false")
                .param(DEPARTMENT_ATTRIBUTE, Integer.toString(UNKNOWN_DEPARTMENT_ID)))

        ).hasCauseInstanceOf(UnknownDepartmentException.class);
    }

    @Test
    void showPersonUsesDepartmentWithGivenId() throws Exception {

        final Department department = new Department();

        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getDepartmentById(PERSON_ID)).thenReturn(Optional.of(department));

        perform(get("/web/person")
            .param("active", "true")
            .param(DEPARTMENT_ATTRIBUTE, "1")
        ).andExpect(model().attribute(DEPARTMENT_ATTRIBUTE, department));
    }

    @Test
    void showPersonUsesDepartmentUsesCorrectView() throws Exception {

        when(personService.getSignedInUser()).thenReturn(person);
        perform(get("/web/person").param("active", "true"))
            .andExpect(view().name("person/person_view"));
    }

    @Test
    void showPersonWithActiveFlagUsesGivenYear() throws Exception {

        when(personService.getSignedInUser()).thenReturn(person);
        perform(get("/web/person/")
            .param("active", "true")
            .param(YEAR_ATTRIBUTE, "1985")
        ).andExpect(model().attribute(YEAR_ATTRIBUTE, 1985));
    }

    @Test
    void showPersonWithActiveFlagUsesCurrentYearIfNoYearGiven() throws Exception {

        when(personService.getSignedInUser()).thenReturn(person);
        final int currentYear = Year.now(clock).getValue();

        perform(get("/web/person/").param("active", "true"))
            .andExpect(model().attribute(YEAR_ATTRIBUTE, currentYear));
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

    private static Person personWithRole(Role... role) {
        final Person person = new Person();
        person.setId(1);
        person.setPermissions(Arrays.asList(role));

        return person;
    }
}
