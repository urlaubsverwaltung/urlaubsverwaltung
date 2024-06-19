package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_CONVERTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_EDITED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_REJECTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_REVOKED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_UPCOMING;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

class MailNotificationTest {

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED_isValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED.isValidWith(List.of(USER, role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED_isNotValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED.isValidWith(List.of(USER, role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED_isValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED.isValidWith(List.of(USER, role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED_isNotValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED.isValidWith(List.of(USER, role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED_isValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED.isValidWith(List.of(USER, role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED_isNotValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED.isValidWith(List.of(USER, role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED_isValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED.isValidWith(List.of(USER, role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED_isNotValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED.isValidWith(List.of(USER, role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED_isValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED.isValidWith(List.of(USER, role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED_isNotValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED.isValidWith(List.of(USER, role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_isValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION.isValidWith(List.of(USER, role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_isNotValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION.isValidWith(List.of(USER, role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED_isValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED.isValidWith(List.of(USER, role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED_isNotValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED.isValidWith(List.of(USER, role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED_isValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED.isValidWith(List.of(USER, role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED_isNotValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED.isValidWith(List.of(USER, role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER_isValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER.isValidWith(List.of(USER, role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER_isNotValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER.isValidWith(List.of(USER, role))).isFalse();
    }

    @Test
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED_isValidForUserAndOffice() {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED.isValidWith(List.of(USER, OFFICE))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED_isValidForUserAndAPPLICATION_CANCELLATION_REQUESTEDWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED.isValidWith(List.of(USER, APPLICATION_CANCELLATION_REQUESTED, role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED_isNotValidWithoutAPPLICATION_CANCELLATION_REQUESTED(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED.isValidWith(List.of(USER, role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_APPLIED_IsValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_APPLIED.isValidWith(List.of(role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_APPLIED_IsNotValidWithRole(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_APPLIED.isValidWith(List.of(role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_ALLOWED_IsValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_ALLOWED.isValidWith(List.of(role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_ALLOWED_IsNotValidWithRole(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_ALLOWED.isValidWith(List.of(role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_REVOKED_IsValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_REVOKED.isValidWith(List.of(role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_REVOKED_IsNotValidWithRole(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_REVOKED.isValidWith(List.of(role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_REJECTED_IsValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_REJECTED.isValidWith(List.of(role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_REJECTED_IsNotValidWithRole(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_REJECTED.isValidWith(List.of(role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED_IsValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED.isValidWith(List.of(role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED_IsNotValidWithRole(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED.isValidWith(List.of(role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_CANCELLATION_IsValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION.isValidWith(List.of(role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_CANCELLATION_IsNotValidWithRole(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION.isValidWith(List.of(role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_EDITED_IsValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_EDITED.isValidWith(List.of(role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_EDITED_IsNotValidWithRole(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_EDITED.isValidWith(List.of(role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_CONVERTED_IsValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_CONVERTED.isValidWith(List.of(role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_CONVERTED_IsNotValidWithRole(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_CONVERTED.isValidWith(List.of(role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_UPCOMING_IsValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_UPCOMING.isValidWith(List.of(role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_UPCOMING_IsNotValidWithRole(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_UPCOMING.isValidWith(List.of(role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_IsValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT.isValidWith(List.of(role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_IsNotValidWithRole(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT.isValidWith(List.of(role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"})
    void ensureNOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING_IsValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING.isValidWith(List.of(role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING_IsNotValidWithRole(Role role) {
        assertThat(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING.isValidWith(List.of(role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
    void ensureNOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL_IsValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL.isValidWith(List.of(USER, role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "INACTIVE", "APPLICATION_ADD", "APPLICATION_CANCEL", "APPLICATION_CANCELLATION_REQUESTED", "SICK_NOTE_VIEW", "SICK_NOTE_ADD", "SICK_NOTE_EDIT", "SICK_NOTE_CANCEL", "SICK_NOTE_COMMENT"})
    void ensureNOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL_IsNotValidWithRole(Role role) {
        assertThat(NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL.isValidWith(List.of(USER, role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureNOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED_IsValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED.isValidWith(List.of(USER, role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"INACTIVE", "APPLICATION_ADD", "APPLICATION_CANCEL", "APPLICATION_CANCELLATION_REQUESTED", "SICK_NOTE_VIEW", "SICK_NOTE_ADD", "SICK_NOTE_EDIT", "SICK_NOTE_CANCEL", "SICK_NOTE_COMMENT"})
    void ensureNOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED_IsNotValidWithRole(Role role) {
        assertThat(NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED.isValidWith(List.of(USER, role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"})
    void ensureNOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT_IsValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT.isValidWith(List.of(role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT_IsNotValidWithRole(Role role) {
        assertThat(NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT.isValidWith(List.of(role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"})
    void ensureNOTIFICATION_EMAIL_OVERTIME_APPLIED_IsValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_OVERTIME_APPLIED.isValidWith(List.of(role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_OVERTIME_APPLIED_IsNotValidWithRole(Role role) {
        assertThat(NOTIFICATION_EMAIL_OVERTIME_APPLIED.isValidWith(List.of(role))).isFalse();
    }

    @Test
    void ensureNOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT_IsValidWithOffice() {
        assertThat(NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT.isValidWith(List.of(USER, OFFICE))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS"})
    void ensureNOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT_IsValidWith(Role role) {
        assertThat(NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT.isValidWith(List.of(USER, SICK_NOTE_ADD, role))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "OFFICE"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT_IsNotValidWithoutRole(Role role) {
        assertThat(NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT.isValidWith(List.of(USER, SICK_NOTE_ADD, role))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE"}, mode = EXCLUDE)
    void ensureNOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT_IsNotValidWithoutOffice(Role role) {
        assertThat(NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT.isValidWith(List.of(role))).isFalse();
    }
}
