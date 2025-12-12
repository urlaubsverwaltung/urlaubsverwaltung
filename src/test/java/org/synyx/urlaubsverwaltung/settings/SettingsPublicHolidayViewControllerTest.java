package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsPublicHolidayViewControllerTest {

    @Mock
    private SettingsService settingsService;
    @Mock
    private SettingsPublicHolidayValidator settingsValidator;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private SettingsPublicHolidayViewController sut;

    @BeforeEach
    void setup() {
        sut = new SettingsPublicHolidayViewController(settingsService, settingsValidator, applicationEventPublisher);
    }

    @Test
    void settingsSaved_publishesEventsOnSuccessfulSave() {
        final PublicHolidaysSettings publicHolidaysSettings = new PublicHolidaysSettings();
        publicHolidaysSettings.setFederalState(org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG);
        publicHolidaysSettings.setWorkingDurationForChristmasEve(DayLength.MORNING);
        publicHolidaysSettings.setWorkingDurationForNewYearsEve(DayLength.NOON);

        final SettingsPublicHolidayDto dto = new SettingsPublicHolidayDto();
        dto.setId(42L);
        dto.setPublicHolidaysSettings(publicHolidaysSettings);

        final Errors errors = new BeanPropertyBindingResult(dto, "settings");
        final Model model = new ConcurrentModel();
        final RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        final Settings existing = new Settings();
        when(settingsService.getSettings()).thenReturn(existing);

        final String view = sut.settingsSaved(dto, errors, model, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/web/settings/public-holidays");
        assertThat(redirectAttributes.getFlashAttributes().get("success")).isEqualTo(Boolean.TRUE);

        verify(settingsService).save(existing);
        assertThat(existing.getPublicHolidaysSettings()).isSameAs(publicHolidaysSettings);

        final ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher, times(2)).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues()).hasSize(2);

        assertThat(eventCaptor.getAllValues().stream()
            .filter(WorkingDurationForChristmasEveUpdatedEvent.class::isInstance)
            .map(e -> ((WorkingDurationForChristmasEveUpdatedEvent) e).workingDurationForChristmasEve())
            .anyMatch(dl -> dl == DayLength.MORNING)).isTrue();
        assertThat(eventCaptor.getAllValues().stream()
            .filter(WorkingDurationForNewYearsEveUpdatedEvent.class::isInstance)
            .map(e -> ((WorkingDurationForNewYearsEveUpdatedEvent) e).workingDurationForNewYearsEve())
            .anyMatch(dl -> dl == DayLength.NOON)).isTrue();
    }

    @Test
    void settingsSaved_doesNotPublishEventsWhenValidationFails() {
        final SettingsPublicHolidayDto dto = new SettingsPublicHolidayDto();
        dto.setId(1L);
        final Errors errors = new BeanPropertyBindingResult(dto, "settings");
        errors.reject("invalid");
        final Model model = new ConcurrentModel();
        final RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        final String view = sut.settingsSaved(dto, errors, model, redirectAttributes);

        assertThat(view).isEqualTo("settings/public-holidays/settings_public_holidays");
        verifyNoInteractions(settingsService, applicationEventPublisher);
    }
}
