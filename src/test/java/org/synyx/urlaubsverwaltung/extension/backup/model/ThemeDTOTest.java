package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.user.Theme;

import static org.assertj.core.api.Assertions.assertThat;

class ThemeDTOTest {
    @ParameterizedTest
    @EnumSource(Theme.class)
    void happyPathThemeToDTO(Theme theme) {
        ThemeDTO themeDTO = ThemeDTO.valueOf(theme.name());
        assertThat(themeDTO).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(ThemeDTO.class)
    void happyPathDTOToTheme(ThemeDTO themeDTO) {
        Theme theme = themeDTO.toTheme();
        assertThat(theme).isNotNull();
    }
}
