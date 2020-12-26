package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
@Transactional
class PersonRepositoryIT extends TestContainersBase {

    @Autowired
    private PersonRepository sut;

    @Autowired
    private PersonService personService;

    @Test
    void countPersonByPermissionsIsNot() {

        final Person marlene = new Person("marlene", "Muster", "Marlene", "muster@example.org");
        marlene.setPermissions(List.of(USER, INACTIVE));
        personService.save(marlene);

        final Person peter = new Person("peter", "Muster", "Peter", "peter@example.org");
        peter.setPermissions(List.of(USER, OFFICE));
        personService.save(peter);

        final Person bettina = new Person("bettina", "Muster", "bettina", "bettina@example.org");
        bettina.setPermissions(List.of(USER));
        personService.save(bettina);

        final int countOfActivePersons = sut.countByPermissionsNotContaining(INACTIVE);
        assertThat(countOfActivePersons).isEqualTo(2);
    }
}



