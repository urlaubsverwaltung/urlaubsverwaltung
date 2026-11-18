package org.synyx.urlaubsverwaltung.publicholiday;

import de.focus_shift.jollyday.core.CalendarHierarchy;
import de.focus_shift.jollyday.core.HolidayManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

class PublicHolidayConfigurationTest {

    @Test
    void ensureThatHolidaysManagerMapContainsAllKeys() {
        final Map<String, HolidayManager> holidayManagers = new PublicHolidayConfiguration().holidayManagerMap();
        assertThat(holidayManagers)
                .hasSize(22)
                .containsKeys("de", "at", "ch", "gb", "gr", "mt", "it", "hr", "es", "nl", "lt", "be", "pl", "fi", "ro", "pt", "bg", "lv", "us", "gg", "je", "im");
    }

    // jollyday silently ignores unknown region codes and falls back to national holidays, so check each one exists
    @ParameterizedTest
    @EnumSource(value = FederalState.class, mode = EXCLUDE, names = "NONE")
    void ensureFederalStateCodesExistInJollyday(FederalState federalState) {
        final Map<String, HolidayManager> holidayManagers = new PublicHolidayConfiguration().holidayManagerMap();
        assertThat(holidayManagers).containsKey(federalState.getCountry());

        CalendarHierarchy hierarchy = holidayManagers.get(federalState.getCountry()).getCalendarHierarchy();
        for (String code : federalState.getCodes()) {
            assertThat(hierarchy.getChildren()).containsKey(code);
            hierarchy = hierarchy.getChildren().get(code);
        }
    }
}
