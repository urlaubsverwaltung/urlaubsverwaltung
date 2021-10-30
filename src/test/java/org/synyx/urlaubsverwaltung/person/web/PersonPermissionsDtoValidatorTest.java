package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.application.Application;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
class PersonPermissionsDtoValidatorTest {

    private PersonPermissionsDtoValidator sut;

    @Mock
    private Errors errors;

    @BeforeEach
    void setUp() {
        sut = new PersonPermissionsDtoValidator();
    }

    @Test
    void ensureSupportsOnlyPersonClass() {

        boolean returnValue;

        returnValue = sut.supports(null);
        assertThat(returnValue).isFalse();

        returnValue = sut.supports(Application.class);
        assertThat(returnValue).isFalse();

        returnValue = sut.supports(PersonPermissionsDto.class);
        assertThat(returnValue).isTrue();
    }

    @Test
    void ensureAtLeastOneRoleMustBeSelected() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(new ArrayList<>());

        sut.validatePermissions(personPermissionsDto, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.mandatory");
    }

    @Test
    void ensureAtLeastOneRoleMustBeSelectedButNull() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(null);

        sut.validatePermissions(personPermissionsDto, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.mandatory");
    }

    @Test
    void ensureIfSelectedInactiveAsRoleNoOtherRoleCanBeSelected() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(asList(INACTIVE, USER));

        sut.validatePermissions(personPermissionsDto, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.inactive");
    }

    @Test
    void ensureSelectingOnlyInactiveRoleIsValid() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(singletonList(INACTIVE));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureUserRoleMustBeSelectedIfUserShouldNotBeDeactivated() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(singletonList(OFFICE));

        sut.validatePermissions(personPermissionsDto, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.user");
    }

    @Test
    void ensureDepartmentHeadRoleAndOfficeRoleCanNotBeSelectedBoth() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(asList(USER, OFFICE, DEPARTMENT_HEAD));

        sut.validatePermissions(personPermissionsDto, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.combination.departmentHead");
    }

    @Test
    void ensureSecondStageRoleAndOfficeRoleCanNotBeSelectedBoth() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(asList(USER, OFFICE, SECOND_STAGE_AUTHORITY));

        sut.validatePermissions(personPermissionsDto, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.combination.secondStage");
    }

    @Test
    void ensureDepartmentHeadRoleAndBossRoleCanNotBeSelectedBoth() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(asList(USER, BOSS, DEPARTMENT_HEAD));

        sut.validatePermissions(personPermissionsDto, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.combination.departmentHead");
    }

    @Test
    void ensureSecondStageRoleAndBossRoleCanNotBeSelectedBoth() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(asList(USER, BOSS, SECOND_STAGE_AUTHORITY));

        sut.validatePermissions(personPermissionsDto, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.combination.secondStage");
    }

    @Test
    void ensureValidBossRoleSelectionHasNoValidationError() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(asList(USER, BOSS));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidOfficeRoleSelectionHasNoValidationError() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(asList(USER, OFFICE));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidBossAndOfficeRoleSelectionHasNoValidationError() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(asList(USER, BOSS, OFFICE));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureSecondStageRoleAndDepartmentHeadRolesCanBeSelectedBoth() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(asList(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureNoErrorInNotificationsIfPermissionsNotGiven() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(null);

        sut.validateNotifications(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureDepartmentHeadMailNotificationIsOnlyValidIfDepartmentHeadRoleSelected() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(asList(USER, BOSS));
        personPermissionsDto.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_DEPARTMENT_HEAD));

        sut.validateNotifications(personPermissionsDto, errors);
        verify(errors).rejectValue("notifications", "person.form.notifications.error.combination");
    }

    @Test
    void ensureSecondStageMailNotificationIsOnlyValidIfSecondStageRoleSelected() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(asList(USER, DEPARTMENT_HEAD));
        personPermissionsDto.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_SECOND_STAGE_AUTHORITY));

        sut.validateNotifications(personPermissionsDto, errors);
        verify(errors).rejectValue("notifications", "person.form.notifications.error.combination");
    }

    @Test
    void ensureBossMailNotificationIsOnlyValidIfBossRoleSelected() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(singletonList(USER));
        personPermissionsDto.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL));

        sut.validateNotifications(personPermissionsDto, errors);
        verify(errors).rejectValue("notifications", "person.form.notifications.error.combination");
    }

    @Test
    void ensureOfficeMailNotificationIsOnlyValidIfOfficeRoleSelected() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(asList(USER, BOSS));
        personPermissionsDto.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL, NOTIFICATION_OFFICE));

        sut.validateNotifications(personPermissionsDto, errors);
        verify(errors).rejectValue("notifications", "person.form.notifications.error.combination");
    }

    @Test
    void ensureValidNotificationSelectionForDepartmentHeadHasNoValidationError() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(asList(USER, DEPARTMENT_HEAD));
        personPermissionsDto.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_DEPARTMENT_HEAD));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidNotificationSelectionForSecondStageHasNoValidationError() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(asList(USER, SECOND_STAGE_AUTHORITY));
        personPermissionsDto.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_SECOND_STAGE_AUTHORITY));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidNotificationSelectionForBossHasNoValidationError() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(asList(USER, BOSS));
        personPermissionsDto.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidNotificationSelectionForOfficeHasNoValidationError() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(asList(USER, OFFICE));
        personPermissionsDto.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_OFFICE));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }
}
