package org.synyx.urlaubsverwaltung.security;

/**
 * Spring Security rules that can be used within {@link org.springframework.security.access.prepost.PreAuthorize}
 * annotations.
 */
public final class SecurityRules {

    public static final String IS_OFFICE = "hasAuthority('OFFICE')";
    public static final String IS_BOSS_OR_OFFICE = "hasAnyAuthority('BOSS', 'OFFICE')";
    public static final String IS_BOSS_OR_DEPARTMENT_HEAD_OR_SECOND_STAGE_AUTHORITY =
        "hasAnyAuthority('BOSS', 'DEPARTMENT_HEAD', 'SECOND_STAGE_AUTHORITY')";

    public static final String IS_PRIVILEGED_USER =
        "hasAnyAuthority('DEPARTMENT_HEAD', 'BOSS', 'OFFICE', 'SECOND_STAGE_AUTHORITY')";

    private SecurityRules() {
        // Hide constructor for util classes
    }
}
