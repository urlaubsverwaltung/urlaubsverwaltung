package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SettingsImportServiceTest {

    private SettingsImportService sut;

    @Mock
    private SettingsRepository settingsRepository;
    @Mock
    private SettingsCache settingsCache;

    @BeforeEach
    void setUp() {
        sut = new SettingsImportService(settingsRepository, settingsCache);
    }

    @Test
    void ensureImportSettingsInvalidatesTheCacheAfterSaving() {

        final Settings settings = new Settings();

        sut.importSettings(settings);

        final InOrder inOrder = inOrder(settingsRepository, settingsCache);
        inOrder.verify(settingsRepository).save(settings);
        inOrder.verify(settingsCache).invalidate();
    }

    @Test
    void ensureDeleteAllInvalidatesTheCache() {

        sut.deleteAll();

        verify(settingsRepository).deleteAll();
        verify(settingsCache).invalidate();
    }
}
