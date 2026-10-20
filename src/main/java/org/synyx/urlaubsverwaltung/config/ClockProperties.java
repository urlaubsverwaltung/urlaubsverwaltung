package org.synyx.urlaubsverwaltung.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Instant;

/**
 * Time source of the whole application.
 *
 * @param fixedInstant freezes the application time at this instant. Meant for visual regression tests only, so that
 *                     demo data and everything relative to 'today' stays the same on every start. Leave it empty to
 *                     use the system time.
 */
@ConfigurationProperties("uv.development.clock")
public record ClockProperties(Instant fixedInstant) {
}
