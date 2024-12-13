package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationStatusDTOTest {

    @ParameterizedTest
    @EnumSource(ApplicationStatus.class)
    void happyPathApplicationStatusToDTO(ApplicationStatus status) {
        ApplicationStatusDTO statusDTO = ApplicationStatusDTO.valueOf(status.name());
        assertThat(statusDTO).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(ApplicationStatusDTO.class)
    void happyPathDTOToApplicationStatus(ApplicationStatusDTO statusDTO) {
        ApplicationStatus status = statusDTO.toApplicationStatus();
        assertThat(status).isNotNull();
    }
}
