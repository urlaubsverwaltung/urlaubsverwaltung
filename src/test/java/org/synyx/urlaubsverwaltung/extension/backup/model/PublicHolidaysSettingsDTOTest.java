package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettings;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.extension.backup.model.DayLengthDTO.MORNING;
import static org.synyx.urlaubsverwaltung.extension.backup.model.FederalStateDTO.GERMANY_BADEN_WUERTTEMBERG;

class PublicHolidaysSettingsDTOTest {

    @Test
    void happyPathPublicHolidaysSettingsToDTO() {
        final PublicHolidaysSettings publicHolidaysSettings = new PublicHolidaysSettings();
        publicHolidaysSettings.setWorkingDurationForChristmasEve(DayLength.MORNING);
        publicHolidaysSettings.setWorkingDurationForNewYearsEve(DayLength.MORNING);
        publicHolidaysSettings.setFederalState(FederalState.GERMANY_BADEN_WUERTTEMBERG);

        final PublicHolidaysSettingsDTO dto = PublicHolidaysSettingsDTO.of(publicHolidaysSettings);
        assertThat(dto).isEqualTo(new PublicHolidaysSettingsDTO(MORNING, MORNING, GERMANY_BADEN_WUERTTEMBERG));
    }

    @Test
    void happyPathDTOToPublicHolidaysSettings() {
        PublicHolidaysSettingsDTO dto = new PublicHolidaysSettingsDTO(MORNING, MORNING, FederalStateDTO.GERMANY_RHEINLAND_PFALZ);

        final PublicHolidaysSettings publicHolidaysSettings = dto.toPublicHolidaysSettings();
        assertThat(publicHolidaysSettings.getWorkingDurationForChristmasEve()).isEqualTo(DayLength.MORNING);
        assertThat(publicHolidaysSettings.getWorkingDurationForNewYearsEve()).isEqualTo(DayLength.MORNING);
        assertThat(publicHolidaysSettings.getFederalState()).isEqualTo(FederalState.GERMANY_RHEINLAND_PFALZ);
    }
}
