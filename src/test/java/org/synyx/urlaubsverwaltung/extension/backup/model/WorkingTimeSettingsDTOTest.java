package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import static org.assertj.core.api.Assertions.assertThat;

class WorkingTimeSettingsDTOTest {

    @Test
    void happyPathWorkingTimeSettingsToDTO() {
        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setMonday(DayLength.FULL);
        workingTimeSettings.setTuesday(DayLength.FULL);
        workingTimeSettings.setWednesday(DayLength.FULL);
        workingTimeSettings.setThursday(DayLength.FULL);
        workingTimeSettings.setFriday(DayLength.FULL);
        workingTimeSettings.setSaturday(DayLength.ZERO);
        workingTimeSettings.setSunday(DayLength.ZERO);

        final WorkingTimeSettingsDTO dto = WorkingTimeSettingsDTO.of(workingTimeSettings);
        assertThat(dto).isEqualTo(new WorkingTimeSettingsDTO(DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.ZERO, DayLengthDTO.ZERO));
    }

    @Test
    void happyPathDTOToWorkingTimeSettings() {
        WorkingTimeSettingsDTO dto = new WorkingTimeSettingsDTO(DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.ZERO, DayLengthDTO.ZERO);

        final WorkingTimeSettings workingTimeSettings = dto.toWorkingTimeSettings();
        assertThat(workingTimeSettings.getMonday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimeSettings.getTuesday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimeSettings.getWednesday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimeSettings.getThursday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimeSettings.getFriday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimeSettings.getSaturday()).isEqualTo(DayLength.ZERO);
        assertThat(workingTimeSettings.getSunday()).isEqualTo(DayLength.ZERO);
    }
}
