package org.synyx.urlaubsverwaltung.user.pagination;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserPaginationSettingsRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private UserPaginationSettingsRepository sut;

    @Autowired
    private PersonService personService;

    @Autowired
    private UserPaginationSettingsService userPaginationSettingsService;

    @Test
    void ensureFindByPersonId() {

        final Person person = personService.create("batman", "Wayne", "Bruce", "batman@example.org");

        userPaginationSettingsService.updatePageableDefaultSize(new PersonId(person.getId()), 9001);

        final Optional<UserPaginationSettingsEntity> actualMaybe = sut.findByPersonId(person.getId());

        assertThat(actualMaybe).hasValueSatisfying(actual -> {
            assertThat(actual.getPersonId()).isEqualTo(person.getId());
            assertThat(actual.getDefaultPageSize()).isEqualTo(9001);
        });
    }
}
