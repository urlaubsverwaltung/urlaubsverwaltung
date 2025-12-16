package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class SettingsServiceImplTest {

    private SettingsServiceImpl sut;

    @Mock
    private SettingsRepository settingsRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setUp() {
        sut = new SettingsServiceImpl(settingsRepository, applicationEventPublisher);
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

    @Test
    void insertDefaultSettings_savesAndPublishesEvent_whenRepositoryEmpty() {
        when(settingsRepository.count()).thenReturn(0L);
        when(settingsRepository.save(any(Settings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sut.insertDefaultSettings();

        verify(settingsRepository).save(any(Settings.class));
        verify(applicationEventPublisher).publishEvent(any(InitialDefaultSettingsSavedEvent.class));
    }

    @Test
    void insertDefaultSettings_doesNothing_whenRepositoryNotEmpty() {
        when(settingsRepository.count()).thenReturn(1L);

        sut.insertDefaultSettings();

        verify(settingsRepository, never()).save(any(Settings.class));
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}
