package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.settings.AvatarSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class UserSettingsViewControllerIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private UserSettingsService userSettingsService;
    @MockitoBean
    private SettingsService settingsService;

    @Test
    void rendersGravatarTilesWhenGloballyEnabled() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("user");
        person.setGravatarEnabled(true);
        when(personService.getSignedInUser()).thenReturn(person);
        when(userSettingsService.getUserSettingsForPerson(person)).thenReturn(UserSettings.DEFAULT);
        when(userSettingsService.getUserSettingsForUsername("user")).thenReturn(UserSettings.DEFAULT);

        globalGravatarEnabled(true);

        perform(get("/web/person/1/settings")
            .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("userSettings-gravatarEnabled-true")))
            .andExpect(content().string(containsString("userSettings-gravatarEnabled-false")));
    }

    @Test
    void hidesGravatarTilesWhenGloballyDisabled() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("user");
        when(personService.getSignedInUser()).thenReturn(person);
        when(userSettingsService.getUserSettingsForPerson(person)).thenReturn(UserSettings.DEFAULT);
        when(userSettingsService.getUserSettingsForUsername("user")).thenReturn(UserSettings.DEFAULT);

        globalGravatarEnabled(false);

        perform(get("/web/person/1/settings")
            .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("userSettings-gravatarEnabled-true"))));
    }

    private void globalGravatarEnabled(boolean enabled) {
        final Settings settings = new Settings();
        final AvatarSettings avatarSettings = new AvatarSettings();
        avatarSettings.setGravatarEnabled(enabled);
        settings.setAvatarSettings(avatarSettings);
        when(settingsService.getSettings()).thenReturn(settings);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
