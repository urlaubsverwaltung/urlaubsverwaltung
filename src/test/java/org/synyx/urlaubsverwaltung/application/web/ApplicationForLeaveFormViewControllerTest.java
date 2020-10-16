package org.synyx.urlaubsverwaltung.application.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.application.service.EditApplicationForLeaveNotAllowedException;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.settings.WorkingTimeSettings;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveFormViewControllerTest {

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

    private static final int PERSON_ID = 1;
    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveFormViewController(personService, accountService, vacationTypeService, applicationInteractionService, applicationForLeaveFormValidator, settingsService, clock);
    }

    @Test
    void overtimeIsActivated() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

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
    void overtimeIsDeactivated() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

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
    void getNewApplicationFormDefaultsToSignedInPersonIfPersonIdNotGiven() throws Exception {

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(accountService.getHolidaysAccount(anyInt(), eq(signedInPerson))).thenReturn(Optional.of(new Account()));
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get("/web/application/new"))
            .andExpect(model().attribute("person", signedInPerson));
    }

    @Test
    void getNewApplicationFormUsesPersonOfGivenPersonId() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));

        final Person person = new Person();
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));

        when(accountService.getHolidaysAccount(anyInt(), eq(person))).thenReturn(Optional.of(new Account()));
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get("/web/application/new")
            .param("person", Integer.toString(PERSON_ID)))
            .andExpect(model().attribute("person", person));
    }

    @Test
    void getNewApplicationFormForUnknownPersonIdThrowsUnknownPersonException() {

        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            perform(get("/web/application/new")
                .param("person", Integer.toString(PERSON_ID)))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void getNewApplicationFormThrowsAccessDeniedExceptionIfGivenPersonNotSignedInPersonAndNotOffice() {

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person person = personWithId(PERSON_ID);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));

        assertThatThrownBy(() ->
            perform(get("/web/application/new")
                .param("person", Integer.toString(PERSON_ID)))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getNewApplicationFormAccessibleIfGivenPersonIsSignedInPerson() throws Exception {

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(signedInPerson));

        perform(get("/web/application/new")
            .param("person", Integer.toString(PERSON_ID)))
            .andExpect(status().isOk());
    }

    @Test
    void getNewApplicationFormAccessibleForOfficeIfGivenPersonNotSignedInPerson() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(personWithId(PERSON_ID)));

        perform(get("/web/application/new")
            .param("person", Integer.toString(PERSON_ID)))
            .andExpect(status().isOk());
    }

    @Test
    void getNewApplicationFormShowsForm() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        perform(get("/web/application/new"))
            .andExpect(view().name("application/app_form"));
    }

    @Test
    void postNewApplicationFormShowFormIfValidationFails() throws Exception {

        when(settingsService.getSettings()).thenReturn(new Settings());

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
    void postNewApplicationFormCallsServiceToApplyApplication() throws Exception {

        final Person person = personWithRole(OFFICE);
        when(personService.getSignedInUser()).thenReturn(person);
        when(applicationInteractionService.apply(any(), any(), any())).thenReturn(someApplication());

        perform(post("/web/application")
            .param("vacationType.category", "HOLIDAY"));

        verify(applicationInteractionService).apply(any(), eq(person), any());
    }

    @Test
    void postNewApplicationAddsFlashAttributeAndRedirectsToNewApplication() throws Exception {

        final int applicationId = 11;
        final Person person = personWithRole(OFFICE);

        when(personService.getSignedInUser()).thenReturn(person);
        when(applicationInteractionService.apply(any(), any(), any())).thenReturn(applicationWithId(applicationId));

        perform(post("/web/application")
            .param("vacationType.category", "HOLIDAY"))
            .andExpect(flash().attribute("applySuccess", true))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/" + applicationId));
    }

    @Test
    void editApplicationForm() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final int year = Year.now(clock).getValue();
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(new Account()));

        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final VacationType vacationType = new VacationType();
        when(vacationTypeService.getVacationTypes()).thenReturn(singletonList(vacationType));

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(WAITING);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        perform(get("/web/application/1/edit"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("noHolidaysAccount", is(false)))
            .andExpect(view().name("application/app_form"));
    }

    @Test
    void editApplicationFormUnknownApplication() {

        when(applicationInteractionService.get(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            perform(get("/web/application/1/edit"))
        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    void editApplicationFormNotWaiting() throws Exception {

        when(applicationInteractionService.get(1)).thenReturn(Optional.empty());

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(ALLOWED);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        perform(get("/web/application/1/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("application/app_notwaiting"));
    }

    @Test
    void editApplicationFormNoHolidayAccount() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final int year = Year.now(clock).getValue();
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.empty());

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(WAITING);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        perform(get("/web/application/1/edit"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("noHolidaysAccount", is(true)))
            .andExpect(view().name("application/app_form"));
    }

    @Test
    void sendEditApplicationForm() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(WAITING);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));
        when(applicationInteractionService.edit(application, person, Optional.of("comment"))).thenReturn(application);

        perform(post("/web/application/1")
            .param("person.id", "1")
            .param("startDate", "28.10.2020")
            .param("endDate", "28.10.2020")
            .param("vacationType.category", "HOLIDAY")
            .param("dayLength", "FULL")
            .param("comment", "comment"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/application/1"));
    }

    @Test
    void sendEditApplicationFormIsNotWaiting() throws Exception {

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(ALLOWED);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        perform(post("/web/application/1")
            .param("person.id", "1")
            .param("startDate", "28.10.2020")
            .param("endDate", "28.10.2020")
            .param("vacationType.category", "HOLIDAY")
            .param("dayLength", "FULL")
            .param("comment", "comment"))
            .andExpect(status().isOk())
            .andExpect(view().name("application/app_notwaiting"));
    }

    @Test
    void sendEditApplicationFormApplicationNotFound() {

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(WAITING);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            perform(post("/web/application/1")
                .param("person.id", "1")
                .param("startDate", "28.10.2020")
                .param("endDate", "28.10.2020")
                .param("vacationType.category", "HOLIDAY")
                .param("dayLength", "FULL")
                .param("comment", "comment"))
        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    void sendEditApplicationFormCannotBeEdited() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(WAITING);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));
        when(applicationInteractionService.edit(application, person, Optional.of("comment"))).thenThrow(EditApplicationForLeaveNotAllowedException.class);

        perform(post("/web/application/1")
            .param("person.id", "1")
            .param("startDate", "28.10.2020")
            .param("endDate", "28.10.2020")
            .param("vacationType.category", "HOLIDAY")
            .param("dayLength", "FULL")
            .param("comment", "comment"))
            .andExpect(status().isOk())
            .andExpect(view().name("application/app_notwaiting"));
    }

    @Test
    void sendEditApplicationFormHasErrors() throws Exception {

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(WAITING);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        doAnswer(invocation -> {
            final Errors errors = invocation.getArgument(1);
            errors.rejectValue("reason", "errors");
            errors.reject("globalErrors");
            return null;
        }).when(applicationForLeaveFormValidator).validate(any(), any());

        perform(post("/web/application/1")
            .param("person.id", "1")
            .param("startDate", "28.10.2020")
            .param("endDate", "28.10.2020")
            .param("vacationType.category", "HOLIDAY")
            .param("dayLength", "FULL")
            .param("comment", "comment"))
            .andExpect(status().isOk())
            .andExpect(view().name("application/app_form"));
    }

    private Person personWithRole(Role role) {
        Person person = new Person();
        person.setPermissions(List.of(role));

        return person;
    }

    private Person personWithId(int id) {
        Person person = new Person();
        person.setId(id);

        return person;
    }

    private Application someApplication() {
        Application application = new Application();
        application.setStartDate(LocalDate.now().plusDays(10));
        application.setEndDate(LocalDate.now().plusDays(20));

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
