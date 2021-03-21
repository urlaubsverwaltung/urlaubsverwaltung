package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;

class WorkingTimeSettingsTest {

    @Test
    void ensureDefaultValues() {

        final WorkingTimeSettings settings = new WorkingTimeSettings();
        assertThat(settings.getWorkingDurationForChristmasEve()).isEqualTo(MORNING);
        assertThat(settings.getWorkingDurationForNewYearsEve()).isEqualTo(MORNING);
        assertThat(settings.getMonday()).isEqualTo(FULL);
        assertThat(settings.getTuesday()).isEqualTo(FULL);
        assertThat(settings.getWednesday()).isEqualTo(FULL);
        assertThat(settings.getThursday()).isEqualTo(FULL);
        assertThat(settings.getFriday()).isEqualTo(FULL);
        assertThat(settings.getSaturday()).isEqualTo(ZERO);
        assertThat(settings.getSunday()).isEqualTo(ZERO);
    }

    @Test
    void setWorkingDays() {
        final WorkingTimeSettings settings = new WorkingTimeSettings();
        settings.setWorkingDays(List.of(1, 2, 3, 4, 5, 6, 7));
        assertThat(settings.getWorkingDays()).isEqualTo(List.of(1, 2, 3, 4, 5, 6, 7));
    }
}
