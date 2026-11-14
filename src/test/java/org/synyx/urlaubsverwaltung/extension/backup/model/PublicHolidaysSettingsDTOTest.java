package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettings;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.extension.backup.model.FederalStateDTO.GERMANY_BADEN_WUERTTEMBERG;

class PublicHolidaysSettingsDTOTest {

    @Test
    void happyPathPublicHolidaysSettingsToDTO() {
        final PublicHolidaysSettings publicHolidaysSettings = new PublicHolidaysSettings();
        publicHolidaysSettings.setFederalState(FederalState.GERMANY_BADEN_WUERTTEMBERG);

        final PublicHolidaysSettingsDTO dto = PublicHolidaysSettingsDTO.of(publicHolidaysSettings);
        assertThat(dto).isEqualTo(new PublicHolidaysSettingsDTO(null, null, GERMANY_BADEN_WUERTTEMBERG));
    }

    @Test
    void happyPathDTOToPublicHolidaysSettings() {
        PublicHolidaysSettingsDTO dto = new PublicHolidaysSettingsDTO(null, null, FederalStateDTO.GERMANY_RHEINLAND_PFALZ);

        final PublicHolidaysSettings publicHolidaysSettings = dto.toPublicHolidaysSettings();
        assertThat(publicHolidaysSettings.getFederalState()).isEqualTo(FederalState.GERMANY_RHEINLAND_PFALZ);
    }
}
