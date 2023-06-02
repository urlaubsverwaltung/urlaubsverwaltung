package org.synyx.urlaubsverwaltung.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.PersonId;

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
        entity.setPersonId(1);
        entity.setRestrictToDepartments(true);

        when(repository.findById(1)).thenReturn(Optional.of(entity));

        final UserNotificationSettings actual = sut.findNotificationSettings(new PersonId(1));
        assertThat(actual.getPersonId()).isEqualTo(new PersonId(1));
        assertThat(actual.isRestrictToDepartments()).isTrue();
    }

    @Test
    void ensureFindNotificationSettingsReturnsDefault() {

        when(repository.findById(1)).thenReturn(Optional.empty());

        final UserNotificationSettings actual = sut.findNotificationSettings(new PersonId(1));
        assertThat(actual.getPersonId()).isEqualTo(new PersonId(1));
        assertThat(actual.isRestrictToDepartments()).isFalse();
    }

    @Test
    void ensureUpdateNotificationSettings() {

        when(repository.save(any(UserNotificationSettingsEntity.class))).thenAnswer(returnsFirstArg());

        final UserNotificationSettings actual = sut.updateNotificationSettings(new PersonId(1), true);

        assertThat(actual.getPersonId()).isEqualTo(new PersonId(1));
        assertThat(actual.isRestrictToDepartments()).isTrue();

        final ArgumentCaptor<UserNotificationSettingsEntity> captor = ArgumentCaptor.forClass(UserNotificationSettingsEntity.class);
        verify(repository).save(captor.capture());

        final UserNotificationSettingsEntity actualPersisted = captor.getValue();
        assertThat(actualPersisted.getPersonId()).isEqualTo(1);
        assertThat(actualPersisted.isRestrictToDepartments()).isTrue();
    }
}
