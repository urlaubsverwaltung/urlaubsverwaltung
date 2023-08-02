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
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Locale;
import java.util.Set;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    private UserSettingsServiceImpl userSettingsService;
    @Mock
    private SupportedLocaleService supportedLocaleService;
    @Mock
    private MessageSource messageSource;
    @Mock
    private UserSettingsDtoValidator userSettingsDtoValidator;

    @BeforeEach
    void setUp() {
        sut = new UserSettingsViewController(personService, userSettingsService, supportedLocaleService, messageSource, userSettingsDtoValidator);
    }

    @Test
    void ensureGetUserSettings() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(42L);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final UserSettings userSettings = new UserSettings(Theme.DARK, Locale.GERMAN, null);
        when(userSettingsService.getUserSettingsForPerson(signedInPerson)).thenReturn(userSettings);

        when(supportedLocaleService.getSupportedLocales()).thenReturn(Set.of(Locale.GERMAN, Locale.ENGLISH));

        when(messageSource.getMessage("user-settings.theme.DARK", new Object[]{}, Locale.GERMAN)).thenReturn("dark-label");
        when(messageSource.getMessage("user-settings.theme.LIGHT", new Object[]{}, Locale.GERMAN)).thenReturn("light-label");
        when(messageSource.getMessage("user-settings.theme.SYSTEM", new Object[]{}, Locale.GERMAN)).thenReturn("system-label");
        when(messageSource.getMessage("locale", new Object[]{}, Locale.GERMAN)).thenReturn("Deutsch");
        when(messageSource.getMessage("locale", new Object[]{}, Locale.ENGLISH)).thenReturn("English");

        perform(get("/web/person/42/settings").locale(Locale.GERMAN))
            .andExpect(status().isOk())
            .andExpect(model().attribute("supportedLocales",
                hasItems(
                    allOf(
                        hasProperty("locale", is(Locale.GERMAN)),
                        hasProperty("displayName", is("Deutsch"))
                    ),
                    allOf(
                        hasProperty("locale", is(Locale.ENGLISH)),
                        hasProperty("displayName", is("English"))
                    )
                )
            ))
            .andExpect(model().attribute("userSettings",
                hasProperty("theme", is("DARK"))
            ))
            .andExpect(model().attribute("supportedThemes", contains(
                allOf(hasProperty("value", is("SYSTEM")), hasProperty("label", is("system-label"))),
                allOf(hasProperty("value", is("LIGHT")), hasProperty("label", is("light-label"))),
                allOf(hasProperty("value", is("DARK")), hasProperty("label", is("dark-label")))
            )));
    }

    @Test
    void ensureGetUserSettingsThrowsWhenCalledForOtherPerson() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        perform(get("/web/person/42/settings"))
            .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @EnumSource(value = Theme.class)
    void ensureUpdateUserSettings(Theme givenTheme) throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(42L);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        perform(post("/web/person/42/settings")
            .param("theme", givenTheme.name())
            .locale(Locale.GERMANY)
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/person/42/settings"));
    }

    @Test
    void ensureUpdateUserSettingsThrowsWhenThemeNameIsUnknown() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(42L);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        perform(post("/web/person/42/settings")
            .param("theme", "UNKNOWN_THEME")
            .locale(Locale.GERMANY)
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureUpdateUserSettingsThrowsWhenCalledForOtherPerson() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        perform(post("/web/person/42/settings")
            .param("theme", "DARK")
            .locale(Locale.GERMANY)
        )
            .andExpect(status().isNotFound());
    }

    @Test
    void ensureUpdateUserSettingsWithErrorOpensThePageAgainAndDoesNotSave() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(42L);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        doAnswer(invocation -> {
            final Errors errors = invocation.getArgument(1);
            errors.reject("errors");
            return null;
        }).when(userSettingsDtoValidator).validate(any(), any());

        when(supportedLocaleService.getSupportedLocales()).thenReturn(Set.of(Locale.GERMAN));

        perform(post("/web/person/42/settings")
            .param("theme", "someTheme")
            .locale(Locale.ITALIAN)
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("supportedThemes", contains(
                hasProperty("value", is("SYSTEM")),
                hasProperty("value", is("LIGHT")),
                hasProperty("value", is("DARK"))
            )))
            .andExpect(model().attribute("supportedLocales", hasItem(hasProperty("locale", is(Locale.GERMAN)))));

        verify(userSettingsService, never()).updateUserPreference(any(), any(), any());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
