package org.synyx.urlaubsverwaltung.absence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class AbsenceMappingServiceImpl implements AbsenceMappingService {

    private final AbsenceMappingRepository absenceMappingRepository;

    @Autowired
    public AbsenceMappingServiceImpl(AbsenceMappingRepository absenceMappingRepository) {
        this.absenceMappingRepository = absenceMappingRepository;
    }

    @Override
    public AbsenceMapping create(Integer id, AbsenceType absenceType, String eventId) {
        final AbsenceMapping absenceMapping = new AbsenceMapping(id, absenceType, eventId);
        absenceMappingRepository.save(absenceMapping);

        return absenceMapping;
    }

    @Override
    public void delete(AbsenceMapping absenceMapping) {
        absenceMappingRepository.delete(absenceMapping);
    }

    @Override
    public Optional<AbsenceMapping> getAbsenceByIdAndType(Integer id, AbsenceType absenceType) {
        return Optional.ofNullable(absenceMappingRepository.findAbsenceMappingByAbsenceIdAndAbsenceType(id, absenceType));
    }
}
