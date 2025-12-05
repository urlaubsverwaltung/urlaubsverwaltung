package org.synyx.urlaubsverwaltung.security.oidc;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.SUB;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * Claim mapper to parse roles from 'resource_access' claim under the 'client name'. This mapping is
 * used e.g. in keycloak.
 *
 * <p>
 * Demo structure:
 * <pre>
 * {
 *   …
 *   "resource_access": {
 *     "resource_a": {
 *       "roles": [
 *         "a_user"
 *       ]
 *     },
 *     "urlaubsverwaltung": {
 *       "roles": [
 *         "urlaubsverwaltung_user"
 *       ]
 *     },
 *     "resource_b": {
 *       "roles": [
 *         "b_user",
 *       ]
 *   },
 * }
 * </pre>
 */
@Component
@ConditionalOnProperty(value = "uv.security.oidc.claim-mappers.full-resource-access-claim.enabled", havingValue = "true")
class RolesFromFullResourceAccessClaimMapper implements RolesFromClaimMapper {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private static final SimpleGrantedAuthority NEEDED_RESOURCE_ACCESS_ROLE = new SimpleGrantedAuthority(USER.name().toUpperCase());
    private static final String CLAIM_RESOURCE_ACCESS = "resource_access";
    private static final String ROLES = "roles";

    private final RolesFromClaimMapperConverter converter;
    private final RolesFromClaimMappersProperties properties;

    RolesFromFullResourceAccessClaimMapper(
        RolesFromClaimMapperConverter converter,
        RolesFromClaimMappersProperties properties
    ) {
        this.converter = converter;
        this.properties = properties;
    }

    @Override
    public List<GrantedAuthority> mapClaimToRoles(Map<String, Object> claims) {

        final String resourceAppIdentifier = properties.getResourceAccessClaim().getResourceApp();

        final Map<String, Object> resourceAccess = extractFromMap(claims, CLAIM_RESOURCE_ACCESS);

        final List<GrantedAuthority> grantedAuthorities = resourceAccess.keySet().stream().map(appIdentifier -> {
            final Map<String, Object> resourceApp = extractFromMap(resourceAccess, appIdentifier);
            final List<String> resourceAccessRoles = extractRolesFromResourceApp(resourceApp);

            if (appIdentifier.equals(resourceAppIdentifier)) {
                return resourceAccessRoles.stream().map(converter::convert).filter(Objects::nonNull).toList();
            }

            return resourceAccessRoles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role)).toList();
        }).flatMap(Collection::stream).toList();

        if (properties.isAuthorityCheckEnabled() && grantedAuthorities.stream().noneMatch(NEEDED_RESOURCE_ACCESS_ROLE::equals)) {
            final String prefixedNeededRole = properties.getRolePrefix().concat(NEEDED_RESOURCE_ACCESS_ROLE.toString().toLowerCase());
            LOG.error("User with sub '{}' has not required permission '{}' in '{}' to access urlaubsverwaltung!", claims.get(SUB), prefixedNeededRole, grantedAuthorities);
            throw new MissingClaimAuthorityException(format("User has not required permission '%s' in '%s' to access urlaubsverwaltung!", prefixedNeededRole, grantedAuthorities));
        }

        return grantedAuthorities;
    }

    private Map<String, Object> extractFromMap(Map<String, Object> myMap, String key) {
        final Object inner = myMap.get(key);
        if (inner instanceof Map<?, ?> map) {
            return map.entrySet().stream().collect(toMap(entry -> entry.getKey().toString(), Map.Entry::getValue));
        }
        return Map.of();
    }

    private List<String> extractRolesFromResourceApp(final Map<String, Object> clientInformation) {

        final Object roles = clientInformation.get(ROLES);
        if (roles instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
