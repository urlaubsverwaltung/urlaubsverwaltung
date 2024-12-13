package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.springframework.stereotype.Service;

@Service
public class VacationTypeImportService {

    private final VacationTypeRepository vacationTypeRepository;

    VacationTypeImportService(VacationTypeRepository vacationTypeRepository) {
        this.vacationTypeRepository = vacationTypeRepository;
    }

    public void deleteAll() {
        vacationTypeRepository.deleteAll();
    }

    public VacationTypeEntity importVacationType(VacationTypeEntity vacationType) {
        return vacationTypeRepository.save(vacationType);
    }
}
