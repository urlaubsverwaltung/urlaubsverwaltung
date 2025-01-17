package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;

import static org.assertj.core.api.Assertions.assertThat;

class OverTimeSettingsDTOTest {

    @Test
    void happyPathOvertimeSettingsToDTO() {
        OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(true);
        overtimeSettings.setOvertimeReductionWithoutApplicationActive(true);
        overtimeSettings.setOvertimeWritePrivilegedOnly(true);
        overtimeSettings.setMaximumOvertime(10);
        overtimeSettings.setMinimumOvertime(5);
        overtimeSettings.setMinimumOvertimeReduction(2);

        final OverTimeSettingsDTO dto = OverTimeSettingsDTO.of(overtimeSettings);

        assertThat(dto.overtimeActive()).isEqualTo(overtimeSettings.isOvertimeActive());
        assertThat(dto.overtimeReductionWithoutApplicationActive()).isEqualTo(overtimeSettings.isOvertimeReductionWithoutApplicationActive());
        assertThat(dto.overtimeWritePrivilegedOnly()).isEqualTo(overtimeSettings.isOvertimeWritePrivilegedOnly());
        assertThat(dto.maximumOvertime()).isEqualTo(overtimeSettings.getMaximumOvertime());
        assertThat(dto.minimumOvertime()).isEqualTo(overtimeSettings.getMinimumOvertime());
        assertThat(dto.minimumOvertimeReduction()).isEqualTo(overtimeSettings.getMinimumOvertimeReduction());
    }


    @Test
    void happyPathDTOToOvertimeSettings() {
        OverTimeSettingsDTO dto = new OverTimeSettingsDTO(true, true, true, 10, 5, 2);

        final OvertimeSettings overtimeSettings = dto.toOverTimeSettings();

        assertThat(overtimeSettings.isOvertimeActive()).isEqualTo(dto.overtimeActive());
        assertThat(overtimeSettings.isOvertimeReductionWithoutApplicationActive()).isEqualTo(dto.overtimeReductionWithoutApplicationActive());
        assertThat(overtimeSettings.isOvertimeWritePrivilegedOnly()).isEqualTo(dto.overtimeWritePrivilegedOnly());
        assertThat(overtimeSettings.getMaximumOvertime()).isEqualTo(dto.maximumOvertime());
        assertThat(overtimeSettings.getMinimumOvertime()).isEqualTo(dto.minimumOvertime());
        assertThat(overtimeSettings.getMinimumOvertimeReduction()).isEqualTo(dto.minimumOvertimeReduction());
    }

}
