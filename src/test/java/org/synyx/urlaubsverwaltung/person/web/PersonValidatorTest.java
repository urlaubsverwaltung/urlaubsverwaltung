package org.synyx.urlaubsverwaltung.person.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;

import java.util.ArrayList;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
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


@RunWith(MockitoJUnitRunner.class)
public class PersonValidatorTest {

    private PersonValidator sut;

    @Mock
    private PersonService personService;
    @Mock
    private Errors errors;

    private Person person;

    @Before
    public void setUp() {
        person = DemoDataCreator.createPerson();
        sut = new PersonValidator(personService);
    }

    @Test
    public void ensureSupportsOnlyPersonClass() {

        boolean returnValue;

        returnValue = sut.supports(null);
        assertThat(returnValue).isFalse();

        returnValue = sut.supports(Application.class);
        assertThat(returnValue).isFalse();

        returnValue = sut.supports(Person.class);
        assertThat(returnValue).isTrue();
    }

    @Test
    public void ensureNameMustNotBeNull() {
        sut.validateName(null, "nameField", errors);
        verify(errors).rejectValue("nameField", "error.entry.mandatory");
    }

    @Test
    public void ensureNameMustNotBeEmpty() {
        sut.validateName("", "nameField", errors);
        verify(errors).rejectValue("nameField", "error.entry.mandatory");
    }

    @Test
    public void ensureNameMustNotBeTooLong() {
        sut.validateName("AAAAAAAAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "nameField",
            errors);
        verify(errors).rejectValue("nameField", "error.entry.tooManyChars");
    }


    @Test
    public void ensureValidNameHasNoValidationError() {
        sut.validateName("Hans-Peter", "nameField", errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureEmailMustNotBeNull() {
        sut.validateEmail(null, errors);
        verify(errors).rejectValue("email", "error.entry.mandatory");
    }

    @Test
    public void ensureEmailMustNotBeEmpty() {
        sut.validateEmail("", errors);
        verify(errors).rejectValue("email", "error.entry.mandatory");
    }

    @Test
    public void ensureEmailWithoutAtIsInvalid() {
        sut.validateEmail("fraulyoner(at)verwaltung.de", errors);
        verify(errors).rejectValue("email", "error.entry.mail");
    }

    @Test
    public void ensureEmailWithMoreThanOneAtIsInvalid() {
        sut.validateEmail("fraulyoner@verw@ltung.de", errors);
        verify(errors).rejectValue("email", "error.entry.mail");
    }

    @Test
    public void ensureEmailWithAtOnInvalidPlaceIsInvalid() {
        sut.validateEmail("@fraulyonerverwaltung.de", errors);
        verify(errors).rejectValue("email", "error.entry.mail");
    }

    @Test
    public void ensureEmailWithInvalidHostNameIsInvalid() {
        sut.validateEmail("fraulyoner@verwaltungde", errors);
        verify(errors).rejectValue("email", "error.entry.mail");
    }

    @Test
    public void ensureEmailMustNotBeTooLong() {
        sut.validateEmail("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@net.de", errors);
        verify(errors).rejectValue("email", "error.entry.tooManyChars");
    }

    @Test
    public void ensureValidEmailHasNoValidationError() {
        sut.validateEmail("müller@verwaltung.com.de", errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureUsernameMustBeUnique() {
        when(personService.getPersonByUsername("foo")).thenReturn(Optional.of(DemoDataCreator.createPerson()));
        sut.validateUsername("foo", errors);
        verify(errors).rejectValue("username", "person.form.data.login.error");
    }

    @Test
    public void ensureUniqueUsernameHasNoValidationError() {
        when(personService.getPersonByUsername("foo")).thenReturn(Optional.empty());
        sut.validateUsername("foo", errors);
        verify(errors, never()).rejectValue(anyString(), anyString());
    }

    @Test
    public void ensureAtLeastOneRoleMustBeSelected() {
        person.setPermissions(new ArrayList<>());

        sut.validatePermissions(person, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.mandatory");
    }

    @Test
    public void ensureIfSelectedInactiveAsRoleNoOtherRoleCanBeSelected() {
        person.setPermissions(asList(INACTIVE, USER));

        sut.validatePermissions(person, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.inactive");
    }

    @Test
    public void ensureSelectingOnlyInactiveRoleIsValid() {
        person.setPermissions(singletonList(INACTIVE));

        sut.validatePermissions(person, errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureUserRoleMustBeSelectedIfUserShouldNotBeDeactivated() {
        person.setPermissions(singletonList(OFFICE));

        sut.validatePermissions(person, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.user");
    }

    @Test
    public void ensureDepartmentHeadRoleAndOfficeRoleCanNotBeSelectedBoth() {
        person.setPermissions(asList(USER, OFFICE, DEPARTMENT_HEAD));

        sut.validatePermissions(person, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.combination");
    }

    @Test
    public void ensureSecondStageRoleAndOfficeRoleCanNotBeSelectedBoth() {
        person.setPermissions(asList(USER, OFFICE, SECOND_STAGE_AUTHORITY));

        sut.validatePermissions(person, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.combination");
    }

    @Test
    public void ensureDepartmentHeadRoleAndBossRoleCanNotBeSelectedBoth() {
        person.setPermissions(asList(USER, BOSS, DEPARTMENT_HEAD));

        sut.validatePermissions(person, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.combination");
    }

    @Test
    public void ensureSecondStageRoleAndBossRoleCanNotBeSelectedBoth() {
        person.setPermissions(asList(USER, BOSS, SECOND_STAGE_AUTHORITY));

        sut.validatePermissions(person, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.combination");
    }

    @Test
    public void ensureValidBossRoleSelectionHasNoValidationError() {
        person.setPermissions(asList(USER, BOSS));

        sut.validatePermissions(person, errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureValidOfficeRoleSelectionHasNoValidationError() {
        person.setPermissions(asList(USER, OFFICE));

        sut.validatePermissions(person, errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureValidBossAndOfficeRoleSelectionHasNoValidationError() {
        person.setPermissions(asList(USER, BOSS, OFFICE));

        sut.validatePermissions(person, errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureSecondStageRoleAndDepartmentHeadRolesCanBeSelectedBoth() {
        person.setPermissions(asList(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        sut.validatePermissions(person, errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureDepartmentHeadMailNotificationIsOnlyValidIfDepartmentHeadRoleSelected() {
        person.setPermissions(asList(USER, BOSS));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_DEPARTMENT_HEAD));

        sut.validateNotifications(person, errors);
        verify(errors).rejectValue("notifications", "person.form.notifications.error.combination");
    }

    @Test
    public void ensureSecondStageMailNotificationIsOnlyValidIfSecondStageRoleSelected() {
        person.setPermissions(asList(USER, DEPARTMENT_HEAD));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_SECOND_STAGE_AUTHORITY));

        sut.validateNotifications(person, errors);
        verify(errors).rejectValue("notifications", "person.form.notifications.error.combination");
    }

    @Test
    public void ensureBossMailNotificationIsOnlyValidIfBossRoleSelected() {
        person.setPermissions(singletonList(USER));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL));

        sut.validateNotifications(person, errors);
        verify(errors).rejectValue("notifications", "person.form.notifications.error.combination");
    }

    @Test
    public void ensureOfficeMailNotificationIsOnlyValidIfOfficeRoleSelected() {
        person.setPermissions(asList(USER, BOSS));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL, NOTIFICATION_OFFICE));

        sut.validateNotifications(person, errors);
        verify(errors).rejectValue("notifications", "person.form.notifications.error.combination");
    }

    @Test
    public void ensureValidNotificationSelectionForDepartmentHeadHasNoValidationError() {
        person.setPermissions(asList(USER, DEPARTMENT_HEAD));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_DEPARTMENT_HEAD));

        sut.validatePermissions(person, errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureValidNotificationSelectionForSecondStageHasNoValidationError() {
        person.setPermissions(asList(USER, SECOND_STAGE_AUTHORITY));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_SECOND_STAGE_AUTHORITY));

        sut.validatePermissions(person, errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureValidNotificationSelectionForBossHasNoValidationError() {
        person.setPermissions(asList(USER, BOSS));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL));

        sut.validatePermissions(person, errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureValidNotificationSelectionForOfficeHasNoValidationError() {
        person.setPermissions(asList(USER, OFFICE));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_OFFICE));

        sut.validatePermissions(person, errors);
        verifyZeroInteractions(errors);
    }
}
