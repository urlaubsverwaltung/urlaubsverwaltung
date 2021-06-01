package org.synyx.urlaubsverwaltung.absence.settings;

import org.springframework.stereotype.Service;

@Service
public class TimeSettingsService {

    private final TimeSettingsRepository timeSettingsRepository;

    public TimeSettingsService(TimeSettingsRepository timeSettingsRepository) {
        this.timeSettingsRepository = timeSettingsRepository;
    }

    public TimeSettingsDto getSettingsDto() {

        TimeSettingsEntity timeSettingsEntity = timeSettingsRepository.findFirstBy();
        return TimeSettingsDtoMapper.mapToWorkingTimeSettingsDto(timeSettingsEntity);
    }

    public void save(TimeSettingsDto timeSettingsDto) {
        timeSettingsRepository.save(TimeSettingsDtoMapper.mapToWorkingTimeSettingsEntity(timeSettingsDto));
    }
}
