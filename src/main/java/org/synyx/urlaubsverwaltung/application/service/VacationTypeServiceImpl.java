package org.synyx.urlaubsverwaltung.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.dao.VacationTypeDAO;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;

import java.util.List;

import static java.util.stream.Collectors.toList;


@Service
public class VacationTypeServiceImpl implements VacationTypeService {

    private final VacationTypeDAO vacationTypeDAO;

    @Autowired
    public VacationTypeServiceImpl(VacationTypeDAO vacationTypeDAO) {

        this.vacationTypeDAO = vacationTypeDAO;
    }

    @Override
    public List<VacationType> getVacationTypes() {

        return vacationTypeDAO.findAll();
    }

    @Override
    public List<VacationType> getVacationTypesFilteredBy(VacationCategory vacationCategory) {

        return getVacationTypes().stream()
            .filter(vt -> vt.getCategory() != vacationCategory)
            .collect(toList());
    }
}
