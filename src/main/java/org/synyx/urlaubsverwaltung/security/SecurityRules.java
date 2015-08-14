package org.synyx.urlaubsverwaltung.security;

/**
 * Spring Security rules that can be used within {@link org.springframework.security.access.prepost.PreAuthorize}
 * annotations.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SecurityRules {

    public static final String IS_OFFICE = "hasRole('OFFICE')";
    public static final String IS_BOSS_OR_OFFICE = "hasAnyRole('BOSS', 'OFFICE')";
    public static final String IS_BOSS_OR_DEPARTMENT_HEAD = "hasAnyRole('BOSS', 'DEPARTMENT_HEAD')";
    public static final String IS_PRIVILEGED_USER = "hasAnyRole('DEPARTMENT_HEAD', 'BOSS', 'OFFICE')";
}
