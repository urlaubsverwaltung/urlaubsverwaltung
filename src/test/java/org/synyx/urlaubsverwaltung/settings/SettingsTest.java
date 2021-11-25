package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.settings.Settings}.
 */
class SettingsTest {

    @Test
    void ensureDefaultValues() {

        Settings settings = new Settings();

        assertThat(settings.getApplicationSettings()).isNotNull();
        assertThat(settings.getWorkingTimeSettings()).isNotNull();
    }

    @Test
    void equals() {
        final Settings settingsOne = new Settings();
        settingsOne.setId(1);

        final Settings settingsOneOne = new Settings();
        settingsOneOne.setId(1);

        final Settings settingsTwo = new Settings();
        settingsTwo.setId(2);

        assertThat(settingsOne)
            .isEqualTo(settingsOne)
            .isEqualTo(settingsOneOne)
            .isNotEqualTo(settingsTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final Settings settingsOne = new Settings();
        settingsOne.setId(1);

        assertThat(settingsOne.hashCode()).isEqualTo(32);
    }
}
