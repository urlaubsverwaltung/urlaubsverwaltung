package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;

@SpringBootTest
@Transactional
class VacationTypeEntityRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private VacationTypeRepository sut;

    @Test
    void ensureSaveCustomVacationType() {

        final VacationTypeEntity typeEntity = new VacationTypeEntity();
        typeEntity.setActive(true);
        typeEntity.setCustom(true);
        typeEntity.setCategory(HOLIDAY);
        typeEntity.setMessageKey(null);
        typeEntity.setLabelByLocale(Map.of(
            Locale.GERMAN, "label de",
            Locale.ENGLISH, "label en"
        ));
        typeEntity.setColor(YELLOW);

        final VacationTypeEntity actualSaved = sut.save(typeEntity);

        assertThat(actualSaved.getId()).isNotNull();
    }

    @Test
    void findByActiveIsTrue() {

        sut.deleteAll();

        final VacationTypeEntity active = new VacationTypeEntity();
        active.setActive(true);
        active.setCategory(HOLIDAY);
        active.setMessageKey("message.key.active");
        active.setColor(YELLOW);
        final VacationTypeEntity activeSaved = sut.save(active);

        final VacationTypeEntity inactive = new VacationTypeEntity();
        inactive.setActive(false);
        inactive.setCategory(OVERTIME);
        inactive.setMessageKey("message.key.inactive");
        inactive.setColor(YELLOW);
        sut.save(inactive);

        final List<VacationTypeEntity> activeVacationTypes = sut.findByActiveIsTrueOrderById();
        assertThat(activeVacationTypes)
            .hasSize(1)
            .contains(activeSaved);
    }
}
