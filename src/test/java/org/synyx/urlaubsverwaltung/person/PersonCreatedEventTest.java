package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PersonCreatedEventTest {

    @Test
    void ensureReturnsCreatedAt() {

        final Instant createdAt = Instant.parse("2021-01-01T00:00:00Z");

        final PersonCreatedEvent event = new PersonCreatedEvent(
            "source", 1L, "niceName", "username", "email", true, createdAt
        );

        assertThat(event.getCreatedAt()).isEqualTo(createdAt);
    }
}
