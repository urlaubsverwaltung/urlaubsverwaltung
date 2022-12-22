package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;

@SpringBootTest
@Transactional
class VacationTypeEntityRepositoryIT extends TestContainersBase {

    @Autowired
    private VacationTypeRepository sut;

    @Test
    void findByActiveIsTrue() {

        sut.deleteAll();

        final VacationTypeEntity active = new VacationTypeEntity();
        active.setId(1L);
        active.setActive(true);
        active.setCategory(HOLIDAY);
        active.setMessageKey("message.key.active");
        active.setColor(YELLOW);
        sut.save(active);

        final VacationTypeEntity inactive = new VacationTypeEntity();
        inactive.setId(2L);
        inactive.setActive(false);
        inactive.setCategory(OVERTIME);
        inactive.setMessageKey("message.key.inactive");
        inactive.setColor(YELLOW);
        sut.save(inactive);

        final List<VacationTypeEntity> activeVacationTypes = sut.findByActiveIsTrue();
        assertThat(activeVacationTypes)
            .hasSize(1)
            .contains(active);
    }
}
