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
import org.synyx.urlaubsverwaltung.account.service.AccountService;
import org.synyx.urlaubsverwaltung.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.service.ApplicationCommentService;
import org.synyx.urlaubsverwaltung.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.application.service.exception.ImpatientAboutApplicationForLeaveProcessException;
import org.synyx.urlaubsverwaltung.application.service.exception.RemindAlreadySentException;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
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
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationForLeaveDetailsViewControllerTest {

    private ApplicationForLeaveDetailsViewController sut;

    private static final int APPLICATION_ID = 57;
    private static final String ERRORS_ATTRIBUTE = "errors";

    @Mock
    private PersonService personService;

    @Mock
    private AccountService accountService;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ApplicationInteractionService applicationInteractionService;

    @Mock
    private VacationDaysService vacationDaysService;

    @Mock
    private ApplicationCommentService commentService;

    @Mock
    private WorkDaysService workDaysService;

    @Mock
    private ApplicationCommentValidator commentValidator;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private WorkingTimeService workingTimeService;

    @Before
    public void setUp() {

        when(commentService.getCommentsByApplication(any())).thenReturn(singletonList(new ApplicationComment(somePerson())));

        sut = new ApplicationForLeaveDetailsViewController(vacationDaysService, personService, accountService, applicationService,
            applicationInteractionService, commentService, workDaysService, commentValidator, departmentService, workingTimeService, Clock.systemUTC());
    }

    @Test
    public void showApplicationDetailForUnknownApplicationIdThrowsUnknownApplicationForLeaveException() {

        assertThatThrownBy(() ->

            perform(get("/web/application/" + APPLICATION_ID))

        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    public void showApplicationDetailThrowsAccessDeniedExceptionIfSignedInUserIsNotAllowedToAccessPersonData() {

        final Person signedInPerson = somePerson();
        final Person applicationPerson = somePerson();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(applicationPerson)));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, applicationPerson)).thenReturn(false);

        assertThatThrownBy(() ->

            perform(get("/web/application/" + APPLICATION_ID))

        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void showApplicationDetailUsesProvidedYear() throws Exception {

        when(personService.getSignedInUser()).thenReturn(somePerson());
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final int requestedYear = 1987;

        perform(get("/web/application/" + APPLICATION_ID)
            .param("year", Integer.toString(requestedYear))
        ).andExpect(model().attribute("year", requestedYear));
    }

    @Test
    public void showApplicationDetailDefaultsToApplicationEndDateYearIfNoYearProvided() throws Exception {

        when(personService.getSignedInUser()).thenReturn(somePerson());

        Application application = someApplication();

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final int applicationEndYear = application.getEndDate().getYear();
        perform(get("/web/application/" + APPLICATION_ID))
            .andExpect(model().attribute("year", applicationEndYear));
    }

    @Test
    public void showApplicationDetailUsesCorrectView() throws Exception {

        when(personService.getSignedInUser()).thenReturn(somePerson());
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        perform(get("/web/application/" + APPLICATION_ID))
            .andExpect(view().name("application/app_detail"));
    }

    @Test
    public void allowApplicationForUnknownApplicationIdThrowsUnknownApplicationForLeaveException() {

        assertThatThrownBy(() ->

            perform(post("/web/application/" + APPLICATION_ID + "/allow"))

        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    public void allowApplicationThrowsAccessDeniedIfNeitherBossNorDepartmentHeadNorSecondStageAuthority() {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        assertThatThrownBy(() ->

            perform(post("/web/application/" + APPLICATION_ID + "/allow"))

        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void allowApplicationAllowedForBoss() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(allowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow"))
            .andExpect(status().isFound());
    }

    @Test
    public void allowApplicationAllowedForDepartmentHeadOfPerson() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(DEPARTMENT_HEAD));
        when(departmentService.isDepartmentHeadOfPerson(any(), any())).thenReturn(true);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(allowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow"))
            .andExpect(status().isFound());
    }

    @Test
    public void allowApplicationThrowsAccessDeniedForDepartmentHeadOfNotOfPerson() {

        when(personService.getSignedInUser()).thenReturn(personWithRole(DEPARTMENT_HEAD));
        when(departmentService.isDepartmentHeadOfPerson(any(), any())).thenReturn(false);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        assertThatThrownBy(() ->

            perform(post("/web/application/" + APPLICATION_ID + "/allow"))

        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void allowApplicationAllowedForSecondStageAuthorityOfPerson() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(SECOND_STAGE_AUTHORITY));
        when(departmentService.isSecondStageAuthorityOfPerson(any(), any())).thenReturn(true);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(allowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow"))
            .andExpect(status().isFound());
    }

    @Test
    public void allowApplicationThrowsAccessDeniedForSecondStageAuthorityOfNotOfPerson() {

        when(personService.getSignedInUser()).thenReturn(personWithRole(SECOND_STAGE_AUTHORITY));
        when(departmentService.isSecondStageAuthorityOfPerson(any(), any())).thenReturn(false);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        assertThatThrownBy(() ->

            perform(post("/web/application/" + APPLICATION_ID + "/allow"))

        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void allowApplicationAddsFlashAttributeAndRedirectsToApplicationIfValidationFails() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("text", "errors");
            return null;
        }).when(commentValidator).validate(any(), any());

        perform(post("/web/application/" + APPLICATION_ID + "/allow"))
            .andExpect(flash().attribute(ERRORS_ATTRIBUTE, instanceOf(Errors.class)))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/application/" + APPLICATION_ID + "?action=allow"));
    }

    @Test
    public void allowApplicationCallsServiceToAllowApplication() throws Exception {

        Person signedInPerson = personWithRole(BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        Application application = someApplication();
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(allowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow"));

        verify(applicationInteractionService).allow(eq(application), eq(signedInPerson), any());
    }

    @Test
    public void allowApplicationCallsServiceToAllowApplicationAddsFlashAttributeAllowed() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(allowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow"))
            .andExpect(flash().attribute("allowSuccess", true));
    }

    @Test
    public void allowApplicationCallsServiceToAllowApplicationAddsFlashAttributeTemporaryAllowed() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(temporaryAllowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow"))
            .andExpect(flash().attribute("temporaryAllowSuccess", true));
    }

    @Test
    public void allowApplicationRedirectsToRedirectUrlIfProvided() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(allowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow")
            .param("redirect", "/some/url"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/some/url"));
    }

    @Test
    public void allowApplicationDefaultsToRedirectToApplication() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(allowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/application/" + APPLICATION_ID));
    }

    @Test
    public void referApplicationForUnknownApplicationIdThrowsUnknownApplicationForLeaveException() {

        assertThatThrownBy(() ->

            perform(post("/web/application/" + APPLICATION_ID + "/refer"))

        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    public void referApplicationThrowsUnknownPersonExceptionIfNoPersonForProvidedUsername() {

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        final String username = "horst";
        when(personService.getPersonByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->

            perform(post("/web/application/" + APPLICATION_ID + "/refer")
                .param("username", username))

        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    public void referApplicationThrowsAccessDeniedExceptionIfNeitherBossNorDepartmentHead() {

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(personService.getSignedInUser()).thenReturn(somePerson());
        when(personService.getPersonByUsername(any())).thenReturn(Optional.of(somePerson()));

        assertThatThrownBy(() ->

            perform(post("/web/application/" + APPLICATION_ID + "/refer"))

        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void referApplicationAccessibleForRoleBoss() throws Exception {

        final Person signedInPerson = personWithRole(BOSS);
        final Person applicationPerson = somePerson();
        final Person recipientPerson = somePerson();
        final Application application = applicationOfPerson(applicationPerson);

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(personService.getPersonByUsername(any())).thenReturn(Optional.of(recipientPerson));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(departmentService.isDepartmentHeadOfPerson(signedInPerson, applicationPerson)).thenReturn(false);

        perform(post("/web/application/" + APPLICATION_ID + "/refer"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/application/" + APPLICATION_ID));

        verify(applicationInteractionService).refer(eq(application), eq(recipientPerson), eq(signedInPerson));
    }

    @Test
    public void referApplicationAccessibleForRoleDepartmentHead() throws Exception {

        final Person signedInPerson = personWithRole(DEPARTMENT_HEAD);
        final Person applicationPerson = somePerson();
        final Person recipientPerson = somePerson();
        final Application application = applicationOfPerson(applicationPerson);

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(personService.getPersonByUsername(any())).thenReturn(Optional.of(recipientPerson));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(departmentService.isDepartmentHeadOfPerson(signedInPerson, applicationPerson)).thenReturn(true);

        perform(post("/web/application/" + APPLICATION_ID + "/refer"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/application/" + APPLICATION_ID));

        verify(applicationInteractionService).refer(eq(application), eq(recipientPerson), eq(signedInPerson));
    }

    @Test
    public void rejectApplicationForUnknownApplicationIdThrowsUnknownApplicationForLeaveException() {

        assertThatThrownBy(() ->

            perform(post("/web/application/" + APPLICATION_ID + "/reject"))

        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    public void rejectApplicationThrowsAccessDeniedIfNeitherBossNorDepartmentHeadNorSecondStageAuthority() {

        final Person signedInPerson = personWithRole(OFFICE);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(signedInPerson)));

        assertThatThrownBy(() ->

            perform(post("/web/application/" + APPLICATION_ID + "/reject"))

        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void rejectApplicationAccessibleForBoss() throws Exception {

        final Person signedInPerson = personWithRole(BOSS);
        final Person person = somePerson();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(departmentService.isDepartmentHeadOfPerson(signedInPerson, person)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityOfPerson(signedInPerson, person)).thenReturn(false);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(person)));

        perform(post("/web/application/" + APPLICATION_ID + "/reject"))
            .andExpect(flash().attribute("rejectSuccess", true))
            .andExpect(status().isFound());
    }

    @Test
    public void rejectApplicationAccessibleForDepartmentHead() throws Exception {

        final Person signedInPerson = personWithRole(DEPARTMENT_HEAD);
        final Person person = somePerson();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(departmentService.isDepartmentHeadOfPerson(signedInPerson, person)).thenReturn(true);
        when(departmentService.isSecondStageAuthorityOfPerson(signedInPerson, person)).thenReturn(false);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(person)));

        perform(post("/web/application/" + APPLICATION_ID + "/reject"))
            .andExpect(flash().attribute("rejectSuccess", true))
            .andExpect(status().isFound());
    }

    @Test
    public void rejectApplicationAccessibleForSecondStageAuthorityOfPerson() throws Exception {

        final Person signedInPerson = personWithRole(SECOND_STAGE_AUTHORITY);
        final Person person = somePerson();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(departmentService.isDepartmentHeadOfPerson(signedInPerson, person)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityOfPerson(signedInPerson, person)).thenReturn(true);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(person)));

        perform(post("/web/application/" + APPLICATION_ID + "/reject"))
            .andExpect(flash().attribute("rejectSuccess", true))
            .andExpect(status().isFound());
    }

    @Test
    public void rejectApplicationRedirectsToApplicationIfValidationFails() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("text", "errors");
            return null;
        }).when(commentValidator).validate(any(), any());

        perform(post("/web/application/" + APPLICATION_ID + "/reject"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/application/" + APPLICATION_ID + "?action=reject"));

        verify(applicationInteractionService, never()).reject(any(), any(), any());
    }

    @Test
    public void rejectApplicationRejectsApplicationIfValidationSuccessful() throws Exception {

        final Person signedInPerson = personWithRole(BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Application application = someApplication();
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        perform(post("/web/application/" + APPLICATION_ID + "/reject"));

        verify(applicationInteractionService).reject(eq(application), eq(signedInPerson), any());
    }

    @Test
    public void rejectApplicationRedirectToUrlIfProvided() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        perform(post("/web/application/" + APPLICATION_ID + "/reject")
            .param("redirect", "/some/url"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/some/url"));
    }

    @Test
    public void rejectApplicationDefaultsToRedirectToApplication() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        perform(post("/web/application/" + APPLICATION_ID + "/reject"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/application/" + APPLICATION_ID));
    }

    @Test
    public void cancelApplicationForUnknownApplicationIdThrowsUnknownApplicationForLeaveException() {

        assertThatThrownBy(() ->

            perform(post("/web/application/" + APPLICATION_ID + "/cancel"))

        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    public void cancelApplicationOfAnotherPersonThrowsAccessDeniedExceptionIfNotOffice() {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(waitingApplication()));

        assertThatThrownBy(() ->

            perform(post("/web/application/" + APPLICATION_ID + "/cancel"))

        ).hasCauseInstanceOf(AccessDeniedException.class);

        verify(applicationInteractionService, never()).cancel(any(), any(), any());
    }

    @Test
    public void cancelApplicationOfAnotherPersonThrowsAccessDeniedExceptionIfOfficeAndApplicationInWrongState() {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(rejectedApplication()));

        assertThatThrownBy(() ->

            perform(post("/web/application/" + APPLICATION_ID + "/cancel"))

        ).hasCauseInstanceOf(AccessDeniedException.class);

        verify(applicationInteractionService, never()).cancel(any(), any(), any());
    }

    @Test
    public void cancelApplicationOfAnotherPersonAllowedIfOfficeAndApplicationWaiting() throws Exception {

        final Person signedInPerson = personWithRole(OFFICE);
        final Application application = waitingApplication();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        perform(post("/web/application/" + APPLICATION_ID + "/cancel"))
            .andExpect(status().isFound());

        verify(applicationInteractionService).cancel(eq(application), eq(signedInPerson), any());
    }

    @Test
    public void cancelApplicationOfAnotherPersonAllowedIfOfficeAndApplicationAllowed() throws Exception {

        final Person signedInPerson = personWithRole(OFFICE);
        final Application application = allowedApplication();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        perform(post("/web/application/" + APPLICATION_ID + "/cancel"))
            .andExpect(status().isFound());

        verify(applicationInteractionService).cancel(eq(application), eq(signedInPerson), any());
    }

    @Test
    public void cancelApplicationOfAnotherPersonAllowedIfOfficeAndApplicationTemporaryAllowed() throws Exception {

        final Person signedInPerson = personWithRole(OFFICE);
        final Application application = temporaryAllowedApplication();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        perform(post("/web/application/" + APPLICATION_ID + "/cancel"))
            .andExpect(status().isFound());

        verify(applicationInteractionService).cancel(eq(application), eq(signedInPerson), any());
    }

    @Test
    public void cancelApplicationAllowedIfSignedInUserIsApplicationUser() throws Exception {

        final Person signedInPerson = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        Application application = applicationOfPerson(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        perform(post("/web/application/" + APPLICATION_ID + "/cancel"))
            .andExpect(status().isFound());

        verify(applicationInteractionService).cancel(eq(application), eq(signedInPerson), any());
    }

    @Test
    public void cancelApplicationRedirectsToApplicationIfValidationFails() throws Exception {

        final Person signedInPerson = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(signedInPerson)));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("text", "errors");
            return null;
        }).when(commentValidator).validate(any(), any());

        perform(post("/web/application/" + APPLICATION_ID + "/cancel"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/application/" + APPLICATION_ID + "?action=cancel"));

        verify(applicationInteractionService, never()).cancel(any(), any(), any());
    }

    @Test
    public void cancelApplicationRedirectsToApplication() throws Exception {

        final Person signedInPerson = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(signedInPerson)));

        perform(post("/web/application/" + APPLICATION_ID + "/cancel"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/application/" + APPLICATION_ID));
    }

    @Test
    public void remindBossWithUnknownApplicationIdThrowsUnknownApplicationForLeaveException() {

        assertThatThrownBy(() ->

            perform(post("/web/application/" + APPLICATION_ID + "/remind"))

        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    public void remindBossAddsFlashAttributeIfThrowsNoException() throws Exception {

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        perform(post("/web/application/" + APPLICATION_ID + "/remind"))
            .andExpect(flash().attribute("remindIsSent", true));
    }

    @Test
    public void remindBossAddsFlashAttributeIfThrowsRemindAlreadySentException() throws Exception {

        final Application application = someApplication();
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(applicationInteractionService.remind(application)).thenThrow(RemindAlreadySentException.class);

        perform(post("/web/application/" + APPLICATION_ID + "/remind"))
            .andExpect(flash().attribute("remindAlreadySent", true));
    }

    @Test
    public void remindBossAddsFlashAttributeIfThrowsImpatientAboutApplicationForLeaveProcessException() throws Exception {

        final Application application = someApplication();
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(applicationInteractionService.remind(application)).thenThrow(ImpatientAboutApplicationForLeaveProcessException.class);

        perform(post("/web/application/" + APPLICATION_ID + "/remind"))
            .andExpect(flash().attribute("remindNoWay", true));
    }

    @Test
    public void remindBossRedirectsToApplication() throws Exception {

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        perform(post("/web/application/" + APPLICATION_ID + "/remind"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/application/" + APPLICATION_ID));
    }

    private static Person somePerson() {

        return new Person();
    }

    private static Application applicationOfPerson(Person person) {

        Application application = new Application();

        application.setPerson(person);
        application.setStartDate(LocalDate.now().plusDays(10));
        application.setEndDate(LocalDate.now().plusDays(30));
        application.setStatus(WAITING);

        return application;
    }

    private static Application someApplication() {

        return applicationOfPerson(somePerson());
    }

    private static Application allowedApplication() {

        Application application = someApplication();
        application.setStatus(ALLOWED);

        return application;
    }

    private static Application temporaryAllowedApplication() {

        Application application = someApplication();
        application.setStatus(TEMPORARY_ALLOWED);

        return application;
    }

    private static Application waitingApplication() {

        Application application = someApplication();
        application.setStatus(WAITING);

        return application;
    }

    private static Application rejectedApplication() {

        Application application = someApplication();
        application.setStatus(REJECTED);

        return application;
    }

    private static Person personWithRole(Role role) {

        Person person = new Person();
        person.setId(1);
        person.setPermissions(singletonList(role));

        return person;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {

        return standaloneSetup(sut).build().perform(builder);
    }

}
