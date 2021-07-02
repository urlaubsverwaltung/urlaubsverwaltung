package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OvertimeSettingsTest {

    @Test
    void ensureDefaultValues() {

        final OvertimeSettings settings = new OvertimeSettings();
        assertThat(settings.isOvertimeActive()).isFalse();
        assertThat(settings.isOvertimeWritePrivilegedOnly()).isFalse();
        assertThat(settings.isOvertimeReductionWithoutApplicationActive()).isTrue();
        assertThat(settings.getMaximumOvertime()).isEqualTo(100);
        assertThat(settings.getMinimumOvertime()).isEqualTo(5);
    }
}
