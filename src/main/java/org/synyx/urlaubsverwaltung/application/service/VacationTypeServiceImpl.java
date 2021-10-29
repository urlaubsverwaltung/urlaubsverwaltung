package org.synyx.urlaubsverwaltung.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.dao.VacationTypeRepository;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class VacationTypeServiceImpl implements VacationTypeService {

    private final VacationTypeRepository vacationTypeRepository;

    @Autowired
    public VacationTypeServiceImpl(VacationTypeRepository vacationTypeRepository) {
        this.vacationTypeRepository = vacationTypeRepository;
    }

    @Override
    public List<VacationType> getAllVacationTypes() {
        return vacationTypeRepository.findAll();
    }

    @Override
    public List<VacationType> getActiveVacationTypes() {
        return vacationTypeRepository.findByActiveIsTrue();
    }

    @Override
    public List<VacationType> getActiveVacationTypesWithoutCategory(VacationCategory vacationCategory) {
        return getActiveVacationTypes().stream()
            .filter(vacationType -> vacationType.getCategory() != vacationCategory)
            .collect(toList());
    }
}
