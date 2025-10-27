package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettings;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsPublicHolidayViewControllerIT extends SingleTenantTestContainersBase {

    @Autowired
    private MockMvc mockMvc;

    // sut dependencies
    @MockitoBean
    private SettingsService settingsService;
    @MockitoBean
    private SettingsPublicHolidayValidator settingsValidator;

    // application stuff not of interest in this test
    @MockitoBean
    private PersonService personService;

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER", "OFFICE"}, mode = EnumSource.Mode.EXCLUDE)
    void ensureGetSettingsNotAllowed(Role role) throws Exception {

        final Person person = mockPerson("someone");
        final OidcLoginRequestPostProcessor oidcLogin = oidcSubject(person, List.of(USER, role));

        mockMvc.perform(get("/web/settings/public-holidays").with(oidcLogin))
            .andExpect(status().isForbidden());
    }

    @Test
    void ensureGetSettingsDetails() throws Exception {

        final Person person = mockPerson("office");
        final OidcLoginRequestPostProcessor oidcLogin = oidcSubject(person, List.of(USER, OFFICE));

        final PublicHolidaysSettings publicHolidaysSettings = new PublicHolidaysSettings();

        final Settings settings = new Settings();
        settings.setId(1L);
        settings.setPublicHolidaysSettings(publicHolidaysSettings);

        when(settingsService.getSettings()).thenReturn(settings);

        final SettingsPublicHolidayDto expectedSettingsDto = new SettingsPublicHolidayDto();
        expectedSettingsDto.setId(1L);
        expectedSettingsDto.setPublicHolidaysSettings(publicHolidaysSettings);

        mockMvc.perform(get("/web/settings/public-holidays").with(oidcLogin))
            .andExpect(status().isOk())
            .andExpect(model().attribute("settings", expectedSettingsDto))
            .andExpect(model().attribute("federalStateTypes", FederalState.federalStatesTypesByCountry()))
            .andExpect(model().attribute("dayLengthTypes", DayLength.values()))
            .andExpect(view().name("settings/public-holidays/settings_public_holidays"));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"USER", "OFFICE"}, mode = EnumSource.Mode.EXCLUDE)
    void ensurePostSettingsNotAllowed(Role role) throws Exception {

        final Person person = mockPerson("someone");
        final OidcLoginRequestPostProcessor oidcLogin = oidcSubject(person, List.of(USER, role));

        mockMvc.perform(post("/web/settings/public-holidays").with(csrf()).with(oidcLogin))
            .andExpect(status().isForbidden());
    }

    @Test
    void ensurePostSettingsIsValidated() throws Exception {

        mockFrameDataProvider();

        final Person person = mockPerson("office");
        final OidcLoginRequestPostProcessor oidcLogin = oidcSubject(person, List.of(USER, OFFICE));

        final PublicHolidaysSettings publicHolidaysSettings = new PublicHolidaysSettings();
        publicHolidaysSettings.setFederalState(GERMANY_BADEN_WUERTTEMBERG);
        publicHolidaysSettings.setWorkingDurationForChristmasEve(DayLength.MORNING);
        publicHolidaysSettings.setWorkingDurationForNewYearsEve(DayLength.MORNING);

        final SettingsPublicHolidayDto settingsDto = new SettingsPublicHolidayDto();
        settingsDto.setId(1L);
        settingsDto.setPublicHolidaysSettings(publicHolidaysSettings);

        doAnswer(args -> {
            final Errors errors = args.getArgument(1);
            errors.reject("something.is.wrong");
            return null;
        }).when(settingsValidator).validate(eq(settingsDto), any(Errors.class));

        mockMvc.perform(post("/web/settings/public-holidays").with(csrf()).with(oidcLogin)
                .param("id", "1")
                .param("publicHolidaysSettings.workingDurationForChristmasEve", "MORNING")
                .param("publicHolidaysSettings.workingDurationForNewYearsEve", "MORNING")
                .param("publicHolidaysSettings.federalState", "GERMANY_BADEN_WUERTTEMBERG")
            )
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("errors"))
            .andExpect(model().attribute("settings", settingsDto))
            .andExpect(model().attribute("federalStateTypes", FederalState.federalStatesTypesByCountry()))
            .andExpect(model().attribute("dayLengthTypes", DayLength.values()))
            .andExpect(view().name("settings/public-holidays/settings_public_holidays"));

        verify(settingsService, times(0)).save(any(Settings.class));
    }

    @Test
    void ensurePostSettingsSucceeds() throws Exception {

        final Person person = mockPerson("office");
        final OidcLoginRequestPostProcessor oidcLogin = oidcSubject(person, List.of(USER, OFFICE));

        final PublicHolidaysSettings publicHolidaysSettings = new PublicHolidaysSettings();
        publicHolidaysSettings.setFederalState(GERMANY_BADEN_WUERTTEMBERG);
        publicHolidaysSettings.setWorkingDurationForChristmasEve(DayLength.MORNING);
        publicHolidaysSettings.setWorkingDurationForNewYearsEve(DayLength.NOON);

        final SettingsPublicHolidayDto settingsDto = new SettingsPublicHolidayDto();
        settingsDto.setId(1L);
        settingsDto.setPublicHolidaysSettings(publicHolidaysSettings);

        final Settings settingsBeforeSave = new Settings();
        when(settingsService.getSettings()).thenReturn(settingsBeforeSave);

        mockMvc.perform(post("/web/settings/public-holidays").with(csrf()).with(oidcLogin)
                .param("id", "1")
                .param("publicHolidaysSettings.workingDurationForChristmasEve", "MORNING")
                .param("publicHolidaysSettings.workingDurationForNewYearsEve", "NOON")
                .param("publicHolidaysSettings.federalState", "GERMANY_BADEN_WUERTTEMBERG")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/settings/public-holidays"))
            .andExpect(flash().attribute("success", true));

        final ArgumentCaptor<Settings> captor = ArgumentCaptor.forClass(Settings.class);
        verify(settingsService).save(captor.capture());

        assertThat(captor.getValue()).satisfies(savedSettings -> {
            final PublicHolidaysSettings actual = savedSettings.getPublicHolidaysSettings();
            assertThat(actual.getFederalState()).isEqualTo(GERMANY_BADEN_WUERTTEMBERG);
            assertThat(actual.getWorkingDurationForChristmasEve()).isEqualTo(DayLength.MORNING);
            assertThat(actual.getWorkingDurationForNewYearsEve()).isEqualTo(DayLength.NOON);
        });
    }

    private Person mockPerson(String username) {

        final Person person = new Person(username, "Muster", "Marlene", "muster@example.org");

        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        return person;
    }

    private void mockFrameDataProvider() {

        final Settings existingSettings = new Settings();
        existingSettings.setOvertimeSettings(new OvertimeSettings());

        when(settingsService.getSettings()).thenReturn(existingSettings);
    }

    private static OidcLoginRequestPostProcessor oidcSubject(Person person, List<Role> roles) {

        final OidcIdToken.Builder tokenBuilder = OidcIdToken.withTokenValue("not-empty-token-value")
            .claim("sub", person.getUsername());

        final List<SimpleGrantedAuthority> authorities = roles.stream().map(Role::name).map(SimpleGrantedAuthority::new).toList();

        final OidcUser oidcUser = new DefaultOidcUser(authorities, tokenBuilder.build());

        return oidcLogin().oidcUser(oidcUser);
    }
}
