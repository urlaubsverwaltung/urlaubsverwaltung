package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
import static org.synyx.urlaubsverwaltung.settings.SupportedLanguages.compareSupportedLanguageLocale;

@Service
@Transactional
public class VacationTypeServiceImpl implements VacationTypeService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final VacationTypeRepository vacationTypeRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MessageSource messageSource;

    @Autowired
    VacationTypeServiceImpl(VacationTypeRepository vacationTypeRepository,
                            ApplicationEventPublisher applicationEventPublisher,
                            MessageSource messageSource) {
        this.vacationTypeRepository = vacationTypeRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.messageSource = messageSource;
    }

    @Override
    public Optional<VacationType<?>> getById(Long id) {
        return Optional.of(convert(vacationTypeRepository.getReferenceById(id), messageSource));
    }

    @Override
    public List<VacationType<?>> getAllVacationTypes() {
        return vacationTypeRepository.findAll(Sort.by("id")).stream()
            .map(vacationTypeEntity -> convert(vacationTypeEntity, messageSource))
            .collect(toList());
    }

    @Override
    public List<VacationType<?>> getActiveVacationTypes() {
        return vacationTypeRepository.findByActiveIsTrueOrderById().stream()
            .map(vacationTypeEntity -> convert(vacationTypeEntity, messageSource))
            .collect(toList());
    }

    @Override
    public List<VacationType<?>> getActiveVacationTypesWithoutCategory(VacationCategory vacationCategory) {
        return getActiveVacationTypes().stream()
            .filter(vacationType -> vacationType.getCategory() != vacationCategory)
            .collect(toList());
    }

    @Override
    public void updateVacationTypes(List<VacationTypeUpdate> vacationTypeUpdates) {

        final Map<Long, VacationTypeUpdate> byId = vacationTypeUpdates.stream()
            .collect(toMap(VacationTypeUpdate::getId, vacationTypeUpdate -> vacationTypeUpdate));

        final List<VacationTypeEntity> updatedEntities = vacationTypeRepository.findAllById(byId.keySet())
            .stream()
            .map(vacationTypeEntity -> convert(vacationTypeEntity, messageSource))
            .map(vacationType -> {
                final VacationTypeUpdate vacationTypeUpdate = byId.get(vacationType.getId());
                switch (vacationType) {
                    case ProvidedVacationType providedVacationType -> {
                        return Optional.of(updateProvidedVacationType(providedVacationType, vacationTypeUpdate));
                    }
                    case CustomVacationType customVacationType -> {
                        return Optional.of(updateCustomVacationType(customVacationType, vacationTypeUpdate));
                    }
                    default -> {
                        LOG.error("cannot handle vacationTypeUpdate={} for unknown vacationType implementation.", vacationTypeUpdate);
                        return Optional.empty();
                    }
                }
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(VacationType.class::cast)
            .map(VacationTypeServiceImpl::convert)
            .toList();

        vacationTypeRepository.saveAll(updatedEntities).stream()
            .map(entity -> convert(entity, messageSource))
            .map(VacationTypeUpdatedEvent::of)
            .forEach(applicationEventPublisher::publishEvent);
    }

    @Override
    public void createVacationTypes(Collection<VacationType<?>> vacationTypes) {

        final List<VacationTypeEntity> newEntities = vacationTypes.stream()
            .map(VacationTypeServiceImpl::convert)
            .filter(entity -> {
                final boolean isNew = entity.getId() == null;
                if (!isNew) {
                    LOG.info("skipping vacationType={} from list of newly created vacation types due to existing id.", entity);
                }
                return isNew;
            })
            .toList();

        vacationTypeRepository.saveAll(newEntities).stream()
            .map(entity -> convert(entity, messageSource))
            .map(VacationTypeCreatedEvent::of)
            .forEach(applicationEventPublisher::publishEvent);
    }

    public static VacationTypeEntity convert(VacationType<?> vacationType) {
        return switch (vacationType) {
            case ProvidedVacationType providedVacationType -> convertProvidedVacationType(providedVacationType);
            case CustomVacationType customVacationType -> convertCustomVacationType(customVacationType);
            default -> throw new IllegalStateException("could not convert unknown vacationType.");
        };
    }

    public static VacationType<?> convert(VacationTypeEntity vacationTypeEntity, MessageSource messageSource) {
        if (vacationTypeEntity.isCustom()) {
            return convertCustomVacationType(vacationTypeEntity, messageSource);
        } else {
            return convertProvidedVacationType(vacationTypeEntity, messageSource);
        }
    }

    @Override
    public void insertDefaultVacationTypes() {
        final long count = vacationTypeRepository.count();

        if (count == 0) {
            LOG.info("No initial vacation types exists - going to create them.");
            final List<VacationType<?>> vacationTypes = getInitialVacationTypes();
            createVacationTypes(vacationTypes);
            LOG.info("Saved {} initial vacation types", vacationTypes.size());
        }
    }

    private List<VacationType<?>> getInitialVacationTypes() {
        final ProvidedVacationType holiday = createProvidedVacationType(true, HOLIDAY, "application.data.vacationType.holiday", true, true, YELLOW, false);
        final ProvidedVacationType specialleave = createProvidedVacationType(true, SPECIALLEAVE, "application.data.vacationType.specialleave", true, true, YELLOW, false);
        final ProvidedVacationType unpaidleave = createProvidedVacationType(true, UNPAIDLEAVE, "application.data.vacationType.unpaidleave", true, true, YELLOW, false);
        final ProvidedVacationType overtime = createProvidedVacationType(true, OVERTIME, "application.data.vacationType.overtime", true, true, YELLOW, false);
        final ProvidedVacationType parentalLeave = createProvidedVacationType(false, OTHER, "application.data.vacationType.parentalLeave", true, true, YELLOW, false);
        final ProvidedVacationType maternityProtection = createProvidedVacationType(false, OTHER, "application.data.vacationType.maternityProtection", true, true, YELLOW, false);
        final ProvidedVacationType sabbatical = createProvidedVacationType(false, OTHER, "application.data.vacationType.sabbatical", true, true, YELLOW, false);
        final ProvidedVacationType paidLeave = createProvidedVacationType(false, OTHER, "application.data.vacationType.paidLeave", true, true, YELLOW, false);
        final ProvidedVacationType cure = createProvidedVacationType(false, OTHER, "application.data.vacationType.cure", true, true, YELLOW, false);
        final ProvidedVacationType education = createProvidedVacationType(false, OTHER, "application.data.vacationType.education", true, true, YELLOW, false);
        final ProvidedVacationType homeOffice = createProvidedVacationType(false, OTHER, "application.data.vacationType.homeOffice", true, true, YELLOW, false);
        final ProvidedVacationType outOfOffice = createProvidedVacationType(false, OTHER, "application.data.vacationType.outOfOffice", true, true, YELLOW, false);
        final ProvidedVacationType training = createProvidedVacationType(false, OTHER, "application.data.vacationType.training", true, true, YELLOW, false);
        final ProvidedVacationType employmentBan = createProvidedVacationType(false, OTHER, "application.data.vacationType.employmentBan", true, true, YELLOW, false);
        final ProvidedVacationType educationalLeave = createProvidedVacationType(false, OTHER, "application.data.vacationType.educationalLeave", true, true, YELLOW, false);

        return List.of(holiday, specialleave, unpaidleave, overtime, parentalLeave, maternityProtection, sabbatical, paidLeave, cure, education, homeOffice, outOfOffice, training, employmentBan, educationalLeave);
    }

    private ProvidedVacationType createProvidedVacationType(boolean active, VacationCategory category, String messageKey, boolean requiresApprovalToApply, boolean requiresApprovalToCancel, VacationTypeColor color, boolean visibleToEveryone) {
        return ProvidedVacationType.builder(messageSource)
            .id(null)
            .active(active)
            .category(category)
            .messageKey(messageKey)
            .requiresApprovalToApply(requiresApprovalToApply)
            .requiresApprovalToCancel(requiresApprovalToCancel)
            .color(color)
            .visibleToEveryone(visibleToEveryone)
            .build();
    }

    private static CustomVacationType updateCustomVacationType(CustomVacationType customVacationType, VacationTypeUpdate vacationTypeUpdate) {

        final List<VacationTypeLabel> labels = vacationTypeUpdate.getLabels()
            .orElseThrow(() -> new IllegalStateException("expected vacationTypeLabels to be defined. cannot update %s".formatted(customVacationType)));

        return CustomVacationType.builder(customVacationType)
            .active(vacationTypeUpdate.isActive())
            .requiresApprovalToApply(vacationTypeUpdate.isRequiresApprovalToApply())
            .requiresApprovalToCancel(vacationTypeUpdate.isRequiresApprovalToCancel())
            .color(vacationTypeUpdate.getColor())
            .visibleToEveryone(vacationTypeUpdate.isVisibleToEveryone())
            .labels(labels)
            .build();
    }

    private static ProvidedVacationType updateProvidedVacationType(ProvidedVacationType providedVacationType,
                                                                   VacationTypeUpdate vacationTypeUpdate) {
        // updating label of ProvidedVacationType is not yet implemented.
        // therefore no messageSource required. we can just use the given providedVacationType instance.
        // as soon as the label of a ProvidedVacationType can be updated, we get the new label from the VacationTypeUpdate.
        return ProvidedVacationType.builder(providedVacationType)
            .active(vacationTypeUpdate.isActive())
            .requiresApprovalToApply(vacationTypeUpdate.isRequiresApprovalToApply())
            .requiresApprovalToCancel(vacationTypeUpdate.isRequiresApprovalToCancel())
            .color(vacationTypeUpdate.getColor())
            .visibleToEveryone(vacationTypeUpdate.isVisibleToEveryone())
            .messageKey(providedVacationType.getMessageKey())
            .build();
    }

    private static CustomVacationType convertCustomVacationType(VacationTypeEntity customVacationTypeEntity, MessageSource messageSource) {
        return CustomVacationType.builder(messageSource)
            .id(customVacationTypeEntity.getId())
            .active(customVacationTypeEntity.isActive())
            .category(customVacationTypeEntity.getCategory())
            .requiresApprovalToApply(customVacationTypeEntity.isRequiresApprovalToApply())
            .requiresApprovalToCancel(customVacationTypeEntity.isRequiresApprovalToCancel())
            .color(customVacationTypeEntity.getColor())
            .visibleToEveryone(customVacationTypeEntity.isVisibleToEveryone())
            .labels(vacationTypeLabels(customVacationTypeEntity.getLabelByLocale()))
            .build();
    }

    private static List<VacationTypeLabel> vacationTypeLabels(Map<Locale, String> labelByLocale) {
        return labelByLocale.entrySet()
            .stream()
            .map(entry -> new VacationTypeLabel(entry.getKey(), entry.getValue()))
            .sorted((o1, o2) -> compareSupportedLanguageLocale(o1.locale(), o2.locale()))
            .toList();
    }

    private static ProvidedVacationType convertProvidedVacationType(VacationTypeEntity providedVacationType,
                                                                    MessageSource messageSource) {
        return ProvidedVacationType.builder(messageSource)
            .id(providedVacationType.getId())
            .active(providedVacationType.isActive())
            .category(providedVacationType.getCategory())
            .requiresApprovalToApply(providedVacationType.isRequiresApprovalToApply())
            .requiresApprovalToCancel(providedVacationType.isRequiresApprovalToCancel())
            .color(providedVacationType.getColor())
            .visibleToEveryone(providedVacationType.isVisibleToEveryone())
            .messageKey(providedVacationType.getMessageKey())
            .build();
    }

    private static VacationTypeEntity convertProvidedVacationType(ProvidedVacationType providedVacationType) {
        final VacationTypeEntity vacationTypeEntity = toEntityBase(providedVacationType);
        vacationTypeEntity.setCustom(false);
        vacationTypeEntity.setMessageKey(providedVacationType.getMessageKey());
        return vacationTypeEntity;
    }

    private static VacationTypeEntity convertCustomVacationType(CustomVacationType customVacationType) {

        final Map<Locale, String> labelByLocale = customVacationType.labels()
            .stream()
            .collect(toMap(VacationTypeLabel::locale, VacationTypeLabel::label));

        final VacationTypeEntity vacationTypeEntity = toEntityBase(customVacationType);
        vacationTypeEntity.setCustom(true);
        vacationTypeEntity.setLabelByLocale(labelByLocale);

        return vacationTypeEntity;
    }

    private static VacationTypeEntity toEntityBase(VacationType<?> vacationType) {
        final VacationTypeEntity vacationTypeEntity = new VacationTypeEntity();
        vacationTypeEntity.setId(vacationType.getId());
        vacationTypeEntity.setActive(vacationType.isActive());
        vacationTypeEntity.setCategory(vacationType.getCategory());
        vacationTypeEntity.setRequiresApprovalToApply(vacationType.isRequiresApprovalToApply());
        vacationTypeEntity.setRequiresApprovalToCancel(vacationType.isRequiresApprovalToCancel());
        vacationTypeEntity.setColor(vacationType.getColor());
        vacationTypeEntity.setVisibleToEveryone(vacationType.isVisibleToEveryone());
        return vacationTypeEntity;
    }
}

