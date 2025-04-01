package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class SettingsOvertimeViewControllerTest {

    private SettingsOvertimeViewController sut;

    @Mock
    private SettingsService settingsService;
    @Mock
    private SettingsOvertimeValidator settingsValidator;

    @BeforeEach
    void setUp() {
        sut = new SettingsOvertimeViewController(settingsService, settingsValidator);
    }

    @Test
    void ensureGetSettings() throws Exception {

        final OvertimeSettings overtimeSettings = new OvertimeSettings();

        final Settings settings = new Settings();
        settings.setOvertimeSettings(overtimeSettings);

        when(settingsService.getSettings()).thenReturn(settings);

        perform(get("/web/settings/overtime"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("settings", allOf(
                hasProperty("overtimeSettings", sameInstance(overtimeSettings))
            )));
    }

    @Test
    void ensureSaveSettings() throws Exception {

        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(post("/web/settings/overtime")
            .param("id", "42")
            .param("overtimeSettings.overtimeActive", "true")
            .param("overtimeSettings.overtimeReductionWithoutApplicationActive", "true")
            .param("overtimeSettings.maximumOvertime", "1")
            .param("overtimeSettings.minimumOvertime", "2")
            .param("overtimeSettings.minimumOvertimeReduction", "3")
            .param("overtimeSettings.overtimeWritePrivilegedOnly", "true")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/settings/overtime"))
            .andExpect(flash().attribute("success", true));


        final ArgumentCaptor<Settings> captor = ArgumentCaptor.forClass(Settings.class);
        verify(settingsService).save(captor.capture());

        final Settings actualSettings = captor.getValue();
        assertThat(actualSettings.getOvertimeSettings()).satisfies(persistedOvertimeSettings -> {
            assertThat(persistedOvertimeSettings.isOvertimeActive()).isTrue();
            assertThat(persistedOvertimeSettings.isOvertimeReductionWithoutApplicationActive()).isTrue();
            assertThat(persistedOvertimeSettings.getMaximumOvertime()).isEqualTo(1);
            assertThat(persistedOvertimeSettings.getMinimumOvertime()).isEqualTo(2);
            assertThat(persistedOvertimeSettings.getMinimumOvertimeReduction()).isEqualTo(3);
            assertThat(persistedOvertimeSettings.isOvertimeWritePrivilegedOnly()).isTrue();
        });
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
