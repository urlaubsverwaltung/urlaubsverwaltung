package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Deprecated(since = "4.26.0", forRemoval = true)
@Service
public class AbsenceMappingServiceImpl implements AbsenceMappingService {

    private final AbsenceMappingRepository absenceMappingRepository;

    @Autowired
    public AbsenceMappingServiceImpl(AbsenceMappingRepository absenceMappingRepository) {
        this.absenceMappingRepository = absenceMappingRepository;
    }

    @Override
    public AbsenceMapping create(Integer id, AbsenceMappingType absenceMappingType, String eventId) {
        final AbsenceMapping absenceMapping = new AbsenceMapping(id, absenceMappingType, eventId);
        return absenceMappingRepository.save(absenceMapping);
    }

    @Override
    public void delete(AbsenceMapping absenceMapping) {
        absenceMappingRepository.delete(absenceMapping);
    }

    @Override
    public Optional<AbsenceMapping> getAbsenceByIdAndType(Integer id, AbsenceMappingType absenceMappingType) {
        return absenceMappingRepository.findAbsenceMappingByAbsenceIdAndAbsenceMappingType(id, absenceMappingType);
    }
}
