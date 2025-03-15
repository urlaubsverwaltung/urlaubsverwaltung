package org.synyx.urlaubsverwaltung.application.specialleave;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SpecialLeaveSettingsRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private SpecialLeaveSettingsRepository sut;

    @Test
    void ensureFindAllByMessageKey() {

        final SpecialLeaveSettingsEntity specialLeaveSettingsEntity = new SpecialLeaveSettingsEntity();
        specialLeaveSettingsEntity.setDays(1);
        specialLeaveSettingsEntity.setActive(true);
        specialLeaveSettingsEntity.setMessageKey("some-message-key");

        sut.save(specialLeaveSettingsEntity);

        final Optional<SpecialLeaveSettingsEntity> savedSpecialLeave = sut.findAllByMessageKey("some-message-key");
        assertThat(savedSpecialLeave.get().getMessageKey()).isEqualTo("some-message-key");
    }
}
