package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AbsenceMappingExportService {

    private final AbsenceMappingRepository absenceMappingRepository;

    AbsenceMappingExportService(AbsenceMappingRepository absenceMappingRepository) {
        this.absenceMappingRepository = absenceMappingRepository;
    }

    public List<AbsenceMapping> getAbsenceMappings() {
        return absenceMappingRepository.findAllByOrderByIdAsc();
    }
}
