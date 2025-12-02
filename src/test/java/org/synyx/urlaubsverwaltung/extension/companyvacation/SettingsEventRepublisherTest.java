package org.synyx.urlaubsverwaltung.extension.companyvacation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.settings.WorkingDurationForChristmasEveUpdatedEvent;
import org.synyx.urlaubsverwaltung.settings.WorkingDurationForNewYearsEveUpdatedEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsEventRepublisherTest {

    @Mock
    private SettingsService settingsService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Captor
    private ArgumentCaptor<Object> eventCaptor;

    private SettingsEventRepublisher sut;

    @BeforeEach
    void setUp() {
        sut = new SettingsEventRepublisher(settingsService, applicationEventPublisher);
    }

    @Test
    void republishEvents_publishesEventsBasedOnPublicHolidaySettings() {
        final Settings settings = mock(Settings.class);
        final PublicHolidaysSettings publicHolidaysSettings = mock(PublicHolidaysSettings.class);
        when(settingsService.getSettings()).thenReturn(settings);
        when(settings.getPublicHolidaysSettings()).thenReturn(publicHolidaysSettings);
        when(publicHolidaysSettings.getWorkingDurationForChristmasEve()).thenReturn(DayLength.MORNING);
        when(publicHolidaysSettings.getWorkingDurationForNewYearsEve()).thenReturn(DayLength.NOON);

        sut.republishEvents();

        verify(settingsService).getSettings();
        // Erfasse beide Events in einem Verify-Aufruf
        verify(applicationEventPublisher, times(2)).publishEvent(eventCaptor.capture());

        assertThat(eventCaptor.getAllValues()).hasSize(2);
        assertThat(eventCaptor.getAllValues().get(0)).isInstanceOf(WorkingDurationForChristmasEveUpdatedEvent.class);
        assertThat(eventCaptor.getAllValues().get(1)).isInstanceOf(WorkingDurationForNewYearsEveUpdatedEvent.class);

        final WorkingDurationForChristmasEveUpdatedEvent christmasEvent = (WorkingDurationForChristmasEveUpdatedEvent) eventCaptor.getAllValues().get(0);
        assertThat(christmasEvent.workingDurationForChristmasEve()).isEqualTo(DayLength.MORNING);

        final WorkingDurationForNewYearsEveUpdatedEvent newYearsEvent = (WorkingDurationForNewYearsEveUpdatedEvent) eventCaptor.getAllValues().get(1);
        assertThat(newYearsEvent.workingDurationForNewYearsEve()).isEqualTo(DayLength.NOON);
    }
}
