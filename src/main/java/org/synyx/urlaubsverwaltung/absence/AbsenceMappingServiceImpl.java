package org.synyx.urlaubsverwaltung.absence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class AbsenceMappingServiceImpl implements AbsenceMappingService {

    private final AbsenceMappingDAO absenceMappingDAO;

    @Autowired
    public AbsenceMappingServiceImpl(AbsenceMappingDAO absenceMappingDAO) {

        this.absenceMappingDAO = absenceMappingDAO;
    }

    @Override
    public AbsenceMapping create(Integer id, AbsenceType absenceType, String eventId) {

        AbsenceMapping absenceMapping = new AbsenceMapping(id, absenceType, eventId);
        absenceMappingDAO.save(absenceMapping);

        return absenceMapping;
    }


    @Override
    public void delete(AbsenceMapping absenceMapping) {

        absenceMappingDAO.delete(absenceMapping);
    }


    @Override
    public Optional<AbsenceMapping> getAbsenceByIdAndType(Integer id, AbsenceType absenceType) {

        return Optional.ofNullable(absenceMappingDAO.findAbsenceMappingByAbsenceIdAndAbsenceType(id, absenceType));
    }
}
