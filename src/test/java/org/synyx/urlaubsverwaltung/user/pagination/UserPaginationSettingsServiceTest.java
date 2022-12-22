package org.synyx.urlaubsverwaltung.user.pagination;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPaginationSettingsServiceTest {

    private UserPaginationSettingsService sut;

    @Mock
    private UserPaginationSettingsRepository repository;

    private static final int DEFAULT_PAGE_SIZE = 42;

    @BeforeEach
    void setUp() {
        final SpringDataWebProperties dataWebProperties = new SpringDataWebProperties();
        dataWebProperties.getPageable().setDefaultPageSize(DEFAULT_PAGE_SIZE);

        sut = new UserPaginationSettingsService(repository, dataWebProperties);
    }

    @Test
    void ensureGetUserPaginationSettingsWithDefaults() {

        when(repository.findByPersonId(42L)).thenReturn(Optional.empty());

        final UserPaginationSettings actual = sut.getUserPaginationSettings(new PersonId(42L));

        assertThat(actual.getDefaultPageSize()).isEqualTo(DEFAULT_PAGE_SIZE);
    }

    @Test
    void ensureGetUserPaginationSettings() {

        final UserPaginationSettingsEntity entity = new UserPaginationSettingsEntity();
        entity.setPerson(null);
        entity.setPersonId(42L);
        entity.setDefaultPageSize(100);

        when(repository.findByPersonId(42L)).thenReturn(Optional.of(entity));

        final UserPaginationSettings actual = sut.getUserPaginationSettings(new PersonId(42L));

        assertThat(actual.getDefaultPageSize()).isEqualTo(100);
    }

    @Test
    void ensureUpdateUserPaginationSettings() {

        sut.updatePageableDefaultSize(new PersonId(42L), 12);

        final ArgumentCaptor<UserPaginationSettingsEntity> captor = ArgumentCaptor.forClass(UserPaginationSettingsEntity.class);

        verify(repository).save(captor.capture());

        assertThat(captor.getValue()).satisfies(actualEntity -> {
            assertThat(actualEntity.getPerson()).isNull();
            assertThat(actualEntity.getPersonId()).isEqualTo(42L);
            assertThat(actualEntity.getDefaultPageSize()).isEqualTo(12);
        });
    }
}
