package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.person.settings.AvatarSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
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
class SettingsAvatarViewControllerTest {

    private SettingsAvatarViewController sut;

    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new SettingsAvatarViewController(settingsService);
    }

    @Test
    void ensureGetSettings() throws Exception {

        final AvatarSettings avatarSettings = new AvatarSettings();
        avatarSettings.setGravatarEnabled(true);

        final Settings settings = new Settings();
        settings.setAvatarSettings(avatarSettings);

        when(settingsService.getSettings()).thenReturn(settings);

        perform(get("/web/settings/avatar"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("settings", allOf(
                hasProperty("avatarSettings", is(avatarSettings))
            )));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ensureSaveSettings(boolean givenEnabled) throws Exception {

        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(
            post("/web/settings/avatar")
                .param("avatarSettings.gravatarEnabled", String.valueOf(givenEnabled))
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/settings/avatar"))
            .andExpect(flash().attribute("success", true));

        final ArgumentCaptor<Settings> settingsArgumentCaptor = ArgumentCaptor.forClass(Settings.class);
        verify(settingsService).save(settingsArgumentCaptor.capture());

        final Settings savedSettings = settingsArgumentCaptor.getValue();
        final AvatarSettings avatarSettings = savedSettings.getAvatarSettings();
        assertThat(avatarSettings.isGravatarEnabled()).isEqualTo(givenEnabled);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
