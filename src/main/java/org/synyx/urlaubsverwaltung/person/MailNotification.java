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
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * Describes which kind of mail notifications a person can have.
 */
public enum MailNotification {

    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED(true, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED(true, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED(true, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED(true, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED(true, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION(true, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED(true, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED(true, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER(true, hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),

    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED(true, hasRole(USER).and(hasRole(OFFICE).or(hasRole(APPLICATION_CANCELLATION_REQUESTED).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))))),

    NOTIFICATION_EMAIL_APPLICATION_APPLIED(false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_ALLOWED(false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_REVOKED(false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_REJECTED(false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED(false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_CANCELLATION(false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_EDITED(false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_CONVERTED(false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_UPCOMING(false, hasRole(USER)),

    NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT(false, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING(false, hasRole(USER)),

    NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED(true, hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION(true, hasRole(USER)),

    NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL(false, hasRole(USER).and(hasAnyRole(BOSS, OFFICE))),

    NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED(true, hasRole(USER).and(hasAnyRole(OFFICE, BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT(false, hasRole(USER)),
    NOTIFICATION_EMAIL_OVERTIME_APPLIED(false, hasRole(USER)),

    NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT(false, hasRole(USER)),
    NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT(false, hasRole(USER)),
    NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED(true, hasRole(USER)),
    NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED(true, hasRole(USER));

    private final boolean departmentRelated;
    private final Predicate<Collection<Role>> isValid;

    MailNotification(boolean departmentRelated, Predicate<Collection<Role>> isValid) {
        this.departmentRelated = departmentRelated;
        this.isValid = isValid;
    }

    /**
     * Whether this mail notification is department related or not.
     * @return {@code true} if department related, {@code false} otherwise
     */
    public boolean isDepartmentRelated() {
        return departmentRelated;
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
