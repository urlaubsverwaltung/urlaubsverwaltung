package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.calendarintegration.settings.CalendarSettingsEntity;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit test for {@link CalendarSettingsEntity}.
 */
class CalendarSettingsEntityTest {

    @Test
    void ensureHasDefaultValues() {

        final CalendarSettingsEntity calendarSettings = new CalendarSettingsEntity();

        assertThat(calendarSettings.getExchangeCalendarSettings()).isNotNull();
    }
}
