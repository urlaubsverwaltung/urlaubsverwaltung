package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.user.Theme.LIGHT;

@ExtendWith(MockitoExtension.class)
class UserSettingsServiceTest {

    private UserSettingsServiceImpl sut;

    @Mock
    private UserSettingsRepository userSettingsRepository;
    @Mock
    private LocaleResolver localeResolver;
    @Mock
    private PersonService personService;

    @BeforeEach
    void setUp() {
        sut = new UserSettingsServiceImpl(userSettingsRepository, localeResolver, personService);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void ensureUserSettingsForPerson() {

        final Person person = new Person();
        person.setId(42L);

        final UserSettingsEntity entity = new UserSettingsEntity();
        entity.setPersonId(42L);
        entity.setTheme(Theme.DARK);
        entity.setLocale(GERMAN);

        when(userSettingsRepository.findById(42L)).thenReturn(Optional.of(entity));

        final UserSettings actual = sut.getUserSettingsForPerson(person);

        assertThat(actual.theme()).isEqualTo(Theme.DARK);
        assertThat(actual.locale()).hasValue(GERMAN);
    }

    @Test
    void ensureUserSettingsForPersonReturnsDefault() {
        final Person person = new Person();
        person.setId(42L);

        when(userSettingsRepository.findById(42L)).thenReturn(Optional.empty());

        final UserSettings actual = sut.getUserSettingsForPerson(person);

        assertThat(actual.theme()).isEqualTo(Theme.SYSTEM);
        assertThat(actual.locale()).isEmpty();
    }

    @Test
    void ensureUpdateUserPreference() {

        final Person person = new Person();
        person.setId(42L);

        final UserSettingsEntity entity = new UserSettingsEntity();
        entity.setPersonId(42L);
        entity.setTheme(Theme.DARK);
        entity.setLocale(null);
        entity.setLocaleBrowserSpecific(ENGLISH);

        when(userSettingsRepository.findById(42L)).thenReturn(Optional.of(entity));

        final UserSettingsEntity entityToSave = new UserSettingsEntity();
        entityToSave.setPersonId(42L);
        entityToSave.setTheme(LIGHT);
        entityToSave.setLocale(GERMAN);

        when(userSettingsRepository.save(entityToSave)).thenReturn(entityToSave);

        final UserSettings updatedUserSettings = sut.updateUserPreference(person, LIGHT, GERMAN);
        assertThat(updatedUserSettings.theme()).isEqualTo(LIGHT);
        assertThat(updatedUserSettings.locale()).hasValue(GERMAN);
        assertThat(updatedUserSettings.localeBrowserSpecific()).isEmpty();

        final ArgumentCaptor<UserSettingsEntity> entityArgumentCaptor = ArgumentCaptor.forClass(UserSettingsEntity.class);
        verify(userSettingsRepository).save(entityArgumentCaptor.capture());
        assertThat(entityArgumentCaptor.getValue())
            .satisfies(userSettingsEntity -> {
                assertThat(userSettingsEntity.getTheme()).isEqualTo(LIGHT);
                assertThat(userSettingsEntity.getLocale()).isEqualTo(GERMAN);
                assertThat(userSettingsEntity.getLocaleBrowserSpecific()).isNull();
            });
    }

    @Test
    void ensureUpdateUserPreferenceUsesBrowserSpecificLocaleLocaleWillBeNull() {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(GERMAN);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        final Person person = new Person();
        person.setId(42L);

        final UserSettingsEntity entity = new UserSettingsEntity();
        entity.setPersonId(42L);
        entity.setTheme(Theme.DARK);
        entity.setLocale(ENGLISH);
        entity.setLocaleBrowserSpecific(null);

        when(userSettingsRepository.findById(42L)).thenReturn(Optional.of(entity));

        final UserSettingsEntity entityToSave = new UserSettingsEntity();
        entityToSave.setPersonId(42L);
        entityToSave.setTheme(LIGHT);
        entityToSave.setLocale(null);
        entityToSave.setLocaleBrowserSpecific(GERMAN);

        when(userSettingsRepository.save(entityToSave)).thenReturn(entityToSave);

        final UserSettings updatedUserSettings = sut.updateUserPreference(person, LIGHT, null);
        assertThat(updatedUserSettings.theme()).isEqualTo(LIGHT);
        assertThat(updatedUserSettings.locale()).isEmpty();
        assertThat(updatedUserSettings.localeBrowserSpecific()).hasValue(GERMAN);

        final ArgumentCaptor<UserSettingsEntity> entityArgumentCaptor = ArgumentCaptor.forClass(UserSettingsEntity.class);
        verify(userSettingsRepository).save(entityArgumentCaptor.capture());
        assertThat(entityArgumentCaptor.getValue())
            .satisfies(userSettingsEntity -> {
                assertThat(userSettingsEntity.getTheme()).isEqualTo(LIGHT);
                assertThat(userSettingsEntity.getLocale()).isNull();
                assertThat(userSettingsEntity.getLocaleBrowserSpecific()).isEqualTo(GERMAN);
            });
    }

    @Test
    void ensureUpdateUserPreferenceWhenNothingHasBeenPersistedYet() {

        final Person person = new Person();
        person.setId(42L);

        when(userSettingsRepository.findById(42L)).thenReturn(Optional.empty());

        final UserSettingsEntity entityToSave = new UserSettingsEntity();
        entityToSave.setPersonId(42L);
        entityToSave.setTheme(LIGHT);
        entityToSave.setLocale(GERMAN);

        when(userSettingsRepository.save(entityToSave)).thenReturn(entityToSave);

        final UserSettings updatedUserSettings = sut.updateUserPreference(person, LIGHT, GERMAN);
        assertThat(updatedUserSettings.theme()).isEqualTo(LIGHT);

        final ArgumentCaptor<UserSettingsEntity> entityArgumentCaptor = ArgumentCaptor.forClass(UserSettingsEntity.class);
        verify(userSettingsRepository).save(entityArgumentCaptor.capture());
        assertThat(entityArgumentCaptor.getValue())
            .satisfies(userSettingsEntity -> {
                assertThat(userSettingsEntity.getTheme()).isEqualTo(LIGHT);
                assertThat(userSettingsEntity.getLocale()).isEqualTo(GERMAN);
                assertThat(userSettingsEntity.getLocaleBrowserSpecific()).isNull();
            });
    }

    @Test
    void ensureFindLocaleForUsernameReturnsEmptyOptionalWhenUsernameIsUnknown() {

        final Person person = new Person();
        person.setId(1L);

        when(userSettingsRepository.findByPerson(person)).thenReturn(Optional.empty());

        final Optional<Locale> actual = sut.getLocale(person);
        assertThat(actual).isEmpty();
    }

    @Test
    void ensureFindLocaleForUsernameReturnsEmptyOptionalWhenThereIsNoLocale() {

        final Person person = new Person();
        person.setId(1L);

        final UserSettingsEntity entity = new UserSettingsEntity();
        entity.setLocale(null);

        when(userSettingsRepository.findByPerson(person)).thenReturn(Optional.of(entity));

        final Optional<Locale> actual = sut.getLocale(person);
        assertThat(actual).isEmpty();
    }

    @Test
    void ensureFindLocaleForUsernameReturnsLocale() {

        final Person person = new Person();
        person.setId(1L);

        final UserSettingsEntity entity = new UserSettingsEntity();
        entity.setLocale(GERMAN);

        when(userSettingsRepository.findByPerson(person)).thenReturn(Optional.of(entity));

        final Optional<Locale> actual = sut.getLocale(person);
        assertThat(actual).hasValue(GERMAN);
    }

    @Test
    void ensureFindThemeForUsernameReturnsEmptyOptionalWhenUsernameIsUnknown() {

        when(userSettingsRepository.findByPersonUsername("batman")).thenReturn(Optional.empty());

        final Optional<Theme> actual = sut.findThemeForUsername("batman");
        assertThat(actual).isEmpty();
    }

    @Test
    void ensureFindThemeForUsernameReturnsEmptyOptionalWhenThereIsNoLocale() {

        final UserSettingsEntity entity = new UserSettingsEntity();
        entity.setTheme(null);

        when(userSettingsRepository.findByPersonUsername("batman")).thenReturn(Optional.of(entity));

        final Optional<Theme> actual = sut.findThemeForUsername("batman");
        assertThat(actual).isEmpty();
    }

    @Test
    void ensureFindThemeForUsernameReturnsLocale() {

        final UserSettingsEntity entity = new UserSettingsEntity();
        entity.setTheme(Theme.DARK);

        when(userSettingsRepository.findByPersonUsername("batman")).thenReturn(Optional.of(entity));

        final Optional<Theme> actual = sut.findThemeForUsername("batman");
        assertThat(actual).hasValue(Theme.DARK);
    }

    @Test
    void ensureDeletionOnPersonDeletionEvent() {
        final Person person = new Person();

        sut.delete(new PersonDeletedEvent(person));

        verify(userSettingsRepository).deleteByPerson(person);
    }

    static Stream<Arguments> authentications() {
        return Stream.of(
            Arguments.of(new TestingAuthenticationToken(new DefaultOidcUser(List.of(), new OidcIdToken("tokenValue", Instant.parse("2020-12-01T00:00:00.00Z"), Instant.parse("2020-12-02T00:00:00.00Z"), Map.of("sub", "username"))), null), GERMAN),
            Arguments.of(new TestingAuthenticationToken(new DefaultOidcUser(List.of(), new OidcIdToken("tokenValue", Instant.parse("2020-12-01T00:00:00.00Z"), Instant.parse("2020-12-02T00:00:00.00Z"), Map.of("sub", "username"))), null), ENGLISH)
        );
    }

    @ParameterizedTest
    @MethodSource("authentications")
    void ensureAuthenticationSuccessSetsLocaleForAuthentication(Authentication authentication, Locale locale) {

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("username");
        when(personService.getPersonByUsername("username")).thenReturn(Optional.of(person));

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(locale);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        final UserSettingsEntity userSettingsEntity = new UserSettingsEntity();
        userSettingsEntity.setLocale(locale);

        when(userSettingsRepository.findByPerson(person)).thenReturn(Optional.of(userSettingsEntity));

        final AuthenticationSuccessEvent authenticationSuccessEvent = new AuthenticationSuccessEvent(authentication);
        sut.handleAuthenticationSuccess(authenticationSuccessEvent);

        verify(localeResolver).setLocale(request, null, locale);
    }
}
