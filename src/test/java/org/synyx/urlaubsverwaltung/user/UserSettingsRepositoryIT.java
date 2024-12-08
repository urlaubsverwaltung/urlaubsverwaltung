package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.user.Theme.SYSTEM;

@SpringBootTest
@Transactional
class UserSettingsRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private UserSettingsRepository sut;

    @Autowired
    private PersonService personService;

    @Test
    void ensuresToFindUserSettingsByUsername() {

        final Person marlene = personService.create("muster", "Marlene", "Muster", "muster@example.org");

        final UserSettingsEntity userSettingMarlene = new UserSettingsEntity();
        userSettingMarlene.setPersonId(marlene.getId());
        userSettingMarlene.setTheme(SYSTEM);
        userSettingMarlene.setLocale(Locale.GERMAN);
        sut.save(userSettingMarlene);

        final Person petra = personService.create("petra", "Petra", "Petra", "petra@example.org");

        final UserSettingsEntity userSettingPetra = new UserSettingsEntity();
        userSettingPetra.setPersonId(petra.getId());
        userSettingPetra.setTheme(SYSTEM);
        userSettingPetra.setLocale(Locale.GERMAN);
        sut.save(userSettingPetra);

        final Optional<UserSettingsEntity> savedUserSettingOfPetra = sut.findByPersonUsername(petra.getUsername());
        assertThat(savedUserSettingOfPetra).hasValue(userSettingPetra);
    }
}
