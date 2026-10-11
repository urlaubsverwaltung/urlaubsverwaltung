package org.synyx.urlaubsverwaltung.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

class ClockConfigTest {

    private final ClockConfig sut = new ClockConfig();

    @Test
    void ensuresSystemClockWithoutFixedInstant() {
        final Clock actual = sut.clock(new ClockProperties(null));
        assertThat(actual).isEqualTo(Clock.systemUTC());
    }

    @Test
    void ensuresFixedClockWithFixedInstant() {
        final Instant fixedInstant = Instant.parse("2026-09-16T09:00:00Z");
        final Clock actual = sut.clock(new ClockProperties(fixedInstant));
        assertThat(actual).isEqualTo(Clock.fixed(fixedInstant, UTC));
    }
}
