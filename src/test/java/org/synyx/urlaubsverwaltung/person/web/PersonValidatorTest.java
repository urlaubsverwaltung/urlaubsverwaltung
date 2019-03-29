package org.synyx.urlaubsverwaltung.person.web;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


public class PersonValidatorTest {

    private PersonValidator validator;

    private PersonService personService;

    private Person person;
    private Errors errors;

    @Before
    public void setUp() {

        personService = mock(PersonService.class);

        validator = new PersonValidator(personService);

        errors = mock(Errors.class);

        person = TestDataCreator.createPerson();
    }


    // TEST OF SUPPORTS METHOD

    @Test
    public void ensureSupportsOnlyPersonClass() {

        boolean returnValue;

        returnValue = validator.supports(null);
        assertFalse(returnValue);

        returnValue = validator.supports(Application.class);
        assertFalse(returnValue);

        returnValue = validator.supports(Person.class);
        assertTrue(returnValue);
    }


    // VALIDATION OF NAME FIELD

    @Test
    public void ensureNameMustNotBeNull() {

        validator.validateName(null, "nameField", errors);
        verify(errors).rejectValue("nameField", "error.entry.mandatory");
    }


    @Test
    public void ensureNameMustNotBeEmpty() {

        validator.validateName("", "nameField", errors);
        verify(errors).rejectValue("nameField", "error.entry.mandatory");
    }


    @Test
    public void ensureNameMustNotBeTooLong() {

        validator.validateName("AAAAAAAAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "nameField",
            errors);
        verify(errors).rejectValue("nameField", "error.entry.tooManyChars");
    }


    @Test
    public void ensureValidNameHasNoValidationError() {

        validator.validateName("Hans-Peter", "nameField", errors);
        verifyZeroInteractions(errors);
    }


    // VALIDATION OF EMAIL FIELD

    @Test
    public void ensureEmailMustNotBeNull() {

        validator.validateEmail(null, errors);
        verify(errors).rejectValue("email", "error.entry.mandatory");
    }


    @Test
    public void ensureEmailMustNotBeEmpty() {

        validator.validateEmail("", errors);
        verify(errors).rejectValue("email", "error.entry.mandatory");
    }


    @Test
    public void ensureEmailWithoutAtIsInvalid() {

        validator.validateEmail("fraulyoner(at)verwaltung.de", errors);
        verify(errors).rejectValue("email", "error.entry.mail");
    }


    @Test
    public void ensureEmailWithMoreThanOneAtIsInvalid() {

        validator.validateEmail("fraulyoner@verw@ltung.de", errors);
        verify(errors).rejectValue("email", "error.entry.mail");
    }


    @Test
    public void ensureEmailWithAtOnInvalidPlaceIsInvalid() {

        validator.validateEmail("@fraulyonerverwaltung.de", errors);
        verify(errors).rejectValue("email", "error.entry.mail");
    }


    @Test
    public void ensureEmailWithInvalidHostNameIsInvalid() {

        validator.validateEmail("fraulyoner@verwaltungde", errors);
        verify(errors).rejectValue("email", "error.entry.mail");
    }


    @Test
    public void ensureEmailMustNotBeTooLong() {

        validator.validateEmail("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@net.de", errors);
        verify(errors).rejectValue("email", "error.entry.tooManyChars");
    }


    @Test
    public void ensureValidEmailHasNoValidationError() {

        validator.validateEmail("müller@verwaltung.com.de", errors);
        verifyZeroInteractions(errors);
    }


    // VALIDATION OF USERNAME

    @Test
    public void ensureUsernameMustBeUnique() {

        when(personService.getPersonByLogin("foo")).thenReturn(Optional.of(TestDataCreator.createPerson()));
        validator.validateLogin("foo", errors);
        verify(errors).rejectValue("loginName", "person.form.data.login.error");
    }


    @Test
    public void ensureUniqueUsernameHasNoValidationError() {

        when(personService.getPersonByLogin("foo")).thenReturn(Optional.empty());
        validator.validateLogin("foo", errors);
        verify(errors, never()).rejectValue(anyString(), anyString());
    }


    // VALIDATION OF PERMISSIONS

    @Test
    public void ensureAtLeastOneRoleMustBeSelected() {

        person.setPermissions(new ArrayList<>());

        validator.validatePermissions(person, errors);

        verify(errors).rejectValue("permissions", "person.form.permissions.error.mandatory");
    }


    @Test
    public void ensureIfSelectedInactiveAsRoleNoOtherRoleCanBeSelected() {

        person.setPermissions(Arrays.asList(Role.INACTIVE, Role.USER));

        validator.validatePermissions(person, errors);

        verify(errors).rejectValue("permissions", "person.form.permissions.error.inactive");
    }


    @Test
    public void ensureSelectingOnlyInactiveRoleIsValid() {

        person.setPermissions(singletonList(Role.INACTIVE));

        validator.validatePermissions(person, errors);

        verifyZeroInteractions(errors);
    }


    @Test
    public void ensureUserRoleMustBeSelectedIfUserShouldNotBeDeactivated() {

        person.setPermissions(singletonList(Role.OFFICE));

        validator.validatePermissions(person, errors);

        verify(errors).rejectValue("permissions", "person.form.permissions.error.user");
    }


    @Test
    public void ensureDepartmentHeadRoleAndOfficeRoleCanNotBeSelectedBoth() {

        person.setPermissions(Arrays.asList(Role.USER, Role.OFFICE, Role.DEPARTMENT_HEAD));

        validator.validatePermissions(person, errors);

        verify(errors).rejectValue("permissions", "person.form.permissions.error.combination");
    }


    @Test
    public void ensureSecondStageRoleAndOfficeRoleCanNotBeSelectedBoth() {

        person.setPermissions(Arrays.asList(Role.USER, Role.OFFICE, Role.SECOND_STAGE_AUTHORITY));

        validator.validatePermissions(person, errors);

        verify(errors).rejectValue("permissions", "person.form.permissions.error.combination");
    }


    @Test
    public void ensureDepartmentHeadRoleAndBossRoleCanNotBeSelectedBoth() {

        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS, Role.DEPARTMENT_HEAD));

        validator.validatePermissions(person, errors);

        verify(errors).rejectValue("permissions", "person.form.permissions.error.combination");
    }


    @Test
    public void ensureSecondStageRoleAndBossRoleCanNotBeSelectedBoth() {

        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS, Role.SECOND_STAGE_AUTHORITY));

        validator.validatePermissions(person, errors);

        verify(errors).rejectValue("permissions", "person.form.permissions.error.combination");
    }


    @Test
    public void ensureValidBossRoleSelectionHasNoValidationError() {

        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        validator.validatePermissions(person, errors);

        verifyZeroInteractions(errors);
    }


    @Test
    public void ensureValidOfficeRoleSelectionHasNoValidationError() {

        person.setPermissions(Arrays.asList(Role.USER, Role.OFFICE));

        validator.validatePermissions(person, errors);

        verifyZeroInteractions(errors);
    }


    @Test
    public void ensureValidBossAndOfficeRoleSelectionHasNoValidationError() {

        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS, Role.OFFICE));

        validator.validatePermissions(person, errors);

        verifyZeroInteractions(errors);
    }


    @Test
    public void ensureSecondStageRoleAndDepartmentHeadRolesCanBeSelectedBoth() {

        person.setPermissions(Arrays.asList(Role.USER, Role.DEPARTMENT_HEAD, Role.SECOND_STAGE_AUTHORITY));

        validator.validatePermissions(person, errors);

        verifyZeroInteractions(errors);
    }


    // VALIDATION OF MAIL NOTIFICATIONS

    @Test
    public void ensureDepartmentHeadMailNotificationIsOnlyValidIfDepartmentHeadRoleSelected() {

        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS));
        person.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER,
                MailNotification.NOTIFICATION_DEPARTMENT_HEAD));

        validator.validateNotifications(person, errors);

        verify(errors).rejectValue("notifications", "person.form.notifications.error.combination");
    }


    @Test
    public void ensureSecondStageMailNotificationIsOnlyValidIfSecondStageRoleSelected() {

        person.setPermissions(Arrays.asList(Role.USER, Role.DEPARTMENT_HEAD));
        person.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER,
                MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY));

        validator.validateNotifications(person, errors);

        verify(errors).rejectValue("notifications", "person.form.notifications.error.combination");
    }


    @Test
    public void ensureBossMailNotificationIsOnlyValidIfBossRoleSelected() {

        person.setPermissions(singletonList(Role.USER));
        person.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS));

        validator.validateNotifications(person, errors);

        verify(errors).rejectValue("notifications", "person.form.notifications.error.combination");
    }


    @Test
    public void ensureOfficeMailNotificationIsOnlyValidIfOfficeRoleSelected() {

        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS));
        person.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS,
                MailNotification.NOTIFICATION_OFFICE));

        validator.validateNotifications(person, errors);

        verify(errors).rejectValue("notifications", "person.form.notifications.error.combination");
    }


    @Test
    public void ensureValidNotificationSelectionForDepartmentHeadHasNoValidationError() {

        person.setPermissions(Arrays.asList(Role.USER, Role.DEPARTMENT_HEAD));
        person.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER,
                MailNotification.NOTIFICATION_DEPARTMENT_HEAD));

        validator.validatePermissions(person, errors);

        verifyZeroInteractions(errors);
    }


    @Test
    public void ensureValidNotificationSelectionForSecondStageHasNoValidationError() {

        person.setPermissions(Arrays.asList(Role.USER, Role.SECOND_STAGE_AUTHORITY));
        person.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER,
                MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY));

        validator.validatePermissions(person, errors);

        verifyZeroInteractions(errors);
    }


    @Test
    public void ensureValidNotificationSelectionForBossHasNoValidationError() {

        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS));
        person.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS));

        validator.validatePermissions(person, errors);

        verifyZeroInteractions(errors);
    }


    @Test
    public void ensureValidNotificationSelectionForOfficeHasNoValidationError() {

        person.setPermissions(Arrays.asList(Role.USER, Role.OFFICE));
        person.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER,
                MailNotification.NOTIFICATION_OFFICE));

        validator.validatePermissions(person, errors);

        verifyZeroInteractions(errors);
    }
}
