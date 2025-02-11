package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;

import java.util.Locale;
import java.util.Optional;

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

    @BeforeEach
    void setUp() {
        sut = new UserSettingsServiceImpl(userSettingsRepository, localeResolver);
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

}
