package org.synyx.urlaubsverwaltung.overtime;

import static org.springframework.util.Assert.notNull;

/**
 * Identifies a {@link Overtime}.
 *
 * @param value the unique identifier of the overtime, must not be {@code null}
 */
public record OvertimeId(Long value) {

    public OvertimeId {
        notNull(value, "OvertimeId value must not be null");
    }
}
