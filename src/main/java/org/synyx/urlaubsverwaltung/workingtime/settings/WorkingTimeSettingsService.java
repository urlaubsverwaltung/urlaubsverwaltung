package org.synyx.urlaubsverwaltung.workingtime.settings;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeProperties;

@Service
public class WorkingTimeSettingsService {

    private final WorkingTimeSettingsRepository workingTimeSettingsRepository;
    private final WorkingTimeProperties workingTimeProperties;

    public WorkingTimeSettingsService(WorkingTimeSettingsRepository workingTimeSettingsRepository,
                                      WorkingTimeProperties workingTimeProperties) {
        this.workingTimeSettingsRepository = workingTimeSettingsRepository;
        this.workingTimeProperties = workingTimeProperties;
    }

    public WorkingTimeSettingsDto getSettingsDto() {

        WorkingTimeSettingsEntity workingTimeSettingsEntity = workingTimeSettingsRepository.findFirstBy();

        WorkingTimeSettingsDto workingTimeSettingsDto = WorkingTimeSettingsMapper.mapToWorkingTimeSettingsDto(workingTimeSettingsEntity);
        workingTimeSettingsDto.setDefaultWorkingDaysDeactivated(workingTimeProperties.isDefaultWorkingDaysDeactivated());
        return workingTimeSettingsDto;
    }

    public void save(WorkingTimeSettingsDto workingTimeSettingsDto) {

        WorkingTimeSettingsEntity workingTimeSettingsEntity = WorkingTimeSettingsMapper.mapToWorkingTimeSettingsEntity(workingTimeSettingsDto);
        workingTimeSettingsRepository.save(workingTimeSettingsEntity);
    }

    public WorkingTimeSettings getSettings() {
        WorkingTimeSettingsEntity settingsEntity = workingTimeSettingsRepository.findFirstBy();
        return WorkingTimeSettingsMapper.mapToWorkingTimeSettingsModel(settingsEntity);
    }
}
