package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentValidator;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveDetailsViewControllerTest {

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
    private WorkDaysCountService workDaysCountService;
    @Mock
    private ApplicationCommentValidator commentValidator;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private WorkingTimeService workingTimeService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveDetailsViewController(vacationDaysService, personService, accountService, applicationService,
            applicationInteractionService, commentService, workDaysCountService, commentValidator, departmentService, workingTimeService, clock);
    }

    @Test
    void showApplicationDetailForUnknownApplicationIdThrowsUnknownApplicationForLeaveException() {
        assertThatThrownBy(() ->
            perform(get("/web/application/" + APPLICATION_ID))
        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    void showApplicationDetailThrowsAccessDeniedExceptionIfSignedInUserIsNotAllowedToAccessPersonData() {

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
    void showApplicationDetailUsesProvidedYear() throws Exception {

        when(commentService.getCommentsByApplication(any())).thenReturn(singletonList(new ApplicationComment(somePerson(), clock)));
        when(personService.getSignedInUser()).thenReturn(somePerson());
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final int requestedYear = 1987;

        perform(get("/web/application/" + APPLICATION_ID)
            .param("year", Integer.toString(requestedYear))
        ).andExpect(model().attribute("year", requestedYear));
    }

    @Test
    void showApplicationDetailWithUserDepartments() throws Exception {

        when(commentService.getCommentsByApplication(any())).thenReturn(singletonList(new ApplicationComment(somePerson(), clock)));
        when(personService.getSignedInUser()).thenReturn(somePerson());
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final Person person = new Person();
        person.setId(1);
        final Application application = someApplication();
        application.setPerson(person);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        final Department department = new Department();
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of(department));

        perform(get("/web/application/" + APPLICATION_ID))
            .andExpect(model().attribute("departmentsOfPerson", List.of(department)));
    }

    @Test
    void showApplicationDetailDefaultsToApplicationEndDateYearIfNoYearProvided() throws Exception {

        when(commentService.getCommentsByApplication(any())).thenReturn(singletonList(new ApplicationComment(somePerson(), clock)));
        when(personService.getSignedInUser()).thenReturn(somePerson());

        Application application = someApplication();

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final int applicationEndYear = application.getEndDate().getYear();
        perform(get("/web/application/" + APPLICATION_ID))
            .andExpect(model().attribute("year", applicationEndYear));
    }

    @Test
    void showApplicationDetailUsesCorrectView() throws Exception {

        when(commentService.getCommentsByApplication(any())).thenReturn(singletonList(new ApplicationComment(somePerson(), clock)));
        when(personService.getSignedInUser()).thenReturn(somePerson());
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        perform(get("/web/application/" + APPLICATION_ID))
            .andExpect(view().name("application/app_detail"));
    }

    @Test
    void showApplicationDetailSignedInUserIsBoss() throws Exception {

        when(commentService.getCommentsByApplication(any())).thenReturn(singletonList(new ApplicationComment(somePerson(), clock)));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final Person boss = new Person("boss", "boss", "boss", "boss@example.org");
        boss.setPermissions(List.of(USER, BOSS));
        when(personService.getSignedInUser()).thenReturn(boss);

        perform(get("/web/application/" + APPLICATION_ID))
            .andExpect(view().name("application/app_detail"))
            .andExpect(model().attribute("isBoss", true));
    }

    @Test
    void showApplicationDetailSignedInUserIsOffice() throws Exception {

        when(commentService.getCommentsByApplication(any())).thenReturn(singletonList(new ApplicationComment(somePerson(), clock)));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final Person office = new Person("office", "office", "office", "office@example.org");
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);

        perform(get("/web/application/" + APPLICATION_ID))
            .andExpect(view().name("application/app_detail"))
            .andExpect(model().attribute("isOffice", true));
    }

    @Test
    void showApplicationDetailSignedInUserIsDepartmentHeadOfPerson() throws Exception {

        when(commentService.getCommentsByApplication(any())).thenReturn(singletonList(new ApplicationComment(somePerson(), clock)));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final Person departmentHead = new Person("departmentHead", "departmentHead", "departmentHead", "departmentHead@example.org");
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(departmentHead);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(eq(departmentHead), any(Person.class))).thenReturn(true);

        perform(get("/web/application/" + APPLICATION_ID))
            .andExpect(view().name("application/app_detail"))
            .andExpect(model().attribute("isDepartmentHeadOfPerson", true));
    }

    @Test
    void showApplicationDetailSignedInUserIsSecondStageAuthorityOfPerson() throws Exception {

        when(commentService.getCommentsByApplication(any())).thenReturn(singletonList(new ApplicationComment(somePerson(), clock)));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        final Person ssa = new Person("ssa", "ssa", "ssa", "ssa@example.org");
        ssa.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(ssa);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(eq(ssa), any(Person.class))).thenReturn(true);

        perform(get("/web/application/" + APPLICATION_ID))
            .andExpect(view().name("application/app_detail"))
            .andExpect(model().attribute("isSecondStageAuthorityOfPerson", true));
    }

    @Test
    void allowApplicationForUnknownApplicationIdThrowsUnknownApplicationForLeaveException() {
        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/allow"))
        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    void allowApplicationThrowsAccessDeniedForOwnApplication() {

        final Person person = somePerson();
        when(personService.getSignedInUser()).thenReturn(person);
        final Application application = someApplication();
        application.setPerson(person);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/allow"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void allowApplicationThrowsAccessDeniedIfNeitherBossNorDepartmentHeadNorSecondStageAuthority() {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/allow"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void allowApplicationAllowedForBoss() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(allowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow"))
            .andExpect(status().isFound());
    }

    @Test
    void allowOwnApplicationAllowedForBoss() throws Exception {

        final Person boss = personWithRole(BOSS);
        final Application application = someApplication();
        application.setPerson(boss);
        when(personService.getSignedInUser()).thenReturn(boss);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(allowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow"))
            .andExpect(status().isFound());
    }

    @Test
    void allowApplicationAllowedForDepartmentHeadOfPerson() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(DEPARTMENT_HEAD));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(any(), any())).thenReturn(true);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(allowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow"))
            .andExpect(status().isFound());
    }

    @Test
    void allowApplicationThrowsAccessDeniedForDepartmentHeadOfNotOfPerson() {

        when(personService.getSignedInUser()).thenReturn(personWithRole(DEPARTMENT_HEAD));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(any(), any())).thenReturn(false);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/allow"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void allowApplicationAllowedForSecondStageAuthorityOfPerson() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(SECOND_STAGE_AUTHORITY));
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(any(), any())).thenReturn(true);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(allowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow"))
            .andExpect(status().isFound());
    }

    @Test
    void allowApplicationThrowsAccessDeniedForSecondStageAuthorityOfNotOfPerson() {

        when(personService.getSignedInUser()).thenReturn(personWithRole(SECOND_STAGE_AUTHORITY));
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(any(), any())).thenReturn(false);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/allow"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void allowApplicationAddsFlashAttributeAndRedirectsToApplicationIfValidationFails() throws Exception {

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
            .andExpect(redirectedUrl("/web/application/" + APPLICATION_ID + "?action=allow"));
    }

    @Test
    void allowApplicationCallsServiceToAllowApplication() throws Exception {

        Person signedInPerson = personWithRole(BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        Application application = someApplication();
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(allowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow"));

        verify(applicationInteractionService).allow(eq(application), eq(signedInPerson), any());
    }

    @Test
    void allowApplicationCallsServiceToAllowApplicationAddsFlashAttributeAllowed() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(allowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow"))
            .andExpect(flash().attribute("allowSuccess", true));
    }

    @Test
    void allowApplicationCallsServiceToAllowApplicationAddsFlashAttributeTemporaryAllowed() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(temporaryAllowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow"))
            .andExpect(flash().attribute("temporaryAllowSuccess", true));
    }

    @Test
    void allowApplicationRedirectsToRedirectUrlIfProvided() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(allowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow")
            .param("redirect", "/some/url"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/some/url"));
    }

    @Test
    void allowApplicationDefaultsToRedirectToApplication() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(applicationInteractionService.allow(any(), any(), any())).thenReturn(allowedApplication());

        perform(post("/web/application/" + APPLICATION_ID + "/allow"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/" + APPLICATION_ID));
    }

    @Test
    void referApplicationForUnknownApplicationIdThrowsUnknownApplicationForLeaveException() {

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/refer"))
        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    void referApplicationThrowsUnknownPersonExceptionIfNoPersonForProvidedUsername() {

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        final String username = "horst";
        when(personService.getPersonByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/refer")
                .param("username", username))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void referApplicationThrowsAccessDeniedExceptionIfNeitherBossNorDepartmentHead() {

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));
        when(personService.getSignedInUser()).thenReturn(somePerson());
        when(personService.getPersonByUsername(any())).thenReturn(Optional.of(somePerson()));

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/refer"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void referApplicationCannotBeTriggeredForOwnApplication() {

        final Person applicationPerson = somePerson();
        final Application application = applicationOfPerson(applicationPerson);

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(personService.getSignedInUser()).thenReturn(applicationPerson);
        when(personService.getPersonByUsername(any())).thenReturn(Optional.of(applicationPerson));

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/refer"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void referApplicationAccessibleForRoleBoss() throws Exception {

        final Person signedInPerson = personWithRole(BOSS);
        final Person applicationPerson = somePerson();
        final Person recipientPerson = somePerson();
        final Application application = applicationOfPerson(applicationPerson);

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(personService.getPersonByUsername(any())).thenReturn(Optional.of(recipientPerson));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInPerson, applicationPerson)).thenReturn(false);

        perform(post("/web/application/" + APPLICATION_ID + "/refer"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/" + APPLICATION_ID));

        verify(applicationInteractionService).refer(application, recipientPerson, signedInPerson);
    }

    @Test
    void referOwnApplicationAccessibleForRoleBoss() throws Exception {

        final Person signedInPerson = personWithRole(BOSS);
        final Person recipientPerson = somePerson();
        final Application application = applicationOfPerson(signedInPerson);

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(personService.getPersonByUsername(any())).thenReturn(Optional.of(recipientPerson));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        perform(post("/web/application/" + APPLICATION_ID + "/refer"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/" + APPLICATION_ID));

        verify(applicationInteractionService).refer(application, recipientPerson, signedInPerson);
    }

    @Test
    void referApplicationAccessibleForRoleDepartmentHead() throws Exception {

        final Person signedInPerson = personWithRole(DEPARTMENT_HEAD);
        final Person applicationPerson = somePerson();
        final Person recipientPerson = somePerson();
        final Application application = applicationOfPerson(applicationPerson);

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(personService.getPersonByUsername(any())).thenReturn(Optional.of(recipientPerson));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInPerson, applicationPerson)).thenReturn(true);

        perform(post("/web/application/" + APPLICATION_ID + "/refer"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/" + APPLICATION_ID));

        verify(applicationInteractionService).refer(application, recipientPerson, signedInPerson);
    }

    @Test
    void referApplicationAccessibleForRoleSecondStageAuthority() throws Exception {

        final Person signedInPerson = personWithRole(SECOND_STAGE_AUTHORITY);
        final Person applicationPerson = somePerson();
        final Person recipientPerson = somePerson();
        final Application application = applicationOfPerson(applicationPerson);

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(personService.getPersonByUsername(any())).thenReturn(Optional.of(recipientPerson));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInPerson, applicationPerson)).thenReturn(true);

        perform(post("/web/application/" + APPLICATION_ID + "/refer"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/" + APPLICATION_ID));

        verify(applicationInteractionService).refer(application, recipientPerson, signedInPerson);
    }

    @Test
    void referApplicationAccessibleForRoleOffice() throws Exception {

        final Person signedInPerson = personWithRole(OFFICE);
        final Person applicationPerson = somePerson();
        final Person recipientPerson = somePerson();
        final Application application = applicationOfPerson(applicationPerson);

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(personService.getPersonByUsername(any())).thenReturn(Optional.of(recipientPerson));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInPerson, applicationPerson)).thenReturn(true);

        perform(post("/web/application/" + APPLICATION_ID + "/refer"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/" + APPLICATION_ID));

        verify(applicationInteractionService).refer(application, recipientPerson, signedInPerson);
    }

    @Test
    void rejectApplicationForUnknownApplicationIdThrowsUnknownApplicationForLeaveException() {

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/reject"))
        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    void rejectApplicationThrowsAccessDeniedOwnApplication() {

        final Person signedInPerson = somePerson();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(signedInPerson)));

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/reject"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectApplicationThrowsAccessDeniedIfNeitherBossNorDepartmentHeadNorSecondStageAuthority() {

        final Person signedInPerson = personWithRole(OFFICE);
        final Person person = somePerson();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(person)));

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/reject"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectApplicationAccessibleForBoss() throws Exception {

        final Person signedInPerson = personWithRole(BOSS);
        final Person person = somePerson();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInPerson, person)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInPerson, person)).thenReturn(false);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(person)));

        perform(post("/web/application/" + APPLICATION_ID + "/reject"))
            .andExpect(flash().attribute("rejectSuccess", true))
            .andExpect(status().isFound());
    }

    @Test
    void rejectApplicationAccessibleForDepartmentHead() throws Exception {

        final Person signedInPerson = personWithRole(DEPARTMENT_HEAD);
        final Person person = somePerson();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInPerson, person)).thenReturn(true);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInPerson, person)).thenReturn(false);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(person)));

        perform(post("/web/application/" + APPLICATION_ID + "/reject"))
            .andExpect(flash().attribute("rejectSuccess", true))
            .andExpect(status().isFound());
    }

    @Test
    void rejectApplicationAccessibleForSecondStageAuthorityOfPerson() throws Exception {

        final Person signedInPerson = personWithRole(SECOND_STAGE_AUTHORITY);
        final Person person = somePerson();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInPerson, person)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInPerson, person)).thenReturn(true);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(person)));

        perform(post("/web/application/" + APPLICATION_ID + "/reject"))
            .andExpect(flash().attribute("rejectSuccess", true))
            .andExpect(status().isFound());
    }

    @Test
    void rejectApplicationRedirectsToApplicationIfValidationFails() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("text", "errors");
            return null;
        }).when(commentValidator).validate(any(), any());

        perform(post("/web/application/" + APPLICATION_ID + "/reject"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/" + APPLICATION_ID + "?action=reject"));

        verify(applicationInteractionService, never()).reject(any(), any(), any());
    }

    @Test
    void rejectApplicationRejectsApplicationIfValidationSuccessful() throws Exception {

        final Person signedInPerson = personWithRole(BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Application application = someApplication();
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        perform(post("/web/application/" + APPLICATION_ID + "/reject"));

        verify(applicationInteractionService).reject(eq(application), eq(signedInPerson), any());
    }

    @Test
    void rejectApplicationRedirectToUrlIfProvided() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        perform(post("/web/application/" + APPLICATION_ID + "/reject")
            .param("redirect", "/some/url"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/some/url"));
    }

    @Test
    void rejectApplicationDefaultsToRedirectToApplication() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(someApplication()));

        perform(post("/web/application/" + APPLICATION_ID + "/reject"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/" + APPLICATION_ID));
    }

    @Test
    void cancelApplicationForUnknownApplicationIdThrowsUnknownApplicationForLeaveException() {

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/cancel"))
        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    void cancelApplicationOfAnotherPersonThrowsAccessDeniedExceptionIfNotOffice() {

        when(personService.getSignedInUser()).thenReturn(personWithRole(BOSS));

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(waitingApplication()));

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/cancel"))
        ).hasCauseInstanceOf(AccessDeniedException.class);

        verify(applicationInteractionService, never()).cancel(any(), any(), any());
    }

    @Test
    void cancelApplicationOfAnotherPersonThrowsAccessDeniedExceptionIfOfficeAndApplicationInWrongState() {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));

        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(rejectedApplication()));

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/cancel"))
        ).hasCauseInstanceOf(AccessDeniedException.class);

        verify(applicationInteractionService, never()).cancel(any(), any(), any());
    }

    @Test
    void cancelApplicationOfAnotherPersonAllowedIfOfficeAndApplicationWaiting() throws Exception {

        final Person signedInPerson = personWithRole(OFFICE);
        final Application application = waitingApplication();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        perform(post("/web/application/" + APPLICATION_ID + "/cancel"))
            .andExpect(status().isFound());

        verify(applicationInteractionService).cancel(eq(application), eq(signedInPerson), any());
    }

    @Test
    void cancelApplicationOfAnotherPersonAllowedIfOfficeAndApplicationCancellationRequested() throws Exception {

        final Person signedInPerson = personWithRole(OFFICE);
        final Application application = cancellationRequestedApplication();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        perform(post("/web/application/" + APPLICATION_ID + "/cancel"))
            .andExpect(status().isFound());

        verify(applicationInteractionService).cancel(eq(application), eq(signedInPerson), any());
    }

    @Test
    void cancelApplicationOfAnotherPersonAllowedIfOfficeAndApplicationAllowed() throws Exception {

        final Person signedInPerson = personWithRole(OFFICE);
        final Application application = allowedApplication();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        perform(post("/web/application/" + APPLICATION_ID + "/cancel"))
            .andExpect(status().isFound());

        verify(applicationInteractionService).cancel(eq(application), eq(signedInPerson), any());
    }

    @Test
    void cancelApplicationOfAnotherPersonAllowedIfOfficeAndApplicationTemporaryAllowed() throws Exception {

        final Person signedInPerson = personWithRole(OFFICE);
        final Application application = temporaryAllowedApplication();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        perform(post("/web/application/" + APPLICATION_ID + "/cancel"))
            .andExpect(status().isFound());

        verify(applicationInteractionService).cancel(eq(application), eq(signedInPerson), any());
    }

    @Test
    void cancelApplicationAllowedIfSignedInUserIsApplicationUser() throws Exception {

        final Person signedInPerson = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        Application application = applicationOfPerson(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        perform(post("/web/application/" + APPLICATION_ID + "/cancel"))
            .andExpect(status().isFound());

        verify(applicationInteractionService).cancel(eq(application), eq(signedInPerson), any());
    }

    @Test
    void cancelApplicationAllowedIfSignedInUserIsApplicationUserAndDoesNotRequireApproval() throws Exception {

        final Person signedInPerson = somePerson();
        signedInPerson.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Application application = applicationOfPerson(signedInPerson);
        application.getVacationType().setRequiresApproval(false);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        perform(post("/web/application/" + APPLICATION_ID + "/cancel"))
            .andExpect(status().isFound());

        verify(applicationInteractionService).directCancel(eq(application), eq(signedInPerson), any());
    }

    @Test
    void cancelApplicationRedirectsToApplicationIfValidationFails() throws Exception {

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
            .andExpect(redirectedUrl("/web/application/" + APPLICATION_ID + "?action=cancel"));

        verify(applicationInteractionService, never()).cancel(any(), any(), any());
    }

    @Test
    void cancelApplicationRedirectsToApplication() throws Exception {

        final Person signedInPerson = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(signedInPerson)));

        perform(post("/web/application/" + APPLICATION_ID + "/cancel"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/" + APPLICATION_ID));
    }

    @Test
    void cancelCancellationRequestApplicationForUnknownApplicationIdThrowsUnknownApplicationForLeaveException() {

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/decline-cancellation-request"))
        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    void cancelCancellationRequestApplicationWithWrongRolesThrowsAccessDeniedException() {

        when(personService.getSignedInUser()).thenReturn(personWithRole(USER));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(cancellationRequestedApplication()));

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/decline-cancellation-request"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void cancelCancellationRequestApplicationWithWrongStatusThrowsAccessDeniedException() {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(waitingApplication()));

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/decline-cancellation-request"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void cancelCancellationRequestApplicationWithValidationErrors() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(cancellationRequestedApplication()));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("text", "errors");
            return null;
        }).when(commentValidator).validate(any(), any());

        perform(post("/web/application/" + APPLICATION_ID + "/decline-cancellation-request"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/" + APPLICATION_ID + "?action=decline-cancellation-request"));
    }

    @Test
    void cancelCancellationRequestApplication() throws Exception {

        final Person signedInPerson = personWithRole(OFFICE);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        final Application cancellationRequestedApplication = cancellationRequestedApplication();
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(cancellationRequestedApplication));

        perform(post("/web/application/" + APPLICATION_ID + "/decline-cancellation-request"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/" + APPLICATION_ID));

        verify(applicationInteractionService).declineCancellationRequest(eq(cancellationRequestedApplication), eq(signedInPerson), any());
    }

    @Test
    void remindBossWithUnknownApplicationIdThrowsUnknownApplicationForLeaveException() {

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/remind"))
        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    void remindBossForOwnApplication() throws Exception {

        final Person signedInPerson = somePerson();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(signedInPerson)));

        perform(post("/web/application/" + APPLICATION_ID + "/remind"))
            .andExpect(status().isFound());
    }

    @Test
    void remindBossThrowsAccessDeniedIsBossTriggers() {

        final Person signedInPerson = personWithRole(BOSS);
        final Person person = somePerson();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(person)));

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/remind"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void remindBossThrowsAccessDeniedForDepartmentHeadOfPerson() {

        final Person signedInPerson = personWithRole(DEPARTMENT_HEAD);
        final Person person = somePerson();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(person)));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInPerson, person)).thenReturn(true);

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/remind"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void remindBossThrowsAccessDeniedForSecondStageAuthorityOfPerson() {

        final Person signedInPerson = personWithRole(SECOND_STAGE_AUTHORITY);
        final Person person = somePerson();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(applicationOfPerson(person)));
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInPerson, person)).thenReturn(true);

        assertThatThrownBy(() ->
            perform(post("/web/application/" + APPLICATION_ID + "/remind"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void remindBossAddsFlashAttributeIfThrowsNoException() throws Exception {

        final Person signedInPerson = personWithRole(USER);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Application application = someApplication();
        application.setPerson(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        perform(post("/web/application/" + APPLICATION_ID + "/remind"))
            .andExpect(flash().attribute("remindIsSent", true));
    }

    @Test
    void remindBossAddsFlashAttributeIfThrowsRemindAlreadySentException() throws Exception {

        final Person signedInPerson = personWithRole(USER);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Application application = someApplication();
        application.setPerson(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(applicationInteractionService.remind(application)).thenThrow(RemindAlreadySentException.class);

        perform(post("/web/application/" + APPLICATION_ID + "/remind"))
            .andExpect(flash().attribute("remindAlreadySent", true));
    }

    @Test
    void remindBossAddsFlashAttributeIfThrowsImpatientAboutApplicationForLeaveProcessException() throws Exception {

        final Person signedInPerson = personWithRole(USER);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Application application = someApplication();
        application.setPerson(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(applicationInteractionService.remind(application)).thenThrow(ImpatientAboutApplicationForLeaveProcessException.class);

        perform(post("/web/application/" + APPLICATION_ID + "/remind"))
            .andExpect(flash().attribute("remindNoWay", true));
    }

    @Test
    void remindBossRedirectsToApplication() throws Exception {

        final Person signedInPerson = personWithRole(USER);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Application application = someApplication();
        application.setPerson(signedInPerson);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        perform(post("/web/application/" + APPLICATION_ID + "/remind"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/" + APPLICATION_ID));
    }

    private static Person somePerson() {
        return new Person();
    }

    private static Application applicationOfPerson(Person person) {
        final Application application = new Application();
        application.setPerson(person);
        application.setStartDate(LocalDate.now().plusDays(10));
        application.setEndDate(LocalDate.now().plusDays(30));
        application.setStatus(WAITING);
        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setRequiresApproval(true);
        application.setVacationType(vacationType);
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

    private static Application cancellationRequestedApplication() {

        Application application = someApplication();
        application.setStatus(ALLOWED_CANCELLATION_REQUESTED);

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
