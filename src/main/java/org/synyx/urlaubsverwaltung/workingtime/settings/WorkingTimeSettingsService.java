package org.synyx.urlaubsverwaltung.workingtime.settings;

import com.google.api.client.util.Lists;
import org.springframework.stereotype.Service;

@Service
public class WorkingTimeSettingsService {

    private final WorkingTimeSettingsRepository workingTimeSettingsRepository;

    public WorkingTimeSettingsService(WorkingTimeSettingsRepository workingTimeSettingsRepository) {
        this.workingTimeSettingsRepository = workingTimeSettingsRepository;
    }

    public WorkingTimeSettingsDto getSettingsDto() {

        WorkingTimeSettingsEntity workingTimeSettingsEntity = workingTimeSettingsRepository.findFirstBy();

        return WorkingTimeSettingsDtoMapper.mapToWorkingTimeSettingsDto(workingTimeSettingsEntity);
    }

    public void save(WorkingTimeSettingsDto workingTimeSettingsDto) {

        WorkingTimeSettingsEntity workingTimeSettingsEntity = WorkingTimeSettingsDtoMapper.mapToWorkingTimeSettingsEntity(workingTimeSettingsDto);
        workingTimeSettingsRepository.save(workingTimeSettingsEntity);
    }
}
