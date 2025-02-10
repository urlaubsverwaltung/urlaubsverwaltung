package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
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
        workingTimeSettings.setWorkingDurationForChristmasEve(DayLength.MORNING);
        workingTimeSettings.setWorkingDurationForNewYearsEve(DayLength.MORNING);
        workingTimeSettings.setFederalState(FederalState.GERMANY_BADEN_WUERTTEMBERG);

        final WorkingTimeSettingsDTO expected = new WorkingTimeSettingsDTO(DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.ZERO, DayLengthDTO.ZERO, DayLengthDTO.MORNING, DayLengthDTO.MORNING, FederalStateDTO.GERMANY_BADEN_WUERTTEMBERG);

        final WorkingTimeSettingsDTO dto = WorkingTimeSettingsDTO.of(workingTimeSettings);

        assertThat(dto).isEqualTo(expected);
    }

    @Test
    void happyPathDTOToWorkingTimeSettings() {
        WorkingTimeSettingsDTO dto = new WorkingTimeSettingsDTO(DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.ZERO, DayLengthDTO.ZERO, DayLengthDTO.MORNING, DayLengthDTO.MORNING, FederalStateDTO.GERMANY_RHEINLAND_PFALZ);

        final WorkingTimeSettings workingTimeSettings = dto.toWorkingTimeSettings();

        assertThat(workingTimeSettings.getMonday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimeSettings.getTuesday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimeSettings.getWednesday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimeSettings.getThursday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimeSettings.getFriday()).isEqualTo(DayLength.FULL);
        assertThat(workingTimeSettings.getSaturday()).isEqualTo(DayLength.ZERO);
        assertThat(workingTimeSettings.getSunday()).isEqualTo(DayLength.ZERO);
        assertThat(workingTimeSettings.getWorkingDurationForChristmasEve()).isEqualTo(DayLength.MORNING);
        assertThat(workingTimeSettings.getWorkingDurationForNewYearsEve()).isEqualTo(DayLength.MORNING);
        assertThat(workingTimeSettings.getFederalState()).isEqualTo(FederalState.GERMANY_RHEINLAND_PFALZ);
    }
}
