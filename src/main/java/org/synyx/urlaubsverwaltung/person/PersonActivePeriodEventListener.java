package org.synyx.urlaubsverwaltung.person;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.application.application.ApplicationAllowedEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationAppliedEvent;
import org.synyx.urlaubsverwaltung.overtime.OvertimeCreatedEvent;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCreatedEvent;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeConfiguredEvent;

import java.time.Instant;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.ZoneOffset.UTC;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;

/**
 * Keeps the {@link PersonActivePeriod} history of a person in sync with person creation and role changes.
 */
@Component
class PersonActivePeriodEventListener {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonActivePeriodServiceImpl personActivePeriodService;
    private final PersonService personService;

    PersonActivePeriodEventListener(PersonActivePeriodServiceImpl personActivePeriodService, PersonService personService) {
        this.personActivePeriodService = personActivePeriodService;
        this.personService = personService;
    }

    @EventListener
    void on(PersonCreatedEvent event) {
        if (event.isActive()) {
            final PersonId personId = new PersonId(event.getPersonId());
            personActivePeriodService.openPeriod(personId, event.getCreatedAt());
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

    @EventListener
    void on(OvertimeCreatedEvent event) {
        final PersonId personId = getPersonId(event.username());
        final Instant validFrom = event.startDate().atStartOfDay(UTC).toInstant();
        updateActivePeriods(personId, validFrom);
    }

    @EventListener
    void on(SickNoteCreatedEvent event) {
        final PersonId personId = event.sickNote().getPerson().getIdAsPersonId();
        final Instant validFrom = event.sickNote().getStartDate().atStartOfDay(UTC).toInstant();
        updateActivePeriods(personId, validFrom);
    }

    @EventListener
    void on(ApplicationAppliedEvent event) {
        final PersonId personId = event.application().getPerson().getIdAsPersonId();
        final Instant validFrom = event.application().getStartDate().atStartOfDay(UTC).toInstant();
        updateActivePeriods(personId, validFrom);
    }

    @EventListener
    void on(ApplicationAllowedEvent event) {
        final PersonId personId = event.application().getPerson().getIdAsPersonId();
        final Instant validFrom = event.application().getStartDate().atStartOfDay(UTC).toInstant();
        updateActivePeriods(personId, validFrom);
    }

    @EventListener
    void on(WorkingTimeConfiguredEvent event) {
        final PersonId personId = getPersonId(event.username());
        final Instant validFrom = event.validFrom().atStartOfDay(UTC).toInstant();
        updateActivePeriods(personId, validFrom);
    }

    private @NonNull PersonId getPersonId(String username) {
        return personService.getPersonByUsername(username)
            .map(Person::getIdAsPersonId)
            .orElseThrow(() -> new IllegalStateException("Person with username=%s not found.".formatted(username)));
    }

    private void updateActivePeriods(PersonId personId, Instant instant) {

        final List<PersonActivePeriod> activePeriods = personActivePeriodService.getActivePeriods(personId);
        if (activePeriods.isEmpty()) {
            LOG.info("no active periods available for person={}. cannot check whether to create a new PersonActivePeriod or not.", personId);
            return;
        }

        final Instant earliestValidFrom = activePeriods.getFirst().validFrom();
        if (instant.isBefore(earliestValidFrom)) {
            personActivePeriodService.insertPeriod(personId, instant, earliestValidFrom);
        }
    }
}
