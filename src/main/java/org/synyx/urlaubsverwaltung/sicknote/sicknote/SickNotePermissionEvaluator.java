package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_EDIT;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;

/**
 * Single source of truth for the question "who may interact with a sick note?".
 *
 * <p>The rule is: {@link org.synyx.urlaubsverwaltung.person.Role#SICK_NOTE_VIEW} grants sight of sick notes of persons
 * one does <em>not</em> manage. {@link org.synyx.urlaubsverwaltung.person.Role#OFFICE} sees everyone,
 * {@link org.synyx.urlaubsverwaltung.person.Role#BOSS} needs {@code SICK_NOTE_VIEW} to see everyone,
 * {@link org.synyx.urlaubsverwaltung.person.Role#DEPARTMENT_HEAD} and
 * {@link org.synyx.urlaubsverwaltung.person.Role#SECOND_STAGE_AUTHORITY} always see the members they manage and
 * everyone sees their own sick notes.
 *
 * <p>Maintaining a sick note (add, edit, cancel, comment) additionally requires the matching {@code SICK_NOTE_*} role.
 */
@Component
public class SickNotePermissionEvaluator {

    private final DepartmentService departmentService;
    private final SettingsService settingsService;

    public SickNotePermissionEvaluator(DepartmentService departmentService, SettingsService settingsService) {
        this.departmentService = departmentService;
        this.settingsService = settingsService;
    }

    /**
     * Permissions of the given user on sick notes of the given person. The department memberships that the rules depend
     * on are resolved lazily and only once, therefore the returned instance is meant to be used for a single request.
     *
     * @param signedInUser   user asking for permissions
     * @param sickNotePerson person the sick notes belong to
     * @return permissions of {@code signedInUser} on sick notes of {@code sickNotePerson}
     */
    public SickNotePermissions of(Person signedInUser, Person sickNotePerson) {
        return new SickNotePermissions(departmentService, settingsService, signedInUser, sickNotePerson);
    }

    /**
     * Permissions of the given user on the person of the given sick note.
     *
     * @param signedInUser user asking for permissions
     * @param sickNote     sick note to get the person from
     * @return permissions of {@code signedInUser} on sick notes of the person of {@code sickNote}
     */
    public SickNotePermissions of(Person signedInUser, SickNote sickNote) {
        return of(signedInUser, sickNote.getPerson());
    }

    /**
     * Whether the given user may work on submitted sick notes at all. In contrast to the other permissions this one is
     * not bound to a single person, it guards the list of submitted sick notes - which persons are part of that list is
     * decided when the list is loaded.
     *
     * @param signedInUser user asking for permissions
     * @return {@code true} if the user may see and accept submitted sick notes, {@code false} otherwise
     */
    public boolean isAllowedToAccessSickNoteSubmissions(Person signedInUser) {
        return signedInUser.hasRole(OFFICE)
            || (signedInUser.hasRole(SICK_NOTE_EDIT) && signedInUser.hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));
    }

    /**
     * Whether the given user may create sick notes for other persons at all. Like
     * {@link #isAllowedToAccessSickNoteSubmissions(Person)} this permission is not bound to a single person, for which
     * persons a sick note may be created is decided per person.
     *
     * @param signedInUser user asking for permissions
     * @return {@code true} if the user may create sick notes for at least one other person, {@code false} otherwise
     */
    public boolean isAllowedToAddSickNotesForOtherPersons(Person signedInUser) {
        return signedInUser.hasRole(OFFICE)
            || (signedInUser.hasRole(SICK_NOTE_ADD) && signedInUser.hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));
    }

    /**
     * Whether the given user may see sick notes of persons that the user does not manage.
     *
     * @param signedInUser user asking for permissions
     * @return {@code true} if the user may see sick notes of every person, {@code false} otherwise
     */
    public boolean isAllowedToViewSickNotesOfAllPersons(Person signedInUser) {
        return signedInUser.hasRole(OFFICE)
            || (signedInUser.hasRole(BOSS) && signedInUser.hasRole(SICK_NOTE_VIEW));
    }

    /**
     * Whether the given user may see sick notes of any other person - either because the user manages members or
     * because the user is allowed to see sick notes of all persons.
     *
     * @param signedInUser user asking for permissions
     * @return {@code true} if the user may see sick notes of at least one other person, {@code false} otherwise
     */
    public boolean isAllowedToViewSickNotesOfOtherPersons(Person signedInUser) {
        return isAllowedToViewSickNotesOfAllPersons(signedInUser)
            || signedInUser.hasAnyRole(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
    }
}
