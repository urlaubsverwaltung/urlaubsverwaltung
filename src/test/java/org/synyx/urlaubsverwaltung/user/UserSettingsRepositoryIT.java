package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.user.Theme.SYSTEM;

@SpringBootTest
@Transactional
class UserSettingsRepositoryIT extends TestContainersBase {

    @Autowired
    private UserSettingsRepository sut;

    @Autowired
    private PersonService personService;

    @Test
    void ensuresToFindUserSettingsByUsername() {

        final Person marlene = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person savedMarlene = personService.create(marlene);

        final UserSettingsEntity userSettingMarlene = new UserSettingsEntity();
        userSettingMarlene.setPersonId(savedMarlene.getId());
        userSettingMarlene.setTheme(SYSTEM);
        userSettingMarlene.setLocale(Locale.GERMAN);
        sut.save(userSettingMarlene);

        final Person petra = new Person("petra", "Petra", "Petra", "petra@example.org");
        final Person savedPetra = personService.create(petra);

        final UserSettingsEntity userSettingPetra = new UserSettingsEntity();
        userSettingPetra.setPersonId(savedPetra.getId());
        userSettingPetra.setTheme(SYSTEM);
        userSettingPetra.setLocale(Locale.GERMAN);
        sut.save(userSettingPetra);

        final Optional<UserSettingsEntity> savedUserSettingOfPetra = sut.findByPersonUsername(petra.getUsername());
        assertThat(savedUserSettingOfPetra).hasValue(userSettingPetra);
    }
}
