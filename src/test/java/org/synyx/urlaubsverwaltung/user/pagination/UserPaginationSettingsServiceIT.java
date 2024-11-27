package org.synyx.urlaubsverwaltung.user.pagination;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {UserPaginationSettingsService.class, SpringDataWebProperties.class})
class UserPaginationSettingsServiceIT {

    @Autowired
    private UserPaginationSettingsService sut;

    @MockitoBean
    private UserPaginationSettingsRepository repository;

    @Test
    void ensureDefaultPageSize() {

        when(repository.findByPersonId(1L)).thenReturn(Optional.empty());

        final UserPaginationSettings actual = sut.getUserPaginationSettings(new PersonId(1L));

        assertThat(actual.getDefaultPageSize()).isEqualTo(20);
    }
}
