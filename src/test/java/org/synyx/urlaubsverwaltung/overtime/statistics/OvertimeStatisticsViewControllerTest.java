package org.synyx.urlaubsverwaltung.overtime.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.Instant;

import static java.time.ZoneOffset.UTC;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class OvertimeStatisticsViewControllerTest {

    private OvertimeStatisticsViewController sut;

    @Mock
    private SettingsService settingsService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-31T10:15:00Z"), UTC);

    @BeforeEach
    void setUp() {
        sut = new OvertimeStatisticsViewController(settingsService, clock);
    }

    @Test
    void ensureStatisticsForCurrentYearWhenNoYearIsRequested() throws Exception {

        overtimeFeature(true);

        perform(get("/web/overtime/statistics"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedYear", 2026))
            .andExpect(model().attribute("currentYear", 2026))
            .andExpect(view().name("overtime/overtime_statistics"));
    }

    @Test
    void ensureStatisticsForRequestedYear() throws Exception {

        overtimeFeature(true);

        perform(get("/web/overtime/statistics").param("year", "2024"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedYear", 2024))
            .andExpect(model().attribute("currentYear", 2026))
            .andExpect(view().name("overtime/overtime_statistics"));
    }

    @Test
    void ensureUnparsableYearFallsBackToCurrentYear() throws Exception {

        overtimeFeature(true);

        perform(get("/web/overtime/statistics").param("year", "not-a-year"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedYear", 2026));
    }

    @Test
    void ensureNotFoundWhenOvertimeFeatureIsDeactivated() throws Exception {

        overtimeFeature(false);

        perform(get("/web/overtime/statistics"))
            .andExpect(status().isNotFound());
    }

    private void overtimeFeature(boolean active) {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(active);
        when(settingsService.getSettings()).thenReturn(settings);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
