package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosure;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettings;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;

import static java.time.Month.DECEMBER;
import static java.time.Month.NOVEMBER;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
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
    private CompanyClosureService companyClosureService;
    @Mock
    private PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    @Mock
    private PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;

    private SettingsPublicHolidayViewController sut;

    @BeforeEach
    void setup() {
        sut = new SettingsPublicHolidayViewController(settingsService, settingsValidator, companyClosureService,
            Clock.fixed(Instant.parse("2026-11-06T10:00:00Z"), UTC), defaultPersonSuggestionUrlStrategy, personSearchUiFragmentSupplier);
    }

    @Nested
    class PersonSearch {

        @Test
        void personSearchUiFragmentSupplier() {
            assertThat(sut.personSearchUiFragmentSupplier()).isSameAs(personSearchUiFragmentSupplier);
        }

        @Test
        void returnsInjectedStrategy() {
            assertThat(sut.personSuggestionUrlStrategy()).isSameAs(defaultPersonSuggestionUrlStrategy);
        }
    }

    @Test
    void showsWorkingDurationOfCurrentYear() {
        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);
        when(companyClosureService.getCompanyClosures(LocalDate.of(2026, DECEMBER, 24), LocalDate.of(2026, DECEMBER, 24)))
            .thenReturn(List.of(new CompanyClosure(LocalDate.of(2026, DECEMBER, 24), DayLength.NOON, "Heiligabend")));
        when(companyClosureService.getCompanyClosures(LocalDate.of(2026, DECEMBER, 31), LocalDate.of(2026, DECEMBER, 31)))
            .thenReturn(List.of());

        final Model model = new ConcurrentModel();
        sut.settingsDetails(model);

        final SettingsPublicHolidayDto dto = (SettingsPublicHolidayDto) model.getAttribute("settings");
        assertThat(dto.getWorkingDurationForChristmasEve()).isEqualTo(DayLength.MORNING);
        assertThat(dto.getWorkingDurationForNewYearsEve()).isEqualTo(DayLength.FULL);
    }

    @Test
    void showsWorkingDurationOfNextYearWhenDayHasPassed() {
        sut = new SettingsPublicHolidayViewController(settingsService, settingsValidator, companyClosureService,
            Clock.fixed(Instant.parse("2026-12-28T10:00:00Z"), UTC), defaultPersonSuggestionUrlStrategy, personSearchUiFragmentSupplier);
        when(settingsService.getSettings()).thenReturn(new Settings());
        when(companyClosureService.getCompanyClosures(LocalDate.of(2027, DECEMBER, 24), LocalDate.of(2027, DECEMBER, 24)))
            .thenReturn(List.of(new CompanyClosure(LocalDate.of(2027, DECEMBER, 24), DayLength.FULL, "Heiligabend")));
        when(companyClosureService.getCompanyClosures(LocalDate.of(2026, DECEMBER, 31), LocalDate.of(2026, DECEMBER, 31)))
            .thenReturn(List.of());

        final Model model = new ConcurrentModel();
        sut.settingsDetails(model);

        final SettingsPublicHolidayDto dto = (SettingsPublicHolidayDto) model.getAttribute("settings");
        assertThat(dto.getWorkingDurationForChristmasEve()).isEqualTo(DayLength.ZERO);
        assertThat(dto.getWorkingDurationForNewYearsEve()).isEqualTo(DayLength.FULL);
    }

    @Test
    void savesChangedDurationValidFromToday() {
        final PublicHolidaysSettings publicHolidaysSettings = new PublicHolidaysSettings();
        publicHolidaysSettings.setFederalState(FederalState.GERMANY_BADEN_WUERTTEMBERG);

        final SettingsPublicHolidayDto dto = new SettingsPublicHolidayDto();
        dto.setId(42L);
        dto.setPublicHolidaysSettings(publicHolidaysSettings);
        dto.setWorkingDurationForChristmasEve(DayLength.ZERO);
        dto.setWorkingDurationForNewYearsEve(DayLength.NOON);

        final Errors errors = new BeanPropertyBindingResult(dto, "settings");
        final RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        final Settings existing = new Settings();
        when(settingsService.getSettings()).thenReturn(existing);

        final String view = sut.settingsSaved(dto, errors, new ConcurrentModel(), redirectAttributes);

        assertThat(view).isEqualTo("redirect:/web/settings/public-holidays");
        assertThat(redirectAttributes.getFlashAttributes().get("success")).isEqualTo(Boolean.TRUE);

        verify(settingsService).save(existing);
        assertThat(existing.getPublicHolidaysSettings().getFederalState()).isEqualTo(FederalState.GERMANY_BADEN_WUERTTEMBERG);

        verify(companyClosureService).changeCompanyClosure(MonthDay.of(DECEMBER, 24), DayLength.FULL, LocalDate.of(2026, NOVEMBER, 6));
        verify(companyClosureService).changeCompanyClosure(MonthDay.of(DECEMBER, 31), DayLength.MORNING, LocalDate.of(2026, NOVEMBER, 6));
    }

    @Test
    void settingsSaved_doesNotSaveWhenValidationFails() {
        final SettingsPublicHolidayDto dto = new SettingsPublicHolidayDto();
        dto.setId(1L);
        final Errors errors = new BeanPropertyBindingResult(dto, "settings");
        errors.reject("invalid");
        final Model model = new ConcurrentModel();
        final RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        final String view = sut.settingsSaved(dto, errors, model, redirectAttributes);

        assertThat(view).isEqualTo("settings/public-holidays/settings_public_holidays");
        verifyNoInteractions(settingsService, companyClosureService);
    }
}
