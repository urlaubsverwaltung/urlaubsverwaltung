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

    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED(hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED(hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED(hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED(hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED(hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION(hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED(hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED(hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER(hasRole(USER).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),

    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED(hasRole(USER).and(hasRole(OFFICE).or(hasRole(APPLICATION_CANCELLATION_REQUESTED).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))))),

    NOTIFICATION_EMAIL_APPLICATION_APPLIED(hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_ALLOWED(hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_REVOKED(hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_REJECTED(hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED(hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_CANCELLATION(hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_EDITED(hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_CONVERTED(hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_UPCOMING(hasRole(USER)),

    NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT(hasRole(USER)),
    NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING(hasRole(USER)),

    NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL(hasRole(USER).and(hasAnyRole(BOSS, OFFICE))),

    NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED(hasRole(USER).and(hasAnyRole(OFFICE, BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY))),
    NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT(hasRole(USER)),
    NOTIFICATION_EMAIL_OVERTIME_APPLIED(hasRole(USER)),

    NOTIFICATION_EMAIL_ABSENCE_COLLEAGUES_ALLOWED(hasRole(USER)),
    NOTIFICATION_EMAIL_ABSENCE_COLLEAGUES_CANCELLATION(hasRole(USER)),

    NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED(hasRole(USER)),
    NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED(hasRole(USER));

    private final Predicate<Collection<Role>> isValid;

    MailNotification(Predicate<Collection<Role>> isValid) {
        this.isValid = isValid;
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
