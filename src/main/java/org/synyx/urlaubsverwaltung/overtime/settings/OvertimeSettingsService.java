package org.synyx.urlaubsverwaltung.overtime.settings;

import org.springframework.stereotype.Service;

@Service
public class OvertimeSettingsService {

    private final OvertimeSettingsRepository overtimeSettingsRepository;

    public OvertimeSettingsService(OvertimeSettingsRepository overtimeSettingsRepository) {
        this.overtimeSettingsRepository = overtimeSettingsRepository;
    }

    public OvertimeSettingsDto getSettingsDto() {
        return OvertimeSettingsMapper.mapToOvertimeSettingsDto(overtimeSettingsRepository.findFirstBy());
    }

    public void save(OvertimeSettingsDto overtimeSettingsDto) {
        overtimeSettingsRepository.save(OvertimeSettingsMapper.mapToOvertimeSettingsEntity(overtimeSettingsDto));
    }

    public OvertimeSettings getSettings() {
        OvertimeSettingsEntity settingsEntity = overtimeSettingsRepository.findFirstBy();
        return OvertimeSettingsMapper.mapToOvertimeSettingsModel(settingsEntity);
    }

    public void save(OvertimeSettings overtimeSettings) {
        OvertimeSettingsEntity overtimeSettingsEntity = OvertimeSettingsMapper.mapToOvertimeSettingsEntity(overtimeSettings);
        overtimeSettingsRepository.save(overtimeSettingsEntity);
    }
}
