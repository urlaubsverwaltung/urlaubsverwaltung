package org.synyx.urlaubsverwaltung.specialleave;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecialLeaveSettingsServiceTest {

    @Mock
    private SpecialLeaveSettingsRepository specialLeaveSettingsRepository;
    @Captor
    private ArgumentCaptor<SpecialLeaveSettingsEntity> specialLeaveSettingsEntity;
    private SpecialLeaveSettingsService specialLeaveSettingsService;

    @BeforeEach
    void setUp() {
        specialLeaveSettingsService = new SpecialLeaveSettingsService(specialLeaveSettingsRepository, new ObjectMapper());
    }

    @Test
    void save() {
        SpecialLeaveSettings specialLeaveSettings = new SpecialLeaveSettings(1,2,3,4,5,6);

        specialLeaveSettingsService.save(specialLeaveSettings);

        verify(specialLeaveSettingsRepository).save(this.specialLeaveSettingsEntity.capture());
        assertThat(specialLeaveSettings).usingRecursiveComparison().isEqualTo(this.specialLeaveSettingsEntity.getValue());
    }

    @Test
    void getSpecialLeaveSettings() {
        final SpecialLeaveSettingsEntity specialLeaveSettingsEntity = new SpecialLeaveSettingsEntity();
        when(specialLeaveSettingsRepository.findAll()).thenReturn(List.of(specialLeaveSettingsEntity));
        final Optional<SpecialLeaveSettings> specialLeaveSettings = specialLeaveSettingsService.getSpecialLeaveSettings();
        assertThat(specialLeaveSettings).isNotEmpty();
        assertThat(specialLeaveSettingsEntity).usingRecursiveComparison().isEqualTo(specialLeaveSettings.get());
    }

    @Test
    void getSpecialLeaveSettingsReturnsEmpty() {
        when(specialLeaveSettingsRepository.findAll()).thenReturn(Collections.emptyList());
        final Optional<SpecialLeaveSettings> specialLeaveSettings = specialLeaveSettingsService.getSpecialLeaveSettings();
        assertThat(specialLeaveSettings).isEmpty();
    }
}
