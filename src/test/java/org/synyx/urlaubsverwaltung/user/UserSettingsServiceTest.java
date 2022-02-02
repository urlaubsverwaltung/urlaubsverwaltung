package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSettingsServiceTest {

    private UserSettingsService sut;

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @BeforeEach
    void setUp() {
        sut = new UserSettingsService(userSettingsRepository);
    }

    @Test
    void ensureUserSettingsForPerson() {

        final Person person = new Person();
        person.setId(42);

        final UserSettingsEntity entity = new UserSettingsEntity();
        entity.setPersonId(42);
        entity.setTheme(Theme.DARK);

        when(userSettingsRepository.findById(42)).thenReturn(Optional.of(entity));

        final UserSettings actual = sut.getUserSettingsForPerson(person);

        assertThat(actual.theme()).isEqualTo(Theme.DARK);
    }

    @Test
    void ensureUserSettingsForPersonReturnsDefault() {
        final Person person = new Person();
        person.setId(42);

        when(userSettingsRepository.findById(42)).thenReturn(Optional.empty());

        final UserSettings actual = sut.getUserSettingsForPerson(person);

        assertThat(actual.theme()).isEqualTo(Theme.SYSTEM);
    }

    @Test
    void ensureUpdateUserThemePreference() {
        final Person person = new Person();
        person.setId(42);

        final UserSettingsEntity entity = new UserSettingsEntity();
        entity.setPersonId(42);
        entity.setPerson(person);
        entity.setTheme(Theme.DARK);

        when(userSettingsRepository.findById(42)).thenReturn(Optional.of(entity));

        final UserSettingsEntity entityToSave = new UserSettingsEntity();
        entityToSave.setPersonId(42);
        entityToSave.setPerson(null);
        entityToSave.setTheme(Theme.LIGHT);

        when(userSettingsRepository.save(entityToSave)).thenReturn(entityToSave);

        final UserSettings updatedUserSettings = sut.updateUserThemePreference(person, Theme.LIGHT);

        assertThat(updatedUserSettings.theme()).isEqualTo(Theme.LIGHT);
    }

    @Test
    void ensureUpdateUserThemePreferenceWhenNothingHasBeenPersistedYet() {
        final Person person = new Person();
        person.setId(42);

        when(userSettingsRepository.findById(42)).thenReturn(Optional.empty());

        final UserSettingsEntity entityToSave = new UserSettingsEntity();
        entityToSave.setPersonId(42);
        entityToSave.setPerson(null);
        entityToSave.setTheme(Theme.LIGHT);

        when(userSettingsRepository.save(entityToSave)).thenReturn(entityToSave);

        final UserSettings updatedUserSettings = sut.updateUserThemePreference(person, Theme.LIGHT);

        assertThat(updatedUserSettings.theme()).isEqualTo(Theme.LIGHT);
    }
}
