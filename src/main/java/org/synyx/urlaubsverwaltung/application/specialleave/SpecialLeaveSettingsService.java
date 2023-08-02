package org.synyx.urlaubsverwaltung.application.specialleave;

import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
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
            .collect(toMap(SpecialLeaveSettingsItem::getId, identity()));

        final List<SpecialLeaveSettingsEntity> entities = specialLeaveSettingsRepository.findAllById(itemsById.keySet()).stream()
            .map(entity -> mergeUpdates(itemsById, entity))
            .collect(toList());

        specialLeaveSettingsRepository.saveAll(entities);
    }

    public List<SpecialLeaveSettingsItem> getSpecialLeaveSettings() {
        return specialLeaveSettingsRepository.findAll().stream()
            .map(this::toSpecialLeaveSettingsItem)
            .collect(toList());
    }

    private SpecialLeaveSettingsEntity mergeUpdates(Map<Long, SpecialLeaveSettingsItem> itemsById, SpecialLeaveSettingsEntity entity) {
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

    @EventListener
    void insertDefaultSpecialLeaveSettings(ApplicationStartedEvent event) {
        final long count = specialLeaveSettingsRepository.count();
        if (count == 0) {

            final SpecialLeaveSettingsEntity ownWedding = createSpecialLeaveEntity(1L, 1, true, "application.data.specialleave.own_wedding");
            final SpecialLeaveSettingsEntity birthOfChild = createSpecialLeaveEntity(2L, 1, true, "application.data.specialleave.birth_of_child");
            final SpecialLeaveSettingsEntity deathOfChild = createSpecialLeaveEntity(3L, 2, true, "application.data.specialleave.death_of_child");
            final SpecialLeaveSettingsEntity deathOfParent = createSpecialLeaveEntity(4L, 1, true, "application.data.specialleave.death_of_parent");
            final SpecialLeaveSettingsEntity seriousIllnessFamilyMember = createSpecialLeaveEntity(5L, 1, true, "application.data.specialleave.serious_illness_familiy_member");
            final SpecialLeaveSettingsEntity relocationForBusinessReason = createSpecialLeaveEntity(6L, 1, true, "application.data.specialleave.relocation_for_business_reason");

            final List<SpecialLeaveSettingsEntity> vacationTypes = List.of(ownWedding, birthOfChild, deathOfChild, deathOfParent, seriousIllnessFamilyMember, relocationForBusinessReason);
            final List<SpecialLeaveSettingsEntity> savesVacationTypes = specialLeaveSettingsRepository.saveAll(vacationTypes);
            LOG.info("Saved initial special leave {}", savesVacationTypes);
        }
    }

    private static SpecialLeaveSettingsEntity createSpecialLeaveEntity(Long id, int days, boolean active, String messageKey) {
        final SpecialLeaveSettingsEntity specialLeaveSettingsEntity = new SpecialLeaveSettingsEntity();
        specialLeaveSettingsEntity.setId(id);
        specialLeaveSettingsEntity.setDays(days);
        specialLeaveSettingsEntity.setActive(active);
        specialLeaveSettingsEntity.setMessageKey(messageKey);
        return specialLeaveSettingsEntity;
    }
}
