package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.stereotype.Service;

@Service
public class AbsenceMappingImportService {

    private final AbsenceMappingRepository absenceMappingRepository;

    AbsenceMappingImportService(AbsenceMappingRepository absenceMappingRepository) {
        this.absenceMappingRepository = absenceMappingRepository;
    }

    public void deleteAll() {
        absenceMappingRepository.deleteAll();
    }

    public AbsenceMapping importAbsenceMapping(AbsenceMapping absenceMapping) {
        return absenceMappingRepository.save(absenceMapping);
    }
}
