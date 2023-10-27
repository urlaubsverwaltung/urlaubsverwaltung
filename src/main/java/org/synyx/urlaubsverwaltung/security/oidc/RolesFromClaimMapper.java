package org.synyx.urlaubsverwaltung.security.oidc;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Map;

public interface RolesFromClaimMapper {

    /**
     * Converts claims into {@link GrantedAuthority} based on the provided implementation.
     *
     * @param claims to convert into {@link GrantedAuthority}
     * @return list of converted claims as {@link GrantedAuthority}
     */
    List<GrantedAuthority> mapClaimToRoles(Map<String, Object> claims);
}
