package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ThemePropertyEditorTest {

    private ThemePropertyEditor sut;

    @BeforeEach
    void setUp() {
        sut = new ThemePropertyEditor();
    }

    @ParameterizedTest
    @EnumSource(Theme.class)
    void ensureThemeNameIsMappedToTheme(Theme givenTheme) {
        sut.setAsText(givenTheme.name());
        assertThat(sut.getValue()).isEqualTo(givenTheme);
    }

    @ParameterizedTest
    @ValueSource(strings = {"dark", "Dark", "dArK"})
    void ensureThemeNameIsMappedCaseInsensitive(String givenThemeName) {
        sut.setAsText(givenThemeName);
        assertThat(sut.getValue()).isEqualTo(Theme.DARK);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "UNKNOWN_THEME", "someTheme"})
    void ensureUnknownThemeNameFallsBackToSystem(String givenThemeName) {
        sut.setAsText(givenThemeName);
        assertThat(sut.getValue()).isEqualTo(Theme.SYSTEM);
    }

    @ParameterizedTest
    @EnumSource(Theme.class)
    void ensureThemeIsMappedToItsName(Theme givenTheme) {
        sut.setValue(givenTheme);
        assertThat(sut.getAsText()).isEqualTo(givenTheme.name());
    }

    @Test
    void ensureNullThemeIsMappedToEmptyText() {
        sut.setValue(null);
        assertThat(sut.getAsText()).isEmpty();
    }
}
