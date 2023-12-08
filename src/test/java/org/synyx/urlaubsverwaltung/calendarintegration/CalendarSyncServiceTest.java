package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CalendarSyncServiceTest {

    @Mock
    private SettingsService settingsService;
    @Mock
    private CalendarProviderService calendarService;
    @Mock
    private AbsenceMappingRepository absenceMappingRepository;

    private CalendarSyncService sut;
    private Settings settings;

    @BeforeEach
    void setUp() {
        when(calendarService.getCalendarProvider()).thenReturn(Optional.of(mock(ExchangeCalendarProvider.class)));
        sut = new CalendarSyncService(settingsService, calendarService, absenceMappingRepository);
    }

}
