package org.synyx.urlaubsverwaltung.application.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.service.AccountService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.settings.WorkingTimeSettings;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.util.Collections;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationForLeaveFormViewControllerTest {

    private ApplicationForLeaveFormViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private AccountService accountService;
    @Mock
    private VacationTypeService vacationTypeService;
    @Mock
    private ApplicationInteractionService applicationInteractionService;
    @Mock
    private ApplicationForLeaveFormValidator applicationForLeaveFormValidator;
    @Mock
    private SettingsService settingsService;

    private Person person;

    private static final int PERSON_ID = 1;
    private Clock clock;

    @Before
    public void setUp() {
        clock = Clock.systemUTC();
        sut = new ApplicationForLeaveFormViewController(personService, accountService, vacationTypeService,
            applicationInteractionService, applicationForLeaveFormValidator, settingsService, clock);

        person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);
    }

    @Test
    public void overtimeIsActivated() throws Exception {

        final int year = Year.now(clock).getValue();
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(new Account()));

        final VacationType vacationType = new VacationType();
        when(vacationTypeService.getVacationTypes()).thenReturn(singletonList(vacationType));

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setOvertimeActive(true);

        final Settings settings = new Settings();
        settings.setWorkingTimeSettings(workingTimeSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        final ResultActions resultActions = perform(get("/web/application/new"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("overtimeActive", is(true)));
        resultActions.andExpect(model().attribute("vacationTypes", hasItems(vacationType)));
    }

    @Test
    public void overtimeIsDeactivated() throws Exception {

        final int year = Year.now(clock).getValue();
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(new Account()));

        final VacationType vacationType = new VacationType();
        when(vacationTypeService.getVacationTypesFilteredBy(OVERTIME)).thenReturn(singletonList(vacationType));

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setOvertimeActive(false);

        final Settings settings = new Settings();
        settings.setWorkingTimeSettings(workingTimeSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        final ResultActions resultActions = perform(get("/web/application/new"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("overtimeActive", is(false)));
        resultActions.andExpect(model().attribute("vacationTypes", hasItems(vacationType)));
    }

    @Test
    public void getNewApplicationFormDefaultsToSignedInPersonIfPersonIdNotGiven() throws Exception {

        final Person signedInPerson = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(accountService.getHolidaysAccount(anyInt(), eq(signedInPerson))).thenReturn(Optional.of(someAccount()));
        when(settingsService.getSettings()).thenReturn(someSettings());

        perform(get("/web/application/new"))
            .andExpect(model().attribute("person", signedInPerson));
    }

    @Test
    public void getNewApplicationFormUsesPersonOfGivenPersonId() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));

        final Person person = somePerson();
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));

        when(accountService.getHolidaysAccount(anyInt(), eq(person))).thenReturn(Optional.of(someAccount()));
        when(settingsService.getSettings()).thenReturn(someSettings());

        perform(get("/web/application/new")
            .param("person", Integer.toString(PERSON_ID)))
            .andExpect(model().attribute("person", person));
    }

    @Test
    public void getNewApplicationFormForUnknownPersonIdThrowsUnknownPersonException() {

        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            perform(get("/web/application/new")
                .param("person", Integer.toString(PERSON_ID)))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    public void getNewApplicationFormThrowsAccessDeniedExceptionIfGivenPersonNotSignedInPersonAndNotOffice() {

        final Person signedInPerson = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person person = personWithId(PERSON_ID);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));

        assertThatThrownBy(() ->
            perform(get("/web/application/new")
                .param("person", Integer.toString(PERSON_ID)))

        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void getNewApplicationFormAccessibleIfGivenPersonIsSignedInPerson() throws Exception {

        final Person signedInPerson = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(signedInPerson));

        perform(get("/web/application/new")
            .param("person", Integer.toString(PERSON_ID)))
            .andExpect(status().isOk());
    }

    @Test
    public void getNewApplicationFormAccessibleForOfficeIfGivenPersonNotSignedInPerson() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(personWithId(PERSON_ID)));

        perform(get("/web/application/new")
            .param("person", Integer.toString(PERSON_ID)))
            .andExpect(status().isOk());
    }

    @Test
    public void getNewApplicationFormShowsForm() throws Exception {

        perform(get("/web/application/new"))
            .andExpect(view().name("application/app_form"));
    }

    @Test
    public void postNewApplicationFormShowFormIfValidationFails() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettings());

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("reason", "errors");
            errors.reject("globalErrors");
            return null;
        }).when(applicationForLeaveFormValidator).validate(any(), any());

        perform(post("/web/application"))
            .andExpect(model().attribute("errors", instanceOf(Errors.class)))
            .andExpect(view().name("application/app_form"));
    }

    @Test
    public void postNewApplicationFormCallsServiceToApplyApplication() throws Exception {

        final Person person = personWithRole(OFFICE);
        when(personService.getSignedInUser()).thenReturn(person);
        when(applicationInteractionService.apply(any(), any(), any())).thenReturn(someApplication());

        perform(post("/web/application")
            .param("vacationType.category", "HOLIDAY"));

        verify(applicationInteractionService).apply(any(), eq(person), any());
    }

    @Test
    public void postNewApplicationAddsFlashAttributeAndRedirectsToNewApplication() throws Exception {

        final int applicationId = 11;
        final Person person = personWithRole(OFFICE);
        when(personService.getSignedInUser()).thenReturn(person);
        when(applicationInteractionService.apply(any(), any(), any())).thenReturn(applicationWithId(applicationId));

        perform(post("/web/application")
            .param("vacationType.category", "HOLIDAY"))
            .andExpect(flash().attribute("applySuccess", true))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/application/" + applicationId));
    }

    private Person somePerson() {

        return new Person();
    }

    private Person personWithRole(Role role) {

        Person person = somePerson();
        person.setPermissions(Collections.singletonList(role));

        return person;
    }

    private Person personWithId(int id) {

        Person person = somePerson();
        person.setId(id);

        return person;
    }

    private Account someAccount() {

        return new Account();
    }

    private Settings someSettings() {

        return new Settings();
    }

    private Application someApplication() {

        Application application = new Application();
        application.setStartDate(Instant.now().plus(10, DAYS));
        application.setEndDate(Instant.now().plus(20, DAYS));

        return new Application();
    }

    private Application applicationWithId(int id) {

        Application application = someApplication();
        application.setId(id);

        return application;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {

        return standaloneSetup(sut).build().perform(builder);
    }

}
