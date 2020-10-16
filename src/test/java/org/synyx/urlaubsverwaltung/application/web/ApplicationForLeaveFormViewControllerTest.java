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
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.settings.WorkingTimeSettings;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
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

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveFormViewController(personService, accountService, vacationTypeService, applicationInteractionService, applicationForLeaveFormValidator, settingsService);
    }

    @Test
    void overtimeIsActivated() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final int year = ZonedDateTime.now(UTC).getYear();
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

        final int year = ZonedDateTime.now(UTC).getYear();
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

        final Person signedInPerson = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(accountService.getHolidaysAccount(anyInt(), eq(signedInPerson))).thenReturn(Optional.of(someAccount()));
        when(settingsService.getSettings()).thenReturn(someSettings());

        perform(get("/web/application/new"))
            .andExpect(model().attribute("person", signedInPerson));
    }

    @Test
    void getNewApplicationFormUsesPersonOfGivenPersonId() throws Exception {

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
    void getNewApplicationFormForUnknownPersonIdThrowsUnknownPersonException() {

        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            perform(get("/web/application/new")
                .param("person", Integer.toString(PERSON_ID)))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void getNewApplicationFormThrowsAccessDeniedExceptionIfGivenPersonNotSignedInPersonAndNotOffice() {

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
    void getNewApplicationFormAccessibleIfGivenPersonIsSignedInPerson() throws Exception {

        final Person signedInPerson = somePerson();
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
