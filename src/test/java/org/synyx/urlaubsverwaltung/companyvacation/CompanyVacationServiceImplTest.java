package org.synyx.urlaubsverwaltung.companyvacation;

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

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyVacationServiceImplTest {

    private CompanyVacationServiceImpl sut;

    @Captor
    private ArgumentCaptor<CompanyVacationPublishedEvent> companyVacationPublishedEventArgumentCaptor;
    @Captor
    private ArgumentCaptor<CompanyVacationDeletedEvent> companyVacationDeletedEventArgumentCaptor;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new CompanyVacationServiceImpl(settingsService, Clock.systemUTC(), applicationEventPublisher);
    }

    @Test
    void publishesCompanyVacationForConvertChristmasEveToCompanyVacationEventWhenWorkingDurationIsNotFull() {
        final WorkingDurationForChristmasEveUpdatedEvent event = new WorkingDurationForChristmasEveUpdatedEvent(DayLength.MORNING);

        sut.handleWorkingDurationForChristmasEveUpdatedEvent(event);

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(CompanyVacationPublishedEvent.class);
        final CompanyVacationPublishedEvent publishedEvent = (CompanyVacationPublishedEvent) captor.getValue();

        // Inverse of MORNING -> NOON
        assertThat(publishedEvent.dayLength()).isEqualTo(DayLength.NOON);

        final int currentYear = LocalDate.now().getYear();
        assertThat(publishedEvent.startDate()).isEqualTo(LocalDate.of(currentYear, 12, 24));
        assertThat(publishedEvent.endDate()).isEqualTo(LocalDate.of(currentYear, 12, 24));
        assertThat(publishedEvent.sourceId()).isEqualTo("settings-christmas-eve");
    }

    @Test
    void deletesCompanyVacationForConvertChristmasEveToCompanyVacationEventFullWorkingDay() {
        final WorkingDurationForChristmasEveUpdatedEvent event = new WorkingDurationForChristmasEveUpdatedEvent(DayLength.FULL);

        sut.handleWorkingDurationForChristmasEveUpdatedEvent(event);

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(CompanyVacationDeletedEvent.class);
        final CompanyVacationDeletedEvent deletedEvent = (CompanyVacationDeletedEvent) captor.getValue();
        assertThat(deletedEvent.sourceId()).isEqualTo("settings-christmas-eve");
    }

    @Test
    void publishesCompanyVacationForConvertNewYearsEveToCompanyVacationEventWhenWorkingDurationIsNotFull() {
        final WorkingDurationForNewYearsEveUpdatedEvent event = new WorkingDurationForNewYearsEveUpdatedEvent(DayLength.NOON);

        sut.handleWorkingDurationForNewYearsEveUpdatedEvent(event);

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(CompanyVacationPublishedEvent.class);
        final CompanyVacationPublishedEvent publishedEvent = (CompanyVacationPublishedEvent) captor.getValue();

        // Inverse of NOON -> MORNING
        assertThat(publishedEvent.dayLength()).isEqualTo(DayLength.MORNING);

        final int currentYear = LocalDate.now().getYear();
        assertThat(publishedEvent.startDate()).isEqualTo(LocalDate.of(currentYear, 12, 31));
        assertThat(publishedEvent.endDate()).isEqualTo(LocalDate.of(currentYear, 12, 31));
        assertThat(publishedEvent.sourceId()).isEqualTo("settings-new-years-eve");
    }

    @Test
    void deletesCompanyVacationForConvertNewYearsEveToCompanyVacationEventWhenFullWorkingDay() {
        final WorkingDurationForNewYearsEveUpdatedEvent event = new WorkingDurationForNewYearsEveUpdatedEvent(DayLength.FULL);

        sut.handleWorkingDurationForNewYearsEveUpdatedEvent(event);

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(CompanyVacationDeletedEvent.class);
        final CompanyVacationDeletedEvent deletedEvent = (CompanyVacationDeletedEvent) captor.getValue();
        assertThat(deletedEvent.sourceId()).isEqualTo("settings-new-years-eve");
    }

    @Test
    void republishEvents_publishesEventsBasedOnPublicHolidaySettings() {
        final Settings settings = mock(Settings.class);
        final PublicHolidaysSettings publicHolidaysSettings = mock(PublicHolidaysSettings.class);
        when(settingsService.getSettings()).thenReturn(settings);
        when(settings.getPublicHolidaysSettings()).thenReturn(publicHolidaysSettings);
        when(publicHolidaysSettings.getWorkingDurationForChristmasEve()).thenReturn(DayLength.MORNING);
        when(publicHolidaysSettings.getWorkingDurationForNewYearsEve()).thenReturn(DayLength.NOON);

        sut.publishCompanyEvents();

        verify(settingsService).getSettings();
        verify(applicationEventPublisher, times(2)).publishEvent(companyVacationPublishedEventArgumentCaptor.capture());

        assertThat(companyVacationPublishedEventArgumentCaptor.getAllValues()).hasSize(2);
        final CompanyVacationPublishedEvent newYearsEvent = companyVacationPublishedEventArgumentCaptor.getAllValues().get(0);
        assertThat(newYearsEvent.dayLength()).isEqualTo(DayLength.NOON);
        final CompanyVacationPublishedEvent christmasEvent = companyVacationPublishedEventArgumentCaptor.getAllValues().get(1);
        assertThat(christmasEvent.dayLength()).isEqualTo(DayLength.MORNING);
    }

    @Test
    void republishEvents_ensureToPublisheDeleteEventsIfDayLengthIsZero() {
        final Settings settings = mock(Settings.class);
        final PublicHolidaysSettings publicHolidaysSettings = mock(PublicHolidaysSettings.class);
        when(settingsService.getSettings()).thenReturn(settings);
        when(settings.getPublicHolidaysSettings()).thenReturn(publicHolidaysSettings);
        when(publicHolidaysSettings.getWorkingDurationForChristmasEve()).thenReturn(DayLength.FULL);
        when(publicHolidaysSettings.getWorkingDurationForNewYearsEve()).thenReturn(DayLength.FULL);

        sut.publishCompanyEvents();

        verify(settingsService).getSettings();
        verify(applicationEventPublisher, times(2)).publishEvent(companyVacationDeletedEventArgumentCaptor.capture());

        assertThat(companyVacationDeletedEventArgumentCaptor.getAllValues()).hasSize(2);
        final CompanyVacationDeletedEvent christmasEvent = companyVacationDeletedEventArgumentCaptor.getAllValues().get(0);
        assertThat(christmasEvent.sourceId()).isEqualTo("settings-christmas-eve");
        final CompanyVacationDeletedEvent newYearsEvent = companyVacationDeletedEventArgumentCaptor.getAllValues().get(1);
        assertThat(newYearsEvent.sourceId()).isEqualTo("settings-new-years-eve");

    }

    @Test
    void handleInitialDefaultSettingsSavedEvent_publishesEventsBasedOnPublicHolidaySettings() {
        final Settings settings = mock(Settings.class);
        final PublicHolidaysSettings publicHolidaysSettings = mock(PublicHolidaysSettings.class);
        when(settingsService.getSettings()).thenReturn(settings);
        when(settings.getPublicHolidaysSettings()).thenReturn(publicHolidaysSettings);
        when(publicHolidaysSettings.getWorkingDurationForChristmasEve()).thenReturn(DayLength.MORNING);
        when(publicHolidaysSettings.getWorkingDurationForNewYearsEve()).thenReturn(DayLength.NOON);

        sut.handleInitialDefaultSettingsSavedEvent();

        verify(settingsService).getSettings();
        verify(applicationEventPublisher, times(2)).publishEvent(companyVacationPublishedEventArgumentCaptor.capture());

        assertThat(companyVacationPublishedEventArgumentCaptor.getAllValues()).hasSize(2);
        final CompanyVacationPublishedEvent newYearsEvent = companyVacationPublishedEventArgumentCaptor.getAllValues().get(0);
        assertThat(newYearsEvent.dayLength()).isEqualTo(DayLength.NOON);
        final CompanyVacationPublishedEvent christmasEvent = companyVacationPublishedEventArgumentCaptor.getAllValues().get(1);
        assertThat(christmasEvent.dayLength()).isEqualTo(DayLength.MORNING);
    }
}
