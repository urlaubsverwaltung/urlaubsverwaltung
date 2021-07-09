package org.synyx.urlaubsverwaltung.specialleave;

import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

import static org.synyx.urlaubsverwaltung.specialleave.SpecialLeaveDtoMapper.mapToSpecialLeaveDto;
import static org.synyx.urlaubsverwaltung.specialleave.SpecialLeaveDtoMapper.mapToSpecialLeaveEntities;

@Service
public class SpecialLeaveSettingsService {

    private final SpecialLeaveRepository specialLeaveRepository;

    public SpecialLeaveSettingsService(SpecialLeaveRepository specialLeaveRepository) {
        this.specialLeaveRepository = specialLeaveRepository;
    }

    @Transactional
    public void save(SpecialLeaveSettingsDto specialLeaveSettingsDto) {

        mapToSpecialLeaveEntities(specialLeaveSettingsDto).forEach(specialLeaveEntity ->
            specialLeaveRepository.updateDaysOfSpecialLeave(specialLeaveEntity.getDays(), specialLeaveEntity.getSpecialLeave())
            );
    }

    public SpecialLeaveSettingsDto getSettingsDto() {

        List<SpecialLeaveEntity> specialLeaveEntities = Streamable.of(specialLeaveRepository.findAll()).toList();
        return mapToSpecialLeaveDto(specialLeaveEntities);
    }
}
