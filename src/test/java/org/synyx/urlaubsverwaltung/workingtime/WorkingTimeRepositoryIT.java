package org.synyx.urlaubsverwaltung.workingtime;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;

import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class WorkingTimeRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private WorkingTimeRepository sut;

    @Autowired
    private PersonService personService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void ensureUniqueConstraintOfPersonAndValidFrom() {

        final Person person = personService.create("muster", "Marlene", "Muster", "muster@example.org");
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);

        final WorkingTimeEntity workingTime = new WorkingTimeEntity();
        workingTime.setPerson(person);
        workingTime.setValidFrom(validFrom);
        sut.save(workingTime);
        entityManager.flush();

        final WorkingTimeEntity sameValidFrom = new WorkingTimeEntity();
        sameValidFrom.setPerson(person);
        sameValidFrom.setValidFrom(validFrom);
        sut.save(sameValidFrom);

        assertThatThrownBy(() -> entityManager.flush())
            .isInstanceOf(PersistenceException.class);
    }
}
