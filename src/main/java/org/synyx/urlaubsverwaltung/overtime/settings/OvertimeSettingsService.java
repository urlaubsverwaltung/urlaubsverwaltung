package org.synyx.urlaubsverwaltung.overtime.settings;

import org.springframework.stereotype.Service;

@Service
public class OvertimeSettingsService {

    private final OvertimeSettingsRepository overtimeSettingsRepository;

    public OvertimeSettingsService(OvertimeSettingsRepository overtimeSettingsRepository) {
        this.overtimeSettingsRepository = overtimeSettingsRepository;
    }

    public OvertimeSettingsDto getSettingsDto() {
        return OvertimeSettingsDtoMapper.mapToOvertimeSettingsDto(overtimeSettingsRepository.findFirstBy());
    }

    public void save(OvertimeSettingsDto overtimeSettingsDto) {
        overtimeSettingsRepository.save(OvertimeSettingsDtoMapper.mapToOvertimeSettingsEntiy(overtimeSettingsDto));
    }

    public OvertimeSettingsEntity getSettings() {
        return overtimeSettingsRepository.findFirstBy();
    }

    public void save(OvertimeSettingsEntity overtimeSettingsEntity) {
        overtimeSettingsRepository.save(overtimeSettingsEntity);
    }
}
