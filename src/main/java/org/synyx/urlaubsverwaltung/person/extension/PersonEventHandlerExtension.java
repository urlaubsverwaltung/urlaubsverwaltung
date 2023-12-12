package org.synyx.urlaubsverwaltung.person.extension;

import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonCreatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonDeletedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonDisabledEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonUpdatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.extension.ExtensionConfiguration;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonCreatedEvent;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.person.PersonDisabledEvent;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.PersonUpdatedEvent;

@ConditionalOnBean(ExtensionConfiguration.class)
@Component
class PersonEventHandlerExtension {

    private final TenantSupplier tenantSupplier;
    private final PersonService personService;
    private final ApplicationEventPublisher applicationEventPublisher;

    PersonEventHandlerExtension(TenantSupplier tenantSupplier,
                                PersonService personService,
                                ApplicationEventPublisher applicationEventPublisher) {
        this.tenantSupplier = tenantSupplier;
        this.personService = personService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Async
    @EventListener
    void on(PersonCreatedEvent event) {
        personService.getPersonByUsername(event.getUsername())
            .ifPresent(existing -> {
                final PersonCreatedEventDTO eventToPublish = PersonCreatedEventDTO.create(tenantSupplier.get(), existing.getId(), existing.getUsername(), existing.getLastName(), existing.getFirstName(), existing.getEmail(), existing.isActive());
                applicationEventPublisher.publishEvent(eventToPublish);
            });
    }

    @Async
    @EventListener
    void on(PersonUpdatedEvent event) {
        personService.getPersonByUsername(event.getUsername())
            .ifPresent(existing -> {
                final PersonUpdatedEventDTO eventToPublish = PersonUpdatedEventDTO.create(tenantSupplier.get(), existing.getId(), existing.getUsername(), existing.getLastName(), existing.getFirstName(), existing.getEmail(), existing.isActive());
                applicationEventPublisher.publishEvent(eventToPublish);
            });
    }

    @Async
    @EventListener
    void on(PersonDisabledEvent event) {
        personService.getPersonByUsername(event.getUsername())
            .ifPresent(existing -> {
                final PersonDisabledEventDTO eventToPublish = PersonDisabledEventDTO.create(tenantSupplier.get(), existing.getId(), existing.getUsername(), existing.getLastName(), existing.getFirstName(), existing.getEmail());
                applicationEventPublisher.publishEvent(eventToPublish);
            });
    }

    @Async
    @EventListener
    void on(PersonDeletedEvent event) {
        final Person person = event.person();
        final PersonDeletedEventDTO eventToPublish = PersonDeletedEventDTO.create(tenantSupplier.get(), person.getId(), person.getUsername(), person.getLastName(), person.getFirstName(), person.getEmail(), person.isActive());
        applicationEventPublisher.publishEvent(eventToPublish);
    }

}
