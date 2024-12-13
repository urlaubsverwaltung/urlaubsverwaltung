package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.application.application.HolidayReplacementEntity;
import org.synyx.urlaubsverwaltung.person.Person;

import static org.assertj.core.api.Assertions.assertThat;

class HolidayReplacementDTOTest {

    @Test
    void happyPath() {
        Person person = new Person();
        HolidayReplacementDTO dto = new HolidayReplacementDTO("externalId", "Some note");

        HolidayReplacementEntity entity = dto.toHolidayReplacementEntity(person);

        assertThat(entity).isNotNull();
        assertThat(entity.getPerson()).isEqualTo(person);
        assertThat(entity.getNote()).isEqualTo(dto.note());
    }

}
