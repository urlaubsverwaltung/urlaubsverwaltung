package org.synyx.urlaubsverwaltung.security;

/**
 * Spring Security rules that can be used within {@link org.springframework.security.access.prepost.PreAuthorize}
 * annotations.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public final class SecurityRules {

    public static final String IS_OFFICE = "hasAuthority('OFFICE')";
    public static final String IS_BOSS_OR_OFFICE = "hasAnyAuthority('BOSS', 'OFFICE')";
    public static final String IS_BOSS_OR_DEPARTMENT_HEAD = "hasAnyAuthority('BOSS', 'DEPARTMENT_HEAD')";
    public static final String IS_PRIVILEGED_USER = "hasAnyAuthority('DEPARTMENT_HEAD', 'BOSS', 'OFFICE')";

    private SecurityRules() {

        // Hide constructor for util classes
    }
}
