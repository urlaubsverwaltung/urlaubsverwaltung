package org.synyx.urlaubsverwaltung.application.specialleave;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecialLeaveSettingsItemServiceTest {

    @Mock
    private SpecialLeaveSettingsRepository specialLeaveSettingsRepository;
    @Captor
    private ArgumentCaptor<List<SpecialLeaveSettingsEntity>> specialLeaveSettingsEntityList;
    private SpecialLeaveSettingsService specialLeaveSettingsService;

    @BeforeEach
    void setUp() {
        specialLeaveSettingsService = new SpecialLeaveSettingsService(specialLeaveSettingsRepository);
    }

    @Test
    void save() {
        final SpecialLeaveSettingsEntity specialLeaveSettingsEntity = new SpecialLeaveSettingsEntity();
        specialLeaveSettingsEntity.setId(1);
        when(specialLeaveSettingsRepository.findAll()).thenReturn(List.of(specialLeaveSettingsEntity));

        SpecialLeaveSettingsItem specialLeaveSettings = new SpecialLeaveSettingsItem(1, true, "foo", 5);
        final List<SpecialLeaveSettingsItem> specialLeaveSettingsItemList = List.of(specialLeaveSettings);
        specialLeaveSettingsService.saveAll(specialLeaveSettingsItemList);

        verify(specialLeaveSettingsRepository).saveAll(this.specialLeaveSettingsEntityList.capture());
        final List<SpecialLeaveSettingsEntity> leaveSettingsEntityListValue = this.specialLeaveSettingsEntityList.getValue();
        assertThat(leaveSettingsEntityListValue).hasSize(1);

        assertThat(leaveSettingsEntityListValue)
            .usingRecursiveComparison()
            .ignoringFields("messageKey")
            .isEqualTo(specialLeaveSettingsItemList);
    }

    @Test
    void getSpecialLeaveSettings() {
        final SpecialLeaveSettingsEntity specialLeaveSettingsEntity = new SpecialLeaveSettingsEntity();
        when(specialLeaveSettingsRepository.findAll()).thenReturn(List.of(specialLeaveSettingsEntity));

        final List<SpecialLeaveSettingsItem> specialLeaveSettings = specialLeaveSettingsService.getSpecialLeaveSettings();

        assertThat(specialLeaveSettings).hasSize(1);
        assertThat(specialLeaveSettingsEntity).usingRecursiveComparison().isEqualTo(specialLeaveSettings.get(0));
    }

    @Test
    void getSpecialLeaveSettingsReturnsEmpty() {
        when(specialLeaveSettingsRepository.findAll()).thenReturn(Collections.emptyList());
        final List<SpecialLeaveSettingsItem> specialLeaveSettings = specialLeaveSettingsService.getSpecialLeaveSettings();
        assertThat(specialLeaveSettings).isEmpty();
    }
}
