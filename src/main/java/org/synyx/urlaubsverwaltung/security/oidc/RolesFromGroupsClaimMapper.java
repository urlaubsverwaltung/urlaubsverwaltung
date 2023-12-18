package org.synyx.urlaubsverwaltung.security.oidc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.security.oidc.RolesFromClaimMappersProperties.GroupClaimMapperProperties;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * Claim mapper to parse roles from the 'groups' claim.
 *
 * <p>
 * Demo structure:
 * <pre>
 * {
 *  "groups": [
 *    "urlaubsverwaltung_user",
 *    "urlaubsverwaltung_office",
 *    ...
 *  ]
 * }
 * </pre>
 */
@Component
@ConditionalOnProperty(value = "uv.security.oidc.claim-mappers.group-claim.enabled", havingValue = "true")
class RolesFromGroupsClaimMapper implements RolesFromClaimMapper {

    private final RolesFromClaimMapperConverter converter;
    private final RolesFromClaimMappersProperties properties;

    RolesFromGroupsClaimMapper(RolesFromClaimMapperConverter converter,
                               RolesFromClaimMappersProperties properties) {
        this.converter = converter;
        this.properties = properties;
    }

    @Override
    public List<GrantedAuthority> mapClaimToRoles(final Map<String, Object> claims) {

        final GroupClaimMapperProperties groupClaim = properties.getGroupClaim();

        final String neededResourceAccessRole = properties.getRolePrefix().concat(USER.name().toLowerCase());
        if (properties.isAuthorityCheckEnabled() && !claims.containsKey(groupClaim.getClaimName())) {
            throw new MissingClaimAuthorityException(format("User has not required permission '%s' to access urlaubsverwaltung! The claim '%s' is missing!", neededResourceAccessRole, groupClaim.getClaimName()));
        }

        final List<String> groups = extractRolesFromClaimName(claims, groupClaim.getClaimName());
        if (properties.isAuthorityCheckEnabled() && groups.stream().noneMatch(neededResourceAccessRole::equals)) {
            throw new MissingClaimAuthorityException(format("User has not required permission '%s' to access urlaubsverwaltung!", neededResourceAccessRole));
        }

        return groups.stream()
            .map(converter::convert)
            .filter(Objects::nonNull)
            .toList();
    }

    private List<String> extractRolesFromClaimName(final Map<String, Object> claims, final String claimName) {

        final Object roles = claims.get(claimName);
        if (roles instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }

        return List.of();
    }
}
