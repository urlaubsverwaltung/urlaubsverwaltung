package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarSettingsTest {

    @Test
    void ensureHasDefaultValues() {
        final CalendarSettings calendarSettings = new CalendarSettings();
        assertThat(calendarSettings.getExchangeCalendarSettings()).isNotNull();
    }
}
