package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Component
@ConfigurationProperties("uv.calendar")
@Validated
public class CalendarProperties {

    /**
     * Adds the organizer to all iCal events that
     * where generated through the calender integration
     * feature
     */
    @Email
    private String organizer;

    /**
     * Defines a refresh interval for iCal Feed.
     * This property specifies a suggested minimum interval for polling for changes of the calendar data from
     * the original source of that data.
     * <p>
     * Possible inputs:
     * <p><ul>
     * <li>30 (30 Minutes)
     * <li>PT30M (30 Minutes in ISO-8601 Format)
     * <li>30m (30 Minutes)
     * </ul><p>
     * Default refresh interval is one day (P1D).
     * <p>
     *
     * @see <a href="https://icalendar.org/New-Properties-for-iCalendar-RFC-7986/5-7-refresh-interval-property.html">ICal Spec: RefreshInterval</a>
     */
    @DurationUnit(ChronoUnit.MINUTES)
    private Duration refreshInterval = Duration.ofDays(1);

    public Optional<String> getOrganizer() {
        return Optional.ofNullable(organizer);
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
}
