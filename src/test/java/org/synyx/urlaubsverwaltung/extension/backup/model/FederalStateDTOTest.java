package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import static org.assertj.core.api.Assertions.assertThat;

class FederalStateDTOTest {

    @ParameterizedTest
    @EnumSource(FederalState.class)
    void happyPathFederalStateToDTO(FederalState federalState) {
        FederalStateDTO dto = FederalStateDTO.valueOf(federalState.name());
        assertThat(dto).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(FederalStateDTO.class)
    void happyPathDTOToFederalState(FederalStateDTO dto) {
        FederalState federalState = dto.toFederalState();
        assertThat(federalState).isNotNull();
    }
}
