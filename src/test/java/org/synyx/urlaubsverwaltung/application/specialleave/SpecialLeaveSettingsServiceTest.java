package org.synyx.urlaubsverwaltung.application.specialleave;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecialLeaveSettingsServiceTest {

    private SpecialLeaveSettingsService sut;

    @Mock
    private SpecialLeaveSettingsRepository specialLeaveSettingsRepository;
    @Captor
    private ArgumentCaptor<List<SpecialLeaveSettingsEntity>> specialLeaveSettingsListArgument;

    @BeforeEach
    void setUp() {
        sut = new SpecialLeaveSettingsService(specialLeaveSettingsRepository);
    }

    @Test
    void saveAll() {

        final SpecialLeaveSettingsEntity specialLeaveSettingsEntity = new SpecialLeaveSettingsEntity();
        specialLeaveSettingsEntity.setId(1L);
        specialLeaveSettingsEntity.setActive(false);
        specialLeaveSettingsEntity.setMessageKey("messageKey");
        specialLeaveSettingsEntity.setDays(2);
        when(specialLeaveSettingsRepository.findAllById(Set.of(1L))).thenReturn(List.of(specialLeaveSettingsEntity));

        final SpecialLeaveSettingsItem specialLeaveSettingsItem = new SpecialLeaveSettingsItem(1L, false, "messageKey", 2);
        sut.saveAll(List.of(specialLeaveSettingsItem));

        verify(specialLeaveSettingsRepository).saveAll(specialLeaveSettingsListArgument.capture());
        final List<SpecialLeaveSettingsEntity> specialLeaveSettingsEntities = specialLeaveSettingsListArgument.getValue();
        assertThat(specialLeaveSettingsEntities)
            .hasSize(1)
            .extracting("id", "active", "messageKey", "days")
            .contains(tuple(1L, false, "messageKey", 2));
    }

    @Test
    void getSpecialLeaveSettings() {
        final SpecialLeaveSettingsEntity specialLeaveSettingsEntity = new SpecialLeaveSettingsEntity();
        specialLeaveSettingsEntity.setId(1L);
        specialLeaveSettingsEntity.setActive(false);
        specialLeaveSettingsEntity.setMessageKey("messageKey");
        specialLeaveSettingsEntity.setDays(2);
        when(specialLeaveSettingsRepository.findAll(Sort.by("id"))).thenReturn(List.of(specialLeaveSettingsEntity));

        final List<SpecialLeaveSettingsItem> specialLeaveSettings = sut.getSpecialLeaveSettings();
        assertThat(specialLeaveSettings)
            .extracting("id", "active", "messageKey", "days")
            .contains(tuple(1L, false, "messageKey", 2));
    }
}
