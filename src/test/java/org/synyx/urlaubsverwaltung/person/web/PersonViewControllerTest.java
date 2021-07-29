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
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.workingtime.settings.WorkingTimeSettings;
import org.synyx.urlaubsverwaltung.workingtime.settings.WorkingTimeSettingsService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.nullValue;
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
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BADEN_WUERTTEMBERG;


@ExtendWith(MockitoExtension.class)
class PersonViewControllerTest {

    private static final int UNKNOWN_PERSON_ID = 365;
    private static final int PERSON_ID = 1;
    private static final int UNKNOWN_DEPARTMENT_ID = 456;

    private static final String YEAR_ATTRIBUTE = "year";
    private static final String DEPARTMENT_ATTRIBUTE = "department";
    private static Clock clock;

    private PersonViewController sut;

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
    private WorkingTimeSettingsService settingsService;

    private Person person;

    @BeforeEach
    void setUp() {

        clock = Clock.systemUTC();
        sut = new PersonViewController(personService, accountService, vacationDaysService, departmentService,
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
        when(settingsService.getSettings()).thenReturn(settingsWithFederalState(BADEN_WUERTTEMBERG));

        perform(get("/web/person/" + PERSON_ID).param(YEAR_ATTRIBUTE, "1985"))
            .andExpect(model().attribute(YEAR_ATTRIBUTE, 1985));
    }

    @Test
    void showPersonInformationUsesCurrentYearIfNoYearGiven() throws Exception {

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(settingsService.getSettings()).thenReturn(settingsWithFederalState(BADEN_WUERTTEMBERG));

        final int currentYear = Year.now(clock).getValue();

        perform(get("/web/person/" + PERSON_ID))
            .andExpect(model().attribute(YEAR_ATTRIBUTE, currentYear));
    }

    @Test
    void showPersonInformationUsesAccountIfPresent() throws Exception {

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(settingsService.getSettings()).thenReturn(settingsWithFederalState(BADEN_WUERTTEMBERG));

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
        when(settingsService.getSettings()).thenReturn(settingsWithFederalState(BADEN_WUERTTEMBERG));

        perform(get("/web/person/" + PERSON_ID))
            .andExpect(view().name("person/person_detail"));
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleBossCallsCorrectService() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));

        perform(get("/web/person").param("active", "true"));

        verify(personService).getActivePersons();
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleOfficeCallsCorrectService() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));

        perform(get("/web/person").param("active", "true"));

        verify(personService).getActivePersons();
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleDepartmentHeadCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/person").param("active", "true"));

        verify(departmentService).getManagedMembersOfDepartmentHead(signedInUser);
    }

    @Test
    void showPersonWithActiveTrueForUserWithRoleSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/person").param("active", "true"));

        verify(departmentService).getManagedMembersForSecondStageAuthority(signedInUser);
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleBossCallsCorrectService() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));

        perform(get("/web/person").param("active", "false"));

        verify(personService).getInactivePersons();
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleOfficeCallsCorrectService() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));

        perform(get("/web/person").param("active", "false"));

        verify(personService).getInactivePersons();
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleDepartmentHeadCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/person").param("active", "false"));

        verify(departmentService).getManagedMembersOfDepartmentHead(signedInUser);
    }

    @Test
    void showPersonWithActiveFalseForUserWithRoleSecondStageAuthorityCallsCorrectService() throws Exception {

        final Person signedInUser = personWithRole(SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        perform(get("/web/person").param("active", "false"));

        verify(departmentService).getManagedMembersForSecondStageAuthority(signedInUser);
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

    private static WorkingTimeSettings settingsWithFederalState(FederalState federalState) {

        WorkingTimeSettings workingTimeSettingsEntity = new WorkingTimeSettings();
        workingTimeSettingsEntity.setFederalState(federalState);

        return workingTimeSettingsEntity;
    }

    private static Account accountForPerson(Person person) {

        return new Account(person, LocalDate.now(clock), LocalDate.now(clock),
            BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, "");
    }

    private static Person personWithRole(Role role) {

        Person person = new Person();
        person.setId(1);
        person.setPermissions(singletonList(role));

        return person;
    }
}
