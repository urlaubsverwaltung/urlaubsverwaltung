package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.settings.TimeSettingsService;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettingsService;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.calendarintegration.settings.CalendarSettingsEntity;
import org.synyx.urlaubsverwaltung.calendarintegration.settings.CalendarSettingsService;
import org.synyx.urlaubsverwaltung.overtime.settings.OvertimeSettingsService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettingsService;
import org.synyx.urlaubsverwaltung.specialleave.SpecialLeaveSettingsService;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.settings.WorkingTimeSettingsService;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class SettingsViewControllerTest {

    private SettingsViewController sut;

    private static final String OATUH_REDIRECT_REL = "/google-api-handshake";
    private static final String ERRORS_ATTRIBUTE = "errors";
    private static final String OAUTH_ERROR_ATTRIBUTE = "oautherrors";
    private static final String OAUTH_ERROR_VALUE = "some-error";

    private static final CalendarProvider SOME_CALENDAR_PROVIDER = new SomeCalendarProvider();
    private static final CalendarProvider ANOTHER_CALENDAR_PROVIDER = new AnotherCalendarProvider();
    private static final List<CalendarProvider> CALENDAR_PROVIDER_LIST = List.of(SOME_CALENDAR_PROVIDER, ANOTHER_CALENDAR_PROVIDER);

    private static final String SOME_GOOGLE_REFRESH_TOKEN = "0815-4711-242";

    @Mock
    private CalendarSettingsService settingsService;
    @Mock
    private WorkingTimeSettingsService workingTimeSettingsService;
    @Mock
    private TimeSettingsService timeSettingsService;
    @Mock
    private SickNoteSettingsService sickNoteSettingsSerivce;
    @Mock
    private OvertimeSettingsService overtimeSettingsSerivce;
    @Mock
    private org.synyx.urlaubsverwaltung.account.settings.AccountSettingsService accountSettingsService;
    @Mock
    private ApplicationSettingsService applicationSettingsService;
    @Mock
    private CalendarSettingsService calendarSettingsService;
    @Mock
    private SpecialLeaveSettingsService specialLeaveSettingsService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new SettingsViewController(workingTimeSettingsService, timeSettingsService, sickNoteSettingsSerivce, overtimeSettingsSerivce, accountSettingsService, applicationSettingsService, calendarSettingsService, specialLeaveSettingsService);
    }

    @Test
    void ensureSettingsDetailsFillsModelCorrectly() throws Exception {

        final CalendarSettingsEntity settings = someSettings();
        when(settingsService.getSettings()).thenReturn(settings);

        final String requestUrl = "/web/settings";

        perform(get(requestUrl))
            .andExpect(model().attribute("settings", settings))
            .andExpect(model().attribute("federalStateTypes", FederalState.values()))
            .andExpect(model().attribute("dayLengthTypes", DayLength.values()))
            .andExpect(model().attribute("providers", contains("SomeCalendarProvider", "AnotherCalendarProvider")))
            .andExpect(model().attribute("availableTimezones", containsInAnyOrder(TimeZone.getAvailableIDs())))
            .andExpect(model().attribute("defaultVacationDaysFromSettings", is(false)))
            .andExpect(model().attribute("defaultWorkingTimeFromSettings", is(false)));
    }

    @Test
    void ensureSettingsDetailsAddsOAuthErrorToModelIfErrorProvidedAndNoCurrentRefreshToken() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettingsWithoutGoogleCalendarRefreshToken());

        perform(get("/web/settings")
            .param(OAUTH_ERROR_ATTRIBUTE, OAUTH_ERROR_VALUE))
            .andExpect(model().attribute(OAUTH_ERROR_ATTRIBUTE, OAUTH_ERROR_VALUE))
            .andExpect(model().attribute(ERRORS_ATTRIBUTE, OAUTH_ERROR_VALUE));
    }

    @Test
    void ensureSettingsDetailsDoesNotAddOAuthErrorToModelIfErrorProvidedAndCurrentRefreshToken() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettingsWithGoogleCalendarRefreshToken());

        perform(get("/web/settings")
            .param(OAUTH_ERROR_ATTRIBUTE, OAUTH_ERROR_VALUE))
            .andExpect(model().attribute(OAUTH_ERROR_ATTRIBUTE, nullValue()))
            .andExpect(model().attribute(ERRORS_ATTRIBUTE, nullValue()));
    }

    @Test
    void ensureSettingsDetailsDoesNotAddOAuthErrorToModelIfNoErrorProvidedAndNoCurrentRefreshToken() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettingsWithoutGoogleCalendarRefreshToken());

        perform(get("/web/settings"))
            .andExpect(model().attribute(OAUTH_ERROR_ATTRIBUTE, nullValue()))
            .andExpect(model().attribute(ERRORS_ATTRIBUTE, nullValue()));
    }

    @Test
    void ensureSettingsDetailsDoesNotAddOAuthErrorToModelIfNoErrorProvidedAndCurrentRefreshToken() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettingsWithGoogleCalendarRefreshToken());

        perform(get("/web/settings"))
            .andExpect(model().attribute(OAUTH_ERROR_ATTRIBUTE, nullValue()))
            .andExpect(model().attribute(ERRORS_ATTRIBUTE, nullValue()));
    }

    @Test
    void ensureSettingsDetailsSetsDefaultExchangeTimeZoneIfNoneConfigured() throws Exception {

        final CalendarSettingsEntity settings = someSettingsWithNoExchangeTimezone();
        when(settingsService.getSettings()).thenReturn(settings);

        assertThat(settings.getExchangeCalendarSettings().getTimeZoneId()).isNull();

        perform(get("/web/settings"));

        assertThat(settings.getExchangeCalendarSettings().getTimeZoneId())
            .isEqualTo(clock.getZone().getId());
    }

    @Test
    void ensureSettingsDetailsDoesNotAlterExchangeTimeZoneIfAlreadyConfigured() throws Exception {

        final String timeZoneId = "XYZ";
        final CalendarSettingsEntity settings = someSettingsWithExchangeTimeZone(timeZoneId);
        when(settingsService.getSettings()).thenReturn(someSettingsWithExchangeTimeZone(timeZoneId));

        assertThat(settings.getExchangeCalendarSettings().getTimeZoneId()).isEqualTo(timeZoneId);

        perform(get("/web/settings"));

        assertThat(settings.getExchangeCalendarSettings().getTimeZoneId()).isEqualTo(timeZoneId);
    }

    @Test
    void ensureSettingsDetailsUsesCorrectView() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettings());

        perform(get("/web/settings")).andExpect(view().name("settings/settings_form"));
    }

    @Test
    void ensureSettingsSavedSavesSettingsIfValidationSuccessfully() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettings());

        perform(post("/web/settings"));
        verify(settingsService).save(any(CalendarSettingsEntity.class));
    }

    @Test
    void ensureSettingsSavedAddFlashAttributeAndRedirectsToSettings() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettings());

        perform(post("/web/settings")).andExpect(flash().attribute("success", true));

        perform(post("/web/settings"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/settings"));
    }

    private static CalendarSettingsEntity someSettings() {

        return new CalendarSettingsEntity();
    }

    private static CalendarSettingsEntity someSettingsWithNoExchangeTimezone() {

        return someSettings();
    }

    private static CalendarSettingsEntity someSettingsWithExchangeTimeZone(String timeZoneId) {

        CalendarSettingsEntity settings = someSettings();
        settings.getExchangeCalendarSettings().setTimeZoneId(timeZoneId);

        return settings;
    }

    private static CalendarSettingsEntity someSettingsWithoutGoogleCalendarRefreshToken() {

        return someSettings();
    }

    private static CalendarSettingsEntity someSettingsWithGoogleCalendarRefreshToken() {

        CalendarSettingsEntity settings = someSettings();
        settings.getGoogleCalendarSettings().setRefreshToken(SOME_GOOGLE_REFRESH_TOKEN);

        return settings;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }

    private static class SomeCalendarProvider implements CalendarProvider {

        @Override
        public Optional<String> add(Absence absence, CalendarSettingsEntity calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalendarProvider impl.");
        }

        @Override
        public void update(Absence absence, String eventId, CalendarSettingsEntity calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalendarProvider impl.");
        }

        @Override
        public void delete(String eventId, CalendarSettingsEntity calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalendarProvider impl.");
        }

        @Override
        public void checkCalendarSyncSettings(CalendarSettingsEntity calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalendarProvider impl.");
        }
    }

    private static class AnotherCalendarProvider implements CalendarProvider {

        @Override
        public Optional<String> add(Absence absence, CalendarSettingsEntity calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalendarProvider impl.");
        }

        @Override
        public void update(Absence absence, String eventId, CalendarSettingsEntity calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalendarProvider impl.");
        }

        @Override
        public void delete(String eventId, CalendarSettingsEntity calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalendarProvider impl.");
        }

        @Override
        public void checkCalendarSyncSettings(CalendarSettingsEntity calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalendarProvider impl.");
        }
    }
}
