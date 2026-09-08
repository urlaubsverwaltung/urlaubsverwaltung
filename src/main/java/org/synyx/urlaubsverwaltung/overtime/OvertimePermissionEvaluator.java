package org.synyx.urlaubsverwaltung.overtime;

import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

/**
 * Single source of truth for the question "who may interact with overtime?".
 *
 * <p>Reading follows the responsibility for a person: everyone sees their own overtime,
 * {@link org.synyx.urlaubsverwaltung.person.Role#OFFICE} and {@link org.synyx.urlaubsverwaltung.person.Role#BOSS} see
 * everyone, a {@link org.synyx.urlaubsverwaltung.person.Role#DEPARTMENT_HEAD} or
 * {@link org.synyx.urlaubsverwaltung.person.Role#SECOND_STAGE_AUTHORITY} sees the members they manage.
 *
 * <p>Maintaining overtime - adding, editing, commenting - depends on the overtime settings. When
 * {@code overtimeWritePrivilegedOnly} is switched off, everyone maintains their own overtime and nobody else's. When it
 * is switched on, a privileged user maintains their own and - as {@code BOSS} everyone's, as a manager the overtime of
 * the members they are responsible for. {@code OFFICE} always maintains the overtime of everyone.
 */
@Component
public class OvertimePermissionEvaluator {

    private final DepartmentService departmentService;
    private final SettingsService settingsService;

    public OvertimePermissionEvaluator(DepartmentService departmentService, SettingsService settingsService) {
        this.departmentService = departmentService;
        this.settingsService = settingsService;
    }

    /**
     * Permissions of the given user on the overtime of the given person. The department memberships and the settings
     * that the rules depend on are read once and answer every question of the returned instance, which is therefore
     * meant to be used for a single request.
     *
     * @param signedInUser   user asking for permissions
     * @param overtimePerson person the overtime belongs to
     * @return permissions of {@code signedInUser} on overtime of {@code overtimePerson}
     */
    public OvertimePermissions of(Person signedInUser, Person overtimePerson) {
        final OvertimeSettings overtimeSettings = settingsService.getSettings().getOvertimeSettings();
        return new OvertimePermissions(signedInUser, overtimePerson,
            departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, overtimePerson),
            departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, overtimePerson),
            overtimeSettings.isOvertimeActive(),
            overtimeSettings.isOvertimeSyncActive(),
            overtimeSettings.isOvertimeWritePrivilegedOnly());
    }
}
