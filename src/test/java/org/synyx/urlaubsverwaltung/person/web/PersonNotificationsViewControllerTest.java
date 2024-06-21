package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.notification.UserNotificationSettings;
import org.synyx.urlaubsverwaltung.notification.UserNotificationSettingsService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class PersonNotificationsViewControllerTest {

    private PersonNotificationsViewController sut;

    private static final int UNKNOWN_PERSON_ID = 675;

    @Mock
    private PersonService personService;
    @Mock
    private PersonNotificationsDtoValidator validator;
    @Mock
    private UserNotificationSettingsService userNotificationSettingsService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new PersonNotificationsViewController(personService, validator, userNotificationSettingsService, departmentService, settingsService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/web/person/{personId}/notifications", "/web/person/{personId}/notifications/departments"})
    void ensureModelAttributesWhenThereAreNoDepartments(String givenUrl) throws Exception {

        final Person signedInPerson = personWithId(42);
        signedInPerson.setFirstName("Office");
        signedInPerson.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person person = personWithId(1);
        person.setFirstName("Hans");
        person.setPermissions(List.of(USER));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final UserNotificationSettings userNotificationSettings = new UserNotificationSettings(new PersonId(1L), true);
        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L))).thenReturn(userNotificationSettings);

        when(departmentService.getNumberOfDepartments()).thenReturn(0L);

        userIsAllowedToSubmitSickNotes(false);

        perform(get(givenUrl, 1))
            .andExpect(status().isOk())
            .andExpect(model().attribute("departmentsAvailable", is(false)))
            .andExpect(model().attribute("personAssignedToDepartments", is(false)));

        verifyNoMoreInteractions(departmentService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/web/person/{personId}/notifications", "/web/person/{personId}/notifications/departments"})
    void ensureModelAttributesWhenThereAreDepartmentsAndPersonHasAssignedDepartments(String givenUrl) throws Exception {

        final Person signedInPerson = personWithId(42);
        signedInPerson.setFirstName("Office");
        signedInPerson.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person person = personWithId(1);
        person.setFirstName("Hans");
        person.setPermissions(List.of(USER));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final UserNotificationSettings userNotificationSettings = new UserNotificationSettings(new PersonId(1L), true);
        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L))).thenReturn(userNotificationSettings);

        when(departmentService.getNumberOfDepartments()).thenReturn(4L);
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of(new Department()));

        userIsAllowedToSubmitSickNotes(false);

        perform(get(givenUrl, 1))
            .andExpect(status().isOk())
            .andExpect(model().attribute("departmentsAvailable", is(true)))
            .andExpect(model().attribute("personAssignedToDepartments", is(true)));

        verifyNoMoreInteractions(departmentService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/web/person/{personId}/notifications", "/web/person/{personId}/notifications/departments"})
    void ensureModelAttributesWhenThereAreDepartmentsButPersonHasNoAssignedDepartments(String givenUrl) throws Exception {

        final Person signedInPerson = personWithId(42);
        signedInPerson.setFirstName("Office");
        signedInPerson.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person person = personWithId(1);
        person.setFirstName("Hans");
        person.setPermissions(List.of(USER));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final UserNotificationSettings userNotificationSettings = new UserNotificationSettings(new PersonId(1L), true);
        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L))).thenReturn(userNotificationSettings);

        when(departmentService.getNumberOfDepartments()).thenReturn(4L);
        when(departmentService.getDepartmentsPersonHasAccessTo(person)).thenReturn(List.of());

        userIsAllowedToSubmitSickNotes(false);

        perform(get(givenUrl, 1))
            .andExpect(status().isOk())
            .andExpect(model().attribute("departmentsAvailable", is(true)))
            .andExpect(model().attribute("personAssignedToDepartments", is(false)));
    }

    @Test
    void ensuresThatOnlyVisibleAndActive() throws Exception {

        final Person person = personWithId(1);
        person.setFirstName("Hans");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));
        person.setPermissions(List.of(USER));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        final UserNotificationSettings userNotificationSettings = new UserNotificationSettings(new PersonId(1L), true);
        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L))).thenReturn(userNotificationSettings);

        userIsAllowedToSubmitSickNotes(false);

        perform(get("/web/person/{personId}/notifications", 1))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personId", is(1L))))
            .andExpect(model().attribute("personNotificationsDto",
                hasProperty("restrictToDepartments", allOf(
                    hasProperty("visible", is(false)),
                    hasProperty("active", is(true))
                ))
            ))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedForManagement", hasProperty("active", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationTemporaryAllowedForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationTemporaryAllowedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAllowedForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAllowedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAdaptedForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAdaptedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationWaitingReminderForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationWaitingReminderForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationRequestedForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationRequestedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteCreatedByManagementForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteCreatedByManagementForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteSubmittedByUserForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteSubmittedByUserForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteAcceptedByManagementForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteAcceptedByManagementForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedAndChanges", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedAndChanges", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationUpcoming", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationUpcoming", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacementUpcoming", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacementUpcoming", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personNewManagementAll", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personNewManagementAll", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedByManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedByManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeApplied", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeApplied", hasProperty("active", is(false)))));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensuresThatOnlyVisibleAndActiveForDepartmentManagersWithoutSpecialPermissions(final Role role) throws Exception {

        final Person person = personWithId(1);
        person.setFirstName("Hans");
        person.setNotifications(List.of());
        person.setPermissions(List.of(USER, role));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L)))
            .thenReturn(new UserNotificationSettings(new PersonId(1L), false));

        userIsAllowedToSubmitSickNotes(false);

        perform(get("/web/person/{personId}/notifications", 1))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personId", is(1L))))
            .andExpect(model().attribute("personNotificationsDto",
                hasProperty("restrictToDepartments", allOf(
                    hasProperty("visible", is(false)),
                    hasProperty("active", is(false))
                ))
            ))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationTemporaryAllowedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationTemporaryAllowedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAllowedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAllowedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAdaptedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAdaptedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationWaitingReminderForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationWaitingReminderForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationRequestedForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationRequestedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteCreatedByManagementForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteCreatedByManagementForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteSubmittedByUserForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteSubmittedByUserForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteAcceptedByManagementForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteAcceptedByManagementForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedAndChanges", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedAndChanges", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationUpcoming", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationUpcoming", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacementUpcoming", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacementUpcoming", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personNewManagementAll", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personNewManagementAll", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedByManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedByManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeApplied", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeApplied", hasProperty("active", is(false)))));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensuresThatOnlyVisibleAndActiveForDepartmentManagersWithoutSpecialPermissionCancellationRequested(final Role role) throws Exception {

        final Person person = personWithId(1);
        person.setFirstName("Hans");
        person.setNotifications(List.of());
        person.setPermissions(List.of(USER, role, APPLICATION_CANCELLATION_REQUESTED));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L)))
            .thenReturn(new UserNotificationSettings(new PersonId(1L), false));

        userIsAllowedToSubmitSickNotes(false);

        perform(get("/web/person/{personId}/notifications", 1))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personId", is(1L))))
            .andExpect(model().attribute("personNotificationsDto",
                hasProperty("restrictToDepartments", allOf(
                    hasProperty("visible", is(false)),
                    hasProperty("active", is(false))
                ))
            ))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationTemporaryAllowedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationTemporaryAllowedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAllowedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAllowedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAdaptedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAdaptedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationWaitingReminderForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationWaitingReminderForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationRequestedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationRequestedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteCreatedByManagementForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteCreatedByManagementForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteSubmittedByUserForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteSubmittedByUserForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteAcceptedByManagementForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteAcceptedByManagementForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedAndChanges", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedAndChanges", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationUpcoming", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationUpcoming", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacementUpcoming", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacementUpcoming", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personNewManagementAll", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personNewManagementAll", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedByManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedByManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeApplied", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeApplied", hasProperty("active", is(false)))));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS"})
    void ensuresThatOnlyVisibleAndActiveForBoss(final Role role) throws Exception {

        final Person person = personWithId(1);
        person.setFirstName("Hans");
        person.setNotifications(List.of());
        person.setPermissions(List.of(USER, role));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L)))
            .thenReturn(new UserNotificationSettings(new PersonId(1L), false));

        userIsAllowedToSubmitSickNotes(false);

        perform(get("/web/person/{personId}/notifications", 1))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personId", is(1L))))
            .andExpect(model().attribute("personNotificationsDto",
                hasProperty("restrictToDepartments", allOf(
                    hasProperty("visible", is(true)),
                    hasProperty("active", is(false))
                ))
            ))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationTemporaryAllowedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationTemporaryAllowedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAllowedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAllowedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAdaptedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAdaptedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationWaitingReminderForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationWaitingReminderForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationRequestedForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationRequestedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteCreatedByManagementForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteCreatedByManagementForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteSubmittedByUserForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteSubmittedByUserForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteAcceptedByManagementForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteAcceptedByManagementForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedAndChanges", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedAndChanges", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationUpcoming", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationUpcoming", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacementUpcoming", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacementUpcoming", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personNewManagementAll", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personNewManagementAll", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedByManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedByManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeApplied", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeApplied", hasProperty("active", is(false)))));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensuresRestrictToDepartmentsIsVisibleForRole(final Role role) throws Exception {

        final Person person = personWithId(1);
        person.setFirstName("Hans");
        person.setNotifications(List.of());
        person.setPermissions(List.of(USER, role));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L)))
            .thenReturn(new UserNotificationSettings(new PersonId(1L), false));

        userIsAllowedToSubmitSickNotes(false);

        perform(get("/web/person/{personId}/notifications", 1))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personId", is(1L))))
            .andExpect(model().attribute("personNotificationsDto",
                hasProperty("restrictToDepartments", allOf(
                    hasProperty("visible", is(true)),
                    hasProperty("active", is(false))
                ))
            ));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensuresRestrictToDepartmentsIsNotVisibleForRole(final Role role) throws Exception {

        final Person person = personWithId(1);
        person.setFirstName("Hans");
        person.setNotifications(List.of());
        person.setPermissions(List.of(USER, role));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L)))
            .thenReturn(new UserNotificationSettings(new PersonId(1L), false));

        userIsAllowedToSubmitSickNotes(false);

        perform(get("/web/person/{personId}/notifications", 1))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personId", is(1L))))
            .andExpect(model().attribute("personNotificationsDto",
                hasProperty("restrictToDepartments", allOf(
                    hasProperty("visible", is(false)),
                    hasProperty("active", is(false))
                ))
            ));
    }

    @Test
    void ensuresThatOnlyVisibleAndActiveForBossWithSpecialPermissionCancellationRequested() throws Exception {

        final Person person = personWithId(1);
        person.setFirstName("Hans");
        person.setNotifications(List.of());
        person.setPermissions(List.of(USER, BOSS, APPLICATION_CANCELLATION_REQUESTED));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L)))
            .thenReturn(new UserNotificationSettings(new PersonId(1L), false));

        userIsAllowedToSubmitSickNotes(false);

        perform(get("/web/person/{personId}/notifications", 1))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personId", is(1L))))
            .andExpect(model().attribute("personNotificationsDto",
                hasProperty("restrictToDepartments", allOf(
                    hasProperty("visible", is(true)),
                    hasProperty("active", is(false))
                ))
            ))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationTemporaryAllowedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationTemporaryAllowedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAllowedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAllowedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAdaptedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAdaptedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationWaitingReminderForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationWaitingReminderForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationRequestedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationRequestedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteCreatedByManagementForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteCreatedByManagementForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteSubmittedByUserForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteSubmittedByUserForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteAcceptedByManagementForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteAcceptedByManagementForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedAndChanges", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedAndChanges", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationUpcoming", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationUpcoming", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacementUpcoming", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacementUpcoming", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personNewManagementAll", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personNewManagementAll", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedByManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedByManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeApplied", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeApplied", hasProperty("active", is(false)))));
    }

    @Test
    void ensuresThatOnlyVisibleAndActiveForOffice() throws Exception {

        final Person person = personWithId(1);
        person.setFirstName("Hans");
        person.setNotifications(List.of());
        person.setPermissions(List.of(USER, OFFICE));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L)))
            .thenReturn(new UserNotificationSettings(new PersonId(1L), false));

        userIsAllowedToSubmitSickNotes(false);

        perform(get("/web/person/{personId}/notifications", 1))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personId", is(1L))))
            .andExpect(model().attribute("personNotificationsDto",
                hasProperty("restrictToDepartments", allOf(
                    hasProperty("visible", is(true)),
                    hasProperty("active", is(false))
                ))
            ))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationTemporaryAllowedForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationTemporaryAllowedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAllowedForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAllowedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAdaptedForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAdaptedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationWaitingReminderForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationWaitingReminderForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationRequestedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationCancellationRequestedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteCreatedByManagementForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteCreatedByManagementForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteSubmittedByUserForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteSubmittedByUserForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteAcceptedByManagementForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteAcceptedByManagementForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedAndChanges", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationAppliedAndChanges", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationUpcoming", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationUpcoming", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacementUpcoming", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacementUpcoming", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personNewManagementAll", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personNewManagementAll", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedByManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeAppliedByManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeApplied", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeApplied", hasProperty("active", is(false)))));
    }

    @Test
    void ensuresThatOnlyVisibleAndActiveAndUserIsNotAllowedToSubmitSickNotesForOffice() throws Exception {

        final Person person = personWithId(1);
        person.setFirstName("Hans");
        person.setNotifications(List.of());
        person.setPermissions(List.of(USER, OFFICE));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L)))
            .thenReturn(new UserNotificationSettings(new PersonId(1L), false));

        userIsAllowedToSubmitSickNotes(false);

        perform(get("/web/person/{personId}/notifications", 1))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personId", is(1L))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteSubmittedByUserForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteSubmittedByUserForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteAcceptedByManagementForManagement", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteAcceptedByManagementForManagement", hasProperty("active", is(false)))))
        ;
    }

    @Test
    void ensuresThatOnlyVisibleAndActiveAndUserIsAllowedToSubmitSickNotesForOffice() throws Exception {

        final Person person = personWithId(1);
        person.setFirstName("Hans");
        person.setNotifications(List.of());
        person.setPermissions(List.of(USER, OFFICE));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L)))
            .thenReturn(new UserNotificationSettings(new PersonId(1L), false));

        userIsAllowedToSubmitSickNotes(true);

        perform(get("/web/person/{personId}/notifications", 1))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personId", is(1L))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteSubmittedByUserForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteSubmittedByUserForManagement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteAcceptedByManagementForManagement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("sickNoteAcceptedByManagementForManagement", hasProperty("active", is(false)))))
            ;
    }

    @Test
    void showPersonNotificationsForUnknownIdThrowsUnknownPersonException() {
        assertThatThrownBy(() ->
            perform(get("/web/person/{personId}/notifications", UNKNOWN_PERSON_ID)))
            .hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void editPersonNotificationsForUnknownIdThrowsUnknownPersonException() {
        assertThatThrownBy(() ->
            perform(post("/web/person/{personId}/notifications", UNKNOWN_PERSON_ID)))
            .hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void ensuresWhenEditingPersonNotificationsThatItWillBeSavedAndRedirected() throws Exception {

        final Person personWithoutNotifications = new Person();
        personWithoutNotifications.setId(1L);
        personWithoutNotifications.setFirstName("Hans");
        personWithoutNotifications.setNotifications(List.of());
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(personWithoutNotifications));

        perform(
            post("/web/person/{personId}/notifications", 1)
                .param("personId", "1")
                .param("restrictToDepartments.active", "false")
                .param("applicationAppliedAndChanges.visible", "true")
                .param("applicationAppliedAndChanges.active", "true")
        )
            .andExpect(redirectedUrl("/web/person/1/notifications"))
            .andExpect(flash().attribute("success", true));

        final ArgumentCaptor<Person> personArgumentCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personService).update(personArgumentCaptor.capture());
        final Person savedPerson = personArgumentCaptor.getValue();
        assertThat(savedPerson.getNotifications()).contains(NOTIFICATION_EMAIL_APPLICATION_APPLIED);

        verify(userNotificationSettingsService).updateNotificationSettings(new PersonId(1L), false);
    }

    @Test
    void ensuresWhenEditingPersonNotificationsThatItWillUpdateNotificationSettings() throws Exception {

        final Person personWithoutNotifications = new Person();
        personWithoutNotifications.setId(1L);
        personWithoutNotifications.setFirstName("Hans");
        personWithoutNotifications.setNotifications(List.of());
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(personWithoutNotifications));

        perform(
            post("/web/person/{personId}/notifications", 1)
                .param("personId", "1")
                .param("restrictToDepartments.active", "true")
                .param("applicationAppliedAndChanges.visible", "true")
                .param("applicationAppliedAndChanges.active", "true")
        )
            .andExpect(redirectedUrl("/web/person/1/notifications"))
            .andExpect(flash().attribute("success", true));

        verify(userNotificationSettingsService).updateNotificationSettings(new PersonId(1L), true);
    }

    @Test
    void ensuresWhenEditingPersonNotificationsWithMismatchingIdReturnsNotFoundStatus() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(post("/web/person/{personId}/notifications", 1)
            .param("personId", "2"))
            .andExpect(status().isNotFound());
    }

    @Test
    void ensuresWhenEditingPersonNotificationsHasValidationError() throws Exception {

        final Person personWithoutNotifications = new Person();
        personWithoutNotifications.setId(1L);
        personWithoutNotifications.setFirstName("Hans");
        personWithoutNotifications.setNotifications(List.of());
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(personWithoutNotifications));

        doAnswer(invocation -> {
            final Errors errors = invocation.getArgument(1);
            errors.reject("errors");
            return null;
        }).when(validator).validate(any(), any());

        userIsAllowedToSubmitSickNotes(false);

        perform(post("/web/person/{personId}/notifications", 1)
            .param("personId", "1")
            .param("applicationAppliedAndChanges.visible", "true")
            .param("applicationAppliedAndChanges.active", "true")
        )
            .andExpect(model().attribute("error", true))
            .andExpect(view().name("person/person_notifications"));

        verify(personService, never()).update(personWithoutNotifications);
    }

    @Test
    void ensureSickNoteSubmissionOnDepartmentNotifications() throws Exception {

        final Person person = personWithId(1);
        person.setFirstName("Hans");
        person.setNotifications(List.of());
        person.setPermissions(List.of(USER, OFFICE));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L)))
            .thenReturn(new UserNotificationSettings(new PersonId(1L), true));

        userIsAllowedToSubmitSickNotes(true);

        perform(get("/web/person/{personId}/notifications/departments", 1))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personId", is(1L))));
    }

    private static Person personWithId(long personId) {
        final Person person = new Person();
        person.setId(personId);
        return person;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }

    private void userIsAllowedToSubmitSickNotes(boolean userIsAllowedToSubmit) {
        final Settings settings = new Settings();
        final SickNoteSettings sickNoteSettings = new SickNoteSettings();
        sickNoteSettings.setUserIsAllowedToSubmitSickNotes(userIsAllowedToSubmit);
        settings.setSickNoteSettings(sickNoteSettings);
        when(settingsService.getSettings()).thenReturn(settings);
    }
}
