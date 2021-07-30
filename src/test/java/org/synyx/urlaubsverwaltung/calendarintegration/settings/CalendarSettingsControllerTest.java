package org.synyx.urlaubsverwaltung.calendarintegration.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.CalendarProvider;

import javax.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class CalendarSettingsControllerTest {

    private static final String OATUH_REDIRECT_REL = "/google-api-handshake";
    private static final String ERRORS_ATTRIBUTE = "errors";
    private static final String OAUTH_ERROR_ATTRIBUTE = "oautherrors";
    private static final String OAUTH_ERROR_VALUE = "some-error";

    private static final CalendarProvider SOME_CALENDAR_PROVIDER = new SomeCalendarProvider();
    private static final CalendarProvider ANOTHER_CALENDAR_PROVIDER = new AnotherCalendarProvider();
    private static final List<CalendarProvider> CALENDAR_PROVIDER_LIST = List.of(SOME_CALENDAR_PROVIDER, ANOTHER_CALENDAR_PROVIDER);

    private static final String SOME_GOOGLE_REFRESH_TOKEN = "0815-4711-242";
    public static final String CALENDAR_SETTINGS = "/web/calendar/settings";

    private final Clock clock = Clock.systemUTC();

    @Mock
    private CalendarSettingsService calendarSettingsService;

    private CalendarSettingsController sut;

    @BeforeEach
    void setUp() {
        sut = new CalendarSettingsController(calendarSettingsService, CALENDAR_PROVIDER_LIST);
    }

    @Test
    void ensureSettingsDetailsAddsOAuthErrorToModelIfErrorProvidedAndNoCurrentRefreshToken() throws Exception {

        setupCalenderSettings();

        perform(get(CALENDAR_SETTINGS)
            .param(OAUTH_ERROR_ATTRIBUTE, OAUTH_ERROR_VALUE))
            .andExpect(model().attribute(OAUTH_ERROR_ATTRIBUTE, OAUTH_ERROR_VALUE))
            .andExpect(model().attribute(ERRORS_ATTRIBUTE, OAUTH_ERROR_VALUE));
    }

    @Test
    void ensureSettingsDetailsDoesNotAddOAuthErrorToModelIfErrorProvidedAndCurrentRefreshToken() throws Exception {

        setupCalenderSettings().getGoogleCalendarSettings().setRefreshToken(SOME_GOOGLE_REFRESH_TOKEN);

        perform(get(CALENDAR_SETTINGS)
            .param(OAUTH_ERROR_ATTRIBUTE, OAUTH_ERROR_VALUE))
            .andExpect(model().attribute(OAUTH_ERROR_ATTRIBUTE, nullValue()))
            .andExpect(model().attribute(ERRORS_ATTRIBUTE, nullValue()));
    }

    @Test
    void ensureSettingsDetailsDoesNotAddOAuthErrorToModelIfNoErrorProvidedAndNoCurrentRefreshToken() throws Exception {

        setupCalenderSettings();

        perform(get(CALENDAR_SETTINGS))
            .andExpect(model().attribute(OAUTH_ERROR_ATTRIBUTE, nullValue()))
            .andExpect(model().attribute(ERRORS_ATTRIBUTE, nullValue()));
    }

    @Test
    void ensureSettingsDetailsDoesNotAddOAuthErrorToModelIfNoErrorProvidedAndCurrentRefreshToken() throws Exception {

        setupCalenderSettings().getGoogleCalendarSettings().setRefreshToken(SOME_GOOGLE_REFRESH_TOKEN);

        perform(get(CALENDAR_SETTINGS))
            .andExpect(model().attribute(OAUTH_ERROR_ATTRIBUTE, nullValue()))
            .andExpect(model().attribute(ERRORS_ATTRIBUTE, nullValue()));
    }

    @Test
    void ensureSettingsDetailsSetsDefaultExchangeTimeZoneIfNoneConfigured() throws Exception {

        setupCalenderSettings();

        perform(get(CALENDAR_SETTINGS))
            .andExpect(model().attribute("timeZoneId", clock.getZone().getId()));
    }

    @Test
    void ensureSettingsDetailsDoesNotAlterExchangeTimeZoneIfAlreadyConfigured() throws Exception {
        //TODO: fix test setup?!

        //setupCalenderSettings().getExchangeCalendarSettings().setTimeZoneId("XYZ");

        perform(get(CALENDAR_SETTINGS));
    }

    @Test
    void ensureSettingsDetailsUsesCorrectView() throws Exception {

        //setupCalenderSettings();

        perform(get(CALENDAR_SETTINGS)).andExpect(view().name("calendarintegration/calendar_settings"));
    }

    @Test
    void ensureSettingsSavedSavesSettingsIfValidationSuccessfully() throws Exception {

        //TODO: add missing params
        perform(post(CALENDAR_SETTINGS));
        verify(calendarSettingsService).save(any(HttpServletRequest.class), any(CalendarSettingsDto.class));
    }

    @Test
    void ensureSettingsSavedAddFlashAttributeAndRedirectsToSettings() throws Exception {

        //TODO: is this redirect correct?
        perform(post(CALENDAR_SETTINGS)).andExpect(flash().attribute("success", true));

        perform(post(CALENDAR_SETTINGS))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/settings"));
    }

    private static CalendarSettingsEntity someSettings() {

        return new CalendarSettingsEntity();
    }

    private CalendarSettingsEntity setupCalenderSettings() {
        final CalendarSettingsEntity settings = someSettings();
        when(calendarSettingsService.getSettings()).thenReturn(settings);
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
