package org.synyx.urlaubsverwaltung.core.application.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.dao.VacationTypeDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;

import java.util.List;


@Service
public class VacationTypeServiceImpl implements VacationTypeService {

    private VacationTypeDAO vacationTypeDAO;

    @Autowired
    public VacationTypeServiceImpl(VacationTypeDAO vacationTypeDAO) {

        this.vacationTypeDAO = vacationTypeDAO;
    }

    @Override
    public List<VacationType> getVacationTypes() {

        return this.vacationTypeDAO.findAll();
    }
}
