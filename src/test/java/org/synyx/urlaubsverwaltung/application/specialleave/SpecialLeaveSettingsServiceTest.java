package org.synyx.urlaubsverwaltung.application.specialleave;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecialLeaveSettingsServiceTest {

    private SpecialLeaveSettingsService specialLeaveSettingsService;

    @Mock
    private SpecialLeaveSettingsRepository specialLeaveSettingsRepository;

    @Captor
    private ArgumentCaptor<Iterable<SpecialLeaveSettingsEntity>> specialLeaveSettingsListArgument;

    @BeforeEach
    void setUp() {
        specialLeaveSettingsService = new SpecialLeaveSettingsService(specialLeaveSettingsRepository);
    }

    @Test
    void saveAll() {
        Integer id = 1;
        Boolean active = false;
        String messageKey = "messageKey";
        Integer days = 2;
        SpecialLeaveSettingsItem specialLeaveSettingsItem = new SpecialLeaveSettingsItem(id, active, messageKey, days);

        final SpecialLeaveSettingsEntity specialLeaveSettingsEntity = new SpecialLeaveSettingsEntity();
        specialLeaveSettingsEntity.setId(id);
        specialLeaveSettingsEntity.setActive(active);
        specialLeaveSettingsEntity.setMessageKey(messageKey);
        specialLeaveSettingsEntity.setDays(days);
        when(specialLeaveSettingsRepository.findAll()).thenReturn(List.of(specialLeaveSettingsEntity));

        List<SpecialLeaveSettingsItem> specialLeaveSettingsItems = List.of(specialLeaveSettingsItem);

        specialLeaveSettingsService.saveAll(specialLeaveSettingsItems);

        verify(specialLeaveSettingsRepository).saveAll(specialLeaveSettingsListArgument.capture());

        final Iterable<SpecialLeaveSettingsEntity> specialLeaveSettingsListArgumentValue = specialLeaveSettingsListArgument.getValue();
        assertThat(specialLeaveSettingsListArgumentValue)
            .extracting("id","active", "messageKey", "days")
            .contains(tuple(id, active, messageKey, days));
    }

    @Test
    void getSpecialLeaveSettings() {
        Integer id = 1;
        Boolean active = false;
        String messageKey = "messageKey";
        Integer days = 2;

        final SpecialLeaveSettingsEntity specialLeaveSettingsEntity = new SpecialLeaveSettingsEntity();
        specialLeaveSettingsEntity.setId(id);
        specialLeaveSettingsEntity.setActive(active);
        specialLeaveSettingsEntity.setMessageKey(messageKey);
        specialLeaveSettingsEntity.setDays(days);
        when(specialLeaveSettingsRepository.findAll()).thenReturn(List.of(specialLeaveSettingsEntity));


        final List<SpecialLeaveSettingsItem> specialLeaveSettings = specialLeaveSettingsService.getSpecialLeaveSettings();

        assertThat(specialLeaveSettings)
            .extracting("id","active", "messageKey", "days")
            .contains(tuple(id, active, messageKey, days));
    }
}
