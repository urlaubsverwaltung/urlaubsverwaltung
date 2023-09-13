package org.synyx.urlaubsverwaltung.dev;

import org.slf4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.synyx.urlaubsverwaltung.person.PersonCreatedEvent;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

class PersonCreatedEventListener {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final DemoDataCreationService demoDataCreationService;

    PersonCreatedEventListener(DemoDataCreationService demoDataCreationService) {
        this.demoDataCreationService = demoDataCreationService;
    }

    @Async
    @EventListener
    void on(PersonCreatedEvent event) {
        final String email = event.getEmail();
        if (email == null || email.isEmpty()) {
            LOG.info("received PersonCreatedEvent for unknown person - going to skip create demo data");
            return;
        }

        LOG.info("received PersonCreatedEvent for person.email={} - going to create demo data", email);
        demoDataCreationService.createDemoData(email);
        LOG.info("finished creating demo data for PersonCreatedEvent of person.email={}", email);
    }
}
