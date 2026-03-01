package org.synyx.urlaubsverwaltung.person;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record PersonPermissionsChangedEvent(
    UUID id, Instant createdAt, Long personId, String personNiceName,
    String username, String email,
    Collection<Role> previousPermissions, Collection<Role> currentPermissions,
    Collection<Role> grantedPermissions, Collection<Role> revokedPermissions
) {

    public static PersonPermissionsChangedEvent of(Person person,
                                                   Collection<Role> previousPermissions,
                                                   Collection<Role> currentPermissions) {

        final List<Role> granted = currentPermissions.stream()
            .filter(role -> !previousPermissions.contains(role))
            .toList();

        final List<Role> revoked = previousPermissions.stream()
            .filter(role -> !currentPermissions.contains(role))
            .toList();

        return new PersonPermissionsChangedEvent(
            UUID.randomUUID(), Instant.now(),
            person.getId(), person.getNiceName(), person.getUsername(), person.getEmail(),
            List.copyOf(previousPermissions), List.copyOf(currentPermissions),
            granted, revoked
        );
    }
}
