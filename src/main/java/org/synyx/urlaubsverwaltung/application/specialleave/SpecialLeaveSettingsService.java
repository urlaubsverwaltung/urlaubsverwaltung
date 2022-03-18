package org.synyx.urlaubsverwaltung.application.specialleave;

import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class SpecialLeaveSettingsService {

    private final SpecialLeaveSettingsRepository specialLeaveSettingsRepository;

    public SpecialLeaveSettingsService(SpecialLeaveSettingsRepository specialLeaveSettingsRepository) {
        this.specialLeaveSettingsRepository = specialLeaveSettingsRepository;
    }

    public void saveAll(List<SpecialLeaveSettingsItem> specialLeaveSettingsItems) {

        final Map<Integer, SpecialLeaveSettingsItem> itemsById = Streamable.of(specialLeaveSettingsItems).stream()
            .collect(toMap(SpecialLeaveSettingsItem::getId, entity -> entity));

        final List<SpecialLeaveSettingsEntity> specialLeaveSettingsEntities = Streamable.of(specialLeaveSettingsRepository.findAll()).stream()
            .map(entity -> mergeUpdates(itemsById, entity))
            .collect(toList());
        specialLeaveSettingsRepository.saveAll(specialLeaveSettingsEntities);
    }

    public List<SpecialLeaveSettingsItem> getSpecialLeaveSettings() {
        final Iterable<SpecialLeaveSettingsEntity> allSpecialLeaveSettings = this.specialLeaveSettingsRepository.findAll();
        return Streamable.of(allSpecialLeaveSettings).map(this::toSpecialLeaveSettingsItem).toList();
    }

    private SpecialLeaveSettingsEntity mergeUpdates(Map<Integer, SpecialLeaveSettingsItem> itemsById, SpecialLeaveSettingsEntity entity) {
        final SpecialLeaveSettingsItem specialLeaveSettingsItem = itemsById.get(entity.getId());
        entity.setActive(specialLeaveSettingsItem.isActive());
        entity.setDays(specialLeaveSettingsItem.getDays());
        return entity;
    }

    private SpecialLeaveSettingsItem toSpecialLeaveSettingsItem(SpecialLeaveSettingsEntity specialLeaveSettingsEntity) {
        return new SpecialLeaveSettingsItem(specialLeaveSettingsEntity.getId(),
            specialLeaveSettingsEntity.isActive(),
            specialLeaveSettingsEntity.getMessageKey(),
            specialLeaveSettingsEntity.getDays());
    }

}
