package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.ArrayList;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


@ExtendWith(MockitoExtension.class)
class PersonValidatorTest {

    private PersonValidator sut;

    @Mock
    private PersonService personService;
    @Mock
    private Errors errors;

    private Person person;

    @BeforeEach
    void setUp() {
        person = TestDataCreator.createPerson();
        sut = new PersonValidator(personService);
    }

    @Test
    void ensureSupportsOnlyPersonClass() {

        boolean returnValue;

        returnValue = sut.supports(null);
        assertThat(returnValue).isFalse();

        returnValue = sut.supports(Application.class);
        assertThat(returnValue).isFalse();

        returnValue = sut.supports(Person.class);
        assertThat(returnValue).isTrue();
    }

    @Test
    void ensureNameMustNotBeNull() {
        sut.validateName(null, "nameField", errors);
        verify(errors).rejectValue("nameField", "error.entry.mandatory");
    }

    @Test
    void ensureNameMustNotBeEmpty() {
        sut.validateName("", "nameField", errors);
        verify(errors).rejectValue("nameField", "error.entry.mandatory");
    }

    @Test
    void ensureNameMustNotBeTooLong() {
        sut.validateName("AAAAAAAAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "nameField",
            errors);
        verify(errors).rejectValue("nameField", "error.entry.tooManyChars");
    }


    @Test
    void ensureValidNameHasNoValidationError() {
        sut.validateName("Hans-Peter", "nameField", errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureEmailMustNotBeNull() {
        sut.validateEmail(null, errors);
        verify(errors).rejectValue("email", "error.entry.mandatory");
    }

    @Test
    void ensureEmailMustNotBeEmpty() {
        sut.validateEmail("", errors);
        verify(errors).rejectValue("email", "error.entry.mandatory");
    }

    @Test
    void ensureEmailWithoutAtIsInvalid() {
        sut.validateEmail("fraulyoner(at)verwaltung.de", errors);
        verify(errors).rejectValue("email", "error.entry.mail");
    }

    @Test
    void ensureEmailWithMoreThanOneAtIsInvalid() {
        sut.validateEmail("fraulyoner@verw@ltung.de", errors);
        verify(errors).rejectValue("email", "error.entry.mail");
    }

    @Test
    void ensureEmailWithAtOnInvalidPlaceIsInvalid() {
        sut.validateEmail("@fraulyonerverwaltung.de", errors);
        verify(errors).rejectValue("email", "error.entry.mail");
    }

    @Test
    void ensureEmailWithInvalidHostNameIsInvalid() {
        sut.validateEmail("fraulyoner@verwaltungde", errors);
        verify(errors).rejectValue("email", "error.entry.mail");
    }

    @Test
    void ensureEmailMustNotBeTooLong() {
        sut.validateEmail("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@net.de", errors);
        verify(errors).rejectValue("email", "error.entry.tooManyChars");
    }

    @Test
    void ensureValidEmailHasNoValidationError() {
        sut.validateEmail("müller@verwaltung.com.de", errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureUsernameMustBeUnique() {
        when(personService.getPersonByUsername("foo")).thenReturn(Optional.of(TestDataCreator.createPerson()));
        sut.validateUsername("foo", errors);
        verify(errors).rejectValue("username", "person.form.data.login.error");
    }

    @Test
    void ensureUniqueUsernameHasNoValidationError() {
        when(personService.getPersonByUsername("foo")).thenReturn(Optional.empty());
        sut.validateUsername("foo", errors);
        verify(errors, never()).rejectValue(anyString(), anyString());
    }

    @Test
    void ensureAtLeastOneRoleMustBeSelected() {
        person.setPermissions(new ArrayList<>());

        sut.validatePermissions(person, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.mandatory");
    }

    @Test
    void ensureIfSelectedInactiveAsRoleNoOtherRoleCanBeSelected() {
        person.setPermissions(asList(INACTIVE, USER));

        sut.validatePermissions(person, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.inactive");
    }

    @Test
    void ensureSelectingOnlyInactiveRoleIsValid() {
        person.setPermissions(singletonList(INACTIVE));

        sut.validatePermissions(person, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureUserRoleMustBeSelectedIfUserShouldNotBeDeactivated() {
        person.setPermissions(singletonList(OFFICE));

        sut.validatePermissions(person, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.user");
    }

    @Test
    void ensureDepartmentHeadRoleAndOfficeRoleCanNotBeSelectedBoth() {
        person.setPermissions(asList(USER, OFFICE, DEPARTMENT_HEAD));

        sut.validatePermissions(person, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.combination");
    }

    @Test
    void ensureSecondStageRoleAndOfficeRoleCanNotBeSelectedBoth() {
        person.setPermissions(asList(USER, OFFICE, SECOND_STAGE_AUTHORITY));

        sut.validatePermissions(person, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.combination");
    }

    @Test
    void ensureDepartmentHeadRoleAndBossRoleCanNotBeSelectedBoth() {
        person.setPermissions(asList(USER, BOSS, DEPARTMENT_HEAD));

        sut.validatePermissions(person, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.combination");
    }

    @Test
    void ensureSecondStageRoleAndBossRoleCanNotBeSelectedBoth() {
        person.setPermissions(asList(USER, BOSS, SECOND_STAGE_AUTHORITY));

        sut.validatePermissions(person, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.combination");
    }

    @Test
    void ensureValidBossRoleSelectionHasNoValidationError() {
        person.setPermissions(asList(USER, BOSS));

        sut.validatePermissions(person, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidOfficeRoleSelectionHasNoValidationError() {
        person.setPermissions(asList(USER, OFFICE));

        sut.validatePermissions(person, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidBossAndOfficeRoleSelectionHasNoValidationError() {
        person.setPermissions(asList(USER, BOSS, OFFICE));

        sut.validatePermissions(person, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureSecondStageRoleAndDepartmentHeadRolesCanBeSelectedBoth() {
        person.setPermissions(asList(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        sut.validatePermissions(person, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureDepartmentHeadMailNotificationIsOnlyValidIfDepartmentHeadRoleSelected() {
        person.setPermissions(asList(USER, BOSS));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_DEPARTMENT_HEAD));

        sut.validateNotifications(person, errors);
        verify(errors).rejectValue("notifications", "person.form.notifications.error.combination");
    }

    @Test
    void ensureSecondStageMailNotificationIsOnlyValidIfSecondStageRoleSelected() {
        person.setPermissions(asList(USER, DEPARTMENT_HEAD));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_SECOND_STAGE_AUTHORITY));

        sut.validateNotifications(person, errors);
        verify(errors).rejectValue("notifications", "person.form.notifications.error.combination");
    }

    @Test
    void ensureBossMailNotificationIsOnlyValidIfBossRoleSelected() {
        person.setPermissions(singletonList(USER));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL));

        sut.validateNotifications(person, errors);
        verify(errors).rejectValue("notifications", "person.form.notifications.error.combination");
    }

    @Test
    void ensureOfficeMailNotificationIsOnlyValidIfOfficeRoleSelected() {
        person.setPermissions(asList(USER, BOSS));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL, NOTIFICATION_OFFICE));

        sut.validateNotifications(person, errors);
        verify(errors).rejectValue("notifications", "person.form.notifications.error.combination");
    }

    @Test
    void ensureValidNotificationSelectionForDepartmentHeadHasNoValidationError() {
        person.setPermissions(asList(USER, DEPARTMENT_HEAD));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_DEPARTMENT_HEAD));

        sut.validatePermissions(person, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidNotificationSelectionForSecondStageHasNoValidationError() {
        person.setPermissions(asList(USER, SECOND_STAGE_AUTHORITY));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_SECOND_STAGE_AUTHORITY));

        sut.validatePermissions(person, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidNotificationSelectionForBossHasNoValidationError() {
        person.setPermissions(asList(USER, BOSS));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL));

        sut.validatePermissions(person, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidNotificationSelectionForOfficeHasNoValidationError() {
        person.setPermissions(asList(USER, OFFICE));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_OFFICE));

        sut.validatePermissions(person, errors);
        verifyNoInteractions(errors);
    }
}
