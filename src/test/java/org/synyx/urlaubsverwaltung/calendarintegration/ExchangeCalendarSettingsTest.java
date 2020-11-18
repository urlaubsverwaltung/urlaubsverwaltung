package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.calendarintegration.ExchangeCalendarSettings;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit test for {@link ExchangeCalendarSettings}.
 */
class ExchangeCalendarSettingsTest {

    @Test
    void ensureHasSomeDefaultValues() {

        final ExchangeCalendarSettings calendarSettings = new ExchangeCalendarSettings();

        // No default values
        assertThat(calendarSettings.getEmail()).isNull();
        assertThat(calendarSettings.getPassword()).isNull();

        // Default values
        assertThat(calendarSettings.getCalendar()).isEmpty();
        assertThat(calendarSettings.isSendInvitationActive()).isFalse();
    }
}
