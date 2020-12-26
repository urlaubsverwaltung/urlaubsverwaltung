package org.synyx.urlaubsverwaltung.person.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.PersonService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonMetricsTest {

    @Mock
    private PersonService personService;

    @Test
    void countActiveUsers() {
        final SimpleMeterRegistry registry = new SimpleMeterRegistry();

        final PersonMetrics sut = new PersonMetrics(personService, registry);

        when(personService.numberOfActivePersons()).thenReturn(1);

        final int countActiveUsers = sut.countActiveUsers();
        assertThat(countActiveUsers).isOne();

        final Gauge gauge = registry.find("users.active").gauge();
        assertThat(gauge.value()).isOne();
    }
}
