package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.calendar.CalendarAbsence;

import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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
class SettingsCalendarSyncViewControllerTest {

    private SettingsCalendarSyncViewController sut;

    private static final String OATUH_REDIRECT_REL = "/google-api-handshake";
    private static final String ERRORS_ATTRIBUTE = "errors";
    private static final String OAUTH_ERROR_ATTRIBUTE = "oautherrors";
    private static final String OAUTH_ERROR_VALUE = "some-error";

    private static final CalendarProvider SOME_CALENDAR_PROVIDER = new SomeCalendarProvider();
    private static final CalendarProvider ANOTHER_CALENDAR_PROVIDER = new AnotherCalendarProvider();
    private static final List<CalendarProvider> CALENDAR_PROVIDER_LIST = List.of(SOME_CALENDAR_PROVIDER, ANOTHER_CALENDAR_PROVIDER);

    private static final String SOME_GOOGLE_REFRESH_TOKEN = "0815-4711-242";

    @Mock
    private CalendarSettingsService calendarSettingsService;
    @Mock
    private SettingsCalendarSyncValidator settingsValidator;

    @BeforeEach
    void setUp() {
        sut = new SettingsCalendarSyncViewController(calendarSettingsService, CALENDAR_PROVIDER_LIST, settingsValidator);
    }

    @Test
    void getAuthorizedRedirectUrl() {
        final String actual = sut.getAuthorizedRedirectUrl("http://localhost:8080/web/settings/calendar-sync", OATUH_REDIRECT_REL);
        final String expected = "http://localhost:8080/web" + OATUH_REDIRECT_REL;
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void ensureSettingsDetailsFillsModelCorrectly() throws Exception {

        final CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setId(42L);
        when(calendarSettingsService.getCalendarSettings()).thenReturn(calendarSettings);

        final SettingsCalendarSyncDto expectedSettingsCalendarSyncDto = new SettingsCalendarSyncDto();
        expectedSettingsCalendarSyncDto.setId(42L);
        expectedSettingsCalendarSyncDto.setCalendarSettings(calendarSettings);

        perform(get("/web/settings/calendar-sync"))
            .andExpect(model().attribute("settings", allOf(
                hasProperty("id", is(42L)),
                hasProperty("calendarSettings", sameInstance(calendarSettings))
            )))
            .andExpect(model().attribute("providers", contains("NoSyncProvider", "SomeCalendarProvider", "AnotherCalendarProvider")))
            .andExpect(model().attribute("availableTimezones", containsInAnyOrder(TimeZone.getAvailableIDs())))
            .andExpect(model().attribute("authorizedRedirectUrl",
                sut.getAuthorizedRedirectUrl("http://localhost/web/settings/calendar-sync", OATUH_REDIRECT_REL)));
    }

    @Test
    void ensureSettingsDetailsAddsOAuthErrorToModelIfErrorProvidedAndNoCurrentRefreshToken() throws Exception {

        when(calendarSettingsService.getCalendarSettings()).thenReturn(someSettingsWithoutGoogleCalendarRefreshToken());

        perform(get("/web/settings/calendar-sync")
            .param(OAUTH_ERROR_ATTRIBUTE, OAUTH_ERROR_VALUE))
            .andExpect(model().attribute(OAUTH_ERROR_ATTRIBUTE, OAUTH_ERROR_VALUE))
            .andExpect(model().attribute(ERRORS_ATTRIBUTE, OAUTH_ERROR_VALUE));
    }

    @Test
    void ensureSettingsDetailsDoesNotAddOAuthErrorToModelIfErrorProvidedAndCurrentRefreshToken() throws Exception {

        when(calendarSettingsService.getCalendarSettings()).thenReturn(someSettingsWithGoogleCalendarRefreshToken());

        perform(get("/web/settings/calendar-sync")
            .param(OAUTH_ERROR_ATTRIBUTE, OAUTH_ERROR_VALUE))
            .andExpect(model().attribute(OAUTH_ERROR_ATTRIBUTE, nullValue()))
            .andExpect(model().attribute(ERRORS_ATTRIBUTE, nullValue()));
    }

    @Test
    void ensureSettingsDetailsDoesNotAddOAuthErrorToModelIfNoErrorProvidedAndNoCurrentRefreshToken() throws Exception {

        when(calendarSettingsService.getCalendarSettings()).thenReturn(someSettingsWithoutGoogleCalendarRefreshToken());

        perform(get("/web/settings/calendar-sync"))
            .andExpect(model().attribute(OAUTH_ERROR_ATTRIBUTE, nullValue()))
            .andExpect(model().attribute(ERRORS_ATTRIBUTE, nullValue()));
    }

    @Test
    void ensureSettingsDetailsDoesNotAddOAuthErrorToModelIfNoErrorProvidedAndCurrentRefreshToken() throws Exception {

        when(calendarSettingsService.getCalendarSettings()).thenReturn(someSettingsWithGoogleCalendarRefreshToken());

        perform(get("/web/settings/calendar-sync"))
            .andExpect(model().attribute(OAUTH_ERROR_ATTRIBUTE, nullValue()))
            .andExpect(model().attribute(ERRORS_ATTRIBUTE, nullValue()));
    }

    @Test
    void ensureSettingsDetailsUsesCorrectView() throws Exception {
        when(calendarSettingsService.getCalendarSettings()).thenReturn(new CalendarSettings());
        perform(get("/web/settings/calendar-sync"))
            .andExpect(view().name("settings/calendar/settings_calendar_sync"));
    }

    @Test
    void ensureSettingsSavedShowsFormIfValidationFails() throws Exception {

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("calendarSettings", "error");
            return null;
        }).when(settingsValidator).validate(any(), any());

        perform(
            post("/web/settings/calendar-sync")
                .param("calendarSettings.provider", "NoopCalendarSyncProvider")
                .param("calendarSettings.googleCalendarSettings.clientId", "")
                .param("calendarSettings.googleCalendarSettings.clientSecret", "")
                .param("calendarSettings.googleCalendarSettings.calendarId", "")
                .param("calendarSettings.googleCalendarSettings.authorizedRedirectUrl", "http://localhost:8080/web/google-api-handshake")
        )
            .andExpect(view().name("settings/calendar/settings_calendar_sync"));
    }

    @Test
    void ensureSettingsSavedSavesSettingsIfValidationSuccessfully() throws Exception {

        when(calendarSettingsService.getCalendarSettings()).thenReturn(new CalendarSettings());

        perform(
            post("/web/settings/calendar-sync")
                .param("absenceTypeSettings.items[0].id", "10")
                .param("specialLeaveSettings.specialLeaveSettingsItems[0].id", "11")
                .param("calendarSettings.clientId", "clientId")
        );

        verify(calendarSettingsService).save(any(CalendarSettings.class));
    }

    @Test
    void ensureSettingsSavedAddFlashAttributeAndRedirectsToSettings() throws Exception {

        when(calendarSettingsService.getCalendarSettings()).thenReturn(new CalendarSettings());

        perform(
            post("/web/settings/calendar-sync")
                .param("absenceTypeSettings.items[0].id", "10")
                .param("specialLeaveSettings.specialLeaveSettingsItems[0].id", "11")
                .param("calendarSettings.clientId", "clientId")
        )
            .andExpect(flash().attribute("success", true));

        perform(
            post("/web/settings/calendar-sync")
                .param("absenceTypeSettings.items[0].id", "10")
                .param("specialLeaveSettings.specialLeaveSettingsItems[0].id", "11")
                .param("calendarSettings.clientId", "clientId")
        )
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/settings/calendar-sync"));
    }

    private static CalendarSettings someSettingsWithoutGoogleCalendarRefreshToken() {
        return new CalendarSettings();
    }

    private static CalendarSettings someSettingsWithGoogleCalendarRefreshToken() {
        final CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.getGoogleCalendarSettings().setRefreshToken(SOME_GOOGLE_REFRESH_TOKEN);
        return calendarSettings;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }

    private static class SomeCalendarProvider implements CalendarProvider {

        @Override
        public Optional<String> add(CalendarAbsence absence, CalendarSettings calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalendarProvider impl.");
        }

        @Override
        public void update(CalendarAbsence absence, String eventId, CalendarSettings calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalendarProvider impl.");
        }

        @Override
        public Optional<String> delete(String eventId, CalendarSettings calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalendarProvider impl.");
        }

        @Override
        public void checkCalendarSyncSettings(CalendarSettings calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalendarProvider impl.");
        }
    }

    private static class AnotherCalendarProvider implements CalendarProvider {

        @Override
        public Optional<String> add(CalendarAbsence absence, CalendarSettings calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalendarProvider impl.");
        }

        @Override
        public void update(CalendarAbsence absence, String eventId, CalendarSettings calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalendarProvider impl.");
        }

        @Override
        public Optional<String> delete(String eventId, CalendarSettings calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalendarProvider impl.");
        }

        @Override
        public void checkCalendarSyncSettings(CalendarSettings calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalendarProvider impl.");
        }
    }
}
