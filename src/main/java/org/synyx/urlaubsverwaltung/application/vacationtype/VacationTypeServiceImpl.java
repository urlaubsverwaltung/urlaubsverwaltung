package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
public class VacationTypeServiceImpl implements VacationTypeService {

    private final VacationTypeRepository vacationTypeRepository;

    @Autowired
    public VacationTypeServiceImpl(VacationTypeRepository vacationTypeRepository) {
        this.vacationTypeRepository = vacationTypeRepository;
    }

    @Override
    public Optional<VacationType> getById(Integer id) {
        return Optional.of(convert(vacationTypeRepository.getById(id)));
    }

    @Override
    public List<VacationType> getAllVacationTypes() {
        return vacationTypeRepository.findAll().stream()
            .map(convertToVacationType())
            .collect(toList());
    }

    @Override
    public List<VacationType> getActiveVacationTypes() {
        return vacationTypeRepository.findByActiveIsTrue().stream()
            .map(convertToVacationType())
            .collect(toList());
    }

    @Override
    public List<VacationType> getActiveVacationTypesWithoutCategory(VacationCategory vacationCategory) {
        return getActiveVacationTypes().stream()
            .filter(vacationType -> vacationType.getCategory() != vacationCategory)
            .collect(toList());
    }

    private Function<VacationTypeEntity, VacationType> convertToVacationType() {
        return vacationTypeEntity -> new VacationType(vacationTypeEntity.getId(), vacationTypeEntity.isActive(), vacationTypeEntity.getCategory(), vacationTypeEntity.getMessageKey());
    }

    public static VacationTypeEntity convert(VacationType vacationType) {
        final VacationTypeEntity vacationTypeEntity = new VacationTypeEntity();
        vacationTypeEntity.setId(vacationType.getId());
        vacationTypeEntity.setActive(vacationType.isActive());
        vacationTypeEntity.setCategory(vacationType.getCategory());
        vacationTypeEntity.setMessageKey(vacationType.getMessageKey());
        return vacationTypeEntity;
    }

    public static VacationType convert(VacationTypeEntity vacationTypeEntity) {
        return new VacationType(vacationTypeEntity.getId(), vacationTypeEntity.isActive(), vacationTypeEntity.getCategory(), vacationTypeEntity.getMessageKey());
    }
}
