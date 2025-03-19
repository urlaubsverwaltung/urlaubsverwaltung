package org.synyx.urlaubsverwaltung.application.specialleave;

import org.slf4j.Logger;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SpecialLeaveSettingsService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SpecialLeaveSettingsRepository specialLeaveSettingsRepository;

    SpecialLeaveSettingsService(SpecialLeaveSettingsRepository specialLeaveSettingsRepository) {
        this.specialLeaveSettingsRepository = specialLeaveSettingsRepository;
    }

    public void saveAll(List<SpecialLeaveSettingsItem> specialLeaveSettingsItems) {

        final Map<Long, SpecialLeaveSettingsItem> itemsById = specialLeaveSettingsItems.stream()
            .collect(toMap(SpecialLeaveSettingsItem::id, identity()));

        final List<SpecialLeaveSettingsEntity> entities = specialLeaveSettingsRepository.findAllById(itemsById.keySet()).stream()
            .map(entity -> mergeUpdates(itemsById, entity))
            .toList();

        specialLeaveSettingsRepository.saveAll(entities);
    }

    public List<SpecialLeaveSettingsItem> getSpecialLeaveSettings() {
        return specialLeaveSettingsRepository.findAll(Sort.by("id")).stream()
            .map(this::toSpecialLeaveSettingsItem)
            .toList();
    }

    private SpecialLeaveSettingsEntity mergeUpdates(Map<Long, SpecialLeaveSettingsItem> itemsById, SpecialLeaveSettingsEntity entity) {
        final SpecialLeaveSettingsItem specialLeaveSettingsItem = itemsById.get(entity.getId());
        entity.setActive(specialLeaveSettingsItem.active());
        entity.setDays(specialLeaveSettingsItem.days());
        return entity;
    }

    private SpecialLeaveSettingsItem toSpecialLeaveSettingsItem(SpecialLeaveSettingsEntity specialLeaveSettingsEntity) {
        return new SpecialLeaveSettingsItem(specialLeaveSettingsEntity.getId(),
            specialLeaveSettingsEntity.isActive(),
            specialLeaveSettingsEntity.getMessageKey(),
            specialLeaveSettingsEntity.getDays());
    }

    public void insertDefaultSpecialLeaveSettings() {

        final List<SpecialLeaveSettingsEntity> vacationTypes = List.of(
            createSpecialLeaveEntity(1, true, "application.data.specialleave.own_wedding"),
            createSpecialLeaveEntity(1, true, "application.data.specialleave.birth_of_child"),
            createSpecialLeaveEntity(2, true, "application.data.specialleave.death_of_child"),
            createSpecialLeaveEntity(1, true, "application.data.specialleave.death_of_parent"),
            createSpecialLeaveEntity(1, true, "application.data.specialleave.serious_illness_familiy_member"),
            createSpecialLeaveEntity(1, true, "application.data.specialleave.relocation_for_business_reason"),
            createSpecialLeaveEntity(1, false, "application.data.specialleave.company_anniversaries")
        );

        final List<SpecialLeaveSettingsEntity> missingVacationTypes = vacationTypes.stream()
            .filter(specialLeaveSettingsEntity -> specialLeaveSettingsRepository.findAllByMessageKey(specialLeaveSettingsEntity.getMessageKey()).isEmpty())
            .toList();

        if (!missingVacationTypes.isEmpty()) {
            final List<SpecialLeaveSettingsEntity> savesVacationTypes = specialLeaveSettingsRepository.saveAll(missingVacationTypes);
            LOG.info("Saved new special leave {}", savesVacationTypes);
        }
    }

    private static SpecialLeaveSettingsEntity createSpecialLeaveEntity(int days, boolean active, String messageKey) {
        final SpecialLeaveSettingsEntity specialLeaveSettingsEntity = new SpecialLeaveSettingsEntity();
        specialLeaveSettingsEntity.setDays(days);
        specialLeaveSettingsEntity.setActive(active);
        specialLeaveSettingsEntity.setMessageKey(messageKey);
        return specialLeaveSettingsEntity;
    }
}
