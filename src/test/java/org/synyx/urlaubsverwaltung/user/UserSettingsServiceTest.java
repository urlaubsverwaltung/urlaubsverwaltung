package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSettingsServiceTest {

    private UserSettingsService sut;

    @Mock
    private UserSettingsRepository userSettingsRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Captor
    private ArgumentCaptor<UserLocaleChangedEvent> userLocaleChangedEventCaptor;


    @BeforeEach
    void setUp() {
        sut = new UserSettingsService(userSettingsRepository, applicationEventPublisher);
    }

    @Test
    void ensureUserSettingsForPerson() {

        final Person person = new Person();
        person.setId(42L);

        final UserSettingsEntity entity = new UserSettingsEntity();
        entity.setPersonId(42L);
        entity.setTheme(Theme.DARK);
        entity.setLocale(Locale.GERMAN);

        when(userSettingsRepository.findById(42L)).thenReturn(Optional.of(entity));

        final UserSettings actual = sut.getUserSettingsForPerson(person);

        assertThat(actual.theme()).isEqualTo(Theme.DARK);
        assertThat(actual.locale()).hasValue(Locale.GERMAN);
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
    void ensureUpdateUserThemePreference() {
        final Person person = new Person();
        person.setId(42L);

        final UserSettingsEntity entity = new UserSettingsEntity();
        entity.setPersonId(42L);
        entity.setPerson(person);
        entity.setTheme(Theme.DARK);

        when(userSettingsRepository.findById(42L)).thenReturn(Optional.of(entity));

        final UserSettingsEntity entityToSave = new UserSettingsEntity();
        entityToSave.setPersonId(42L);
        entityToSave.setPerson(null);
        entityToSave.setTheme(Theme.LIGHT);
        entityToSave.setLocale(Locale.GERMAN);

        when(userSettingsRepository.save(entityToSave)).thenReturn(entityToSave);

        final UserSettings updatedUserSettings = sut.updateUserThemePreference(person, Theme.LIGHT, Locale.GERMAN);
        assertThat(updatedUserSettings.theme()).isEqualTo(Theme.LIGHT);

        verify(applicationEventPublisher).publishEvent(userLocaleChangedEventCaptor.capture());
        final UserLocaleChangedEvent userLocaleChangedEvent = userLocaleChangedEventCaptor.getValue();
        assertThat(userLocaleChangedEvent.getLocale()).isEqualTo(Locale.GERMAN);
    }

    @Test
    void ensureUpdateUserThemePreferenceWhenNothingHasBeenPersistedYet() {
        final Person person = new Person();
        person.setId(42L);

        when(userSettingsRepository.findById(42L)).thenReturn(Optional.empty());

        final UserSettingsEntity entityToSave = new UserSettingsEntity();
        entityToSave.setPersonId(42L);
        entityToSave.setPerson(null);
        entityToSave.setTheme(Theme.LIGHT);
        entityToSave.setLocale(Locale.GERMAN);

        when(userSettingsRepository.save(entityToSave)).thenReturn(entityToSave);

        final UserSettings updatedUserSettings = sut.updateUserThemePreference(person, Theme.LIGHT, Locale.GERMAN);
        assertThat(updatedUserSettings.theme()).isEqualTo(Theme.LIGHT);

        verify(applicationEventPublisher).publishEvent(userLocaleChangedEventCaptor.capture());
        final UserLocaleChangedEvent userLocaleChangedEvent = userLocaleChangedEventCaptor.getValue();
        assertThat(userLocaleChangedEvent.getLocale()).isEqualTo(Locale.GERMAN);
    }

    @Test
    void ensureFindLocaleForUsernameReturnsEmptyOptionalWhenUsernameIsUnknown() {

        when(userSettingsRepository.findByPersonUsername("batman")).thenReturn(Optional.empty());

        final Optional<Locale> actual = sut.findLocaleForUsername("batman");
        assertThat(actual).isEmpty();
    }

    @Test
    void ensureFindLocaleForUsernameReturnsEmptyOptionalWhenThereIsNoLocale() {

        final UserSettingsEntity entity = new UserSettingsEntity();
        entity.setLocale(null);

        when(userSettingsRepository.findByPersonUsername("batman")).thenReturn(Optional.of(entity));

        final Optional<Locale> actual = sut.findLocaleForUsername("batman");
        assertThat(actual).isEmpty();
    }

    @Test
    void ensureFindLocaleForUsernameReturnsLocale() {

        final UserSettingsEntity entity = new UserSettingsEntity();
        entity.setLocale(Locale.GERMAN);

        when(userSettingsRepository.findByPersonUsername("batman")).thenReturn(Optional.of(entity));

        final Optional<Locale> actual = sut.findLocaleForUsername("batman");
        assertThat(actual).hasValue(Locale.GERMAN);
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
