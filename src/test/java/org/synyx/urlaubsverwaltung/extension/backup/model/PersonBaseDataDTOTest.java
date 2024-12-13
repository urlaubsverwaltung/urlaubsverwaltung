package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataEntity;

import static org.assertj.core.api.Assertions.assertThat;

class PersonBaseDataDTOTest {

    @Test
    void happyPath() {
        PersonBaseDataDTO dto = new PersonBaseDataDTO("12345", "Some info");

        final PersonBasedataEntity entity = dto.toPersonBaseDataEntity(1L);

        assertThat(entity.getPersonId()).isEqualTo(1L);
        assertThat(entity.getPersonnelNumber()).isEqualTo(dto.personnelNumber());
        assertThat(entity.getAdditionalInformation()).isEqualTo(dto.additionalInformation());
    }

}
