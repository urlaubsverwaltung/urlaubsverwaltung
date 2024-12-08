package org.synyx.urlaubsverwaltung.person.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@ConditionalOnSingleTenantMode
class PersonMetrics {

    private static final String METRIC_USERS_ACTIVE = "users.active";
    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;

    PersonMetrics(PersonService personService, MeterRegistry meterRegistry) {

        this.personService = personService;

        Gauge.builder(METRIC_USERS_ACTIVE, this::countActiveUsers).register(meterRegistry);
    }

    int countActiveUsers() {
        final int activeUsersCount = this.personService.numberOfActivePersons();
        LOG.debug("active users count is {}", activeUsersCount);
        return activeUsersCount;
    }
}
