package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OTHER;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.UNPAIDLEAVE;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;

@Service
@Transactional
public class VacationTypeServiceImpl implements VacationTypeService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final VacationTypeRepository vacationTypeRepository;

    @Autowired
    VacationTypeServiceImpl(VacationTypeRepository vacationTypeRepository) {
        this.vacationTypeRepository = vacationTypeRepository;
    }

    @Override
    public Optional<VacationType> getById(Long id) {
        return Optional.of(convert(vacationTypeRepository.getReferenceById(id)));
    }

    @Override
    public List<VacationType> getAllVacationTypes() {
        return vacationTypeRepository.findAll(Sort.by("id")).stream()
            .map(convertToVacationType())
            .collect(toList());
    }

    @Override
    public List<VacationType> getActiveVacationTypes() {
        return vacationTypeRepository.findByActiveIsTrueOrderById().stream()
            .map(convertToVacationType())
            .collect(toList());
    }

    @Override
    public List<VacationType> getActiveVacationTypesWithoutCategory(VacationCategory vacationCategory) {
        return getActiveVacationTypes().stream()
            .filter(vacationType -> vacationType.getCategory() != vacationCategory)
            .collect(toList());
    }

    @Override
    public void updateVacationTypes(List<VacationTypeUpdate> vacationTypeUpdates) {

        final Map<Long, VacationTypeUpdate> byId = vacationTypeUpdates.stream().collect(toMap(VacationTypeUpdate::getId, vacationTypeUpdate -> vacationTypeUpdate));

        final List<VacationTypeEntity> updatedEntities = vacationTypeRepository.findAllById(byId.keySet())
            .stream()
            .map(VacationTypeServiceImpl::convert)
            .map(vacationType -> {
                final VacationTypeUpdate vacationTypeUpdate = byId.get(vacationType.getId());
                vacationType.setActive(vacationTypeUpdate.isActive());
                vacationType.setRequiresApprovalToApply(vacationTypeUpdate.isRequiresApprovalToApply());
                vacationType.setRequiresApprovalToCancel(vacationTypeUpdate.isRequiresApprovalToCancel());
                vacationType.setColor(vacationTypeUpdate.getColor());
                vacationType.setVisibleToEveryone(vacationTypeUpdate.isVisibleToEveryone());
                return vacationType;
            })
            .map(VacationTypeServiceImpl::convert)
            .collect(toList());

        vacationTypeRepository.saveAll(updatedEntities);
    }

    private Function<VacationTypeEntity, VacationType> convertToVacationType() {
        return vacationTypeEntity ->
            new VacationType(
                vacationTypeEntity.getId(),
                vacationTypeEntity.isActive(),
                vacationTypeEntity.getCategory(),
                vacationTypeEntity.getMessageKey(),
                vacationTypeEntity.isRequiresApprovalToApply(),
                vacationTypeEntity.isRequiresApprovalToCancel(),
                vacationTypeEntity.getColor(),
                vacationTypeEntity.isVisibleToEveryone()
            );
    }

    public static VacationTypeEntity convert(VacationType vacationType) {
        final VacationTypeEntity vacationTypeEntity = new VacationTypeEntity();
        vacationTypeEntity.setId(vacationType.getId());
        vacationTypeEntity.setActive(vacationType.isActive());
        vacationTypeEntity.setCategory(vacationType.getCategory());
        vacationTypeEntity.setMessageKey(vacationType.getMessageKey());
        vacationTypeEntity.setRequiresApprovalToApply(vacationType.isRequiresApprovalToApply());
        vacationTypeEntity.setRequiresApprovalToCancel(vacationType.isRequiresApprovalToCancel());
        vacationTypeEntity.setColor(vacationType.getColor());
        vacationTypeEntity.setVisibleToEveryone(vacationType.isVisibleToEveryone());
        return vacationTypeEntity;
    }

    public static VacationType convert(VacationTypeEntity vacationTypeEntity) {
        return new VacationType(
            vacationTypeEntity.getId(),
            vacationTypeEntity.isActive(),
            vacationTypeEntity.getCategory(),
            vacationTypeEntity.getMessageKey(),
            vacationTypeEntity.isRequiresApprovalToApply(),
            vacationTypeEntity.isRequiresApprovalToCancel(),
            vacationTypeEntity.getColor(),
            vacationTypeEntity.isVisibleToEveryone()
        );
    }

    @EventListener(ApplicationStartedEvent.class)
    void insertDefaultVacationTypes() {
        final long count = vacationTypeRepository.count();
        if (count == 0) {

            final VacationTypeEntity holiday = createVacationTypeEntity(1000L, true, HOLIDAY, "application.data.vacationType.holiday", true, true, YELLOW, false);
            final VacationTypeEntity specialleave = createVacationTypeEntity(2000L, true, SPECIALLEAVE, "application.data.vacationType.specialleave", true, true, YELLOW, false);
            final VacationTypeEntity unpaidleave = createVacationTypeEntity(3000L, true, UNPAIDLEAVE, "application.data.vacationType.unpaidleave", true, true, YELLOW, false);
            final VacationTypeEntity overtime = createVacationTypeEntity(4000L, true, OVERTIME, "application.data.vacationType.overtime", true, true, YELLOW, false);
            final VacationTypeEntity parentalLeave = createVacationTypeEntity(5000L, false, OTHER, "application.data.vacationType.parentalLeave", true, true, YELLOW, false);
            final VacationTypeEntity maternityProtection = createVacationTypeEntity(5001L, false, OTHER, "application.data.vacationType.maternityProtection", true, true, YELLOW, false);
            final VacationTypeEntity sabbatical = createVacationTypeEntity(5002L, false, OTHER, "application.data.vacationType.sabbatical", true, true, YELLOW, false);
            final VacationTypeEntity paidLeave = createVacationTypeEntity(5003L, false, OTHER, "application.data.vacationType.paidLeave", true, true, YELLOW, false);
            final VacationTypeEntity cure = createVacationTypeEntity(5004L, false, OTHER, "application.data.vacationType.cure", true, true, YELLOW, false);
            final VacationTypeEntity education = createVacationTypeEntity(5005L, false, OTHER, "application.data.vacationType.education", true, true, YELLOW, false);
            final VacationTypeEntity homeOffice = createVacationTypeEntity(5006L, false, OTHER, "application.data.vacationType.homeOffice", true, true, YELLOW, false);
            final VacationTypeEntity outOfOffice = createVacationTypeEntity(5007L, false, OTHER, "application.data.vacationType.outOfOffice", true, true, YELLOW, false);
            final VacationTypeEntity training = createVacationTypeEntity(5008L, false, OTHER, "application.data.vacationType.training", true, true, YELLOW, false);
            final VacationTypeEntity employmentBan = createVacationTypeEntity(5009L, false, OTHER, "application.data.vacationType.employmentBan", true, true, YELLOW, false);
            final VacationTypeEntity educationalLeave = createVacationTypeEntity(5010L, false, OTHER, "application.data.vacationType.educationalLeave", true, true, YELLOW, false);

            final List<VacationTypeEntity> vacationTypes = List.of(holiday, holiday, specialleave, unpaidleave, overtime, parentalLeave, maternityProtection, sabbatical, paidLeave, cure, education, homeOffice, outOfOffice, training, employmentBan, educationalLeave);
            final List<VacationTypeEntity> savesVacationTypes = vacationTypeRepository.saveAll(vacationTypes);
            LOG.info("Saved initial vacation types {}", savesVacationTypes);
        }
    }

    private static VacationTypeEntity createVacationTypeEntity(Long id, boolean active, VacationCategory category, String messageKey, boolean requiresApprovalToApply, boolean requiresApprovalToCancel, VacationTypeColor color, boolean visibleToEveryone) {
        final VacationTypeEntity vacationTypeEntity = new VacationTypeEntity();
        vacationTypeEntity.setId(id);
        vacationTypeEntity.setActive(active);
        vacationTypeEntity.setCategory(category);
        vacationTypeEntity.setMessageKey(messageKey);
        vacationTypeEntity.setRequiresApprovalToApply(requiresApprovalToApply);
        vacationTypeEntity.setRequiresApprovalToCancel(requiresApprovalToCancel);
        vacationTypeEntity.setColor(color);
        vacationTypeEntity.setVisibleToEveryone(visibleToEveryone);
        return vacationTypeEntity;
    }
}

