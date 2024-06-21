package org.synyx.urlaubsverwaltung.person;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * Describes which kind of mail notifications a person can have.
 */
public enum MailNotification {

    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED(true, false, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED(true, false, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED(true, false, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED(true, false, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED(true, false, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION(true, false, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED(true, false, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED(true, false, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER(true, false, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),

    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED(true, false, hasRole(USER).and(hasRole(OFFICE).or(hasRole(APPLICATION_CANCELLATION_REQUESTED).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))))),

    NOTIFICATION_EMAIL_APPLICATION_APPLIED(false, false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_ALLOWED(false, false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_REVOKED(false, false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_REJECTED(false, false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED(false, false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_CANCELLATION(false, false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_EDITED(false, false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_CONVERTED(false, false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_UPCOMING(false, false, hasRole(USER)),

    NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT(false, false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING(false, false, hasRole(USER)),

    NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED(true, false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION(true, false, hasRole(USER)),

    NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL(false, false, hasRole(USER).and(hasAnyRole(BOSS, OFFICE))),

    NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED(true, false, hasRole(USER).and(hasAnyRole(OFFICE, BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT(false, false, hasRole(USER)),
    NOTIFICATION_EMAIL_OVERTIME_APPLIED(false, false, hasRole(USER)),

    NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_USER(false, true, hasRole(USER)),
    NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT(true, true, hasRole(USER).and(hasAnyRole(OFFICE).or(hasRole(SICK_NOTE_ADD).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))))),
    NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT(false, false, hasRole(USER)),
    NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT_TO_MANAGEMENT(true, false, hasRole(USER).and(hasRole(OFFICE).or(hasRole(SICK_NOTE_ADD).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))))),
    NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_USER(false, true, hasRole(USER)),
    NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_MANAGEMENT(true, true, hasRole(USER).and(hasAnyRole(OFFICE).or(hasRole(SICK_NOTE_ADD).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))))),
    NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT(false, false, hasRole(USER)),
    NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT(false, false, hasRole(USER)),
    NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED(true, false, hasRole(USER)),
    NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED(true, false, hasRole(USER));

    private final boolean departmentRelated;
    private final boolean sickNoteSubmissionRelated;
    private final Predicate<Collection<Role>> isValid;

    MailNotification(boolean departmentRelated, boolean sickNoteSubmissionRelated, Predicate<Collection<Role>> isValid) {
        this.departmentRelated = departmentRelated;
        this.sickNoteSubmissionRelated = sickNoteSubmissionRelated;
        this.isValid = isValid;
    }

    /**
     * Whether this mail notification is department related or not.
     *
     * @return {@code true} if department related, {@code false} otherwise
     */
    public boolean isDepartmentRelated() {
        return departmentRelated;
    }

    public boolean isSickNoteSubmissionRelated() {
        return sickNoteSubmissionRelated;
    }

    public boolean isValidWith(final Collection<Role> roles) {
        return isValid.test(roles);
    }

    private static Predicate<Collection<Role>> hasRole(final Role role) {
        return roles -> roles.contains(role);
    }

    private static Predicate<Collection<Role>> hasAnyRole(final Role... anyNeededRole) {
        final List<Role> anyNeededRoleList = asList(anyNeededRole);
        return roles -> roles.stream().anyMatch(anyNeededRoleList::contains);
    }
}
