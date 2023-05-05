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

/**
 * Describes which kind of mail notifications a person can have.
 */
public enum MailNotification {

    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)),
    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)),

    NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED(hasRole(OFFICE).or(hasRole(APPLICATION_CANCELLATION_REQUESTED).and(hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)))),

    NOTIFICATION_EMAIL_APPLICATION_APPLIED,
    NOTIFICATION_EMAIL_APPLICATION_ALLOWED,
    NOTIFICATION_EMAIL_APPLICATION_REVOKED,
    NOTIFICATION_EMAIL_APPLICATION_REJECTED,
    NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED,
    NOTIFICATION_EMAIL_APPLICATION_CANCELLATION,
    NOTIFICATION_EMAIL_APPLICATION_EDITED,
    NOTIFICATION_EMAIL_APPLICATION_CONVERTED,
    NOTIFICATION_EMAIL_APPLICATION_UPCOMING,

    NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT,
    NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING,

    NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL(hasAnyRole(BOSS, OFFICE)),

    NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED(hasAnyRole(OFFICE, BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)),
    NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT,
    NOTIFICATION_EMAIL_OVERTIME_APPLIED;

    private final Predicate<Collection<Role>> isValid;

    MailNotification() {
        this(nothing -> true);
    }

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
