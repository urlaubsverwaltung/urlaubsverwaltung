package org.synyx.urlaubsverwaltung.person.metrics;

import io.micrometer.core.instrument.Metrics;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.person.PersonService;

@Component
class PersonMetrics {

    private static final String METRIC_USERS_ACTIVE = "users.active";

    private final PersonService personService;

    PersonMetrics(PersonService personService) {

        this.personService = personService;
    }

    @Scheduled(fixedDelay = 300000)
    void countActiveUsers() {
        Metrics.gauge(METRIC_USERS_ACTIVE, this.personService.getActivePersons().size());
    }

}
