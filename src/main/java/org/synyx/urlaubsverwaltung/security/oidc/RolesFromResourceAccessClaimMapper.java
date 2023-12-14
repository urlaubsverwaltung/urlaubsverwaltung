package org.synyx.urlaubsverwaltung.security.oidc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * Claim mapper to parse roles from 'resource_access' claim under the 'client name'. This mapping is
 * use e.g. in keycloak.
 *
 * <p>
 * Demo structure:
 * <pre>
 * {
 *   "resource_access": {
 *     "$clientName": {
 *       "roles": [
 *         "urlaubsverwaltung_user",
 *         "urlaubsverwaltung_office",
 *         ...
 *       ]
 *     }
 *   }
 * }
 * </pre>
 */
@Component
@ConditionalOnProperty(value = "uv.security.oidc.claim-mappers.resource-access-claim.enabled", havingValue = "true")
class RolesFromResourceAccessClaimMapper implements RolesFromClaimMapper {

    private static final String CLAIM_RESOURCE_ACCESS = "resource_access";
    private static final String ROLES = "roles";

    private final RolesFromClaimMapperConverter converter;
    private final RolesFromClaimMappersProperties properties;

    RolesFromResourceAccessClaimMapper(RolesFromClaimMapperConverter converter,
                                       RolesFromClaimMappersProperties properties) {
        this.converter = converter;
        this.properties = properties;
    }

    @Override
    public List<GrantedAuthority> mapClaimToRoles(Map<String, Object> claims) {

        final String resourceAppIdentifier = properties.getResourceAccessClaim().getResourceApp();

        final Map<String, Object> resourceAccess = extractFromMap(claims, CLAIM_RESOURCE_ACCESS);
        final Map<String, Object> resourceApp = extractFromMap(resourceAccess, resourceAppIdentifier);

        final List<String> resourceAccessRoles = extractRolesFromResourceApp(resourceApp, ROLES);
        final String neededResourceAccessRole = properties.getRolePrefix().concat(USER.name().toLowerCase());

        if (properties.isAuthorityCheckEnabled() && resourceAccessRoles.stream().noneMatch(neededResourceAccessRole::equals)) {
            throw new MissingClaimAuthorityException(format("User has not required permission '%s' to access urlaubsverwaltung!", neededResourceAccessRole));
        }

        return resourceAccessRoles.stream()
            .map(converter::convert)
            .filter(Objects::nonNull)
            .toList();
    }

    private Map<String, Object> extractFromMap(Map<String, Object> myMap, String key) {
        final Object inner = myMap.get(key);
        if (inner instanceof Map<?, ?> map) {
            return map.entrySet().stream().collect(toMap(entry -> entry.getKey().toString(), Map.Entry::getValue));
        }
        return emptyMap();
    }

    private List<String> extractRolesFromResourceApp(final Map<String, Object> clientInformation, final String key) {

        final Object roles = clientInformation.get(key);
        if (roles instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return emptyList();
    }
}
