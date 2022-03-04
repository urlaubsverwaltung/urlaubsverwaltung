package org.synyx.urlaubsverwaltung.specialleave;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpecialLeaveSettingsService {

    private final SpecialLeaveSettingsRepository specialLeaveSettingsRepository;
    private final ObjectMapper objectMapper;

    public SpecialLeaveSettingsService(SpecialLeaveSettingsRepository specialLeaveSettingsRepository, ObjectMapper objectMapper) {
        this.specialLeaveSettingsRepository = specialLeaveSettingsRepository;
        this.objectMapper = objectMapper;
    }

    public void save(SpecialLeaveSettings specialLeave) {
        specialLeaveSettingsRepository.save(mapToEntity(specialLeave));
    }

    public Optional<SpecialLeaveSettings> getSpecialLeaveSettings() {
        final Iterable<SpecialLeaveSettingsEntity> allEntities = this.specialLeaveSettingsRepository.findAll();
        if (allEntities.iterator().hasNext()) {
            final SpecialLeaveSettingsEntity specialLeaveSettingsEntity = allEntities.iterator().next();
            return Optional.of(mapFromEntity(specialLeaveSettingsEntity));
        } else {
            return Optional.empty();
        }
    }

    private SpecialLeaveSettings mapFromEntity(SpecialLeaveSettingsEntity specialLeaveSettingsEntity) {
        return objectMapper.convertValue(specialLeaveSettingsEntity, SpecialLeaveSettings.class);
    }

    private SpecialLeaveSettingsEntity mapToEntity(SpecialLeaveSettings specialLeave) {
        return objectMapper.convertValue(specialLeave, SpecialLeaveSettingsEntity.class);
    }

}
