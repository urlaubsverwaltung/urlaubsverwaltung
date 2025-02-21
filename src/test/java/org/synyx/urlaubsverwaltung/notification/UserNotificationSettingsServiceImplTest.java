package org.synyx.urlaubsverwaltung.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNotificationSettingsServiceImplTest {

    private UserNotificationSettingsServiceImpl sut;

    @Mock
    private UserNotificationSettingsRepository repository;

    @BeforeEach
    void setUp() {
        sut = new UserNotificationSettingsServiceImpl(repository);
    }

    @Test
    void ensureFindNotificationSettings() {

        final UserNotificationSettingsEntity entity = new UserNotificationSettingsEntity();
        entity.setPersonId(1L);
        entity.setRestrictToDepartments(true);

        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        final UserNotificationSettings actual = sut.findNotificationSettings(new PersonId(1L));
        assertThat(actual.personId()).isEqualTo(new PersonId(1L));
        assertThat(actual.restrictToDepartments()).isTrue();
    }

    @Test
    void ensureFindNotificationSettingsReturnsDefault() {

        when(repository.findById(1L)).thenReturn(Optional.empty());

        final UserNotificationSettings actual = sut.findNotificationSettings(new PersonId(1L));
        assertThat(actual.personId()).isEqualTo(new PersonId(1L));
        assertThat(actual.restrictToDepartments()).isFalse();
    }

    @Test
    void ensureUpdateNotificationSettings() {

        when(repository.save(any(UserNotificationSettingsEntity.class))).thenAnswer(returnsFirstArg());

        final UserNotificationSettings actual = sut.updateNotificationSettings(new PersonId(1L), true);

        assertThat(actual.personId()).isEqualTo(new PersonId(1L));
        assertThat(actual.restrictToDepartments()).isTrue();

        final ArgumentCaptor<UserNotificationSettingsEntity> captor = ArgumentCaptor.forClass(UserNotificationSettingsEntity.class);
        verify(repository).save(captor.capture());

        final UserNotificationSettingsEntity actualPersisted = captor.getValue();
        assertThat(actualPersisted.getPersonId()).isEqualTo(1);
        assertThat(actualPersisted.isRestrictToDepartments()).isTrue();
    }

    @Test
    void ensureFindNotificationSettingsWithMultipleSameIdsReturnsCorrectValue() {

        final UserNotificationSettingsEntity entity = new UserNotificationSettingsEntity();
        entity.setPersonId(1L);
        entity.setRestrictToDepartments(true);

        when(repository.findAllById(List.of(1L, 1L))).thenReturn(List.of(entity, entity));

        final Map<PersonId, UserNotificationSettings> notificationSettings = sut.findNotificationSettings(List.of(new PersonId(1L), new PersonId(1L)));
        assertThat(notificationSettings.get(new PersonId(1L)).personId()).isEqualTo(new PersonId(1L));
        assertThat(notificationSettings.get(new PersonId(1L)).restrictToDepartments()).isTrue();
    }
}
