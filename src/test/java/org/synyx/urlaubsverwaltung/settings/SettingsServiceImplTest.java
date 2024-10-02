package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsServiceImplTest {

    private SettingsServiceImpl sut;

    @Mock
    private SettingsRepository settingsRepository;

    @BeforeEach
    void setUp() {
        sut = new SettingsServiceImpl(settingsRepository);
    }

    @Test
    void ensureGetSettingsReturnsFromDB() {
        final Settings settings = new Settings();
        when(settingsRepository.findAll()).thenReturn(List.of(settings));

        final Settings actualSettings = sut.getSettings();
        assertThat(actualSettings).isEqualTo(settings);
    }

    @Test
    void ensureGetSettingsRequiresInitializationFirst() {

        when(settingsRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> sut.getSettings())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No settings found in database!");

    }
}
