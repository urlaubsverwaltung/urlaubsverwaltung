package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Locale;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class UserSettingsViewControllerTest {

    private UserSettingsViewController sut;

    @Mock
    private PersonService personService;

    @Mock
    private UserSettingsService userSettingsService;

    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        sut = new UserSettingsViewController(personService, userSettingsService, messageSource);
    }

    @Test
    void ensureGetUserSettings() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(42);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final UserSettings userSettings = new UserSettings(Theme.DARK);
        when(userSettingsService.getUserSettingsForPerson(signedInPerson)).thenReturn(userSettings);

        when(messageSource.getMessage("user-settings.theme.DARK", new Object[]{}, Locale.GERMANY)).thenReturn("dark-label");
        when(messageSource.getMessage("user-settings.theme.LIGHT", new Object[]{}, Locale.GERMANY)).thenReturn("light-label");
        when(messageSource.getMessage("user-settings.theme.SYSTEM", new Object[]{}, Locale.GERMANY)).thenReturn("system-label");

        perform(get("/web/person/42/settings").locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(model().attribute("userSettings",
                allOf(
                    hasProperty("selectedTheme", is("DARK")),
                    hasProperty("themes", contains(
                        allOf(hasProperty("value", is("SYSTEM")), hasProperty("label", is("system-label"))),
                        allOf(hasProperty("value", is("LIGHT")), hasProperty("label", is("light-label"))),
                        allOf(hasProperty("value", is("DARK")), hasProperty("label", is("dark-label")))
                    ))
                ))
            );
    }

    @Test
    void ensureGetUserSettingsThrowsWhenCalledForOtherPerson() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        perform(get("/web/person/42/settings"))
            .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @EnumSource(value = Theme.class)
    void ensureUpdateUserSettings(Theme givenTheme) throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(42);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final ResultActions perform = perform(post("/web/person/42/settings")
            .param("selectedTheme", givenTheme.name())
            .locale(Locale.GERMANY));

        perform
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/person/42/settings"));
    }

    @Test
    void ensureUpdateUserSettingsThrowsWhenThemeNameIsUnknown() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(42);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final ResultActions perform = perform(post("/web/person/42/settings")
            .param("selectedTheme", "UNKNOWN_THEME")
            .locale(Locale.GERMANY));

        perform
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureUpdateUserSettingsThrowsWhenCalledForOtherPerson() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final ResultActions perform = perform(post("/web/person/42/settings")
            .param("selectedTheme", "DARK")
            .locale(Locale.GERMANY));

        perform
            .andExpect(status().isNotFound());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
