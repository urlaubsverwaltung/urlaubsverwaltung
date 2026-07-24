package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.settings.AvatarSettings;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class ScratchAvatarRenderIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private SettingsService settingsService;

    @Test
    void dumpRenderedHtml() throws Exception {

        final Settings settings = new Settings();
        settings.setId(1L);
        final AvatarSettings avatarSettings = new AvatarSettings();
        avatarSettings.setGravatarEnabled(true);
        settings.setAvatarSettings(avatarSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        final MvcResult result = webAppContextSetup(context).apply(springSecurity()).build()
            .perform(get("/web/settings/avatar")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE"))))
            .andReturn();

        final String html = result.getResponse().getContentAsString();
        Files.writeString(Path.of("/tmp/claude-1001/-home-schneider-projects-urlaubsverwaltung-urlaubsverwaltung/ce02bd77-1ddd-42c5-8d66-23c07141270c/scratchpad/rendered-avatar-settings.html"), html);
    }
}
