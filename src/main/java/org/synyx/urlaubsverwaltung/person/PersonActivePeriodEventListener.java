package org.synyx.urlaubsverwaltung.person;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;

/**
 * Keeps the {@link PersonActivePeriod} history of a person in sync with person creation and role changes.
 */
@Component
class PersonActivePeriodEventListener {

    private final PersonActivePeriodServiceImpl personActivePeriodService;

    PersonActivePeriodEventListener(PersonActivePeriodServiceImpl personActivePeriodService) {
        this.personActivePeriodService = personActivePeriodService;
    }

    @EventListener
    void on(PersonCreatedEvent event) {
        if (event.isActive()) {
            final PersonId personId = new PersonId(event.getPersonId());
            personActivePeriodService.openPeriod(personId, Instant.ofEpochMilli(event.getTimestamp()));
        }
    }

    @EventListener
    void on(PersonPermissionsChangedEvent event) {

        final PersonId personId = new PersonId(event.personId());

        if (event.grantedPermissions().contains(INACTIVE)) {
            personActivePeriodService.closeOpenPeriod(personId, event.createdAt());
        } else if (event.revokedPermissions().contains(INACTIVE)) {
            personActivePeriodService.openPeriod(personId, event.createdAt());
        }
    }
}
