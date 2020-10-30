package org.synyx.urlaubsverwaltung.person.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonMetricsTest {

    @Mock
    private PersonService personService;

    private PersonMetrics sut;

    @Test
    void countActiveUsers() {
        Metrics.addRegistry(new SimpleMeterRegistry());
        sut = new PersonMetrics(personService);

        when(personService.getActivePersons()).thenReturn(List.of(new Person()));

        sut.countActiveUsers();

        Gauge gauge = Metrics.globalRegistry.find("users.active").gauge();
        assertThat(gauge.value()).isOne();
    }
}
