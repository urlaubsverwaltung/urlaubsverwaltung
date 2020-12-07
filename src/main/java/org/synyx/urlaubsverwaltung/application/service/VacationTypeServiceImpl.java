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
    public List<VacationType> getVacationTypes() {
        return vacationTypeRepository.findAll();
    }

    @Override
    public List<VacationType> getVacationTypesFilteredBy(VacationCategory vacationCategory) {

        return getVacationTypes().stream()
            .filter(vt -> vt.getCategory() != vacationCategory)
            .collect(toList());
    }
}
