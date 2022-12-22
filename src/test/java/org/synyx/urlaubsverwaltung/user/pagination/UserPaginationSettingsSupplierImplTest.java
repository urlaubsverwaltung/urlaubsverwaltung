package org.synyx.urlaubsverwaltung.user.pagination;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.PersonId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPaginationSettingsSupplierImplTest {

    @Mock
    private UserPaginationSettingsService userPaginationSettingsService;

    @Test
    void ensureGetUserPaginationSettings() {

        final UserPaginationSettingsSupplierImpl sut = new UserPaginationSettingsSupplierImpl(userPaginationSettingsService);

        final UserPaginationSettings expectedUserPaginationSettings = new UserPaginationSettings(42);
        when(userPaginationSettingsService.getUserPaginationSettings(new PersonId(1L))).thenReturn(expectedUserPaginationSettings);

        final UserPaginationSettings actual = sut.getUserPaginationSettings(new PersonId(1L));

        assertThat(actual).isSameAs(expectedUserPaginationSettings);
    }
}
