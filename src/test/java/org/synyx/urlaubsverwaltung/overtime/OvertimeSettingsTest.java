package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.overtime.settings.OvertimeSettingsEntity;

import static org.assertj.core.api.Assertions.assertThat;

class OvertimeSettingsTest {

    @Test
    void ensureDefaultValues() {

        final OvertimeSettingsEntity settings = new OvertimeSettingsEntity();
        assertThat(settings.isOvertimeActive()).isFalse();
        assertThat(settings.isOvertimeWritePrivilegedOnly()).isFalse();
        assertThat(settings.isOvertimeReductionWithoutApplicationActive()).isTrue();
        assertThat(settings.getMaximumOvertime()).isEqualTo(100);
        assertThat(settings.getMinimumOvertime()).isEqualTo(5);
    }
}
