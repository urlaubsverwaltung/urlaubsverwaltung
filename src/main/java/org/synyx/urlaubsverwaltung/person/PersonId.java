package org.synyx.urlaubsverwaltung.person;

import static org.springframework.util.Assert.notNull;

/**
 * Identifies a {@link Person}.
 *
 * @param value the unique identifier of the person, must not be {@code null}
 */
public record PersonId(Long value) {

    public PersonId {
        notNull(value, "PersonId value must not be null");
    }
}
